package app.model;

public class InventarioConteoDetalle {

    private int id;
    private int idConteo;
    private int idLibro;
    private String tituloLibro;
    private Integer cantidadFisica;
    private Integer cantidadSistema;
    private Integer diferencia;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdConteo() { return idConteo; }
    public void setIdConteo(int idConteo) { this.idConteo = idConteo; }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }

    public String getTituloLibro() { return tituloLibro; }
    public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }

    public Integer getCantidadFisica() { return cantidadFisica; }
    public void setCantidadFisica(Integer cantidadFisica) { this.cantidadFisica = cantidadFisica; }

    public Integer getCantidadSistema() { return cantidadSistema; }
    public void setCantidadSistema(Integer cantidadSistema) { this.cantidadSistema = cantidadSistema; }

    public Integer getDiferencia() { return diferencia; }
    public void setDiferencia(Integer diferencia) { this.diferencia = diferencia; }
}
