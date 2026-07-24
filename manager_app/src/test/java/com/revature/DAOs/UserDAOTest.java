package com.revature.DAOs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.revature.models.User;

@ExtendWith(MockitoExtension.class)
public class UserDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Test
    public void getManagersMapsAllRowsFromResultSet() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("user_id")).thenReturn(1, 2);
        when(resultSet.getString("username")).thenReturn("mgr1", "mgr2");
        when(resultSet.getString("password")).thenReturn("pw1", "pw2");
        when(resultSet.getString("role")).thenReturn("manager", "manager");

        UserDAO userDAO = new UserDAO(connection);
        ArrayList<User> managers = userDAO.getManagers();

        assertEquals(2, managers.size());
        assertEquals("mgr1", managers.get(0).getUsername());
        assertEquals("mgr2", managers.get(1).getUsername());
    }

    @Test
    public void getManagersReturnsEmptyListWhenNoRows() throws SQLException {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        UserDAO userDAO = new UserDAO(connection);
        ArrayList<User> managers = userDAO.getManagers();

        assertTrue(managers.isEmpty());
    }

    @Test
    public void insertUserSetsPreparedStatementParametersAndReturnsUser() throws SQLException {
        User user = new User("alice", "pw123", "employee");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        UserDAO userDAO = new UserDAO(connection);
        User result = userDAO.insertUser(user);
        verify(preparedStatement).setString(1, user.getUsername());
        verify(preparedStatement).setString(2, user.getPassword());
        verify(preparedStatement).setString(3, user.getRole());
        verify(preparedStatement).executeUpdate();
        assertEquals(user, result);
    }

    @Test
    public void deleteUserSetsUserIdOnPreparedStatement() throws SQLException {
        User user = new User(2, "alice", "pw123", "employee");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        UserDAO userDAO = new UserDAO(connection);
        boolean deleted = userDAO.deleteUser(user);
        verify(preparedStatement).setInt(1, user.getUser_id());
        verify(preparedStatement).executeUpdate();
        assertTrue(deleted);
    }
}
