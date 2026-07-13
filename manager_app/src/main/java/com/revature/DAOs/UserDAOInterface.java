package com.revature.DAOs;

import java.util.ArrayList;

import com.revature.models.User;


public interface UserDAOInterface {

    //here we will lay out functionalities that ManagerDAO will implement

    //a method to select all employees
    ArrayList<User> getManagers();
    //Why Arraylist? Get all will return multiple employees.
    //So we need something that can store multiple Employee objects are once

    //A method to insert a new employee
    User insertUser(User emp);
    //if we're sending an Employee, why return one back?
    //just so the User can see what they've inserted. Think of it as a confirmation

    //TODO: delete a manager
    User deleteUser(User emp);

    
}