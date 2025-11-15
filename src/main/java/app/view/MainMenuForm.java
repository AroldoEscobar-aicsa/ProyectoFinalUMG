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
    private JButton btnAutores;
    private JButton btnLibros;
    private JButton btnClientes;
    private JButton btnPrestamos;
    private JButton btnUsuarios;
    private JButton btnSalir;
    private JButton btnEditoriales;

    // Nuevos módulos
    private JButton btnReservas;
    private JButton btnMultasPendientes;
    private JButton btnBusquedaLibros;

    private JButton btnCategorias;
    private JButton btnPrestamosDevoluciones;
    private JButton btnInventarioFisico;
    private JButton btnReportesOperativos;

    private JButton btnMultas;
    private JButton btnCajaDiaria;
    private JButton btnRecaudacion;
    private JButton btnReportesFinancieros;
    private JButton btnExoneraciones;

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
        setSize(1000, 600);
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
        // 0 filas y 4 columnas = se acomodan automáticamente en columnas
        JPanel tiles = new JPanel(new GridLayout(0, 4, 16, 16));
        tiles.setBorder(new EmptyBorder(12, 0, 12, 0));
        tiles.setOpaque(false);

        // Botones existentes
        btnAutores   = createTileButton("Autores", "Catálogo de autores", "✍️");
        btnLibros    = createTileButton("Libros", "Gestión de libros", "📚");
        btnClientes  = createTileButton("Clientes", "Lectores / usuarios", "👥");
        btnPrestamos = createTileButton("Préstamos", "Préstamos y devoluciones", "🔁");
        btnUsuarios  = createTileButton("Usuarios", "Administración del sistema", "🛡️");
        btnSalir     = createTileButton("Cerrar Sesión", "Finalizar y volver al login", "🚪");
        btnEditoriales = createTileButton("Editoriales", "Catálogo de editoriales", "🏢");

        // Botones nuevos (Cliente)
        btnReservas          = createTileButton("Reservas", "Gestión de reservas", "📅");
        btnMultasPendientes  = createTileButton("Multas pendientes", "Tus multas por pagar", "⚠️");
        btnBusquedaLibros    = createTileButton("Búsqueda de libros", "Buscar en el catálogo", "🔍");

        // Botones nuevos (Bibliotecario)
        btnCategorias            = createTileButton("Categorías", "Gestión de categorías", "🏷️");
        //btnPrestamosDevoluciones = createTileButton("Préstamos/Devoluciones", "Operaciones de circulación", "🔄");
        btnInventarioFisico      = createTileButton("Inventario físico", "Conteos, ajustes y stock", "📦");
        btnReportesOperativos    = createTileButton("Reportes operativos", "Movimientos diarios", "📊");

        // Botones nuevos (Financiero)
        btnMultas             = createTileButton("Caja diaria", "Gestión de multas, corte y arqueo", "💰");
        //btnCajaDiaria         = createTileButton("Caja diaria", "Corte y arqueo", "🧾");
        btnRecaudacion        = createTileButton("Adquisiciones", "Solicitudes de compra", "💰");
        btnReportesFinancieros= createTileButton("Reportes financieros", "Informes de caja y multas", "📉");
        btnExoneraciones      = createTileButton("Exoneraciones", "Gestión de condonaciones", "✅");

        // Agregamos todos al panel (el permiso los habilita / deshabilita)
        tiles.add(btnAutores);
        tiles.add(btnLibros);
        tiles.add(btnClientes);
        tiles.add(btnPrestamos);
        tiles.add(btnEditoriales);

        tiles.add(btnReservas);
        tiles.add(btnMultasPendientes);
        tiles.add(btnBusquedaLibros);
        tiles.add(btnCategorias);

        //tiles.add(btnPrestamosDevoluciones);
        tiles.add(btnInventarioFisico);
        tiles.add(btnReportesOperativos);
        tiles.add(btnMultas);

        //tiles.add(btnCajaDiaria);
        tiles.add(btnRecaudacion);
        tiles.add(btnReportesFinancieros);
        tiles.add(btnExoneraciones);

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
        String rolRaw = (usuario.getRolPrincipal() != null ? usuario.getRolPrincipal() : "").trim().toUpperCase();

        // Normalizar algunos posibles valores desde BD
        String rol;
        if (rolRaw.startsWith("ADMIN")) {
            rol = "ADMIN";
        } else if (rolRaw.startsWith("BIBLIOT")) {
            rol = "BIBLIOTECARIO";
        } else if (rolRaw.startsWith("FINAN")) {
            rol = "FINANCIERO";
        } else if (rolRaw.startsWith("CLIEN")) {
            rol = "CLIENTE";
        } else {
            rol = rolRaw;
        }

        // Deshabilitar todo por defecto
        btnAutores.setEnabled(false);
        btnLibros.setEnabled(false);
        btnClientes.setEnabled(false);
        btnPrestamos.setEnabled(false);
        btnUsuarios.setEnabled(false);

        btnReservas.setEnabled(false);
        btnMultasPendientes.setEnabled(false);
        btnBusquedaLibros.setEnabled(false);

        btnCategorias.setEnabled(false);
        //btnPrestamosDevoluciones.setEnabled(false);
        btnInventarioFisico.setEnabled(false);
        btnReportesOperativos.setEnabled(false);

        btnMultas.setEnabled(false);
        //btnCajaDiaria.setEnabled(false);
        btnRecaudacion.setEnabled(false);
        btnReportesFinancieros.setEnabled(false);
        btnExoneraciones.setEnabled(false);
        btnEditoriales.setEnabled(false);

        // Siempre puede cerrar sesión
        btnSalir.setEnabled(true);

        switch (rol) {
            case "ADMIN":
                // Administrador ve TODO
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                btnClientes.setEnabled(true);
                btnPrestamos.setEnabled(true);
                btnUsuarios.setEnabled(true);
                btnEditoriales.setEnabled(true);

                btnReservas.setEnabled(true);
                btnMultasPendientes.setEnabled(true);
                btnBusquedaLibros.setEnabled(true);

                btnCategorias.setEnabled(true);
                //btnPrestamosDevoluciones.setEnabled(true);
                btnInventarioFisico.setEnabled(true);
                btnReportesOperativos.setEnabled(true);

                btnMultas.setEnabled(true);
                //btnCajaDiaria.setEnabled(true);
                btnRecaudacion.setEnabled(true);
                btnReportesFinancieros.setEnabled(true);
                btnExoneraciones.setEnabled(true);
                break;

            case "BIBLIOTECARIO":
                // Libros, Autores, Categorías, Préstamos/Devoluciones, Reservas, Inventario físico, Reportes operativos
                btnAutores.setEnabled(true);
                btnLibros.setEnabled(true);
                btnCategorias.setEnabled(true);
                btnPrestamos.setEnabled(true);              // opción general de préstamos
                //btnPrestamosDevoluciones.setEnabled(true);  // botón específico
                btnClientes.setEnabled(true);
                btnReservas.setEnabled(true);
                btnInventarioFisico.setEnabled(true);
                btnReportesOperativos.setEnabled(true);
                btnEditoriales.setEnabled(true);
                btnRecaudacion.setEnabled(true);
                break;

            case "FINANCIERO":
                // Multas, Caja diaria, Recaudación, Reportes financieros, Exoneraciones
                btnMultas.setEnabled(true);
                // btnCajaDiaria.setEnabled(true);
                // btnRecaudacion.setEnabled(true);
                btnReportesFinancieros.setEnabled(true);
                btnExoneraciones.setEnabled(true);
                break;

            case "CLIENTE":
                // Préstamos, Reservas, Multas pendientes, Búsqueda de libros
                btnPrestamos.setEnabled(true);
                btnReservas.setEnabled(true);
                btnMultasPendientes.setEnabled(true);
                btnBusquedaLibros.setEnabled(true);
                break;

            default:
                // Cualquier otro rol raro: no tiene nada, más que cerrar sesión
                break;
        }
    }

    private void configurarEventos() {
        // EXISTENTES

        btnAutores.addActionListener((ActionEvent e) -> {
            try { new AutorForm().setVisible(true); }
            catch (Throwable ex) { showInfo("Módulo de Autores no disponible: " + ex.getMessage()); }
        });

        btnLibros.addActionListener(e -> {
            try { new LibroForm().setVisible(true); }
            catch (Throwable ex) { showError("No se pudo abrir Libros: " + ex.getMessage()); }
        });

        btnClientes.addActionListener(e -> {
            try { new ClienteForm().setVisible(true); }
            catch (Throwable ex) { showError("No se pudo abrir Clientes: " + ex.getMessage()); }
        });

        btnPrestamos.addActionListener(e -> {
            try {
                new PrestamosForm(usuario).setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Préstamos: " + ex.getMessage());
            }
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

        // NUEVOS: por ahora solo mensaje de "no implementado"

        btnReservas.addActionListener(e -> {
            try {
                new ReservasForm(usuario).setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir \"Reservas\": " + ex.getMessage());
            }
        });

        btnMultasPendientes.addActionListener(e -> {
            try {
                new MultasPendientesForm(usuario).setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir \"Multas pendientes\": " + ex.getMessage());
            }
        });

        btnBusquedaLibros.addActionListener(e -> {
            try {
                new BusquedaLibrosForm().setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Categorías: " + ex.getMessage());
            }
        });

        btnCategorias.addActionListener(e -> {
            try {
                new CategoriaForm().setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Categorías: " + ex.getMessage());
            }
        });

        //btnPrestamosDevoluciones.addActionListener(e ->
        //showInfo("Módulo \"Préstamos / Devoluciones\" aún no implementado.")
        //);

        // Antes tenías solo un showInfo("no implementado")
        btnInventarioFisico.addActionListener(e -> {
            try {
                new InventarioFisicoForm(usuario).setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Inventario físico: " + ex.getMessage());
            }
        });

        btnEditoriales.addActionListener(e -> {
            try {
                new EditorialForm().setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Editoriales: " + ex.getMessage());
            }
        });

        btnReportesOperativos.addActionListener(e ->
                showInfo("Módulo \"Reportes operativos\" aún no implementado.")
        );

        btnMultas.addActionListener(e -> {
            try {
                new MultaForm().setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Editoriales: " + ex.getMessage());
            }
        });

        //btnCajaDiaria.addActionListener(e ->
        //showInfo("Módulo \"Caja diaria\" aún no implementado.")
        //);

        btnRecaudacion.addActionListener(e ->
                showInfo("Módulo \"Recaudación\" aún no implementado.")
        );

        btnReportesFinancieros.addActionListener(e ->
                showInfo("Módulo \"Reportes financieros\" aún no implementado.")
        );

        btnExoneraciones.addActionListener(e -> {
            try {
                new ExoneracionesForm().setVisible(true);
            } catch (Throwable ex) {
                showError("No se pudo abrir Editoriales: " + ex.getMessage());
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
