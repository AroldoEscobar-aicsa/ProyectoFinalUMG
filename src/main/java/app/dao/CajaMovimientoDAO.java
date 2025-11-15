package app.dao;

import app.db.Conexion;
import app.model.CajaMovimiento;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para movimientos de caja (entradas/salidas en una sesión).
 * Actualizado para coincidir con la estructura SQL real.
 */
public class CajaMovimientoDAO {

    /**
     * Registra un nuevo movimiento de caja (ENTRADA o SALIDA).
     */
    public boolean reistrar(CajaMovimiento movimiento) throws SQLException {
        String sql = "INSERT INTO CajaMovimientos " +
                "(IdCajaSesion, Tipo, Concepto, Monto, IdMulta, CreadoUtc) " +
                "VALUES (?, ?, ?, ?, ?, GETUTCDATE())";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, movimiento.getIdCajaSesion());
            pstmt.setString(2, movimiento.getTipo());
            pstmt.setString(3, movimiento.getConcepto());
            pstmt.setDouble(4, movimiento.getMonto());

            // IdMulta puede ser NULL
            if (movimiento.getIdMulta() != null) {
                pstmt.setInt(5, movimiento.getIdMulta());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setIdMovimiento(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Obtiene todos los movimientos de una sesión de caja.
     */
    public List<CajaMovimiento> getMovimientosPorSesion(int idCajaSesion) throws SQLException {
        List<CajaMovimiento> movimientos = new ArrayList<>();

        String sql = "SELECT * FROM CajaMovimientos " +
                "WHERE IdCajaSesion = ? " +
                "ORDER BY CreadoUtc ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCajaSesion);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearResultSet(rs));
                }
            }
        }
        return movimientos;
    }

    /**
     * Calcula el total de una sesión de caja.
     * Suma: ENTRADAS - SALIDAS
     */
    public double calcularTotalSesion(int idCajaSesion) throws SQLException {
        String sql = "SELECT " +
                "ISNULL(SUM(CASE WHEN Tipo = 'ENTRADA' THEN Monto ELSE 0 END), 0) - " +
                "ISNULL(SUM(CASE WHEN Tipo = 'SALIDA' THEN Monto ELSE 0 END), 0) AS Total " +
                "FROM CajaMovimientos " +
                "WHERE IdCajaSesion = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCajaSesion);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Obtiene solo las ENTRADAS de una sesión (para reportes).
     */
    public List<CajaMovimiento> getEntradasPorSesion(int idCajaSesion) throws SQLException {
        List<CajaMovimiento> entradas = new ArrayList<>();

        String sql = "SELECT * FROM CajaMovimientos " +
                "WHERE IdCajaSesion = ? AND Tipo = 'ENTRADA' " +
                "ORDER BY CreadoUtc ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCajaSesion);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entradas.add(mapearResultSet(rs));
                }
            }
        }
        return entradas;
    }

    /**
     * REPORTE: Obtiene todos los movimientos en un rango de fechas.
     * Útil para reportes de recaudación.
     */
    public List<CajaMovimiento> getMovimientosPorRangoFechas(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String tipo) throws SQLException {

        List<CajaMovimiento> movimientos = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT cm.* FROM CajaMovimientos cm ");
        sql.append("JOIN CajaSesiones cs ON cs.Id = cm.IdCajaSesion ");
        sql.append("WHERE CAST(cs.AbiertaUtc AS DATE) BETWEEN ? AND ? ");

        if (tipo != null && !tipo.isEmpty()) {
            sql.append("AND cm.Tipo = ? ");
        }

        sql.append("ORDER BY cm.CreadoUtc DESC");

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));

            if (tipo != null && !tipo.isEmpty()) {
                pstmt.setString(3, tipo);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearResultSet(rs));
                }
            }
        }
        return movimientos;
    }

    /**
     * Obtiene el total recaudado en un rango de fechas.
     */
    public double getTotalRecaudado(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = "SELECT ISNULL(SUM(cm.Monto), 0) AS Total " +
                "FROM CajaMovimientos cm " +
                "JOIN CajaSesiones cs ON cs.Id = cm.IdCajaSesion " +
                "WHERE cm.Tipo = 'ENTRADA' " +
                "AND CAST(cs.AbiertaUtc AS DATE) BETWEEN ? AND ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Busca un movimiento por ID.
     */
    public CajaMovimiento buscarPorId(int idMovimiento) throws SQLException {
        String sql = "SELECT * FROM CajaMovimientos WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMovimiento);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * MÉTODO AÑADIDO: Verifica si un usuario ya tiene una sesión
     * en estado 'ABIERTA' para una fecha específica.
     *
     * @param idUsuario El ID del usuario cajero.
     * @param fecha La fecha a verificar (generalmente 'hoy').
     * @return true si ya existe una sesión abierta, false si no.
     */
    public boolean verificarCajaAbierta(int idUsuario, LocalDate fecha) throws SQLException {

        // Esta consulta busca en la tabla CajaSesiones
        String sql = "SELECT COUNT(Id) AS Total " +
                "FROM CajaSesiones " +
                "WHERE IdUsuario = ? " +
                "AND Estado = 'ABIERTA' " +
                "AND CAST(AbiertaUtc AS DATE) = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            pstmt.setDate(2, Date.valueOf(fecha));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si el conteo es > 0, significa que SÍ hay una caja abierta
                    return rs.getInt("Total") > 0;
                }
            }
        }
        // Si hay un error o no encuentra nada, asumimos que no hay caja
        return false;
    }

    // --- Helper para mapear ResultSet ---
    private CajaMovimiento mapearResultSet(ResultSet rs) throws SQLException {
        CajaMovimiento mov = new CajaMovimiento();

        mov.setIdMovimiento(rs.getInt("Id"));
        mov.setIdCajaSesion(rs.getInt("IdCajaSesion"));
        mov.setTipo(rs.getString("Tipo"));
        mov.setConcepto(rs.getString("Concepto"));
        mov.setMonto(rs.getDouble("Monto"));

        Timestamp ts = rs.getTimestamp("CreadoUtc");
        if (ts != null) {
            mov.setCreadoUtc(ts.toLocalDateTime());
        }

        // IdMulta puede ser NULL
        int idMulta = rs.getInt("IdMulta");
        if (!rs.wasNull()) {
            mov.setIdMulta(idMulta);
        }

        return mov;
    }
}