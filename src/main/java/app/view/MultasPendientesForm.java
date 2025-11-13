package app.view;

import app.dao.MultaDAO;
import app.dao.PrestamosDAO;
import app.dao.PrestamosDAO.ClienteMin;
import app.model.Multa;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Multas pendientes
 * - ADMIN / BIBLIOTECARIO / FINANCIERO: pueden ver todas las multas pendientes y filtrar por cliente.
 * - CLIENTE: solo ve sus propias multas pendientes (solo lectura).
 */
public class MultasPendientesForm extends JFrame {

    private final MultaDAO multaDAO = new MultaDAO();
    private final PrestamosDAO prestamosDAO = new PrestamosDAO();

    // Usuario autenticado
    private final Usuario usuario;
    private String rolNormalizado;
    private Integer idClienteActual; // para modo CLIENTE

    // Controles
    private JComboBox<ComboItemCliente> cboCliente;
    private JTable tblMultas;
    private DefaultTableModel modelo;
    private JLabel lblTotal;

    // Paneles para ocultar en modo CLIENTE
    private JPanel panelFiltros;

    // Constructor principal: se usa desde MainMenuForm
    public MultasPendientesForm(Usuario usuario) {
        this.usuario = usuario;
        this.rolNormalizado = normalizarRol(usuario != null ? usuario.getRolPrincipal() : null);

        setTitle("Multas pendientes");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();

        if (esCliente()) {
            configurarModoCliente();
        } else {
            cargarCombosClientes();
            cargarMultasTodasPendientes();
        }
    }

    // Constructor sin usuario (para pruebas sueltas)
    public MultasPendientesForm() {
        this(null);
    }

    // ==== Helpers de rol ====

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

    private boolean esPersonalBibliotecaOFInanzas() {
        return "ADMIN".equals(rolNormalizado)
                || "BIBLIOTECARIO".equals(rolNormalizado)
                || "FINANCIERO".equals(rolNormalizado);
    }

    // ==== UI ====

    private void initUI() {
        setLayout(new BorderLayout(8, 8));

        // --------- Panel filtros (solo ADMIN/BIBLIO/FINAN) ---------
        panelFiltros = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        cboCliente = new JComboBox<>();
        JButton btnVerCliente = new JButton("Ver multas del cliente");
        JButton btnVerTodas = new JButton("Ver todas las pendientes");

        btnVerCliente.addActionListener(e -> onVerMultasPorCliente());
        btnVerTodas.addActionListener(e -> cargarMultasTodasPendientes());

        int col = 0;
        g.gridx = col++; g.gridy = 0;
        panelFiltros.add(new JLabel("Cliente:"), g);

        g.gridx = col++;
        panelFiltros.add(cboCliente, g);

        g.gridx = col++;
        panelFiltros.add(btnVerCliente, g);

        g.gridx = col++;
        panelFiltros.add(btnVerTodas, g);

        add(panelFiltros, BorderLayout.NORTH);

        // --------- Tabla ---------
        modelo = new DefaultTableModel(new Object[]{
                "Id Multa", "Id Préstamo", "Id Cliente", "Fecha generación", "Monto", "Estado"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tblMultas = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tblMultas);
        sp.setPreferredSize(new Dimension(880, 420));
        add(sp, BorderLayout.CENTER);

        // --------- Barra inferior (total) ---------
        JPanel panelBottom = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total pendiente: Q 0.00");
        lblTotal.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panelBottom.add(lblTotal, BorderLayout.WEST);

        add(panelBottom, BorderLayout.SOUTH);

        // Si el rol no es de personal de biblioteca ni finanzas, ocultar filtros
        if (!esPersonalBibliotecaOFInanzas()) {
            panelFiltros.setVisible(false);
        }
    }

    // ==== Carga de combos y datos ====

    private void cargarCombosClientes() {
        try {
            cboCliente.removeAllItems();
            List<ClienteMin> clientes = prestamosDAO.listarClientesActivosMin();
            for (ClienteMin c : clientes) {
                cboCliente.addItem(new ComboItemCliente(c.id, c.codigo, c.nombreCompleto));
            }
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void cargarMultasTodasPendientes() {
        try {
            List<Multa> lista = multaDAO.getMultasPendientes();
            llenarTabla(lista);
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    private void onVerMultasPorCliente() {
        ComboItemCliente item = (ComboItemCliente) cboCliente.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un cliente.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Multa> lista = multaDAO.getMultasPendientesPorCliente(item.id);
            llenarTabla(lista);
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // ==== Modo CLIENTE: solo sus multas pendientes ====

    private void configurarModoCliente() {
        setTitle("Mis multas pendientes");

        // Ocultar filtros (cliente no puede ver las de otros)
        panelFiltros.setVisible(false);

        if (usuario == null) {
            JOptionPane.showMessageDialog(this,
                    "No se encontró información del usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Regla que me diste:
        //  - Usuario se identifica por su Id en Usuarios
        //  - Clientes.Codigo contiene ese Id (como texto)
        // Por lo tanto: Clientes.Codigo = String.valueOf(usuario.getId())
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

        cargarMultasPendientesClienteActual();
    }

    private void cargarMultasPendientesClienteActual() {
        if (idClienteActual == null) return;
        try {
            List<Multa> lista = multaDAO.getMultasPendientesPorCliente(idClienteActual);
            llenarTabla(lista);
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Actualmente no tienes multas pendientes.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            PrestamosDAO.showError(this, ex);
        }
    }

    // ==== Helpers tabla ====

    private void llenarTabla(List<Multa> lista) {
        modelo.setRowCount(0);
        double total = 0.0;

        for (Multa m : lista) {
            total += m.getMontoCalculado();
            modelo.addRow(new Object[]{
                    m.getIdMulta(),
                    m.getIdPrestamo(),
                    m.getIdCliente(),
                    m.getFechaGeneracion(),  // LocalDate -> toString()
                    m.getMontoCalculado(),
                    m.getEstado()
            });
        }

        lblTotal.setText(String.format("Total pendiente: Q %.2f", total));
    }

    // ==== Helpers para combo de clientes ====

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

    // Para pruebas rápidas
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultasPendientesForm().setVisible(true));
    }
}
