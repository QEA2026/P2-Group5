const state = {
    selectedApprovalId: null,
    approvals: [],
};

document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.dataset.page;

    if (page === "login") {
        initLoginPage();
        return;
    }

    if (page === "dashboard") {
        initDashboardPage();
    }
});

async function initLoginPage() {
    const existingSession = await fetchJson("/api/session", { allowUnauthorized: true });
    if (existingSession.ok) {
        window.location.href = "/dashboard";
        return;
    }

    const loginForm = document.getElementById("login-form");
    const message = document.getElementById("login-message");

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        message.textContent = "";
        message.classList.remove("success");

        const payload = {
            username: document.getElementById("username").value.trim(),
            password: document.getElementById("password").value,
        };

        const response = await fetchJson("/api/login", {
            method: "POST",
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            message.textContent = response.message || "Login failed.";
            return;
        }

        window.location.href = "/dashboard";
    });
}

async function initDashboardPage() {
    const session = await fetchJson("/api/session", { allowUnauthorized: true });
    if (!session.ok) {
        window.location.href = "/login";
        return;
    }

    document.getElementById("manager-name").textContent = session.data.username;
    document.getElementById("logout-button").addEventListener("click", handleLogout);
    document.getElementById("refresh-approvals").addEventListener("click", () => loadApprovals(true));
    document.getElementById("refresh-reports").addEventListener("click", () => loadReports(true));
    document.getElementById("approval-filters").addEventListener("input", () => loadApprovals(true));
    document.getElementById("approval-filters").addEventListener("change", () => loadApprovals(true));
    document.getElementById("report-filters").addEventListener("input", () => loadReports(true));
    document.getElementById("report-filters").addEventListener("change", () => loadReports(true));
    document.getElementById("review-form").addEventListener("submit", handleApprovalUpdate);
    document.getElementById("review-date").value = new Date().toISOString().slice(0, 10);

    await loadFilterOptions();
    await Promise.all([
        loadSummary(),
        loadApprovals(false),
        loadReports(false),
    ]);
}

async function handleLogout() {
    await fetchJson("/api/logout", { method: "POST" });
    window.location.href = "/login";
}

async function loadFilterOptions() {
    const response = await fetchJson("/api/options");
    if (!response.ok) {
        return;
    }

    populateSelect("approval-employee", response.data.employees, "All employees");
    populateSelect("report-employee", response.data.employees, "All employees");
    populateSelect("approval-reviewer", response.data.managers, "All reviewers");
}

function populateSelect(id, values, placeholder) {
    const select = document.getElementById(id);
    const currentValue = select.value;

    select.innerHTML = "";

    const emptyOption = document.createElement("option");
    emptyOption.value = "";
    emptyOption.textContent = placeholder;
    select.appendChild(emptyOption);

    values.forEach((value) => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = value;
        select.appendChild(option);
    });

    select.value = currentValue;
}

async function loadSummary() {
    const response = await fetchJson("/api/summary");
    if (!response.ok) {
        return;
    }

    document.getElementById("summary-total").textContent = response.data.total;
    document.getElementById("summary-pending").textContent = response.data.pending;
    document.getElementById("summary-approved").textContent = response.data.approved;
    document.getElementById("summary-denied").textContent = response.data.denied;
}

async function loadApprovals(resetSelection) {
    const query = new URLSearchParams(new FormData(document.getElementById("approval-filters")));
    const response = await fetchJson(`/api/approvals?${query.toString()}`);
    if (!response.ok) {
        return;
    }

    state.approvals = response.data;
    if (resetSelection || !response.data.some((item) => item.approvalId === state.selectedApprovalId)) {
        state.selectedApprovalId = response.data.length ? response.data[0].approvalId : null;
    }

    renderApprovals();
    syncSelectedApproval();
}

function renderApprovals() {
    const tableBody = document.getElementById("approval-table-body");
    const emptyState = document.getElementById("approval-empty");
    tableBody.innerHTML = "";

    if (!state.approvals.length) {
        emptyState.hidden = false;
        return;
    }

    emptyState.hidden = true;

    state.approvals.forEach((item) => {
        const row = document.createElement("tr");
        row.dataset.approvalId = item.approvalId;
        if (item.approvalId === state.selectedApprovalId) {
            row.classList.add("is-selected");
        }

        row.innerHTML = `
            <td>${escapeHtml(item.employeeUsername)}</td>
            <td>${escapeHtml(item.description)}</td>
            <td>${escapeHtml(item.expenseDate)}</td>
            <td>${formatCurrency(item.amount)}</td>
            <td><span class="status-pill ${item.status}">${escapeHtml(item.status)}</span></td>
        `;

        row.addEventListener("click", () => {
            state.selectedApprovalId = item.approvalId;
            renderApprovals();
            syncSelectedApproval();
        });

        tableBody.appendChild(row);
    });
}

function syncSelectedApproval() {
    const selected = state.approvals.find((item) => item.approvalId === state.selectedApprovalId);
    const emptyPanel = document.getElementById("review-empty");
    const form = document.getElementById("review-form");

    if (!selected) {
        emptyPanel.hidden = false;
        form.hidden = true;
        return;
    }

    emptyPanel.hidden = true;
    form.hidden = false;

    document.getElementById("review-approval-id").value = selected.approvalId;
    document.getElementById("detail-employee").textContent = selected.employeeUsername;
    document.getElementById("detail-expense-id").textContent = `${selected.expenseId}`;
    document.getElementById("detail-amount").textContent = formatCurrency(selected.amount);
    document.getElementById("detail-date").textContent = selected.expenseDate;
    document.getElementById("detail-description").value = selected.description;
    document.getElementById("review-status").value = selected.status;
    document.getElementById("review-comment").value = selected.comment || "";
    document.getElementById("review-date").value = selected.reviewDate || new Date().toISOString().slice(0, 10);
    document.getElementById("review-message").textContent = "";
    document.getElementById("review-message").classList.remove("success");
}

async function handleApprovalUpdate(event) {
    event.preventDefault();

    const message = document.getElementById("review-message");
    const approvalId = document.getElementById("review-approval-id").value;
    const payload = {
        status: document.getElementById("review-status").value,
        comment: document.getElementById("review-comment").value,
        reviewDate: document.getElementById("review-date").value,
    };

    const response = await fetchJson(`/api/approvals/${approvalId}`, {
        method: "POST",
        body: JSON.stringify(payload),
    });

    if (!response.ok) {
        message.textContent = response.message || "Update failed.";
        message.classList.remove("success");
        return;
    }

    message.textContent = response.message || "Approval updated.";
    message.classList.add("success");

    await Promise.all([
        loadSummary(),
        loadApprovals(false),
        loadReports(false),
    ]);
}

async function loadReports() {
    const query = new URLSearchParams(new FormData(document.getElementById("report-filters")));
    const response = await fetchJson(`/api/reports?${query.toString()}`);
    if (!response.ok) {
        return;
    }

    const tableBody = document.getElementById("report-table-body");
    const emptyState = document.getElementById("report-empty");
    tableBody.innerHTML = "";

    if (!response.data.length) {
        emptyState.hidden = false;
        return;
    }

    emptyState.hidden = true;

    response.data.forEach((item) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${escapeHtml(item.employeeUsername)}</td>
            <td>${escapeHtml(item.description)}</td>
            <td>${escapeHtml(item.expenseDate)}</td>
            <td>${formatCurrency(item.amount)}</td>
            <td><span class="status-pill ${item.status}">${escapeHtml(item.status)}</span></td>
            <td>${escapeHtml(item.reviewerUsername || "Unassigned")}</td>
            <td>${escapeHtml(item.comment || "")}</td>
        `;
        tableBody.appendChild(row);
    });
}

async function fetchJson(url, options = {}) {
    const fetchOptions = {
        method: options.method || "GET",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {}),
        },
        body: options.body,
    };

    try {
        const response = await fetch(url, fetchOptions);
        const payload = await response.json().catch(() => ({}));

        if (response.status === 401 && options.allowUnauthorized) {
            return {
                ok: false,
                status: response.status,
                message: payload.message || "Unauthorized.",
                data: null,
            };
        }

        return {
            ok: response.ok,
            status: response.status,
            message: Array.isArray(payload) ? "" : (payload.message || ""),
            data: payload,
        };
    } catch (error) {
        return {
            ok: false,
            message: "Could not reach the manager app.",
            data: null,
        };
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
    }).format(amount);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
