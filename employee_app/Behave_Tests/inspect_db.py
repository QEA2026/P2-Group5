import os
import sqlite3

path = r'C:\Users\Audrey\team1_p0\P0\db\expense_manager.db'
print('exists', os.path.exists(path))
conn = sqlite3.connect(path)
cur = conn.cursor()
print(cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name").fetchall())
for table in ['USERS', 'EXPENSES', 'APPROVALS']:
    try:
        print(table, cur.execute(f'SELECT COUNT(*) FROM {table}').fetchone())
    except Exception as exc:
        print(table, 'ERR', exc)
print(cur.execute("SELECT username, password, role FROM USERS LIMIT 10").fetchall())
conn.close()
