package app.dao; // Paquete 'dao'

import app.db.Conexion; // Usando 'db.Conexion'
import model.Reserva; // Usando 'model.Reserva'

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para el Módulo E: Reservas.
 * Sigue el formato de MultaDAO (mapeador y PreparedStatement).
 */
public class ReservaDAO {

    /**
     * Crea una nueva reserva[cite: 36].
     * Este método debe ser transaccional para calcular la 'posicionCola'[cite: 85].
     * Esta es la única "lógica compleja" que debe estar en el DAO
     * si no hay capa de servicio, para garantizar la integridad de la cola.
     */
    public boolean crearReserva(int idCliente, int idLibro) throws SQLException {

        String sqlPosicion = "SELECT ISNULL(MAX(posicionCola), 0) + 1 FROM Reserva WHERE idLibro = ? AND estado = 'Pendiente'";
        String sqlInsert = "INSERT INTO Reserva (idCliente, idLibro, fechaReserva, estado, posicionCola) " +
                "VALUES (?, ?, GETDATE(), 'Pendiente', ?)";

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false); // 1. Iniciar transacción

            int proximaPosicion;

            // 2. Calcular la siguiente posición
            try (PreparedStatement pstmtPos = conn.prepareStatement(sqlPosicion)) {
                pstmtPos.setInt(1, idLibro);
                try (ResultSet rs = pstmtPos.executeQuery()) {
                    proximaPosicion = rs.next() ? rs.getInt(1) : 1;
                }
            }

            // 3. Insertar la reserva
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setInt(1, idCliente);
                pstmtInsert.setInt(2, idLibro);
                pstmtInsert.setInt(3, proximaPosicion);

                int filasAfectadas = pstmtInsert.executeUpdate();

                if (filasAfectadas > 0) {
                    conn.commit(); // 4. Confirmar transacción
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear reserva (transacción): " + e.getMessage());
            if (conn != null) conn.rollback(); // 4c. Revertir en caso de excepción
            throw e; // Relanzar la excepción para que el controlador la vea
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // 5. Restaurar modo auto-commit
                conn.close(); // 6. Cerrar conexión
            }
        }
    }

    /**
     * Actualiza el estado de una reserva (Ej: "Expirada", "Completada", "Cancelada").
     */
    public boolean actualizarEstadoReserva(int idReserva, String nuevoEstado) throws SQLException {
        String sql = "UPDATE Reserva SET estado = ? WHERE idReserva = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idReserva);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Lista las reservas activas (pendientes) de un cliente específico[cite: 58].
     */
    public List<Reserva> listarReservasPendientesPorCliente(int idCliente) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        String sql = "SELECT * FROM Reserva WHERE idCliente = ? AND estado = 'Pendiente' ORDER BY fechaReserva";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapearResultSet(rs));
                }
            }
        }
        return reservas;
    }

    /**
     * Verifica si existe una reserva activa (pendiente) para un libro[cite: 72].
     */
    public boolean existeReservaPendiente(int idLibro) throws SQLException {
        String sql = "SELECT COUNT(idReserva) FROM Reserva WHERE idLibro = ? AND estado = 'Pendiente'";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idLibro);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // --- Helper ---

    /**
     * Metodo helper para convertir un ResultSet en un objeto Reserva.
     */
    private Reserva mapearResultSet(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("idReserva"));
        r.setIdCliente(rs.getInt("idCliente"));
        r.setIdLibro(rs.getInt("idLibro"));
        r.setFechaReserva(rs.getTimestamp("fechaReserva"));
        r.setEstado(rs.getString("estado"));
        r.setPosicionCola(rs.getInt("posicionCola"));
        return r;
    }
}