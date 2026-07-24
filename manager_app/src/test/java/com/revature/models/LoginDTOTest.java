package com.revature.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LoginDTOTest {

    @Test
    public void constructorAndGettersReturnValuesPassedIn() {
        LoginDTO loginDTO = new LoginDTO("jsmith", "password123");

        assertEquals("jsmith", loginDTO.getUsername());
        assertEquals("password123", loginDTO.getPassword());
    }

    @Test
    public void settersUpdateFields() {
        LoginDTO loginDTO = new LoginDTO();

        loginDTO.setUsername("adoe");
        loginDTO.setPassword("secret");

        assertEquals("adoe", loginDTO.getUsername());
        assertEquals("secret", loginDTO.getPassword());
    }
}