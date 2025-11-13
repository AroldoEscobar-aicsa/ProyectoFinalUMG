package app.view;

import app.dao.PrestamosDAO;
import app.dao.PrestamosDAO.ClienteMin;
import app.dao.PrestamosDAO.LibroDisp;
import app.model.Prestamos;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Préstamos y Devoluciones
 * - ADMIN / BIBLIOTECARIO: puede seleccionar cliente/libro, prestar, renovar, devolver.
 * - CLIENTE: solo ve su historial de préstamos, sin poder operar.
 */
public class PrestamosForm extends JFrame {

    private final PrestamosDAO dao = new PrestamosDAO();

    // Usuario autenticado
    private final Usuario usuario;
    /**
     * Valor que se envía a los SP como "usuarioEjecuta".
     * Regla: aquí mandamos el ID del usuario (como String).
     */
    private String usuarioActual = "0"; // valor por defecto
    private String rolNormalizado;
    private Integer idClienteActual; // para modo CLIENTE

    // Controles
    private JComboBox<ComboItemCliente> cboCliente;
    private JComboBox<ComboItemLibro> cboLibro;
    private JLabel lblDisponibles;
    private JTable tblPrestamos;
    private DefaultTableModel modelo;

    // Paneles para poder ocultarlos en modo CLIENTE
    private JPanel panelFiltros;
    private JPanel panelAcciones;

    // Constructor principal: se usa desde MainMenuForm
    public PrestamosForm(Usuario usuario) {
        this.usuario = usuario;

        // ===== Usuario por ID (para auditoría en SP) =====
        if (usuario != null) {
            this.usuarioActual = String.valueOf(usuario.getId());
        }

        this.rolNormalizado = normalizarRol(usuario != null ? usuario.getRolPrincipal() : null);

        setTitle("Préstamos y Devoluciones");
        setSize(900, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();

        if (esCliente()) {
            configurarModoCliente();
        } else {
            cargarCombos();
        }
    }

    // Constructor sin usuario (para pruebas sueltas)
    public PrestamosForm() {
        this(null);
    }

    private String normalizarRol(String rolRaw) {
        if (rolRaw == null) return "";
        String r = rolRaw.trim().toUpperCase();
        if (r.startsWith("ADMIN")) return "ADMIN";
        if (r.startsWith("BIBLIOT")) return "BIBLIOTECARIO";
        if (r.startsWith("FINAN")) return "FINANCIERO";
        if (r.startsWith("CLIEN")) return "CLIENTE";
        return r;
    }

    private boolean esCliente() {
        return "CLIENTE".equals(rolNormalizado);
    }

    private void initUI() {
        panelFiltros = new JPanel(new GridBagLayout());
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
        g.gridx = col++; g.gridy = 0; panelFiltros.add(new JLabel("Cliente:"), g);
        g.gridx = col++; panelFiltros.add(cboCliente, g);
        g.gridx = col++; panelFiltros.add(btnRefrescar, g);

        // Fila 2: Libro
        col = 0;
        g.gridx = col++; g.gridy = 1; panelFiltros.add(new JLabel("Libro:"), g);
        g.gridx = col++; panelFiltros.add(cboLibro, g);
        g.gridx = col++; panelFiltros.add(btnVerDisp, g);

        // Fila 3: Disponibles
        col = 0;
        g.gridx = col++; g.gridy = 2; panelFiltros.add(new JLabel("Copias disponibles:"), g);
        g.gridx = col++; g.gridwidth = 2; panelFiltros.add(lblDisponibles, g);
        g.gridwidth = 1;

        // Panel acciones
        panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAcciones.add(btnPrestar);
        panelAcciones.add(btnRenovar);
        panelAcciones.add(btnDevolver);

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
        add(panelFiltros, BorderLayout.NORTH);
        add(panelAcciones, BorderLayout.CENTER);
        add(sp, BorderLayout.SOUTH);
    }

    // ====== Modo BIBLIOTECARIO / ADMIN (flujo normal) ======

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
        if (esCliente()) {
            // En modo cliente no usamos el combo, sino su propio historial
            cargarHistorialClienteActual();
            return;
        }

        Integer idCliente = getClienteSeleccionadoId();
        if (idCliente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }
        try {
            List<Prestamos> lista = dao.listarPrestamosActivosPorCliente(idCliente);
            llenarTabla(lista);
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onPrestar() {
        if (esCliente()) {
            JOptionPane.showMessageDialog(this,
                    "Esta acción no está permitida para tu rol.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idCliente = getClienteSeleccionadoId();
        Integer idLibro = getLibroSeleccionadoId();
        if (idCliente == null || idLibro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione cliente y libro.");
            return;
        }

        // Validación extra contra UI
        try {
            int disponibles = dao.contarDisponiblesPorLibro(idLibro);
            if (disponibles <= 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay copias DISPONIBLES para este libro.",
                        "Sin stock",
                        JOptionPane.WARNING_MESSAGE);
                onVerDisponibles(); // refresca label
                return;
            }
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
            return;
        }

        try {
            // Aquí usuarioActual contiene el ID del usuario como String
            int idPrestamo = dao.crearPrestamoPorLibro(idCliente, idLibro, usuarioActual);
            JOptionPane.showMessageDialog(this, "Préstamo creado. Id=" + idPrestamo);
            onRefrescar();
            onVerDisponibles();
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onRenovar() {
        if (esCliente()) {
            JOptionPane.showMessageDialog(this,
                    "Esta acción no está permitida para tu rol.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

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
        if (esCliente()) {
            JOptionPane.showMessageDialog(this,
                    "Esta acción no está permitida para tu rol.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

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

    // ====== Modo CLIENTE: solo ver su propio historial ======

    private void configurarModoCliente() {
        setTitle("Mis préstamos");

        // Ocultamos filtros y acciones
        panelFiltros.setVisible(false);
        panelAcciones.setVisible(false);

        // ⚠️ IMPORTANTE:
        // Para clientes, el vínculo es:
        //   Cliente.Codigo = Usuario.id (como texto)
        if (usuario != null) {
            try {
                String codigoCliente = String.valueOf(usuario.getId()); // <--- AQUÍ EL CAMBIO CLAVE
                idClienteActual = dao.getIdClienteByCodigo(codigoCliente);
            } catch (SQLException ex) {
                PrestamosDAO.showError(this, ex);
            }
        }

        if (idClienteActual == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo identificar tu registro de cliente.\n" +
                            "Contacta al administrador.",
                    "Cliente no encontrado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        cargarHistorialClienteActual();
    }

    private void cargarHistorialClienteActual() {
        if (idClienteActual == null) return;
        try {
            List<Prestamos> lista = dao.listarPrestamosHistorialPorCliente(idClienteActual);
            llenarTabla(lista);
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // ====== Helpers tabla ======

    private void llenarTabla(List<Prestamos> lista) {
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

    // Para pruebas rápidas
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamosForm().setVisible(true));
    }
}
