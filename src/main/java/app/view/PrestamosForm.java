package app.view;

import app.dao.PrestamosDAO;
import app.model.Prestamos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PrestamosForm extends JFrame {

    private final PrestamosDAO dao = new PrestamosDAO();

    private JTextField txtCodigoCliente;
    private JTextField txtCodigoBarra;
    private JLabel lblTituloLibro;
    private JLabel lblEstadoCopia;
    private JTable tblPrestamos;
    private DefaultTableModel modelo;

    // Cambia por el usuario que tengas autenticado en tu app
    private String usuarioActual = "admin";

    public PrestamosForm() {
        setTitle("Préstamos y Devoluciones");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtCodigoCliente = new JTextField(12);
        txtCodigoBarra = new JTextField(12);
        lblTituloLibro = new JLabel("-");
        lblEstadoCopia = new JLabel("-");

        JButton btnBuscarLibro = new JButton("Ver título/estado");
        btnBuscarLibro.addActionListener(e -> onVerLibro());

        JButton btnRefrescar = new JButton("Refrescar préstamos");
        btnRefrescar.addActionListener(e -> onRefrescar());

        JButton btnPrestar = new JButton("Prestar");
        btnPrestar.addActionListener(e -> onPrestar());

        JButton btnRenovar = new JButton("Renovar");
        btnRenovar.addActionListener(e -> onRenovar());

        JButton btnDevolver = new JButton("Devolver");
        btnDevolver.addActionListener(e -> onDevolver());

        int col = 0;
        g.gridx = col++; g.gridy = 0; top.add(new JLabel("Código Cliente:"), g);
        g.gridx = col++; top.add(txtCodigoCliente, g);
        g.gridx = col++; top.add(btnRefrescar, g);

        col = 0;
        g.gridx = col++; g.gridy = 1; top.add(new JLabel("Código de Barra:"), g);
        g.gridx = col++; top.add(txtCodigoBarra, g);
        g.gridx = col++; top.add(btnBuscarLibro, g);

        col = 0;
        g.gridx = col++; g.gridy = 2; top.add(new JLabel("Título:"), g);
        g.gridx = col++; g.gridwidth = 2; top.add(lblTituloLibro, g);
        g.gridwidth = 1;

        col = 0;
        g.gridx = col++; g.gridy = 3; top.add(new JLabel("Estado copia:"), g);
        g.gridx = col++; g.gridwidth = 2; top.add(lblEstadoCopia, g);
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

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(acciones, BorderLayout.CENTER);
        add(sp, BorderLayout.SOUTH);

        // Altura de la tabla
        sp.setPreferredSize(new Dimension(880, 350));
    }

    private void onVerLibro() {
        String cb = txtCodigoBarra.getText().trim();
        if (cb.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el código de barra.");
            return;
        }
        try {
            String titulo = dao.getTituloPorCodigoBarra(cb);
            String estado = dao.getEstadoCopia(cb);
            lblTituloLibro.setText(titulo != null ? titulo : "(no encontrado)");
            lblEstadoCopia.setText(estado != null ? estado : "(no encontrado)");
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onRefrescar() {
        String cod = txtCodigoCliente.getText().trim();
        if (cod.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el código del cliente.");
            return;
        }
        try {
            Integer idCliente = dao.getIdClienteByCodigo(cod);
            if (idCliente == null) {
                JOptionPane.showMessageDialog(this, "Cliente no encontrado o inactivo.");
                return;
            }
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
        String codCliente = txtCodigoCliente.getText().trim();
        String cb = txtCodigoBarra.getText().trim();
        if (codCliente.isEmpty() || cb.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa código de cliente y código de barra.");
            return;
        }
        try {
            Integer idCliente = dao.getIdClienteByCodigo(codCliente);
            if (idCliente == null) { JOptionPane.showMessageDialog(this, "Cliente no encontrado."); return; }

            Integer idCopia = dao.getIdCopiaByCodigoBarra(cb);
            if (idCopia == null) { JOptionPane.showMessageDialog(this, "Copia no encontrada."); return; }

            int idPrestamo = dao.crearPrestamo(idCliente, idCopia, usuarioActual);
            JOptionPane.showMessageDialog(this, "Préstamo creado. Id=" + idPrestamo);
            onRefrescar();
            onVerLibro();
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
        String cb = txtCodigoBarra.getText().trim();
        if (cb.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingresa el código de barra."); return; }
        try {
            Integer idPrestamo = dao.getIdPrestamoActivoPorCodigoBarra(cb);
            if (idPrestamo == null) {
                JOptionPane.showMessageDialog(this, "No existe préstamo activo para esa copia.");
                return;
            }
            dao.devolverPrestamo(idPrestamo, usuarioActual);
            JOptionPane.showMessageDialog(this, "Devolución registrada.");
            onRefrescar();
            onVerLibro();
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // Para probar el form solo:
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamosForm().setVisible(true));
    }
}
