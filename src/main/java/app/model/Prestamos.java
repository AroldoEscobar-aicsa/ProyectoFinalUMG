package app.model;

import java.time.LocalDateTime;

public class Prestamos {
    private int id;
    private int idCliente;
    private String codigoCliente;
    private String nombreCliente;

    private int idCopia;
    private String codigoBarra;
    private int idLibro;
    private String titulo;

    private LocalDateTime fechaPrestamoUtc;
    private LocalDateTime fechaVencimientoUtc;
    private LocalDateTime fechaDevolucionUtc;
    private int renovaciones;
    private String estado; // ACTIVO, ATRASADO, CERRADO, CANCELADO
    private double multaCalculada;

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public int getIdCopia() { return idCopia; }
    public void setIdCopia(int idCopia) { this.idCopia = idCopia; }

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDateTime getFechaPrestamoUtc() { return fechaPrestamoUtc; }
    public void setFechaPrestamoUtc(LocalDateTime fechaPrestamoUtc) { this.fechaPrestamoUtc = fechaPrestamoUtc; }

    public LocalDateTime getFechaVencimientoUtc() { return fechaVencimientoUtc; }
    public void setFechaVencimientoUtc(LocalDateTime fechaVencimientoUtc) { this.fechaVencimientoUtc = fechaVencimientoUtc; }

    public LocalDateTime getFechaDevolucionUtc() { return fechaDevolucionUtc; }
    public void setFechaDevolucionUtc(LocalDateTime fechaDevolucionUtc) { this.fechaDevolucionUtc = fechaDevolucionUtc; }

    public int getRenovaciones() { return renovaciones; }
    public void setRenovaciones(int renovaciones) { this.renovaciones = renovaciones; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getMultaCalculada() { return multaCalculada; }
    public void setMultaCalculada(double multaCalculada) { this.multaCalculada = multaCalculada; }
}
