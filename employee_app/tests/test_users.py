import os
import sys
import unittest

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

from user_model import User


class UserModelTests(unittest.TestCase):
    def test_user_valid_role(self):
        user = User(
            user_id=1,
            username="bob_employee",
            password="password123",
            role="employee",
        )

        self.assertEqual(user.user_id, 1)
        self.assertEqual(user.username, "bob_employee")
        self.assertEqual(user.role, "employee")

    def test_user_invalid_role(self):
        with self.assertRaises(ValueError):
            User(
                user_id=2,
                username="manager_eve",
                password="manager123",
                role="manager",
            )


if __name__ == "__main__":
    unittest.main(verbosity=2)


    
    

    




