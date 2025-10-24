package app.model;

import java.util.Date;

public class Adquisiciones {
    private int idAdquisicion;
    private String codigoCompra;
    private String proveedor;
    private String categoria;
    private String descripcion;
    private int cantidad;
    private double costoUnitario;
    private Date fechaSolicitud;
    private Date fechaAprobacion;
    private String estado; // "Solicitado", "Aprobado", "Rechazado"
    private boolean eliminado;

    public Adquisiciones() {}

    public Adquisiciones(int idAdquisicion, String codigoCompra, String proveedor, String categoria,
                         String descripcion, int cantidad, double costoUnitario,
                         Date fechaSolicitud, Date fechaAprobacion, String estado, boolean eliminado) {
        this.idAdquisicion = idAdquisicion;
        this.codigoCompra = codigoCompra;
        this.proveedor = proveedor;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaAprobacion = fechaAprobacion;
        this.estado = estado;
        this.eliminado = eliminado;
    }

    // --- Getters y Setters ---
    public int getIdAdquisicion() { return idAdquisicion; }
    public void setIdAdquisicion(int idAdquisicion) { this.idAdquisicion = idAdquisicion; }

    public String getCodigoCompra() { return codigoCompra; }
    public void setCodigoCompra(String codigoCompra) { this.codigoCompra = codigoCompra; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }

    public Date getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Date fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Date getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(Date fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }

    // --- Método de utilidad ---
    public double getTotalCompra() {
        return cantidad * costoUnitario;
    }

    @Override
    public String toString() {
        return codigoCompra + " - " + descripcion + " (" + estado + ")";
    }
}
