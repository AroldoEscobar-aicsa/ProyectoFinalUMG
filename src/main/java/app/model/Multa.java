package app.model;

import java.time.LocalDate;

/**
 * Representa una multa generada (cuenta por cobrar).
 * En la BD se almacena en dbo.Multas:
 *  - Monto     -> montoCalculado
 *  - Estado    -> estado (PENDIENTE / PAGADA / EXONERADA)
 *  - Justificacion -> justificacionExoneracion
 *  - CreadoUtc -> fechaGeneracion (solo fecha)
 *  - PagadoUtc -> fechaLimitePago (solo fecha, si aplica)
 *
 * Campos como diasAtraso y montoPagado son de lógica de negocio.
 */
public class Multa {
    private int idMulta;
    private int idPrestamo; // El préstamo que originó la multa
    private int idCliente;  // El cliente deudor

    private LocalDate fechaGeneracion;   // CreadoUtc (solo fecha)
    private int diasAtraso;              // Calculado a partir del préstamo
    private double montoCalculado;       // Columna Monto
    private double montoPagado;          // Derivado: 0 si PENDIENTE, = montoCalculado si pagada/exonerada
    private String estado;               // "PENDIENTE", "PAGADA", "EXONERADA"
    private LocalDate fechaLimitePago;   // Usamos PagadoUtc como fecha de pago/histórico
    private String justificacionExoneracion; // Columna Justificacion

    // Campos extra solo para mostrar en UI
    private String codigoCliente;
    private String nombreCliente;   // Nombres + Apellidos
    private String tituloLibro;
    private Integer idCopia;
    private String codigoBarraCopia;

    // Constructor
    public Multa() {
        this.montoPagado = 0.0;
        this.estado = "PENDIENTE"; // coherente con la tabla y su CHECK
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

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTituloLibro() {
        return tituloLibro;
    }

    public void setTituloLibro(String tituloLibro) {
        this.tituloLibro = tituloLibro;
    }

    public Integer getIdCopia() {
        return idCopia;
    }

    public void setIdCopia(Integer idCopia) {
        this.idCopia = idCopia;
    }

    public String getCodigoBarraCopia() {
        return codigoBarraCopia;
    }

    public void setCodigoBarraCopia(String codigoBarraCopia) {
        this.codigoBarraCopia = codigoBarraCopia;
    }
}
