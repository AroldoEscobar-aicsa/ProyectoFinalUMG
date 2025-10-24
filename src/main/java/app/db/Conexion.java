package app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String HOST = "interchange.proxy.rlwy.net";
    private static final int PORT = 32064;              // puerto público TCP Proxy de Railway
    private static final String DATABASE = "ProyectoFinal";
    private static final String USER = "sa";
    private static final String PASSWORD = "54224703_Arol";

    private static Connection conexion;

    // Con JDBC 4+ no hace falta Class.forName, pero si quieres dejarlo está bien
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se pudo cargar el driver JDBC de SQL Server", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            String url = String.format(
                    "jdbc:sqlserver://%s:%d;"
                            + "databaseName=%s;"
                            + "encrypt=true;"
                            + "trustServerCertificate=true;"
                            + "loginTimeout=30;",
                    HOST, PORT, DATABASE
            );
            conexion = DriverManager.getConnection(url, USER, PASSWORD);
            System.out.println("✅ Conectado a " + DATABASE);
        }
        return conexion;
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try { conexion.close(); } catch (SQLException ignored) {}
        }
    }
}
