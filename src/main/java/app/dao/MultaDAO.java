package app.dao;

import app.db.Conexion;
import app.model.Multa;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultaDAO {

    /**
     * Crea un nuevo registro de multa en la BD.
     * Se llama cuando un préstamo entra en mora (sp_Prestamo_Devolver).
     */
    public boolean crear(Multa multa) throws SQLException {
        String sql = "INSERT INTO Multas (IdPrestamo, IdCliente, Monto, Estado, Justificacion) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, multa.getIdPrestamo());
            pstmt.setInt(2, multa.getIdCliente());
            pstmt.setDouble(3, multa.getMontoCalculado());

            String estado = multa.getEstado();
            if (estado == null || estado.trim().isEmpty()) {
                estado = "PENDIENTE";
            }
            pstmt.setString(4, estado.toUpperCase());

            pstmt.setString(5, multa.getJustificacionExoneracion());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Marca una multa como PAGADA.
     * (Versión simple: solo actualiza Estado y PagadoUtc. El movimiento en caja lo puedes
     * hacer llamando a sp_Caja_PagarMulta desde otra capa si quieres integrarlo después).
     */
    public boolean actualizarPago(int idMulta, double nuevoMontoPagadoTotal, String nuevoEstado) throws SQLException {
        String sql = "UPDATE Multas " +
                "SET Estado = ?, PagadoUtc = SYSUTCDATETIME() " +
                "WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado.toUpperCase());
            pstmt.setInt(2, idMulta);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Marca una multa como EXONERADA y guarda la justificación.
     * (Si más adelante quieres usar sp_Multa_Exonerar, aquí puedes reemplazar el UPDATE
     * por una llamada al SP vía CallableStatement).
     */
    public boolean exonerar(int idMulta, String justificacion) throws SQLException {
        String sql = "UPDATE Multas " +
                "SET Estado = 'EXONERADA', Justificacion = ?, PagadoUtc = SYSUTCDATETIME() " +
                "WHERE Id = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, justificacion);
            pstmt.setInt(2, idMulta);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Multas pendientes con información enriquecida:
     * - Nombre completo del cliente
     * - Código del cliente
     * - Título del libro
     * - IdCopia y código de barra
     * - Días de atraso (recalculado aprox desde el préstamo)
     */
    public List<Multa> getMultasPendientes() throws SQLException {
        List<Multa> multas = new ArrayList<>();

        String sql = """
            SELECT 
                m.Id                AS IdMulta,
                m.IdPrestamo,
                m.IdCliente,
                m.Monto,
                m.Estado,
                m.Justificacion,
                m.CreadoUtc,
                m.PagadoUtc,
                c.Codigo           AS CodigoCliente,
                c.Nombres,
                c.Apellidos,
                p.IdCopia,
                cp.CodigoBarra,
                l.Titulo           AS TituloLibro,
                DATEDIFF(DAY, p.FechaVencimientoUtc, ISNULL(p.FechaDevolucionUtc, m.CreadoUtc)) AS DiasAtraso
            FROM dbo.Multas m
            JOIN dbo.Clientes  c  ON c.Id  = m.IdCliente
            JOIN dbo.Prestamos p  ON p.Id  = m.IdPrestamo
            JOIN dbo.Copias    cp ON cp.Id = p.IdCopia
            JOIN dbo.Libros    l  ON l.Id  = cp.IdLibro
            WHERE m.Estado = 'PENDIENTE'
            ORDER BY m.CreadoUtc DESC
            """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                multas.add(mapearMultaConJoin(rs));
            }
        }
        return multas;
    }

    /**
     * Multas pendientes de un cliente específico (también con joins).
     */
    public List<Multa> getMultasPendientesPorCliente(int idCliente) throws SQLException {
        List<Multa> multas = new ArrayList<>();

        String sql = """
            SELECT 
                m.Id                AS IdMulta,
                m.IdPrestamo,
                m.IdCliente,
                m.Monto,
                m.Estado,
                m.Justificacion,
                m.CreadoUtc,
                m.PagadoUtc,
                c.Codigo           AS CodigoCliente,
                c.Nombres,
                c.Apellidos,
                p.IdCopia,
                cp.CodigoBarra,
                l.Titulo           AS TituloLibro,
                DATEDIFF(DAY, p.FechaVencimientoUtc, ISNULL(p.FechaDevolucionUtc, m.CreadoUtc)) AS DiasAtraso
            FROM dbo.Multas m
            JOIN dbo.Clientes  c  ON c.Id  = m.IdCliente
            JOIN dbo.Prestamos p  ON p.Id  = m.IdPrestamo
            JOIN dbo.Copias    cp ON cp.Id = p.IdCopia
            JOIN dbo.Libros    l  ON l.Id  = cp.IdLibro
            WHERE m.Estado = 'PENDIENTE'
              AND m.IdCliente = ?
            ORDER BY m.CreadoUtc DESC
            """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    multas.add(mapearMultaConJoin(rs));
                }
            }
        }
        return multas;
    }

    /**
     * Buscar multa por Id con datos enriquecidos (para Buscar por ID en pantalla).
     */
    public Multa buscarPorId(int idMulta) throws SQLException {
        String sql = """
            SELECT 
                m.Id                AS IdMulta,
                m.IdPrestamo,
                m.IdCliente,
                m.Monto,
                m.Estado,
                m.Justificacion,
                m.CreadoUtc,
                m.PagadoUtc,
                c.Codigo           AS CodigoCliente,
                c.Nombres,
                c.Apellidos,
                p.IdCopia,
                cp.CodigoBarra,
                l.Titulo           AS TituloLibro,
                DATEDIFF(DAY, p.FechaVencimientoUtc, ISNULL(p.FechaDevolucionUtc, m.CreadoUtc)) AS DiasAtraso
            FROM dbo.Multas m
            JOIN dbo.Clientes  c  ON c.Id  = m.IdCliente
            JOIN dbo.Prestamos p  ON p.Id  = m.IdPrestamo
            JOIN dbo.Copias    cp ON cp.Id = p.IdCopia
            JOIN dbo.Libros    l  ON l.Id  = cp.IdLibro
            WHERE m.Id = ?
            """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMulta);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMultaConJoin(rs);
                }
            }
        }
        return null;
    }

    /**
     * REPORTE: Clientes morosos (igual que lo tenías).
     */
    public Map<Integer, Double> getReporteClientesMorosos() throws SQLException {
        Map<Integer, Double> reporte = new HashMap<>();
        String sql = "SELECT IdCliente, SUM(Monto) AS deudaTotal " +
                "FROM Multas " +
                "WHERE Estado = 'PENDIENTE' " +
                "GROUP BY IdCliente " +
                "HAVING SUM(Monto) > 0 " +
                "ORDER BY deudaTotal DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reporte.put(rs.getInt("IdCliente"), rs.getDouble("deudaTotal"));
            }
        }
        return reporte;
    }

    /**
     * Mapea resultSet de las consultas JOIN (con cliente, libro, copia, días atraso).
     */
    private Multa mapearMultaConJoin(ResultSet rs) throws SQLException {
        Multa m = new Multa();

        m.setIdMulta(rs.getInt("IdMulta"));
        m.setIdPrestamo(rs.getInt("IdPrestamo"));
        m.setIdCliente(rs.getInt("IdCliente"));

        m.setMontoCalculado(rs.getDouble("Monto"));

        String estado = rs.getString("Estado");
        m.setEstado(estado);

        // montoPagado derivado
        if ("PENDIENTE".equalsIgnoreCase(estado)) {
            m.setMontoPagado(0.0);
        } else {
            m.setMontoPagado(m.getMontoCalculado());
        }

        m.setJustificacionExoneracion(rs.getString("Justificacion"));

        Timestamp creado = rs.getTimestamp("CreadoUtc");
        if (creado != null) {
            LocalDateTime ldt = creado.toLocalDateTime();
            m.setFechaGeneracion(ldt.toLocalDate());
        }

        Timestamp pagado = rs.getTimestamp("PagadoUtc");
        if (pagado != null) {
            LocalDateTime ldt = pagado.toLocalDateTime();
            m.setFechaLimitePago(ldt.toLocalDate());
        }

        int dias = rs.getInt("DiasAtraso");
        if (dias < 0) dias = 0;
        m.setDiasAtraso(dias);

        // Datos de cliente y libro
        m.setCodigoCliente(rs.getString("CodigoCliente"));

        String nombres = rs.getString("Nombres");
        String apellidos = rs.getString("Apellidos");
        String nombreCompleto = ((nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "")).trim();
        m.setNombreCliente(nombreCompleto);

        m.setTituloLibro(rs.getString("TituloLibro"));

        int idCopia = rs.getInt("IdCopia");
        if (!rs.wasNull()) {
            m.setIdCopia(idCopia);
        }
        m.setCodigoBarraCopia(rs.getString("CodigoBarra"));

        return m;
    }

    /**
     * Multas EXONERADAS con información enriquecida.
     */
    public List<Multa> getMultasExoneradas() throws SQLException {
        List<Multa> multas = new ArrayList<>();

        String sql = """
            SELECT 
                m.Id                AS IdMulta,
                m.IdPrestamo,
                m.IdCliente,
                m.Monto,
                m.Estado,
                m.Justificacion,
                m.CreadoUtc,
                m.PagadoUtc,
                c.Codigo           AS CodigoCliente,
                c.Nombres,
                c.Apellidos,
                p.IdCopia,
                cp.CodigoBarra,
                l.Titulo           AS TituloLibro,
                DATEDIFF(DAY, p.FechaVencimientoUtc, ISNULL(p.FechaDevolucionUtc, m.CreadoUtc)) AS DiasAtraso
            FROM dbo.Multas m
            JOIN dbo.Clientes  c  ON c.Id  = m.IdCliente
            JOIN dbo.Prestamos p  ON p.Id  = m.IdPrestamo
            JOIN dbo.Copias    cp ON cp.Id = p.IdCopia
            JOIN dbo.Libros    l  ON l.Id  = cp.IdLibro
            WHERE m.Estado = 'EXONERADA'
            ORDER BY m.PagadoUtc DESC, m.CreadoUtc DESC
            """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                multas.add(mapearMultaConJoin(rs));
            }
        }
        return multas;
    }

    /**
     * Multas EXONERADAS de un cliente específico.
     */
    public List<Multa> getMultasExoneradasPorCliente(int idCliente) throws SQLException {
        List<Multa> multas = new ArrayList<>();

        String sql = """
            SELECT 
                m.Id                AS IdMulta,
                m.IdPrestamo,
                m.IdCliente,
                m.Monto,
                m.Estado,
                m.Justificacion,
                m.CreadoUtc,
                m.PagadoUtc,
                c.Codigo           AS CodigoCliente,
                c.Nombres,
                c.Apellidos,
                p.IdCopia,
                cp.CodigoBarra,
                l.Titulo           AS TituloLibro,
                DATEDIFF(DAY, p.FechaVencimientoUtc, ISNULL(p.FechaDevolucionUtc, m.CreadoUtc)) AS DiasAtraso
            FROM dbo.Multas m
            JOIN dbo.Clientes  c  ON c.Id  = m.IdCliente
            JOIN dbo.Prestamos p  ON p.Id  = m.IdPrestamo
            JOIN dbo.Copias    cp ON cp.Id = p.IdCopia
            JOIN dbo.Libros    l  ON l.Id  = cp.IdLibro
            WHERE m.Estado = 'EXONERADA'
              AND m.IdCliente = ?
            ORDER BY m.PagadoUtc DESC, m.CreadoUtc DESC
            """;

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    multas.add(mapearMultaConJoin(rs));
                }
            }
        }
        return multas;
    }
}
