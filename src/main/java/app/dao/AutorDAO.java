package app.dao;

import app.db.Conexion;
import app.model.Autor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO de Autores (usa baja lógica con IsActive) */
public class AutorDAO {

    // LISTAR TODOS
    public List<Autor> listarTodos() throws Exception {
        String sql = "SELECT Id, Nombre, Pais, IsActive FROM dbo.Autores ORDER BY Nombre";
        List<Autor> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }
        return lista;
    }

    // LISTAR ACTIVOS
    public List<Autor> listarActivos() throws Exception {
        String sql = "SELECT Id, Nombre, Pais, IsActive FROM dbo.Autores WHERE IsActive = 1 ORDER BY Nombre";
        List<Autor> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }
        return lista;
    }

    // CREAR
    public void crear(Autor autor) throws Exception {
        String sql = "INSERT INTO dbo.Autores (Nombre, Pais, IsActive) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getPais());
            ps.setBoolean(3, autor.isActivo());
            ps.executeUpdate();
        }
    }

    // ACTUALIZAR
    public void actualizar(Autor autor) throws Exception {
        String sql = "UPDATE dbo.Autores SET Nombre = ?, Pais = ?, IsActive = ? WHERE Id = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getPais());
            ps.setBoolean(3, autor.isActivo());
            ps.setInt(4, autor.getId());
            ps.executeUpdate();
        }
    }

    // BAJA LÓGICA
    public void eliminarLogico(int id) throws Exception {
        String sql = "UPDATE dbo.Autores SET IsActive = 0 WHERE Id = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // BUSCAR POR ID
    public Autor buscarPorId(int id) throws Exception {
        String sql = "SELECT Id, Nombre, Pais, IsActive FROM dbo.Autores WHERE Id = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // --- mapper ---
    private Autor map(ResultSet rs) throws SQLException {
        Autor a = new Autor();
        a.setId(rs.getInt("Id"));
        a.setNombre(rs.getString("Nombre"));
        a.setPais(rs.getString("Pais"));
        a.setActivo(rs.getBoolean("IsActive"));
        return a;
    }
}
