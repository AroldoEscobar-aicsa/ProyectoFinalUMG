package app.view;

import app.dao.CajaMovimientoDAO;
import app.model.CajaMovimiento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.SQLException;
import java.util.List;

public class CajaMovimientoForm extends JFrame{
    private JPanel panelMain;
    private JTable tablaMovimientos;
    private DefaultTableModel modeloTabla;
    private JButton btnApertura;
    private JButton btnIngreso;
    private JButton btnCierre;
    private JButton btnConsultar;
    private JButton btnSalir;
    private JTextField txtIdUsuario;
    private JTextField txtDescripcion;
    private JTextField txtMonto;
    private JTextField txtIdMulta;
    private JTextField txtMontoContado;
    private JTextField txtFechaConsulta;
    private CajaMovimientoDAO cajaDAO;

    public CajaMovimientoForm() {
        setTitle("Gestión de Caja - Movimientos");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cajaDAO = new CajaMovimientoDAO();
        panelMain = new JPanel(new BorderLayout());
        add(panelMain);

        // --- Tabla de movimientos ---
        String[] columnas = {"ID", "Usuario", "FechaHora", "Tipo", "Descripción", "Monto", "ID Multa", "Sistema", "Contado", "Diferencia"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaMovimientos = new JTable(modeloTabla);
        panelMain.add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);

        // --- Panel de datos ---
        JPanel panelDatos = new JPanel(new GridLayout(3, 4, 10, 10));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del Movimiento"));

        txtIdUsuario = new JTextField();
        txtDescripcion = new JTextField();
        txtMonto = new JTextField();
        txtIdMulta = new JTextField();
        txtMontoContado = new JTextField();
        txtFechaConsulta = new JTextField(LocalDate.now().toString());

        panelDatos.add(new JLabel("ID Usuario Cajero:"));
        panelDatos.add(txtIdUsuario);
        panelDatos.add(new JLabel("Monto:"));
        panelDatos.add(txtMonto);
        panelDatos.add(new JLabel("Descripción:"));
        panelDatos.add(txtDescripcion);
        panelDatos.add(new JLabel("ID Multa (opcional):"));
        panelDatos.add(txtIdMulta);
        panelDatos.add(new JLabel("Monto contado (solo cierre):"));
        panelDatos.add(txtMontoContado);
        panelDatos.add(new JLabel("Fecha consulta (YYYY-MM-DD):"));
        panelDatos.add(txtFechaConsulta);

        panelMain.add(panelDatos, BorderLayout.NORTH);

        // --- Panel de botones ---
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnApertura = new JButton("Apertura de Caja");
        btnIngreso = new JButton("Registrar Ingreso");
        btnCierre = new JButton("Cierre de Caja");
        btnConsultar = new JButton("Consultar Día");
        btnSalir = new JButton("Salir");

        panelBotones.add(btnApertura);
        panelBotones.add(btnIngreso);
        panelBotones.add(btnCierre);
        panelBotones.add(btnConsultar);
        panelBotones.add(btnSalir);

        panelMain.add(panelBotones, BorderLayout.SOUTH);

        // --- Eventos ---
        btnApertura.addActionListener(e -> registrarApertura());
        btnIngreso.addActionListener(e -> registrarIngreso());
        btnCierre.addActionListener(e -> registrarCierre());
        btnConsultar.addActionListener(e -> consultarMovimientos());
        btnSalir.addActionListener(e -> dispose());
    }

    // --- MÉTODOS DE OPERACIÓN ---

    private void registrarApertura() {
        try {
            int idUsuario = Integer.parseInt(txtIdUsuario.getText());
            double monto = Double.parseDouble(txtMonto.getText());
            LocalDate hoy = LocalDate.now();

            // Validar si ya hay una caja abierta
            if (cajaDAO.verificarCajaAbierta(idUsuario, hoy)) {
                mostrarInfo("Ya tienes una caja abierta para hoy. No puedes abrir otra.");
                return;
            }

            CajaMovimiento mov = new CajaMovimiento();
            mov.setIdUsuarioCajero(idUsuario);
            mov.setFechaHora(LocalDateTime.now());
            mov.setTipoMovimiento("Apertura");
            mov.setDescripcion("Apertura de caja");
            mov.setMonto(monto);

            if (cajaDAO.registrar(mov)) {
                mostrarInfo("Apertura registrada correctamente.");
                consultarMovimientos();
            }

        } catch (Exception ex) {
            mostrarError("Error al registrar apertura: " + ex.getMessage());
        }
    }

    private void registrarIngreso() {
        try {
            int idUsuario = Integer.parseInt(txtIdUsuario.getText());
            double monto = Double.parseDouble(txtMonto.getText());
            String desc = txtDescripcion.getText();
            Integer idMulta = null;
            if (!txtIdMulta.getText().isBlank()) {
                idMulta = Integer.parseInt(txtIdMulta.getText());
            }

            if (!cajaDAO.verificarCajaAbierta(idUsuario, LocalDate.now())) {
                mostrarInfo("No hay caja abierta para registrar ingresos.");
                return;
            }

            CajaMovimiento mov = new CajaMovimiento();
            mov.setIdUsuarioCajero(idUsuario);
            mov.setFechaHora(LocalDateTime.now());
            mov.setTipoMovimiento("Ingreso");
            mov.setDescripcion(desc.isEmpty() ? "Ingreso" : desc);
            mov.setMonto(monto);
            mov.setIdMulta(idMulta);

            if (cajaDAO.registrar(mov)) {
                mostrarInfo("Ingreso registrado correctamente.");
                consultarMovimientos();
            }

        } catch (Exception ex) {
            mostrarError("Error al registrar ingreso: " + ex.getMessage());
        }
    }

    private void registrarCierre() {
        try {
            int idUsuario = Integer.parseInt(txtIdUsuario.getText());
            double contado = Double.parseDouble(txtMontoContado.getText());
            LocalDate hoy = LocalDate.now();

            if (!cajaDAO.verificarCajaAbierta(idUsuario, hoy)) {
                mostrarInfo("No hay caja abierta para cerrar.");
                return;
            }

            double totalSistema = cajaDAO.calcularTotalSistemaDia(idUsuario, hoy);
            double diferencia = contado - totalSistema;

            CajaMovimiento mov = new CajaMovimiento();
            mov.setIdUsuarioCajero(idUsuario);
            mov.setFechaHora(LocalDateTime.now());
            mov.setTipoMovimiento("Cierre");
            mov.setDescripcion("Cierre de caja");
            mov.setMonto(0);
            mov.setMontoCalculadoSistema(totalSistema);
            mov.setMontoRealContado(contado);
            mov.setDiferencia(diferencia);

            if (cajaDAO.registrar(mov)) {
                mostrarInfo("Cierre registrado.\nTotal Sistema: " + totalSistema +
                        "\nContado: " + contado + "\nDiferencia: " + diferencia);
                consultarMovimientos();
            }

        } catch (Exception ex) {
            mostrarError("Error al registrar cierre: " + ex.getMessage());
        }
    }

    private void consultarMovimientos() {
        try {
            modeloTabla.setRowCount(0);
            int idUsuario = Integer.parseInt(txtIdUsuario.getText());
            LocalDate fecha = LocalDate.parse(txtFechaConsulta.getText());

            List<CajaMovimiento> lista = cajaDAO.getMovimientosDiaUsuario(idUsuario, fecha);
            for (CajaMovimiento m : lista) {
                modeloTabla.addRow(new Object[]{
                        m.getIdMovimiento(),
                        m.getIdUsuarioCajero(),
                        m.getFechaHora(),
                        m.getTipoMovimiento(),
                        m.getDescripcion(),
                        m.getMonto(),
                        m.getIdMulta(),
                        m.getMontoCalculadoSistema(),
                        m.getMontoRealContado(),
                        m.getDiferencia()
                });
            }

        } catch (SQLException ex) {
            mostrarError("Error al consultar movimientos: " + ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Entrada inválida: " + ex.getMessage());
        }
    }

    // --- UTILITARIOS ---
    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // --- MAIN PARA PRUEBAS ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CajaMovimientoForm().setVisible(true));
    }
}