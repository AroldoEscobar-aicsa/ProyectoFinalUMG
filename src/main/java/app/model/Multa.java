package app.model;

import java.time.LocalDate;
 /* Representa una multa generada (una cuenta por cobrar).
 El cálculo del monto se basa en reglas de negocio (ParametroDAO)
 pero se almacena aquí una vez generado.*/

public class Multa {
    private int idMulta;
    private int idPrestamo; // El préstamo que originó la multa
    private int idCliente;  // El cliente deudor

    private LocalDate fechaGeneracion;
    private int diasAtraso;
    private double montoCalculado; // Monto total según las reglas (tarifa * días, con tope)
    private double montoPagado;    // Suma de todos los pagos realizados
    private String estado; // "Pendiente", "Pagada", "Exonerada"
    private LocalDate fechaLimitePago;
    private String justificacionExoneracion; // Requerido si estado="Exonerada"

    // Constructor
    public Multa() {
        this.montoPagado = 0.0;
        this.estado = "Pendiente";
    }

    // --- Getters y Setters ---

    public int getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(int idMulta) {
        this.idMulta = idMulta;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public double getMontoCalculado() {
        return montoCalculado;
    }

    public void setMontoCalculado(double montoCalculado) {
        this.montoCalculado = montoCalculado;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(LocalDate fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    public String getJustificacionExoneracion() {
        return justificacionExoneracion;
    }

    public void setJustificacionExoneracion(String justificacionExoneracion) {
        this.justificacionExoneracion = justificacionExoneracion;
    }
}