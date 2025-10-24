package app.dao;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDate;

import app.db.Conexion;
import app.model.CajaMovimiento;
import java.sql.*;

public class CajaMovimientoDAO {
    /*Registra cualquier movimiento de caja (Apertura, Ingreso, Cierre).
     Este es el metodo central de la caja.*/

    public boolean registrar(CajaMovimiento mov) throws SQLException {
        // SQL Server maneja bien los nulos para campos no especificados
        String sql = "INSERT INTO CajaMovimiento (idUsuarioCajero, fechaHora, tipoMovimiento, " +
                "descripcion, monto, idMulta, montoCalculadoSistema, montoRealContado, diferencia) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mov.getIdUsuarioCajero());
            pstmt.setTimestamp(2, Timestamp.valueOf(mov.getFechaHora()));
            pstmt.setString(3, mov.getTipoMovimiento());
            pstmt.setString(4, mov.getDescripcion());
            pstmt.setDouble(5, mov.getMonto());

            // Manejo de nulos para FK y campos de cierre
            if (mov.getIdMulta() != null) {
                pstmt.setInt(6, mov.getIdMulta());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            if (mov.getMontoCalculadoSistema() != null) {
                pstmt.setDouble(7, mov.getMontoCalculadoSistema());
            } else {
                pstmt.setNull(7, Types.DECIMAL);
            }

            if (mov.getMontoRealContado() != null) {
                pstmt.setDouble(8, mov.getMontoRealContado());
            } else {
                pstmt.setNull(8, Types.DECIMAL);
            }

            if (mov.getDiferencia() != null) {
                pstmt.setDouble(9, mov.getDiferencia());
            } else {
                pstmt.setNull(9, Types.DECIMAL);
            }

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene todos los movimientos de un cajero en una fecha específica.
     * Usado para el "Resumen por fecha/usuario".
     */
    public List<CajaMovimiento> getMovimientosDiaUsuario(int idUsuarioCajero, LocalDate fecha) throws SQLException {
        List<CajaMovimiento> movimientos = new ArrayList<>();
        // CONVERT(DATE, ...) es sintaxis de SQL Server para ignorar la hora
        String sql = "SELECT * FROM CajaMovimiento " +
                "WHERE idUsuarioCajero = ? AND CONVERT(DATE, fechaHora) = ? " +
                "ORDER BY fechaHora ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuarioCajero);
            pstmt.setDate(2, Date.valueOf(fecha));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearResultSet(rs));
                }
            }
        }
        return movimientos;
    }

    /**
     * Verifica si un cajero ya tiene una caja abierta (Apertura)
     * pero aún no la ha cerrado (Cierre) para el día de hoy.
     */
    public boolean verificarCajaAbierta(int idUsuarioCajero, LocalDate fecha) throws SQLException {
        String sqlApertura = "SELECT COUNT(*) FROM CajaMovimiento WHERE idUsuarioCajero = ? AND CONVERT(DATE, fechaHora) = ? AND tipoMovimiento = 'Apertura'";
        String sqlCierre = "SELECT COUNT(*) FROM CajaMovimiento WHERE idUsuarioCajero = ? AND CONVERT(DATE, fechaHora) = ? AND tipoMovimiento = 'Cierre'";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmtApertura = conn.prepareStatement(sqlApertura);
             PreparedStatement pstmtCierre = conn.prepareStatement(sqlCierre)) {

            pstmtApertura.setInt(1, idUsuarioCajero);
            pstmtApertura.setDate(2, Date.valueOf(fecha));

            pstmtCierre.setInt(1, idUsuarioCajero);
            pstmtCierre.setDate(2, Date.valueOf(fecha));

            boolean hayApertura = false;
            try(ResultSet rsA = pstmtApertura.executeQuery()) {
                if(rsA.next() && rsA.getInt(1) > 0) {
                    hayApertura = true;
                }
            }

            boolean hayCierre = false;
            try(ResultSet rsC = pstmtCierre.executeQuery()) {
                if(rsC.next() && rsC.getInt(1) > 0) {
                    hayCierre = true;
                }
            }

            return hayApertura && !hayCierre; // Abierta si hay apertura Y NO hay cierre
        }
    }

    /**
     * Calcula el total del sistema para el arqueo.
     * Suma la Apertura + todos los Ingresos de un cajero para una fecha.
     */
    public double calcularTotalSistemaDia(int idUsuarioCajero, LocalDate fecha) throws SQLException {
        String sql = "SELECT SUM(monto) FROM CajaMovimiento " +
                "WHERE idUsuarioCajero = ? AND CONVERT(DATE, fechaHora) = ? " +
                "AND tipoMovimiento IN ('Apertura', 'Ingreso')";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuarioCajero);
            pstmt.setDate(2, Date.valueOf(fecha));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1); // Devuelve la suma
                }
            }
        }
        return 0.0; // Si no hay movimientos
    }

    // --- REPORTE DE RECAUDACIÓN ---
    // (Este es solo un ejemplo, puedes hacerlo tan complejo como necesites)
    public List<Map<String, Object>> getReporteRecaudacion(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        List<Map<String, Object>> reporte = new ArrayList<>();
        String sql = "SELECT CONVERT(DATE, fechaHora) as Fecha, idUsuarioCajero, SUM(monto) as TotalRecaudado " +
                "FROM CajaMovimiento " +
                "WHERE tipoMovimiento = 'Ingreso' AND fechaHora BETWEEN ? AND ? " +
                "GROUP BY CONVERT(DATE, fechaHora), idUsuarioCajero " +
                "ORDER BY Fecha, idUsuarioCajero";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(fechaInicio.atStartOfDay()));
            pstmt.setTimestamp(2, Timestamp.valueOf(fechaFin.atTime(23, 59, 59)));

            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("Fecha", rs.getDate("Fecha").toLocalDate());
                    fila.put("idUsuarioCajero", rs.getInt("idUsuarioCajero"));
                    fila.put("TotalRecaudado", rs.getDouble("TotalRecaudado"));
                    reporte.add(fila);
                }
            }
        }
        return reporte;
    }


    // --- Helper ---
    private CajaMovimiento mapearResultSet(ResultSet rs) throws SQLException {
        CajaMovimiento m = new CajaMovimiento();
        m.setIdMovimiento(rs.getInt("idMovimiento"));
        m.setIdUsuarioCajero(rs.getInt("idUsuarioCajero"));
        m.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
        m.setTipoMovimiento(rs.getString("tipoMovimiento"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setMonto(rs.getDouble("monto"));

        m.setIdMulta((Integer) rs.getObject("idMulta")); // Maneja nulos
        m.setMontoCalculadoSistema((Double) rs.getObject("montoCalculadoSistema"));
        m.setMontoRealContado((Double) rs.getObject("montoRealContado"));
        m.setDiferencia((Double) rs.getObject("diferencia"));

        return m;
    }
}