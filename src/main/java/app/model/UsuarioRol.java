package app.model;

public class UsuarioRol {
    private int idUsuario;
    private int idRol;

    public UsuarioRol() {}
    public UsuarioRol(int idUsuario, int idRol) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
}
