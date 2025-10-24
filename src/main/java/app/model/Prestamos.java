package app.model;

import java.util.Date;

/**
 * Modelo para la entidad Prestamos.
 * Representa el registro de un préstamo de una CopiaLibro a un Cliente.
 * Basado en los requisitos del Módulo D (Préstamos y Devoluciones).
 */
public class Prestamos {

    private int idPrestamo;
    private int idCopiaLibro; // El ID de la copia específica (no del libro) [cite: 30]
    private int idCliente; // El ID del cliente que presta

    // Es útil saber qué bibliotecario registró el préstamo (para auditoría [cite: 50])
    private int idUsuarioBibliotecario;

    private Date fechaPrestamo; // Fecha y hora en que se realiza el préstamo
    private Date fechaVencimiento; // Fecha calculada de devolución [cite: 31]
    private Date fechaDevolucion; // Fecha real (null si no ha sido devuelto)

    private String estado; // Ej: "Activo", "Devuelto", "Vencido"

    // Constructor vacío
    public Prestamos() {
    }

    // Constructor completo (para leer datos de la BD)
    public Prestamos(int idPrestamo, int idCopiaLibro, int idCliente, int idUsuarioBibliotecario,
                     Date fechaPrestamo, Date fechaVencimiento, Date fechaDevolucion, String estado) {
        this.idPrestamo = idPrestamo;
        this.idCopiaLibro = idCopiaLibro;
        this.idCliente = idCliente;
        this.idUsuarioBibliotecario = idUsuarioBibliotecario;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
    }

    // --- Getters y Setters ---
    // (Asegúrate de generar todos los getters y setters para los atributos)

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getIdCopiaLibro() {
        return idCopiaLibro;
    }

    public void setIdCopiaLibro(int idCopiaLibro) {
        this.idCopiaLibro = idCopiaLibro;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdUsuarioBibliotecario() {
        return idUsuarioBibliotecario;
    }

    public void setIdUsuarioBibliotecario(int idUsuarioBibliotecario) {
        this.idUsuarioBibliotecario = idUsuarioBibliotecario;
    }

    public Date getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(Date fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Date getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(Date fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}