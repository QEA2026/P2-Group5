package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    public void constructorAndGettersReturnValuesPassedIn() {
        User user = new User(7, "jsmith", "password123", "manager");

        assertEquals(7, user.getUser_id());
        assertEquals("jsmith", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("manager", user.getRole());
    }

    @Test
    public void settersUpdateMutableFields() {
        User user = new User();

        user.setUser_id(8);
        user.setUsername("adoe");
        user.setPassword("secret");
        user.setRole("employee");

        assertEquals(8, user.getUser_id());
        assertEquals("adoe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("employee", user.getRole());
    }

    @Test
    public void toStringIncludesCoreFields() {
        User user = new User(9, "bwayne", "batman", "manager");

        assertEquals("User{role=manager, user_id=9, username='bwayne'}", user.toString());
    }
}