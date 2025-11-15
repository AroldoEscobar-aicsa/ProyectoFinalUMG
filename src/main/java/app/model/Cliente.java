package app.model;

/** Entidad Cliente (dbo.Clientes) */
public class Cliente {

    private int id;
    private String codigo;     // carnet/matrícula (único)
    private String nombres;
    private String apellidos;
    private String nit;        // opcional
    private String telefono;   // opcional
    private String email;      // opcional/obligatorio según tu BD
    private boolean activo;    // mapea a IsActive
    private String estado;     // 'ACTIVO' | 'BLOQUEADO'
    private double moraAcumulada;

    public Cliente() {
        this.activo = true;
        this.estado = "ACTIVO";
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getMoraAcumulada() {return moraAcumulada;}

    public void setMoraAcumulada(double moraAcumulada) {this.moraAcumulada = moraAcumulada;}

    @Override public String toString() {
        return codigo + " - " + apellidos + ", " + nombres + (activo ? "" : " (INACTIVO)");
    }
}
