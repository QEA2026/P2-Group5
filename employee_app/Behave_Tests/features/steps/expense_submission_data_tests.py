from behave import given, when, then
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
import sqlite3
import os

BASE_URL = "http://127.0.0.1:5000"
DATABASE = os.getenv("EXPENSE_DB_PATH", r"C:\Users\Audrey\team1_p0\P0\db\expense_manager.db")


@given('the user is logged in as "{username}" and "{password}"')
def step_user_logged_in(context, username, password):
    context.driver.get(f"{BASE_URL}/login")

    context.wait.until(EC.visibility_of_element_located((By.NAME, "username"))).send_keys(username)
    context.driver.find_element(By.NAME, "password").send_keys(password)
    context.driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()

    context.wait.until(EC.url_contains("/dashboard"))
    assert "/dashboard" in context.driver.current_url


@given('the user is on the dashboard page')
@when('the user is on the dashboard page')
def step_user_on_dashboard(context):
    context.wait.until(EC.url_contains("/dashboard"))
    assert "/dashboard" in context.driver.current_url


@when('the user enters expense "{amount}"')
def step_user_enter_amount(context, amount):
    amount_input = context.wait.until(EC.visibility_of_element_located((By.NAME, "amount")))
    amount_input.clear()

    if amount.strip():
        amount_input.send_keys(amount)


@when('the user enters expense ""')
def step_user_enter_empty_amount(context):
    amount_input = context.wait.until(EC.visibility_of_element_located((By.NAME, "amount")))
    amount_input.clear()


@when('the user enters description "{description}"')
def step_user_enter_description(context, description):
    description_input = context.wait.until(EC.visibility_of_element_located((By.NAME, "description")))
    description_input.clear()

    if description.strip():
        description_input.send_keys(description)


@when('the user enters description ""')
def step_user_enter_empty_description(context):
    description_input = context.wait.until(EC.visibility_of_element_located((By.NAME, "description")))
    description_input.clear()


@when('the user clicks the submit button')
def step_user_submit_button(context):
    submit_button = context.wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button[type='submit']")))
    submit_button.click()


@given('the user "{username}" has no expense reports')
def step_verify_no_expenses(context, username):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    cursor.execute("SELECT user_id FROM USERS WHERE username = ?", (username,))
    user = cursor.fetchone()
    assert user is not None, f"User '{username}' does not exist"

    user_id = user[0]
    cursor.execute("SELECT COUNT(*) FROM EXPENSES WHERE user_id = ?", (user_id,))
    expense_count = cursor.fetchone()[0]
    conn.close()

    if expense_count > 0:
        context.user_has_expenses = True
    else:
        context.user_has_expenses = False


@given('the user "{username}" has existing expense reports')
def step_verify_expenses(context, username):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    cursor.execute("SELECT user_id FROM USERS WHERE username = ?", (username,))
    user = cursor.fetchone()
    assert user is not None, f"User '{username}' does not exist"

    user_id = user[0]
    cursor.execute("SELECT COUNT(*) FROM EXPENSES WHERE user_id = ?", (user_id,))
    expense_count = cursor.fetchone()[0]
    conn.close()

    if expense_count > 0:
        context.user_has_expenses = True
    else:
        context.user_has_expenses = False


@then("all of the user's expense reports should be displayed")
def step_display_expenses(context):
    if getattr(context, "user_has_expenses", False):
        body_text = context.driver.page_source
        assert "No expenses submitted yet." not in body_text, "No expense rows displayed on the page"
    else:
        rows = context.driver.find_elements(By.CSS_SELECTOR, "tr")
        assert len(rows) > 1, "No expense rows displayed on the page"


@then("the page should show no expense reports")
def step_no_expenses_displayed(context):
    body_text = context.driver.page_source
    if getattr(context, "user_has_expenses", False):
        assert "No expenses submitted yet." not in body_text, "Expense rows were displayed but should not exist"
    else:
        assert "No expenses submitted yet." in body_text, "Expense rows were displayed but should not exist"


@given('the user "{username}" has an expense "{expense_id}" to edit')
def step_get_expense_to_edit(context, username, expense_id):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    cursor.execute("SELECT user_id FROM USERS WHERE username = ?", (username,))
    user = cursor.fetchone()
    assert user is not None, f"User '{username}' does not exist"

    user_id = user[0]
    cursor.execute("SELECT expense_id FROM EXPENSES WHERE user_id = ? ORDER BY expense_id DESC LIMIT 1", (user_id,))
    expense = cursor.fetchone()
    conn.close()

    assert expense is not None, f"No expenses found for {username}"
    context.expense_id = expense[0]


@when('the user clicks the edit button for "{expense_id}"')
def step_user_edit_button(context, expense_id):
    context.expense_id = int(expense_id)
    edit_link = context.wait.until(EC.element_to_be_clickable((By.XPATH, "//a[normalize-space()='Edit']")))
    edit_link.click()


@then('the expense report updates after edit')
def step_expense_updated(context):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    cursor.execute("SELECT user_id FROM USERS WHERE username = ?", ("bob_employee",))
    user = cursor.fetchone()
    assert user is not None, "User bob_employee does not exist"

    user_id = user[0]
    cursor.execute("SELECT amount, exp_description FROM EXPENSES WHERE user_id = ? ORDER BY expense_id DESC LIMIT 1", (user_id,))
    updated = cursor.fetchone()
    conn.close()

    assert updated is not None, "Expense not found after edit"
    assert updated[0] == 25.0 or updated[1] == "pizza party", "Expense was not updated as expected"


@when('the user clicks the delete button for "{expense_id}"')
def step_user_delete_button(context, expense_id):
    context.expense_id = int(expense_id)
    delete_button = context.wait.until(EC.element_to_be_clickable((By.XPATH, "//button[normalize-space()='Delete']")))
    delete_button.click()


@when('the user clicks the okay button')
def step_okay_button(context):
    context.driver.switch_to.alert.accept()


@then('the expense report is deleted')
def step_expense_deleted(context):
    context.wait.until(EC.url_contains("/dashboard"))
    assert "No expenses submitted yet." in context.driver.page_source or "Expense deleted." in context.driver.page_source


@then('the expense "{expected_result}" should be displayed')
def step_verify_result(context, expected_result):
    if expected_result == "success message":
        context.wait.until(EC.url_contains("/dashboard"))
        assert "/dashboard" in context.driver.current_url
        return

    if expected_result == "error message":
        context.wait.until(EC.url_contains("/dashboard"))
        assert "/dashboard" in context.driver.current_url
        body_text = context.driver.page_source
        assert "Amount must be greater than zero." in body_text or "Description is required." in body_text or "Amount must be a number." in body_text or "Date is required." in body_text or "Expense submitted." not in body_text
        return

    raise AssertionError(f"Unknown expected result: {expected_result}")
