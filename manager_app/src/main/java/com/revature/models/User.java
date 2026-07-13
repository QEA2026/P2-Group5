package com.revature.models;

public class User {

    static int managerCount = 0;

    private int user_id;
    private String username;
    private String password;
    private String role;


    public User()
    {

    }

    public User(String username, String password, String role)
    {
        this.username = username;
        this.password = password;
        this.role = role;

    }

    public User(int user_id, String username, String password, String role)
    {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getUser_id()
    {
        return this.user_id;
    }

    public void setUser_id(int new_id)
    {
        this.user_id = new_id;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String new_username)
    {
        this.username = new_username;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String new_pass)
    {
        this.password = new_pass;
    }

    public void setRole(String newRole)
    {
        this.role = newRole;
    }

    public String getRole()
    {
        return this.role;
    }


    @Override
    public String toString()
    {
        return "User{" +
                "role=" + role +
                ", user_id=" + user_id +
                ", username='" + username + '\'' +
                '}';
    }
}
