import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/expense_tracker";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String getSetting(String key, String defaultValue) {
        String value = System.getenv(key);

        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        value = System.getProperty(key);

        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        String url = getSetting("DB_URL", DEFAULT_URL);
        String username = getSetting("DB_USERNAME", DEFAULT_USERNAME);
        String password = getSetting("DB_PASSWORD", DEFAULT_PASSWORD);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Make sure mysql-connector-j.jar is on the classpath.", e);
        }

        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
}