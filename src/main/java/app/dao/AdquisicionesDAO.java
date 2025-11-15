package app.dao;

import app.db.Conexion;
import app.model.Proveedores;
import app.model.SolicitudCompra;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdquisicionesDAO {

    // ------------------ PROVEEDORES ------------------

    public List<Proveedores> listarProveedoresActivos() throws SQLException {
        String sql = """
            SELECT Id, Nombre, NIT, Telefono, Email, IsActive
            FROM dbo.Proveedores
            WHERE IsActive = 1
            ORDER BY Nombre
            """;

        List<Proveedores> lista = new ArrayList<>();

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Proveedores p = new Proveedores();
                p.setId(rs.getInt("Id"));
                p.setNombre(rs.getString("Nombre"));
                p.setNit(rs.getString("NIT"));
                p.setTelefono(rs.getString("Telefono"));
                p.setEmail(rs.getString("Email"));
                p.setActive(rs.getBoolean("IsActive"));
                lista.add(p);
            }
        }
        return lista;
    }

    // ------------------ SOLICITUDES DE COMPRA ------------------

    /**
     * Crea una nueva solicitud de compra:
     * INSERT INTO SolicitudesCompra(IdLibro, Cantidad, SolicitadoPor)
     * Estado queda en 'PENDIENTE' por defecto.
     */
    public int crearSolicitudCompra(int idLibro, int cantidad, int idUsuarioSolicita) throws SQLException {
        String sql = """
            INSERT INTO dbo.SolicitudesCompra(IdLibro, Cantidad, SolicitadoPor)
            VALUES (?, ?, ?)
            """;

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idLibro);
            ps.setInt(2, cantidad);
            ps.setInt(3, idUsuarioSolicita);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo generar Id de SolicitudCompra.");
    }

    /**
     * Lista todas las solicitudes de compra con información de libro y usuarios.
     * (PENDIENTE/APROBADA/RECHAZADA/COMPRADA)
     */
    public List<SolicitudCompra> listarSolicitudesCompra() throws SQLException {
        String sql = """
            SELECT 
                s.Id,
                s.IdLibro,
                s.Cantidad,
                s.SolicitadoPor,
                s.Estado,
                s.CreadoUtc,
                s.AprobadoPor,
                s.AprobadoUtc,
                l.Titulo AS TituloLibro,
                uSol.NombreCompleto AS NombreSolicitante,
                uApr.NombreCompleto AS NombreAprobador
            FROM dbo.SolicitudesCompra s
            JOIN dbo.Libros   l    ON l.Id   = s.IdLibro
            JOIN dbo.Usuarios uSol ON uSol.Id = s.SolicitadoPor
            LEFT JOIN dbo.Usuarios uApr ON uApr.Id = s.AprobadoPor
            ORDER BY s.CreadoUtc DESC
            """;

        List<SolicitudCompra> lista = new ArrayList<>();

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SolicitudCompra sc = new SolicitudCompra();
                sc.setId(rs.getInt("Id"));
                sc.setIdLibro(rs.getInt("IdLibro"));
                sc.setCantidad(rs.getInt("Cantidad"));
                sc.setSolicitadoPor(rs.getInt("SolicitadoPor"));
                sc.setEstado(rs.getString("Estado"));

                Timestamp creado = rs.getTimestamp("CreadoUtc");
                if (creado != null) {
                    sc.setCreadoUtc(creado.toLocalDateTime());
                }

                int aprobadoPor = rs.getInt("AprobadoPor");
                if (!rs.wasNull()) {
                    sc.setAprobadoPor(aprobadoPor);
                }

                Timestamp aprobadoUtc = rs.getTimestamp("AprobadoUtc");
                if (aprobadoUtc != null) {
                    sc.setAprobadoUtc(aprobadoUtc.toLocalDateTime());
                }

                sc.setTituloLibro(rs.getString("TituloLibro"));
                sc.setNombreSolicitante(rs.getString("NombreSolicitante"));
                sc.setNombreAprobador(rs.getString("NombreAprobador"));

                lista.add(sc);
            }
        }
        return lista;
    }

    /**
     * APRUEBA una solicitud de compra (ADMIN/BIBLIOT con permiso ADQ_APROBAR).
     * Setea Estado='APROBADA', AprobadoPor=@idUsuarioAprueba, AprobadoUtc=SYSUTCDATETIME().
     */
    public boolean aprobarSolicitudCompra(int idSolicitud, int idUsuarioAprueba) throws SQLException {
        String sql = """
            UPDATE dbo.SolicitudesCompra
            SET Estado='APROBADA',
                AprobadoPor = ?,
                AprobadoUtc = SYSUTCDATETIME()
            WHERE Id = ?
              AND Estado = 'PENDIENTE'
            """;

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuarioAprueba);
            ps.setInt(2, idSolicitud);

            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * (Opcional) Rechazar solicitud de compra.
     */
    public boolean rechazarSolicitudCompra(int idSolicitud, int idUsuarioAprueba) throws SQLException {
        String sql = """
            UPDATE dbo.SolicitudesCompra
            SET Estado='RECHAZADA',
                AprobadoPor = ?,
                AprobadoUtc = SYSUTCDATETIME()
            WHERE Id = ?
              AND Estado = 'PENDIENTE'
            """;

        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuarioAprueba);
            ps.setInt(2, idSolicitud);

            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    // ------------------ REGISTRO DE ADQUISICIÓN ------------------

    /**
     * Registra la adquisición llamando al SP dbo.sp_Adquisicion_Registrar
     * que:
     *  - Verifica que la solicitud esté APROBADA
     *  - Inserta en Adquisiciones
     *  - Crea Copias físicas (aumenta stock)
     *  - Marca SolicitudCompra.Estado = 'COMPRADA'
     */
    public void registrarAdquisicionDesdeSolicitud(
            int idSolicitud,
            int idProveedor,
            double costoUnitario,
            int idUbicacion,
            String usuarioEjecuta
    ) throws SQLException {

        String sql = "{ call dbo.sp_Adquisicion_Registrar(?, ?, ?, ?, ?) }";

        try (Connection cn = Conexion.getConnection();
             CallableStatement cs = cn.prepareCall(sql)) {

            cs.setInt(1, idSolicitud);
            cs.setInt(2, idProveedor);
            cs.setBigDecimal(3, java.math.BigDecimal.valueOf(costoUnitario));
            cs.setInt(4, idUbicacion);
            cs.setString(5, usuarioEjecuta);

            cs.execute();
        }
    }
}
