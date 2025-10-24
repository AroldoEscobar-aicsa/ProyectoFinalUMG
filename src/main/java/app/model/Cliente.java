package app.model;

import java.util.Date;

/**
 * Modelo para la entidad Cliente (Usuario Lector).
 * Basado en los requisitos del Módulo C (Fuentes 26, 27, 69).
 */
public class Cliente {

    private int idCliente;
    private String nombres;
    private String apellidos;
    private String nit; // Opcional
    private String telefono; // Opcional
    private String email;
    private String estado; // "Activo" o "Bloqueado"
    private Date fechaRegistro;
    private boolean eliminado; // Para eliminación lógica

    // Constructor vacío (necesario para frameworks y librerías)
    public Cliente() {
    }

    // Constructor completo (útil para leer datos de la BD)
    public Cliente(int idCliente, String nombres, String apellidos, String nit, String telefono, String email, String estado, Date fechaRegistro, boolean eliminado) {
        this.idCliente = idCliente;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.nit = nit;
        this.telefono = telefono;
        this.email = email;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.eliminado = eliminado;
    }

    // --- Getters y Setters ---
    // (Se deben generar todos los getters y setters para los atributos)

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}