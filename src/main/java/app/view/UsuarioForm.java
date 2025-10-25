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
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        cargarRoles();
        cargarTablaUsuarios();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre = new JTextField();
        txtEmail = new JTextField();
        txtTelefono = new JTextField();
        cmbRoles = new JComboBox<>();
        chkActivo = new JCheckBox("Activo", true);

        form.add(new JLabel("Username:"));     form.add(txtUsername);
        form.add(new JLabel("Contraseña:"));   form.add(txtPassword);
        form.add(new JLabel("Nombre completo:")); form.add(txtNombre);
        form.add(new JLabel("Email:"));        form.add(txtEmail);
        form.add(new JLabel("Teléfono:"));     form.add(txtTelefono);
        form.add(new JLabel("Rol principal:"));form.add(cmbRoles);
        form.add(new JLabel("Estado:"));       form.add(chkActivo);

        btnGuardar = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Desactivar");
        btnLimpiar = new JButton("Limpiar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(btnGuardar);
        acciones.add(btnActualizar);
        acciones.add(btnEliminar);
        acciones.add(btnLimpiar);

        form.add(new JLabel()); form.add(acciones);

        tableUsuarios = new JTable();
        JScrollPane sp = new JScrollPane(tableUsuarios);

        panel.add(form, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        add(panel);

        // Eventos
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        tableUsuarios.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
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
    }

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
            if (u == null) { JOptionPane.showMessageDialog(this, "Usuario no encontrado."); return; }

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
