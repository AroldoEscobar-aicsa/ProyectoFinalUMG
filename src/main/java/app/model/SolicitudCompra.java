package app.model;

import java.time.LocalDateTime;

public class SolicitudCompra {

    private int id;
    private int idLibro;
    private int cantidad;
    private int solicitadoPor;
    private String estado;              // PENDIENTE, APROBADA, RECHAZADA, COMPRADA
    private LocalDateTime creadoUtc;
    private Integer aprobadoPor;
    private LocalDateTime aprobadoUtc;

    // Campos de apoyo para UI
    private String tituloLibro;
    private String nombreSolicitante;
    private String nombreAprobador;

    // --- Getters/Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getSolicitadoPor() {
        return solicitadoPor;
    }

    public void setSolicitadoPor(int solicitadoPor) {
        this.solicitadoPor = solicitadoPor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoUtc() {
        return creadoUtc;
    }

    public void setCreadoUtc(LocalDateTime creadoUtc) {
        this.creadoUtc = creadoUtc;
    }

    public Integer getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(Integer aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public LocalDateTime getAprobadoUtc() {
        return aprobadoUtc;
    }

    public void setAprobadoUtc(LocalDateTime aprobadoUtc) {
        this.aprobadoUtc = aprobadoUtc;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public String getNombreAprobador() {
        return nombreAprobador;
    }

    public void setNombreAprobador(String nombreAprobador) {
        this.nombreAprobador = nombreAprobador;
    }

    @Override
    public String toString() {
        // Útil si algún día la usas en un combo
        return "Sol#" + id + " - " + tituloLibro + " x" + cantidad + " [" + estado + "]";
    }
}
