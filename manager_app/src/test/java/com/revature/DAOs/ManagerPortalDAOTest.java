package com.revature.DAOs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.models.ManagerExpenseApprovalRecord;
import com.revature.models.ManagerSummary;

@ExtendWith(MockitoExtension.class)
public class ManagerPortalDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private void stubSingleRecordRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1);
        when(resultSet.getInt("expense_id")).thenReturn(10);
        when(resultSet.getInt("employee_id")).thenReturn(20);
        when(resultSet.getString("employee_username")).thenReturn("jdoe");
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("expense_date")).thenReturn("2026-07-01");
        when(resultSet.getString("status")).thenReturn("pending");
        when(resultSet.getInt("reviewer")).thenReturn(5);
        when(resultSet.getString("reviewer_username")).thenReturn("mgr1");
        when(resultSet.getString("comment")).thenReturn("needs review");
        when(resultSet.getString("review_date")).thenReturn("2026-07-02");
    }

    @Test
    public void getApprovalRecordsWithNoFiltersAppliesNoParameters() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        stubSingleRecordRow();

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<ManagerExpenseApprovalRecord> records = dao.getApprovalRecords(null, null, null, null);

        assertEquals(1, records.size());
        assertEquals("jdoe", records.get(0).getEmployeeUsername());
        assertEquals(Integer.valueOf(5), records.get(0).getReviewerId());
    }

    @Test
    public void getApprovalRecordsAppliesStatusEmployeeReviewerAndKeywordFilters() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        stubSingleRecordRow();

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<ManagerExpenseApprovalRecord> records =
            dao.getApprovalRecords("pending", "jdoe", "mgr1", "taxi");

        assertEquals(1, records.size());
        verify(preparedStatement).setObject(1, "pending");
        verify(preparedStatement).setObject(2, "jdoe");
        verify(preparedStatement).setObject(3, "mgr1");
        verify(preparedStatement).setObject(4, "%taxi%");
        verify(preparedStatement).setObject(5, "%taxi%");
    }

    @Test
    public void getApprovalRecordsMapsNullReviewerWhenReviewerColumnIsSqlNull() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1);
        when(resultSet.getInt("expense_id")).thenReturn(10);
        when(resultSet.getInt("employee_id")).thenReturn(20);
        when(resultSet.getString("employee_username")).thenReturn("jdoe");
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("expense_date")).thenReturn("2026-07-01");
        when(resultSet.getString("status")).thenReturn("pending");
        when(resultSet.getInt("reviewer")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        when(resultSet.getString("reviewer_username")).thenReturn(null);
        when(resultSet.getString("comment")).thenReturn(null);
        when(resultSet.getString("review_date")).thenReturn(null);

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<ManagerExpenseApprovalRecord> records = dao.getApprovalRecords(null, null, null, null);

        assertEquals(1, records.size());
        assertNull(records.get(0).getReviewerId());
    }

    @Test
    public void getExpenseReportsAppliesDateRangeFilter() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        stubSingleRecordRow();

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<ManagerExpenseApprovalRecord> records =
            dao.getExpenseReports(null, null, "2026-07-01", "2026-07-31", null);

        assertEquals(1, records.size());
        verify(preparedStatement).setObject(1, "2026-07-01");
        verify(preparedStatement).setObject(2, "2026-07-31");
    }

    @Test
    public void getSummaryReturnsMappedSummaryWhenRowPresent() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total")).thenReturn(10);
        when(resultSet.getInt("pending_count")).thenReturn(4);
        when(resultSet.getInt("approved_count")).thenReturn(5);
        when(resultSet.getInt("denied_count")).thenReturn(1);

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ManagerSummary summary = dao.getSummary();

        assertEquals(10, summary.getTotal());
        assertEquals(4, summary.getPending());
        assertEquals(5, summary.getApproved());
        assertEquals(1, summary.getDenied());
    }

    @Test
    public void getSummaryReturnsZeroedSummaryWhenNoRow() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ManagerSummary summary = dao.getSummary();

        assertEquals(0, summary.getTotal());
        assertEquals(0, summary.getPending());
        assertEquals(0, summary.getApproved());
        assertEquals(0, summary.getDenied());
    }

    @Test
    public void getEmployeesReturnsUsernamesFilteredByEmployeeRole() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("username")).thenReturn("adoe", "jdoe");

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<String> employees = dao.getEmployees();

        assertEquals(2, employees.size());
        assertTrue(employees.contains("adoe"));
        assertTrue(employees.contains("jdoe"));
        verify(preparedStatement).setString(1, "employee");
    }

    @Test
    public void getManagersReturnsUsernamesFilteredByManagerRole() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("username")).thenReturn("mgr1");

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<String> managers = dao.getManagers();

        assertEquals(1, managers.size());
        assertEquals("mgr1", managers.get(0));
        verify(preparedStatement).setString(1, "manager");
    }

    @Test
    public void getApprovalRecordsReturnsEmptyListWhenQueryThrowsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("simulated database failure"));

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<ManagerExpenseApprovalRecord> records = dao.getApprovalRecords(null, null, null, null);

        assertTrue(records.isEmpty());
    }

    @Test
    public void getExpenseReportsReturnsEmptyListWhenQueryThrowsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("simulated database failure"));

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<ManagerExpenseApprovalRecord> records =
            dao.getExpenseReports(null, null, null, null, null);

        assertTrue(records.isEmpty());
    }

    @Test
    public void getSummaryReturnsZeroedSummaryWhenSqlExceptionThrown() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("simulated database failure"));

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ManagerSummary summary = dao.getSummary();

        assertEquals(0, summary.getTotal());
        assertEquals(0, summary.getPending());
        assertEquals(0, summary.getApproved());
        assertEquals(0, summary.getDenied());
    }

    @Test
    public void getEmployeesReturnsEmptyListWhenQueryThrowsSqlException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("simulated database failure"));

        ManagerPortalDAO dao = new ManagerPortalDAO(connection);
        ArrayList<String> employees = dao.getEmployees();

        assertTrue(employees.isEmpty());
    }
}
