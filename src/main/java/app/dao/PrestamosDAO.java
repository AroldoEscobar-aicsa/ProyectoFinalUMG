package app.dao;

import app.db.Conexion;
import app.model.Prestamos;

import javax.swing.*;
import java.sql.*;
import java.awt.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar Préstamos, Renovaciones y Devoluciones.
 * Usa SP:
 *  - sp_Prestamo_Crear(@IdCliente, @IdCopia, @UsuarioEjecuta)
 *  - sp_Prestamo_Renovar(@IdPrestamo, @UsuarioEjecuta)
 *  - sp_Prestamo_Devolver(@IdPrestamo, @UsuarioEjecuta)
 * Y la vista:
 *  - vw_PrestamosActivosPorCliente
 */
public class PrestamosDAO {

    // -------- utilidades de resolución ----------------

    /** Devuelve IdCliente por su "Codigo" (matrícula/carnet) */
    public Integer getIdClienteByCodigo(String codigo) throws SQLException {
        final String sql = "SELECT Id FROM dbo.Clientes WHERE Codigo = ? AND IsActive = 1";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Devuelve IdCopia por su Código de barras si está activa (no inactiva/extraviada/deteriorada) */
    public Integer getIdCopiaByCodigoBarra(String codigoBarra) throws SQLException {
        final String sql = "SELECT Id FROM dbo.Copias WHERE CodigoBarra = ? AND IsActive = 1 AND Estado IN ('DISPONIBLE','RESERVADA','PRESTADA')";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigoBarra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Devuelve el Id del préstamo ACTIVO/ATRASADO para una copia (si existe) */
    public Integer getPrestamoActivoByCopia(int idCopia) throws SQLException {
        final String sql = "SELECT TOP 1 Id FROM dbo.Prestamos WHERE IdCopia=? AND Estado IN ('ACTIVO','ATRASADO') ORDER BY Id DESC";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCopia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    // -------- operaciones principales -----------------

    /** Crea un préstamo (valida con SP). Devuelve el Id del préstamo creado. */
    public int crearPrestamo(int idCliente, int idCopia, String usuarioEjecuta) throws SQLException {
        try (Connection c = Conexion.getConnection();
             CallableStatement cs = c.prepareCall("{ call dbo.sp_Prestamo_Crear(?,?,?) }")) {
            cs.setInt(1, idCliente);
            cs.setInt(2, idCopia);
            cs.setString(3, usuarioEjecuta);
            cs.execute();

            // El SP no devuelve ID; lo buscamos (último préstamo del cliente para esa copia)
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT TOP 1 Id FROM dbo.Prestamos WHERE IdCliente=? AND IdCopia=? ORDER BY Id DESC")) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idCopia);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el Id del préstamo recién creado.");
        }
    }

    /** Renueva préstamo si aplica (sin reservas pendientes). */
    public void renovarPrestamo(int idPrestamo, String usuarioEjecuta) throws SQLException {
        try (Connection c = Conexion.getConnection();
             CallableStatement cs = c.prepareCall("{ call dbo.sp_Prestamo_Renovar(?,?) }")) {
            cs.setInt(1, idPrestamo);
            cs.setString(2, usuarioEjecuta);
            cs.execute();
        }
    }

    /** Devuelve la copia y calcula multa. */
    public void devolverPrestamo(int idPrestamo, String usuarioEjecuta) throws SQLException {
        try (Connection c = Conexion.getConnection();
             CallableStatement cs = c.prepareCall("{ call dbo.sp_Prestamo_Devolver(?,?) }")) {
            cs.setInt(1, idPrestamo);
            cs.setString(2, usuarioEjecuta);
            cs.execute();
        }
    }

    /** Lista préstamos activos/atrasados de un cliente (vista). */
    public List<Prestamos> listarPrestamosActivosPorCliente(int idCliente) throws SQLException {
        final String sql = "SELECT Id, IdCliente, Codigo, Nombres, IdCopia, CodigoBarra, Titulo, " +
                "FechaPrestamoUtc, FechaVencimientoUtc, Renovaciones, Estado " +
                "FROM dbo.vw_PrestamosActivosPorCliente WHERE IdCliente = ? ORDER BY FechaPrestamoUtc DESC";

        List<Prestamos> lista = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prestamos p = new Prestamos();
                    p.setId(rs.getInt("Id"));
                    p.setIdCliente(rs.getInt("IdCliente"));
                    p.setCodigoCliente(rs.getString("Codigo"));
                    p.setNombreCliente(rs.getString("Nombres"));
                    p.setIdCopia(rs.getInt("IdCopia"));
                    p.setCodigoBarra(rs.getString("CodigoBarra"));
                    p.setTitulo(rs.getString("Titulo"));
                    Timestamp fp = rs.getTimestamp("FechaPrestamoUtc");
                    Timestamp fv = rs.getTimestamp("FechaVencimientoUtc");
                    p.setFechaPrestamoUtc(fp != null ? fp.toLocalDateTime() : null);
                    p.setFechaVencimientoUtc(fv != null ? fv.toLocalDateTime() : null);
                    p.setRenovaciones(rs.getInt("Renovaciones"));
                    p.setEstado(rs.getString("Estado"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // -------- helpers para UI -------------------------------------

    public String getTituloPorCodigoBarra(String codigoBarra) throws SQLException {
        final String sql = "SELECT l.Titulo FROM dbo.Copias c JOIN dbo.Libros l ON l.Id = c.IdLibro WHERE c.CodigoBarra = ?";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigoBarra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public String getEstadoCopia(String codigoBarra) throws SQLException {
        final String sql = "SELECT Estado FROM dbo.Copias WHERE CodigoBarra = ?";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigoBarra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public Integer getIdPrestamoActivoPorCodigoBarra(String codigoBarra) throws SQLException {
        Integer idCopia = getIdCopiaByCodigoBarra(codigoBarra);
        return idCopia == null ? null : getPrestamoActivoByCopia(idCopia);
    }

    // Mensajería rápida
    public static void showError(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
