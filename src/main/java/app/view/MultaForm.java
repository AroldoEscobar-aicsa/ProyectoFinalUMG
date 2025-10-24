package app.view;

import app.dao.MultaDAO;
import app.model.Multa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
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
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        multaDAO = new MultaDAO();
        panelMain = new JPanel(new BorderLayout());
        add(panelMain);

        // --- TABLA ---
        String[] columnas = {"ID", "Cliente", "Préstamo", "Fecha", "Días Atraso", "Monto", "Pagado", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaMultas = new JTable(modeloTabla);
        JScrollPane scroll = new JScrollPane(tablaMultas);
        panelMain.add(scroll, BorderLayout.CENTER);

        // --- PANEL INFERIOR ---
        JPanel panelAcciones = new JPanel(new GridLayout(2, 4, 10, 10));
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Acciones de Multa"));

        txtIdMulta = new JTextField();
        txtMontoPago = new JTextField();
        txtJustificacion = new JTextField();

        btnCargarPendientes = new JButton("Cargar Pendientes");
        btnBuscarPorId = new JButton("Buscar por ID");
        btnRegistrarPago = new JButton("Registrar Pago");
        btnExonerar = new JButton("Exonerar");
        btnSalir = new JButton("Salir");

        panelAcciones.add(new JLabel("ID Multa:"));
        panelAcciones.add(txtIdMulta);
        panelAcciones.add(new JLabel("Monto Pago:"));
        panelAcciones.add(txtMontoPago);
        panelAcciones.add(new JLabel("Justificación Exoneración:"));
        panelAcciones.add(txtJustificacion);
        panelAcciones.add(btnRegistrarPago);
        panelAcciones.add(btnExonerar);

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnCargarPendientes);
        panelBotones.add(btnBuscarPorId);
        panelBotones.add(btnSalir);

        panelMain.add(panelAcciones, BorderLayout.SOUTH);
        panelMain.add(panelBotones, BorderLayout.NORTH);

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
                modeloTabla.addRow(new Object[]{
                        m.getIdMulta(),
                        m.getIdCliente(),
                        m.getIdPrestamo(),
                        m.getFechaGeneracion(),
                        m.getDiasAtraso(),
                        m.getMontoCalculado(),
                        m.getMontoPagado(),
                        m.getEstado()
                });
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar multas pendientes: " + ex.getMessage());
        }
    }

    private void buscarMultaPorId() {
        try {
            int id = Integer.parseInt(txtIdMulta.getText());
            Multa m = multaDAO.buscarPorId(id);
            modeloTabla.setRowCount(0);
            if (m != null) {
                modeloTabla.addRow(new Object[]{
                        m.getIdMulta(), m.getIdCliente(), m.getIdPrestamo(),
                        m.getFechaGeneracion(), m.getDiasAtraso(),
                        m.getMontoCalculado(), m.getMontoPagado(), m.getEstado()
                });
            } else {
                mostrarInfo("No se encontró ninguna multa con ese ID.");
            }
        } catch (Exception ex) {
            mostrarError("Error al buscar multa: " + ex.getMessage());
        }
    }

    private void registrarPago() {
        try {
            int id = Integer.parseInt(txtIdMulta.getText());
            double monto = Double.parseDouble(txtMontoPago.getText());

            Multa multa = multaDAO.buscarPorId(id);
            if (multa == null) {
                mostrarInfo("No se encontró la multa.");
                return;
            }

            double nuevoTotal = multa.getMontoPagado() + monto;
            String nuevoEstado = (nuevoTotal >= multa.getMontoCalculado()) ? "Pagada" : "Pendiente";

            boolean ok = multaDAO.actualizarPago(id, nuevoTotal, nuevoEstado);
            if (ok) {
                mostrarInfo("Pago registrado correctamente.");
                cargarMultasPendientes();
            } else {
                mostrarError("No se pudo registrar el pago.");
            }
        } catch (Exception ex) {
            mostrarError("Error al registrar pago: " + ex.getMessage());
        }
    }

    private void exonerarMulta() {
        try {
            int id = Integer.parseInt(txtIdMulta.getText());
            String justificacion = txtJustificacion.getText().trim();
            if (justificacion.isEmpty()) {
                mostrarInfo("Debe ingresar una justificación para exonerar.");
                return;
            }

            boolean ok = multaDAO.exonerar(id, justificacion);
            if (ok) {
                mostrarInfo("Multa exonerada correctamente.");
                cargarMultasPendientes();
            } else {
                mostrarError("No se pudo exonerar la multa.");
            }
        } catch (Exception ex) {
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