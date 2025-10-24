package app.model;

/**
 * Clase Usuario
 * Representa a un usuario del sistema con su información básica.
 * Compatible con SQL Server y estructura CRUD general del proyecto.
 */
public class Usuario {

    private int id;
    private String username;
    private String passwordHash;
    private int idRol;
    private String nombreCompleto;
    private String email;
    private boolean estado;

    // ====== Constructores ======
    public Usuario() {
        this.estado = true; // Por defecto activo
    }

    public Usuario(int id, String username, String passwordHash, int idRol,
                   String nombreCompleto, String email, boolean estado) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.idRol = idRol;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.estado = estado;
    }

    // ====== Getters y Setters ======
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    // ====== Representación en texto ======
    @Override
    public String toString() {
        return nombreCompleto + " (" + username + ")";
    }
}
