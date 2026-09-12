const apiUrl = "http://localhost:8080/api";
const transactionForm = document.getElementById("transactionForm");
const transactionList = document.getElementById("transactionList");
const statusMessage = document.getElementById("statusMessage");
const monthFilter = document.getElementById("monthFilter");

monthFilter.value = new Date().toISOString().slice(0, 7);

document.getElementById("addIncomeBtn").addEventListener("click", () => openForm("INCOME"));
document.getElementById("addExpenseBtn").addEventListener("click", () => openForm("EXPENSE"));
document.getElementById("cancelBtn").addEventListener("click", () => transactionForm.hidden = true);
transactionForm.addEventListener("submit", saveTransaction);
monthFilter.addEventListener("change", loadDashboard);
transactionList.addEventListener("click", deleteTransaction);

function openForm(type) {
    transactionForm.hidden = false;
    document.getElementById("type").value = type;
    document.getElementById("formTitle").textContent = `Add ${type.toLowerCase()}`;
    document.getElementById("date").value = new Date().toISOString().slice(0, 10);
    document.getElementById("amount").focus();
}

async function saveTransaction(event) {
    event.preventDefault();
    const formData = new URLSearchParams(new FormData(transactionForm));
    try {
        const response = await fetch(`${apiUrl}/transactions`, { method: "POST", body: formData });
        const result = await response.json();
        if (!response.ok) throw new Error(result.error || "Unable to save transaction");
        transactionForm.reset();
        transactionForm.hidden = true;
        showStatus(result.message, "success");
        await loadDashboard();
    } catch (error) {
        showStatus(error.message, "error");
    }
}

async function loadDashboard() {
    try {
        const response = await fetch(`${apiUrl}/transactions`);
        if (!response.ok) throw new Error("Backend request failed");
        const transactions = await response.json();
        const monthTransactions = transactions.filter(transaction => transaction.date.startsWith(monthFilter.value));
        renderSummary(monthTransactions);
        renderTransactions(monthTransactions);
    } catch (error) {
        showStatus("Connect the Java backend to load your transactions.", "error");
    }
}

function renderSummary(transactions) {
    const totalIncome = transactions
        .filter(transaction => transaction.type === "INCOME")
        .reduce((total, transaction) => total + transaction.amount, 0);
    const totalExpense = transactions
        .filter(transaction => transaction.type === "EXPENSE")
        .reduce((total, transaction) => total + transaction.amount, 0);
    document.getElementById("totalIncome").textContent = formatMoney(totalIncome);
    document.getElementById("totalExpense").textContent = formatMoney(totalExpense);
    document.getElementById("balance").textContent = formatMoney(totalIncome - totalExpense);
}

function renderTransactions(transactions) {
    if (!transactions.length) {
        transactionList.innerHTML = '<p class="empty">No transactions yet. Add your first one above.</p>';
        return;
    }
    transactionList.innerHTML = transactions.map(transaction => `
        <article class="transaction ${transaction.type.toLowerCase()}" data-id="${transaction.id}">
            <div><strong>${escapeHtml(transaction.category)}</strong><span>${escapeHtml(transaction.description || "No description")}</span></div>
            <div class="transaction-meta"><time>${escapeHtml(transaction.date)}</time><strong>${transaction.type === "INCOME" ? "+" : "-"}${formatMoney(transaction.amount)}</strong><button class="delete-transaction" type="button" data-id="${transaction.id}" aria-label="Delete transaction">Delete</button></div>
        </article>`).join("");
}

async function deleteTransaction(event) {
    const button = event.target.closest(".delete-transaction");
    if (!button || !window.confirm("Delete this transaction?")) return;

    try {
        const response = await fetch(`${apiUrl}/transactions/${button.dataset.id}`, { method: "DELETE" });
        const result = await response.json();
        if (!response.ok) throw new Error(result.error || "Unable to delete transaction");
        showStatus(result.message, "success");
        await loadDashboard();
    } catch (error) {
        showStatus(error.message, "error");
    }
}

function formatMoney(value) {
    return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(value || 0);
}

function escapeHtml(value) {
    return String(value).replace(/[&<>'"]/g, character => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[character]));
}

function showStatus(message, type) {
    statusMessage.textContent = message;
    statusMessage.className = type;
}

loadDashboard();
setInterval(loadDashboard, 5000);