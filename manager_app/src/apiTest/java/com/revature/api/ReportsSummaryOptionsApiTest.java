package com.revature.api;

import static com.revature.api.ManagerApiTestSupport.baseUrl;
import static com.revature.api.ManagerApiTestSupport.loginAsManager;
import static com.revature.api.ManagerApiTestSupport.managerApi;
import static com.revature.api.ManagerApiTestSupport.newSessionFilter;
import static com.revature.api.ManagerApiTestSupport.startTestApp;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.models.ManagerExpenseApprovalRecord;
import com.revature.models.ManagerSummary;
import com.revature.models.User;

import io.javalin.Javalin;
import io.restassured.filter.session.SessionFilter;
import io.restassured.specification.RequestSpecification;

// Covers /api/summary, /api/reports, and /api/options: query-param normalization (trim/lowercase
// applied before hitting the DAO) and the JSON shape returned to the frontend. AuthApiTest already
// covers the 401 guard on these routes, so every test here logs in first.
@ExtendWith(MockitoExtension.class)
public class ReportsSummaryOptionsApiTest {

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

    private ManagerExpenseApprovalRecord sampleRecord() {
        return new ManagerExpenseApprovalRecord(
            1, 10, 20, "jdoe", 45.50, "Taxi", "2026-07-01",
            "pending", null, null, null, null
        );
    }

    @Test
    public void summaryReturnsCountsMappedFromPortalDAO() {
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

    @Test
    public void reportsAppliesAllFiltersNormalizedToLowercase() {
        when(portalDAO.getExpenseReports("pending", "jdoe", "2026-07-01", "2026-07-31", "taxi"))
            .thenReturn(new ArrayList<>(List.of(sampleRecord())));

        api()
            .queryParam("status", "PENDING")
            .queryParam("employee", "JDoe")
            .queryParam("startDate", "2026-07-01")
            .queryParam("endDate", "2026-07-31")
            .queryParam("keyword", "Taxi")
        .get("/api/reports")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].employeeUsername", equalTo("jdoe"))
            .body("[0].status", equalTo("pending"));

        verify(portalDAO).getExpenseReports("pending", "jdoe", "2026-07-01", "2026-07-31", "taxi");
    }

    @Test
    public void reportsWithNoQueryParamsPassesAllNullFiltersToPortalDAO() {
        when(portalDAO.getExpenseReports(null, null, null, null, null)).thenReturn(new ArrayList<>());

        api()
        .get("/api/reports")
        .then()
            .statusCode(200);

        verify(portalDAO).getExpenseReports(null, null, null, null, null);
    }

    @Test
    public void reportsWithBlankQueryParamsAreNormalizedToNull() {
        when(portalDAO.getExpenseReports(null, null, null, null, null)).thenReturn(new ArrayList<>());

        api()
            .queryParam("status", " ")
            .queryParam("employee", "")
        .get("/api/reports")
        .then()
            .statusCode(200);

        verify(portalDAO).getExpenseReports(null, null, null, null, null);
    }

    @Test
    public void optionsReturnsEmployeesAndManagersFromPortalDAO() {
        when(portalDAO.getEmployees()).thenReturn(new ArrayList<>(List.of("adoe", "jdoe")));
        when(portalDAO.getManagers()).thenReturn(new ArrayList<>(List.of("mgr1")));

        api()
        .get("/api/options")
        .then()
            .statusCode(200)
            .body("employees", equalTo(List.of("adoe", "jdoe")))
            .body("managers", equalTo(List.of("mgr1")));
    }

    @Test
    public void optionsReturnsEmptyListsWhenNoUsersExist() {
        when(portalDAO.getEmployees()).thenReturn(new ArrayList<>());
        when(portalDAO.getManagers()).thenReturn(new ArrayList<>());

        api()
        .get("/api/options")
        .then()
            .statusCode(200)
            .body("employees", empty())
            .body("managers", empty());
    }
}
