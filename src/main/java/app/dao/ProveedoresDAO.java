package app.dao;

import app.db.Conexion; // Asegúrate de que esta es tu clase de conexión
import app.model.Proveedores;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para las operaciones CRUD de la tabla 'Proveedores'.
 */
public class ProveedoresDAO {

    /**
     * Mapea un ResultSet a un objeto Proveedor.
     */
    private Proveedores mapearProveedor(ResultSet rs) throws SQLException {
        Proveedores p = new Proveedores();
        p.setId(rs.getInt("Id"));
        p.setNombre(rs.getString("Nombre"));
        p.setNit(rs.getString("NIT"));
        p.setTelefono(rs.getString("Telefono"));
        p.setEmail(rs.getString("Email"));
        p.setActive(rs.getBoolean("IsActive"));
        return p;
    }

    /**
     * Obtiene todos los proveedores activos.
     */
    public List<Proveedores> listarActivos() throws SQLException {
        List<Proveedores> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM Proveedores WHERE IsActive = 1 ORDER BY Nombre";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
        }
        return proveedores;
    }

    /**
     * Busca un proveedor por su ID.
     */
    public Proveedores buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Proveedores WHERE Id = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProveedor(rs);
                }
            }
        }
        return null; // No encontrado
    }

    /**
     * Crea un nuevo proveedor.
     */
    public boolean crear(Proveedores p) throws SQLException {
        String sql = "INSERT INTO Proveedores (Nombre, NIT, Telefono, Email, IsActive) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getNombre());
            pstmt.setString(2, p.getNit());
            pstmt.setString(3, p.getTelefono());
            pstmt.setString(4, p.getEmail());
            pstmt.setBoolean(5, p.isActive()); // O siempre 'true' (1) por defecto

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza un proveedor existente.
     */
    public boolean actualizar(Proveedores p) throws SQLException {
        String sql = "UPDATE Proveedores SET Nombre = ?, NIT = ?, Telefono = ?, Email = ?, IsActive = ? WHERE Id = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getNombre());
            pstmt.setString(2, p.getNit());
            pstmt.setString(3, p.getTelefono());
            pstmt.setString(4, p.getEmail());
            pstmt.setBoolean(5, p.isActive());
            pstmt.setInt(6, p.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina (lógicamente) un proveedor.
     * (Cambia IsActive a 0)
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "UPDATE Proveedores SET IsActive = 0 WHERE Id = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}