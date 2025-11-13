package app.view;

import app.dao.EditorialDAO;
import app.model.Editorial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class EditorialForm extends JFrame {

    private final EditorialDAO editorialDAO = new EditorialDAO();

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtNombre;
    private JTextField txtPais;
    private JCheckBox chkActivo;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDesactivar;
    private JButton btnLimpiar;

    private int editorialSeleccionadaId = -1;

    public EditorialForm() {
        setTitle("Catálogo de editoriales");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarTabla();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));

        // ----- Panel de formulario (izquierda) -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos de la editorial"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblPais   = new JLabel("País:");
        JLabel lblEstado = new JLabel("Estado:");

        txtNombre = new JTextField();
        txtPais   = new JTextField();
        chkActivo = new JCheckBox("Activo", true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(lblNombre, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        form.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(lblPais, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        form.add(txtPais, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(lblEstado, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        form.add(chkActivo, gbc);

        // Botones
        btnGuardar    = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnDesactivar = new JButton("Desactivar");
        btnLimpiar    = new JButton("Limpiar");

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBtns.add(btnGuardar);
        panelBtns.add(btnActualizar);
        panelBtns.add(btnDesactivar);
        panelBtns.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(panelBtns, gbc);

        // ----- Tabla (derecha) -----
        String[] cols = {"ID", "Nombre", "País", "Estado"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        main.add(form, BorderLayout.WEST);
        main.add(sp, BorderLayout.CENTER);

        form.setPreferredSize(new Dimension(340, getHeight()));

        add(main);

        // Eventos
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnDesactivar.addActionListener(e -> desactivar());
        btnLimpiar.addActionListener(e -> limpiar());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int fila = table.getSelectedRow();
                if (fila >= 0) {
                    editorialSeleccionadaId = (int) table.getValueAt(fila, 0);
                    txtNombre.setText((String) table.getValueAt(fila, 1));
                    txtPais.setText((String) table.getValueAt(fila, 2));
                    String estado = (String) table.getValueAt(fila, 3);
                    chkActivo.setSelected("Activo".equalsIgnoreCase(estado));
                }
            }
        });
    }

    private void cargarTabla() {
        try {
            model.setRowCount(0);
            List<Editorial> lista = editorialDAO.listarTodas();
            for (Editorial e : lista) {
                model.addRow(new Object[]{
                        e.getId(),
                        e.getNombre(),
                        e.getPais() != null ? e.getPais() : "",
                        e.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar editoriales: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        try {
            if (txtNombre.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "El nombre de la editorial es obligatorio.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Editorial e = new Editorial();
            e.setNombre(txtNombre.getText().trim());
            e.setPais(txtPais.getText().trim());
            e.setActivo(chkActivo.isSelected());

            editorialDAO.crear(e);

            JOptionPane.showMessageDialog(this,
                    "Editorial creada correctamente.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
            limpiar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al crear editorial: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizar() {
        if (editorialSeleccionadaId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una editorial en la tabla.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Editorial e = editorialDAO.buscarPorId(editorialSeleccionadaId);
            if (e == null) {
                JOptionPane.showMessageDialog(this,
                        "La editorial ya no existe.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            e.setNombre(txtNombre.getText().trim());
            e.setPais(txtPais.getText().trim());
            e.setActivo(chkActivo.isSelected());

            editorialDAO.actualizar(e);

            JOptionPane.showMessageDialog(this,
                    "Editorial actualizada.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
            limpiar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al actualizar editorial: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivar() {
        if (editorialSeleccionadaId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una editorial en la tabla.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int op = JOptionPane.showConfirmDialog(this,
                    "¿Desea desactivar esta editorial?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op != JOptionPane.YES_OPTION) return;

            editorialDAO.desactivar(editorialSeleccionadaId);

            JOptionPane.showMessageDialog(this,
                    "Editorial desactivada.",
                    "Información", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
            limpiar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al desactivar editorial: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        editorialSeleccionadaId = -1;
        txtNombre.setText("");
        txtPais.setText("");
        chkActivo.setSelected(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EditorialForm().setVisible(true));
    }
}
