    package app.dao;

    import app.db.Conexion;
    import app.model.Usuario;

    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * UsuarioDAO - Acceso a datos para la entidad Usuario
     * Usa eliminación lógica (campo estado).
     */
    public class UsuarioDAO {

        // ====== Listar usuarios activos ======
        public List<Usuario> listarActivos() throws Exception {
            List<Usuario> lista = new ArrayList<>();
            String sql = "SELECT id, username, nombreCompleto, email, idRol, estado FROM Usuario WHERE estado = 1 ORDER BY username";

            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setNombreCompleto(rs.getString("nombreCompleto"));
                    u.setEmail(rs.getString("email"));
                    u.setIdRol(rs.getInt("idRol"));
                    u.setEstado(rs.getBoolean("estado"));
                    lista.add(u);
                }
            }
            return lista;
        }

        // ====== Crear nuevo usuario ======
        public void crear(Usuario usuario) throws Exception {
            String sql = "INSERT INTO Usuario(username, password_hash, idRol, nombreCompleto, email, estado) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, usuario.getUsername());
                ps.setString(2, usuario.getPasswordHash());
                ps.setInt(3, usuario.getIdRol());
                ps.setString(4, usuario.getNombreCompleto());
                ps.setString(5, usuario.getEmail());
                ps.setBoolean(6, usuario.isEstado());
                ps.executeUpdate();
            }
        }

        // ====== Actualizar usuario ======
        public void actualizar(Usuario usuario) throws Exception {
            String sql = "UPDATE Usuario SET username = ?, nombreCompleto = ?, email = ?, idRol = ?, estado = ? WHERE id = ?";
            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, usuario.getUsername());
                ps.setString(2, usuario.getNombreCompleto());
                ps.setString(3, usuario.getEmail());
                ps.setInt(4, usuario.getIdRol());
                ps.setBoolean(5, usuario.isEstado());
                ps.setInt(6, usuario.getId());
                ps.executeUpdate();
            }
        }

        // ====== Eliminar lógico (estado = 0) ======
        public void eliminarLogico(int id) throws Exception {
            String sql = "UPDATE Usuario SET estado = 0 WHERE id = ?";
            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }

        // ====== Buscar usuario por ID ======
        public Usuario buscarPorId(int id) throws Exception {
            Usuario u = null;
            String sql = "SELECT id, username, nombreCompleto, email, idRol, estado FROM Usuario WHERE id = ?";
            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setNombreCompleto(rs.getString("nombreCompleto"));
                        u.setEmail(rs.getString("email"));
                        u.setIdRol(rs.getInt("idRol"));
                        u.setEstado(rs.getBoolean("estado"));
                    }
                }
            }
            return u;
        }

        // ====== Autenticación (login) ======
        public Usuario autenticar(String username, String passwordHash) throws Exception {
            Usuario u = null;
            String sql = "SELECT id, username, nombreCompleto, email, idRol, estado " +
                    "FROM Usuario WHERE username = ? AND password_hash = ? AND estado = 1";

            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setNombreCompleto(rs.getString("nombreCompleto"));
                        u.setEmail(rs.getString("email"));
                        u.setIdRol(rs.getInt("idRol"));
                        u.setEstado(rs.getBoolean("estado"));
                    }
                }
            }
            return u;
        }

        // ====== Listar todos los usuarios ======
        public List<Usuario> listarTodos() throws Exception {
            List<Usuario> lista = new ArrayList<>();
            String sql = "SELECT id, username, nombreCompleto, email, idRol, estado FROM Usuario ORDER BY username";

            try (Connection cn = Conexion.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getInt("id"),
                            rs.getString("username"),
                            null, // No devolvemos el hash aquí
                            rs.getInt("idRol"),
                            rs.getString("nombreCompleto"),
                            rs.getString("email"),
                            rs.getBoolean("estado")
                    );
                    lista.add(u);
                }
            }
            return lista;
        }
    }
