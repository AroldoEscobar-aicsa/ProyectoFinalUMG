package app.reportes;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Clase base abstracta para todos los reportes del sistema.
 * Define la estructura común y utilidades compartidas.
 */
public abstract class ReporteBase {

    protected static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter FORMATO_FECHA_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd");

    protected String titulo;
    protected String subtitulo;
    protected LocalDate fechaGeneracion;

    /**
     * Constructor base.
     */
    public ReporteBase(String titulo) {
        this.titulo = titulo;
        this.fechaGeneracion = LocalDate.now();
    }

    /**
     * Genera el reporte en formato PDF.
     * @param rutaDestino Ruta donde se guardará el archivo
     * @return Archivo generado
     */
    public abstract File generarPDF(String rutaDestino) throws Exception;

    /**
     * Genera el reporte en formato Excel.
     * @param rutaDestino Ruta donde se guardará el archivo
     * @return Archivo generado
     */
    public abstract File generarExcel(String rutaDestino) throws Exception;

    /**
     * Genera un nombre de archivo único para el reporte.
     */
    protected String generarNombreArchivo(String prefijo, String extension) {
        String fecha = fechaGeneracion.format(FORMATO_FECHA_ARCHIVO);
        return String.format("%s_%s.%s", prefijo, fecha, extension);
    }

    /**
     * Formatea un monto monetario con símbolo de moneda.
     */
    protected String formatearMonto(double monto) {
        return String.format("Q %.2f", monto);
    }

    /**
     * Formatea una fecha en el formato estándar del sistema.
     */
    protected String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "N/A";
    }

    // Getters y Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
}