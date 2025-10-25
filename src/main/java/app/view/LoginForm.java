package app.view;

import app.core.LoginController;

import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * LoginForm - Ventana de inicio de sesión del sistema.
 * Compatible con LoginController y UsuarioDAO.
 */
public class LoginForm extends JFrame {

    // Componentes
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnCancelar;

    // Envoltura para tooltip cuando el botón está deshabilitado
    private JLayer<JComponent> loginLayer;

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

        // Tamaño y fondo base
        setSize(480, 360);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(0, 0));

        // Encabezado con icono + título
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(24, 24, 8, 24));

        JLabel icono = new JLabel(buildLoginIcon(72));
        icono.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titulo = new JLabel("Inicio de Sesión", SwingConstants.CENTER);
        titulo.setFont(selectFont("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(Color.BLACK);
        titulo.setBorder(new EmptyBorder(12, 0, 0, 0));

        header.add(icono, BorderLayout.NORTH);
        header.add(titulo, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Panel central
        JPanel panelCentral = new JPanel(new GridLayout(2, 2, 12, 12));
        panelCentral.setBorder(new EmptyBorder(16, 24, 16, 24));
        panelCentral.setBackground(Color.WHITE);

        JLabel lUsuario = new JLabel("Usuario:");
        JLabel lPassword = new JLabel("Contraseña:");
        lUsuario.setFont(selectFont("Segoe UI", Font.PLAIN, 14));
        lPassword.setFont(selectFont("Segoe UI", Font.PLAIN, 14));
        lUsuario.setForeground(Color.BLACK);
        lPassword.setForeground(Color.BLACK);

        txtUsuario = new JTextField();
        txtPassword = new JPasswordField();

        // Campos con borde fino y padding interno
        styleTextField(txtUsuario);
        styleTextField(txtPassword);

        panelCentral.add(lUsuario);
        panelCentral.add(txtUsuario);
        panelCentral.add(lPassword);
        panelCentral.add(txtPassword);

        add(panelCentral, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        panelBotones.setBackground(Color.WHITE);

        btnLogin = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");

        // Estilo de botones (redondeados, azul sólido, hover más oscuro)
        styleRoundedButton(btnLogin);
        styleRoundedButton(btnCancelar);

        // Envolver "Ingresar" en JLayer para tooltip cuando está deshabilitado
        loginLayer = new JLayer<>((JComponent) btnLogin);
        loginLayer.setToolTipText("Completa usuario y contraseña para continuar");

        // “Ingresar” deshabilitado hasta que ambos campos tengan texto
        btnLogin.setEnabled(false);
        DocumentListener listener = new DocumentListener() {
            private void checkFields() {
                String usuario  = txtUsuario.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();
                boolean enable = !usuario.isEmpty() && !password.isEmpty();

                btnLogin.setEnabled(enable);
                loginLayer.setToolTipText(enable ? null : "Completa usuario y contraseña para continuar");
                btnLogin.setCursor(enable
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                btnLogin.repaint(); // reflejar "grayed out" al instante
            }
            @Override public void insertUpdate(DocumentEvent e) { checkFields(); }
            @Override public void removeUpdate(DocumentEvent e) { checkFields(); }
            @Override public void changedUpdate(DocumentEvent e) { checkFields(); }
        };
        txtUsuario.getDocument().addDocumentListener(listener);
        txtPassword.getDocument().addDocumentListener(listener);

        panelBotones.add(loginLayer);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        // Fuente global
        applyGlobalFont(this, selectFont("Segoe UI", Font.PLAIN, 14));
    }

    // ===== Getters para el controlador =====
    public JButton getBtnLogin() { return btnLogin; }
    public JButton getBtnCancelar() { return btnCancelar; }
    public JTextField getTxtUsuario() { return txtUsuario; }
    public JPasswordField getTxtPassword() { return txtPassword; }

    // ===== Popups corporativos =====

    /** Popup de bienvenida con estilo corporativo. */
    public void showBienvenida(String usuario) {
        showModernDialog(
                "Bienvenido",
                "¡Hola " + (usuario == null ? "" : usuario) + "!\nAcceso concedido correctamente.",
                DialogType.SUCCESS
        );
    }

    /** Popup de error de credenciales con estilo corporativo. */
    public void showCredencialesInvalidas(String detalle) {
        showModernDialog(
                "Acceso denegado",
                (detalle == null || detalle.isBlank())
                        ? "Usuario o contraseña incorrectos."
                        : detalle,
                DialogType.ERROR
        );
    }

    // ===== Main de prueba =====
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }

    // =======================
    // Estilo / Utilidades
    // =======================

    private static Font selectFont(String preferredName, int style, int size) {
        for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            if (f.getName().equalsIgnoreCase(preferredName)) {
                return new Font(preferredName, style, size);
            }
        }
        return new Font("SansSerif", style, size);
    }

    private static void applyGlobalFont(Component comp, Font font) {
        comp.setFont(font);
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyGlobalFont(child, font);
            }
        }
    }

    private static void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xDDDDDD), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
    }

    /** Botón redondeado con relleno azul claro; hover/press más oscuro; disabled con “grayed out”. */
    private static void styleRoundedButton(JButton btn) {
        btn.setFont(selectFont("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(true);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18)); // padding

        final Color baseColor  = new Color(173, 216, 230); // #ADD8E6
        final Color hoverColor = new Color(158, 205, 222);
        final Color pressColor = new Color(140, 190, 210);
        final Color textDisabled = new Color(60, 60, 60);

        btn.addChangeListener(e -> btn.repaint()); // repintar en cambios de estado

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel m = b.getModel();

                // Color de fondo según estado (si está habilitado)
                Color fill = baseColor;
                if (b.isEnabled()) {
                    if (m.isPressed())      fill = pressColor;
                    else if (m.isRollover()) fill = hoverColor;
                }

                // Fondo redondeado; si está deshabilitado lo atenuamos
                if (!b.isEnabled()) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 50% opacidad
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 20, 20);

                // Texto centrado
                g2.setComposite(AlphaComposite.SrcOver); // texto 100% opaco
                g2.setColor(b.isEnabled() ? b.getForeground() : textDisabled);
                g2.setFont(b.getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = b.getText();
                int x = (b.getWidth() - fm.stringWidth(text)) / 2;
                int y = (b.getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(text, x, y);

                g2.dispose();
            }
        });
    }

    /** Diálogo moderno reutilizable (éxito / error). */
    private void showModernDialog(String title, String message, DialogType type) {
        final Color borderColor = new Color(0xE0E0E0);
        final Color bg = Color.WHITE;

        // Dialog base
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(new LineBorder(borderColor, 1, true));

        JPanel content = new JPanel(new BorderLayout(16, 16)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        content.setBackground(bg);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header (icono + título)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        header.setBackground(bg);

        JLabel icon = new JLabel(type == DialogType.SUCCESS ? buildSuccessIcon(28) : buildErrorIcon(28));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(selectFont("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.BLACK);

        header.add(icon);
        header.add(lblTitle);

        // Mensaje
        JTextArea txt = new JTextArea(message);
        txt.setEditable(false);
        txt.setWrapStyleWord(true);
        txt.setLineWrap(true);
        txt.setOpaque(false);
        txt.setForeground(Color.DARK_GRAY);
        txt.setFont(selectFont("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(null);

        // Botón cerrar/continuar
        JButton btnOk = new JButton(type == DialogType.SUCCESS ? "Continuar" : "Reintentar");
        styleRoundedButton(btnOk);
        btnOk.addActionListener(e -> dialog.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setBackground(bg);
        footer.add(btnOk);

        content.add(header, BorderLayout.NORTH);
        content.add(txt, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setSize(420, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private enum DialogType { SUCCESS, ERROR }

    /** Ícono de éxito (check) en negro. */
    private static Icon buildSuccessIcon(int size) {
        Image img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        int s = size;
        g.setStroke(new BasicStroke(Math.max(2f, s * 0.1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int x1 = (int)(s*0.22), y1 = (int)(s*0.55);
        int x2 = (int)(s*0.42), y2 = (int)(s*0.75);
        int x3 = (int)(s*0.80), y3 = (int)(s*0.28);
        g.drawLine(x1,y1,x2,y2);
        g.drawLine(x2,y2,x3,y3);
        g.dispose();
        return new ImageIcon(img);
    }

    /** Ícono de error (X) en negro. */
    private static Icon buildErrorIcon(int size) {
        Image img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        int s = size;
        g.setStroke(new BasicStroke(Math.max(2f, s * 0.1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int p = (int)(s*0.22), q = s - p;
        g.drawLine(p, p, q, q);
        g.drawLine(q, p, p, q);
        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * Ícono vectorial monocromático (blanco/negro) de usuario con candado.
     */
    private static Icon buildLoginIcon(int size) {
        int w = size, h = size;
        Image img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo circular
        g.setColor(Color.WHITE);
        g.fillOval(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(1, 1, w - 2, h - 2);

        // Cabeza
        int cx = w / 2;
        int cy = (int) (h * 0.36);
        int r = (int) (h * 0.16);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Hombros
        int shoulderW = (int) (w * 0.56);
        int shoulderH = (int) (h * 0.28);
        int shoulderX = cx - shoulderW / 2;
        int shoulderY = (int) (h * 0.50);
        g.fillRoundRect(shoulderX, shoulderY, shoulderW, shoulderH, 24, 24);

        // Candado
        int lockW = (int) (w * 0.34);
        int lockH = (int) (h * 0.28);
        int lockX = w - lockW - (int) (w * 0.08);
        int lockY = h - lockH - (int) (h * 0.08);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawArc(lockX + lockW / 4, lockY - lockH / 4, lockW / 2, lockH / 2, 0, 180);
        g.fillRoundRect(lockX, lockY, lockW, lockH, 8, 8);
        g.setColor(Color.WHITE);
        g.fillOval(lockX + lockW / 2 - 4, lockY + lockH / 2 - 4, 8, 8);

        g.dispose();
        return new ImageIcon(img);
    }
}
