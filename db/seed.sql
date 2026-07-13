-- Seed data for Revature Expense Manager

-- Insert sample users
INSERT INTO users (username, password, role) VALUES
('alice_employee', 'password123', 'employee'),
('bob_employee', 'password123', 'employee'),
('charlie_employee', 'password123', 'employee'),
('manager_diana', 'manager123', 'manager'),
('manager_eve', 'manager123', 'manager');

-- Insert sample expenses
INSERT INTO expenses (user_id, amount, exp_description, date) VALUES
(1, 45.99, 'Lunch meeting with client', '2026-06-08'),
(1, 120.00, 'Software training course', '2026-06-07'),
(1, 25.50, 'Office supplies', '2026-06-09'),
(2, 89.99, 'Travel to conference', '2026-06-06'),
(2, 15.00, 'Team lunch', '2026-06-08'),
(3, 200.00, 'Equipment rental', '2026-06-05'),
(3, 35.75, 'Client dinner', '2026-06-09'),
(2, 52.00, 'Printer cartridge', '2026-06-09');

-- Insert sample approvals
INSERT INTO approvals (expense_id, status, reviewer, comment, review_date) VALUES
(1, 'approved', 4, 'Approved. Valid business expense.', '2026-06-09'),
(2, 'pending', NULL, NULL, NULL),
(3, 'denied', 5, 'Receipt missing. Resubmit with documentation.', '2026-06-09'),
(4, 'approved', 4, 'Conference attendance approved.', '2026-06-08'),
(5, 'pending', NULL, NULL, NULL),
(6, 'approved', 5, 'Approved for project needs.', '2026-06-07'),
(7, 'pending', NULL, NULL, NULL),
(8, 'denied', 4, 'Non-business expense. Cannot reimburse.', '2026-06-09');
