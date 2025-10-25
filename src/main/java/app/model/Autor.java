package app.model;

/** Entidad Autor (dbo.Autores) */
public class Autor {

    private int id;
    private String nombre;
    private String pais;     // opcional en tu BD
    private boolean activo;  // mapea a IsActive

    public Autor() { this.activo = true; }

    public Autor(int id, String nombre, String pais, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
        this.activo = activo;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + (pais != null && !pais.isBlank() ? " - " + pais : "");
    }
}
