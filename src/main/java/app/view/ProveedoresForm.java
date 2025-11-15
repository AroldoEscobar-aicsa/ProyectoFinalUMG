package app.view;

import app.dao.ProveedoresDAO;
import app.model.Proveedores;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Formulario Swing para el CRUD de Proveedores.
 * (Versión corregida con los nombres de clase correctos)
 */
public class ProveedoresForm extends JDialog {

    private ProveedoresDAO dao; // <-- CORREGIDO (Plural)
    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;

    // Campos del formulario
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtNit;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JCheckBox chkActivo;

    // Botones
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEliminar;

    public ProveedoresForm(Frame owner) {
        super(owner, "Gestión de Proveedores", true);
        this.dao = new ProveedoresDAO(); // <-- CORREGIDO (Plural)

        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Panel del Formulario (Norte) ---
        JPanel panelForm = new JPanel(new GridLayout(0, 4, 10, 10));
        panelForm.setBorder(BorderFactory.createTitledBorder("Datos del Proveedor"));

        txtId = new JTextField();
        txtId.setEditable(false);
        txtNombre = new JTextField();
        txtNit = new JTextField();
        txtTelefono = new JTextField();
        txtEmail = new JTextField();
        chkActivo = new JCheckBox("Activo", true);

        panelForm.add(new JLabel("ID:"));
        panelForm.add(txtId);
        panelForm.add(new JLabel("Nombre:"));
        panelForm.add(txtNombre);
        panelForm.add(new JLabel("NIT:"));
        panelForm.add(txtNit);
        panelForm.add(new JLabel("Teléfono:"));
        panelForm.add(txtTelefono);
        panelForm.add(new JLabel("Email:"));
        panelForm.add(txtEmail);
        panelForm.add(new JLabel("Estado:"));
        panelForm.add(chkActivo);

        add(panelForm, BorderLayout.NORTH);

        // --- Panel de la Tabla (Centro) ---
        String[] columnas = {"ID", "Nombre", "NIT", "Teléfono", "Email", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProveedores = new JTable(modeloTabla);
        add(new JScrollPane(tablaProveedores), BorderLayout.CENTER);

        // --- Panel de Botones (Sur) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEliminar = new JButton("Eliminar (Desactivar)");
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- Eventos ---
        tablaProveedores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProveedores.getSelectedRow() != -1) {
                cargarProveedorSeleccionado();
            }
        });

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());

        // Carga inicial de datos
        cargarDatosAsync();
    }

    /**
     * Carga los datos de la BD en un hilo separado (SwingWorker).
     */
    private void cargarDatosAsync() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<Proveedores>, Void> worker = new SwingWorker<>() { // <-- CORREGIDO (Plural)
            @Override
            protected List<Proveedores> doInBackground() throws Exception { // <-- CORREGIDO (Plural)
                return dao.listarActivos();
            }

            @Override
            protected void done() {
                try {
                    List<Proveedores> proveedores = get(); // <-- CORREGIDO (Plural)
                    modeloTabla.setRowCount(0); // Limpiar tabla
                    for (Proveedores p : proveedores) { // <-- CORREGIDO (Plural)
                        modeloTabla.addRow(new Object[]{
                                p.getId(),
                                p.getNombre(),
                                p.getNit(),
                                p.getTelefono(),
                                p.getEmail(),
                                p.isActive()
                        });
                    }
                } catch (Exception e) {
                    mostrarError("Error al cargar proveedores: " + e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    /**
     * Muestra los datos del proveedor seleccionado en el formulario.
     */
    private void cargarProveedorSeleccionado() {
        int fila = tablaProveedores.getSelectedRow();
        txtId.setText(modeloTabla.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        txtNit.setText(modeloTabla.getValueAt(fila, 2) != null ? modeloTabla.getValueAt(fila, 2).toString() : "");
        txtTelefono.setText(modeloTabla.getValueAt(fila, 3) != null ? modeloTabla.getValueAt(fila, 3).toString() : "");
        txtEmail.setText(modeloTabla.getValueAt(fila, 4) != null ? modeloTabla.getValueAt(fila, 4).toString() : "");
        chkActivo.setSelected((Boolean) modeloTabla.getValueAt(fila, 5));
    }

    /**
     * Limpia el formulario para crear un nuevo registro.
     */
    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtNit.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        chkActivo.setSelected(true);
        tablaProveedores.clearSelection();
    }

    /**
     * Lógica para guardar (Crea o Actualiza).
     */

    private void guardar() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }

        try {
            Proveedores p = new Proveedores();
            p.setNombre(txtNombre.getText());
            p.setNit(txtNit.getText());
            p.setTelefono(txtTelefono.getText());
            p.setEmail(txtEmail.getText());
            p.setActive(chkActivo.isSelected());

            boolean resultado;
            if (txtId.getText().isEmpty()) {
                // Crear nuevo
                resultado = dao.crear(p);
                mostrarInfo("Proveedor creado con éxito.");
            } else {
                // Actualizar existente
                p.setId(Integer.parseInt(txtId.getText()));
                resultado = dao.actualizar(p);
                mostrarInfo("Proveedor actualizado con éxito.");
            }

            if (resultado) {
                limpiarFormulario();
                cargarDatosAsync(); // Recargar la tabla
            }
        } catch (Exception e) {
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    /**
     * Lógica para eliminar (desactivar).
     */
    private void eliminar() {
        if (txtId.getText().isEmpty()) {
            mostrarError("Seleccione un proveedor de la tabla para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea desactivar este proveedor?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                dao.eliminar(id);
                mostrarInfo("Proveedor desactivado con éxito.");
                limpiarFormulario();
                cargarDatosAsync(); // Recargar la tabla
            } catch (Exception e) {
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
    }

    // --- Métodos de ayuda (puedes moverlos a una clase Util) ---
    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}