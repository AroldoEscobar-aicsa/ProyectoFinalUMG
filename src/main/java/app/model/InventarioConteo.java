package app.model;

import java.time.LocalDateTime;

public class InventarioConteo {
    private int id;
    private int idUsuario;
    private String comentario;
    private LocalDateTime creadoUtc;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getCreadoUtc() { return creadoUtc; }
    public void setCreadoUtc(LocalDateTime creadoUtc) { this.creadoUtc = creadoUtc; }

    @Override
    public String toString() {
        return "Conteo #" + id + " - Usuario " + idUsuario;
    }
}
