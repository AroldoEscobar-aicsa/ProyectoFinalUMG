package app.model;

import java.time.LocalDateTime;

/*Representa un movimiento individual en la caja diaria de un usuario.
Cubre aperturas, ingresos (cobros) y cierres (arqueo)*/

public class CajaMovimiento {
    private int idMovimiento;
    private int idUsuarioCajero; // El usuario (Financiero) que realiza el movimiento
    private LocalDateTime fechaHora;

    /**
     * Tipo de movimiento: "Apertura", "Ingreso", "Cierre"
     */
    private String tipoMovimiento;

    /**
     * Descripción del movimiento.
     * Ej: "Fondo inicial", "Pago Multa ID: 123", "Cierre de caja"
     */
    private String descripcion;

    /**
     * Monto del movimiento.
     * - Positivo para "Apertura" (fondo inicial).
     * - Positivo para "Ingreso" (cobro de multa).
     * - Cero para "Cierre".
     */
    private double monto;

    /**
     * (Opcional) Referencia a la multa que se está pagando.
     * Null si es "Apertura" o "Cierre".
     */
    private Integer idMulta;

    // --- Campos de Arqueo (usados solo en "Cierre") ---

    /**
     * (Solo para "Cierre") Monto total que el sistema calculó.
     * (Apertura + Suma de Ingresos)
     */
    private Double montoCalculadoSistema;

    /**
     * (Solo para "Cierre") Monto que el cajero contó físicamente.
     */
    private Double montoRealContado;

    /**
     * (Solo para "Cierre") Diferencia: (montoRealContado - montoCalculadoSistema)
     */
    private Double diferencia;


    // --- Getters y Setters ---

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdUsuarioCajero() {
        return idUsuarioCajero;
    }

    public void setIdUsuarioCajero(int idUsuarioCajero) {
        this.idUsuarioCajero = idUsuarioCajero;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public Double getMontoCalculadoSistema() {
        return montoCalculadoSistema;
    }

    public void setMontoCalculadoSistema(Double montoCalculadoSistema) {
        this.montoCalculadoSistema = montoCalculadoSistema;
    }

    public Double getMontoRealContado() {
        return montoRealContado;
    }

    public void setMontoRealContado(Double montoRealContado) {
        this.montoRealContado = montoRealContado;
    }

    public Double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(Double diferencia) {
        this.diferencia = diferencia;
    }
}