package app.view;

import app.dao.LibroDAO;
import app.model.Autor;
import app.model.Categoria;
import app.model.Editorial;
import app.model.Libro;
import app.model.LibroBusqueda;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
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

    // Buscador
    private JTextField txtBuscar;
    private JCheckBox chkSoloDisponibles;

    // Tabla
    private JTable tabla;
    private DefaultTableModel modelo;

    // Botones
    private JButton btnNuevo, btnGuardar, btnEditar, btnEliminar, btnRefrescar, btnCerrar;
    private JButton btnBuscar;

    private boolean modoEdicion = false;

    public LibroForm() {
        setTitle("Gestión de Libros");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();

        // Mostrar cursor de espera mientras se cargan datos
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            cargarCombosYListas();
            cargarTabla(); // carga inicial sin filtros
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void initUI() {
        // ========= PANEL DE DATOS BÁSICOS =========
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

        // ========= PANEL DE RELACIONES (AUTORES / CATEGORÍAS) =========
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

        // ========= PANEL FORMULARIO (DATOS + RELACIONES) =========
        JPanel pFormulario = new JPanel(new BorderLayout(8, 8));
        pFormulario.setBorder(BorderFactory.createTitledBorder("Formulario de libro"));
        pFormulario.add(pDatos, BorderLayout.NORTH);
        pFormulario.add(pListas, BorderLayout.CENTER);

        // ========= PANEL BUSCADOR + TABLA + BOTONES =========
        JPanel pBottom = new JPanel(new BorderLayout(8, 8));
        pBottom.setBorder(BorderFactory.createTitledBorder("Catálogo de libros"));

        // --- Panel de búsqueda ---
        JPanel pBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pBusqueda.setBorder(BorderFactory.createTitledBorder("Buscar por título del libro"));

        txtBuscar = new JTextField(25);
        chkSoloDisponibles = new JCheckBox("Solo con copias disponibles");
        btnBuscar = new JButton("Buscar");

        pBusqueda.add(new JLabel("Título:"));
        pBusqueda.add(txtBuscar);
        pBusqueda.add(chkSoloDisponibles);
        pBusqueda.add(btnBuscar);

        pBottom.add(pBusqueda, BorderLayout.NORTH);

        // --- Tabla de catálogo (usando vw_CatalogoLibros) ---
        String[] cols = {"ID", "Título", "Editorial", "Año", "Idioma", "Autores", "Categorías", "Disp.", "Estado"};
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

        // --- Botones CRUD ---
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

        // ========= SPLITPANE PARA SEPARAR FORMULARIO Y TABLA =========
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pFormulario, pBottom);
        split.setResizeWeight(0.45);        // 45% arriba, 55% abajo aprox.
        split.setOneTouchExpandable(true);  // flechitas para ajustar
        add(split, BorderLayout.CENTER);

        // ========= EVENTOS =========
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarLibro());
        btnEditar.addActionListener(e -> cargarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnRefrescar.addActionListener(e -> cargarTabla());
        btnCerrar.addActionListener(e -> dispose());
        btnBuscar.addActionListener(e -> cargarTabla());  // usa texto y check de búsqueda actuales

        // Enter en el buscador también dispara la búsqueda
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    cargarTabla();
                }
            }
        });
    }

    // =================== CARGA DE DATOS ===================

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

    /**
     * Carga la tabla usando la vista vw_CatalogoLibros (método buscarLibrosCatalogo),
     * que ya trae Título, Editorial, Autores, Categorías y disponibilidad en una sola consulta.
     */
    private void cargarTabla() {
        String texto = txtBuscar != null ? txtBuscar.getText().trim() : "";
        boolean soloDisp = chkSoloDisponibles != null && chkSoloDisponibles.isSelected();

        try {
            modelo.setRowCount(0);

            // Una sola consulta para todo el catálogo (con o sin filtro)
            List<LibroBusqueda> lista = dao.buscarLibrosCatalogo(texto, soloDisp);

            for (LibroBusqueda l : lista) {
                String estado = l.getCopiasDisponibles() > 0 ? "Disponible" : "Sin stock";

                modelo.addRow(new Object[] {
                        l.getId(),
                        l.getTitulo(),
                        l.getEditorial(),
                        l.getAnio(),
                        l.getIdioma(),
                        l.getAutores(),
                        l.getCategorias(),
                        l.getCopiasDisponibles(),
                        estado
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar libros: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =================== FORMULARIO ===================

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
            txtTitulo.requestFocus();
            return;
        }
        if (titulo.length() > 200) {
            JOptionPane.showMessageDialog(this, "El título es demasiado largo (máx. 200 caracteres).", "Validación", JOptionPane.WARNING_MESSAGE);
            txtTitulo.requestFocus();
            return;
        }

        int anio = (Integer) spAnio.getValue();
        if (anio <= 0) {
            JOptionPane.showMessageDialog(this, "Ingresa un año válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            spAnio.requestFocus();
            return;
        }

        int stockMin = (Integer) spStockMin.getValue();
        if (stockMin < 0) {
            JOptionPane.showMessageDialog(this, "El stock mínimo no puede ser negativo.", "Validación", JOptionPane.WARNING_MESSAGE);
            spStockMin.requestFocus();
            return;
        }

        Editorial selEd = (Editorial) cmbEditorial.getSelectedItem();
        if (selEd == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una editorial.", "Validación", JOptionPane.WARNING_MESSAGE);
            cmbEditorial.requestFocus();
            return;
        }

        // Autores/Categorías seleccionados
        List<Autor> autoresSel = lstAutores.getSelectedValuesList();
        if (autoresSel == null || autoresSel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un autor.", "Validación", JOptionPane.WARNING_MESSAGE);
            lstAutores.requestFocus();
            return;
        }

        List<Integer> autoresIds = autoresSel.stream().map(Autor::getId).collect(Collectors.toList());
        List<Integer> categoriasIds = lstCategorias.getSelectedValuesList()
                .stream().map(Categoria::getId).collect(Collectors.toList());

        try {
            Libro l = new Libro();
            l.setTitulo(titulo);
            l.setIsbn(txtISBN.getText().trim());
            l.setEdicion(txtEdicion.getText().trim());
            l.setAnio(anio);
            l.setIdioma(txtIdioma.getText().trim());
            l.setStockMinimo(stockMin);
            l.setActivo(chkActivo.isSelected());
            l.setIdEditorial(selEd.getId());

            if (modoEdicion) {
                // ACTUALIZAR
                l.setId(Integer.parseInt(txtId.getText()));
                dao.actualizar(l, autoresIds, categoriasIds);
                JOptionPane.showMessageDialog(this, "Libro actualizado correctamente.");
            } else {
                // CREAR NUEVO
                int nuevoId = dao.crear(l, autoresIds, categoriasIds);
                JOptionPane.showMessageDialog(this, "Libro creado correctamente. ID=" + nuevoId);
                // Tras crear, limpiar todo y volver a modo "nuevo"
                limpiarFormulario();
            }

            // Recargar catálogo con los filtros actuales
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
