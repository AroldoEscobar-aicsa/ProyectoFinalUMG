package app.core;

import app.dao.UsuarioDAO;
import app.model.Usuario;
import app.view.LoginForm;
import app.view.MainMenuForm;

import javax.swing.*;

/**
 * LoginController - Controla la lógica del inicio de sesión.
 * Valida las credenciales y abre el menú principal.
 */
public class LoginController {

    private final LoginForm vista;
    private final UsuarioDAO usuarioDAO;

    public LoginController(LoginForm vista) {
        this.vista = vista;
        this.usuarioDAO = new UsuarioDAO();
        inicializarEventos();
    }

    private void inicializarEventos() {
        vista.getBtnLogin().addActionListener(e -> autenticarUsuario());
        vista.getBtnCancelar().addActionListener(e -> System.exit(0));
    }

    // ===== AUTENTICACIÓN =====
    private void autenticarUsuario() {
        String username = vista.getTxtUsuario().getText().trim();
        String password = new String(vista.getTxtPassword().getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Debe ingresar usuario y contraseña.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(username, password);

            if (usuario != null) {
                String rol = usuario.getRolPrincipal() != null ? " - " + usuario.getRolPrincipal() : "";
                JOptionPane.showMessageDialog(vista,
                        "Bienvenido, " + usuario.getNombreCompleto() + rol + "!",
                        "Acceso permitido",
                        JOptionPane.INFORMATION_MESSAGE);
                vista.dispose();
                new MainMenuForm(usuario).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(vista,
                        "Usuario o contraseña incorrectos.",
                        "Acceso denegado",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista,
                    "Error al autenticar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
