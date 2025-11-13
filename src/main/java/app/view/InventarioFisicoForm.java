package app.view;

import app.dao.InventarioDAO;
import app.model.InventarioConteoDetalle;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventarioFisicoForm extends JFrame {

    private final Usuario usuarioActual;
    private final InventarioDAO inventarioDAO = new InventarioDAO();

    private JTextField txtUsuario;
    private JTextArea txtComentario;
    private JTable table;
    private DefaultTableModel tableModel;

    private JButton btnCargarLibros;
    private JButton btnGuardarConteo;
    private JButton btnCerrar;

    public InventarioFisicoForm(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;

        setTitle("Inventario físico de libros");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(10, 10));

        // ----- Panel izquierdo (datos del conteo) -----
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createTitledBorder("Datos del conteo"));

        JPanel datos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUsuario = new JLabel("Usuario:");
        txtUsuario = new JTextField();
        txtUsuario.setEditable(false);
        txtUsuario.setText(usuarioActual != null ? usuarioActual.getUsername() : "");

        JLabel lblComentario = new JLabel("Comentario:");
        txtComentario = new JTextArea(5, 20);
        txtComentario.setLineWrap(true);
        txtComentario.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        datos.add(lblUsuario, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        datos.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        datos.add(lblComentario, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        datos.add(new JScrollPane(txtComentario), gbc);

        left.add(datos, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCargarLibros = new JButton("Cargar libros");
        btnGuardarConteo = new JButton("Guardar conteo");
        btnCerrar = new JButton("Cerrar");

        acciones.add(btnCargarLibros);
        acciones.add(btnGuardarConteo);
        acciones.add(btnCerrar);

        left.add(acciones, BorderLayout.SOUTH);

        // ----- Tabla derecha -----
        String[] cols = {"IdLibro", "Título", "Cant. sistema", "Cant. física", "Diferencia"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo se edita la cantidad física
                return column == 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3 || columnIndex == 4) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane spTable = new JScrollPane(table);

        // recalcular diferencia cuando cambie la cantidad física
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() != TableModelEvent.UPDATE) return;
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 3 && row >= 0) {
                    recalcularDiferencia(row);
                }
            }
        });

        main.add(left, BorderLayout.WEST);
        main.add(spTable, BorderLayout.CENTER);

        // Ajustar ancho del panel izquierdo
        left.setPreferredSize(new Dimension(320, getHeight()));

        add(main);

        // Eventos
        btnCargarLibros.addActionListener(e -> cargarLibrosParaConteo());
        btnGuardarConteo.addActionListener(e -> guardarConteo());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void cargarLibrosParaConteo() {
        try {
            tableModel.setRowCount(0);
            List<InventarioConteoDetalle> lista = inventarioDAO.prepararDetalleParaConteo();
            for (InventarioConteoDetalle d : lista) {
                tableModel.addRow(new Object[]{
                        d.getIdLibro(),
                        d.getTituloLibro(),
                        d.getCantidadSistema(),
                        d.getCantidadFisica(),
                        d.getDiferencia()
                });
            }
            JOptionPane.showMessageDialog(this, "Libros cargados para conteo.", "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar libros: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalcularDiferencia(int row) {
        try {
            Integer cantSistema = (Integer) tableModel.getValueAt(row, 2);
            Object fisObj = tableModel.getValueAt(row, 3);
            int cantFisica = 0;
            if (fisObj != null) {
                if (fisObj instanceof Integer) {
                    cantFisica = (Integer) fisObj;
                } else {
                    cantFisica = Integer.parseInt(fisObj.toString());
                }
            }
            int dif = cantFisica - (cantSistema != null ? cantSistema : 0);
            tableModel.setValueAt(dif, row, 4);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad física inválida en fila " + (row + 1),
                    "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void guardarConteo() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Primero cargue los libros para conteo.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (usuarioActual == null || usuarioActual.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Usuario no válido para registrar el conteo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(this,
                "¿Guardar el conteo de inventario?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (op != JOptionPane.YES_OPTION) return;

        try {
            String comentario = txtComentario.getText();
            int idConteo = inventarioDAO.crearConteo(usuarioActual.getId(), comentario);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idLibro = (Integer) tableModel.getValueAt(i, 0);
                Object fisObj = tableModel.getValueAt(i, 3);
                int cantFisica = 0;
                if (fisObj != null) {
                    if (fisObj instanceof Integer) cantFisica = (Integer) fisObj;
                    else cantFisica = Integer.parseInt(fisObj.toString());
                }
                inventarioDAO.insertarDetalleConteo(idConteo, idLibro, cantFisica);
            }

            JOptionPane.showMessageDialog(this,
                    "Conteo guardado correctamente con Id #" + idConteo,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el conteo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // demo
        app.model.Usuario u = new app.model.Usuario();
        u.setId(1);
        u.setUsername("admin");
        javax.swing.SwingUtilities.invokeLater(() ->
                new InventarioFisicoForm(u).setVisible(true));
    }
}
