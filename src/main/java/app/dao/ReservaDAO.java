package app.dao;

import app.db.Conexion;
import model.Reserva;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para Reservas sobre la tabla dbo.Reservas.
 */
public class ReservaDAO {

    /**
     * Crea una nueva reserva.
     * Usamos:
     *  - CreadoUtc = GETUTCDATE()
     *  - Estado    = 'PENDIENTE'
     *  - IdCopia   = NULL (es una reserva por libro, no por copia específica)
     */
    public boolean crearReserva(int idCliente, int idLibro) throws SQLException {

        // Regla: la reserva expira 24 horas después de creada (ajusta a días/horas que tú quieras)
        String sqlInsert = """
        INSERT INTO dbo.Reservas (IdCliente, IdLibro, IdCopia, CreadoUtc, ExpiraUtc, Estado, Notas)
        VALUES (?, ?, NULL, GETUTCDATE(), DATEADD(HOUR, 24, GETUTCDATE()), 'PENDIENTE', NULL)
    """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {

            pstmt.setInt(1, idCliente);
            pstmt.setInt(2, idLibro);

            int filas = pstmt.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * Actualiza el estado de una reserva (Ej: "EXPIRADA", "COMPLETADA", "CANCELADA").
     * Si quieres también podrías setear ExpiraUtc aquí según el negocio.
     */
    public boolean actualizarEstadoReserva(int idReserva, String nuevoEstado) throws SQLException {
        String sql = """
            UPDATE dbo.Reservas
            SET Estado = ?
            WHERE Id = ?
        """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idReserva);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Lista las reservas PENDIENTES de un cliente.
     * Calcula PosicionCola usando ROW_NUMBER() por libro, ordenado por CreadoUtc.
     */
    public List<Reserva> listarReservasPendientesPorCliente(int idCliente) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();

        String sql = """
            SELECT
                r.Id,
                r.IdCliente,
                r.IdLibro,
                r.IdCopia,
                r.CreadoUtc,
                r.ExpiraUtc,
                r.Estado,
                r.Notas,
                ROW_NUMBER() OVER(
                    PARTITION BY r.IdLibro
                    ORDER BY r.CreadoUtc
                ) AS PosicionCola
            FROM dbo.Reservas r
            WHERE r.IdCliente = ? AND r.Estado = 'PENDIENTE'
            ORDER BY r.CreadoUtc
        """;

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
     * Verifica si existe al menos una reserva pendiente para un libro.
     */
    public boolean existeReservaPendiente(int idLibro) throws SQLException {
        String sql = """
            SELECT COUNT(Id)
            FROM dbo.Reservas
            WHERE IdLibro = ? AND Estado = 'PENDIENTE'
        """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idLibro);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // --- Helper ---

    private Reserva mapearResultSet(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("Id"));
        r.setIdCliente(rs.getInt("IdCliente"));
        r.setIdLibro(rs.getInt("IdLibro"));

        int idCopia = rs.getInt("IdCopia");
        r.setIdCopia(rs.wasNull() ? null : idCopia);

        Timestamp creado = rs.getTimestamp("CreadoUtc");
        if (creado != null) {
            r.setFechaCreado(new java.util.Date(creado.getTime()));
        }

        Timestamp expira = rs.getTimestamp("ExpiraUtc");
        if (expira != null) {
            r.setFechaExpira(new java.util.Date(expira.getTime()));
        }

        r.setEstado(rs.getString("Estado"));
        r.setNotas(rs.getString("Notas"));

        // Campo calculado
        try {
            int pos = rs.getInt("PosicionCola");
            if (!rs.wasNull()) r.setPosicionCola(pos);
        } catch (SQLException ignore) {
            // Si la consulta no trae PosicionCola, simplemente se queda en 0
        }

        return r;
    }
}
