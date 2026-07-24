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

import com.revature.models.Expense;

@ExtendWith(MockitoExtension.class)
public class ExpenseDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Test
    public void getExpensesMapsAllRowsFromResultSet() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("expense_id")).thenReturn(1, 2);
        when(resultSet.getInt("user_id")).thenReturn(10, 20);
        when(resultSet.getDouble("amount")).thenReturn(45.50, 12.00);
        when(resultSet.getString("exp_description")).thenReturn("Taxi", "Lunch");
        when(resultSet.getString("date")).thenReturn("2026-07-01", "2026-07-02");

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        ArrayList<Expense> expenses = expenseDAO.getExpenses();

        assertEquals(2, expenses.size());
        assertEquals("Taxi", expenses.get(0).getDescription());
        assertEquals("Lunch", expenses.get(1).getDescription());
    }

    @Test
    public void getExpensesByEmployeeFiltersByUsername() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("expense_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(10);
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("date")).thenReturn("2026-07-01");

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        ArrayList<Expense> expenses = expenseDAO.getExpensesByEmployee("someUsername");

        assertEquals(1, expenses.size());
        assertEquals("Taxi", expenses.get(0).getDescription());
        verify(preparedStatement).setString(1, "someUsername");
    }

    @Test
    public void getExpensesByStatusFiltersByStatus() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("expense_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(10);
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("date")).thenReturn("2026-07-01");

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        ArrayList<Expense> expenses = expenseDAO.getExpensesByStatus("approved");

        assertEquals(1, expenses.size());
        assertEquals("Taxi", expenses.get(0).getDescription());
        verify(preparedStatement).setString(1, "approved");
    }

    @Test
    public void getExpensesByDateFiltersByDateRange() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("expense_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(10);
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("date")).thenReturn("2026-07-01");

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        ArrayList<Expense> expenses = expenseDAO.getExpensesByDate("2026-07-01", "2026-07-31");

        assertEquals(1, expenses.size());
        verify(preparedStatement).setString(1, "2026-07-01");
        verify(preparedStatement).setString(2, "2026-07-31");
    }

    @Test
    public void getExpensesByCategoryFiltersByDescriptionKeyword() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("expense_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(10);
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("date")).thenReturn("2026-07-01");

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        ArrayList<Expense> expenses = expenseDAO.getExpensesByCategory("Taxi");

        assertEquals(1, expenses.size());
        verify(preparedStatement).setString(1, "%Taxi%");
    }

    @Test
    public void getExpenseByIDReturnsMappedExpenseWhenFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("expense_id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(10);
        when(resultSet.getDouble("amount")).thenReturn(45.50);
        when(resultSet.getString("exp_description")).thenReturn("Taxi");
        when(resultSet.getString("date")).thenReturn("2026-07-01");

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        Expense result = expenseDAO.getExpenseByID(1);

        assertEquals(1, result.getExpense_id());
        assertEquals(10, result.getUser_id());
        assertEquals(45.50, result.getAmount());
        assertEquals("Taxi", result.getDescription());
        assertEquals("2026-07-01", result.getDate());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    public void getExpenseByIDReturnsNullWhenNotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ExpenseDAO expenseDAO = new ExpenseDAO(connection);
        Expense result = expenseDAO.getExpenseByID(999);

        assertNull(result);
    }
}
