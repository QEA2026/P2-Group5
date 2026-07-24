package com.revature.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.revature.models.ManagerExpenseApprovalRecord;
import com.revature.models.ManagerSummary;
import com.revature.utils.ConnectionUtil;

public class ManagerPortalDAO {

    public ArrayList<ManagerExpenseApprovalRecord> getApprovalRecords(
        String status,
        String employee,
        String reviewer,
        String keyword
    ) {
        StringBuilder sql = new StringBuilder(baseQuery());
        List<Object> parameters = new ArrayList<>();

        appendStatusFilter(sql, parameters, status);
        appendEmployeeFilter(sql, parameters, employee);
        appendReviewerFilter(sql, parameters, reviewer);
        appendKeywordFilter(sql, parameters, keyword);
        sql.append(" ORDER BY CASE a.status WHEN 'pending' THEN 0 WHEN 'approved' THEN 1 ELSE 2 END, e.date DESC, a.approval_id DESC");

        return runRecordQuery(sql.toString(), parameters);
    }

    public ArrayList<ManagerExpenseApprovalRecord> getExpenseReports(
        String status,
        String employee,
        String startDate,
        String endDate,
        String keyword
    ) {
        StringBuilder sql = new StringBuilder(baseQuery());
        List<Object> parameters = new ArrayList<>();

        appendStatusFilter(sql, parameters, status);
        appendEmployeeFilter(sql, parameters, employee);
        appendDateFilter(sql, parameters, startDate, endDate);
        appendKeywordFilter(sql, parameters, keyword);
        sql.append(" ORDER BY e.date DESC, a.approval_id DESC");

        return runRecordQuery(sql.toString(), parameters);
    }

    public ManagerSummary getSummary() {
        String sql = """
            SELECT
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count,
                SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) AS approved_count,
                SUM(CASE WHEN status = 'denied' THEN 1 ELSE 0 END) AS denied_count
            FROM approvals;
            """;

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new ManagerSummary(
                    resultSet.getInt("total"),
                    resultSet.getInt("pending_count"),
                    resultSet.getInt("approved_count"),
                    resultSet.getInt("denied_count")
                );
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return new ManagerSummary(0, 0, 0, 0);
    }

    public ArrayList<String> getEmployees() {
        return getUsernamesByRole("employee");
    }

    public ArrayList<String> getManagers() {
        return getUsernamesByRole("manager");
    }

    private ArrayList<String> getUsernamesByRole(String role) {
        String sql = "SELECT username FROM users WHERE role = ? ORDER BY username;";
        ArrayList<String> usernames = new ArrayList<>();

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, role);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                usernames.add(resultSet.getString("username"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return usernames;
    }

    private String baseQuery() {
        return """
            SELECT
                a.approval_id,
                a.expense_id,
                e.user_id AS employee_id,
                employee.username AS employee_username,
                e.amount,
                e.exp_description,
                e.date AS expense_date,
                a.status,
                a.reviewer,
                reviewer.username AS reviewer_username,
                a.comment,
                a.review_date
            FROM approvals a
            JOIN expenses e ON e.expense_id = a.expense_id
            JOIN users employee ON employee.user_id = e.user_id
            LEFT JOIN users reviewer ON reviewer.user_id = a.reviewer
            WHERE 1 = 1
            """;
    }

    private void appendStatusFilter(StringBuilder sql, List<Object> parameters, String status) {
        if (status != null) {
            sql.append(" AND LOWER(a.status) = ?");
            parameters.add(status);
        }
    }

    private void appendEmployeeFilter(StringBuilder sql, List<Object> parameters, String employee) {
        if (employee != null) {
            sql.append(" AND LOWER(employee.username) = ?");
            parameters.add(employee);
        }
    }

    private void appendReviewerFilter(StringBuilder sql, List<Object> parameters, String reviewer) {
        if (reviewer != null) {
            sql.append(" AND LOWER(COALESCE(reviewer.username, '')) = ?");
            parameters.add(reviewer);
        }
    }

    private void appendKeywordFilter(StringBuilder sql, List<Object> parameters, String keyword) {
        if (keyword != null) {
            sql.append(" AND (LOWER(e.exp_description) LIKE ? OR LOWER(employee.username) LIKE ?)");
            String searchValue = "%" + keyword + "%";
            parameters.add(searchValue);
            parameters.add(searchValue);
        }
    }

    private void appendDateFilter(StringBuilder sql, List<Object> parameters, String startDate, String endDate) {
        if (startDate != null) {
            sql.append(" AND e.date >= ?");
            parameters.add(startDate);
        }

        if (endDate != null) {
            sql.append(" AND e.date <= ?");
            parameters.add(endDate);
        }
    }

    private ArrayList<ManagerExpenseApprovalRecord> runRecordQuery(String sql, List<Object> parameters) {
        ArrayList<ManagerExpenseApprovalRecord> records = new ArrayList<>();

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            bindParameters(statement, parameters);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Integer reviewerId = null;
                int rawReviewerId = resultSet.getInt("reviewer");
                if (!resultSet.wasNull()) {
                    reviewerId = rawReviewerId;
                }

                records.add(new ManagerExpenseApprovalRecord(
                    resultSet.getInt("approval_id"),
                    resultSet.getInt("expense_id"),
                    resultSet.getInt("employee_id"),
                    resultSet.getString("employee_username"),
                    resultSet.getDouble("amount"),
                    resultSet.getString("exp_description"),
                    resultSet.getString("expense_date"),
                    resultSet.getString("status"),
                    reviewerId,
                    resultSet.getString("reviewer_username"),
                    resultSet.getString("comment"),
                    resultSet.getString("review_date")
                ));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return records;
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            statement.setObject(index + 1, parameter);
        }
    }
}
