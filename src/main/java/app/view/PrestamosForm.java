package app.view;

import app.dao.PrestamosDAO;
import app.dao.PrestamosDAO.ClienteMin;
import app.dao.PrestamosDAO.LibroDisp;
import app.model.Prestamos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Préstamos y Devoluciones (sin código de barras)
 * - Selección de Cliente y Libro.
 * - El DAO elige internamente una copia DISPONIBLE para el libro.
 */
public class PrestamosForm extends JFrame {

    private final PrestamosDAO dao = new PrestamosDAO();

    // Controles
    private JComboBox<ComboItemCliente> cboCliente;
    private JComboBox<ComboItemLibro> cboLibro;
    private JLabel lblDisponibles;
    private JTable tblPrestamos;
    private DefaultTableModel modelo;

    // Usuario autenticado
    private String usuarioActual = "admin";

    public PrestamosForm() {
        setTitle("Préstamos y Devoluciones");
        setSize(900, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        cargarCombos();
    }

    private void initUI() {
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;
        int col;

        cboCliente = new JComboBox<>();
        cboLibro   = new JComboBox<>();
        lblDisponibles = new JLabel("-");

        JButton btnRefrescar = new JButton("Ver préstamos del cliente");
        btnRefrescar.addActionListener(e -> onRefrescar());

        JButton btnVerDisp = new JButton("Disponibles del libro");
        btnVerDisp.addActionListener(e -> onVerDisponibles());

        JButton btnPrestar = new JButton("Prestar");
        btnPrestar.addActionListener(e -> onPrestar());

        JButton btnRenovar = new JButton("Renovar");
        btnRenovar.addActionListener(e -> onRenovar());

        JButton btnDevolver = new JButton("Devolver");
        btnDevolver.addActionListener(e -> onDevolver());

        // Fila 1: Cliente
        col = 0;
        g.gridx = col++; g.gridy = 0; top.add(new JLabel("Cliente:"), g);
        g.gridx = col++; top.add(cboCliente, g);
        g.gridx = col++; top.add(btnRefrescar, g);

        // Fila 2: Libro
        col = 0;
        g.gridx = col++; g.gridy = 1; top.add(new JLabel("Libro:"), g);
        g.gridx = col++; top.add(cboLibro, g);
        g.gridx = col++; top.add(btnVerDisp, g);

        // Fila 3: Disponibles
        col = 0;
        g.gridx = col++; g.gridy = 2; top.add(new JLabel("Copias disponibles:"), g);
        g.gridx = col++; g.gridwidth = 2; top.add(lblDisponibles, g);
        g.gridwidth = 1;

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(btnPrestar);
        acciones.add(btnRenovar);
        acciones.add(btnDevolver);

        // Tabla
        modelo = new DefaultTableModel(new Object[]{
                "Id", "Código Barra", "Título", "Prestado", "Vence", "Renov.", "Estado"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblPrestamos = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tblPrestamos);
        sp.setPreferredSize(new Dimension(880, 360));

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(acciones, BorderLayout.CENTER);
        add(sp, BorderLayout.SOUTH);
    }

    private void cargarCombos() {
        try {
            // Clientes
            cboCliente.removeAllItems();
            List<ClienteMin> clientes = dao.listarClientesActivosMin();
            for (ClienteMin c : clientes) {
                cboCliente.addItem(new ComboItemCliente(c.id, c.codigo, c.nombreCompleto));
            }
            // Libros
            cboLibro.removeAllItems();
            List<LibroDisp> libros = dao.listarLibrosConDisponibles();
            for (LibroDisp l : libros) {
                cboLibro.addItem(new ComboItemLibro(l.idLibro, l.titulo, l.disponibles));
            }
            // Disponibles del libro seleccionado
            onVerDisponibles();
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private Integer getClienteSeleccionadoId() {
        ComboItemCliente it = (ComboItemCliente) cboCliente.getSelectedItem();
        return it == null ? null : it.id;
    }

    private Integer getLibroSeleccionadoId() {
        ComboItemLibro it = (ComboItemLibro) cboLibro.getSelectedItem();
        return it == null ? null : it.idLibro;
    }

    private void onVerDisponibles() {
        Integer idLibro = getLibroSeleccionadoId();
        if (idLibro == null) { lblDisponibles.setText("-"); return; }
        try {
            int cant = dao.contarDisponiblesPorLibro(idLibro);
            lblDisponibles.setText(String.valueOf(cant));
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onRefrescar() {
        Integer idCliente = getClienteSeleccionadoId();
        if (idCliente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }
        try {
            List<Prestamos> lista = dao.listarPrestamosActivosPorCliente(idCliente);
            modelo.setRowCount(0);
            for (Prestamos p : lista) {
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getCodigoBarra(),
                        p.getTitulo(),
                        p.getFechaPrestamoUtc(),
                        p.getFechaVencimientoUtc(),
                        p.getRenovaciones(),
                        p.getEstado()
                });
            }
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onPrestar() {
        Integer idCliente = getClienteSeleccionadoId();
        Integer idLibro = getLibroSeleccionadoId();
        if (idCliente == null || idLibro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione cliente y libro.");
            return;
        }
        try {
            int idPrestamo = dao.crearPrestamoPorLibro(idCliente, idLibro, usuarioActual);
            JOptionPane.showMessageDialog(this, "Préstamo creado. Id=" + idPrestamo);
            onRefrescar();
            onVerDisponibles();
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onRenovar() {
        int row = tblPrestamos.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un préstamo en la tabla."); return; }
        int idPrestamo = (int) modelo.getValueAt(row, 0);
        try {
            dao.renovarPrestamo(idPrestamo, usuarioActual);
            JOptionPane.showMessageDialog(this, "Préstamo renovado.");
            onRefrescar();
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onDevolver() {
        int row = tblPrestamos.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un préstamo en la tabla."); return; }
        int idPrestamo = (int) modelo.getValueAt(row, 0);
        try {
            dao.devolverPrestamo(idPrestamo, usuarioActual);
            JOptionPane.showMessageDialog(this, "Devolución registrada.");
            onRefrescar();
            onVerDisponibles();
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // ====== Helpers para combos ======

    private static class ComboItemCliente {
        final int id; final String codigo; final String nombre;
        ComboItemCliente(int id, String codigo, String nombre) {
            this.id = id; this.codigo = codigo; this.nombre = nombre;
        }
        @Override public String toString() { return codigo + " - " + nombre; }
    }

    private static class ComboItemLibro {
        final int idLibro; final String titulo; final int disponibles;
        ComboItemLibro(int idLibro, String titulo, int disponibles) {
            this.idLibro = idLibro; this.titulo = titulo; this.disponibles = disponibles;
        }
        @Override public String toString() { return titulo + "  [" + disponibles + "]"; }
    }

    // Para probar el form
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamosForm().setVisible(true));
    }
}
