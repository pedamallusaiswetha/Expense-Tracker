# Expense Tracker

Expense Tracker & Budget Management System built with Java, MySQL, JDBC, HTML, CSS, and JavaScript. It helps users manage income and expenses, view real-time financial summaries, store transactions securely in a database, validate inputs, and maintain data persistence through a simple full-stack web application.

## Features

- Add income and expense transactions
- View recent transactions
- Calculate total income, total expenses, and balance
- Filter transactions by month
- Delete transactions
- Persist data in MySQL using JDBC

## Technologies

- Java
- MySQL
- JDBC
- HTML
- CSS
- JavaScript

## Project structure

- `Backend/` — Java server, DAO, JDBC connection, and reusable classes
- `Frontend/` — HTML, CSS, and JavaScript dashboard
- `schema.sql` — MySQL schema setup script
- `README.md` — setup and execution guide

## Prerequisites

- Java 17 or newer
- MySQL 8 or newer
- MySQL Connector/J JAR already included in `Backend/`

## Database setup

```powershell
mysql -u root -p
```

Then run:

```sql
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;
SOURCE schema.sql;
```

## Environment / configuration setup

Set environment variables for your local MySQL setup in PowerShell:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
$env:DB_URL = "jdbc:mysql://localhost:3306/expense_tracker"
```

If your MySQL root user does not use a password, set `DB_PASSWORD` to an empty string:

```powershell
$env:DB_PASSWORD = ""
```

## How to compile

From the project root:

```powershell
javac -d Backend Backend\*.java
```

## How to run

```powershell
java -cp "Backend;Backend\mysql-connector-j-26.7.0.jar" ExpenseServer
```

The backend will start on `http://localhost:8080`.

## How to open the application

Open this in your browser:

```text
http://localhost:8080/
```

## API

- `GET /api/test` — checks whether the backend is running
- `GET /api/transactions` — returns all transactions
- `POST /api/transactions` — adds a new transaction
- `DELETE /api/transactions/{id}` — deletes a transaction
- `GET /api/summary` — returns summary totals

## Known limitation

- Update/Edit transaction is not currently implemented.

## Notes for interview use

- The Java backend uses JDBC to connect to MySQL.
- SQL queries use `PreparedStatement` to help prevent SQL injection.
- Database credentials are loaded from environment variables instead of being hardcoded in source code.
- The frontend communicates with the Java server over HTTP at `localhost:8080`.
- Financial amounts are stored in MySQL as `DECIMAL(12,2)` for better precision.
>>>>>>> origin/main
