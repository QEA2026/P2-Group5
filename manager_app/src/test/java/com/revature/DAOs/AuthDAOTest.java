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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.models.User;

@ExtendWith(MockitoExtension.class)
public class AuthDAOTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Test
    public void loginReturnsUserWhenCredentialsMatch() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("user_id")).thenReturn(1);
        when(resultSet.getString("username")).thenReturn("jsmith");
        when(resultSet.getString("password")).thenReturn("password123");
        when(resultSet.getString("role")).thenReturn("manager");

        AuthDAO authDAO = new AuthDAO(connection);
        User result = authDAO.login("jsmith", "password123");

        assertEquals("jsmith", result.getUsername());
        assertEquals("manager", result.getRole());
    }

    @Test
    public void loginReturnsNullWhenNoMatchingUser() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AuthDAO authDAO = new AuthDAO(connection);
        User result = authDAO.login("baduser", "badpass");

        assertNull(result);
    }

    @Test
    public void loginSetsUsernameAndPasswordOnPreparedStatement() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        AuthDAO authDAO = new AuthDAO(connection);
        authDAO.login("jsmith", "password123");

        verify(preparedStatement).setString(1, "jsmith");
        verify(preparedStatement).setString(2, "password123");
    }

    @Test
    public void logoutReturnsNull() {
        User user = new User(1, "jsmith", "password123", "manager");
        AuthDAO authDAO = new AuthDAO();
        User result = authDAO.logout(user);

        assertNull(result);
    }

    @Test
    public void loginReturnsNullWhenSqlExceptionThrown() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("simulated database failure"));

        AuthDAO authDAO = new AuthDAO(connection);
        User result = authDAO.login("jsmith", "password123");

        assertNull(result);
    }
}
