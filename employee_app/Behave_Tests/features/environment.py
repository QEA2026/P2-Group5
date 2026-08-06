import os
import sqlite3
import sys
import threading
import time
import urllib.request
from pathlib import Path

from werkzeug.serving import make_server

APP_ROOT = Path(__file__).resolve().parents[2]
if str(APP_ROOT) not in sys.path:
    sys.path.insert(0, str(APP_ROOT))

from app import app as employee_app_module
import database as app_database


class FlaskServer:
    def __init__(self, app):
        self.server = make_server("127.0.0.1", 5000, app)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)

    def start(self):
        self.thread.start()
        self._wait_until_ready()

    def stop(self):
        self.server.shutdown()
        self.thread.join(timeout=5)

    def _wait_until_ready(self):
        deadline = time.time() + 15
        while time.time() < deadline:
            try:
                with urllib.request.urlopen("http://127.0.0.1:5000/login", timeout=1) as response:
                    if response.status == 200:
                        return
            except Exception:
                time.sleep(0.2)
        raise RuntimeError("Flask app did not start successfully")


server = None


def initialize_database():
    db_path = Path(app_database.DB_PATH)
    db_path.parent.mkdir(parents=True, exist_ok=True)

    schema_path = db_path.parent / "schema.sql"
    seed_path = db_path.parent / "seed.sql"

    if db_path.exists():
        db_path.unlink()

    with sqlite3.connect(db_path) as conn:
        conn.executescript(schema_path.read_text(encoding="utf-8"))
        conn.executescript(seed_path.read_text(encoding="utf-8"))
        conn.commit()


def before_all(context):
    global server
    initialize_database()
    server = FlaskServer(employee_app_module)
    server.start()


def after_scenario(context, scenario):
    driver = getattr(context, "driver", None)
    if driver is not None:
        driver.quit()


def after_all(context):
    if server is not None:
        server.stop()
