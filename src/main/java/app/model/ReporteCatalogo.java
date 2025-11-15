package app.model;

/**
 * Modelo (POJO) para el reporte de Catálogo/Inventario.
 */

public class ReporteCatalogo {

    private String titulo;
    private String autores;
    private String categorias;
    private String isbn;
    private String editorial;
    private int anio;
    private int totalCopias;
    private int copiasDisponibles;

    // --- Getters y Setters ---

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutores() { return autores; }
    public void setAutores(String autores) { this.autores = autores; }

    public String getCategorias() { return categorias; }
    public void setCategorias(String categorias) { this.categorias = categorias; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getTotalCopias() { return totalCopias; }
    public void setTotalCopias(int totalCopias) { this.totalCopias = totalCopias; }

    public int getCopiasDisponibles() { return copiasDisponibles; }
    public void setCopiasDisponibles(int copiasDisponibles) { this.copiasDisponibles = copiasDisponibles; }
}