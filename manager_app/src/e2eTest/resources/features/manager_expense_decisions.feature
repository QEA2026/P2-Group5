Feature: Manager reviews employee expenses

  Scenario: Manager approves a pending expense
    Given an employee submits a new expense
    When the manager views and approves the expense
    Then the employee should see the expense as approved

  Scenario: Manager denies a pending expense
    Given an employee submits a new expense
    When the manager views and denies the expense
    Then the employee should see the expense as denied
