package app.view;

import app.dao.AutorDAO;
import app.model.Autor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * AutorForm - Formulario CRUD para la entidad Autor.
 * Compatible con la estructura de proyecto app.* y Conexion.getConnection().
 */
public class AutorForm extends JFrame {

    private final AutorDAO autorDAO = new AutorDAO();

    private JTextField txtId, txtNombre;
    private JButton btnNuevo, btnGuardar, btnEditar, btnEliminar, btnCerrar;
    private JTable tablaAutores;
    private DefaultTableModel modelo;

    private boolean modoEdicion = false;

    public AutorForm() {
        setTitle("Gestión de Autores");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ===== PANEL DE DATOS =====
        JPanel panelDatos = new JPanel(new GridLayout(2, 2, 5, 5));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del Autor"));

        panelDatos.add(new JLabel("ID:"));
        txtId = new JTextField();
        txtId.setEnabled(false);
        panelDatos.add(txtId);

        panelDatos.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelDatos.add(txtNombre);

        add(panelDatos, BorderLayout.NORTH);

        // ===== TABLA =====
        String[] columnas = {"ID", "Nombre", "Estado"};
        modelo = new DefaultTableModel(columnas, 0);
        tablaAutores = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tablaAutores);
        add(scroll, BorderLayout.CENTER);

        // ===== BOTONES =====
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnCerrar = new JButton("Cerrar");

        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.SOUTH);

        // ===== EVENTOS =====
        btnNuevo.addActionListener(e -> limpiarCampos());
        btnGuardar.addActionListener(e -> guardarAutor());
        btnEditar.addActionListener(e -> cargarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarAutor());
        btnCerrar.addActionListener(e -> dispose());

        tablaAutores.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) cargarSeleccionado();
            }
        });

        // ===== CARGAR DATOS =====
        cargarTabla();
    }

    // ===== LIMPIAR CAMPOS =====
    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtNombre.requestFocus();
        modoEdicion = false;
    }

    // ===== CARGAR TABLA =====
    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            List<Autor> lista = autorDAO.listarTodos();
            for (Autor a : lista) {
                modelo.addRow(new Object[]{
                        a.getId(),
                        a.getNombre(),
                        a.isEstado() ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar autores: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== GUARDAR O ACTUALIZAR =====
    private void guardarAutor() {
        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese el nombre del autor.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        try {
            Autor a = new Autor();
            a.setNombre(nombre);
            a.setEstado(true);

            if (modoEdicion) {
                a.setId(Integer.parseInt(txtId.getText()));
                autorDAO.actualizar(a);
                JOptionPane.showMessageDialog(this, "Autor actualizado correctamente.");
            } else {
                autorDAO.crear(a);
                JOptionPane.showMessageDialog(this, "Autor agregado correctamente.");
            }

            limpiarCampos();
            cargarTabla();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar autor: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== CARGAR AUTOR SELECCIONADO =====
    private void cargarSeleccionado() {
        int fila = tablaAutores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un autor de la tabla.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        txtId.setText(tablaAutores.getValueAt(fila, 0).toString());
        txtNombre.setText(tablaAutores.getValueAt(fila, 1).toString());
        modoEdicion = true;
    }

    // ===== ELIMINAR AUTOR =====
    private void eliminarAutor() {
        int fila = tablaAutores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un autor para eliminar.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = Integer.parseInt(tablaAutores.getValueAt(fila, 0).toString());
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar este autor?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                autorDAO.eliminarLogico(id);
                JOptionPane.showMessageDialog(this, "Autor eliminado (baja lógica).");
                cargarTabla();
                limpiarCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al eliminar autor: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== MAIN TEST =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AutorForm().setVisible(true));
    }
}
