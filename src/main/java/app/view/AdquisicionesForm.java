package app.view;

import app.dao.AdquisicionesDAO;
import app.model.Proveedores;
import app.model.SolicitudCompra;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdquisicionesForm extends JFrame {

    private final AdquisicionesDAO adquisicionesDAO = new AdquisicionesDAO();

    // Usuario actual (bibliotecario / admin)
    private final int idUsuarioActual;
    private final String usernameActual;

    // Campos para crear solicitud
    private JTextField txtIdLibro;
    private JTextField txtCantidadSolicitud;

    // Tabla de solicitudes
    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;

    // Panel de aprobación/compra
    private JTextField txtIdSolicitudSeleccionada;
    private JComboBox<Proveedores> cboProveedor;
    private JTextField txtCostoUnitario;
    private JTextField txtIdUbicacion;

    private JButton btnCrearSolicitud;
    private JButton btnAprobarSolicitud;
    private JButton btnRegistrarCompra;
    private JButton btnRefrescar;
    private JButton btnCerrar;

    // ===== Constructor principal (con usuario real) =====
    public AdquisicionesForm(int idUsuarioActual, String usernameActual) {
        this.idUsuarioActual = idUsuarioActual;
        this.usernameActual = usernameActual;

        initUI();
        cargarProveedores();
        cargarSolicitudes();
    }

    // ===== Constructor sin parámetros (para tu botón actual) =====
    public AdquisicionesForm() {
        this(1, "admin");  // luego puedes pasar el usuario real desde tu menú
    }

    private void initUI() {
        setTitle("Adquisiciones y Solicitudes de Compra - Biblioteca");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // =============== PANEL SUPERIOR: CREAR SOLICITUD ===============
        JPanel panelSolicitud = new JPanel(new GridBagLayout());
        panelSolicitud.setBorder(BorderFactory.createTitledBorder("Crear Solicitud de Compra"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtIdLibro = new JTextField();
        txtCantidadSolicitud = new JTextField();
        btnCrearSolicitud = new JButton("Crear solicitud");

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        panelSolicitud.add(new JLabel("Id Libro:"), g);
        g.gridx = 1; g.weightx = 1;
        panelSolicitud.add(txtIdLibro, g);

        g.gridx = 2; g.gridy = 0; g.weightx = 0;
        panelSolicitud.add(new JLabel("Cantidad:"), g);
        g.gridx = 3; g.weightx = 1;
        panelSolicitud.add(txtCantidadSolicitud, g);

        g.gridx = 4; g.gridy = 0; g.weightx = 0;
        panelSolicitud.add(btnCrearSolicitud, g);

        // =============== PANEL CENTRAL: TABLA SOLICITUDES ===============
        String[] cols = {
                "IdSolicitud", "Libro", "Cantidad", "Estado",
                "Solicitante", "Aprobador",
                "Creado", "Aprobado"
        };
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaSolicitudes = new JTable(modeloTabla);
        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollTabla = new JScrollPane(tablaSolicitudes);

        // =============== PANEL INFERIOR: APROBACIÓN Y COMPRA ===============
        JPanel panelAcciones = new JPanel(new GridBagLayout());
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Aprobación y Registro de Compra"));
        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(4, 4, 4, 4);
        a.fill = GridBagConstraints.HORIZONTAL;

        txtIdSolicitudSeleccionada = new JTextField();
        txtIdSolicitudSeleccionada.setEditable(false);

        cboProveedor = new JComboBox<>();
        txtCostoUnitario = new JTextField();
        txtIdUbicacion = new JTextField();

        btnAprobarSolicitud = new JButton("Aprobar solicitud");
        btnRegistrarCompra = new JButton("Registrar compra (crear copias)");
        btnRefrescar = new JButton("Refrescar");
        btnCerrar = new JButton("Cerrar");

        int row = 0;
        a.gridx = 0; a.gridy = row; a.weightx = 0;
        panelAcciones.add(new JLabel("Id Solicitud seleccionada:"), a);
        a.gridx = 1; a.weightx = 1;
        panelAcciones.add(txtIdSolicitudSeleccionada, a);

        row++;
        a.gridx = 0; a.gridy = row; a.weightx = 0;
        panelAcciones.add(new JLabel("Proveedor:"), a);
        a.gridx = 1; a.weightx = 1;
        panelAcciones.add(cboProveedor, a);

        row++;
        a.gridx = 0; a.gridy = row; a.weightx = 0;
        panelAcciones.add(new JLabel("Costo unitario (Q):"), a);
        a.gridx = 1; a.weightx = 1;
        panelAcciones.add(txtCostoUnitario, a);

        row++;
        a.gridx = 0; a.gridy = row; a.weightx = 0;
        panelAcciones.add(new JLabel("Id Ubicación (Ubicaciones.Id):"), a);
        a.gridx = 1; a.weightx = 1;
        panelAcciones.add(txtIdUbicacion, a);

        row++;
        a.gridx = 0; a.gridy = row; a.gridwidth = 2;
        JPanel panelBotonesAccion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonesAccion.add(btnAprobarSolicitud);
        panelBotonesAccion.add(btnRegistrarCompra);
        panelBotonesAccion.add(btnRefrescar);
        panelBotonesAccion.add(btnCerrar);
        panelAcciones.add(panelBotonesAccion, a);

        // =============== ARMAR FRAME ===============
        add(panelSolicitud, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(panelAcciones, BorderLayout.SOUTH);

        // =============== EVENTOS ===============

        // Click en tabla → llenar IdSolicitud
        tablaSolicitudes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaSolicitudes.getSelectedRow();
                if (fila >= 0) {
                    Object val = modeloTabla.getValueAt(fila, 0);
                    txtIdSolicitudSeleccionada.setText(val != null ? val.toString() : "");
                }
            }
        });

        btnCrearSolicitud.addActionListener(e -> onCrearSolicitud());
        btnAprobarSolicitud.addActionListener(e -> onAprobarSolicitud());
        btnRegistrarCompra.addActionListener(e -> onRegistrarCompra());
        btnRefrescar.addActionListener(e -> cargarSolicitudes());
        btnCerrar.addActionListener(e -> dispose());
    }

    // ================== LÓGICA ==================

    private void cargarProveedores() {
        try {
            cboProveedor.removeAllItems();
            List<Proveedores> proveedores = adquisicionesDAO.listarProveedoresActivos();
            for (Proveedores p : proveedores) {
                cboProveedor.addItem(p);
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar proveedores: " + ex.getMessage());
        }
    }

    private void cargarSolicitudes() {
        modeloTabla.setRowCount(0);
        try {
            List<SolicitudCompra> lista = adquisicionesDAO.listarSolicitudesCompra();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (SolicitudCompra sc : lista) {
                String creado = sc.getCreadoUtc() != null ? sc.getCreadoUtc().format(fmt) : "";
                String aprobado = sc.getAprobadoUtc() != null ? sc.getAprobadoUtc().format(fmt) : "";

                modeloTabla.addRow(new Object[]{
                        sc.getId(),
                        sc.getTituloLibro(),
                        sc.getCantidad(),
                        sc.getEstado(),
                        sc.getNombreSolicitante(),
                        sc.getNombreAprobador(),
                        creado,
                        aprobado
                });
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar solicitudes");
        }
    }

    private void onCrearSolicitud() {
        try {
            int idLibro = Integer.parseInt(txtIdLibro.getText().trim());
            int cantidad = Integer.parseInt(txtCantidadSolicitud.getText().trim());

            if (cantidad <= 0) {
                mostrarInfo("La cantidad debe ser mayor que cero.");
                return;
            }

            int idSolicitud = adquisicionesDAO.crearSolicitudCompra(idLibro, cantidad, idUsuarioActual);
            mostrarInfo("Solicitud creada correctamente. Id = " + idSolicitud);
            txtIdLibro.setText("");
            txtCantidadSolicitud.setText("");
            cargarSolicitudes();
        } catch (NumberFormatException ex) {
            mostrarError("Id de libro y cantidad deben ser numéricos.");
        } catch (SQLException ex) {
            mostrarError("Error al crear solicitud: " + ex.getMessage());
        }
    }

    private void onAprobarSolicitud() {
        String txtId = txtIdSolicitudSeleccionada.getText().trim();
        if (txtId.isEmpty()) {
            mostrarInfo("Seleccione una solicitud en la tabla.");
            return;
        }

        try {
            int idSolicitud = Integer.parseInt(txtId);

            int opt = JOptionPane.showConfirmDialog(this,
                    "¿Aprobar la solicitud #" + idSolicitud + "?",
                    "Confirmar aprobación",
                    JOptionPane.YES_NO_OPTION);

            if (opt != JOptionPane.YES_OPTION) return;

            boolean ok = adquisicionesDAO.aprobarSolicitudCompra(idSolicitud, idUsuarioActual);
            if (ok) {
                mostrarInfo("Solicitud aprobada correctamente.");
            } else {
                mostrarInfo("No se pudo aprobar la solicitud (ya no está en estado PENDIENTE).");
            }
            cargarSolicitudes();
        } catch (NumberFormatException ex) {
            mostrarError("Id de solicitud inválido.");
        } catch (SQLException ex) {
            mostrarError("Error al aprobar solicitud: " + ex.getMessage());
        }
    }

    private void onRegistrarCompra() {
        String txtIdSol = txtIdSolicitudSeleccionada.getText().trim();
        if (txtIdSol.isEmpty()) {
            mostrarInfo("Seleccione una solicitud en la tabla.");
            return;
        }

        int fila = tablaSolicitudes.getSelectedRow();
        if (fila < 0) {
            mostrarInfo("Seleccione una solicitud en la tabla.");
            return;
        }

        String estado = (String) modeloTabla.getValueAt(fila, 3);
        if (!"APROBADA".equalsIgnoreCase(estado)) {
            mostrarInfo("Solo se pueden registrar compras para solicitudes en estado APROBADA.");
            return;
        }

        Proveedores proveedor = (Proveedores) cboProveedor.getSelectedItem();
        if (proveedor == null) {
            mostrarInfo("Seleccione un proveedor.");
            return;
        }

        try {
            int idSolicitud = Integer.parseInt(txtIdSol);

            double costoUnit = Double.parseDouble(txtCostoUnitario.getText().trim());
            if (costoUnit < 0) {
                mostrarInfo("El costo unitario no puede ser negativo.");
                return;
            }

            int idUbicacion = Integer.parseInt(txtIdUbicacion.getText().trim());

            int opt = JOptionPane.showConfirmDialog(this,
                    "Registrar compra para solicitud #" + idSolicitud +
                            "\nProveedor: " + proveedor.getNombre() +
                            "\nCosto unitario: Q" + costoUnit +
                            "\nUbicación Id: " + idUbicacion,
                    "Confirmar registro de adquisición",
                    JOptionPane.YES_NO_OPTION);

            if (opt != JOptionPane.YES_OPTION) return;

            adquisicionesDAO.registrarAdquisicionDesdeSolicitud(
                    idSolicitud,
                    proveedor.getId(),
                    costoUnit,
                    idUbicacion,
                    usernameActual
            );

            mostrarInfo("Adquisición registrada. Se crearon nuevas copias y la solicitud pasó a COMPRADA.");
            txtCostoUnitario.setText("");
            txtIdUbicacion.setText("");
            cargarSolicitudes();
        } catch (NumberFormatException ex) {
            mostrarError("Costo unitario e IdUbicacion deben ser numéricos.");
        } catch (SQLException ex) {
            mostrarError("Error al registrar la compra: " + ex.getMessage());
        }
    }

    // ================== UTIL ==================

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // Demo rápido
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new AdquisicionesForm(1, "admin").setVisible(true)
        );
    }
}
