package app.view;

import app.dao.MultaDAO;
import app.model.Multa;

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

    private MultaDAO multaDAO;

    public MultaForm() {
        setTitle("Gestión de Multas");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        multaDAO = new MultaDAO();
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

        // Cuando seleccionas una fila, rellenar campos ID y Monto
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

        // --- PANEL INFERIOR (Acciones) ---
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

        // --- PANEL SUPERIOR (botones generales) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCargarPendientes = new JButton("Cargar pendientes");
        btnBuscarPorId = new JButton("Buscar por ID");
        btnSalir = new JButton("Salir");

        panelBotones.add(btnCargarPendientes);
        panelBotones.add(btnBuscarPorId);
        panelBotones.add(btnSalir);

        panelMain.add(panelBotones, BorderLayout.NORTH);
        panelMain.add(panelAcciones, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnCargarPendientes.addActionListener(e -> cargarMultasPendientes());
        btnBuscarPorId.addActionListener(e -> buscarMultaPorId());
        btnRegistrarPago.addActionListener(e -> registrarPago());
        btnExonerar.addActionListener(e -> exonerarMulta());
        btnSalir.addActionListener(e -> dispose());
    }

    // --- MÉTODOS DE LÓGICA ---

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
                // Opcional: limpiar selección
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
