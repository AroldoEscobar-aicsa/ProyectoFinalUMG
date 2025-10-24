package app.dao;

import app.db.Conexion;
import app.model.Autor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AutorDAO - Acceso a datos para la entidad Autor.
 * Utiliza eliminación lógica (campo estado).
 */
public class AutorDAO {

    // ===== LISTAR TODOS =====
    public List<Autor> listarTodos() throws Exception {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, estado FROM Autor ORDER BY nombre";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Autor a = new Autor(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("estado")
                );
                lista.add(a);
            }
        }
        return lista;
    }

    // ===== CREAR =====
    public void crear(Autor autor) throws Exception {
        String sql = "INSERT INTO Autor(nombre, estado) VALUES (?, ?)";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setBoolean(2, autor.isEstado());
            ps.executeUpdate();
        }
    }

    // ===== ACTUALIZAR =====
    public void actualizar(Autor autor) throws Exception {
        String sql = "UPDATE Autor SET nombre = ?, estado = ? WHERE id = ?";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, autor.getNombre());
            ps.setBoolean(2, autor.isEstado());
            ps.setInt(3, autor.getId());
            ps.executeUpdate();
        }
    }

    // ===== ELIMINAR LÓGICO =====
    public void eliminarLogico(int id) throws Exception {
        String sql = "UPDATE Autor SET estado = 0 WHERE id = ?";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ===== BUSCAR POR ID =====
    public Autor buscarPorId(int id) throws Exception {
        Autor a = null;
        String sql = "SELECT id, nombre, estado FROM Autor WHERE id = ?";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a = new Autor();
                    a.setId(rs.getInt("id"));
                    a.setNombre(rs.getString("nombre"));
                    a.setEstado(rs.getBoolean("estado"));
                }
            }
        }
        return a;
    }

    // ===== LISTAR ACTIVOS =====
    public List<Autor> listarActivos() throws Exception {
        List<Autor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, estado FROM Autor WHERE estado = 1 ORDER BY nombre";

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Autor a = new Autor();
                a.setId(rs.getInt("id"));
                a.setNombre(rs.getString("nombre"));
                a.setEstado(rs.getBoolean("estado"));
                lista.add(a);
            }
        }
        return lista;
    }
}
