package app.dao;

import app.db.Conexion;
import app.model.Rol;
import app.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // ----------- Roles -------------
    public List<Rol> listarRoles() throws SQLException {
        String sql = "SELECT Id, Nombre FROM dbo.Roles WHERE IsActive=1 ORDER BY Nombre";
        List<Rol> roles = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) roles.add(new Rol(rs.getInt("Id"), rs.getString("Nombre")));
        }
        return roles;
    }

    public Integer getRolIdPorNombre(String nombre) throws SQLException {
        String sql = "SELECT Id FROM dbo.Roles WHERE Nombre = ?";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public void asignarRol(int idUsuario, int idRol) throws SQLException {
        String q = "IF NOT EXISTS (SELECT 1 FROM dbo.UsuarioRoles WHERE IdUsuario=? AND IdRol=?) " +
                "INSERT INTO dbo.UsuarioRoles(IdUsuario, IdRol) VALUES(?, ?)";
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(q)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idRol);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idRol);
            ps.executeUpdate();
        }
    }

    // ----------- CRUD Usuarios -------------
    public List<Usuario> listarActivos() throws Exception {
        String sql = """
            SELECT u.Id, u.Username, u.NombreCompleto, u.Email, u.Telefono, u.IsActive,
                   u.IntentosFallidos, u.UltimoLoginUtc,
                   ur.IdRol AS RolId, r.Nombre AS RolNombre
            FROM dbo.Usuarios u
            OUTER APPLY (SELECT TOP 1 IdRol FROM dbo.UsuarioRoles WHERE IdUsuario=u.Id ORDER BY IdRol) ur
            LEFT JOIN dbo.Roles r ON r.Id = ur.IdRol
            WHERE u.IsActive = 1
            ORDER BY u.Username
        """;
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapUsuario(rs, false));
        }
        return lista;
    }

    public List<Usuario> listarTodos() throws Exception {
        String sql = """
            SELECT u.Id, u.Username, u.NombreCompleto, u.Email, u.Telefono, u.IsActive,
                   ur.IdRol AS RolId, r.Nombre AS RolNombre
            FROM dbo.Usuarios u
            OUTER APPLY (SELECT TOP 1 IdRol FROM dbo.UsuarioRoles WHERE IdUsuario=u.Id ORDER BY IdRol) ur
            LEFT JOIN dbo.Roles r ON r.Id = ur.IdRol
            ORDER BY u.Username
        """;
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapUsuario(rs, false));
        }
        return lista;
    }

    public void crear(Usuario usuario, String passwordPlano) throws Exception {
        String sql = """
            INSERT INTO dbo.Usuarios(Username, PasswordHash, NombreCompleto, Email, Telefono, IsActive)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        String hash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(12));

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, hash);
            ps.setString(3, usuario.getNombreCompleto());
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getTelefono());
            ps.setBoolean(6, usuario.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) usuario.setId(keys.getInt(1));
            }
        }
        if (usuario.getRolPrincipalId() != null) asignarRol(usuario.getId(), usuario.getRolPrincipalId());
    }

    public void actualizar(Usuario u) throws Exception {
        String sql = """
            UPDATE dbo.Usuarios
            SET Username=?, NombreCompleto=?, Email=?, Telefono=?, IsActive=?
            WHERE Id=?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getNombreCompleto());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelefono());
            ps.setBoolean(5, u.isActivo());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
        if (u.getRolPrincipalId() != null) asignarRol(u.getId(), u.getRolPrincipalId());
    }

    public void actualizarPasswordPlano(int idUsuario, String passwordPlano) throws Exception {
        String hash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(12));
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE dbo.Usuarios SET PasswordHash=? WHERE Id=?")) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void eliminarLogico(int id) throws Exception {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE dbo.Usuarios SET IsActive=0 WHERE Id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Usuario buscarPorId(int id) throws Exception {
        String sql = """
            SELECT u.Id, u.Username, u.PasswordHash, u.NombreCompleto, u.Email, u.Telefono, u.IsActive,
                   u.IntentosFallidos, u.UltimoLoginUtc,
                   ur.IdRol AS RolId, r.Nombre AS RolNombre
            FROM dbo.Usuarios u
            OUTER APPLY (SELECT TOP 1 IdRol FROM dbo.UsuarioRoles WHERE IdUsuario=u.Id ORDER BY IdRol) ur
            LEFT JOIN dbo.Roles r ON r.Id = ur.IdRol
            WHERE u.Id=?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUsuario(rs, true);
            }
        }
        return null;
    }

    // ----------- Login -------------
    public Usuario autenticar(String username, String passwordPlano) throws Exception {
        String sql = """
            SELECT u.Id, u.Username, u.PasswordHash, u.NombreCompleto, u.Email, u.Telefono, u.IsActive,
                   u.IntentosFallidos, u.UltimoLoginUtc,
                   ur.IdRol AS RolId, r.Nombre AS RolNombre
            FROM dbo.Usuarios u
            OUTER APPLY (SELECT TOP 1 IdRol FROM dbo.UsuarioRoles WHERE IdUsuario=u.Id ORDER BY IdRol) ur
            LEFT JOIN dbo.Roles r ON r.Id = ur.IdRol
            WHERE u.Username=?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Usuario u = mapUsuario(rs, true);
                if (!u.isActivo()) return null;

                String hash = u.getPasswordHash();
                boolean ok = (hash != null) && BCrypt.checkpw(passwordPlano, hash);
                if (!ok) {
                    incrementarIntentosFallidos(u.getId());
                    return null;
                }
                resetearIntentosYUltimoLogin(u.getId());
                u.setPasswordHash(null);
                return u;
            }
        }
    }

    private void incrementarIntentosFallidos(int idUsuario) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE dbo.Usuarios SET IntentosFallidos = ISNULL(IntentosFallidos,0) + 1 WHERE Id=?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private void resetearIntentosYUltimoLogin(int idUsuario) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE dbo.Usuarios SET IntentosFallidos=0, UltimoLoginUtc=SYSUTCDATETIME() WHERE Id=?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    // ----------- Mapeo -------------
    private Usuario mapUsuario(ResultSet rs, boolean incluirHash) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("Id"));
        u.setUsername(rs.getString("Username"));
        if (incluirHash) u.setPasswordHash(rs.getString("PasswordHash"));
        u.setNombreCompleto(rs.getString("NombreCompleto"));
        u.setEmail(rs.getString("Email"));
        u.setTelefono(rs.getString("Telefono"));
        u.setActivo(rs.getBoolean("IsActive"));
        u.setIntentosFallidos(rs.getInt("IntentosFallidos"));
        Timestamp t = rs.getTimestamp("UltimoLoginUtc");
        if (t != null) u.setUltimoLoginUtc(t.toLocalDateTime());
        Object rolId = rs.getObject("RolId");
        if (rolId != null) u.setRolPrincipalId(rs.getInt("RolId"));
        u.setRolPrincipal(rs.getString("RolNombre"));
        return u;
    }
}
