package app.view;

import app.dao.AutorDAO;
import app.model.Autor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/** Form CRUD de Autores (dbo.Autores) */
public class AutorForm extends JFrame {

    private final AutorDAO autorDAO = new AutorDAO();

    private JTextField txtId, txtNombre, txtPais;
    private JCheckBox chkActivo;
    private JButton btnNuevo, btnGuardar, btnEditar, btnEliminar, btnCerrar, btnRefrescar;
    private JTable tablaAutores;
    private DefaultTableModel modelo;

    private boolean modoEdicion = false;

    public AutorForm() {
        setTitle("Gestión de Autores");
        setSize(720, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ===== PANEL DE DATOS =====
        JPanel panelDatos = new JPanel(new GridLayout(3, 2, 8, 8));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del Autor"));

        panelDatos.add(new JLabel("ID:"));
        txtId = new JTextField();
        txtId.setEnabled(false);
        panelDatos.add(txtId);

        panelDatos.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelDatos.add(txtNombre);

        panelDatos.add(new JLabel("País:"));
        txtPais = new JTextField();
        panelDatos.add(txtPais);

        add(panelDatos, BorderLayout.NORTH);

        // ===== TABLA =====
        String[] columnas = {"ID", "Nombre", "País", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaAutores = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tablaAutores);
        add(scroll, BorderLayout.CENTER);

        // ===== BOTONES =====
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        chkActivo = new JCheckBox("Activo", true);
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnRefrescar = new JButton("Refrescar");
        btnCerrar = new JButton("Cerrar");

        panelBotones.add(chkActivo);
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnCerrar);

        add(panelBotones, BorderLayout.SOUTH);

        // ===== EVENTOS =====
        btnNuevo.addActionListener(e -> limpiarCampos());
        btnGuardar.addActionListener(e -> guardarAutor());
        btnEditar.addActionListener(e -> cargarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarAutor());
        btnRefrescar.addActionListener(e -> cargarTabla());
        btnCerrar.addActionListener(e -> dispose());

        tablaAutores.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) cargarSeleccionado();
            }
        });

        // ===== CARGAR DATOS =====
        cargarTabla();
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtPais.setText("");
        chkActivo.setSelected(true);
        txtNombre.requestFocus();
        modoEdicion = false;
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            List<Autor> lista = autorDAO.listarTodos();
            for (Autor a : lista) {
                modelo.addRow(new Object[]{
                        a.getId(),
                        a.getNombre(),
                        a.getPais(),
                        a.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar autores: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarAutor() {
        String nombre = txtNombre.getText().trim();
        String pais = txtPais.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del autor.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        try {
            Autor a = new Autor();
            a.setNombre(nombre);
            a.setPais(pais.isBlank() ? null : pais);
            a.setActivo(chkActivo.isSelected());

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

    private void cargarSeleccionado() {
        int fila = tablaAutores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un autor de la tabla.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        txtId.setText(String.valueOf(tablaAutores.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(tablaAutores.getValueAt(fila, 1)));
        txtPais.setText(String.valueOf(tablaAutores.getValueAt(fila, 2)));
        chkActivo.setSelected("Activo".equals(tablaAutores.getValueAt(fila, 3)));
        modoEdicion = true;
    }

    private void eliminarAutor() {
        int fila = tablaAutores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un autor para eliminar.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(String.valueOf(tablaAutores.getValueAt(fila, 0)));
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de desactivar este autor?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                autorDAO.eliminarLogico(id);
                JOptionPane.showMessageDialog(this, "Autor desactivado (baja lógica).");
                cargarTabla();
                limpiarCampos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al desactivar autor: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AutorForm().setVisible(true));
    }
}
