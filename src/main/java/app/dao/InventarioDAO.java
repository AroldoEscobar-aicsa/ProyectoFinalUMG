package app.dao;

import app.db.Conexion;
import app.model.InventarioConteoDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    /**
     * Guarda un conteo completo (encabezado + detalle) y aplica los ajustes de inventario.
     * - Crea registro en dbo.InventarioConteos
     * - Inserta todas las filas en dbo.InventarioConteoDetalle
     * - Ejecuta dbo.sp_Inventario_AplicarConteo para ajustar Copias/Disponibles
     *
     * Todo en UNA sola transacción.
     */
    public int guardarConteoConAjustes(int idUsuario, String comentario,
                                       List<InventarioConteoDetalle> detalles) throws SQLException {

        if (detalles == null || detalles.isEmpty()) {
            throw new SQLException("No hay detalles de conteo para guardar.");
        }

        String sqlConteo =
                "INSERT INTO dbo.InventarioConteos (IdUsuario, Comentario) VALUES (?, ?)";
        String sqlDetalle =
                "INSERT INTO dbo.InventarioConteoDetalle (IdConteo, IdLibro, CantidadFisica) VALUES (?, ?, ?)";
        String sqlAplicar =
                "EXEC dbo.sp_Inventario_AplicarConteo ?, ?";

        Connection cn = null;
        try {
            cn = Conexion.getConnection();
            cn.setAutoCommit(false);

            int idConteo;

            // 1) Insert encabezado
            try (PreparedStatement ps = cn.prepareStatement(sqlConteo, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idUsuario);
                if (comentario == null || comentario.isBlank()) {
                    ps.setNull(2, Types.NVARCHAR);
                } else {
                    ps.setString(2, comentario.trim());
                }
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se pudo obtener el Id del conteo.");
                    }
                    idConteo = rs.getInt(1);
                }
            }

            // 2) Insert detalle en batch
            try (PreparedStatement psDet = cn.prepareStatement(sqlDetalle)) {
                for (InventarioConteoDetalle d : detalles) {
                    psDet.setInt(1, idConteo);
                    psDet.setInt(2, d.getIdLibro());
                    psDet.setInt(3, d.getCantidadFisica() != null ? d.getCantidadFisica() : 0);
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            // 3) Aplicar conteo (ajustar existencias / CopiasDisponibles)
            try (PreparedStatement psApl = cn.prepareStatement(sqlAplicar)) {
                psApl.setInt(1, idConteo);
                psApl.setInt(2, idUsuario);
                psApl.execute();
            }

            cn.commit();
            return idConteo;

        } catch (SQLException ex) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw ex;
        } finally {
            if (cn != null) {
                try { cn.setAutoCommit(true); cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * PREPARA los libros para conteo, tomando CantidadSistema desde
     * dbo.ObtenerCantidadSistema (sólo DISPONIBLES tras el cambio).
     */
    public List<InventarioConteoDetalle> prepararDetalleParaConteo() throws SQLException {
        String sql = """
        SELECT l.Id       AS IdLibro,
               l.Titulo   AS Titulo,
               dbo.ObtenerCantidadDisponibles(l.Id) AS CantidadSistema
        FROM dbo.Libros l
        WHERE l.IsActive = 1
        ORDER BY l.Titulo
        """;

        List<InventarioConteoDetalle> lista = new ArrayList<>();

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventarioConteoDetalle d = new InventarioConteoDetalle();
                d.setIdLibro(rs.getInt("IdLibro"));
                d.setTituloLibro(rs.getString("Titulo"));
                int cantSis = rs.getInt("CantidadSistema");
                if (rs.wasNull()) cantSis = 0;
                d.setCantidadSistema(cantSis);
                d.setCantidadFisica(cantSis); // default = sistema
                d.setDiferencia(0);
                lista.add(d);
            }
        }
        return lista;
    }

    // Mantengo los métodos anteriores por si los usas en otros lados
    public int crearConteo(int idUsuario, String comentario) throws SQLException {
        String sql = "INSERT INTO dbo.InventarioConteos (IdUsuario, Comentario) VALUES (?, ?)";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idUsuario);
            if (comentario == null || comentario.isBlank()) {
                ps.setNull(2, Types.NVARCHAR);
            } else {
                ps.setString(2, comentario.trim());
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo generar Id de conteo de inventario.");
    }

    public void insertarDetalleConteo(int idConteo, int idLibro, int cantidadFisica) throws SQLException {
        String sql = "INSERT INTO dbo.InventarioConteoDetalle (IdConteo, IdLibro, CantidadFisica) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idConteo);
            ps.setInt(2, idLibro);
            ps.setInt(3, cantidadFisica);
            ps.executeUpdate();
        }
    }

    // listarDetallesPorConteo se queda igual si lo necesitas para historial
    public List<InventarioConteoDetalle> listarDetallesPorConteo(int idConteo) throws SQLException {
        String sql = """
            SELECT d.Id,
                   d.IdConteo,
                   d.IdLibro,
                   l.Titulo,
                   d.CantidadFisica,
                   d.CantidadSistema,
                   d.Diferencia
            FROM dbo.InventarioConteoDetalle d
            JOIN dbo.Libros l ON l.Id = d.IdLibro
            WHERE d.IdConteo = ?
            ORDER BY l.Titulo
            """;

        List<InventarioConteoDetalle> lista = new ArrayList<>();

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idConteo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventarioConteoDetalle d = new InventarioConteoDetalle();
                    d.setId(rs.getInt("Id"));
                    d.setIdConteo(rs.getInt("IdConteo"));
                    d.setIdLibro(rs.getInt("IdLibro"));
                    d.setTituloLibro(rs.getString("Titulo"));
                    d.setCantidadFisica(rs.getInt("CantidadFisica"));
                    d.setCantidadSistema(rs.getInt("CantidadSistema"));
                    d.setDiferencia(rs.getInt("Diferencia"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }
}
