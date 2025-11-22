package app.model;

import java.util.Date;

/**
 * Modelo para la entidad Reserva.
 * Mapea la tabla dbo.Reservas:
 *  - Id         -> idReserva
 *  - IdCliente  -> idCliente
 *  - IdLibro    -> idLibro
 *  - IdCopia    -> idCopia (opcional)
 *  - CreadoUtc  -> fechaCreado
 *  - ExpiraUtc  -> fechaExpira
 *  - Estado     -> estado
 *  - Notas      -> notas
 *
 * Campo posicionCola se calcula en los SELECT (no existe físicamente).
 */
public class Reserva {

    private int idReserva;
    private int idCliente;
    private int idLibro;
    private Integer idCopia;       // puede ser null
    private Date fechaCreado;      // CreadoUtc
    private Date fechaExpira;      // ExpiraUtc (puede ser null)
    private String estado;         // "PENDIENTE", "COMPLETADA", "EXPIRADA", etc.
    private String notas;          // nvarchar(200)

    // Campo calculado (no está en la tabla)
    private int posicionCola;

    public Reserva() {}

    // --- Getters y Setters ---

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

    public Integer getIdCopia() {
        return idCopia;
    }

    public void setIdCopia(Integer idCopia) {
        this.idCopia = idCopia;
    }

    public Date getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(Date fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public Date getFechaExpira() {
        return fechaExpira;
    }

    public void setFechaExpira(Date fechaExpira) {
        this.fechaExpira = fechaExpira;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public int getPosicionCola() {
        return posicionCola;
    }

    public void setPosicionCola(int posicionCola) {
        this.posicionCola = posicionCola;
    }
}
