package app.view;

import app.dao.PrestamosDAO;
import app.dao.PrestamosDAO.ClienteMin;
import app.dao.PrestamosDAO.LibroDisp;
import app.dao.ReservaDAO;
import app.model.Usuario;
import model.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Reservas de libros
 * - ADMIN / BIBLIOTECARIO: pueden crear reservas para un cliente/libro
 *   y ver las reservas pendientes de un cliente.
 * - CLIENTE: solo ve sus propias reservas pendientes y la posición en la cola.
 */
public class ReservasForm extends JFrame {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final PrestamosDAO prestamosDAO = new PrestamosDAO();

    // Usuario autenticado
    private final Usuario usuario;
    private String rolNormalizado;
    private Integer idClienteActual; // para modo CLIENTE

    // Controles
    private JComboBox<ComboItemCliente> cboCliente;
    private JComboBox<ComboItemLibro> cboLibro;
    private JTable tblReservas;
    private DefaultTableModel modelo;

    // Paneles que se ocultan para CLIENTE
    private JPanel panelFiltros;
    private JPanel panelAcciones;

    public ReservasForm(Usuario usuario) {
        this.usuario = usuario;
        this.rolNormalizado = normalizarRol(usuario != null ? usuario.getRolPrincipal() : null);

        setTitle("Reservas de libros");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();

        if (esCliente()) {
            configurarModoCliente();
        } else {
            cargarCombos();
        }
    }

    // Constructor sin usuario (pruebas rápidas)
    public ReservasForm() {
        this(null);
    }

    // ====== Helpers de rol ======

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

    private boolean esAdminOBibliotecario() {
        return "ADMIN".equals(rolNormalizado) || "BIBLIOTECARIO".equals(rolNormalizado);
    }

    // ====== UI ======

    private void initUI() {
        setLayout(new BorderLayout(8, 8));

        // --- Panel superior: filtros (Cliente / Libro) ---
        panelFiltros = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        cboCliente = new JComboBox<>();
        cboLibro   = new JComboBox<>();

        JButton btnVerReservasCliente = new JButton("Ver reservas del cliente");
        btnVerReservasCliente.addActionListener(e -> onVerReservasCliente());

        int col = 0;
        g.gridx = col++; g.gridy = 0;
        panelFiltros.add(new JLabel("Cliente:"), g);

        g.gridx = col++;
        panelFiltros.add(cboCliente, g);

        g.gridx = col++;
        panelFiltros.add(btnVerReservasCliente, g);

        // Segunda fila: Libro para crear reserva
        col = 0;
        g.gridx = col++; g.gridy = 1;
        panelFiltros.add(new JLabel("Libro:"), g);

        g.gridx = col++;
        panelFiltros.add(cboLibro, g);

        add(panelFiltros, BorderLayout.NORTH);

        // --- Panel central: acciones ---
        panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCrearReserva = new JButton("Crear reserva");
        btnCrearReserva.addActionListener(e -> onCrearReserva());
        panelAcciones.add(btnCrearReserva);

        add(panelAcciones, BorderLayout.CENTER);

        // --- Tabla de reservas ---
        modelo = new DefaultTableModel(new Object[]{
                "Id Reserva", "Id Libro", "Fecha creada", "Fecha expira", "Estado", "Posición cola"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tblReservas = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tblReservas);
        sp.setPreferredSize(new Dimension(880, 400));
        add(sp, BorderLayout.SOUTH);

        // Si el rol no es ADMIN ni BIBLIOTECARIO, ocultamos filtros y acciones
        if (!esAdminOBibliotecario()) {
            panelFiltros.setVisible(false);
            panelAcciones.setVisible(false);
        }
    }

    // ====== Carga de combos (ADMIN/BIBLIOTECARIO) ======

    private void cargarCombos() {
        try {
            // Clientes
            cboCliente.removeAllItems();
            List<ClienteMin> clientes = prestamosDAO.listarClientesActivosMin();
            for (ClienteMin c : clientes) {
                cboCliente.addItem(new ComboItemCliente(c.id, c.codigo, c.nombreCompleto));
            }

            // Libros (se reutiliza el DTO de PrestamosDAO)
            cboLibro.removeAllItems();
            List<LibroDisp> libros = prestamosDAO.listarLibrosConDisponibles();
            for (LibroDisp l : libros) {
                cboLibro.addItem(new ComboItemLibro(l.idLibro, l.titulo));
            }

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

    // ====== Acciones (ADMIN/BIBLIOTECARIO) ======

    private void onCrearReserva() {
        if (!esAdminOBibliotecario()) {
            JOptionPane.showMessageDialog(this,
                    "Esta acción no está permitida para tu rol.",
                    "Acción no permitida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idCliente = getClienteSeleccionadoId();
        Integer idLibro   = getLibroSeleccionadoId();

        if (idCliente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }
        if (idLibro == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro.");
            return;
        }

        try {
            boolean ok = reservaDAO.crearReserva(idCliente, idLibro);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Reserva creada correctamente.");
                // Refrescamos las reservas del cliente seleccionado
                onVerReservasCliente();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo crear la reserva.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onVerReservasCliente() {
        if (!esAdminOBibliotecario()) {
            // Para cliente se usa otro flujo
            cargarReservasClienteActual();
            return;
        }

        Integer idCliente = getClienteSeleccionadoId();
        if (idCliente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return;
        }

        try {
            List<Reserva> lista = reservaDAO.listarReservasPendientesPorCliente(idCliente);
            llenarTabla(lista);
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // ====== Modo CLIENTE: solo ver sus reservas pendientes ======

    private void configurarModoCliente() {
        setTitle("Mis reservas");

        // Ocultamos filtros y acciones (el cliente no crea reservas desde aquí)
        panelFiltros.setVisible(false);
        panelAcciones.setVisible(false);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró información del usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla: Clientes.Codigo = String.valueOf(usuario.getId())
        String codigoCliente = String.valueOf(usuario.getId());

        try {
            idClienteActual = prestamosDAO.getIdClienteByCodigo(codigoCliente);
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }

        if (idClienteActual == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo identificar tu registro de cliente.\n" +
                            "Verifica que en la tabla Clientes el campo Codigo sea el Id del usuario: " + codigoCliente,
                    "Cliente no encontrado",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        cargarReservasClienteActual();
    }

    private void cargarReservasClienteActual() {
        if (idClienteActual == null) return;

        try {
            List<Reserva> lista = reservaDAO.listarReservasPendientesPorCliente(idClienteActual);
            llenarTabla(lista);

            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Actualmente no tienes reservas pendientes.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // ====== Tabla ======

    private void llenarTabla(List<Reserva> reservas) {
        modelo.setRowCount(0);
        for (Reserva r : reservas) {
            modelo.addRow(new Object[]{
                    r.getIdReserva(),
                    r.getIdLibro(),               // si quieres luego cambias a título con un join
                    r.getFechaCreado(),
                    r.getFechaExpira(),
                    r.getEstado(),
                    r.getPosicionCola()
            });
        }
    }

    // ====== Helpers para combos ======

    private static class ComboItemCliente {
        final int id;
        final String codigo;
        final String nombre;

        ComboItemCliente(int id, String codigo, String nombre) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return codigo + " - " + nombre;
        }
    }

    private static class ComboItemLibro {
        final int idLibro;
        final String titulo;

        ComboItemLibro(int idLibro, String titulo) {
            this.idLibro = idLibro;
            this.titulo = titulo;
        }

        @Override
        public String toString() {
            return titulo + " (Id=" + idLibro + ")";
        }
    }

    // Para pruebas rápidas
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservasForm().setVisible(true));
    }
}
