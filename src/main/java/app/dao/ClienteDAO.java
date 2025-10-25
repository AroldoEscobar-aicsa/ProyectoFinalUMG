package app.dao;

import app.db.Conexion;
import app.model.Cliente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // ---------- Utilidades ----------

    public boolean existeCodigo(String codigo) throws SQLException {
        String q = "SELECT 1 FROM dbo.Clientes WHERE Codigo = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(q)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Integer getIdByCodigo(String codigo) throws SQLException {
        String q = "SELECT Id FROM dbo.Clientes WHERE Codigo=? AND IsActive=1";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(q)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
        }
    }

    // ---------- CRUD ----------

    public List<Cliente> listarTodos() throws SQLException {
        String sql = """
            SELECT Id, Codigo, Nombres, Apellidos, NIT, Telefono, Email, IsActive, Estado,
                   FechaRegistroUtc, BloqueadoHastaUtc
            FROM dbo.Clientes
            ORDER BY Apellidos, Nombres
        """;
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<Cliente> listarActivos() throws SQLException {
        String sql = """
            SELECT Id, Codigo, Nombres, Apellidos, NIT, Telefono, Email, IsActive, Estado,
                   FechaRegistroUtc, BloqueadoHastaUtc
            FROM dbo.Clientes
            WHERE IsActive = 1
            ORDER BY Apellidos, Nombres
        """;
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT Id, Codigo, Nombres, Apellidos, NIT, Telefono, Email, IsActive, Estado,
                   FechaRegistroUtc, BloqueadoHastaUtc
            FROM dbo.Clientes WHERE Id=?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public Cliente buscarPorCodigo(String codigo) throws SQLException {
        String sql = """
            SELECT Id, Codigo, Nombres, Apellidos, NIT, Telefono, Email, IsActive, Estado,
                   FechaRegistroUtc, BloqueadoHastaUtc
            FROM dbo.Clientes WHERE Codigo=?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    /** Inserta con FechaRegistroUtc = SYSUTCDATETIME(). */
    public void crear(Cliente c) throws SQLException {
        String sql = """
            INSERT INTO dbo.Clientes (Codigo, Nombres, Apellidos, NIT, Telefono, Email, IsActive, Estado, FechaRegistroUtc)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidos());
            ps.setString(4, nullIfBlank(c.getNit()));
            ps.setString(5, nullIfBlank(c.getTelefono()));
            ps.setString(6, nullIfBlank(c.getEmail()));
            ps.setBoolean(7, c.isActivo());
            ps.setString(8, c.getEstado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) c.setId(keys.getInt(1)); }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = """
            UPDATE dbo.Clientes
            SET Codigo=?, Nombres=?, Apellidos=?, NIT=?, Telefono=?, Email=?, IsActive=?, Estado=?, BloqueadoHastaUtc=?
            WHERE Id=?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidos());
            ps.setString(4, nullIfBlank(c.getNit()));
            ps.setString(5, nullIfBlank(c.getTelefono()));
            ps.setString(6, nullIfBlank(c.getEmail()));
            ps.setBoolean(7, c.isActivo());
            ps.setString(8, c.getEstado());
            if (c.getBloqueadoHastaUtc() == null) ps.setNull(9, Types.TIMESTAMP);
            else ps.setTimestamp(9, Timestamp.valueOf(c.getBloqueadoHastaUtc()));
            ps.setInt(10, c.getId());
            ps.executeUpdate();
        }
    }

    /** Baja lógica -> IsActive=0 */
    public void eliminarLogico(int id) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE dbo.Clientes SET IsActive=0 WHERE Id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ---------- Reglas rápidas ----------

    public void bloquear(int idCliente, LocalDateTime hastaUtc) throws SQLException {
        String sql = "UPDATE dbo.Clientes SET Estado='BLOQUEADO', BloqueadoHastaUtc=? WHERE Id=?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (hastaUtc == null) ps.setNull(1, Types.TIMESTAMP);
            else ps.setTimestamp(1, Timestamp.valueOf(hastaUtc));
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    public void desbloquear(int idCliente) throws SQLException {
        String sql = "UPDATE dbo.Clientes SET Estado='ACTIVO', BloqueadoHastaUtc=NULL WHERE Id=?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.executeUpdate();
        }
    }

    // ---------- Helpers ----------

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("Id"));
        c.setCodigo(rs.getString("Codigo"));
        c.setNombres(rs.getString("Nombres"));
        c.setApellidos(rs.getString("Apellidos"));
        c.setNit(rs.getString("NIT"));
        c.setTelefono(rs.getString("Telefono"));
        c.setEmail(rs.getString("Email"));
        c.setActivo(rs.getBoolean("IsActive"));
        c.setEstado(rs.getString("Estado"));
        Timestamp fr = rs.getTimestamp("FechaRegistroUtc");
        if (fr != null) c.setFechaRegistroUtc(fr.toLocalDateTime());
        Timestamp bh = rs.getTimestamp("BloqueadoHastaUtc");
        if (bh != null) c.setBloqueadoHastaUtc(bh.toLocalDateTime());
        return c;
    }

    private String nullIfBlank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
