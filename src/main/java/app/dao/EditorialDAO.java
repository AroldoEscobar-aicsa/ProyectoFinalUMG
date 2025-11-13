package app.dao;

import app.db.Conexion;
import app.model.Editorial;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EditorialDAO {

    public List<Editorial> listarTodas() throws SQLException {
        String sql = """
            SELECT Id, Nombre, Pais, IsActive
            FROM dbo.Editoriales
            ORDER BY Nombre
            """;
        List<Editorial> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Editorial e = new Editorial();
                e.setId(rs.getInt("Id"));
                e.setNombre(rs.getString("Nombre"));
                e.setPais(rs.getString("Pais"));
                e.setActivo(rs.getBoolean("IsActive"));
                lista.add(e);
            }
        }
        return lista;
    }

    public List<Editorial> listarActivas() throws SQLException {
        String sql = """
            SELECT Id, Nombre, Pais, IsActive
            FROM dbo.Editoriales
            WHERE IsActive = 1
            ORDER BY Nombre
            """;
        List<Editorial> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Editorial e = new Editorial();
                e.setId(rs.getInt("Id"));
                e.setNombre(rs.getString("Nombre"));
                e.setPais(rs.getString("Pais"));
                e.setActivo(rs.getBoolean("IsActive"));
                lista.add(e);
            }
        }
        return lista;
    }

    public Editorial buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT Id, Nombre, Pais, IsActive
            FROM dbo.Editoriales
            WHERE Id = ?
            """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Editorial e = new Editorial();
                    e.setId(rs.getInt("Id"));
                    e.setNombre(rs.getString("Nombre"));
                    e.setPais(rs.getString("Pais"));
                    e.setActivo(rs.getBoolean("IsActive"));
                    return e;
                }
            }
        }
        return null;
    }

    public void crear(Editorial e) throws SQLException {
        String sql = """
            INSERT INTO dbo.Editoriales (Nombre, Pais, IsActive)
            VALUES (?, ?, ?)
            """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNombre());
            if (e.getPais() == null || e.getPais().isBlank()) {
                ps.setNull(2, Types.NVARCHAR);
            } else {
                ps.setString(2, e.getPais().trim());
            }
            ps.setBoolean(3, e.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getInt(1));
                }
            }
        }
    }

    public void actualizar(Editorial e) throws SQLException {
        String sql = """
            UPDATE dbo.Editoriales
            SET Nombre = ?, Pais = ?, IsActive = ?
            WHERE Id = ?
            """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            if (e.getPais() == null || e.getPais().isBlank()) {
                ps.setNull(2, Types.NVARCHAR);
            } else {
                ps.setString(2, e.getPais().trim());
            }
            ps.setBoolean(3, e.isActivo());
            ps.setInt(4, e.getId());
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE dbo.Editoriales SET IsActive = 0 WHERE Id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
