package com.revature;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.models.Approval;
import com.revature.models.ApprovalUpdateRequest;
import com.revature.models.LoginDTO;
import com.revature.models.User;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Main {

    private static final int PORT = 8080;
    private static final Set<String> ALLOWED_STATUSES = Set.of("pending", "approved", "denied");

    public static void main(String[] args) {
        Javalin app = createApp(new AuthDAO(), new ApprovalDAO(), new ManagerPortalDAO());

        app.start(PORT);
        System.out.println("Manager web UI running at http://localhost:" + PORT);
    }

    // Extracted so tests can build the app with injected (mock) DAOs instead of hitting a real database.
    public static Javalin createApp(AuthDAO authDAO, ApprovalDAO approvalDAO, ManagerPortalDAO portalDAO) {
        return Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            config.routes.apiBuilder(() -> {
                get("/", ctx -> ctx.redirect("/login"));
                get("/login", ctx -> {
                    if (isManagerLoggedIn(ctx)) {
                        ctx.redirect("/dashboard");
                        return;
                    }
                    ctx.redirect("/login.html");
                });
                get("/dashboard", ctx -> {
                    if (!isManagerLoggedIn(ctx)) {
                        ctx.redirect("/login");
                        return;
                    }
                    ctx.redirect("/dashboard.html");
                });

                post("/api/login", ctx -> {
                    LoginDTO loginRequest = ctx.bodyAsClass(LoginDTO.class);

                    if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                        ctx.status(400).json(Map.of("message", "Username and password are required."));
                        return;
                    }

                    User user = authDAO.login(loginRequest.getUsername().trim(), loginRequest.getPassword());

                    if (user == null || !"manager".equalsIgnoreCase(user.getRole())) {
                        ctx.status(401).json(Map.of("message", "Invalid manager credentials."));
                        return;
                    }

                    ctx.sessionAttribute("managerId", user.getUser_id());
                    ctx.sessionAttribute("managerUsername", user.getUsername());

                    ctx.json(Map.of(
                        "message", "Login successful.",
                        "username", user.getUsername(),
                        "managerId", user.getUser_id()
                    ));
                });

                get("/api/session", ctx -> {
                    Integer managerId = ctx.sessionAttribute("managerId");
                    String username = ctx.sessionAttribute("managerUsername");

                    if (managerId == null || username == null) {
                        ctx.status(401).json(Map.of("message", "No active manager session."));
                        return;
                    }

                    ctx.json(Map.of(
                        "managerId", managerId,
                        "username", username
                    ));
                });

                post("/api/logout", ctx -> {
                    if (ctx.req().getSession(false) != null) {
                        ctx.req().getSession().invalidate();
                    }
                    ctx.json(Map.of("message", "Logged out."));
                });

                get("/api/options", ctx -> {
                    if (!requireManagerApi(ctx)) {
                        return;
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("employees", portalDAO.getEmployees());
                    response.put("managers", portalDAO.getManagers());
                    ctx.json(response);
                });

                get("/api/summary", ctx -> {
                    if (!requireManagerApi(ctx)) {
                        return;
                    }
                    ctx.json(portalDAO.getSummary());
                });

                get("/api/approvals", ctx -> {
                    if (!requireManagerApi(ctx)) {
                        return;
                    }

                    String status = normalizeFilter(ctx.queryParam("status"));
                    String employee = normalizeFilter(ctx.queryParam("employee"));
                    String reviewer = normalizeFilter(ctx.queryParam("reviewer"));
                    String keyword = normalizeFilter(ctx.queryParam("keyword"));

                    ctx.json(portalDAO.getApprovalRecords(status, employee, reviewer, keyword));
                });

                get("/api/reports", ctx -> {
                    if (!requireManagerApi(ctx)) {
                        return;
                    }

                    String status = normalizeFilter(ctx.queryParam("status"));
                    String employee = normalizeFilter(ctx.queryParam("employee"));
                    String startDate = normalizeFilter(ctx.queryParam("startDate"));
                    String endDate = normalizeFilter(ctx.queryParam("endDate"));
                    String keyword = normalizeFilter(ctx.queryParam("keyword"));

                    ctx.json(portalDAO.getExpenseReports(status, employee, startDate, endDate, keyword));
                });

                post("/api/approvals/{approvalId}", ctx -> {
                    if (!requireManagerApi(ctx)) {
                        return;
                    }

                    int approvalId;
                    try {
                        approvalId = Integer.parseInt(ctx.pathParam("approvalId"));
                    } catch (NumberFormatException exception) {
                        ctx.status(400).json(Map.of("message", "Approval id must be a number."));
                        return;
                    }

                    ApprovalUpdateRequest updateRequest = ctx.bodyAsClass(ApprovalUpdateRequest.class);
                    Approval existingApproval = approvalDAO.getApprovalByID(approvalId);

                    if (existingApproval == null) {
                        ctx.status(404).json(Map.of("message", "Approval not found."));
                        return;
                    }

                    String status = normalizeFilter(updateRequest.getStatus());
                    if (status == null || !ALLOWED_STATUSES.contains(status)) {
                        ctx.status(400).json(Map.of("message", "Status must be pending, approved, or denied."));
                        return;
                    }

                    String comment = updateRequest.getComment() == null ? "" : updateRequest.getComment().trim();
                    String reviewDate = normalizeFilter(updateRequest.getReviewDate());
                    if (reviewDate == null) {
                        reviewDate = LocalDate.now().toString();
                    }

                    Integer managerId = ctx.sessionAttribute("managerId");
                    Approval updatedApproval = new Approval(
                        existingApproval.getApproval_id(),
                        existingApproval.getExpense_id(),
                        status,
                        managerId,
                        comment,
                        reviewDate
                    );

                    approvalDAO.updateApproval(updatedApproval);
                    ctx.json(Map.of("message", "Approval updated."));
                });
            });

            config.routes.exception(Exception.class, (exception, ctx) -> {
                exception.printStackTrace();
                ctx.status(500).json(Map.of("message", "Something went wrong on the manager side."));
            });
        });
    }

    private static boolean isManagerLoggedIn(io.javalin.http.Context ctx) {
        Integer managerId = ctx.sessionAttribute("managerId");
        return managerId != null;
    }

    private static boolean requireManagerApi(io.javalin.http.Context ctx) {
        if (isManagerLoggedIn(ctx)) {
            return true;
        }

        ctx.status(401).json(Map.of("message", "Please log in as a manager."));
        return false;
    }

    private static String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}
