package app.view;

import app.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * MainMenuForm
 * Ventana principal del sistema (menú general).
 * Muestra las opciones según el rol del usuario.
 */
public class MainMenuForm extends JFrame {

    private final Usuario usuario;
    private JLabel lblBienvenida;
    private JButton btnAutores, btnLibros, btnClientes, btnPrestamos, btnUsuarios, btnSalir;

    public MainMenuForm(Usuario usuario) {
        this.usuario = usuario;
        inicializarComponentes();
        configurarEventos();
    }

    // ====== Inicialización del menú ======
    private void inicializarComponentes() {
        setTitle("Menú Principal - " + usuario.getNombreCompleto());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior (bienvenida)
        JPanel panelSuperior = new JPanel();
        lblBienvenida = new JLabel("Bienvenido, " + usuario.getNombreCompleto() + " (" + usuario.getUsername() + ")");
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelSuperior.add(lblBienvenida);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel central con botones
        JPanel panelCentral = new JPanel(new GridLayout(2, 3, 15, 15));
        panelCentral.setBorder(BorderFactory.createTitledBorder("Opciones del Sistema"));

        btnAutores = new JButton("Autores");
        btnLibros = new JButton("Libros");
        btnClientes = new JButton("Clientes");
        btnPrestamos = new JButton("Préstamos");
        btnUsuarios = new JButton("Usuarios");
        btnSalir = new JButton("Cerrar Sesión");

        panelCentral.add(btnAutores);
        panelCentral.add(btnLibros);
        panelCentral.add(btnClientes);
        panelCentral.add(btnPrestamos);
        panelCentral.add(btnUsuarios);
        panelCentral.add(btnSalir);

        add(panelCentral, BorderLayout.CENTER);

        // Configurar visibilidad según rol
        configurarPermisos();
    }

    // ====== Permisos por rol ======
    private void configurarPermisos() {
        int rol = usuario.getIdRol();

        /*
         * Ejemplo de roles:
         * 1 = ADMIN (todo habilitado)
         * 2 = OPERADOR (solo autores, libros, clientes, préstamos)
         * 3 = INVITADO (solo consulta de autores)
         */

        switch (rol) {
            case 1: // ADMIN
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                btnClientes.setEnabled(true);
                btnPrestamos.setEnabled(true);
                btnUsuarios.setEnabled(true);
                break;

            case 2: // OPERADOR
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                btnClientes.setEnabled(true);
                btnPrestamos.setEnabled(true);
                btnUsuarios.setEnabled(false);
                break;

            case 3: // INVITADO
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(false);
                btnClientes.setEnabled(false);
                btnPrestamos.setEnabled(false);
                btnUsuarios.setEnabled(false);
                break;

            default:
                // Si no se reconoce el rol, se bloquean las opciones críticas
                btnUsuarios.setEnabled(false);
                btnLibros.setEnabled(false);
                btnPrestamos.setEnabled(false);
                btnClientes.setEnabled(false);
        }
    }

    // ====== Eventos ======
    private void configurarEventos() {

        btnAutores.addActionListener((ActionEvent e) -> new AutorForm().setVisible(true));

        btnLibros.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Módulo de Libros aún no implementado.", "Info", JOptionPane.INFORMATION_MESSAGE));

        btnClientes.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Módulo de Clientes aún no implementado.", "Info", JOptionPane.INFORMATION_MESSAGE));

        btnPrestamos.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Módulo de Préstamos aún no implementado.", "Info", JOptionPane.INFORMATION_MESSAGE));

        btnUsuarios.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Módulo de Usuarios aún no implementado.", "Info", JOptionPane.INFORMATION_MESSAGE));

        btnSalir.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Desea cerrar sesión?", "Confirmar salida",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (respuesta == JOptionPane.YES_OPTION) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });
    }

    // ====== Main de prueba ======
    public static void main(String[] args) {
        Usuario testUser = new Usuario(1, "admin", "1234", 1, "Administrador", "admin@correo.com", true);
        SwingUtilities.invokeLater(() -> new MainMenuForm(testUser).setVisible(true));
    }
}
