import os
import sqlite3
import sys
import tempfile
import unittest

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

from app import app
import database


class TestExpenseManagerDatabase(unittest.TestCase):
    def setUp(self):
        app.config["TESTING"] = True
        app.config["WTF_CSRF_ENABLED"] = False

        self._original_db_path = database.DB_PATH
        self.temp_dir = tempfile.TemporaryDirectory()
        self.db_path = os.path.join(self.temp_dir.name, "expense_manager_test.db")
        database.DB_PATH = self.db_path

        conn = sqlite3.connect(self.db_path)
        with open(os.path.join(os.path.dirname(__file__), "..", "..", "db", "schema.sql"), "r", encoding="utf-8") as f:
            conn.executescript(f.read())
        with open(os.path.join(os.path.dirname(__file__), "..", "..", "db", "seed.sql"), "r", encoding="utf-8") as f:
            conn.executescript(f.read())
        conn.commit()
        conn.close()

        self.client = app.test_client()

    def tearDown(self):
        self.temp_dir.cleanup()
        database.DB_PATH = self._original_db_path

    # Test if logging in redirects user to dashboard
    def test_login_redirects_to_dashboard(self):
        response = self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
            follow_redirects=True,
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"dashboard", response.data.lower())

    # Test redirect to login when trying to access dashboard without logging in 
    def test_dashboard_redirect_when_not_logged_in(self):
        response = self.client.get("/dashboard", follow_redirects=True)

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Please log in to continue.", response.data)
        with self.client.session_transaction() as sess:
            self.assertNotIn("user_id", sess)
            self.assertNotIn("username", sess)

    # test failed login with wrong username and password 
    def test_failed_login(self):
        response = self.client.post(
            "/login",
            data={"username": "wronguser", "password": "wrongpass"},
            follow_redirects=True,
        )

        self.assertIn(b"Invalid username or password", response.data)
        with self.client.session_transaction() as sess:
            self.assertNotIn("user_id", sess)
            self.assertNotIn("username", sess)

    # test submitting an expense 
    def test_submit_expense(self):
        self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
        )

        response = self.client.post(
            "/expenses",
            data={"amount": "25.99", "description": "Printer paper", "date": "2026-07-29"},
            follow_redirects=True,
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Expense submitted.", response.data)
        self.assertIn(b"dashboard", response.data.lower())

        with self.client.session_transaction() as sess:
            self.assertEqual(sess["user_id"], 2)
            self.assertEqual(sess["username"], "bob_employee")

    # test editing an expense 
    def test_edit_expense(self):
        self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
        )

        expense_id = 5
        response = self.client.post(
            f"/expenses/{expense_id}/edit",
            data={"amount": "99.99", "description": "Updated travel expense", "date": "2026-07-30"},
            follow_redirects=True,
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"dashboard", response.data.lower())

        updated_expense = database.get_expense_by_id(expense_id)
        self.assertIsNotNone(updated_expense)
        self.assertEqual(updated_expense.amount, 99.99)
        self.assertEqual(updated_expense.description, "Updated travel expense")
        self.assertEqual(updated_expense.date, "2026-07-30")

    # test deleting an expense 
    def test_delete_expense(self):
        self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
        )

        expense_id = 5
        response = self.client.post(f"/expenses/{expense_id}/delete", follow_redirects=True)

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"dashboard", response.data.lower())

        deleted_expense = database.get_expense_by_id(expense_id)
        self.assertIsNone(deleted_expense)

    # test dashboard showing all user expenses once logged in 
    def test_dashboard_shows_user_expenses(self):
        self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
        )

        response = self.client.get("/dashboard")

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Travel to conference", response.data)
        self.assertIn(b"Printer cartridge", response.data)

    # test attempting to delete a non pending expense 
    def test_non_pending_expenses_deletion(self):
        self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
        )

        expense_id = 4
        response = self.client.post(f"/expenses/{expense_id}/delete", follow_redirects=True)

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Only pending expenses can be deleted.", response.data)
        self.assertIn(b"dashboard", response.data.lower())

        deleted_expense = database.get_expense_by_id(expense_id)
        self.assertIsNotNone(deleted_expense)

    # test attempting to edit a non pending expense 
    def test_non_pending_expenses_edit(self):
        self.client.post(
            "/login",
            data={"username": "bob_employee", "password": "password123"},
        )

        expense_id = 4
        original_expense = database.get_expense_by_id(expense_id)
        response = self.client.post(
            f"/expenses/{expense_id}/edit",
            data={"amount": "99.99", "description": "Should not update", "date": "2026-07-31"},
            follow_redirects=True,
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Only pending expenses can be edited.", response.data)
        self.assertIn(b"dashboard", response.data.lower())

        edited_expense = database.get_expense_by_id(expense_id)
        self.assertIsNotNone(edited_expense)
        self.assertEqual(edited_expense.amount, original_expense.amount)
        self.assertEqual(edited_expense.description, original_expense.description)
        self.assertEqual(edited_expense.date, original_expense.date)


if __name__ == "__main__":
    unittest.main(verbosity=2)
