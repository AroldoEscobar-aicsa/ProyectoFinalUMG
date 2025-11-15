package app.model;

import java.time.LocalDateTime;

/**
 * Representa un movimiento individual en una sesión de caja.
 * Mapea a la tabla CajaMovimientos de la BD.
 */
public class CajaMovimiento {
    private int idMovimiento;
    private int idCajaSesion;       // FK a CajaSesiones
    private LocalDateTime creadoUtc;

    private String tipo;            // "ENTRADA" o "SALIDA"
    private String concepto;        // Descripción del movimiento
    private double monto;

    private Integer idMulta;        // NULL si no es pago de multa

    // Constructor
    public CajaMovimiento() {
    }

    // --- Getters y Setters ---

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdCajaSesion() {
        return idCajaSesion;
    }

    public void setIdCajaSesion(int idCajaSesion) {
        this.idCajaSesion = idCajaSesion;
    }

    public LocalDateTime getCreadoUtc() {
        return creadoUtc;
    }

    public void setCreadoUtc(LocalDateTime creadoUtc) {
        this.creadoUtc = creadoUtc;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Integer getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(Integer idMulta) {
        this.idMulta = idMulta;
    }
}