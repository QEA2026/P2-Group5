package com.revature.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.revature.DAOs.ApprovalDAO;
import com.revature.DAOs.AuthDAO;
import com.revature.DAOs.ManagerPortalDAO;
import com.revature.Main;
import com.revature.models.Approval;
import com.revature.models.ManagerExpenseApprovalRecord;
import com.revature.models.ManagerSummary;
import com.revature.models.User;

import io.javalin.Javalin;

class ManagerPlainSeleniumE2ETest {

    private static final User MANAGER =
        new User(4, "manager_diana", "manager123", "manager");

    private TestAuthDAO authDAO;
    private TestApprovalDAO approvalDAO;
    private TestManagerPortalDAO portalDAO;
    private Javalin app;
    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        authDAO = new TestAuthDAO();
        portalDAO = new TestManagerPortalDAO();
        approvalDAO = new TestApprovalDAO(portalDAO);

        app = Main.createApp(authDAO, approvalDAO, portalDAO);
        app.start(0);
        baseUrl = "http://127.0.0.1:" + app.port();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1440,1100");
        if (Boolean.getBoolean("selenium.headless")) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            if (app != null) {
                app.stop();
            }
        }
    }

    @Test
    void rejectsInvalidManagerCredentials() {
        driver.get(baseUrl + "/login");
        type(By.id("username"), "not_a_manager");
        type(By.id("password"), "wrong-password");
        driver.findElement(By.cssSelector("#login-form button[type='submit']")).click();

        WebElement message = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("login-message"))
        );

        assertEquals("Invalid manager credentials.", message.getText().trim());
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertEquals("not_a_manager", authDAO.lastUsername);
        assertEquals("wrong-password", authDAO.lastPassword);
    }

    @Test
    void filtersApprovalsByKeyword() {
        String keyword = "selenium-keyword-target";
        ManagerExpenseApprovalRecord matchingApproval = record(
            9101,
            8101,
            keyword,
            "pending",
            null,
            null
        );
        portalDAO.approvalRecords = records(matchingApproval);

        loginAsManager();
        type(By.id("approval-keyword"), keyword);

        WebElement matchingRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                "//tbody[@id='approval-table-body']/tr[td[normalize-space()='"
                    + keyword + "']]"
            )
        ));
        List<WebElement> cells = matchingRow.findElements(By.tagName("td"));

        assertEquals("selenium_employee", cells.get(0).getText().trim());
        assertEquals(keyword, cells.get(1).getText().trim());
        assertEquals("Pending", cells.get(4).getText().trim());
    }

    @Test
    void displaysExpenseInReportsView() {
        String description = "plain-selenium-report-entry";
        ManagerExpenseApprovalRecord reportRecord = record(
            9201,
            8201,
            description,
            "approved",
            "manager_diana",
            "Report verified by Selenium"
        );
        portalDAO.reportRecords = records(reportRecord);

        loginAsManager();

        WebElement reportRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                "//tbody[@id='report-table-body']/tr[td[normalize-space()='"
                    + description + "']]"
            )
        ));
        List<WebElement> cells = reportRow.findElements(By.tagName("td"));

        assertEquals("selenium_employee", cells.get(0).getText().trim());
        assertEquals(description, cells.get(1).getText().trim());
        assertEquals("Approved", cells.get(4).getText().trim());
        assertEquals("manager_diana", cells.get(5).getText().trim());
        assertEquals("Report verified by Selenium", cells.get(6).getText().trim());
    }

    @Test
    void updatesApprovalStatusAndComment() {
        String description = "plain-selenium-approval-update";
        String comment = "Approved through the manager Selenium form";
        ManagerExpenseApprovalRecord pendingRecord = record(
            9301,
            8301,
            description,
            "pending",
            null,
            null
        );
        portalDAO.approvalRecords = records(pendingRecord);
        approvalDAO.existingApproval = new Approval(
            9301,
            8301,
            "pending",
            0,
            null,
            null
        );

        loginAsManager();

        WebElement approvalRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            rowContaining("approval-table-body", description)
        ));
        scrollAndClick(approvalRow);

        WebElement statusSelect = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("review-status"))
        );
        new Select(statusSelect).selectByValue("approved");
        type(By.id("review-comment"), comment);

        WebElement saveButton = driver.findElement(
            By.cssSelector("#review-form button[type='submit']")
        );
        scrollAndClick(saveButton);

        WebElement updatedRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            rowContaining("approval-table-body", description)
        ));
        wait.until(currentDriver ->
            updatedRow.findElements(By.tagName("td"))
                .get(4)
                .getText()
                .trim()
                .equalsIgnoreCase("approved")
        );

        assertEquals("approved", approvalDAO.lastUpdatedApproval.getStatus());
        assertEquals(comment, approvalDAO.lastUpdatedApproval.getComment());
        assertEquals(MANAGER.getUser_id(), approvalDAO.lastUpdatedApproval.getReviewer_id());
    }

    @Test
    void filtersApprovalsByStatus() {
        portalDAO.approvalRecords = new ArrayList<>(List.of(
            record(9401, 8401, "pending-filter-record", "pending", null, null),
            record(
                9402,
                8402,
                "approved-filter-record",
                "approved",
                "manager_diana",
                "Approved fixture"
            ),
            record(
                9403,
                8403,
                "denied-filter-record",
                "denied",
                "manager_diana",
                "Denied fixture"
            )
        ));

        loginAsManager();

        assertStatusFilter("pending", "pending-filter-record");
        assertStatusFilter("approved", "approved-filter-record");
        assertStatusFilter("denied", "denied-filter-record");
    }

    @Test
    void blocksUnauthorizedAccessToApprovalAndReportDashboard() {
        driver.get(baseUrl + "/dashboard");

        wait.until(ExpectedConditions.urlContains("/login"));

        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-form")))
                .isDisplayed()
        );
    }

    @Test
    void logoutInvalidatesManagerSession() {
        loginAsManager();

        scrollAndClick(driver.findElement(By.id("logout-button")));
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.get(baseUrl + "/dashboard");

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-form")))
                .isDisplayed()
        );
    }

    @Test
    void approvalUpdatePersistsAfterBrowserRefresh() {
        String description = "persistent-selenium-approval";
        String comment = "This decision must remain after refresh";
        portalDAO.approvalRecords = records(
            record(9501, 8501, description, "pending", null, null)
        );
        approvalDAO.existingApproval = new Approval(
            9501,
            8501,
            "pending",
            0,
            null,
            null
        );

        loginAsManager();
        scrollAndClick(
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                rowContaining("approval-table-body", description)
            ))
        );

        new Select(driver.findElement(By.id("review-status"))).selectByValue("denied");
        type(By.id("review-comment"), comment);
        scrollAndClick(
            driver.findElement(By.cssSelector("#review-form button[type='submit']"))
        );

        wait.until(currentDriver ->
            approvalDAO.lastUpdatedApproval != null
                && "denied".equals(approvalDAO.lastUpdatedApproval.getStatus())
        );

        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            rowContaining("approval-table-body", description)
        ));
        WebElement refreshedStatus = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("review-status"))
        );
        WebElement refreshedComment = driver.findElement(By.id("review-comment"));

        assertEquals("denied", new Select(refreshedStatus).getFirstSelectedOption().getAttribute("value"));
        assertEquals(comment, refreshedComment.getAttribute("value"));
    }

    @Test
    void filtersReportsByEmployeeAndStatus() {
        portalDAO.reportRecords = new ArrayList<>(List.of(
            recordForEmployee(
                9601,
                8601,
                "report_alice",
                "alice-pending-report",
                "pending",
                null,
                null
            ),
            recordForEmployee(
                9602,
                8602,
                "report_alice",
                "alice-approved-report",
                "approved",
                "manager_diana",
                "Approved Alice report"
            ),
            recordForEmployee(
                9603,
                8603,
                "report_bob",
                "bob-approved-report",
                "approved",
                "manager_diana",
                "Approved Bob report"
            )
        ));

        loginAsManager();

        new Select(driver.findElement(By.id("report-status"))).selectByValue("approved");
        new Select(driver.findElement(By.id("report-employee"))).selectByValue("report_alice");

        wait.until(currentDriver -> {
            List<WebElement> rows = currentDriver.findElements(
                By.cssSelector("#report-table-body tr")
            );
            if (rows.size() != 1) {
                return false;
            }

            List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
            return "report_alice".equals(cells.get(0).getText().trim())
                && "alice-approved-report".equals(cells.get(1).getText().trim())
                && "Approved".equals(cells.get(4).getText().trim());
        });
    }

    @Test
    void displaysEmptyStateWhenApprovalSearchHasNoMatches() {
        portalDAO.approvalRecords = records(
            record(
                9701,
                8701,
                "visible-approval-record",
                "pending",
                null,
                null
            )
        );

        loginAsManager();
        type(By.id("approval-keyword"), "expense-that-does-not-exist");

        WebElement emptyState = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("approval-empty"))
        );

        assertEquals("No approvals match these filters.", emptyState.getText().trim());
        assertTrue(driver.findElements(By.cssSelector("#approval-table-body tr")).isEmpty());
    }

    private void loginAsManager() {
        driver.get(baseUrl + "/login");
        type(By.id("username"), "manager_diana");
        type(By.id("password"), "manager123");
        driver.findElement(By.cssSelector("#login-form button[type='submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("manager-name")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("approval-keyword")));
    }

    private void assertStatusFilter(String status, String expectedDescription) {
        new Select(driver.findElement(By.id("approval-status"))).selectByValue(status);

        wait.until(currentDriver -> {
            List<WebElement> rows = currentDriver.findElements(
                By.cssSelector("#approval-table-body tr")
            );
            if (rows.size() != 1) {
                return false;
            }

            List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
            return expectedDescription.equals(cells.get(1).getText().trim())
                && status.equalsIgnoreCase(cells.get(4).getText().trim());
        });
    }

    private void scrollAndClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});",
            element
        );
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    private static By rowContaining(String tableBodyId, String value) {
        return By.xpath(
            "//tbody[@id='" + tableBodyId + "']/tr[td[normalize-space()='"
                + value + "']]"
        );
    }

    private void type(By locator, String value) {
        WebElement field = wait.until(
            ExpectedConditions.visibilityOfElementLocated(locator)
        );
        field.clear();
        field.sendKeys(value);
    }

    private static ArrayList<ManagerExpenseApprovalRecord> records(
        ManagerExpenseApprovalRecord record
    ) {
        return new ArrayList<>(List.of(record));
    }

    private static ManagerExpenseApprovalRecord record(
        int approvalId,
        int expenseId,
        String description,
        String status,
        String reviewerUsername,
        String comment
    ) {
        return recordForEmployee(
            approvalId,
            expenseId,
            "selenium_employee",
            description,
            status,
            reviewerUsername,
            comment
        );
    }

    private static ManagerExpenseApprovalRecord recordForEmployee(
        int approvalId,
        int expenseId,
        String employeeUsername,
        String description,
        String status,
        String reviewerUsername,
        String comment
    ) {
        Integer reviewerId = reviewerUsername == null ? null : MANAGER.getUser_id();
        return new ManagerExpenseApprovalRecord(
            approvalId,
            expenseId,
            7001,
            employeeUsername,
            125.75,
            description,
            "2026-07-29",
            status,
            reviewerId,
            reviewerUsername,
            comment,
            reviewerUsername == null ? null : "2026-07-29"
        );
    }

    private static final class TestAuthDAO extends AuthDAO {

        private String lastUsername;
        private String lastPassword;

        @Override
        public User login(String username, String password) {
            lastUsername = username;
            lastPassword = password;

            if ("manager_diana".equals(username) && "manager123".equals(password)) {
                return MANAGER;
            }
            return null;
        }
    }

    private static final class TestApprovalDAO extends ApprovalDAO {

        private final TestManagerPortalDAO portalDAO;
        private Approval existingApproval;
        private Approval lastUpdatedApproval;

        private TestApprovalDAO(TestManagerPortalDAO portalDAO) {
            this.portalDAO = portalDAO;
        }

        @Override
        public Approval getApprovalByID(int id) {
            if (existingApproval != null && existingApproval.getApproval_id() == id) {
                return existingApproval;
            }
            return null;
        }

        @Override
        public Approval updateApproval(Approval approval) {
            lastUpdatedApproval = approval;
            portalDAO.applyApprovalUpdate(approval);
            return approval;
        }
    }

    private static final class TestManagerPortalDAO extends ManagerPortalDAO {

        private ArrayList<ManagerExpenseApprovalRecord> approvalRecords =
            new ArrayList<>();
        private ArrayList<ManagerExpenseApprovalRecord> reportRecords =
            new ArrayList<>();

        @Override
        public ArrayList<ManagerExpenseApprovalRecord> getApprovalRecords(
            String status,
            String employee,
            String reviewer,
            String keyword
        ) {
            ArrayList<ManagerExpenseApprovalRecord> filteredRecords = new ArrayList<>();
            for (ManagerExpenseApprovalRecord record : approvalRecords) {
                if (matchesStatus(record, status)
                    && matchesEmployee(record, employee)
                    && matchesReviewer(record, reviewer)
                    && matchesKeyword(record, keyword)) {
                    filteredRecords.add(record);
                }
            }
            return filteredRecords;
        }

        @Override
        public ArrayList<ManagerExpenseApprovalRecord> getExpenseReports(
            String status,
            String employee,
            String startDate,
            String endDate,
            String keyword
        ) {
            ArrayList<ManagerExpenseApprovalRecord> filteredRecords = new ArrayList<>();
            for (ManagerExpenseApprovalRecord record : reportRecords) {
                if (matchesStatus(record, status)
                    && matchesEmployee(record, employee)
                    && matchesKeyword(record, keyword)) {
                    filteredRecords.add(record);
                }
            }
            return filteredRecords;
        }

        @Override
        public ManagerSummary getSummary() {
            return new ManagerSummary(0, 0, 0, 0);
        }

        @Override
        public ArrayList<String> getEmployees() {
            ArrayList<String> employees = new ArrayList<>();
            addEmployees(employees, approvalRecords);
            addEmployees(employees, reportRecords);
            return employees;
        }

        @Override
        public ArrayList<String> getManagers() {
            return new ArrayList<>(List.of(MANAGER.getUsername()));
        }

        private static boolean matchesStatus(
            ManagerExpenseApprovalRecord record,
            String status
        ) {
            return status == null || status.equalsIgnoreCase(record.getStatus());
        }

        private static boolean matchesEmployee(
            ManagerExpenseApprovalRecord record,
            String employee
        ) {
            return employee == null
                || employee.equalsIgnoreCase(record.getEmployeeUsername());
        }

        private static boolean matchesReviewer(
            ManagerExpenseApprovalRecord record,
            String reviewer
        ) {
            return reviewer == null
                || reviewer.equalsIgnoreCase(record.getReviewerUsername());
        }

        private static boolean matchesKeyword(
            ManagerExpenseApprovalRecord record,
            String keyword
        ) {
            if (keyword == null) {
                return true;
            }

            String normalizedKeyword = keyword.toLowerCase();
            return record.getDescription().toLowerCase().contains(normalizedKeyword)
                || record.getEmployeeUsername().toLowerCase().contains(normalizedKeyword);
        }

        private static void addEmployees(
            ArrayList<String> employees,
            ArrayList<ManagerExpenseApprovalRecord> records
        ) {
            for (ManagerExpenseApprovalRecord record : records) {
                if (!employees.contains(record.getEmployeeUsername())) {
                    employees.add(record.getEmployeeUsername());
                }
            }
        }

        private void applyApprovalUpdate(Approval approval) {
            for (int index = 0; index < approvalRecords.size(); index++) {
                ManagerExpenseApprovalRecord currentRecord = approvalRecords.get(index);
                if (currentRecord.getApprovalId() != approval.getApproval_id()) {
                    continue;
                }

                approvalRecords.set(index, new ManagerExpenseApprovalRecord(
                    currentRecord.getApprovalId(),
                    currentRecord.getExpenseId(),
                    currentRecord.getEmployeeId(),
                    currentRecord.getEmployeeUsername(),
                    currentRecord.getAmount(),
                    currentRecord.getDescription(),
                    currentRecord.getExpenseDate(),
                    approval.getStatus(),
                    approval.getReviewer_id(),
                    MANAGER.getUsername(),
                    approval.getComment(),
                    approval.getReview_date()
                ));
            }
        }
    }
}
