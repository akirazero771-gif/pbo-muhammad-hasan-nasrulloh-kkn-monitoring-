package id.simonkkn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MySqlConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/simon_kkn?serverTimezone=UTC";

    private MySqlConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("SIMON_MYSQL_URL", DEFAULT_URL);
        String username = setting("SIMON_MYSQL_USER", "root");
        String password = setting("SIMON_MYSQL_PASSWORD", "");
        return DriverManager.getConnection(url, username, password);
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static void main(String[] args) throws SQLException {
        try (Connection connection = getConnection()) {
            System.out.println("Terhubung ke "
                    + connection.getMetaData().getDatabaseProductName()
                    + " " + connection.getMetaData().getDatabaseProductVersion()
                    + " (" + connection.getCatalog() + ")");
        }
    }
}
