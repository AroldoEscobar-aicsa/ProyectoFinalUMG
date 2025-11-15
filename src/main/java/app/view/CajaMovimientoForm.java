package app.view;

import app.dao.CajaMovimientoDAO;
import app.dao.CajaSesionDAO;     // ¡Importamos el DAO que ya tenías!
import app.model.CajaMovimiento;
import app.model.CajaSesion;     // Asumo que tienes este modelo

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
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

    // --- DAOs ---
    private CajaMovimientoDAO movimientoDAO;
    private CajaSesionDAO sesionDAO; //
    private JLabel lblEstado;

    public CajaMovimientoForm() {
        setTitle("Gestión de Caja - Movimientos");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Instanciamos ambos DAOs
        movimientoDAO = new CajaMovimientoDAO();
        sesionDAO = new CajaSesionDAO();

        panelMain = new JPanel(new BorderLayout());
        add(panelMain);

        // --- Tabla de movimientos ---
        String[] columnas = {"ID Mov", "ID Sesión", "Fecha/Hora", "Tipo", "Concepto", "Monto", "ID Multa"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaMovimientos = new JTable(modeloTabla);
        panelMain.add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);

        // --- Panel de datos ---
        JPanel panelDatos = new JPanel(new GridLayout(3, 4, 10, 10));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del Movimiento"));

        txtIdUsuario = new JTextField("3"); // Hardcodeamos ID 3 (Financiero) para pruebas
        txtDescripcion = new JTextField();
        txtMonto = new JTextField();
        txtIdMulta = new JTextField();
        txtMontoContado = new JTextField();
        txtFechaConsulta = new JTextField(LocalDate.now().toString());

        panelDatos.add(new JLabel("ID Usuario Cajero:"));
        panelDatos.add(txtIdUsuario);
        panelDatos.add(new JLabel("Monto (Apertura/Ingreso):"));
        panelDatos.add(txtMonto);
        panelDatos.add(new JLabel("Concepto (Ingreso):"));
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

    // --- MÉTODOS DE OPERACIÓN (CORREGIDOS) ---

    private void registrarApertura() {
        try {
            int idUsuario = Integer.parseInt(txtIdUsuario.getText());
            double montoInicial = Double.parseDouble(txtMonto.getText());
            LocalDate hoy = LocalDate.now();

            // 1. Validar si ya hay una caja abierta (usando el DAO correcto)
            // (Tu DAO no recibe fecha, asume que es 'hoy')
            if (sesionDAO.tieneCajaAbierta(idUsuario)) {
                mostrarInfo("Ya tienes una caja abierta para hoy. No puedes abrir otra.");
                return;
            }

            // 2. Llamar al DAO de Sesiones para abrir
            CajaSesion nuevaSesion = sesionDAO.abrirCaja(idUsuario);
            if (nuevaSesion == null) {
                throw new SQLException("No se pudo crear la sesión.");
            }

            // 3. Crear el primer movimiento (el fondo inicial)
            CajaMovimiento movApertura = new CajaMovimiento();
            movApertura.setIdCajaSesion(nuevaSesion.getId());
            movApertura.setTipo("ENTRADA");
            movApertura.setConcepto("Apertura de caja (fondo inicial)");
            movApertura.setMonto(montoInicial);

            if (movimientoDAO.crear(movApertura)) { // Asumiendo que renombraste 'crear' a 'registrar'
                mostrarInfo("Apertura registrada correctamente. ID Sesión: " + nuevaSesion.getId());
                consultarMovimientos(); // Refrescar la tabla
            }

        } catch (NumberFormatException ex) {
            mostrarError("Formato de número inválido: " + ex.getMessage());
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

            // 1. Verificar que la caja esté abierta
            CajaSesion sesionActual = sesionDAO.getCajaAbierta(idUsuario);
            if (sesionActual == null) {
                mostrarInfo("No hay caja abierta para registrar ingresos.");
                return;
            }

            // 2. Crear el objeto Movimiento
            CajaMovimiento mov = new CajaMovimiento();
            mov.setIdCajaSesion(sesionActual.getId()); // ¡ID de la sesión actual!
            mov.setTipo("ENTRADA"); // Tu BD usa ENTRADA
            mov.setConcepto(desc.isEmpty() ? "Ingreso manual" : desc);
            mov.setMonto(monto);
            mov.setIdMulta(idMulta);

            // 3. Registrar el movimiento (usando el DAO de Movimientos)
            if (movimientoDAO.crear(mov)) {
                mostrarInfo("Ingreso registrado correctamente.");
                consultarMovimientos();
            }

        } catch (NumberFormatException ex) {
            mostrarError("Formato de número inválido: " + ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error al registrar ingreso: " + ex.getMessage());
        }
    }

    private void registrarCierre() {
        try {
            int idUsuario = Integer.parseInt(txtIdUsuario.getText());
            double contado = Double.parseDouble(txtMontoContado.getText());
            LocalDate hoy = LocalDate.now();

            // 1. Verificar que la caja esté abierta
            CajaSesion sesionActual = sesionDAO.getCajaAbierta(idUsuario);
            if (sesionActual == null) {
                mostrarInfo("No hay caja abierta para cerrar.");
                return;
            }

            // 2. Calcular totales usando el DAO de Movimientos
            // (Tu DAO 'calcularTotalSesion' recibe el ID de la sesión)
            double totalSistema = movimientoDAO.calcularTotalSesion(sesionActual.getId());

            // 3. Crear la observación para el cierre
            double diferencia = contado - totalSistema;
            String observacion = String.format(
                    "Cierre. Sistema: %.2f, Contado: %.2f, Diferencia: %.2f",
                    totalSistema, contado, diferencia
            );

            // 4. Llamar al DAO de Sesiones para cerrar
            if (sesionDAO.cerrarCaja(sesionActual.getId(), observacion)) {
                mostrarInfo("Cierre registrado.\n" + observacion);
                consultarMovimientos();
            }

        } catch (NumberFormatException ex) {
            mostrarError("Formato de número inválido: " + ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error al registrar cierre: " + ex.getMessage());
        }
    }

    private void consultarMovimientos() {
        try {
            modeloTabla.setRowCount(0);
            int idUsuario;
            LocalDate fecha;
            try {
                idUsuario = Integer.parseInt(txtIdUsuario.getText());
                fecha = LocalDate.parse(txtFechaConsulta.getText());
            } catch (Exception e) {
                // No mostramos error, solo no consultamos si los campos están mal
                return;
            }

            // 1. Encontrar la sesión (o sesiones) de ese día
            List<CajaSesion> sesionesDia = sesionDAO.getSesionesPorUsuarioYFecha(idUsuario, fecha, fecha);
            if (sesionesDia.isEmpty()) {
                lblEstado.setText("No se encontraron sesiones para este usuario en esta fecha.");
                return;
            }

            // 2. Por cada sesión, traer sus movimientos
            for (CajaSesion sesion : sesionesDia) {
                List<CajaMovimiento> lista = movimientoDAO.getMovimientosPorSesion(sesion.getId());
                for (CajaMovimiento m : lista) {
                    modeloTabla.addRow(new Object[]{
                            m.getIdMovimiento(),
                            m.getIdCajaSesion(),
                            m.getCreadoUtc(),
                            m.getTipo(),
                            m.getConcepto(),
                            m.getMonto(),
                            m.getIdMulta()
                    });
                }
            }
            lblEstado.setText("Movimientos cargados.");

        } catch (SQLException ex) {
            mostrarError("Error al consultar movimientos: " + ex.getMessage());
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