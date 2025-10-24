package app.dao;

import app.db.Conexion;
import app.model.Multa;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultaDAO {
    /**
     * Crea un nuevo registro de multa en la BD.
    se llama cuando un préstamo entra en mora.*/

    public boolean crear(Multa multa) throws SQLException {
        String sql = "INSERT INTO Multas (idPrestamo, idCliente, fechaGeneracion, diasAtraso, " +
                "montoCalculado, montoPagado, estado, fechaLimitePago) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, multa.getIdPrestamo());
            pstmt.setInt(2, multa.getIdCliente());
            pstmt.setDate(3, Date.valueOf(multa.getFechaGeneracion()));
            pstmt.setInt(4, multa.getDiasAtraso());
            pstmt.setDouble(5, multa.getMontoCalculado());
            pstmt.setDouble(6, multa.getMontoPagado());
            pstmt.setString(7, multa.getEstado());
            pstmt.setDate(8, Date.valueOf(multa.getFechaLimitePago()));

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza el monto pagado y el estado de una multa.
     * Esto se llama DESPUÉS de registrar el CajaMovimiento.
     */
    public boolean actualizarPago(int idMulta, double nuevoMontoPagadoTotal, String nuevoEstado) throws SQLException {
        String sql = "UPDATE Multas SET montoPagado = ?, estado = ? WHERE idMulta = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, nuevoMontoPagadoTotal);
            pstmt.setString(2, nuevoEstado);
            pstmt.setInt(3, idMulta);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Marca una multa como Exonerada y guarda la justificación.
     * Cumple el requisito de "exoneraciones con justificación".
     */
    public boolean exonerar(int idMulta, String justificacion) throws SQLException {
        String sql = "UPDATE Multas SET estado = 'Exonerada', justificacionExoneracion = ? WHERE idMulta = ?";

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
        String sql = "SELECT * FROM Multas WHERE estado = 'Pendiente' AND (montoCalculado > montoPagado)";

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
        String sql = "SELECT * FROM Multas WHERE idCliente = ? AND estado = 'Pendiente' AND (montoCalculado > montoPagado)";

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
        String sql = "SELECT * FROM Multas WHERE idMulta = ?";
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
     */
    public Map<Integer, Double> getReporteClientesMorosos() throws SQLException {
        Map<Integer, Double> reporte = new HashMap<>();
        String sql = "SELECT idCliente, SUM(montoCalculado - montoPagado) AS deudaTotal " +
                "FROM Multas " +
                "WHERE estado = 'Pendiente' " +
                "GROUP BY idCliente " +
                "HAVING SUM(montoCalculado - montoPagado) > 0 " +
                "ORDER BY deudaTotal DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reporte.put(rs.getInt("idCliente"), rs.getDouble("deudaTotal"));
            }
        }
        return reporte;
    }


    // --- Helper ---
    private Multa mapearResultSet(ResultSet rs) throws SQLException {
        Multa m = new Multa();
        m.setIdMulta(rs.getInt("idMulta"));
        m.setIdPrestamo(rs.getInt("idPrestamo"));
        m.setIdCliente(rs.getInt("idCliente"));
        m.setFechaGeneracion(rs.getDate("fechaGeneracion").toLocalDate());
        m.setDiasAtraso(rs.getInt("diasAtraso"));
        m.setMontoCalculado(rs.getDouble("montoCalculado"));
        m.setMontoPagado(rs.getDouble("montoPagado"));
        m.setEstado(rs.getString("estado"));

        Date fechaLimite = rs.getDate("fechaLimitePago");
        if (fechaLimite != null) {
            m.setFechaLimitePago(fechaLimite.toLocalDate());
        }

        m.setJustificacionExoneracion(rs.getString("justificacionExoneracion"));
        return m;
    }
}