package app.model;

public class Libro {
    private int id;
    private String titulo;
    private String isbn;
    private String edicion;
    private Integer anio;        // puede venir null en clásicos
    private String idioma;
    private Integer idEditorial; // FK
    private String editorialNombre; // para mostrar en tabla
    private Integer stockMinimo;
    private boolean activo;      // mapea a IsActive

    public Libro() { this.activo = true; this.stockMinimo = 1; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getEdicion() { return edicion; }
    public void setEdicion(String edicion) { this.edicion = edicion; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public Integer getIdEditorial() { return idEditorial; }
    public void setIdEditorial(Integer idEditorial) { this.idEditorial = idEditorial; }

    public String getEditorialNombre() { return editorialNombre; }
    public void setEditorialNombre(String editorialNombre) { this.editorialNombre = editorialNombre; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override public String toString() {
        return titulo + (editorialNombre != null ? " - " + editorialNombre : "");
    }
}
