package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BBDDUtils {
    private static final String DB_URL = "jdbc:sqlite:pokeapp.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error al cargar el driver de SQLite", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
