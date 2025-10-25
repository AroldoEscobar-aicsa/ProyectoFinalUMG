package app.view;

import app.dao.LibroDAO;
import app.model.Autor;
import app.model.Categoria;
import app.model.Editorial;
import app.model.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibroForm extends JFrame {

    private final LibroDAO dao = new LibroDAO();

    // Campos
    private JTextField txtId, txtTitulo, txtISBN, txtEdicion, txtIdioma;
    private JSpinner spAnio, spStockMin;
    private JComboBox<Editorial> cmbEditorial;
    private JList<Autor> lstAutores;
    private JList<Categoria> lstCategorias;
    private JCheckBox chkActivo;

    // Tabla
    private JTable tabla;
    private DefaultTableModel modelo;

    // Botones
    private JButton btnNuevo, btnGuardar, btnEditar, btnEliminar, btnRefrescar, btnCerrar;

    private boolean modoEdicion = false;

    public LibroForm() {
        setTitle("Gestión de Libros");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        cargarCombosYListas();
        cargarTabla();
    }

    private void initUI() {
        // Panel superior (datos básicos)
        JPanel pDatos = new JPanel(new GridLayout(5, 4, 8, 8));
        pDatos.setBorder(BorderFactory.createTitledBorder("Datos del libro"));

        txtId = new JTextField(); txtId.setEnabled(false);
        txtTitulo = new JTextField();
        txtISBN = new JTextField();
        txtEdicion = new JTextField();
        txtIdioma = new JTextField();

        spAnio = new JSpinner(new SpinnerNumberModel(2000, 0, 3000, 1));
        spStockMin = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        cmbEditorial = new JComboBox<>();
        chkActivo = new JCheckBox("Activo", true);

        pDatos.add(new JLabel("ID:"));           pDatos.add(txtId);
        pDatos.add(new JLabel("Título:"));       pDatos.add(txtTitulo);

        pDatos.add(new JLabel("ISBN:"));         pDatos.add(txtISBN);
        pDatos.add(new JLabel("Edición:"));      pDatos.add(txtEdicion);

        pDatos.add(new JLabel("Año:"));          pDatos.add(spAnio);
        pDatos.add(new JLabel("Idioma:"));       pDatos.add(txtIdioma);

        pDatos.add(new JLabel("Editorial:"));    pDatos.add(cmbEditorial);
        pDatos.add(new JLabel("Stock mínimo:")); pDatos.add(spStockMin);

        pDatos.add(new JLabel("Estado:"));       pDatos.add(chkActivo);
        // relleno
        pDatos.add(new JLabel(""));              pDatos.add(new JLabel(""));

        add(pDatos, BorderLayout.NORTH);

        // Panel central (listas autores / categorías)
        JPanel pListas = new JPanel(new GridLayout(1, 2, 10, 10));
        pListas.setBorder(BorderFactory.createTitledBorder("Relaciones"));

        lstAutores = new JList<>(new DefaultListModel<>());
        lstAutores.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstCategorias = new JList<>(new DefaultListModel<>());
        lstCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel boxAutores = new JPanel(new BorderLayout());
        boxAutores.add(new JLabel("Autores"), BorderLayout.NORTH);
        boxAutores.add(new JScrollPane(lstAutores), BorderLayout.CENTER);

        JPanel boxCategorias = new JPanel(new BorderLayout());
        boxCategorias.add(new JLabel("Categorías"), BorderLayout.NORTH);
        boxCategorias.add(new JScrollPane(lstCategorias), BorderLayout.CENTER);

        pListas.add(boxAutores);
        pListas.add(boxCategorias);

        add(pListas, BorderLayout.CENTER);

        // Panel inferior (tabla + botones)
        JPanel pBottom = new JPanel(new BorderLayout(8, 8));

        String[] cols = {"ID", "Título", "Editorial", "Año", "Idioma", "Autores", "Categorías", "Estado"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) cargarSeleccionado();
            }
        });

        pBottom.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnRefrescar = new JButton("Refrescar");
        btnCerrar = new JButton("Cerrar");
        pBtns.add(btnNuevo); pBtns.add(btnGuardar); pBtns.add(btnEditar);
        pBtns.add(btnEliminar); pBtns.add(btnRefrescar); pBtns.add(btnCerrar);

        pBottom.add(pBtns, BorderLayout.SOUTH);
        add(pBottom, BorderLayout.SOUTH);

        // Eventos
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarLibro());
        btnEditar.addActionListener(e -> cargarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnRefrescar.addActionListener(e -> cargarTabla());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void cargarCombosYListas() {
        try {
            // Editoriales
            cmbEditorial.removeAllItems();
            for (Editorial ed : dao.listarEditorialesActivas()) cmbEditorial.addItem(ed);

            // Autores
            DefaultListModel<Autor> mA = (DefaultListModel<Autor>) lstAutores.getModel();
            mA.removeAllElements();
            for (Autor a : dao.listarAutoresActivos()) mA.addElement(a);

            // Categorías
            DefaultListModel<Categoria> mC = (DefaultListModel<Categoria>) lstCategorias.getModel();
            mC.removeAllElements();
            for (Categoria c : dao.listarCategoriasActivas()) mC.addElement(c);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar combos/listas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            // Para mostrar autores/categorías en tabla, haremos consultas por libro (sencillo y claro)
            for (Libro l : dao.listarTodos()) {
                String autores = joinNombresAutores(l.getId());
                String categorias = joinNombresCategorias(l.getId());
                modelo.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getEditorialNombre(),
                        l.getAnio(),
                        l.getIdioma(),
                        autores,
                        categorias,
                        l.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar libros: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String joinNombresAutores(int idLibro) throws SQLException {
        List<Integer> ids = dao.getAutoresIdsPorLibro(idLibro);
        if (ids.isEmpty()) return "";
        // Reutilizamos el modelo del JList (ya cargado) para mapear id->nombre
        DefaultListModel<Autor> m = (DefaultListModel<Autor>) lstAutores.getModel();
        List<String> nombres = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {
            Autor a = m.getElementAt(i);
            if (ids.contains(a.getId())) nombres.add(a.getNombre());
        }
        return String.join(", ", nombres);
    }

    private String joinNombresCategorias(int idLibro) throws SQLException {
        List<Integer> ids = dao.getCategoriasIdsPorLibro(idLibro);
        if (ids.isEmpty()) return "";
        DefaultListModel<Categoria> m = (DefaultListModel<Categoria>) lstCategorias.getModel();
        List<String> nombres = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {
            Categoria c = m.getElementAt(i);
            if (ids.contains(c.getId())) nombres.add(c.getNombre());
        }
        return String.join(", ", nombres);
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtTitulo.setText("");
        txtISBN.setText("");
        txtEdicion.setText("");
        spAnio.setValue(2000);
        txtIdioma.setText("");
        spStockMin.setValue(1);
        chkActivo.setSelected(true);
        if (cmbEditorial.getItemCount() > 0) cmbEditorial.setSelectedIndex(0);
        lstAutores.clearSelection();
        lstCategorias.clearSelection();
        modoEdicion = false;
        txtTitulo.requestFocus();
    }

    private void guardarLibro() {
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el título.", "Validación", JOptionPane.WARNING_MESSAGE);
            txtTitulo.requestFocus(); return;
        }

        try {
            Libro l = new Libro();
            l.setTitulo(titulo);
            l.setIsbn(txtISBN.getText().trim());
            l.setEdicion(txtEdicion.getText().trim());
            l.setAnio((Integer) spAnio.getValue());
            l.setIdioma(txtIdioma.getText().trim());
            l.setStockMinimo((Integer) spStockMin.getValue());
            l.setActivo(chkActivo.isSelected());

            Editorial selEd = (Editorial) cmbEditorial.getSelectedItem();
            l.setIdEditorial(selEd != null ? selEd.getId() : null);

            // Autores/Categorías seleccionados
            List<Integer> autoresIds = lstAutores.getSelectedValuesList()
                    .stream().map(Autor::getId).collect(Collectors.toList());
            List<Integer> categoriasIds = lstCategorias.getSelectedValuesList()
                    .stream().map(Categoria::getId).collect(Collectors.toList());

            if (modoEdicion) {
                l.setId(Integer.parseInt(txtId.getText()));
                dao.actualizar(l, autoresIds, categoriasIds);
                JOptionPane.showMessageDialog(this, "Libro actualizado correctamente.");
            } else {
                int nuevoId = dao.crear(l, autoresIds, categoriasIds);
                txtId.setText(String.valueOf(nuevoId));
                JOptionPane.showMessageDialog(this, "Libro creado correctamente. ID=" + nuevoId);
                modoEdicion = true;
            }
            cargarTabla();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro en la tabla.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int id = (int) tabla.getValueAt(fila, 0);
            Libro l = dao.buscarPorId(id);
            if (l == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el libro seleccionado.");
                return;
            }
            txtId.setText(String.valueOf(l.getId()));
            txtTitulo.setText(l.getTitulo());
            txtISBN.setText(l.getIsbn());
            txtEdicion.setText(l.getEdicion());
            spAnio.setValue(l.getAnio() == null ? 0 : l.getAnio());
            txtIdioma.setText(l.getIdioma());
            spStockMin.setValue(l.getStockMinimo() == null ? 0 : l.getStockMinimo());
            chkActivo.setSelected(l.isActivo());

            // Editorial
            if (l.getIdEditorial() != null) {
                for (int i = 0; i < cmbEditorial.getItemCount(); i++) {
                    if (cmbEditorial.getItemAt(i).getId() == l.getIdEditorial()) {
                        cmbEditorial.setSelectedIndex(i); break;
                    }
                }
            } else if (cmbEditorial.getItemCount() > 0) {
                cmbEditorial.setSelectedIndex(0);
            }

            // Autores/Categorías seleccionados
            seleccionarAutores(dao.getAutoresIdsPorLibro(id));
            seleccionarCategorias(dao.getCategoriasIdsPorLibro(id));

            modoEdicion = true;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar libro: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarAutores(List<Integer> ids) {
        DefaultListModel<Autor> m = (DefaultListModel<Autor>) lstAutores.getModel();
        int[] indices = ids.stream().mapToInt(id -> {
            for (int i = 0; i < m.size(); i++) if (m.get(i).getId() == id) return i;
            return -1;
        }).filter(x -> x >= 0).toArray();
        lstAutores.setSelectedIndices(indices);
    }

    private void seleccionarCategorias(List<Integer> ids) {
        DefaultListModel<Categoria> m = (DefaultListModel<Categoria>) lstCategorias.getModel();
        int[] indices = ids.stream().mapToInt(id -> {
            for (int i = 0; i < m.size(); i++) if (m.get(i).getId() == id) return i;
            return -1;
        }).filter(x -> x >= 0).toArray();
        lstCategorias.setSelectedIndices(indices);
    }

    private void eliminarLibro() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro para eliminar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tabla.getValueAt(fila, 0);
        int op = JOptionPane.showConfirmDialog(this, "¿Desactivar este libro?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                dao.eliminarLogico(id);
                JOptionPane.showMessageDialog(this, "Libro desactivado.");
                cargarTabla();
                limpiarFormulario();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibroForm().setVisible(true));
    }
}
