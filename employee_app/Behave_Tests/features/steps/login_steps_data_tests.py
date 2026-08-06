from behave import given, when, then
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import sqlite3
import re

BASE_URL = "http://127.0.0.1:5000"
DATABASE = r"C:\Users\Audrey\team1_p0\P0\db\expense_manager.db"


@given("the application is running")
def step_application_running(context):
    options = Options()
    options.add_argument("--start-maximized")
    # options.add_argument("--headless=new")

    context.driver = webdriver.Chrome(options=options)
    context.wait = WebDriverWait(context.driver, 10)

    context.driver.get(BASE_URL)

    heading = context.wait.until(
        EC.visibility_of_element_located((By.TAG_NAME, "h1"))
    )
    assert heading.text == "Employee Expenses"


@given("the test database is seeded with users")
def step_database_seeded(context):
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()

    cursor.execute("SELECT COUNT(*) FROM USERS")
    count = cursor.fetchone()[0]

    conn.close()

    assert count > 0, "Database contains no users."


@given("the user is on the login page")
def step_user_on_login_page(context):
    context.driver.get(f"{BASE_URL}/login")

    context.wait.until(
        EC.visibility_of_element_located((By.NAME, "username"))
    )


@when('the user enters username "{username}"')
def step_enter_username(context, username):
    username_input = context.wait.until(
        EC.visibility_of_element_located((By.NAME, "username"))
    )
    username_input.clear()

    if username:
        username_input.send_keys(username)


@when('the user enters username ""')
def step_enter_empty_username(context):
    username_input = context.wait.until(
        EC.visibility_of_element_located((By.NAME, "username"))
    )
    username_input.clear()


@when('the user enters password "{password}"')
def step_enter_password(context, password):
    password_input = context.wait.until(
        EC.visibility_of_element_located((By.NAME, "password"))
    )
    password_input.clear()

    if password:
        password_input.send_keys(password)


@when('the user enters password ""')
def step_enter_empty_password(context):
    password_input = context.wait.until(
        EC.visibility_of_element_located((By.NAME, "password"))
    )
    password_input.clear()


@when("the user clicks the login button")
def step_click_login(context):
    login_button = context.wait.until(
        EC.element_to_be_clickable((By.CSS_SELECTOR, "button[type='submit']"))
    )
    login_button.click()


@when("the user clicks the logout control")
def step_click_logout(context):
    logout_selectors = [
        (By.CSS_SELECTOR, "a[href*='logout']"),
        (By.CSS_SELECTOR, "button[href*='logout']"),
        (By.XPATH, "//a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'logout')]"),
        (By.XPATH, "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'logout')]"),
        (By.XPATH, "//a[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sign out')]"),
        (By.XPATH, "//button[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'sign out')]"),
    ]

    for by, selector in logout_selectors:
        matches = context.driver.find_elements(by, selector)
        if matches:
            context.wait.until(EC.element_to_be_clickable((by, selector))).click()
            return

    # Fallback for apps that expose only a logout route.
    context.driver.get(f"{BASE_URL}/logout")


@then('the "{expected_result}" should be displayed')
def step_verify_result(context, expected_result):
    if expected_result == "success message":
        context.wait.until(EC.url_contains("/dashboard"))
        assert "/dashboard" in context.driver.current_url

        heading2 = context.wait.until(
            EC.visibility_of_element_located((By.TAG_NAME, "h2"))
        )
        assert heading2.text == "Submit an expense"
        return

    if expected_result == "error message":
        context.wait.until(EC.url_contains("/login"))
        assert "/login" in context.driver.current_url
        return

    raise AssertionError(f"Unknown expected result: {expected_result}")


@then('the user should be on the "{expected_page}" page')
def step_verify_page(context, expected_page):
    if expected_page == "dashboard":
        context.wait.until(EC.url_contains("/dashboard"))
        assert "/dashboard" in context.driver.current_url
    elif expected_page == "login":
        context.wait.until(EC.url_contains("/login"))
        assert "/login" in context.driver.current_url
    elif expected_page == "edit":
        url = context.driver.current_url
        assert re.search(r"/expenses/\d+/edit", url), f"Unexpected URL: {url}"
    else:
        raise AssertionError(f"Unknown expected page: {expected_page}")


@then("the user should be redirected to the login page")
def step_redirected_to_login(context):
    context.wait.until(EC.url_contains("/login"))
    assert "/login" in context.driver.current_url

    context.wait.until(
        EC.visibility_of_element_located((By.NAME, "username"))
    )


@then("the user cannot access the dashboard after logout")
def step_dashboard_blocked_after_logout(context):
    context.driver.get(f"{BASE_URL}/dashboard")

    context.wait.until(lambda driver: "/login" in driver.current_url or "/dashboard" in driver.current_url)
    assert "/login" in context.driver.current_url, (
        f"Expected to be redirected to login after logout, but was at: {context.driver.current_url}"
    )
