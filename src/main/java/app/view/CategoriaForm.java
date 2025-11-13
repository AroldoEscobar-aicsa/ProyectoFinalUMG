package app.view;

import app.dao.CategoriaDAO;
import app.model.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CategoriaForm extends JFrame {

    private JTable tableCategorias;
    private JTextField txtNombre;
    private JCheckBox chkActivo;
    private JButton btnGuardar, btnActualizar, btnDesactivar, btnLimpiar;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private int categoriaSeleccionadaId = -1;

    public CategoriaForm() {
        setTitle("Gestión de Categorías");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarTablaCategorias();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Gestión de Categorías de Libros");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        root.add(lblTitulo, BorderLayout.NORTH);

        // Panel izquierdo: formulario
        JPanel panelForm = crearPanelFormulario();

        // Panel derecho: tabla
        JPanel panelTabla = crearPanelTabla();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelForm, panelTabla);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        root.add(split, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel crearPanelFormulario() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("Datos de la categoría"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        txtNombre = new JTextField();
        chkActivo = new JCheckBox("Activa", true);

        // Nombre
        form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(txtNombre, gbc);

        // Estado
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        form.add(chkActivo, gbc);

        // Botones
        btnGuardar = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnDesactivar = new JButton("Desactivar");
        btnLimpiar = new JButton("Limpiar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        acciones.add(btnGuardar);
        acciones.add(btnActualizar);
        acciones.add(btnDesactivar);
        acciones.add(btnLimpiar);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        form.add(acciones, gbc);

        wrapper.add(form, BorderLayout.NORTH);

        // Eventos
        btnGuardar.addActionListener(e -> guardarCategoria());
        btnActualizar.addActionListener(e -> actualizarCategoria());
        btnDesactivar.addActionListener(e -> desactivarCategoria());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        return wrapper;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Lista de categorías"));

        tableCategorias = new JTable();
        tableCategorias.setFillsViewportHeight(true);
        tableCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(tableCategorias);

        JLabel lblInfo = new JLabel("Selecciona una categoría para editarla.");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 2));

        panel.add(lblInfo, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        tableCategorias.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tableCategorias.getSelectedRow();
                if (fila >= 0) {
                    categoriaSeleccionadaId = (int) tableCategorias.getValueAt(fila, 0);
                    txtNombre.setText((String) tableCategorias.getValueAt(fila, 1));
                    chkActivo.setSelected("Activa".equals(tableCategorias.getValueAt(fila, 2)));
                }
            }
        });

        return panel;
    }

    // ============ LÓGICA ============

    private void cargarTablaCategorias() {
        try {
            List<Categoria> lista = categoriaDAO.listarTodas();
            String[] cols = {"ID", "Nombre", "Estado"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            for (Categoria c : lista) {
                model.addRow(new Object[]{
                        c.getId(),
                        c.getNombre(),
                        c.isActivo() ? "Activa" : "Inactiva"
                });
            }
            tableCategorias.setModel(model);

            if (tableCategorias.getColumnCount() > 0) {
                tableCategorias.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
                tableCategorias.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar categorías: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarCategoria() {
        try {
            if (txtNombre.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Ingresa el nombre de la categoría.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Categoria c = new Categoria();
            c.setNombre(txtNombre.getText().trim());
            c.setActivo(chkActivo.isSelected());

            categoriaDAO.crear(c);
            JOptionPane.showMessageDialog(this, "Categoría creada correctamente.");
            cargarTablaCategorias();
            limpiarFormulario();

        } catch (Exception ex) {
            // Aquí podrías revisar si es violación de UNIQUE
            JOptionPane.showMessageDialog(this,
                    "Error al crear categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarCategoria() {
        if (categoriaSeleccionadaId == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría en la tabla.");
            return;
        }
        try {
            Categoria c = categoriaDAO.buscarPorId(categoriaSeleccionadaId);
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Categoría no encontrada.");
                return;
            }

            if (txtNombre.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Ingresa el nombre de la categoría.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            c.setNombre(txtNombre.getText().trim());
            c.setActivo(chkActivo.isSelected());

            categoriaDAO.actualizar(c);
            JOptionPane.showMessageDialog(this, "Categoría actualizada.");
            cargarTablaCategorias();
            limpiarFormulario();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarCategoria() {
        if (categoriaSeleccionadaId == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría en la tabla.");
            return;
        }
        try {
            int op = JOptionPane.showConfirmDialog(this,
                    "¿Desactivar esta categoría?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                categoriaDAO.desactivar(categoriaSeleccionadaId);
                JOptionPane.showMessageDialog(this, "Categoría desactivada.");
                cargarTablaCategorias();
                limpiarFormulario();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al desactivar categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        categoriaSeleccionadaId = -1;
        txtNombre.setText("");
        chkActivo.setSelected(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CategoriaForm().setVisible(true));
    }
}
