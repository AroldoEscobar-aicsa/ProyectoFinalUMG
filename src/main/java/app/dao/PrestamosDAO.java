package app.dao;

import app.db.Conexion;
import app.model.Prestamos;

import javax.swing.*;
import java.awt.Component;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamosDAO {

    // ====== UTILIDADES DE RESOLUCIÓN ======

    /** Devuelve IdCliente por su "Codigo" (carnet) */
    public Integer getIdClienteByCodigo(String codigo) throws SQLException {
        final String sql = "SELECT Id FROM dbo.Clientes WHERE Codigo = ? AND IsActive = 1";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Devuelve IdCopia por su código de barras (si existiera flujo con CB) */
    public Integer getIdCopiaByCodigoBarra(String codigoBarra) throws SQLException {
        final String sql = "SELECT Id FROM dbo.Copias WHERE CodigoBarra = ? AND IsActive = 1 AND Estado IN ('DISPONIBLE','RESERVADA','PRESTADA')";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigoBarra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Devuelve el Id del préstamo ACTIVO/ATRASADO para una copia (si existe) */
    public Integer getPrestamoActivoByCopia(int idCopia) throws SQLException {
        final String sql = "SELECT TOP 1 Id FROM dbo.Prestamos WHERE IdCopia=? AND Estado IN ('ACTIVO','ATRASADO') ORDER BY Id DESC";
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCopia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    // ====== LISTADOS PARA COMBOS ======

    /** Lista clientes activos (Id, Codigo, NombreCompleto) */
    public List<ClienteMin> listarClientesActivosMin() throws SQLException {
        final String sql = """
            SELECT Id, Codigo, (RTRIM(LTRIM(Nombres)) + ' ' + RTRIM(LTRIM(Apellidos))) AS Nombre
            FROM dbo.Clientes
            WHERE IsActive = 1
            ORDER BY Apellidos, Nombres
        """;
        List<ClienteMin> res = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res.add(new ClienteMin(
                        rs.getInt("Id"),
                        rs.getString("Codigo"),
                        rs.getString("Nombre")
                ));
            }
        }
        return res;
    }

    /** Lista libros con cantidad de copias disponibles */
    public List<LibroDisp> listarLibrosConDisponibles() throws SQLException {
        final String sql = """
            SELECT l.Id, l.Titulo, ISNULL(disp.Cant, 0) AS Disponibles
            FROM dbo.Libros l
            OUTER APPLY (
                SELECT COUNT(*) AS Cant
                FROM dbo.Copias c
                WHERE c.IdLibro = l.Id
                  AND c.IsActive = 1
                  AND c.Estado = 'DISPONIBLE'
            ) disp
            ORDER BY l.Titulo
        """;
        List<LibroDisp> res = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res.add(new LibroDisp(
                        rs.getInt("Id"),
                        rs.getString("Titulo"),
                        rs.getInt("Disponibles")
                ));
            }
        }
        return res;
    }

    /** Devuelve la primera copia disponible para un libro (o null) */
    public Integer getPrimeraCopiaDisponible(int idLibro) throws SQLException {
        final String sql = """
            SELECT TOP 1 Id
            FROM dbo.Copias
            WHERE IdLibro = ?
              AND IsActive = 1
              AND Estado = 'DISPONIBLE'
            ORDER BY Id
        """;
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /** Cantidad de copias disponibles por libro */
    public int contarDisponiblesPorLibro(int idLibro) throws SQLException {
        final String sql = """
            SELECT COUNT(*) 
            FROM dbo.Copias
            WHERE IdLibro=? AND IsActive=1 AND Estado='DISPONIBLE'
        """;
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ====== OPERACIONES PRINCIPALES ======

    /** Crea un préstamo (por copia). Devuelve el Id del préstamo creado. */
    public int crearPrestamo(int idCliente, int idCopia, String usuarioEjecuta) throws SQLException {
        try (Connection c = Conexion.getConnection();
             CallableStatement cs = c.prepareCall("{ call dbo.sp_Prestamo_Crear(?,?,?) }")) {
            cs.setInt(1, idCliente);
            cs.setInt(2, idCopia);
            cs.setString(3, usuarioEjecuta);
            cs.execute();

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT TOP 1 Id FROM dbo.Prestamos WHERE IdCliente=? AND IdCopia=? ORDER BY Id DESC")) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idCopia);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el Id del préstamo recién creado.");
        }
    }

    /** Crea un préstamo por LIBRO: toma la primera copia disponible. */
    public int crearPrestamoPorLibro(int idCliente, int idLibro, String usuarioEjecuta) throws SQLException {
        Integer idCopia = getPrimeraCopiaDisponible(idLibro);
        if (idCopia == null)
            throw new SQLException("No hay copias DISPONIBLES para el libro seleccionado.");
        return crearPrestamo(idCliente, idCopia, usuarioEjecuta);
    }

    /** Renueva préstamo si aplica (sin reservas pendientes). */
    public void renovarPrestamo(int idPrestamo, String usuarioEjecuta) throws SQLException {
        try (Connection c = Conexion.getConnection();
             CallableStatement cs = c.prepareCall("{ call dbo.sp_Prestamo_Renovar(?,?) }")) {
            cs.setInt(1, idPrestamo);
            cs.setString(2, usuarioEjecuta);
            cs.execute();
        }
    }

    /** Devuelve la copia y calcula multa. */
    public void devolverPrestamo(int idPrestamo, String usuarioEjecuta) throws SQLException {
        try (Connection c = Conexion.getConnection();
             CallableStatement cs = c.prepareCall("{ call dbo.sp_Prestamo_Devolver(?,?) }")) {
            cs.setInt(1, idPrestamo);
            cs.setString(2, usuarioEjecuta);
            cs.execute();
        }
    }

    /** Lista préstamos activos/atrasados de un cliente (vista). */
    public List<Prestamos> listarPrestamosActivosPorCliente(int idCliente) throws SQLException {
        final String sql = "SELECT Id, IdCliente, Codigo, Nombres, IdCopia, CodigoBarra, Titulo, " +
                "FechaPrestamoUtc, FechaVencimientoUtc, Renovaciones, Estado " +
                "FROM dbo.vw_PrestamosActivosPorCliente WHERE IdCliente = ? ORDER BY FechaPrestamoUtc DESC";

        List<Prestamos> lista = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prestamos p = new Prestamos();
                    p.setId(rs.getInt("Id"));
                    p.setIdCliente(rs.getInt("IdCliente"));
                    p.setCodigoCliente(rs.getString("Codigo"));
                    p.setNombreCliente(rs.getString("Nombres"));
                    p.setIdCopia(rs.getInt("IdCopia"));
                    p.setCodigoBarra(rs.getString("CodigoBarra"));
                    p.setTitulo(rs.getString("Titulo"));
                    Timestamp fp = rs.getTimestamp("FechaPrestamoUtc");
                    Timestamp fv = rs.getTimestamp("FechaVencimientoUtc");
                    p.setFechaPrestamoUtc(fp != null ? fp.toLocalDateTime() : null);
                    p.setFechaVencimientoUtc(fv != null ? fv.toLocalDateTime() : null);
                    p.setRenovaciones(rs.getInt("Renovaciones"));
                    p.setEstado(rs.getString("Estado"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // ... (aquí va el método listarPrestamosActivosPorCliente) ...

    /**
     * ¡MÉTODO AÑADIDO!
     * Lista el historial de préstamos CERRADOS de un cliente.
     */
    public List<Prestamos> listarPrestamosHistorialPorCliente(int idCliente) throws SQLException {
        // Esta consulta busca en la tabla principal de Préstamos
        // y se une a otras tablas para obtener los nombres.
        final String sql = """
            SELECT 
                P.Id, P.IdCliente, P.IdCopia, 
                P.FechaPrestamoUtc, P.FechaVencimientoUtc, P.FechaDevolucionUtc, 
                P.Renovaciones, P.Estado,
                COP.CodigoBarra,
                L.Titulo,
                (CLI.Nombres + ' ' + CLI.Apellidos) AS NombreCliente,
                CLI.Codigo AS CodigoCliente
            FROM dbo.Prestamos P
            JOIN dbo.Clientes CLI ON P.IdCliente = CLI.Id
            JOIN dbo.Copias COP ON P.IdCopia = COP.Id
            JOIN dbo.Libros L ON COP.IdLibro = L.Id
            WHERE P.IdCliente = ? AND P.Estado = 'ACTIVO'
            ORDER BY P.FechaDevolucionUtc DESC
            """;

        List<Prestamos> lista = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mapeamos el resultado al modelo
                    // (Asegúrate de que tu app.model.Prestamos tenga estos setters)
                    Prestamos p = new Prestamos();
                    p.setId(rs.getInt("Id"));
                    p.setIdCliente(rs.getInt("IdCliente"));
                    p.setCodigoCliente(rs.getString("CodigoCliente"));
                    p.setNombreCliente(rs.getString("NombreCliente"));
                    p.setIdCopia(rs.getInt("IdCopia"));
                    p.setCodigoBarra(rs.getString("CodigoBarra"));
                    p.setTitulo(rs.getString("Titulo"));

                    Timestamp fp = rs.getTimestamp("FechaPrestamoUtc");
                    Timestamp fv = rs.getTimestamp("FechaVencimientoUtc");
                    Timestamp fd = rs.getTimestamp("FechaDevolucionUtc");

                    p.setFechaPrestamoUtc(fp != null ? fp.toLocalDateTime() : null);
                    p.setFechaVencimientoUtc(fv != null ? fv.toLocalDateTime() : null);
                    p.setFechaDevolucionUtc(fd != null ? fd.toLocalDateTime() : null);

                    p.setRenovaciones(rs.getInt("Renovaciones"));
                    p.setEstado(rs.getString("Estado"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // ====== HELPERS UI ======

    public static void showError(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ====== DTOs ligeros para combos ======
    public static class ClienteMin {
        public final int id;
        public final String codigo;
        public final String nombreCompleto;
        public ClienteMin(int id, String codigo, String nombreCompleto) {
            this.id = id; this.codigo = codigo; this.nombreCompleto = nombreCompleto;
        }
    }

    public static class LibroDisp {
        public final int idLibro;
        public final String titulo;
        public final int disponibles;
        public LibroDisp(int idLibro, String titulo, int disponibles) {
            this.idLibro = idLibro; this.titulo = titulo; this.disponibles = disponibles;
        }
    }

    // ... (aquí va el método listarPrestamosHistorialPorCliente) ...

    /**
     * ¡MÉTODO AÑADIDO!
     * Lista TODOS los préstamos (históricos y activos) en un rango de fechas
     * y, opcionalmente, filtrados por estado.
     */
    public List<Prestamos> listarPrestamosPorPeriodo(
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            String estadoFiltro) throws SQLException {

        StringBuilder sql = new StringBuilder("""
            SELECT 
                P.Id, P.IdCliente, P.IdCopia, 
                P.FechaPrestamoUtc, P.FechaVencimientoUtc, P.FechaDevolucionUtc, 
                P.Renovaciones, P.Estado,
                COP.CodigoBarra,
                L.Titulo,
                (CLI.Nombres + ' ' + CLI.Apellidos) AS NombreCliente,
                CLI.Codigo AS CodigoCliente
            FROM dbo.Prestamos P
            JOIN dbo.Clientes CLI ON P.IdCliente = CLI.Id
            JOIN dbo.Copias COP ON P.IdCopia = COP.Id
            JOIN dbo.Libros L ON COP.IdLibro = L.Id
            WHERE CAST(P.FechaPrestamoUtc AS DATE) BETWEEN ? AND ? 
        """);

        // Añadir el filtro de estado SOLO si es necesario
        // Asumimos que "TODOS" (o un string vacío) no filtra
        if (estadoFiltro != null && !estadoFiltro.isEmpty() && !estadoFiltro.equalsIgnoreCase("TODOS")) {
            sql.append(" AND P.Estado = ? ");
        }

        sql.append(" ORDER BY P.FechaPrestamoUtc DESC");

        List<Prestamos> lista = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {

            // Parámetro 1: Fecha Inicio
            ps.setDate(1, java.sql.Date.valueOf(fechaInicio));
            // Parámetro 2: Fecha Fin
            ps.setDate(2, java.sql.Date.valueOf(fechaFin));

            // Parámetro 3 (Opcional): Estado
            if (estadoFiltro != null && !estadoFiltro.isEmpty() && !estadoFiltro.equalsIgnoreCase("TODOS")) {
                ps.setString(3, estadoFiltro);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Mapeamos el resultado al modelo
                    Prestamos p = new Prestamos();
                    p.setId(rs.getInt("Id"));
                    p.setIdCliente(rs.getInt("IdCliente"));
                    p.setCodigoCliente(rs.getString("CodigoCliente"));
                    p.setNombreCliente(rs.getString("NombreCliente"));
                    p.setIdCopia(rs.getInt("IdCopia"));
                    p.setCodigoBarra(rs.getString("CodigoBarra"));
                    p.setTitulo(rs.getString("Titulo"));

                    Timestamp fp = rs.getTimestamp("FechaPrestamoUtc");
                    Timestamp fv = rs.getTimestamp("FechaVencimientoUtc");
                    Timestamp fd = rs.getTimestamp("FechaDevolucionUtc");

                    p.setFechaPrestamoUtc(fp != null ? fp.toLocalDateTime() : null);
                    p.setFechaVencimientoUtc(fv != null ? fv.toLocalDateTime() : null);
                    p.setFechaDevolucionUtc(fd != null ? fd.toLocalDateTime() : null);

                    p.setRenovaciones(rs.getInt("Renovaciones"));
                    p.setEstado(rs.getString("Estado"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }
    /**
     * Lista TODOS los préstamos ACTIVO / ATRASADO
     * (para usarlos en el combo de creación de multas).
     */
    public List<Prestamos> listarPrestamosActivosYAtrasados() throws SQLException {
        final String sql = """
            SELECT 
                P.Id, P.IdCliente, P.IdCopia, 
                P.FechaPrestamoUtc, P.FechaVencimientoUtc, P.FechaDevolucionUtc, 
                P.Renovaciones, P.Estado,
                COP.CodigoBarra,
                L.Titulo,
                (CLI.Nombres + ' ' + CLI.Apellidos) AS NombreCliente,
                CLI.Codigo AS CodigoCliente
            FROM dbo.Prestamos P
            JOIN dbo.Clientes CLI ON P.IdCliente = CLI.Id
            JOIN dbo.Copias   COP ON P.IdCopia   = COP.Id
            JOIN dbo.Libros   L   ON COP.IdLibro = L.Id
            WHERE P.Estado IN ('ACTIVO','ATRASADO')
            ORDER BY P.FechaPrestamoUtc DESC
            """;

        List<Prestamos> lista = new ArrayList<>();
        try (Connection c = Conexion.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Prestamos p = new Prestamos();
                p.setId(rs.getInt("Id"));
                p.setIdCliente(rs.getInt("IdCliente"));
                p.setCodigoCliente(rs.getString("CodigoCliente"));
                p.setNombreCliente(rs.getString("NombreCliente"));
                p.setIdCopia(rs.getInt("IdCopia"));
                p.setCodigoBarra(rs.getString("CodigoBarra"));
                p.setTitulo(rs.getString("Titulo"));

                Timestamp fp = rs.getTimestamp("FechaPrestamoUtc");
                Timestamp fv = rs.getTimestamp("FechaVencimientoUtc");
                Timestamp fd = rs.getTimestamp("FechaDevolucionUtc");

                p.setFechaPrestamoUtc(fp != null ? fp.toLocalDateTime() : null);
                p.setFechaVencimientoUtc(fv != null ? fv.toLocalDateTime() : null);
                p.setFechaDevolucionUtc(fd != null ? fd.toLocalDateTime() : null);

                p.setRenovaciones(rs.getInt("Renovaciones"));
                p.setEstado(rs.getString("Estado"));
                lista.add(p);
            }
        }
        return lista;
    }
}
