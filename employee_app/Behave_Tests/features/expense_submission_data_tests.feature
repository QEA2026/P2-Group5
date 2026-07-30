Feature: Expense Submission
  As a registered user,
  I want to submit expenses and view after I have logged in,
  So that I can manage my employee account expenses.

  Background:
    Given the application is running
    And the test database is seeded with users

  Scenario Outline: Successful submission of new expense
    Given the user is logged in as "<username>" and "<password>"
    And the user is on the dashboard page
    When the user enters expense "<amount>"
    And the user enters description "<description>"
    And the user clicks the submit button
    Then the expense "<expected_result>" should be displayed

  Examples:
    | username     | password    | amount | description       | expected_result   |
    | bob_employee | password123 | 12.50  | pizza party       | success message   |
    | bob_employee | password123 | 500.00 | travel expenses   | success message   |
    | bob_employee | password123 |        | pizza party       | error message     |
    | bob_employee | password123 | 11.00  |                   | error message     |

  Scenario Outline: User has existing expense reports
    Given the user "<username>" has existing expense reports
    Then all of the user's expense reports should be displayed

  Examples:
    | username     |
    | bob_employee |

  Scenario Outline: User has no expense reports
    Given the user "<username>" has no expense reports
    Then the page should show no expense reports

  Examples:
    | username     |
    | bob_employee |

  Scenario Outline: Successful edit of existing expense
    Given the user is logged in as "<username>" and "<password>"
    And the user "<username>" has an expense "<expense_id>" to edit
    When the user clicks the edit button for "<expense_id>"
    And the user enters expense "<amount>"
    And the user enters description "<description>"
    And the user clicks the submit button
    Then the expense report updates after edit

  Examples:
    | username     | password    | expense_id | amount | description |
    | bob_employee | password123 | 5          | 25.00  | pizza party |


  Scenario Outline: Successful deletion of existing expense
    Given the user is logged in as "<username>" and "<password>"
    When the user clicks the delete button for "<expense_id>"
    And the user clicks the okay button
    Then the expense report is deleted

  Examples:
    | username     | password    | expense_id |
    | bob_employee | password123 | 12          |
