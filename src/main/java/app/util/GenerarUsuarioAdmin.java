package app.util;

import app.db.Conexion;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * Clase utilitaria para generar un usuario administrador inicial.
 * Crea el usuario 'admin' con contraseña hasheada (BCrypt) en la tabla dbo.Usuarios
 * y asigna el rol 'ADMIN' si existe en dbo.Roles.
 */
public class GenerarUsuarioAdmin {

    public static void main(String[] args) {
        String username = "admin";
        String passwordPlano = "Admin123!";  // 🔐 cambia esto antes de ejecutar
        String nombreCompleto = "Administrador General";
        String email = "admin@biblioteca.com";
        String telefono = "5555-5555";

        try (Connection conn = Conexion.getConnection()) {

            // 1️⃣ Generar el hash BCrypt
            String hash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt(12));
            System.out.println("Hash generado: " + hash);

            // 2️⃣ Verificar si ya existe el usuario
            int idUsuario;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT Id FROM dbo.Usuarios WHERE Username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idUsuario = rs.getInt(1);
                        System.out.println("Usuario existente: actualizando contraseña...");
                        try (PreparedStatement upd = conn.prepareStatement(
                                "UPDATE dbo.Usuarios SET PasswordHash=?, IsActive=1 WHERE Id=?")) {
                            upd.setString(1, hash);
                            upd.setInt(2, idUsuario);
                            upd.executeUpdate();
                        }
                    } else {
                        // 3️⃣ Insertar nuevo usuario
                        try (PreparedStatement ins = conn.prepareStatement(
                                "INSERT INTO dbo.Usuarios (Username, PasswordHash, NombreCompleto, Email, Telefono, IsActive) " +
                                        "VALUES (?, ?, ?, ?, ?, 1)", Statement.RETURN_GENERATED_KEYS)) {
                            ins.setString(1, username);
                            ins.setString(2, hash);
                            ins.setString(3, nombreCompleto);
                            ins.setString(4, email);
                            ins.setString(5, telefono);
                            ins.executeUpdate();
                            try (ResultSet keys = ins.getGeneratedKeys()) {
                                keys.next();
                                idUsuario = keys.getInt(1);
                            }
                            System.out.println("✅ Usuario admin creado con ID: " + idUsuario);
                        }
                    }
                }
            }

            // 4️⃣ Asignar rol ADMIN (si existe)
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT Id FROM dbo.Roles WHERE Nombre = 'ADMIN'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int idRol = rs.getInt(1);

                        // Evitar duplicado
                        try (PreparedStatement chk = conn.prepareStatement(
                                "SELECT 1 FROM dbo.UsuarioRoles WHERE IdUsuario=? AND IdRol=?")) {
                            chk.setInt(1, idUsuario);
                            chk.setInt(2, idRol);
                            try (ResultSet rsChk = chk.executeQuery()) {
                                if (!rsChk.next()) {
                                    try (PreparedStatement ins = conn.prepareStatement(
                                            "INSERT INTO dbo.UsuarioRoles (IdUsuario, IdRol) VALUES (?, ?)")) {
                                        ins.setInt(1, idUsuario);
                                        ins.setInt(2, idRol);
                                        ins.executeUpdate();
                                    }
                                    System.out.println("🧩 Rol ADMIN asignado correctamente.");
                                } else {
                                    System.out.println("ℹ️ El usuario ya tenía asignado el rol ADMIN.");
                                }
                            }
                        }
                    } else {
                        System.out.println("⚠️ Rol 'ADMIN' no existe en dbo.Roles. Debes crearlo manualmente.");
                    }
                }
            }

            System.out.println("🎯 Operación finalizada correctamente.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Error SQL: " + e.getMessage());
        }
    }
}
