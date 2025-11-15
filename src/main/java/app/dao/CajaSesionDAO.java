package app.dao;

import app.db.Conexion;
import app.model.CajaSesion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de sesiones de caja (apertura/cierre).
 */
public class CajaSesionDAO {

    /**
     * Abre una nueva sesión de caja para el usuario.
     * Valida que no exista una sesión abierta previamente.
     */
    public CajaSesion abrirCaja(int idUsuario) throws SQLException {
        // Primero verificar que no tenga caja abierta
        if (tieneCajaAbierta(idUsuario)) {
            throw new SQLException("El usuario ya tiene una caja abierta.");
        }

        String sql = "INSERT INTO CajaSesiones (IdUsuario, AbiertaUtc, Estado) " +
                "VALUES (?, GETUTCDATE(), 'ABIERTA')";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idUsuario);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idSesion = rs.getInt(1);
                        return buscarPorId(idSesion);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Cierra una sesión de caja existente.
     */
    public boolean cerrarCaja(int idCajaSesion, String observacion) throws SQLException {
        String sql = "UPDATE CajaSesiones " +
                "SET CerradaUtc = GETUTCDATE(), " +
                "    Estado = 'CERRADA', " +
                "    Observacion = ? " +
                "WHERE Id = ? AND Estado = 'ABIERTA'";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (observacion != null) {
                pstmt.setString(1, observacion);
            } else {
                pstmt.setNull(1, Types.NVARCHAR);
            }
            pstmt.setInt(2, idCajaSesion);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Verifica si el usuario tiene una caja abierta actualmente.
     */
    public boolean tieneCajaAbierta(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CajaSesiones " +
                "WHERE IdUsuario = ? AND Estado = 'ABIERTA'";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Obtiene la sesión de caja abierta del usuario (si existe).
     */
    public CajaSesion getCajaAbierta(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM CajaSesiones " +
                "WHERE IdUsuario = ? AND Estado = 'ABIERTA'";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Busca una sesión de caja por ID.
     */
    public CajaSesion buscarPorId(int idSesion) throws SQLException {
        String sql = "SELECT * FROM CajaSesiones WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idSesion);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene todas las sesiones de caja de un usuario en un rango de fechas.
     */
    public List<CajaSesion> getSesionesPorUsuarioYFecha(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        List<CajaSesion> sesiones = new ArrayList<>();

        String sql = "SELECT * FROM CajaSesiones " +
                "WHERE IdUsuario = ? " +
                "AND CAST(AbiertaUtc AS DATE) BETWEEN ? AND ? " +
                "ORDER BY AbiertaUtc DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            pstmt.setDate(2, Date.valueOf(fechaInicio));
            pstmt.setDate(3, Date.valueOf(fechaFin));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapearResultSet(rs));
                }
            }
        }
        return sesiones;
    }

    /**
     * Obtiene todas las sesiones en un rango de fechas (para reportes).
     */
    public List<CajaSesion> getSesionesPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        List<CajaSesion> sesiones = new ArrayList<>();

        String sql = "SELECT * FROM CajaSesiones " +
                "WHERE CAST(AbiertaUtc AS DATE) BETWEEN ? AND ? " +
                "ORDER BY AbiertaUtc DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapearResultSet(rs));
                }
            }
        }
        return sesiones;
    }

    // --- Helper para mapear ResultSet ---
    private CajaSesion mapearResultSet(ResultSet rs) throws SQLException {
        CajaSesion sesion = new CajaSesion();

        sesion.setId(rs.getInt("Id"));
        sesion.setIdUsuario(rs.getInt("IdUsuario"));
        sesion.setEstado(rs.getString("Estado"));

        Timestamp abierta = rs.getTimestamp("AbiertaUtc");
        if (abierta != null) {
            sesion.setAbiertaUtc(abierta.toLocalDateTime());
        }

        Timestamp cerrada = rs.getTimestamp("CerradaUtc");
        if (cerrada != null) {
            sesion.setCerradaUtc(cerrada.toLocalDateTime());
        }

        sesion.setObservacion(rs.getString("Observacion"));

        return sesion;
    }
}