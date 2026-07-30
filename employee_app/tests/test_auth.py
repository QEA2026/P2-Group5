import os
import sys
import unittest
from unittest.mock import MagicMock, patch

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

from employee_app.app import app
from employee_app.user_model import User


class AuthTests(unittest.TestCase):
    def setUp(self):
        app.config["TESTING"] = True
        self.client = app.test_client()

    def test_login_success(self):
        mock_user = User(user_id=1, username="bob_employee", password="password123", role="employee")
        mocked_login = MagicMock(return_value=mock_user)

        with patch("employee_app.app.database.login", mocked_login):
            response = self.client.post(
                "/login",
                data={"username": "bob_employee", "password": "password123"},
                follow_redirects=True,
            )

        mocked_login.assert_called_once_with("bob_employee", "password123")
        self.assertEqual(response.status_code, 200)
        self.assertIn(b"dashboard", response.data.lower())

        with self.client.session_transaction() as sess:
            self.assertEqual(sess["user_id"], 1)
            self.assertEqual(sess["username"], "bob_employee")

    def test_login_wrong_username_and_password(self):
        mocked_login = MagicMock(return_value=None)

        with patch("employee_app.app.database.login", mocked_login):
            response = self.client.post(
                "/login",
                data={"username": "wronguser", "password": "wrongpass"},
                follow_redirects=True,
            )

        mocked_login.assert_called_once_with("wronguser", "wrongpass")
        self.assertIn(b"Invalid username or password", response.data)

        with self.client.session_transaction() as sess:
            self.assertNotIn("user_id", sess)
            self.assertNotIn("username", sess)

    def test_login_manager_attempt(self):
        mocked_login = MagicMock(return_value=None)

        with patch("employee_app.app.database.login", mocked_login):
            response = self.client.post(
                "/login",
                data={"username": "manager_eve", "password": "manager123"},
                follow_redirects=True,
            )

        mocked_login.assert_called_once_with("manager_eve", "manager123")
        self.assertIn(b"Invalid username or password", response.data)

        with self.client.session_transaction() as sess:
            self.assertNotIn("user_id", sess)
            self.assertNotIn("username", sess)

    def test_dashboard_redirects_to_login_when_not_logged_in(self):
        response = self.client.get("/dashboard", follow_redirects=True)

        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Log in", response.data)
        self.assertIn(b"Please log in to continue.", response.data)

        with self.client.session_transaction() as sess:
            self.assertNotIn("user_id", sess)
            self.assertNotIn("username", sess)


if __name__ == "__main__":
    unittest.main(verbosity=2)

