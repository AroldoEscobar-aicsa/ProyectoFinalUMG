package app.view;

import app.dao.LibroDAO;
import app.model.LibroBusqueda;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BusquedaLibrosForm extends JFrame {

    private JTextField txtBusqueda;
    private JCheckBox chkSoloDisponibles;
    private JTable tblLibros;
    private DefaultTableModel modelo;
    private JButton btnBuscar;
    private JButton btnSeleccionar;
    private JButton btnCerrar;

    private final LibroDAO libroDAO = new LibroDAO();

    private LibroBusqueda libroSeleccionado;

    // Constructor sin parámetros (el que estás usando en el botón)
    public BusquedaLibrosForm() {
        this(null);
    }

    // Constructor opcional con owner por si quieres centrar sobre otra ventana
    public BusquedaLibrosForm(Frame owner) {
        super("Búsqueda de libros");
        initUI();
        setSize(900, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner); // si owner es null, centra en la pantalla
    }

    private void initUI() {
        JPanel pnlTop = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        txtBusqueda = new JTextField(30);
        chkSoloDisponibles = new JCheckBox("Solo disponibles");
        chkSoloDisponibles.setSelected(true);
        btnBuscar = new JButton("Buscar");

        g.gridx = 0; g.gridy = 0;
        pnlTop.add(new JLabel("Buscar (título, autor, categoría, ISBN):"), g);

        g.gridx = 1; g.gridy = 0;
        pnlTop.add(txtBusqueda, g);

        g.gridx = 2; g.gridy = 0;
        pnlTop.add(chkSoloDisponibles, g);

        g.gridx = 3; g.gridy = 0;
        pnlTop.add(btnBuscar, g);

        // Tabla
        modelo = new DefaultTableModel(
                new Object[]{"Id", "Título", "Autores", "Categorías", "Editorial", "Año", "Disp.", "Total"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLibros = new JTable(modelo);
        tblLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLibros.getColumnModel().getColumn(0).setPreferredWidth(40);  // Id
        tblLibros.getColumnModel().getColumn(1).setPreferredWidth(220); // Título
        tblLibros.getColumnModel().getColumn(2).setPreferredWidth(180); // Autores
        tblLibros.getColumnModel().getColumn(3).setPreferredWidth(150); // Categorías

        JScrollPane scroll = new JScrollPane(tblLibros);

        // Botones inferiores
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSeleccionar = new JButton("Seleccionar");
        btnCerrar = new JButton("Cerrar");
        pnlBottom.add(btnSeleccionar);
        pnlBottom.add(btnCerrar);

        // Layout principal
        setLayout(new BorderLayout());
        add(pnlTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);

        // Eventos
        btnBuscar.addActionListener(e -> realizarBusqueda());
        btnCerrar.addActionListener(e -> dispose());

        btnSeleccionar.addActionListener(e -> seleccionarLibro());

        // Doble clic en la tabla para seleccionar
        tblLibros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblLibros.getSelectedRow() != -1) {
                    seleccionarLibro();
                }
            }
        });
    }

    private void realizarBusqueda() {
        String texto = txtBusqueda.getText();
        boolean soloDisp = chkSoloDisponibles.isSelected();

        modelo.setRowCount(0);

        try {
            List<LibroBusqueda> resultados = libroDAO.buscarLibrosCatalogo(texto, soloDisp);
            for (LibroBusqueda l : resultados) {
                Object[] fila = {
                        l.getId(),
                        l.getTitulo(),
                        l.getAutores(),
                        l.getCategorias(),
                        l.getEditorial(),
                        l.getAnio(),
                        l.getCopiasDisponibles(),
                        l.getCopiasTotales()
                };
                modelo.addRow(fila);
            }

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron libros que coincidan con el criterio.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al buscar libros:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarLibro() {
        int fila = tblLibros.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un libro de la lista.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        libroSeleccionado = new LibroBusqueda();
        libroSeleccionado.setId((int) tblLibros.getValueAt(fila, 0));
        libroSeleccionado.setTitulo((String) tblLibros.getValueAt(fila, 1));
        libroSeleccionado.setAutores((String) tblLibros.getValueAt(fila, 2));
        libroSeleccionado.setCategorias((String) tblLibros.getValueAt(fila, 3));
        libroSeleccionado.setEditorial((String) tblLibros.getValueAt(fila, 4));
        Object anioObj = tblLibros.getValueAt(fila, 5);
        if (anioObj instanceof Integer) {
            libroSeleccionado.setAnio((Integer) anioObj);
        } else if (anioObj != null) {
            try {
                libroSeleccionado.setAnio(Integer.parseInt(anioObj.toString()));
            } catch (NumberFormatException ignore) {}
        }
        libroSeleccionado.setCopiasDisponibles((int) tblLibros.getValueAt(fila, 6));
        libroSeleccionado.setCopiasTotales((int) tblLibros.getValueAt(fila, 7));

        dispose();
    }

    public LibroBusqueda getLibroSeleccionado() {
        return libroSeleccionado;
    }
}
