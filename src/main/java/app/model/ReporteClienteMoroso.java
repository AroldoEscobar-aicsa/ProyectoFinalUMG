package app.model;

/**
 * Modelo (POJO) para el reporte de Clientes Morosos.
 */

public class ReporteClienteMoroso {

    private String nombreCompleto;
    private String email;
    private String telefono;
    private int totalMultasPendientes;
    private double montoTotalDeuda;

    // --- Getters y Setters ---

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getTotalMultasPendientes() { return totalMultasPendientes; }
    public void setTotalMultasPendientes(int totalMultasPendientes) { this.totalMultasPendientes = totalMultasPendientes; }

    public double getMontoTotalDeuda() { return montoTotalDeuda; }
    public void setMontoTotalDeuda(double montoTotalDeuda) { this.montoTotalDeuda = montoTotalDeuda; }
}