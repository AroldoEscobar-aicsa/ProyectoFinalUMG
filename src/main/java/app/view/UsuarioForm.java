package app.view;

import app.dao.UsuarioDAO;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * UsuarioForm - Formulario para gestionar usuarios y asignar roles
 */
public class UsuarioForm extends JFrame {

    private JTable tableUsuarios;
    private JTextField txtUsername, txtNombre, txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRoles;
    private JButton btnGuardar, btnActualizar, btnEliminar, btnLimpiar;

    private UsuarioDAO usuarioDAO;
    private int usuarioSeleccionadoId = -1;

    // Roles fijos para el proyecto
    private final String[] ROLES = {"Administrador", "Bibliotecario", "Financiero", "Cliente"};

    public UsuarioForm() {
        setTitle("Gestión de Usuarios");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        usuarioDAO = new UsuarioDAO();

        initComponents();
        cargarTablaUsuarios();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel panelFormulario = new JPanel(new GridLayout(6, 2, 10, 10));

        // Campos del formulario
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtNombre = new JTextField();
        txtEmail = new JTextField();
        cmbRoles = new JComboBox<>(ROLES);

        panelFormulario.add(new JLabel("Username:"));
        panelFormulario.add(txtUsername);
        panelFormulario.add(new JLabel("Contraseña:"));
        panelFormulario.add(txtPassword);
        panelFormulario.add(new JLabel("Nombre completo:"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Email:"));
        panelFormulario.add(txtEmail);
        panelFormulario.add(new JLabel("Rol:"));
        panelFormulario.add(cmbRoles);

        btnGuardar = new JButton("Guardar");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        panelFormulario.add(panelBotones);

        // Tabla de usuarios
        tableUsuarios = new JTable();
        JScrollPane scrollPane = new JScrollPane(tableUsuarios);

        panel.add(panelFormulario, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        // Eventos
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        tableUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tableUsuarios.getSelectedRow();
                if (fila >= 0) {
                    usuarioSeleccionadoId = Integer.parseInt(tableUsuarios.getValueAt(fila, 0).toString());
                    txtUsername.setText(tableUsuarios.getValueAt(fila, 1).toString());
                    txtNombre.setText(tableUsuarios.getValueAt(fila, 2).toString());
                    txtEmail.setText(tableUsuarios.getValueAt(fila, 3).toString());
                    cmbRoles.setSelectedIndex(Integer.parseInt(tableUsuarios.getValueAt(fila, 4).toString()) - 1);
                }
            }
        });
    }

    private void cargarTablaUsuarios() {
        try {
            List<Usuario> lista = usuarioDAO.listarTodos();
            String[] columnas = {"ID", "Username", "Nombre completo", "Email", "Rol", "Estado"};
            DefaultTableModel model = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // tabla solo lectura
                }
            };
            for (Usuario u : lista) {
                Object[] fila = {
                        u.getId(),
                        u.getUsername(),
                        u.getNombreCompleto(),
                        u.getEmail(),
                        u.getIdRol(),
                        u.isEstado() ? "Activo" : "Inactivo"
                };
                model.addRow(fila);
            }
            tableUsuarios.setModel(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage());
        }
    }

    private void guardarUsuario() {
        try {
            Usuario u = new Usuario();
            u.setUsername(txtUsername.getText());
            u.setPasswordHash(new String(txtPassword.getPassword())); // luego se debe hashear con BCrypt
            u.setNombreCompleto(txtNombre.getText());
            u.setEmail(txtEmail.getText());
            u.setIdRol(cmbRoles.getSelectedIndex() + 1);
            u.setEstado(true);

            usuarioDAO.crear(u);
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente.");
            cargarTablaUsuarios();
            limpiarFormulario();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear usuario: " + ex.getMessage());
        }
    }

    private void actualizarUsuario() {
        if (usuarioSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.");
            return;
        }
        try {
            Usuario u = usuarioDAO.buscarPorId(usuarioSeleccionadoId);
            u.setUsername(txtUsername.getText());
            String pass = new String(txtPassword.getPassword());
            if (!pass.isEmpty()) {
                u.setPasswordHash(pass); // hashear con BCrypt
            }
            u.setNombreCompleto(txtNombre.getText());
            u.setEmail(txtEmail.getText());
            u.setIdRol(cmbRoles.getSelectedIndex() + 1);

            usuarioDAO.actualizar(u);
            JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.");
            cargarTablaUsuarios();
            limpiarFormulario();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar usuario: " + ex.getMessage());
        }
    }

    private void eliminarUsuario() {
        if (usuarioSeleccionadoId == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.");
            return;
        }
        try {
            int opcion = JOptionPane.showConfirmDialog(this, "¿Seguro desea desactivar el usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) {
                usuarioDAO.eliminarLogico(usuarioSeleccionadoId);
                JOptionPane.showMessageDialog(this, "Usuario desactivado correctamente.");
                cargarTablaUsuarios();
                limpiarFormulario();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar usuario: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtNombre.setText("");
        txtEmail.setText("");
        cmbRoles.setSelectedIndex(0);
        usuarioSeleccionadoId = -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UsuarioForm().setVisible(true);
        });
    }
}
