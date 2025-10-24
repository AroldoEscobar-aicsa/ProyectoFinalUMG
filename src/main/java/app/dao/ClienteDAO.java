package app.dao;
import app.db.Conexion;
import app.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para el Módulo C: Clientes/Usuarios lectores.
 * Sigue el formato de MultaDAO (mapeador y PreparedStatement).
 */
public class ClienteDAO {

    /**
     * Registra un nuevo cliente en la base de datos.
     */
    public boolean registrarCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO Cliente (nombres, apellidos, nit, telefono, email, estado, fechaRegistro, eliminado) " +
                "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), 0)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombres());
            pstmt.setString(2, cliente.getApellidos());
            pstmt.setString(3, cliente.getNit());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getEmail());
            pstmt.setString(6, cliente.getEstado()); // "Activo"

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza la información de un cliente existente.
     */
    public boolean actualizarCliente(Cliente cliente) throws SQLException {
        String sql = "UPDATE Cliente SET nombres = ?, apellidos = ?, nit = ?, telefono = ?, email = ?, estado = ? " +
                "WHERE idCliente = ? AND eliminado = 0";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombres());
            pstmt.setString(2, cliente.getApellidos());
            pstmt.setString(3, cliente.getNit());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getEmail());
            pstmt.setString(6, cliente.getEstado()); // "Activo" o "Bloqueado"
            pstmt.setInt(7, cliente.getIdCliente());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Realiza una eliminación lógica de un cliente[cite: 69].
     */
    public boolean eliminarCliente(int idCliente) throws SQLException {
        String sql = "UPDATE Cliente SET eliminado = 1 WHERE idCliente = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Busca un cliente por su ID (solo si no está eliminado).
     */
    public Cliente buscarClientePorId(int idCliente) throws SQLException {
        String sql = "SELECT * FROM Cliente WHERE idCliente = ? AND eliminado = 0";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos los clientes que NO han sido eliminados lógicamente.
     */
    public List<Cliente> listarClientesActivos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM Cliente WHERE eliminado = 0 ORDER BY apellidos, nombres";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapearResultSet(rs));
            }
        }
        return clientes;
    }

    // --- Helper ---

    /**
     * Método helper para convertir un ResultSet en un objeto Cliente.
     * (Igual que en MultaDAO).
     */
    private Cliente mapearResultSet(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("idCliente"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setNit(rs.getString("nit"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setEstado(rs.getString("estado"));
        c.setFechaRegistro(rs.getTimestamp("fechaRegistro"));
        c.setEliminado(rs.getBoolean("eliminado"));
        return c;
    }
}