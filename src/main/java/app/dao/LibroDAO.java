package app.dao;

import app.db.Conexion;
import app.model.Categoria;
import app.model.Editorial;
import app.model.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    // ---------- Combos: Editoriales, Autores, Categorias ----------

    public List<Editorial> listarEditorialesActivas() throws SQLException {
        String sql = "SELECT Id, Nombre FROM dbo.Editoriales WHERE IsActive = 1 ORDER BY Nombre";
        List<Editorial> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Editorial(rs.getInt("Id"), rs.getString("Nombre")));
        }
        return lista;
    }

    public List<app.model.Autor> listarAutoresActivos() throws SQLException {
        String sql = "SELECT Id, Nombre FROM dbo.Autores WHERE IsActive = 1 ORDER BY Nombre";
        List<app.model.Autor> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                app.model.Autor a = new app.model.Autor();
                a.setId(rs.getInt("Id"));
                a.setNombre(rs.getString("Nombre"));
                a.setActivo(true);
                lista.add(a);
            }
        }
        return lista;
    }

    public List<Categoria> listarCategoriasActivas() throws SQLException {
        String sql = "SELECT Id, Nombre FROM dbo.Categorias WHERE IsActive = 1 ORDER BY Nombre";
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Categoria(rs.getInt("Id"), rs.getString("Nombre")));
        }
        return lista;
    }

    // ---------- CRUD Libros ----------

    public List<Libro> listarTodos() throws SQLException {
        String sql = """
            SELECT l.Id, l.Titulo, l.ISBN, l.Edicion, l.Anio, l.Idioma,
                   l.IdEditorial, l.StockMinimo, l.IsActive,
                   e.Nombre AS EditorialNombre
            FROM dbo.Libros l
            LEFT JOIN dbo.Editoriales e ON e.Id = l.IdEditorial
            ORDER BY l.Titulo
        """;
        List<Libro> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public List<Libro> listarActivos() throws SQLException {
        String sql = """
            SELECT l.Id, l.Titulo, l.ISBN, l.Edicion, l.Anio, l.Idioma,
                   l.IdEditorial, l.StockMinimo, l.IsActive,
                   e.Nombre AS EditorialNombre
            FROM dbo.Libros l
            LEFT JOIN dbo.Editoriales e ON e.Id = l.IdEditorial
            WHERE l.IsActive = 1
            ORDER BY l.Titulo
        """;
        List<Libro> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public Libro buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT l.Id, l.Titulo, l.ISBN, l.Edicion, l.Anio, l.Idioma,
                   l.IdEditorial, l.StockMinimo, l.IsActive,
                   e.Nombre AS EditorialNombre
            FROM dbo.Libros l
            LEFT JOIN dbo.Editoriales e ON e.Id = l.IdEditorial
            WHERE l.Id = ?
        """;
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Crea libro y asigna autores/categorías (todo en una transacción). */
    public int crear(Libro libro, List<Integer> autoresIds, List<Integer> categoriasIds) throws SQLException {
        String ins = "INSERT INTO dbo.Libros (Titulo, ISBN, Edicion, Anio, Idioma, IdEditorial, StockMinimo, IsActive) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = Conexion.getConnection()) {
            cn.setAutoCommit(false);
            int nuevoId;
            try (PreparedStatement ps = cn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, libro.getTitulo());
                ps.setString(2, libro.getIsbn());
                ps.setString(3, libro.getEdicion());
                if (libro.getAnio() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, libro.getAnio());
                ps.setString(5, libro.getIdioma());
                if (libro.getIdEditorial() == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, libro.getIdEditorial());
                if (libro.getStockMinimo() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, libro.getStockMinimo());
                ps.setBoolean(8, libro.isActivo());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    nuevoId = keys.getInt(1);
                }
            }
            asignarAutoresTx(cn, nuevoId, autoresIds);
            asignarCategoriasTx(cn, nuevoId, categoriasIds);
            cn.commit();
            return nuevoId;
        }
    }

    /** Actualiza libro y reasigna autores/categorías (transacción). */
    public void actualizar(Libro libro, List<Integer> autoresIds, List<Integer> categoriasIds) throws SQLException {
        String upd = "UPDATE dbo.Libros SET Titulo=?, ISBN=?, Edicion=?, Anio=?, Idioma=?, IdEditorial=?, StockMinimo=?, IsActive=? WHERE Id=?";
        try (Connection cn = Conexion.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(upd)) {
                ps.setString(1, libro.getTitulo());
                ps.setString(2, libro.getIsbn());
                ps.setString(3, libro.getEdicion());
                if (libro.getAnio() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, libro.getAnio());
                ps.setString(5, libro.getIdioma());
                if (libro.getIdEditorial() == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, libro.getIdEditorial());
                if (libro.getStockMinimo() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, libro.getStockMinimo());
                ps.setBoolean(8, libro.isActivo());
                ps.setInt(9, libro.getId());
                ps.executeUpdate();
            }
            // reasignar relaciones
            borrarAutoresTx(cn, libro.getId());
            borrarCategoriasTx(cn, libro.getId());
            asignarAutoresTx(cn, libro.getId(), autoresIds);
            asignarCategoriasTx(cn, libro.getId(), categoriasIds);
            cn.commit();
        }
    }

    /** Baja lógica. */
    public void eliminarLogico(int id) throws SQLException {
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement("UPDATE dbo.Libros SET IsActive=0 WHERE Id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ---------- Relaciones (consultas) ----------

    public List<Integer> getAutoresIdsPorLibro(int idLibro) throws SQLException {
        String sql = "SELECT IdAutor FROM dbo.LibroAutores WHERE IdLibro = ?";
        List<Integer> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(rs.getInt(1));
            }
        }
        return lista;
    }

    public List<Integer> getCategoriasIdsPorLibro(int idLibro) throws SQLException {
        String sql = "SELECT IdCategoria FROM dbo.LibroCategorias WHERE IdLibro = ?";
        List<Integer> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(rs.getInt(1));
            }
        }
        return lista;
    }

    // ---------- Helpers TX ----------

    private void borrarAutoresTx(Connection cn, int idLibro) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM dbo.LibroAutores WHERE IdLibro=?")) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }

    private void borrarCategoriasTx(Connection cn, int idLibro) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("DELETE FROM dbo.LibroCategorias WHERE IdLibro=?")) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }

    private void asignarAutoresTx(Connection cn, int idLibro, List<Integer> autoresIds) throws SQLException {
        if (autoresIds == null) return;
        String ins = "INSERT INTO dbo.LibroAutores(IdLibro, IdAutor) VALUES(?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(ins)) {
            for (Integer idAutor : autoresIds) {
                if (idAutor == null) continue;
                ps.setInt(1, idLibro);
                ps.setInt(2, idAutor);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void asignarCategoriasTx(Connection cn, int idLibro, List<Integer> categoriasIds) throws SQLException {
        if (categoriasIds == null) return;
        String ins = "INSERT INTO dbo.LibroCategorias(IdLibro, IdCategoria) VALUES(?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(ins)) {
            for (Integer idCat : categoriasIds) {
                if (idCat == null) continue;
                ps.setInt(1, idLibro);
                ps.setInt(2, idCat);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ---------- Mapper ----------
    private Libro map(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setId(rs.getInt("Id"));
        l.setTitulo(rs.getString("Titulo"));
        l.setIsbn(rs.getString("ISBN"));
        l.setEdicion(rs.getString("Edicion"));
        int anio = rs.getInt("Anio");
        l.setAnio(rs.wasNull() ? null : anio);
        l.setIdioma(rs.getString("Idioma"));
        int idEd = rs.getInt("IdEditorial");
        l.setIdEditorial(rs.wasNull() ? null : idEd);
        int st = rs.getInt("StockMinimo");
        l.setStockMinimo(rs.wasNull() ? null : st);
        l.setActivo(rs.getBoolean("IsActive"));
        l.setEditorialNombre(rs.getString("EditorialNombre"));
        return l;
    }
}
