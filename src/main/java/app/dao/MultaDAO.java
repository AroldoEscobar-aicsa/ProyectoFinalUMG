package app.dao;

import app.db.Conexion;
import app.model.Multa;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultaDAO {

    /**
     * Crea un nuevo registro de multa en la BD.
     * Se llama cuando un préstamo entra en mora.
     * Mapea:
     *  - IdPrestamo, IdCliente, Monto, Estado, Justificacion
     *  - CreadoUtc lo llena la BD con GETUTCDATE()
     */
    public boolean crear(Multa multa) throws SQLException {
        String sql = "INSERT INTO Multas (IdPrestamo, IdCliente, Monto, Estado, Justificacion) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, multa.getIdPrestamo());
            pstmt.setInt(2, multa.getIdCliente());
            pstmt.setDouble(3, multa.getMontoCalculado());

            String estado = multa.getEstado();
            if (estado == null || estado.trim().isEmpty()) {
                estado = "PENDIENTE";
            }
            pstmt.setString(4, estado.toUpperCase());

            pstmt.setString(5, multa.getJustificacionExoneracion());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el estado de una multa a PAGADA (pago total).
     * No hay columna montoPagado, así que sólo se marca el estado y la fecha de pago.
     * Esto se llama DESPUÉS de registrar el CajaMovimiento.
     */
    public boolean actualizarPago(int idMulta, double nuevoMontoPagadoTotal, String nuevoEstado) throws SQLException {
        String sql = "UPDATE Multas " +
                "SET Estado = ?, PagadoUtc = SYSUTCDATETIME() " +
                "WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado.toUpperCase());
            pstmt.setInt(2, idMulta);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Marca una multa como EXONERADA y guarda la justificación.
     * Cumple el requisito de "exoneraciones con justificación".
     */
    public boolean exonerar(int idMulta, String justificacion) throws SQLException {
        String sql = "UPDATE Multas " +
                "SET Estado = 'EXONERADA', Justificacion = ?, PagadoUtc = SYSUTCDATETIME() " +
                "WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, justificacion);
            pstmt.setInt(2, idMulta);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene todas las multas pendientes (Cuentas por Cobrar).
     */
    public List<Multa> getMultasPendientes() throws SQLException {
        List<Multa> multas = new ArrayList<>();
        String sql = "SELECT * FROM Multas WHERE Estado = 'PENDIENTE'";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                multas.add(mapearResultSet(rs));
            }
        }
        return multas;
    }

    /**
     * Obtiene las multas pendientes de un cliente específico.
     */
    public List<Multa> getMultasPendientesPorCliente(int idCliente) throws SQLException {
        List<Multa> multas = new ArrayList<>();
        String sql = "SELECT * FROM Multas " +
                "WHERE IdCliente = ? AND Estado = 'PENDIENTE'";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    multas.add(mapearResultSet(rs));
                }
            }
        }
        return multas;
    }

    public Multa buscarPorId(int idMulta) throws SQLException {
        String sql = "SELECT * FROM Multas WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMulta);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * REPORTE: Clientes Morosos.
     * Devuelve un mapa donde la llave es el ID del cliente y el valor es su deuda total.
     * Deuda = SUM(Monto) de multas PENDIENTE.
     */
    public Map<Integer, Double> getReporteClientesMorosos() throws SQLException {
        Map<Integer, Double> reporte = new HashMap<>();
        String sql = "SELECT IdCliente, SUM(Monto) AS deudaTotal " +
                "FROM Multas " +
                "WHERE Estado = 'PENDIENTE' " +
                "GROUP BY IdCliente " +
                "HAVING SUM(Monto) > 0 " +
                "ORDER BY deudaTotal DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reporte.put(rs.getInt("IdCliente"), rs.getDouble("deudaTotal"));
            }
        }
        return reporte;
    }

    // --- Helper ---
    private Multa mapearResultSet(ResultSet rs) throws SQLException {
        Multa m = new Multa();

        m.setIdMulta(rs.getInt("Id"));
        m.setIdPrestamo(rs.getInt("IdPrestamo"));
        m.setIdCliente(rs.getInt("IdCliente"));

        // Monto -> montoCalculado
        m.setMontoCalculado(rs.getDouble("Monto"));

        // Estado
        String estado = rs.getString("Estado");
        m.setEstado(estado);

        // montoPagado derivado:
        if ("PENDIENTE".equalsIgnoreCase(estado)) {
            m.setMontoPagado(0.0);
        } else {
            m.setMontoPagado(m.getMontoCalculado());
        }

        // Justificación
        m.setJustificacionExoneracion(rs.getString("Justificacion"));

        // Fechas (tomando solo la parte de fecha)
        Timestamp creado = rs.getTimestamp("CreadoUtc");
        if (creado != null) {
            LocalDateTime ldt = creado.toLocalDateTime();
            m.setFechaGeneracion(ldt.toLocalDate());
        }

        Timestamp pagado = rs.getTimestamp("PagadoUtc");
        if (pagado != null) {
            LocalDateTime ldt = pagado.toLocalDateTime();
            m.setFechaLimitePago(ldt.toLocalDate());
        }

        // No existe columna diasAtraso en la BD; por ahora lo dejamos en 0
        m.setDiasAtraso(0);

        return m;
    }
}
