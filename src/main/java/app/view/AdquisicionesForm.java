package app.view;

import app.dao.AdquisicionesDAO;
import app.model.Adquisiciones;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AdquisicionesForm extends JFrame {

    private JTextField txtCodigo, txtProveedor, txtCategoria, txtDescripcion,
            txtCantidad, txtCostoUnitario, txtEstado, txtId;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private AdquisicionesDAO adquisicionesDAO;

    public AdquisicionesForm() {
        adquisicionesDAO = new AdquisicionesDAO();
        initUI();
        cargarDatosTabla();
    }

    private void initUI() {
        setTitle("Gestión de Adquisiciones - Biblioteca");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel superior con campos de entrada
        JPanel panelForm = new JPanel(new GridLayout(6, 4, 8, 8));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos de la Adquisición"));

        txtId = new JTextField();
        txtId.setEnabled(false);
        txtCodigo = new JTextField();
        txtProveedor = new JTextField();
        txtCategoria = new JTextField();
        txtDescripcion = new JTextField();
        txtCantidad = new JTextField();
        txtCostoUnitario = new JTextField();
        txtEstado = new JTextField();

        panelForm.add(new JLabel("ID:"));
        panelForm.add(txtId);
        panelForm.add(new JLabel("Código Compra:"));
        panelForm.add(txtCodigo);
        panelForm.add(new JLabel("Proveedor:"));
        panelForm.add(txtProveedor);
        panelForm.add(new JLabel("Categoría:"));
        panelForm.add(txtCategoria);
        panelForm.add(new JLabel("Descripción:"));
        panelForm.add(txtDescripcion);
        panelForm.add(new JLabel("Cantidad:"));
        panelForm.add(txtCantidad);
        panelForm.add(new JLabel("Costo Unitario:"));
        panelForm.add(txtCostoUnitario);
        panelForm.add(new JLabel("Estado:"));
        panelForm.add(txtEstado);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{
                "ID", "Código", "Proveedor", "Categoría", "Descripción",
                "Cantidad", "Costo Unitario", "Total", "Estado"
        }, 0);
        tabla = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tabla);

        add(panelForm, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnGuardar.addActionListener(e -> guardarAdquisicion());
        btnActualizar.addActionListener(e -> actualizarAdquisicion());
        btnEliminar.addActionListener(e -> eliminarAdquisicion());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) {
                    txtId.setText(modeloTabla.getValueAt(fila, 0).toString());
                    txtCodigo.setText(modeloTabla.getValueAt(fila, 1).toString());
                    txtProveedor.setText(modeloTabla.getValueAt(fila, 2).toString());
                    txtCategoria.setText(modeloTabla.getValueAt(fila, 3).toString());
                    txtDescripcion.setText(modeloTabla.getValueAt(fila, 4).toString());
                    txtCantidad.setText(modeloTabla.getValueAt(fila, 5).toString());
                    txtCostoUnitario.setText(modeloTabla.getValueAt(fila, 6).toString());
                    txtEstado.setText(modeloTabla.getValueAt(fila, 8).toString());
                }
            }
        });
    }

    private void guardarAdquisicion() {
        try {
            Adquisiciones a = new Adquisiciones();
            a.setCodigoCompra(txtCodigo.getText());
            a.setProveedor(txtProveedor.getText());
            a.setCategoria(txtCategoria.getText());
            a.setDescripcion(txtDescripcion.getText());
            a.setCantidad(Integer.parseInt(txtCantidad.getText()));
            a.setCostoUnitario(Double.parseDouble(txtCostoUnitario.getText()));
            a.setFechaSolicitud(new Date());
            a.setFechaAprobacion(null);
            a.setEstado(txtEstado.getText());
            a.setEliminado(false);

            if (adquisicionesDAO.registrarAdquisicion(a)) {
                JOptionPane.showMessageDialog(this, "Adquisición registrada correctamente.");
                cargarDatosTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar la adquisición.");
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarAdquisicion() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una adquisición de la tabla.");
            return;
        }

        try {
            Adquisiciones a = new Adquisiciones();
            a.setIdAdquisicion(Integer.parseInt(txtId.getText()));
            a.setCodigoCompra(txtCodigo.getText());
            a.setProveedor(txtProveedor.getText());
            a.setCategoria(txtCategoria.getText());
            a.setDescripcion(txtDescripcion.getText());
            a.setCantidad(Integer.parseInt(txtCantidad.getText()));
            a.setCostoUnitario(Double.parseDouble(txtCostoUnitario.getText()));
            a.setFechaSolicitud(new Date());
            a.setFechaAprobacion(null);
            a.setEstado(txtEstado.getText());

            if (adquisicionesDAO.actualizarAdquisicion(a)) {
                JOptionPane.showMessageDialog(this, "Adquisición actualizada correctamente.");
                cargarDatosTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la adquisición.");
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarAdquisicion() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una adquisición a eliminar.");
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText());
            if (adquisicionesDAO.eliminarAdquisicion(id)) {
                JOptionPane.showMessageDialog(this, "Adquisición eliminada correctamente.");
                cargarDatosTabla();
                limpiarCampos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar la adquisición.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Adquisiciones> lista = adquisicionesDAO.listarAdquisicionesActivas();
            for (Adquisiciones a : lista) {
                modeloTabla.addRow(new Object[]{
                        a.getIdAdquisicion(),
                        a.getCodigoCompra(),
                        a.getProveedor(),
                        a.getCategoria(),
                        a.getDescripcion(),
                        a.getCantidad(),
                        a.getCostoUnitario(),
                        a.getTotalCompra(),
                        a.getEstado()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar adquisiciones: " + e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtCodigo.setText("");
        txtProveedor.setText("");
        txtCategoria.setText("");
        txtDescripcion.setText("");
        txtCantidad.setText("");
        txtCostoUnitario.setText("");
        txtEstado.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdquisicionesForm().setVisible(true));
    }
}
