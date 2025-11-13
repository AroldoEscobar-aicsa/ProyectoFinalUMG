package app.dao;

import app.db.Conexion;
import app.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // Listar todas las categorías (activas e inactivas)
    public List<Categoria> listarTodas() throws Exception {
        String sql = """
            SELECT Id, Nombre, IsActive
            FROM dbo.Categorias
            ORDER BY Nombre
        """;
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapCategoria(rs));
            }
        }
        return lista;
    }

    // Listar solo activas (por si lo necesitas en combos)
    public List<Categoria> listarActivas() throws Exception {
        String sql = """
            SELECT Id, Nombre, IsActive
            FROM dbo.Categorias
            WHERE IsActive = 1
            ORDER BY Nombre
        """;
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapCategoria(rs));
            }
        }
        return lista;
    }

    public Categoria buscarPorId(int id) throws Exception {
        String sql = """
            SELECT Id, Nombre, IsActive
            FROM dbo.Categorias
            WHERE Id = ?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCategoria(rs);
            }
        }
        return null;
    }

    public void crear(Categoria c) throws Exception {
        String sql = """
            INSERT INTO dbo.Categorias(Nombre, IsActive)
            VALUES (?, ?)
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        }
    }

    public void actualizar(Categoria c) throws Exception {
        String sql = """
            UPDATE dbo.Categorias
            SET Nombre = ?, IsActive = ?
            WHERE Id = ?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActivo());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }
    }

    // Eliminado lógico
    public void desactivar(int id) throws Exception {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE dbo.Categorias SET IsActive = 0 WHERE Id = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Categoria mapCategoria(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("Id"));
        c.setNombre(rs.getString("Nombre"));
        c.setActivo(rs.getBoolean("IsActive"));
        return c;
    }
}
