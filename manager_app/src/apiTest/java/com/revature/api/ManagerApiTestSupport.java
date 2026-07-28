package com.revature.api;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.Main;
import com.revature.models.User;

import io.javalin.Javalin;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

// Shared helpers for spinning up the real Javalin app (with mock DAOs) and driving it over HTTP
// with REST Assured, so API tests exercise the actual route wiring/session handling instead of
// mocking Context directly.
public final class ManagerApiTestSupport {

    private ManagerApiTestSupport() {
    }

    public static Javalin startTestApp(AuthDAO authDAO, ApprovalDAO approvalDAO, ManagerPortalDAO portalDAO) {
        Javalin app = Main.createApp(authDAO, approvalDAO, portalDAO);
        app.start(0);
        return app;
    }

    public static String baseUrl(Javalin app) {
        return "http://localhost:" + app.port();
    }

    // A SessionFilter captures the Set-Cookie header from login and replays it on every later
    // request made with the same filter instance, which is what keeps requireManagerApi(ctx)
    // passing across calls without manually managing a cookie jar.
    public static SessionFilter newSessionFilter() {
        return new SessionFilter();
    }

    // Base request spec every call in a test should build on: correct base URI + the shared
    // session cookie filter.
    public static RequestSpecification managerApi(String baseUrl, SessionFilter sessionFilter) {
        return given().baseUri(baseUrl).filter(sessionFilter);
    }

    // Stubs authDAO to accept any credentials as the given manager, then logs in through the real
    // /api/login endpoint so the session filter picks up a genuine session cookie.
    public static Response loginAsManager(
        String baseUrl, SessionFilter sessionFilter, AuthDAO authDAO, User managerUser
    ) {
        when(authDAO.login(anyString(), anyString())).thenReturn(managerUser);

        return managerApi(baseUrl, sessionFilter)
            .contentType("application/json")
            .body("{\"username\":\"jsmith\",\"password\":\"password123\"}")
            .post("/api/login");
    }
}
