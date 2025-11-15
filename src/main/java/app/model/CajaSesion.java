package app.model;

import java.time.LocalDateTime;

/**
 * Representa una sesión de caja (apertura/cierre de un cajero).
 */
public class CajaSesion {
    private int id;
    private int idUsuario;
    private LocalDateTime abiertaUtc;
    private LocalDateTime cerradaUtc;
    private String observacion;
    private String estado; // "ABIERTA" o "CERRADA"

    // Constructor
    public CajaSesion() {
        this.estado = "ABIERTA";
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getAbiertaUtc() {
        return abiertaUtc;
    }

    public void setAbiertaUtc(LocalDateTime abiertaUtc) {
        this.abiertaUtc = abiertaUtc;
    }

    public LocalDateTime getCerradaUtc() {
        return cerradaUtc;
    }

    public void setCerradaUtc(LocalDateTime cerradaUtc) {
        this.cerradaUtc = cerradaUtc;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}