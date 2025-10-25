package app.view;

import app.core.LoginController;

import javax.swing.*;
import java.awt.*;

/**
 * LoginForm - Ventana de inicio de sesión del sistema.
 * Compatible con LoginController y UsuarioDAO.
 */
public class LoginForm extends JFrame {

    // Componentes
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnCancelar;

    // Controlador
    private LoginController controller;

    public LoginForm() {
        inicializarComponentes();
        setLocationRelativeTo(null); // Centrar ventana
        controller = new LoginController(this); // Vincula el controlador
    }

    private void inicializarComponentes() {
        setTitle("Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLayout(new BorderLayout(10, 10));

        // Panel principal
        JPanel panelCentral = new JPanel(new GridLayout(2, 2, 10, 10));
        panelCentral.setBorder(BorderFactory.createTitledBorder("Ingrese sus credenciales"));

        panelCentral.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField();
        panelCentral.add(txtUsuario);

        panelCentral.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelCentral.add(txtPassword);

        add(panelCentral, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnLogin = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");

        panelBotones.add(btnLogin);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);
    }

    // ===== Getters para el controlador =====
    public JButton getBtnLogin() { return btnLogin; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JTextField getTxtUsuario() { return txtUsuario; }
    public JPasswordField getTxtPassword() { return txtPassword; }

    // ===== Main de prueba =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
