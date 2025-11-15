package app.view;

import app.dao.MultaDAO;
import app.dao.PrestamosDAO;
import app.model.Multa;
import app.model.Prestamos;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MultaForm extends JFrame {

    private JPanel panelMain;
    private JTable tablaMultas;
    private JButton btnCargarPendientes;
    private JButton btnRegistrarPago;
    private JButton btnExonerar;
    private JTextField txtIdMulta;
    private JTextField txtMontoPago;
    private JTextField txtJustificacion;
    private JButton btnBuscarPorId;
    private JButton btnSalir;
    private DefaultTableModel modeloTabla;

    // Combo para CREAR multa manual
    private JComboBox<Prestamos> cboPrestamoMulta;
    private JTextField txtMontoNueva;
    private JTextField txtJustificacionNueva;
    private JButton btnCrearMulta;

    private MultaDAO multaDAO;
    private PrestamosDAO prestamosDAO;

    public MultaForm() {
        setTitle("Gestión de Multas");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        multaDAO = new MultaDAO();
        prestamosDAO = new PrestamosDAO();

        panelMain = new JPanel(new BorderLayout(10, 10));
        add(panelMain);

        // --- TABLA ---
        String[] columnas = {
                "ID Multa",      // 0
                "Cliente",       // 1 (código + nombre)
                "Libro",         // 2
                "Id Préstamo",   // 3
                "Fecha multa",   // 4
                "Días atraso",   // 5
                "Monto",         // 6
                "Estado"         // 7
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaMultas = new JTable(modeloTabla);
        tablaMultas.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(tablaMultas);
        panelMain.add(scroll, BorderLayout.CENTER);

        // Selección de fila -> rellenar ID multa y monto
        tablaMultas.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaMultas.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int row = tablaMultas.getSelectedRow();
                    if (row >= 0) {
                        Object id = modeloTabla.getValueAt(row, 0);
                        Object monto = modeloTabla.getValueAt(row, 6);
                        if (id != null) {
                            txtIdMulta.setText(String.valueOf(id));
                        }
                        if (monto != null) {
                            txtMontoPago.setText(String.valueOf(monto));
                        }
                    }
                }
            }
        });

        // --- PANEL SUPERIOR (botones generales) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCargarPendientes = new JButton("Cargar pendientes");
        btnBuscarPorId = new JButton("Buscar por ID");
        btnSalir = new JButton("Salir");

        panelBotones.add(btnCargarPendientes);
        panelBotones.add(btnBuscarPorId);
        panelBotones.add(btnSalir);

        panelMain.add(panelBotones, BorderLayout.NORTH);

        // --- PANEL PARA CREAR MULTA MANUAL (CON COMBO) ---
        JPanel panelCrearMulta = new JPanel(new GridLayout(2, 4, 10, 10));
        panelCrearMulta.setBorder(BorderFactory.createTitledBorder("Crear multa manual sobre préstamo"));

        cboPrestamoMulta = new JComboBox<>();
        txtMontoNueva = new JTextField();
        txtJustificacionNueva = new JTextField();
        btnCrearMulta = new JButton("Crear multa");

        // Renderer para ver más bonito el préstamo en el combo
        cboPrestamoMulta.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Prestamos) {
                    Prestamos p = (Prestamos) value;
                    String texto = "#" + p.getId()
                            + " | " + (p.getCodigoCliente() != null ? p.getCodigoCliente() : "")
                            + " - " + (p.getNombreCliente() != null ? p.getNombreCliente() : "")
                            + " | " + (p.getTitulo() != null ? p.getTitulo() : "")
                            + " | " + (p.getEstado() != null ? p.getEstado() : "");
                    setText(texto);
                } else if (value == null && index == -1) {
                    setText("-- Seleccione un préstamo --");
                }
                return this;
            }
        });

        panelCrearMulta.add(new JLabel("Préstamo:"));
        panelCrearMulta.add(cboPrestamoMulta);
        panelCrearMulta.add(new JLabel("Monto multa:"));
        panelCrearMulta.add(txtMontoNueva);

        panelCrearMulta.add(new JLabel("Justificación:"));
        panelCrearMulta.add(txtJustificacionNueva);
        panelCrearMulta.add(new JLabel(""));
        panelCrearMulta.add(btnCrearMulta);

        // --- PANEL INFERIOR (Acciones sobre multas EXISTENTES) ---
        JPanel panelAcciones = new JPanel(new GridLayout(2, 4, 10, 10));
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Acciones de Multa"));

        txtIdMulta = new JTextField();
        txtMontoPago = new JTextField();
        txtJustificacion = new JTextField();

        btnRegistrarPago = new JButton("Registrar Pago");
        btnExonerar = new JButton("Exonerar");

        panelAcciones.add(new JLabel("ID Multa:"));
        panelAcciones.add(txtIdMulta);
        panelAcciones.add(new JLabel("Monto a pagar:"));
        panelAcciones.add(txtMontoPago);
        panelAcciones.add(new JLabel("Justificación Exoneración:"));
        panelAcciones.add(txtJustificacion);
        panelAcciones.add(btnRegistrarPago);
        panelAcciones.add(btnExonerar);

        // Contenedor para ambos paneles de abajo
        JPanel panelSur = new JPanel(new BorderLayout(10, 10));
        panelSur.add(panelCrearMulta, BorderLayout.NORTH);
        panelSur.add(panelAcciones, BorderLayout.SOUTH);

        panelMain.add(panelSur, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnCargarPendientes.addActionListener(e -> cargarMultasPendientes());
        btnBuscarPorId.addActionListener(e -> buscarMultaPorId());
        btnRegistrarPago.addActionListener(e -> registrarPago());
        btnExonerar.addActionListener(e -> exonerarMulta());
        btnSalir.addActionListener(e -> dispose());
        btnCrearMulta.addActionListener(e -> crearMultaManual());

        // Cargar datos iniciales
        cargarPrestamosEnCombo();
        cargarMultasPendientes();
    }

    // --- LÓGICA PARA CARGAR PRESTAMOS EN EL COMBO ---

    private void cargarPrestamosEnCombo() {
        try {
            cboPrestamoMulta.removeAllItems();
            List<Prestamos> prestamos = prestamosDAO.listarPrestamosActivosYAtrasados();
            for (Prestamos p : prestamos) {
                cboPrestamoMulta.addItem(p);
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar préstamos para multas: " + ex.getMessage());
        }
    }

    // --- MÉTODOS DE LÓGICA DE MULTAS ---

    private void cargarMultasPendientes() {
        try {
            modeloTabla.setRowCount(0);
            List<Multa> multas = multaDAO.getMultasPendientes();
            for (Multa m : multas) {
                String clienteStr = (m.getCodigoCliente() != null ? m.getCodigoCliente() : "")
                        + " - "
                        + (m.getNombreCliente() != null ? m.getNombreCliente() : "");
                modeloTabla.addRow(new Object[]{
                        m.getIdMulta(),           // 0
                        clienteStr,               // 1
                        m.getTituloLibro(),       // 2
                        m.getIdPrestamo(),        // 3
                        m.getFechaGeneracion(),   // 4
                        m.getDiasAtraso(),        // 5
                        m.getMontoCalculado(),    // 6
                        m.getEstado()             // 7
                });
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar multas pendientes: " + ex.getMessage());
        }
    }

    private void buscarMultaPorId() {
        try {
            if (txtIdMulta.getText().trim().isEmpty()) {
                mostrarInfo("Ingrese un ID de multa.");
                return;
            }

            int id = Integer.parseInt(txtIdMulta.getText().trim());
            Multa m = multaDAO.buscarPorId(id);

            modeloTabla.setRowCount(0);
            if (m != null) {
                String clienteStr = (m.getCodigoCliente() != null ? m.getCodigoCliente() : "")
                        + " - "
                        + (m.getNombreCliente() != null ? m.getNombreCliente() : "");
                modeloTabla.addRow(new Object[]{
                        m.getIdMulta(),
                        clienteStr,
                        m.getTituloLibro(),
                        m.getIdPrestamo(),
                        m.getFechaGeneracion(),
                        m.getDiasAtraso(),
                        m.getMontoCalculado(),
                        m.getEstado()
                });

                // Rellenar monto
                txtMontoPago.setText(String.valueOf(m.getMontoCalculado()));
            } else {
                mostrarInfo("No se encontró ninguna multa con ese ID.");
            }
        } catch (NumberFormatException ex) {
            mostrarError("El ID de la multa debe ser numérico.");
        } catch (SQLException ex) {
            mostrarError("Error al buscar multa: " + ex.getMessage());
        }
    }

    private void registrarPago() {
        try {
            String idStr = txtIdMulta.getText().trim();
            String montoStr = txtMontoPago.getText().trim();

            if (idStr.isEmpty() || montoStr.isEmpty()) {
                mostrarInfo("Debe seleccionar una multa y especificar el monto a pagar.");
                return;
            }

            int id = Integer.parseInt(idStr);
            double monto = Double.parseDouble(montoStr);

            Multa multa = multaDAO.buscarPorId(id);
            if (multa == null) {
                mostrarInfo("No se encontró la multa.");
                return;
            }

            if (!"PENDIENTE".equalsIgnoreCase(multa.getEstado())) {
                mostrarInfo("Solo se pueden registrar pagos de multas PENDIENTE.");
                return;
            }

            if (multa.getDiasAtraso() <= 0) {
                mostrarInfo("Esta multa no tiene días de atraso (o no se pudo calcular).");
                return;
            }

            double montoMulta = multa.getMontoCalculado();
            if (Math.abs(monto - montoMulta) > 0.001) {
                mostrarInfo("El monto a pagar debe ser exactamente Q" + montoMulta + ".");
                return;
            }

            String nuevoEstado = "PAGADA";

            boolean ok = multaDAO.actualizarPago(id, monto, nuevoEstado);
            if (ok) {
                mostrarInfo("Pago registrado correctamente.");
                cargarMultasPendientes();
                txtMontoPago.setText("");
                tablaMultas.clearSelection();
            } else {
                mostrarError("No se pudo registrar el pago.");
            }
        } catch (NumberFormatException ex) {
            mostrarError("ID de multa y monto deben ser numéricos.");
        } catch (SQLException ex) {
            mostrarError("Error al registrar pago: " + ex.getMessage());
        }
    }

    private void exonerarMulta() {
        try {
            String idStr = txtIdMulta.getText().trim();
            if (idStr.isEmpty()) {
                mostrarInfo("Ingrese o seleccione el ID de la multa a exonerar.");
                return;
            }

            int id = Integer.parseInt(idStr);
            String justificacion = txtJustificacion.getText().trim();
            if (justificacion.isEmpty()) {
                mostrarInfo("Debe ingresar una justificación para exonerar.");
                return;
            }

            Multa multa = multaDAO.buscarPorId(id);
            if (multa == null) {
                mostrarInfo("No se encontró la multa.");
                return;
            }

            if (!"PENDIENTE".equalsIgnoreCase(multa.getEstado())) {
                mostrarInfo("Solo se pueden exonerar multas PENDIENTE.");
                return;
            }

            boolean ok = multaDAO.exonerar(id, justificacion);
            if (ok) {
                mostrarInfo("Multa exonerada correctamente.");
                cargarMultasPendientes();
                txtJustificacion.setText("");
                tablaMultas.clearSelection();
            } else {
                mostrarError("No se pudo exonerar la multa.");
            }
        } catch (NumberFormatException ex) {
            mostrarError("El ID de la multa debe ser numérico.");
        } catch (SQLException ex) {
            mostrarError("Error al exonerar multa: " + ex.getMessage());
        }
    }

    private void crearMultaManual() {
        try {
            Prestamos prestamoSel = (Prestamos) cboPrestamoMulta.getSelectedItem();
            String montoStr = txtMontoNueva.getText().trim();
            String justif = txtJustificacionNueva.getText().trim();

            if (prestamoSel == null) {
                mostrarInfo("Debe seleccionar un préstamo.");
                return;
            }

            if (montoStr.isEmpty()) {
                mostrarInfo("Debe indicar el monto de la multa.");
                return;
            }

            int idPrestamo = prestamoSel.getId();
            double monto = Double.parseDouble(montoStr);

            if (monto <= 0) {
                mostrarInfo("El monto de la multa debe ser mayor que cero.");
                return;
            }

            int opt = JOptionPane.showConfirmDialog(this,
                    "¿Crear multa por Q" + monto + " para el préstamo #" + idPrestamo + "?\n" +
                            "Cliente: " + prestamoSel.getNombreCliente() + "\n" +
                            "Libro: " + prestamoSel.getTitulo(),
                    "Confirmar creación de multa",
                    JOptionPane.YES_NO_OPTION);

            if (opt != JOptionPane.YES_OPTION) return;

            boolean ok = multaDAO.crearMultaManual(idPrestamo, monto, justif);
            if (!ok) {
                mostrarInfo("No se pudo crear la multa. El préstamo no existe o no está en estado PRESTADO/ACTIVO.");
                return;
            }

            mostrarInfo("Multa creada correctamente.");
            txtMontoNueva.setText("");
            txtJustificacionNueva.setText("");

            // Refrescar lista de pendientes
            cargarMultasPendientes();

        } catch (NumberFormatException ex) {
            mostrarError("El monto debe ser numérico.");
        } catch (SQLException ex) {
            mostrarError("Error al crear la multa: " + ex.getMessage());
        }
    }

    // --- UTILITARIOS ---
    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- MAIN PARA PRUEBAS ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultaForm().setVisible(true));
    }
}
