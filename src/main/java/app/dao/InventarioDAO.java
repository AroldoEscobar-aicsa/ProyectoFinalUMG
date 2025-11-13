package app.dao;

import app.db.Conexion;
import app.model.InventarioConteoDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    /**
     * Crea un conteo nuevo en dbo.InventarioConteos y devuelve el Id generado.
     */
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

    /**
     * Inserta un detalle de conteo: solo se envían IdConteo, IdLibro y CantidadFisica.
     * CantidadSistema y Diferencia son columnas calculadas en la BD.
     */
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

    /**
     * Devuelve lista de libros activos con la cantidad en sistema (función dbo.ObtenerCantidadSistema).
     * Esta lista se usa para llenar la tabla del inventario físico antes de guardar.
     */
    public List<InventarioConteoDetalle> prepararDetalleParaConteo() throws SQLException {
        String sql = """
            SELECT l.Id       AS IdLibro,
                   l.Titulo   AS Titulo,
                   dbo.ObtenerCantidadSistema(l.Id) AS CantidadSistema
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
                // Por defecto, tomamos la física igual a la de sistema; el usuario puede modificar
                d.setCantidadFisica(cantSis);
                d.setDiferencia(0);
                lista.add(d);
            }
        }
        return lista;
    }

    /**
     * Consulta detalles de un conteo ya guardado (por si quieres mostrar historial).
     */
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
