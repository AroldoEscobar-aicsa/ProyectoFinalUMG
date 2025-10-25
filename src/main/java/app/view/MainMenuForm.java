package app.view;

import app.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainMenuForm extends JFrame {

    private final Usuario usuario;

    // Tiles/botones
    private JButton btnAutores, btnLibros, btnClientes, btnPrestamos, btnUsuarios, btnSalir;

    // Barra superior e inferior
    private JLabel lblBienvenida, lblRol;
    private JLabel lblStatus;

    public MainMenuForm(Usuario usuario) {
        this.usuario = usuario;
        initLaf();
        inicializarComponentes();
        configurarPermisos();
        configurarEventos();
    }

    private void initLaf() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}
    }

    private void inicializarComponentes() {
        setTitle("Menú Principal - " + (usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : usuario.getUsername()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        String rolTxt = usuario.getRolPrincipal() != null ? usuario.getRolPrincipal() : "(sin rol)";
        lblBienvenida = new JLabel("Bienvenido, " + (usuario.getNombreCompleto() != null ? usuario.getNombreCompleto() : usuario.getUsername()));
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBienvenida.setBorder(new EmptyBorder(4, 0, 4, 0));

        lblRol = new JLabel("Rol: " + rolTxt);
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(new Color(90, 90, 90));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        headerText.add(lblBienvenida);
        headerText.add(lblRol);

        header.add(headerText, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Tiles center
        JPanel tiles = new JPanel(new GridLayout(2, 3, 16, 16));
        tiles.setBorder(new EmptyBorder(12, 0, 12, 0));
        tiles.setOpaque(false);

        btnAutores   = createTileButton("Autores", "Catálogo de autores", "✍️");
        btnLibros    = createTileButton("Libros", "Gestión de libros", "📚");
        btnClientes  = createTileButton("Clientes", "Lectores / usuarios", "👥");
        btnPrestamos = createTileButton("Préstamos", "Préstamos y devoluciones", "🔁");
        btnUsuarios  = createTileButton("Usuarios", "Administración del sistema", "🛡️");
        btnSalir     = createTileButton("Cerrar Sesión", "Finalizar y volver al login", "🚪");

        tiles.add(btnAutores);
        tiles.add(btnLibros);
        tiles.add(btnClientes);
        tiles.add(btnPrestamos);
        tiles.add(btnUsuarios);
        tiles.add(btnSalir);

        add(tiles, BorderLayout.CENTER);

        // Status bar
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        lblStatus = new JLabel(buildStatusText());
        lblStatus.setBorder(new EmptyBorder(6, 4, 6, 4));
        lblStatus.setForeground(new Color(100, 100, 100));
        status.add(lblStatus, BorderLayout.WEST);

        add(status, BorderLayout.SOUTH);
    }

    private String buildStatusText() {
        String user = usuario.getUsername() != null ? usuario.getUsername() : "";
        String rol = usuario.getRolPrincipal() != null ? usuario.getRolPrincipal() : "(sin rol)";
        String dt  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return "Usuario: " + user + "   |   Rol: " + rol + "   |   " + dt;
    }

    private JButton createTileButton(String title, String subtitle, String emoji) {
        JButton b = new JButton("<html><div style='text-align:center;'>" +
                "<div style='font-size:24px;'>" + emoji + "</div>" +
                "<div style='font-size:15px; font-weight:bold; margin-top:4px;'>" + title + "</div>" +
                "<div style='font-size:12px; color:#666; margin-top:2px;'>" + subtitle + "</div>" +
                "</div></html>");
        b.setFocusPainted(false);
        b.setBackground(new Color(245, 247, 250));
        b.setBorder(new MatteBorder(1, 1, 1, 1, new Color(230, 230, 230)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(200, 120));
        b.setOpaque(true);
        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(new Color(238, 242, 248));
            else b.setBackground(new Color(245, 247, 250));
        });
        return b;
    }

    private void configurarPermisos() {
        String rol = (usuario.getRolPrincipal() != null ? usuario.getRolPrincipal() : "").trim().toUpperCase();

        btnAutores.setEnabled(false);
        btnLibros.setEnabled(false);
        btnClientes.setEnabled(false);
        btnPrestamos.setEnabled(false);
        btnUsuarios.setEnabled(false);

        switch (rol) {
            case "ADMIN":
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                btnClientes.setEnabled(true);
                btnPrestamos.setEnabled(true);
                btnUsuarios.setEnabled(true);
                break;
            case "BIBLIOTECARIO":
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                btnClientes.setEnabled(true);
                btnPrestamos.setEnabled(true);
                break;
            case "FINANCIERO":
                btnLibros.setEnabled(true);
                btnClientes.setEnabled(true);
                break;
            case "CLIENTE":
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                break;
            default:
                btnAutores.setEnabled(true);
        }
    }

    private void configurarEventos() {
        btnAutores.addActionListener((ActionEvent e) -> {
            try { new AutorForm().setVisible(true); }
            catch (Throwable ex) { showInfo("Módulo de Autores no disponible: " + ex.getMessage()); }
        });

        btnLibros.addActionListener(e -> {
            try { new LibroForm().setVisible(true); }
            catch (Throwable ex) { showError("No se pudo abrir Libros: " + ex.getMessage()); }
        });

        // >>> INTEGRACIÓN DEL MÓDULO DE CLIENTES <<<
        btnClientes.addActionListener(e -> {
            try { new ClienteForm().setVisible(true); }
            catch (Throwable ex) { showError("No se pudo abrir Clientes: " + ex.getMessage()); }
        });

        btnPrestamos.addActionListener(e -> {
            try { new PrestamosForm().setVisible(true); }
            catch (Throwable ex) { showError("No se pudo abrir Préstamos: " + ex.getMessage()); }
        });

        btnUsuarios.addActionListener(e -> {
            try { new UsuarioForm().setVisible(true); }
            catch (Throwable ex) { showError("No se pudo abrir Usuarios: " + ex.getMessage()); }
        });

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

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        Usuario u = new Usuario();
        u.setId(1);
        u.setUsername("admin");
        u.setNombreCompleto("Administrador General");
        u.setEmail("admin@correo.com");
        u.setRolPrincipalId(1);
        u.setRolPrincipal("ADMIN");
        u.setActivo(true);

        SwingUtilities.invokeLater(() -> new MainMenuForm(u).setVisible(true));
    }
}
