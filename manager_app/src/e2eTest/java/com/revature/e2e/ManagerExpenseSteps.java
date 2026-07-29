package com.revature.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ManagerExpenseSteps {

    private static final String EMPLOYEE_URL =
        System.getProperty("employee.baseUrl", "http://127.0.0.1:5001");
    private static final String MANAGER_URL =
        System.getProperty("manager.baseUrl", "http://127.0.0.1:8080");

    private WebDriver driver;
    private WebDriverWait wait;
    private String expenseDescription;
    private String expectedStatus;

    @Before
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @After
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("an employee submits a new expense")
    public void employeeSubmitsExpense() {
        expenseDescription = "E2E expense " + UUID.randomUUID();

        driver.get(EMPLOYEE_URL + "/login");
        type(By.name("username"), "alice_employee");
        type(By.name("password"), "password123");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("amount")));
        type(By.name("amount"), "42.50");
        type(By.name("description"), expenseDescription);
        type(By.name("date"), LocalDate.now().toString());
        driver.findElement(By.cssSelector("form[action$='/expenses'] button[type='submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(expenseRow()));
    }

    @When("the manager views and approves the expense")
    public void managerApprovesExpense() {
        updateExpenseStatus("approved", "Approved by automated E2E test");
    }

    @When("the manager views and denies the expense")
    public void managerDeniesExpense() {
        updateExpenseStatus("denied", "Denied by automated E2E test");
    }

    @Then("the employee should see the expense as approved")
    public void employeeSeesApprovedExpense() {
        verifyEmployeeStatus("approved");
    }

    @Then("the employee should see the expense as denied")
    public void employeeSeesDeniedExpense() {
        verifyEmployeeStatus("denied");
    }

    private void updateExpenseStatus(String status, String comment) {
        expectedStatus = status;

        driver.get(MANAGER_URL + "/login");
        type(By.id("username"), "manager_diana");
        type(By.id("password"), "manager123");
        driver.findElement(By.cssSelector("#login-form button[type='submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("approval-keyword")));
        type(By.id("approval-keyword"), expenseDescription);

        By matchingManagerRow = By.xpath(
            "//tbody[@id='approval-table-body']/tr[td[normalize-space()="
                + xpathLiteral(expenseDescription) + "]]"
        );
        WebElement managerRow = wait.until(
            ExpectedConditions.visibilityOfElementLocated(matchingManagerRow)
        );
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});",
            managerRow
        );
        wait.until(ExpectedConditions.elementToBeClickable(managerRow)).click();

        WebElement statusSelect = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("review-status"))
        );
        new Select(statusSelect).selectByValue(status);
        type(By.id("review-comment"), comment);

        WebElement saveButton = driver.findElement(
            By.cssSelector("#review-form button[type='submit']")
        );
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});",
            saveButton
        );
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();

        By managerStatusCell = By.xpath(
            "//tbody[@id='approval-table-body']/tr[td[normalize-space()="
                + xpathLiteral(expenseDescription) + "]]/td[5]"
        );
        wait.until(currentDriver -> {
            try {
                return currentDriver.findElement(managerStatusCell)
                    .getText()
                    .trim()
                    .equalsIgnoreCase(status);
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });
    }

    private void verifyEmployeeStatus(String status) {
        driver.get(EMPLOYEE_URL + "/dashboard");

        WebElement statusCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                "//tr[td[normalize-space()=" + xpathLiteral(expenseDescription) + "]]/td[4]"
            )
        ));

        assertEquals(expectedStatus, status);
        assertEquals(status, statusCell.getText().trim().toLowerCase());
    }

    private By expenseRow() {
        return By.xpath(
            "//tr[td[normalize-space()=" + xpathLiteral(expenseDescription) + "]]"
        );
    }

    private void type(By locator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value);
    }

    private static String xpathLiteral(String value) {
        return "'" + value.replace("'", "") + "'";
    }
}
