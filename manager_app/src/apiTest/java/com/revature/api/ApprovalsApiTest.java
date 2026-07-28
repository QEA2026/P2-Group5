package com.revature.api;

import static com.revature.api.ManagerApiTestSupport.baseUrl;
import static com.revature.api.ManagerApiTestSupport.loginAsManager;
import static com.revature.api.ManagerApiTestSupport.managerApi;
import static com.revature.api.ManagerApiTestSupport.newSessionFilter;
import static com.revature.api.ManagerApiTestSupport.startTestApp;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.models.Approval;
import com.revature.models.ManagerExpenseApprovalRecord;
import com.revature.models.User;

import io.javalin.Javalin;
import io.restassured.filter.session.SessionFilter;
import io.restassured.specification.RequestSpecification;

// Covers GET /api/approvals and POST /api/approvals/{approvalId}. The harness below mirrors
// AuthApiTest/ReportsSummaryOptionsApiTest: a real Javalin app on a random port, mock DAOs, and a
// session-filtered REST Assured request spec that's already logged in as a manager by the time
// each test runs.
@ExtendWith(MockitoExtension.class)
public class ApprovalsApiTest {

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
    public void startAppAndLogIn() {
        app = startTestApp(authDAO, approvalDAO, portalDAO);
        baseUrl = baseUrl(app);
        sessionFilter = newSessionFilter();

        User manager = new User(1, "jsmith", "password123", "manager");
        loginAsManager(baseUrl, sessionFilter, authDAO, manager);
    }

    @AfterEach
    public void stopApp() {
        app.stop();
    }

    private RequestSpecification api() {
        return managerApi(baseUrl, sessionFilter);
    }

    @Test
    public void getApprovalsAppliesQueryParamFiltersAndReturnsMappedRecords() {
        when(portalDAO.getApprovalRecords("pending", "jdoe", "mgr1", "taxi"))
            .thenReturn(new ArrayList<>(List.of(
                new ManagerExpenseApprovalRecord(
                    1, 10, 20, "jdoe", 45.50, "Taxi", "2026-07-01",
                    "pending", 5, "mgr1", "needs review", null
                )
            )));

        api()
            .queryParam("status", "PENDING")
            .queryParam("employee", "JDoe")
            .queryParam("reviewer", "Mgr1")
            .queryParam("keyword", "Taxi")
        .get("/api/approvals")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].employeeUsername", equalTo("jdoe"))
            .body("[0].status", equalTo("pending"));

        verify(portalDAO).getApprovalRecords("pending", "jdoe", "mgr1", "taxi");
    }

    @Test
    public void getApprovalsWithNoFiltersPassesAllNullFiltersToPortalDAO() {
        when(portalDAO.getApprovalRecords(null, null, null, null)).thenReturn(new ArrayList<>());

        api()
        .get("/api/approvals")
        .then()
            .statusCode(200);

        verify(portalDAO).getApprovalRecords(null, null, null, null);
    }

    @Test
    public void updateApprovalValidatesStatusAndPersistsChange() {
        Approval existingApproval = new Approval(1, 10, "pending", 5, "needs review", "2026-07-01");
        when(approvalDAO.getApprovalByID(1)).thenReturn(existingApproval);

        api()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"comment\":\"ok\",\"reviewDate\":\"2026-07-28\"}")
        .post("/api/approvals/1")
        .then()
            .statusCode(200)
            .body("message", equalTo("Approval updated."));

        ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalDAO).updateApproval(captor.capture());
        Approval saved = captor.getValue();

        assertEquals(1, saved.getApproval_id());       // carried over from existingApproval
        assertEquals(10, saved.getExpense_id());       // carried over from existingApproval
        assertEquals("approved", saved.getStatus());   // from the request body
        assertEquals(1, saved.getReviewer_id());       // the logged-in manager's id, NOT existingApproval's old reviewer (5)
        assertEquals("ok", saved.getComment());
        assertEquals("2026-07-28", saved.getReview_date());
    }

    @Test
    public void updateApprovalReturns400WhenApprovalIdIsNotNumeric() {
        api()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"comment\":\"ok\",\"reviewDate\":\"2026-07-28\"}")
        .post("/api/approvals/abc")
        .then()
            .statusCode(400)
            .body("message", equalTo("Approval id must be a number."));
    }

    @Test
    public void updateApprovalReturns404WhenApprovalNotFound() {
        when(approvalDAO.getApprovalByID(1)).thenReturn(null);

        api()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"comment\":\"ok\",\"reviewDate\":\"2026-07-28\"}")
        .post("/api/approvals/1")
        .then()
            .statusCode(404)
            .body("message", equalTo("Approval not found."));
    }

    @Test
    public void updateApprovalReturns400WhenStatusIsInvalid() {
        Approval existingApproval = new Approval(1, 10, "pending", 5, "needs review", "2026-07-01");
        when(approvalDAO.getApprovalByID(1)).thenReturn(existingApproval);

        api()
            .contentType("application/json")
            .body("{\"status\":\"archived\",\"comment\":\"ok\",\"reviewDate\":\"2026-07-28\"}")
        .post("/api/approvals/1")
        .then()
            .statusCode(400)
            .body("message", equalTo("Status must be pending, approved, or denied."));
    }

    @Test
    public void updateApprovalDefaultsReviewDateToTodayWhenOmitted() {
        Approval existingApproval = new Approval(1, 10, "pending", 5, "needs review", "2026-07-01");
        when(approvalDAO.getApprovalByID(1)).thenReturn(existingApproval);

        api()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"comment\":\"ok\"}")
        .post("/api/approvals/1")
        .then()
            .statusCode(200);

        ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalDAO).updateApproval(captor.capture());
        assertEquals(LocalDate.now().toString(), captor.getValue().getReview_date());
    }

    @Test
    public void updateApprovalDefaultsCommentToEmptyStringWhenOmitted() {
        Approval existingApproval = new Approval(1, 10, "pending", 5, "needs review", "2026-07-01");
        when(approvalDAO.getApprovalByID(1)).thenReturn(existingApproval);

        api()
            .contentType("application/json")
            .body("{\"status\":\"approved\",\"reviewDate\":\"2026-07-28\"}")
        .post("/api/approvals/1")
        .then()
            .statusCode(200);

        ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
        verify(approvalDAO).updateApproval(captor.capture());
        assertEquals("", captor.getValue().getComment());
    }
}
