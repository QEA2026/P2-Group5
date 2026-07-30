package com.revature.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.models.Approval;
import com.revature.models.ManagerExpenseApprovalRecord;
import com.revature.models.ManagerSummary;

// Runs the real DAOs (default constructor, no injected mocks) against a throwaway SQLite
// database seeded from the project's actual db/schema.sql. Verifies the SQL itself -
// joins, filters, inserts/updates - which the mocked-Connection unit tests can't catch.
class ManagerDatabaseIntegrationTest {

    private String originalUserDir;

    @BeforeEach
    void redirectDatabaseToTempDirectory(@TempDir Path tempDir) throws IOException, SQLException {
        originalUserDir = System.getProperty("user.dir");

        // Surefire runs with the working directory set to this module's root (manager_app/),
        // so the real project's db/ folder is always one level up.
        Path realSchemaFile = Paths.get(originalUserDir, "..", "db", "schema.sql").toAbsolutePath().normalize();

        Path dbDirectory = Files.createDirectory(tempDir.resolve("db"));
        Path dbFile = dbDirectory.resolve("expense_manager.db");
        seedDatabase(dbFile, realSchemaFile);

        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void restoreUserDir() {
        System.setProperty("user.dir", originalUserDir);
    }

    private void seedDatabase(Path dbFile, Path schemaFile) throws IOException, SQLException {
        String schemaSql = Files.readString(schemaFile);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
             Statement statement = connection.createStatement()) {

            for (String ddlStatement : schemaSql.split(";")) {
                String trimmed = ddlStatement.strip();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }

            statement.execute("""
                INSERT INTO users (user_id, username, password, role) VALUES
                    (1, 'alice_employee', 'password123', 'employee'),
                    (2, 'manager_diana', 'manager123', 'manager')
                """);
            statement.execute("""
                INSERT INTO expenses (expense_id, user_id, amount, exp_description, date)
                VALUES (1, 1, 42.50, 'Integration test expense', '2026-07-29')
                """);
            statement.execute("""
                INSERT INTO approvals (approval_id, expense_id, status, reviewer, comment, review_date)
                VALUES (1, 1, 'pending', NULL, NULL, NULL)
                """);
        }
    }

    @Test
    void managerPortalDaoReadsSeededDataThroughRealSql() {
        ManagerPortalDAO portalDAO = new ManagerPortalDAO();

        ManagerSummary summary = portalDAO.getSummary();
        assertEquals(1, summary.getTotal());
        assertEquals(1, summary.getPending());
        assertEquals(0, summary.getApproved());
        assertEquals(0, summary.getDenied());

        ArrayList<ManagerExpenseApprovalRecord> records =
            portalDAO.getApprovalRecords(null, null, null, "integration");
        assertEquals(1, records.size());
        assertEquals("alice_employee", records.get(0).getEmployeeUsername());
        assertEquals("pending", records.get(0).getStatus());

        assertEquals(List.of("alice_employee"), portalDAO.getEmployees());
        assertEquals(List.of("manager_diana"), portalDAO.getManagers());
    }

    @Test
    void approvalDaoRoundTripsAnUpdateThroughRealSql() {
        ApprovalDAO approvalDAO = new ApprovalDAO();

        Approval existing = approvalDAO.getApprovalByID(1);
        assertNotNull(existing);
        assertEquals("pending", existing.getStatus());

        Approval updateRequest = new Approval(
            existing.getApproval_id(),
            existing.getExpense_id(),
            "approved",
            2,
            "Approved by integration test",
            "2026-07-29"
        );
        assertNotNull(approvalDAO.updateApproval(updateRequest));

        Approval reloaded = approvalDAO.getApprovalByID(1);
        assertEquals("approved", reloaded.getStatus());
        assertEquals(2, reloaded.getReviewer_id());
        assertEquals("Approved by integration test", reloaded.getComment());
    }
}
