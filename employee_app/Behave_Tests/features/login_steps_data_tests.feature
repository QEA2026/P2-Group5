Feature: User Authentication
  As a registered user
  I want to log into my account
  So that I can access my personalized dashboard

  Background:
    Given the application is running
    And the test database is seeded with users

  Scenario Outline: Successful login with valid credentials
    Given the user is on the login page
    When the user enters username "<username>"
    And the user enters password "<password>"
    And the user clicks the login button
    Then the "<expected_result>" should be displayed
    And the user should be on the "<expected_page>" page

    Examples: Valid Credentials
      | username       | password    | expected_page | expected_result   |
      | bob_employee   | password123 | dashboard     | success message   |
      | alice_employee | password123 | dashboard     | success message   |

    Examples: Invalid Credentials
      | username       | password      | expected_page | expected_result |
      | bob_employee   | wrongpassword | login         | error message   |
      | wronguser      | anypassword   | login         | error message   |
      |                | somepassword  | login         | error message   |
      | bob_employee   |               | login         | error message   |
      | manager_eve    | manager123    | login         | error message   |


  Scenario Outline: Successful logout after logging in
    Given the user is on the login page
    When the user enters username "<username>"
    And the user enters password "<password>"
    And the user clicks the login button
    And the user clicks the logout control
    Then the user should be redirected to the login page
    And the user cannot access the dashboard after logout

    Examples:
      | username       | password    |
      | bob_employee   | password123 |
      | alice_employee | password123 |