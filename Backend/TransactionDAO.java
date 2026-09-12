import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions " +
                     "(type, amount, category, description, transaction_date) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, transaction.getType());
            ps.setBigDecimal(2, transaction.getAmount());
            ps.setString(3, transaction.getCategory());
            ps.setString(4, transaction.getDescription());
            ps.setString(5, transaction.getTransactionDate());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Unable to add transaction: " + e.getMessage());
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC, id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getBigDecimal("amount"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("transaction_date")
                ));
            }

            return transactions;

        } catch (SQLException e) {
            System.err.println("Unable to load transactions: " + e.getMessage());
            return null;
        }
    }

    public BigDecimal[] getSummary() {
        BigDecimal[] summary = {BigDecimal.ZERO, BigDecimal.ZERO};

        String sql = "SELECT " +
                     "SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS total_income, " +
                     "SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS total_expense " +
                     "FROM transactions";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal totalIncome = rs.getBigDecimal("total_income");
                BigDecimal totalExpense = rs.getBigDecimal("total_expense");

                if (totalIncome != null) {
                    summary[0] = totalIncome;
                }

                if (totalExpense != null) {
                    summary[1] = totalExpense;
                }
            }

            return summary;

        } catch (SQLException e) {
            System.err.println("Unable to load summary: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Unable to delete transaction: " + e.getMessage());
            return false;
        }
    }
}