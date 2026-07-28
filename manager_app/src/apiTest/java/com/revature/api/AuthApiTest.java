package com.revature.api;

import static com.revature.api.ManagerApiTestSupport.baseUrl;
import static com.revature.api.ManagerApiTestSupport.loginAsManager;
import static com.revature.api.ManagerApiTestSupport.managerApi;
import static com.revature.api.ManagerApiTestSupport.newSessionFilter;
import static com.revature.api.ManagerApiTestSupport.startTestApp;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.models.ManagerSummary;
import com.revature.models.User;

import io.javalin.Javalin;
import io.restassured.filter.session.SessionFilter;
import io.restassured.specification.RequestSpecification;

// Exercises the manager portal's real HTTP routes (not just the DAO layer) to cover the login
// role check and the session-based authorization guard on every protected /api/* endpoint.
@ExtendWith(MockitoExtension.class)
public class AuthApiTest {

    @Mock
    private AuthDAO authDAO;

    @Mock
    private ApprovalDAO approvalDAO;

    @Mock
    private ManagerPortalDAO portalDAO;

    private Javalin app;
    private String baseUrl;
    private SessionFilter sessionFilter;

    @BeforeEach
    public void startApp() {
        app = startTestApp(authDAO, approvalDAO, portalDAO);
        baseUrl = baseUrl(app);
        sessionFilter = newSessionFilter();
    }

    @AfterEach
    public void stopApp() {
        app.stop();
    }

    private RequestSpecification api() {
        return managerApi(baseUrl, sessionFilter);
    }

    @Test
    public void loginWithValidManagerCredentialsReturns200AndManagerDetails() {
        User manager = new User(1, "jsmith", "password123", "manager");

        loginAsManager(baseUrl, sessionFilter, authDAO, manager)
        .then()
            .statusCode(200)
            .body("username", equalTo("jsmith"))
            .body("managerId", equalTo(1));
    }

    @Test
    public void loginWithNonManagerRoleReturns401() {
        User employee = new User(2, "asmith", "password123", "employee");
        when(authDAO.login(anyString(), anyString())).thenReturn(employee);

        api()
            .contentType("application/json")
            .body("{\"username\":\"asmith\",\"password\":\"password123\"}")
        .post("/api/login")
        .then()
            .statusCode(401)
            .body("message", equalTo("Invalid manager credentials."));
    }

    @Test
    public void loginWithInvalidCredentialsReturns401() {
        when(authDAO.login(anyString(), anyString())).thenReturn(null);

        api()
            .contentType("application/json")
            .body("{\"username\":\"jsmith\",\"password\":\"wrongpassword\"}")
        .post("/api/login")
        .then()
            .statusCode(401)
            .body("message", equalTo("Invalid manager credentials."));
    }

    @Test
    public void loginWithMissingUsernameReturns400() {
        api()
            .contentType("application/json")
            .body("{\"password\":\"password123\"}")
        .post("/api/login")
        .then()
            .statusCode(400)
            .body("message", equalTo("Username and password are required."));
    }

    @Test
    public void loginWithMissingPasswordReturns400() {
        api()
            .contentType("application/json")
            .body("{\"username\":\"jsmith\"}")
        .post("/api/login")
        .then()
            .statusCode(400)
            .body("message", equalTo("Username and password are required."));
    }

    @Test
    public void sessionReturns401WhenNoActiveSession() {
        api()
        .get("/api/session")
        .then()
            .statusCode(401)
            .body("message", equalTo("No active manager session."));
    }

    @Test
    public void sessionReturnsManagerDetailsAfterLogin() {
        User manager = new User(1, "jsmith", "password123", "manager");
        loginAsManager(baseUrl, sessionFilter, authDAO, manager);

        api()
        .get("/api/session")
        .then()
            .statusCode(200)
            .body("username", equalTo("jsmith"))
            .body("managerId", equalTo(1));
    }

    @Test
    public void logoutInvalidatesSessionSoSessionCheckReturns401() {
        User manager = new User(1, "jsmith", "password123", "manager");
        loginAsManager(baseUrl, sessionFilter, authDAO, manager);

        api()
            .contentType("application/json")
            .body("{}")
        .post("/api/logout")
        .then()
            .statusCode(200);

        api()
        .get("/api/session")
        .then()
            .statusCode(401);
    }

    @Test
    public void summaryRequiresAnActiveManagerSession() {
        api()
        .get("/api/summary")
        .then()
            .statusCode(401)
            .body("message", equalTo("Please log in as a manager."));
    }

    @Test
    public void approvalsRequiresAnActiveManagerSession() {
        api()
        .get("/api/approvals")
        .then()
            .statusCode(401);
    }

    @Test
    public void reportsRequiresAnActiveManagerSession() {
        api()
        .get("/api/reports")
        .then()
            .statusCode(401);
    }

    @Test
    public void optionsRequiresAnActiveManagerSession() {
        api()
        .get("/api/options")
        .then()
            .statusCode(401);
    }

    @Test
    public void approvalUpdateRequiresAnActiveManagerSession() {
        api()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"comment\":\"ok\",\"reviewDate\":\"2026-07-28\"}")
        .post("/api/approvals/1")
        .then()
            .statusCode(401);
    }

    @Test
    public void loginPageRedirectsToLoginHtmlWhenNotLoggedIn() {
        api()
            .redirects().follow(false)
        .get("/login")
        .then()
            .statusCode(302)
            .header("Location", equalTo("/login.html"));
    }

    @Test
    public void loginPageRedirectsToDashboardWhenAlreadyLoggedIn() {
        User manager = new User(1, "jsmith", "password123", "manager");
        loginAsManager(baseUrl, sessionFilter, authDAO, manager);

        api()
            .redirects().follow(false)
        .get("/login")
        .then()
            .statusCode(302)
            .header("Location", equalTo("/dashboard"));
    }

    @Test
    public void dashboardRedirectsToLoginWhenNotLoggedIn() {
        api()
            .redirects().follow(false)
        .get("/dashboard")
        .then()
            .statusCode(302)
            .header("Location", equalTo("/login"));
    }

    @Test
    public void dashboardRedirectsToDashboardHtmlWhenLoggedIn() {
        User manager = new User(1, "jsmith", "password123", "manager");
        loginAsManager(baseUrl, sessionFilter, authDAO, manager);

        api()
            .redirects().follow(false)
        .get("/dashboard")
        .then()
            .statusCode(302)
            .header("Location", equalTo("/dashboard.html"));
    }

    @Test
    public void summaryReturns200WithMappedDataOnceLoggedIn() {
        User manager = new User(1, "jsmith", "password123", "manager");
        loginAsManager(baseUrl, sessionFilter, authDAO, manager);
        when(portalDAO.getSummary()).thenReturn(new ManagerSummary(10, 4, 5, 1));

        api()
        .get("/api/summary")
        .then()
            .statusCode(200)
            .body("total", equalTo(10))
            .body("pending", equalTo(4))
            .body("approved", equalTo(5))
            .body("denied", equalTo(1));
    }
}
