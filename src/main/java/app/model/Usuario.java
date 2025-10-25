package app.model;

import java.time.LocalDateTime;

public class Usuario {
    private int id;
    private String username;
    private String passwordHash;     // hash BCrypt en BD
    private Integer rolPrincipalId;  // primer rol (opcional)
    private String rolPrincipal;     // nombre del rol
    private String nombreCompleto;
    private String email;
    private String telefono;
    private boolean activo;          // mapea a IsActive
    private int intentosFallidos;
    private LocalDateTime ultimoLoginUtc;

    public Usuario() { this.activo = true; }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Integer getRolPrincipalId() { return rolPrincipalId; }
    public void setRolPrincipalId(Integer rolPrincipalId) { this.rolPrincipalId = rolPrincipalId; }
    public String getRolPrincipal() { return rolPrincipal; }
    public void setRolPrincipal(String rolPrincipal) { this.rolPrincipal = rolPrincipal; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public LocalDateTime getUltimoLoginUtc() { return ultimoLoginUtc; }
    public void setUltimoLoginUtc(LocalDateTime ultimoLoginUtc) { this.ultimoLoginUtc = ultimoLoginUtc; }

    @Override public String toString() {
        return nombreCompleto + " (" + username + ") - " + (rolPrincipal != null ? rolPrincipal : "SIN ROL");
    }
}
