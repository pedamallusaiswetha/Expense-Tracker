import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseServer {

    private static final Path FRONTEND_ROOT = Paths.get("Frontend").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/api/test", ExpenseServer::handleTest);

        server.createContext(
            "/api/transactions",
            ExpenseServer::handleTransaction
        );
        server.createContext("/api/summary", ExpenseServer::handleSummary);
        server.createContext("/", ExpenseServer::handleFrontend);

        server.start();

        System.out.println("=================================");
        System.out.println("Expense Tracker Backend Started");
        System.out.println("http://localhost:8080");
        System.out.println("=================================");
    }

    private static void handleFrontend(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Only GET is allowed");
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        String relativePath = "/".equals(requestPath) ? "index.html" : requestPath.substring(1);
        Path file = FRONTEND_ROOT.resolve(relativePath).normalize();

        if (!file.startsWith(FRONTEND_ROOT) || !Files.isRegularFile(file)) {
            sendResponse(exchange, 404, "Page not found");
            return;
        }

        String contentType = Files.probeContentType(file);
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }
        byte[] content = Files.readAllBytes(file);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(content);
        }
    }

    // TEST API
    private static void handleTest(HttpExchange exchange) throws IOException {

        String response = "Java Backend is working!";

        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return;
        }

        exchange.sendResponseHeaders(
            200,
            response.length()
        );

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    // TRANSACTION API
    private static void handleTransaction(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        String requestPath = exchange.getRequestURI().getPath();
        String transactionPath = "/api/transactions";

        if (exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            if (!requestPath.startsWith(transactionPath + "/")) {
                sendJson(exchange, 400, "{\"error\":\"A transaction id is required\"}");
                return;
            }

            try {
                int id = Integer.parseInt(requestPath.substring((transactionPath + "/").length()));
                boolean deleted = new TransactionDAO().deleteTransaction(id);
                if (deleted) {
                    sendJson(exchange, 200, "{\"message\":\"Transaction deleted successfully\"}");
                } else {
                    sendJson(exchange, 404, "{\"error\":\"Transaction not found\"}");
                }
            } catch (NumberFormatException error) {
                sendJson(exchange, 400, "{\"error\":\"Transaction id must be a number\"}");
            }
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            List<Transaction> transactions = new TransactionDAO().getAllTransactions();

            if (transactions == null) {
                sendJson(exchange, 500, "{\"error\":\"Unable to load transactions from the database\"}");
                return;
            }

            StringBuilder response = new StringBuilder("[");
            for (int i = 0; i < transactions.size(); i++) {
                if (i > 0) response.append(',');
                response.append(transactionJson(transactions.get(i)));
            }
            response.append(']');
            sendJson(exchange, 200, response.toString());
            return;
        }

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            InputStream input = exchange.getRequestBody();

            String body = new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8
            );

            System.out.println("Received data:");
            System.out.println(body);

            Map<String, String> data = parseFormData(body);

            String type = data.get("type");
            String amountValue = data.get("amount");
            String category = data.get("category");
            String description = data.get("description");
            String date = data.get("date");

            if ((!"INCOME".equals(type) && !"EXPENSE".equals(type)) ||
                    amountValue == null || category == null || category.trim().isEmpty() || date == null || date.trim().isEmpty()) {
                sendJson(exchange, 400, "{\"error\":\"Type, amount, category, and date are required\"}");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountValue.trim());
            } catch (NumberFormatException error) {
                sendJson(exchange, 400, "{\"error\":\"Amount must be a valid positive number\"}");
                return;
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendJson(exchange, 400, "{\"error\":\"Amount must be greater than zero\"}");
                return;
            }

            try {
                LocalDate.parse(date);
            } catch (DateTimeParseException error) {
                sendJson(exchange, 400, "{\"error\":\"Date must be a valid YYYY-MM-DD value\"}");
                return;
            }

            Transaction transaction = new Transaction(
                type,
                amount,
                category.trim(),
                description == null ? "" : description.trim(),
                date
            );

            TransactionDAO dao = new TransactionDAO();

            boolean result = dao.addTransaction(transaction);

            if (result) {
                sendJson(exchange, 201, "{\"message\":\"Transaction added successfully\"}");
            } else {
                sendJson(exchange, 500, "{\"error\":\"Unable to save transaction. Please check the database connection.\"}");
            }

        } else {

            sendJson(exchange, 405, "{\"error\":\"Only GET, POST and OPTIONS are allowed\"}");
        }
    }

    private static void handleSummary(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            BigDecimal[] summary = new TransactionDAO().getSummary();

            if (summary == null) {
                sendJson(exchange, 500, "{\"error\":\"Unable to load summary from the database\"}");
                return;
            }

            BigDecimal balance = summary[0].subtract(summary[1]);
            sendJson(exchange, 200, "{\"totalIncome\":" + summary[0] +
                ",\"totalExpense\":" + summary[1] +
                ",\"balance\":" + balance + "}");
        } else if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(exchange, 204, "");
        } else {
            sendJson(exchange, 405, "{\"error\":\"Only GET is allowed\"}");
        }
    }

    private static String transactionJson(Transaction transaction) {
        return "{\"id\":" + transaction.getId() + ",\"type\":\"" +
            jsonEscape(transaction.getType()) + "\",\"amount\":" + transaction.getAmount().toPlainString() +
            ",\"category\":\"" + jsonEscape(transaction.getCategory()) +
            "\",\"description\":\"" + jsonEscape(transaction.getDescription()) +
            "\",\"date\":\"" + jsonEscape(transaction.getTransactionDate()) + "\"}";
    }

    private static String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        sendResponse(exchange, status, response);
    }

    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    // Convert form data into key-value pairs
    private static Map<String, String> parseFormData(String body) {

        Map<String, String> data = new HashMap<>();

        String[] pairs = body.split("&");

        for (String pair : pairs) {

            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {

                String key = URLDecoder.decode(
                    keyValue[0],
                    StandardCharsets.UTF_8
                );

                String value = URLDecoder.decode(
                    keyValue[1],
                    StandardCharsets.UTF_8
                );

                data.put(key, value);
            }
        }

        return data;
    }
}