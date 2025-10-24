package model;

import java.util.Date;

/**
 * Modelo para la entidad Reserva.
 * Basado en los requisitos del Módulo E y Modelo de Datos (Fuente 85).
 */
public class Reserva {

    private int idReserva;
    private int idCliente;
    private int idLibro;
    private Date fechaReserva;
    private String estado; // "Pendiente", "Expirada", "Completada" [cite: 85]
    private int posicionCola; // [cite: 85]

    // Constructor vacío
    public Reserva() {
    }

    // Constructor completo
    public Reserva(int idReserva, int idCliente, int idLibro, Date fechaReserva, String estado, int posicionCola) {
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        this.idLibro = idLibro;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.posicionCola = posicionCola;
    }

    // --- Getters y Setters ---
    // (Se deben generar todos los getters y setters para los atributos)

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public Date getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Date fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getPosicionCola() {
        return posicionCola;
    }

    public void setPosicionCola(int posicionCola) {
        this.posicionCola = posicionCola;
    }
}