package com.revature;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ExpenseDAO;
import com.revature.models.Approval;
import com.revature.models.Expense;
import com.revature.models.User;


public class Main {
    public static void main(String[] args) {

        User e1 = new User(1, "jsmith", "password123", "manager");
        Connection connection = null;
        Scanner scanner = new Scanner(System.in);



        // Create console menu 
        while (true) 
        {
            // Generate menu for Manager App
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Welcome to the Employee Management System");
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Select from the following choices:");
            System.out.println("1: Login");
            System.out.println("2: Exit");
            System.out.print("Choice: ");

            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next();
            }

            int choice = scanner.nextInt();

            switch (choice)
            {
                // login 
                case 1:
                    System.out.print("Enter your username: ");
                    String username = scanner.next();
                    System.out.print("Enter your password: ");
                    String password = scanner.next();
                    AuthDAO authDAO = new AuthDAO();

                    try {
                        User loggedInManager = authDAO.login(username, password);

                        if (loggedInManager == null) {
                            System.out.println("Invalid username or password. Please try again.");
                            System.out.println();
                            break;
                        }

                        System.out.println();
                        System.out.println("Logged in as: " + loggedInManager.getUsername() + ", ID: " + loggedInManager.getUser_id());
                        System.out.println();

                        // check if logged in user is a manager
                        if (loggedInManager.getRole().equalsIgnoreCase("manager") == false)
                        {
                            System.out.println("Sorry, you do not have access!");
                            System.out.println("Logging out and exiting the program...");
                            loggedInManager = authDAO.logout(loggedInManager);
                            break;
                        }
                        
                        // Menu for managers 
                        while (true)
                        {

                            ApprovalDAO appDAO = new ApprovalDAO();
                            ExpenseDAO expDAO = new ExpenseDAO();
                            // ArrayLists for approvals and expenses
                            ArrayList<Expense> expList = new ArrayList<>();
                            ArrayList<Approval> appList = new ArrayList<>();

                            // Generate main menu for manager options
                            System.out.println("--------------------------------------------------------------------------"); 
                            System.out.println("Welcome, " + loggedInManager.getUsername());
                            System.out.println("--------------------------------------------------------------------------");
                            System.out.println("Choose from the following options: ");
                            System.out.println("1. View Expense Reports Menu");
                            System.out.println("2. View Approvals Menu");
                            System.out.println("3. Exit");
                            System.out.print("Choice: ");

                            while (!scanner.hasNextInt()) {
                            System.out.print("Invalid input. Please enter a number: ");
                            scanner.next();
                            }
                            choice = scanner.nextInt();
                           
                            switch(choice)
                            {

                                
                                // Expense Reports Menu
                                case 1:

                                    ExpenseMenuLoop:
                                    while (true) { 
                                        
                                        System.out.println("--------------------------------------------------------------------------");
                                        System.out.println("Expense Report Menu");
                                        System.out.println("--------------------------------------------------------------------------");
                                        
                                        System.out.println("Select an option: ");
                                        System.out.println("1. View all expenses");
                                        System.out.println("2. View expenses by status");
                                        System.out.println("3. View expenses by employee");
                                        System.out.println("4. View expenses by date");
                                        System.out.println("5. View expenses by category");
                                        System.out.println("6. Exit menu");

                                        System.out.print("Choice: ");
                                        while (!scanner.hasNextInt()) {
                                        System.out.print("Invalid input. Please enter a number: ");
                                        scanner.next();
                                        }
                                        int new_choice = scanner.nextInt();

                                        switch (new_choice)
                                        {
                                            // view all expense reports
                                            case 1:
                                                expList = expDAO.getExpenses();
                                                TableFormatter.printExpensesTable(expList);
                                                System.out.println();
                                                break;

                                            // view expense reports by status 
                                            case 2:
                                                System.out.println("Choose what expense reports you want to view: pending, approved or denied");
                                                String status = scanner.next();
                                                System.out.println("Viewing all reports that are " + status);
                                                expList = expDAO.getExpensesByStatus(status);
                                                TableFormatter.printExpensesTable(expList);
                                                System.out.println();
                                                break; 
                                            
                                            // view expense reports by employee
                                            case 3:
                                                System.out.println("Enter the employee's username");
                                                String user = scanner.next();
                                                System.out.println("Viewing reports for " + user);
                                                expList = expDAO.getExpensesByEmployee(user);
                                                TableFormatter.printExpensesTable(expList);
                                                System.out.println();
                                                break;
                                            
                                            // view expense reports by date
                                            case 4:
                                                System.out.println("Enter the date range (YYYY-MM-DD): ");
                                                System.out.print("Start date: ");
                                                String start_date = scanner.next();
                                                System.out.print("End date: ");
                                                String end_date = scanner.next();

                                                System.out.println("Viewing reports between " + start_date + " and " + end_date);
                                                expList = expDAO.getExpensesByDate(start_date, end_date);
                                                TableFormatter.printExpensesTable(expList);
                                                System.out.println();
                                                break;

                                            // view expense reports by category

                                            case 5:
                                                System.out.println("Enter the keyword you want to search: ");
                                                System.out.print("Search: ");
                                                String category = scanner.next();
                                                System.out.println("Viewing reports containing keyword " + category);

                                                expList = expDAO.getExpensesByCategory(category);
                                                TableFormatter.printExpensesTable(expList);
                                                System.out.println();
                                                break;

                                            // Exit
                                            case 6:
                                                System.out.println("Exiting the expense reports menu");
                                                System.out.println();
                                                break ExpenseMenuLoop;
                                                


                                            default:
                                                System.out.println("Invalid option. Please try again.");
                                                System.out.println();
                                                break;


                                             

                                            

                                        }

                                        
                                }
                                
                                    break;

                                // Approvals Menu
                                case 2:

                                ApprovalMenuLoop:
                                while (true) 
                                {   System.out.println("--------------------------------------------------------------------------");
                                    System.out.println("Approvals Menu");
                                    System.out.println("--------------------------------------------------------------------------");
                                    System.out.println("1. View all approvals");
                                    System.out.println("2. View approvals by status");
                                    System.out.println("3. View approvals by manager/reviewer");
                                    System.out.println("4. View approvals by employee");
                                    System.out.println("5. Update an approval");
                                    System.out.println("6. Exit");
                                    
                                    System.out.print("Choice: ");
                                        while (!scanner.hasNextInt()) {
                                        System.out.print("Invalid input. Please enter a number: ");
                                        scanner.next();
                                        }
                                    int ch = scanner.nextInt();

                                    switch(ch)
                                    {
                                        case 1:
                                            appList = appDAO.getApprovals();
                                            for (Approval a: appList)
                                            {
                                                Expense e = expDAO.getExpenseByID(a.getExpense_id());
                                                TableFormatter.printExpenseAndApproval(e, a);
                                            }
                                            System.out.println();
                                            break;

                                        // view approvals by status 
                                        case 2:
                                            System.out.println("Choose what approvals you want to view: pending, approved, or denied");
                                            String status = scanner.next();
                                            appList = appDAO.getApprovalsByStatus(status);
                                            for (Approval a: appList)
                                            {
                                                Expense e = expDAO.getExpenseByID(a.getExpense_id());
                                                TableFormatter.printExpenseAndApproval(e, a);
                                            }
                                            System.out.println();
                                            break;
                                        
                                        // view approvals by manager
                                        case 3:
                                            System.out.println("Enter the manager username: ");
                                            String user = scanner.next();
                                            appList = appDAO.getApprovalsByManager(user);
                                            for (Approval a: appList)
                                            {
                                                Expense e = expDAO.getExpenseByID(a.getExpense_id());
                                                TableFormatter.printExpenseAndApproval(e, a);
                                            }
                                            System.out.println();
                                            break;
                                        
                                        // view approvals by employee
                                        case 4:
                                            System.out.println("Enter the employee username: ");
                                            String emp = scanner.next();
                                            appList = appDAO.getApprovalsByEmployee(emp);
                                            
                                            for (Approval a: appList)
                                            {
                                                Expense e = expDAO.getExpenseByID(a.getExpense_id());
                                                TableFormatter.printExpenseAndApproval(e, a);
                                            }

                                            System.out.println();
                                            break;

                                        // submit a new approval
                                        case 5:
                                            System.out.println("Here are all the current expenses and approvals: ");
                                            
                                            appList = appDAO.getApprovals();
                                        

                                            for (Approval a: appList)
                                            {
                                                Expense e = expDAO.getExpenseByID(a.getExpense_id());
                                                TableFormatter.printExpenseAndApproval(e, a);
                                            }

                                            System.out.println("To update an approval, enter the following information: ");
                                            System.out.print("ID of the Approval you want to update: ");
                                            int app_id = scanner.nextInt();

                                            Approval a = appDAO.getApprovalByID(app_id);
                                            int exp_id = a.getExpense_id();

                                            System.out.println("Selected approval: ");
                                            System.out.println(a);

                                            System.out.println();

                                            System.out.print("Status of the approval: ");
                                            String app_status = scanner.next();

                                            String temp = scanner.nextLine(); // consume the newline character
                                            System.out.print("Comment you want to add: ");
                                            String comments = scanner.nextLine();

                                            System.out.print("Review date (YYYY-MM-DD): ");
                                            String review_date = scanner.nextLine();

                                            

                                            Approval new_app = new Approval(app_id, exp_id, app_status, loggedInManager.getUser_id(), comments, review_date);

                                            appDAO.updateApproval(new_app);

                                            Approval n = appDAO.getApprovalByID(app_id);
                                        
                                            System.out.println("Updated approval: ");
                                            System.out.println(n);

                                            System.out.println();
                                            
                                            break;

                                        case 6:
                                            System.out.println("Exiting the approvals menu");
                                            System.out.println();
                                            break ApprovalMenuLoop;



                                        default:
                                            System.out.println("Invalid option. Please try again.");
                                            break;


                                    }

                                    
                                }


                                   
                            }

                            


                            // exit 
                            if (choice == 3)
                            {
                                System.out.println("Exiting the manager menu...");
                                System.out.println();
                                break;
                            }

                        

                        }

                   
                    } catch (Exception e) {
                        System.out.println("Error. Exiting the program");
                    }
                    
                    break;

                case 2:
                    System.out.println("Exiting the program...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;

            }
           
            
        }

        


    }
}