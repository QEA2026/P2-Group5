package com.revature.DAOs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.models.Approval;

@ExtendWith(MockitoExtension.class)
public class ApprovalDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Test
    public void getApprovalsMapsAllRowsFromResultSet() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1, 2);
        when(resultSet.getInt("expense_id")).thenReturn(10, 20);
        when(resultSet.getString("status")).thenReturn("pending", "approved");
        when(resultSet.getInt("reviewer")).thenReturn(5, 6);
        when(resultSet.getString("comment")).thenReturn("needs review", "looks good");
        when(resultSet.getString("review_date")).thenReturn("2026-07-01", "2026-07-02");

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        ArrayList<Approval> approvals = approvalDAO.getApprovals();

        assertEquals(2, approvals.size());
        assertEquals("pending", approvals.get(0).getStatus());
        assertEquals("approved", approvals.get(1).getStatus());
    }

    @Test
    public void getApprovalsByManagerFiltersByReviewerUsername() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1);
        when(resultSet.getInt("expense_id")).thenReturn(10);
        when(resultSet.getString("status")).thenReturn("pending");
        when(resultSet.getInt("reviewer")).thenReturn(5);
        when(resultSet.getString("comment")).thenReturn("needs review");
        when(resultSet.getString("review_date")).thenReturn("2026-07-01");

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        ArrayList<Approval> approvals = approvalDAO.getApprovalsByManager("man_username");

        assertEquals(1, approvals.size());
        assertEquals("pending", approvals.get(0).getStatus());
        verify(preparedStatement).setString(1, "man_username");
    }

    @Test
    public void getApprovalsByEmployeeFiltersByEmployeeUsername() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1, 2);
        when(resultSet.getInt("expense_id")).thenReturn(10, 11);
        when(resultSet.getString("status")).thenReturn("pending", "approved");
        when(resultSet.getInt("reviewer")).thenReturn(66, 67);
        when(resultSet.getString("comment")).thenReturn("a comment", "needs review");
        when(resultSet.getString("review_date")).thenReturn("2026-07-30", "2026-07-31");

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        ArrayList<Approval> approvals = approvalDAO.getApprovalsByEmployee("emp_username");

        assertEquals(2, approvals.size());
        assertEquals("pending", approvals.get(0).getStatus());
        assertEquals("approved", approvals.get(1).getStatus());
        verify(preparedStatement).setString(1, "emp_username");
    }

    @Test
    public void getApprovalsByStatusFiltersByStatus() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1);
        when(resultSet.getInt("expense_id")).thenReturn(10);
        when(resultSet.getString("status")).thenReturn("denied");
        when(resultSet.getInt("reviewer")).thenReturn(5);
        when(resultSet.getString("comment")).thenReturn("needs review");
        when(resultSet.getString("review_date")).thenReturn("2026-07-01");

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        ArrayList<Approval> approvals = approvalDAO.getApprovalsByStatus("denied");

        assertEquals(1, approvals.size());
        assertEquals("denied", approvals.get(0).getStatus());
        verify(preparedStatement).setString(1, "denied");
    }

    @Test
    public void getApprovalsByDateFiltersByReviewDate() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("approval_id")).thenReturn(1);
        when(resultSet.getInt("expense_id")).thenReturn(10);
        when(resultSet.getString("status")).thenReturn("pending");
        when(resultSet.getInt("reviewer")).thenReturn(5);
        when(resultSet.getString("comment")).thenReturn("needs review");
        when(resultSet.getString("review_date")).thenReturn("2026-07-01");

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        ArrayList<Approval> approvals = approvalDAO.getApprovalsByDate("2026-07-01");

        assertEquals(1, approvals.size());
        assertEquals("2026-07-01", approvals.get(0).getReview_date());
        verify(preparedStatement).setString(1, "2026-07-01");
    }

    @Test
    public void insertApprovalSetsPreparedStatementParametersAndReturnsApproval() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Approval approval = new Approval(1, 10, "pending", 5, "needs review", "2026-07-01");
        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        Approval result = approvalDAO.insertApproval(approval);

        verify(preparedStatement).setInt(1, approval.getExpense_id());
        verify(preparedStatement).setString(2, approval.getStatus());
        verify(preparedStatement).setInt(3, approval.getReviewer_id());
        verify(preparedStatement).setString(4, approval.getComment());
        verify(preparedStatement).setString(5, approval.getReview_date());
        assertEquals(approval, result);
    }

    @Test
    public void updateApprovalSetsPreparedStatementParametersAndReturnsApproval() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Approval approval = new Approval(1, 10, "approved", 5, "looks good", "2026-07-02");
        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        Approval result = approvalDAO.updateApproval(approval);

        verify(preparedStatement).setString(1, approval.getStatus());
        verify(preparedStatement).setInt(2, approval.getReviewer_id());
        verify(preparedStatement).setString(3, approval.getComment());
        verify(preparedStatement).setString(4, approval.getReview_date());
        verify(preparedStatement).setInt(5, approval.getApproval_id());
        assertEquals(approval, result);
    }

    @Test
    public void getApprovalByIDReturnsMappedApprovalWhenFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("approval_id")).thenReturn(1);
        when(resultSet.getInt("expense_id")).thenReturn(10);
        when(resultSet.getString("status")).thenReturn("pending");
        when(resultSet.getInt("reviewer")).thenReturn(5);
        when(resultSet.getString("comment")).thenReturn("needs review");
        when(resultSet.getString("review_date")).thenReturn("2026-07-01");

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        Approval result = approvalDAO.getApprovalByID(1);

        assertEquals(1, result.getApproval_id());
        assertEquals(10, result.getExpense_id());
        assertEquals("pending", result.getStatus());
        assertEquals(5, result.getReviewer_id());
        assertEquals("needs review", result.getComment());
        assertEquals("2026-07-01", result.getReview_date());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    public void getApprovalByIDReturnsNullWhenNotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ApprovalDAO approvalDAO = new ApprovalDAO(connection);
        Approval result = approvalDAO.getApprovalByID(999);

        assertNull(result);
    }
}
