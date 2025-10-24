package app.dao;

import app.db.Conexion;
import app.model.Adquisiciones;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdquisicionesDAO {

    public boolean registrarAdquisicion(Adquisiciones a) throws SQLException {
        String sql = "INSERT INTO Adquisiciones " +
                "(codigoCompra, proveedor, categoria, descripcion, cantidad, costoUnitario, fechaSolicitud, fechaAprobacion, estado, eliminado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, a.getCodigoCompra());
            pstmt.setString(2, a.getProveedor());
            pstmt.setString(3, a.getCategoria());
            pstmt.setString(4, a.getDescripcion());
            pstmt.setInt(5, a.getCantidad());
            pstmt.setDouble(6, a.getCostoUnitario());

            if (a.getFechaSolicitud() != null)
                pstmt.setDate(7, new java.sql.Date(a.getFechaSolicitud().getTime()));
            else
                pstmt.setNull(7, Types.DATE);

            if (a.getFechaAprobacion() != null)
                pstmt.setDate(8, new java.sql.Date(a.getFechaAprobacion().getTime()));
            else
                pstmt.setNull(8, Types.DATE);

            pstmt.setString(9, a.getEstado());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean actualizarAdquisicion(Adquisiciones a) throws SQLException {
        String sql = "UPDATE Adquisiciones SET " +
                "codigoCompra=?, proveedor=?, categoria=?, descripcion=?, cantidad=?, costoUnitario=?, " +
                "fechaSolicitud=?, fechaAprobacion=?, estado=? " +
                "WHERE idAdquisicion=? AND eliminado=0";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, a.getCodigoCompra());
            pstmt.setString(2, a.getProveedor());
            pstmt.setString(3, a.getCategoria());
            pstmt.setString(4, a.getDescripcion());
            pstmt.setInt(5, a.getCantidad());
            pstmt.setDouble(6, a.getCostoUnitario());
            pstmt.setDate(7, a.getFechaSolicitud() != null ? new java.sql.Date(a.getFechaSolicitud().getTime()) : null);
            pstmt.setDate(8, a.getFechaAprobacion() != null ? new java.sql.Date(a.getFechaAprobacion().getTime()) : null);
            pstmt.setString(9, a.getEstado());
            pstmt.setInt(10, a.getIdAdquisicion());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean eliminarAdquisicion(int idAdquisicion) throws SQLException {
        String sql = "UPDATE Adquisiciones SET eliminado = 1 WHERE idAdquisicion = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAdquisicion);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Adquisiciones buscarPorId(int idAdquisicion) throws SQLException {
        String sql = "SELECT * FROM Adquisiciones WHERE idAdquisicion = ? AND eliminado = 0";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAdquisicion);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapearResultSet(rs);
            }
        }
        return null;
    }

    public List<Adquisiciones> listarAdquisicionesActivas() throws SQLException {
        List<Adquisiciones> lista = new ArrayList<>();
        String sql = "SELECT * FROM Adquisiciones WHERE eliminado = 0 ORDER BY fechaSolicitud DESC";

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) lista.add(mapearResultSet(rs));
        }
        return lista;
    }

    private Adquisiciones mapearResultSet(ResultSet rs) throws SQLException {
        Adquisiciones a = new Adquisiciones();
        a.setIdAdquisicion(rs.getInt("idAdquisicion"));
        a.setCodigoCompra(rs.getString("codigoCompra"));
        a.setProveedor(rs.getString("proveedor"));
        a.setCategoria(rs.getString("categoria"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setCantidad(rs.getInt("cantidad"));
        a.setCostoUnitario(rs.getDouble("costoUnitario"));
        a.setFechaSolicitud(rs.getDate("fechaSolicitud"));
        a.setFechaAprobacion(rs.getDate("fechaAprobacion"));
        a.setEstado(rs.getString("estado"));
        a.setEliminado(rs.getBoolean("eliminado"));
        return a;
    }
}
