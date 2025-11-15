package app.model;

/**
 * Este es un modelo (POJO) especial, usado solo para
 * los resultados del reporte de libros más prestados.
 */

public class ReporteTopLibro {

    private String titulo;
    private String autores;
    private int totalPrestamos;

    // Constructor vacío
    public ReporteTopLibro() {}

    // --- Getters y Setters ---

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutores() {
        return autores;
    }

    public void setAutores(String autores) {
        this.autores = autores;
    }

    public int getTotalPrestamos() {
        return totalPrestamos;
    }

    public void setTotalPrestamos(int totalPrestamos) {
        this.totalPrestamos = totalPrestamos;
    }
}