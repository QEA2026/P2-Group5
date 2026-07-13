from datetime import date

from flask import Flask, render_template, request, redirect, url_for, session, flash, abort
from werkzeug.exceptions import HTTPException

import database
from auth import login_required
from logging_config import logger

app = Flask(__name__)
app.secret_key = "dev"


@app.errorhandler(Exception)
def handle_unexpected_error(error):
    if isinstance(error, HTTPException):
        return error
    logger.exception("Unhandled error: %s", error)
    raise error


def _validate_expense_form(form):
    """Returns (amount, description, date) from the form, or None if invalid (flashes the reason)."""
    description = form["description"].strip()
    expense_date = form["date"]
    amount_raw = form["amount"]

    try:
        amount = float(amount_raw)
    except ValueError:
        flash("Amount must be a number.")
        return None

    if amount <= 0:
        flash("Amount must be greater than zero.")
        return None

    if not description:
        flash("Description is required.")
        return None

    if not expense_date:
        flash("Date is required.")
        return None

    return amount, description, expense_date


@app.route("/")
def index():
    if "user_id" in session:
        return redirect(url_for("dashboard"))
    return redirect(url_for("login"))


@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        username = request.form["username"].strip()
        password = request.form["password"]
        user = database.login(username, password)
        if user is None:
            logger.warning("Failed login attempt for username '%s'", username)
            flash("Invalid username or password.")
            return render_template("login.html")
        session["user_id"] = user.user_id
        session["username"] = user.username
        logger.info("User '%s' (id=%s) logged in", user.username, user.user_id)
        return redirect(url_for("dashboard"))
    return render_template("login.html")


@app.route("/logout")
def logout():
    if "user_id" in session:
        logger.info("User '%s' (id=%s) logged out", session.get("username"), session.get("user_id"))
    session.clear()
    return redirect(url_for("login"))


@app.route("/dashboard")
@login_required
def dashboard():
    expenses = database.get_employee_expenses(session["user_id"])
    return render_template("dashboard.html", expenses=expenses, today=date.today().isoformat())


@app.route("/expenses", methods=["POST"])
@login_required
def submit_expense():
    parsed = _validate_expense_form(request.form)
    if parsed is None:
        return redirect(url_for("dashboard"))
    amount, description, expense_date = parsed

    expense_id = database.create_expense(session["user_id"], amount, description, expense_date)
    logger.info("User id=%s created expense id=%s amount=%s", session["user_id"], expense_id, amount)
    flash("Expense submitted.")
    return redirect(url_for("dashboard"))


def _get_own_expense_or_404(expense_id):
    record = database.get_expense_with_status(expense_id)
    if record is None or record["expense"].user_id != session["user_id"]:
        abort(404)
    return record


@app.route("/expenses/<int:expense_id>/edit", methods=["GET", "POST"])
@login_required
def edit_expense(expense_id):
    record = _get_own_expense_or_404(expense_id)
    if record["status"] != "pending":
        flash("Only pending expenses can be edited.")
        return redirect(url_for("dashboard"))

    if request.method == "POST":
        parsed = _validate_expense_form(request.form)
        if parsed is None:
            return redirect(url_for("edit_expense", expense_id=expense_id))
        amount, description, expense_date = parsed

        if database.update_expense(expense_id, amount, description, expense_date):
            logger.info("User id=%s updated expense id=%s", session["user_id"], expense_id)
            flash("Expense updated.")
        else:
            flash("Expense could no longer be edited.")
        return redirect(url_for("dashboard"))

    return render_template("edit_expense.html", expense=record["expense"])


@app.route("/expenses/<int:expense_id>/delete", methods=["POST"])
@login_required
def delete_expense(expense_id):
    record = _get_own_expense_or_404(expense_id)
    if record["status"] != "pending":
        flash("Only pending expenses can be deleted.")
        return redirect(url_for("dashboard"))

    database.delete_expense(expense_id)
    flash("Expense deleted.")
    return redirect(url_for("dashboard"))


if __name__ == "__main__":
    app.run(debug=True)
