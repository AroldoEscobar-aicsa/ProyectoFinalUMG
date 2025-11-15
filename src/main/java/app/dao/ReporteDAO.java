package app.dao;

import app.db.Conexion;
import app.model.ReporteCatalogo;
import app.model.ReporteClienteMoroso;
import app.model.ReporteTopLibro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {
    /**
     * Obtiene el Top 5 de libros más prestados.
     * Esta es la consulta central del reporte de rotación.
     */
    public List<ReporteTopLibro> getTop5LibrosMasPrestados() {

        List<ReporteTopLibro> reporte = new ArrayList<>();

        // Esta es la consulta SQL que hace la magia.
        // 1. Une Prestamos -> Copias -> Libros para saber qué libro se prestó.
        // 2. Une Libros -> LibroAutores -> Autores para obtener los autores.
        // 3. Agrupa por Libro para contar los préstamos.
        // 4. Ordena por el conteo (DESC) y toma los 5 primeros.
        String sql = """
            SELECT TOP 5
                L.Titulo,
                STRING_AGG(A.Nombre, ', ') AS Autores, -- Combina múltiples autores en un string
                COUNT(P.Id) AS TotalPrestamos
            FROM Prestamos P
            INNER JOIN Copias C ON P.IdCopia = C.Id
            INNER JOIN Libros L ON C.IdLibro = L.Id
            INNER JOIN LibroAutores LA ON L.Id = LA.IdLibro
            INNER JOIN Autores A ON LA.IdAutor = A.Id
            GROUP BY 
                L.Id, L.Titulo  -- Agrupamos por ID y Título del libro
            ORDER BY 
                TotalPrestamos DESC; -- Ordenamos por el total, de mayor a menor
            """;

        try (Connection conn = Conexion.getConnection(); // Obtén tu conexión a la BD
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ReporteTopLibro item = new ReporteTopLibro();
                item.setTitulo(rs.getString("Titulo"));
                item.setAutores(rs.getString("Autores"));
                item.setTotalPrestamos(rs.getInt("TotalPrestamos"));
                reporte.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error al generar el reporte de Top Libros: " + e.getMessage());
            // En una app real, aquí lanzarías una excepción o la manejarías mejor
        }

        return reporte;
    }

    /**
     * Obtiene la lista de clientes con multas pendientes.
     * Esta es la consulta central del reporte de morosidad.
     */
    public List<ReporteClienteMoroso> getReporteClientesMorosos() {
        List<ReporteClienteMoroso> reporte = new ArrayList<>();

        // Esta consulta busca clientes que tengan multas
        // cuyo estado sea 'PENDIENTE'
        String sql = """
            SELECT
                C.Nombres + ' ' + C.Apellidos AS NombreCompleto,
                C.Email,
                C.Telefono,
                COUNT(M.Id) AS TotalMultasPendientes,
                SUM(M.Monto) AS MontoTotalDeuda
            FROM Clientes C
            INNER JOIN Multas M ON C.Id = M.IdCliente
            WHERE
                M.Estado = 'PENDIENTE'
            GROUP BY
                C.Id, C.Nombres, C.Apellidos, C.Email, C.Telefono
            ORDER BY
                MontoTotalDeuda DESC;
            """;

        try (Connection conn = Conexion.getConnection(); // Tu clase de conexión
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ReporteClienteMoroso item = new ReporteClienteMoroso();
                item.setNombreCompleto(rs.getString("NombreCompleto"));
                item.setEmail(rs.getString("Email"));
                item.setTelefono(rs.getString("Telefono"));
                item.setTotalMultasPendientes(rs.getInt("TotalMultasPendientes"));
                item.setMontoTotalDeuda(rs.getDouble("MontoTotalDeuda"));
                reporte.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error al generar el reporte de Clientes Morosos: " + e.getMessage());
        }

        return reporte;
    }

    /**
     * Obtiene el catálogo completo de libros con su stock.
     * Esta es la consulta central del reporte de Inventario.
     */
    public List<ReporteCatalogo> getReporteCatalogoCompleto() {
        List<ReporteCatalogo> reporte = new ArrayList<>();

        // Esta consulta usa 'WITH' (CTE) para pre-calcular
        // el stock de copias y luego une todo lo demás.
        String sql = """
            WITH ConteoCopias AS (
                -- Subconsulta para contar copias
                SELECT
                    IdLibro,
                    COUNT(*) AS TotalCopias,
                    SUM(CASE WHEN Estado = 'DISPONIBLE' THEN 1 ELSE 0 END) AS CopiasDisponibles
                FROM Copias
                GROUP BY IdLibro
            )
            SELECT
                L.Titulo,
                L.ISBN,
                L.Anio,
                E.Nombre AS Editorial,
                -- Agrupa todos los autores de un libro en un string
                STRING_AGG(DISTINCT A.Nombre, ', ') AS Autores,
                -- Agrupa todas las categorías de un libro en un string
                STRING_AGG(DISTINCT CAT.Nombre, ', ') AS Categorias,
                ISNULL(CC.TotalCopias, 0) AS TotalCopias,
                ISNULL(CC.CopiasDisponibles, 0) AS CopiasDisponibles
            FROM Libros L
            LEFT JOIN Editoriales E ON L.IdEditorial = E.Id
            LEFT JOIN LibroAutores LA ON L.Id = LA.IdLibro
            LEFT JOIN Autores A ON LA.IdAutor = A.Id
            LEFT JOIN LibroCategorias LC ON L.Id = LC.IdLibro
            LEFT JOIN Categorias CAT ON LC.IdCategoria = CAT.Id
            LEFT JOIN ConteoCopias CC ON L.Id = CC.IdLibro
            GROUP BY
                L.Id, L.Titulo, L.ISBN, L.Anio, E.Nombre, 
                CC.TotalCopias, CC.CopiasDisponibles
            ORDER BY
                L.Titulo;
            """;

        try (Connection conn = Conexion.getConnection(); // Tu clase de conexión
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ReporteCatalogo item = new ReporteCatalogo();
                item.setTitulo(rs.getString("Titulo"));
                item.setIsbn(rs.getString("ISBN"));
                item.setAnio(rs.getInt("Anio"));
                item.setEditorial(rs.getString("Editorial"));
                item.setAutores(rs.getString("Autores"));
                item.setCategorias(rs.getString("Categorias"));
                item.setTotalCopias(rs.getInt("TotalCopias"));
                item.setCopiasDisponibles(rs.getInt("CopiasDisponibles"));
                reporte.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error al generar el reporte de Catálogo: " + e.getMessage());
        }

        return reporte;
    }
}