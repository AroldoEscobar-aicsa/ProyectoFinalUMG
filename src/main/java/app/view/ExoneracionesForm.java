package app.view;

import app.dao.MultaDAO;
import app.model.Multa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ExoneracionesForm extends JFrame {

    private MultaDAO multaDAO;

    private JTable tablaExoneradas;
    private DefaultTableModel modeloTabla;

    private JTextField txtIdMulta;
    private JTextField txtIdCliente;

    private JButton btnCargarTodas;
    private JButton btnBuscarPorIdMulta;
    private JButton btnBuscarPorCliente;
    private JButton btnSalir;

    public ExoneracionesForm() {
        setTitle("Historial de Exoneraciones de Multas");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        multaDAO = new MultaDAO();

        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        add(main);

        // --- Tabla ---
        String[] columnas = {
                "ID Multa",        // 0
                "Cliente",         // 1 (código + nombre)
                "Libro",           // 2
                "Id Préstamo",     // 3
                "Fecha multa",     // 4 (CreadoUtc fecha)
                "Fecha exoneración", // 5 (PagadoUtc fecha)
                "Días atraso",     // 6 (histórico)
                "Monto",           // 7
                "Justificación"    // 8
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // solo lectura
            }
        };

        tablaExoneradas = new JTable(modeloTabla);
        tablaExoneradas.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(tablaExoneradas);

        main.add(scroll, BorderLayout.CENTER);

        // --- Panel superior (filtros) ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtIdMulta = new JTextField(6);
        txtIdCliente = new JTextField(6);

        btnCargarTodas = new JButton("Cargar todas");
        btnBuscarPorIdMulta = new JButton("Buscar por ID Multa");
        btnBuscarPorCliente = new JButton("Buscar por IdCliente");
        btnSalir = new JButton("Salir");

        panelFiltros.add(new JLabel("ID Multa:"));
        panelFiltros.add(txtIdMulta);
        panelFiltros.add(btnBuscarPorIdMulta);

        panelFiltros.add(Box.createHorizontalStrut(15));

        panelFiltros.add(new JLabel("IdCliente:"));
        panelFiltros.add(txtIdCliente);
        panelFiltros.add(btnBuscarPorCliente);

        panelFiltros.add(Box.createHorizontalStrut(15));
        panelFiltros.add(btnCargarTodas);
        panelFiltros.add(btnSalir);

        main.add(panelFiltros, BorderLayout.NORTH);

        // --- Eventos ---
        btnCargarTodas.addActionListener(e -> cargarTodasExoneradas());
        btnBuscarPorIdMulta.addActionListener(e -> buscarPorIdMulta());
        btnBuscarPorCliente.addActionListener(e -> buscarPorIdCliente());
        btnSalir.addActionListener(e -> dispose());
    }

    // ========== LÓGICA ==========

    private void cargarTodasExoneradas() {
        try {
            List<Multa> lista = multaDAO.getMultasExoneradas();
            llenarTabla(lista);
        } catch (SQLException ex) {
            mostrarError("Error al cargar exoneraciones: " + ex.getMessage());
        }
    }

    private void buscarPorIdMulta() {
        String txt = txtIdMulta.getText().trim();
        if (txt.isEmpty()) {
            mostrarInfo("Ingrese un ID de multa.");
            return;
        }

        try {
            int id = Integer.parseInt(txt);
            Multa m = multaDAO.buscarPorId(id);
            if (m == null) {
                modeloTabla.setRowCount(0);
                mostrarInfo("No se encontró la multa con ese ID.");
                return;
            }

            if (!"EXONERADA".equalsIgnoreCase(m.getEstado())) {
                modeloTabla.setRowCount(0);
                mostrarInfo("La multa encontrada no está EXONERADA (estado actual: " + m.getEstado() + ").");
                return;
            }

            modeloTabla.setRowCount(0);
            llenarTabla(List.of(m));
        } catch (NumberFormatException ex) {
            mostrarError("El ID de multa debe ser numérico.");
        } catch (SQLException ex) {
            mostrarError("Error al buscar multa: " + ex.getMessage());
        }
    }

    private void buscarPorIdCliente() {
        String txt = txtIdCliente.getText().trim();
        if (txt.isEmpty()) {
            mostrarInfo("Ingrese un IdCliente.");
            return;
        }

        try {
            int idCliente = Integer.parseInt(txt);
            List<Multa> lista = multaDAO.getMultasExoneradasPorCliente(idCliente);
            if (lista.isEmpty()) {
                modeloTabla.setRowCount(0);
                mostrarInfo("El cliente no tiene exoneraciones registradas.");
                return;
            }
            llenarTabla(lista);
        } catch (NumberFormatException ex) {
            mostrarError("El IdCliente debe ser numérico.");
        } catch (SQLException ex) {
            mostrarError("Error al buscar exoneraciones por cliente: " + ex.getMessage());
        }
    }

    private void llenarTabla(List<Multa> lista) {
        modeloTabla.setRowCount(0);

        for (Multa m : lista) {
            String cliente = (m.getCodigoCliente() != null ? m.getCodigoCliente() : "") +
                    " - " +
                    (m.getNombreCliente() != null ? m.getNombreCliente() : "");

            modeloTabla.addRow(new Object[]{
                    m.getIdMulta(),               // 0
                    cliente,                      // 1
                    m.getTituloLibro(),           // 2
                    m.getIdPrestamo(),            // 3
                    m.getFechaGeneracion(),       // 4
                    m.getFechaLimitePago(),       // 5 (fecha de exoneración / PagadoUtc)
                    m.getDiasAtraso(),            // 6
                    m.getMontoCalculado(),        // 7
                    m.getJustificacionExoneracion() // 8
            });
        }
    }

    // ========== UTILITARIOS ==========

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== MAIN PARA PRUEBAS ==========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExoneracionesForm().setVisible(true));
    }
}
