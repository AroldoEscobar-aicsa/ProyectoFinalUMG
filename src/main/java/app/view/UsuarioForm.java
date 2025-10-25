package app.view;

import app.dao.UsuarioDAO;
import app.model.Rol;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class UsuarioForm extends JFrame {

    private JTable tableUsuarios;
    private JTextField txtUsername, txtNombre, txtEmail, txtTelefono;
    private JPasswordField txtPassword;
    private JComboBox<Rol> cmbRoles;
    private JCheckBox chkActivo;
    private JButton btnGuardar, btnActualizar, btnEliminar, btnLimpiar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private int usuarioSeleccionadoId = -1;

    public UsuarioForm() {
        setTitle("Gestión de Usuarios");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarRoles();
        cargarTablaUsuarios();
    }

    private void initComponents() {
        // Layout general
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JLabel lblTitulo = new JLabel("Gestión de Usuarios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        root.add(lblTitulo, BorderLayout.NORTH);

        // Panel izquierdo (formulario)
        JPanel panelForm = crearPanelFormulario();

        // Panel derecho (tabla)
        JPanel panelTabla = crearPanelTabla();

        // SplitPane para separar form y tabla
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelForm, panelTabla);
        split.setResizeWeight(0.35); // 35% form, 65% tabla
        split.setBorder(null);

        root.add(split, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel crearPanelFormulario() {
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre = new JTextField();
        txtEmail = new JTextField();
        txtTelefono = new JTextField();
        cmbRoles = new JComboBox<>();
        chkActivo = new JCheckBox("Activo", true);

        // Helper para agregar campos
        agregarCampo(form, gbc, "Username:", txtUsername);
        agregarCampo(form, gbc, "Contraseña:", txtPassword);
        agregarCampo(form, gbc, "Nombre completo:", txtNombre);
        agregarCampo(form, gbc, "Email:", txtEmail);
        agregarCampo(form, gbc, "Teléfono:", txtTelefono);
        agregarCampo(form, gbc, "Rol principal:", cmbRoles);

        // Estado
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        form.add(chkActivo, gbc);

        // Panel de botones
        btnGuardar = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Desactivar");
        btnLimpiar = new JButton("Limpiar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        acciones.add(btnGuardar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnLimpiar);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(acciones, gbc);

        formWrapper.add(form, BorderLayout.NORTH);

        // Eventos
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        return formWrapper;
    }

    private void agregarCampo(JPanel form, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridx = 0;
        form.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(field, gbc);
        gbc.gridy++;
        gbc.weightx = 0;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Lista de usuarios"));

        tableUsuarios = new JTable();
        tableUsuarios.setFillsViewportHeight(true);
        tableUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(tableUsuarios);

        // Pequeño texto arriba de la tabla
        JLabel lblInfo = new JLabel("Doble clic o clic en la tabla para editar un usuario.");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 2));

        panel.add(lblInfo, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        // Evento de selección
        tableUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tableUsuarios.getSelectedRow();
                if (fila >= 0) {
                    usuarioSeleccionadoId = (int) tableUsuarios.getValueAt(fila, 0);
                    txtUsername.setText((String) tableUsuarios.getValueAt(fila, 1));
                    txtNombre.setText((String) tableUsuarios.getValueAt(fila, 2));
                    txtEmail.setText((String) tableUsuarios.getValueAt(fila, 3));
                    txtTelefono.setText((String) tableUsuarios.getValueAt(fila, 4));
                    String rolNombre = (String) tableUsuarios.getValueAt(fila, 5);
                    seleccionarRolEnCombo(rolNombre);
                    chkActivo.setSelected("Activo".equals(tableUsuarios.getValueAt(fila, 6)));
                    txtPassword.setText(""); // nunca mostramos passwords
                }
            }
        });

        return panel;
    }

    // ============ LÓGICA ============

    private void cargarRoles() {
        try {
            cmbRoles.removeAllItems();
            List<Rol> roles = usuarioDAO.listarRoles();
            for (Rol r : roles) cmbRoles.addItem(r);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTablaUsuarios() {
        try {
            List<Usuario> lista = usuarioDAO.listarTodos();
            String[] cols = {"ID", "Username", "Nombre completo", "Email", "Teléfono", "Rol", "Estado"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Usuario u : lista) {
                model.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getNombreCompleto(),
                        u.getEmail(),
                        u.getTelefono(),
                        u.getRolPrincipal() != null ? u.getRolPrincipal() : "(sin rol)",
                        u.isActivo() ? "Activo" : "Inactivo"
                });
            }
            tableUsuarios.setModel(model);
            // ancho sugerido
            tableUsuarios.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
            tableUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100); // Username
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarUsuario() {
        try {
            if (txtUsername.getText().isBlank() || txtNombre.getText().isBlank()
                    || txtEmail.getText().isBlank() || txtPassword.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Completa Username, Nombre, Email y Contraseña.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario u = new Usuario();
            u.setUsername(txtUsername.getText().trim());
            u.setNombreCompleto(txtNombre.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setTelefono(txtTelefono.getText().trim());
            u.setActivo(chkActivo.isSelected());

            Rol r = (Rol) cmbRoles.getSelectedItem();
            if (r != null) u.setRolPrincipalId(r.getId());

            String passwordPlano = new String(txtPassword.getPassword()); // BCrypt en DAO
            usuarioDAO.crear(u, passwordPlano);

            JOptionPane.showMessageDialog(this, "Usuario creado correctamente.");
            cargarTablaUsuarios();
            limpiarFormulario();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarUsuario() {
        if (usuarioSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario en la tabla.");
            return;
        }
        try {
            Usuario u = usuarioDAO.buscarPorId(usuarioSeleccionadoId);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                return;
            }

            u.setUsername(txtUsername.getText().trim());
            u.setNombreCompleto(txtNombre.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setTelefono(txtTelefono.getText().trim());
            u.setActivo(chkActivo.isSelected());

            Rol r = (Rol) cmbRoles.getSelectedItem();
            if (r != null) u.setRolPrincipalId(r.getId());

            // Si el campo password no está vacío, actualiza el hash
            String pass = new String(txtPassword.getPassword());
            if (!pass.isBlank()) {
                usuarioDAO.actualizarPasswordPlano(u.getId(), pass); // BCrypt
            }
            usuarioDAO.actualizar(u);

            JOptionPane.showMessageDialog(this, "Usuario actualizado.");
            cargarTablaUsuarios();
            limpiarFormulario();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuario() {
        if (usuarioSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario en la tabla.");
            return;
        }
        try {
            int op = JOptionPane.showConfirmDialog(this, "¿Desactivar este usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                usuarioDAO.eliminarLogico(usuarioSeleccionadoId);
                JOptionPane.showMessageDialog(this, "Usuario desactivado.");
                cargarTablaUsuarios();
                limpiarFormulario();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        usuarioSeleccionadoId = -1;
        txtUsername.setText("");
        txtPassword.setText("");
        txtNombre.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        chkActivo.setSelected(true);
        if (cmbRoles.getItemCount() > 0) cmbRoles.setSelectedIndex(0);
    }

    private void seleccionarRolEnCombo(String rolNombre) {
        if (rolNombre == null) return;
        ComboBoxModel<Rol> m = cmbRoles.getModel();
        for (int i = 0; i < m.getSize(); i++) {
            Rol r = m.getElementAt(i);
            if (r.getNombre().equalsIgnoreCase(rolNombre)) {
                cmbRoles.setSelectedIndex(i);
                return;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioForm().setVisible(true));
    }
}
