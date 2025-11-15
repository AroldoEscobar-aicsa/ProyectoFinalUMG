package app.view;

// === IMPORTS DE LÓGICA (DAOs y Modelos) ===
import app.dao.ReporteDAO;
import app.dao.PrestamosDAO;
import app.model.ReporteCatalogo;
import app.model.ReporteTopLibro;
import app.model.Prestamos;

// === IMPORTS DE TU FRAMEWORK DE REPORTES ===
import app.reportes.GeneradorExcel;
import app.reportes.GeneradorPDF;
import app.reportes.ReporteBase; // Lo usamos por sus constantes

// === IMPORTS DE SWING (VISTA) ===
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

// === IMPORTS DE JAVA (UTILIDADES) ===
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

// === IMPORTS DE EXCEL (Apache POI) ===
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// === IMPORTS DE PDF (iText) ===
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

/**
 * Formulario "Todo en Uno" para Reportes Operativos.
 * ¡VERSIÓN V3! Implementa "Lazy Loading" (Carga Perezosa)
 * para evitar errores de conexión (Socket closed).
 */
public class ReportesOperativosForm extends JDialog {

    private JTabbedPane tabbedPane;
    private ReporteDAO reporteDAO;
    private PrestamosDAO prestamosDAO;

    // --- Pestaña 1: Top Libros ---
    private JTable tablaTopLibros;
    private DefaultTableModel modeloTopLibros;
    private JLabel lblEstadoTopLibros;
    private List<ReporteTopLibro> datosTopLibros;

    // --- Pestaña 2: Catálogo ---
    private JTable tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private JLabel lblEstadoCatalogo;
    private List<ReporteCatalogo> datosCatalogo;

    // --- Pestaña 3: Préstamos ---
    private JTable tablaPrestamos;
    private DefaultTableModel modeloPrestamos;
    private JLabel lblEstadoPrestamos;
    private List<Prestamos> datosPrestamos;

    // Control para Lazy Loading
    private Set<Integer> pestañasCargadas;

    public ReportesOperativosForm(Frame owner) {
        super(owner, "Reportes Operativos", true);
        this.reporteDAO = new ReporteDAO();
        this.prestamosDAO = new PrestamosDAO();
        this.pestañasCargadas = new HashSet<>(); // Inicializa el set

        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();

        // 1. Crear y añadir las pestañas (paneles vacíos)
        tabbedPane.addTab("Top 5 Libros Más Prestados", crearPanelTopLibros());
        tabbedPane.addTab("Catálogo de Libros (Inventario)", crearPanelCatalogo());
        tabbedPane.addTab("Préstamos (Últimos 30 días)", crearPanelPrestamos());

        add(tabbedPane, BorderLayout.CENTER);

        // 2. Añadir el Listener para Carga Perezosa
        tabbedPane.addChangeListener(e -> {
            int indiceSeleccionado = tabbedPane.getSelectedIndex();
            // Verificamos si la pestaña ya fue cargada antes
            if (!pestañasCargadas.contains(indiceSeleccionado)) {
                // Si no, la cargamos
                cargarDatosPestaña(indiceSeleccionado);
            }
        });

        // 3. Cargar SÓLO la primera pestaña (índice 0) al inicio
        cargarDatosPestaña(0);
    }

    /**
     * Método central que decide qué SwingWorker lanzar
     * basado en la pestaña seleccionada.
     */
    private void cargarDatosPestaña(int index) {
        if (pestañasCargadas.contains(index)) {
            return; // Ya se está cargando o ya se cargó
        }

        pestañasCargadas.add(index); // Marcarla como "cargando"

        switch (index) {
            case 0:
                cargarDatosPestañaTopLibros();
                break;
            case 1:
                cargarDatosPestañaCatalogo();
                break;
            case 2:
                cargarDatosPestañaPrestamos();
                break;
        }
    }

    // ==================================================================
    // === PESTAÑA 1: TOP LIBROS
    // ==================================================================

    private JPanel crearPanelTopLibros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Posición", "Título del Libro", "Autores", "Total Préstamos"};
        modeloTopLibros = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTopLibros = new JTable(modeloTopLibros);
        tablaTopLibros.setRowHeight(24);
        tablaTopLibros.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(tablaTopLibros), BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnExportarPDF);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        lblEstadoTopLibros = new JLabel("Cargando reporte 'Top Libros', por favor espere...");
        panelSur.add(lblEstadoTopLibros, BorderLayout.SOUTH);
        panel.add(panelSur, BorderLayout.SOUTH);

        btnExportarExcel.addActionListener(e -> exportarReporteTopLibros("excel"));
        btnExportarPDF.addActionListener(e -> exportarReporteTopLibros("pdf"));

        return panel;
    }

    private void cargarDatosPestañaTopLibros() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblEstadoTopLibros.setText("Cargando reporte 'Top Libros', por favor espere...");

        SwingWorker<List<ReporteTopLibro>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReporteTopLibro> doInBackground() throws Exception {
                return reporteDAO.getTop5LibrosMasPrestados();
            }
            @Override
            protected void done() {
                try {
                    datosTopLibros = get();
                    modeloTopLibros.setRowCount(0);
                    int pos = 1;
                    for (ReporteTopLibro item : datosTopLibros) {
                        modeloTopLibros.addRow(new Object[]{
                                pos++, item.getTitulo(), item.getAutores(), item.getTotalPrestamos()
                        });
                    }
                    lblEstadoTopLibros.setText("Reporte 'Top Libros' generado con éxito.");
                } catch (Exception e) {
                    lblEstadoTopLibros.setText("Error al cargar reporte: " + e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    // ==================================================================
    // === PESTAÑA 2: CATÁLOGO
    // ==================================================================

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Título", "Autores", "Categorías", "ISBN", "Editorial", "Año", "Total", "Disp."};
        modeloCatalogo = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
        tablaCatalogo.setRowHeight(24);
        tablaCatalogo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tablaCatalogo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = tablaCatalogo.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(250); tcm.getColumn(1).setPreferredWidth(200);
        tcm.getColumn(2).setPreferredWidth(150); tcm.getColumn(3).setPreferredWidth(120);
        tcm.getColumn(4).setPreferredWidth(120); tcm.getColumn(5).setPreferredWidth(60);
        tcm.getColumn(6).setPreferredWidth(50); tcm.getColumn(7).setPreferredWidth(50);

        JScrollPane scroll = new JScrollPane(tablaCatalogo);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnExportarPDF);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        lblEstadoCatalogo = new JLabel("Seleccione esta pestaña para cargar el reporte de Catálogo.");
        panelSur.add(lblEstadoCatalogo, BorderLayout.SOUTH);
        panel.add(panelSur, BorderLayout.SOUTH);

        btnExportarExcel.addActionListener(e -> exportarReporteCatalogo("excel"));
        btnExportarPDF.addActionListener(e -> exportarReporteCatalogo("pdf"));

        return panel;
    }

    private void cargarDatosPestañaCatalogo() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblEstadoCatalogo.setText("Cargando reporte 'Catálogo', por favor espere...");

        SwingWorker<List<ReporteCatalogo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReporteCatalogo> doInBackground() throws Exception {
                return reporteDAO.getReporteCatalogoCompleto();
            }
            @Override
            protected void done() {
                try {
                    datosCatalogo = get();
                    modeloCatalogo.setRowCount(0);
                    for (ReporteCatalogo item : datosCatalogo) {
                        modeloCatalogo.addRow(new Object[]{
                                item.getTitulo(), item.getAutores(), item.getCategorias(),
                                item.getIsbn(), item.getEditorial(), item.getAnio(),
                                item.getTotalCopias(), item.getCopiasDisponibles()
                        });
                    }
                    lblEstadoCatalogo.setText("Reporte 'Catálogo' generado con éxito.");
                } catch (Exception e) {
                    lblEstadoCatalogo.setText("Error al cargar reporte: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    // ==================================================================
    // === PESTAÑA 3: PRÉSTAMOS RECIENTES
    // ==================================================================

    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columnas = {"ID", "Cliente", "Título", "Copia", "Fecha Préstamo", "Fecha Venc.", "Fecha Dev.", "Estado"};
        modeloPrestamos = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPrestamos = new JTable(modeloPrestamos);
        tablaPrestamos.setRowHeight(24);
        tablaPrestamos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnExportarPDF);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        lblEstadoPrestamos = new JLabel("Seleccione esta pestaña para cargar los préstamos de los últimos 30 días.");
        panelSur.add(lblEstadoPrestamos, BorderLayout.SOUTH);

        panel.add(panelSur, BorderLayout.SOUTH);

        btnExportarExcel.addActionListener(e -> exportarReportePrestamos("excel"));
        btnExportarPDF.addActionListener(e -> exportarReportePrestamos("pdf"));

        return panel;
    }

    private void cargarDatosPestañaPrestamos() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblEstadoPrestamos.setText("Cargando préstamos de los últimos 30 días...");

        SwingWorker<List<Prestamos>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Prestamos> doInBackground() throws Exception {
                LocalDate fechaFin = LocalDate.now();
                LocalDate fechaInicio = fechaFin.minusDays(30);
                return prestamosDAO.listarPrestamosPorPeriodo(fechaInicio, fechaFin, "TODOS");
            }
            @Override
            protected void done() {
                try {
                    datosPrestamos = get();
                    modeloPrestamos.setRowCount(0);
                    for (Prestamos p : datosPrestamos) {
                        modeloPrestamos.addRow(new Object[]{
                                p.getId(), p.getNombreCliente(), p.getTitulo(),
                                p.getCodigoBarra(),
                                p.getFechaPrestamoUtc() != null ? p.getFechaPrestamoUtc().format(ReporteBase.FORMATO_FECHA) : "",
                                p.getFechaVencimientoUtc() != null ? p.getFechaVencimientoUtc().format(ReporteBase.FORMATO_FECHA) : "",
                                p.getFechaDevolucionUtc() != null ? p.getFechaDevolucionUtc().format(ReporteBase.FORMATO_FECHA) : "",
                                p.getEstado()
                        });
                    }
                    lblEstadoPrestamos.setText("Reporte de préstamos generado con éxito.");
                } catch (Exception e) {
                    lblEstadoPrestamos.setText("Error al cargar préstamos: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    // ==================================================================
    // === LÓGICA DE EXPORTACIÓN (Usando tu Framework)
    // =G================================================================

    /**
     * Lógica específica para exportar el reporte "Top Libros"
     */
    private void exportarReporteTopLibros(String formato) {
        if (datosTopLibros == null || datosTopLibros.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String extension = formato.equals("excel") ? "xlsx" : "pdf";
        fileChooser.setSelectedFile(new File("reporte_top_libros." + extension));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith("." + extension)) filePath += "." + extension;

            final String finalFilePath = filePath;
            String titulo = "Top 5 Libros Más Prestados";

            SwingWorker<Void, Void> exportWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (formato.equals("excel")) {
                        XSSFWorkbook wb = GeneradorExcel.crearLibro();
                        Sheet hoja = GeneradorExcel.crearHoja(wb, "Top Libros");
                        CellStyle estiloCelda = GeneradorExcel.crearEstiloCelda(wb);
                        CellStyle estiloNum = GeneradorExcel.crearEstiloCeldaNumero(wb);
                        int filaIdx = GeneradorExcel.agregarEncabezado(hoja, wb, titulo, "", LocalDate.now(), 4);
                        GeneradorExcel.crearFilaEncabezado(hoja, wb, filaIdx++, "Posición", "Título", "Autores", "Total Préstamos");
                        int pos = 1;
                        for (ReporteTopLibro item : datosTopLibros) {
                            Row fila = hoja.createRow(filaIdx++);
                            GeneradorExcel.crearCelda(fila, 0, pos++, estiloNum);
                            GeneradorExcel.crearCelda(fila, 1, item.getTitulo(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 2, item.getAutores(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 3, item.getTotalPrestamos(), estiloNum);
                        }
                        GeneradorExcel.ajustarAnchoColumnas(hoja, 4);
                        GeneradorExcel.guardarLibro(wb, finalFilePath);
                    } else {
                        Document doc = GeneradorPDF.crearDocumento(finalFilePath);
                        GeneradorPDF.agregarEncabezado(doc, titulo, "", LocalDate.now());
                        Table tabla = GeneradorPDF.crearTabla(new float[]{1, 4, 3, 2});
                        tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Posición"));
                        tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Título"));
                        tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Autores"));
                        tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Total Préstamos"));
                        int pos = 1;
                        for (ReporteTopLibro item : datosTopLibros) {
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(pos++), TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getTitulo()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getAutores()));
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getTotalPrestamos()), TextAlignment.RIGHT));
                        }
                        doc.add(tabla);
                        doc.close();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try { get(); JOptionPane.showMessageDialog(ReportesOperativosForm.this, "Reporte exportado con éxito a:\n" + finalFilePath); }
                    catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(ReportesOperativosForm.this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            };
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            exportWorker.execute();
        }
    }

    /**
     * Lógica específica para exportar el reporte "Catálogo"
     */
    private void exportarReporteCatalogo(String formato) {
        if (datosCatalogo == null || datosCatalogo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String extension = formato.equals("excel") ? "xlsx" : "pdf";
        fileChooser.setSelectedFile(new File("reporte_catalogo." + extension));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith("." + extension)) filePath += "." + extension;

            final String finalFilePath = filePath;
            String titulo = "Reporte de Catálogo (Inventario)";

            SwingWorker<Void, Void> exportWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String[] headers = {"Título", "Autores", "Categorías", "ISBN", "Editorial", "Año", "Total", "Disp."};
                    if (formato.equals("excel")) {
                        XSSFWorkbook wb = GeneradorExcel.crearLibro();
                        Sheet hoja = GeneradorExcel.crearHoja(wb, "Catalogo");
                        CellStyle estiloCelda = GeneradorExcel.crearEstiloCelda(wb);
                        CellStyle estiloNum = GeneradorExcel.crearEstiloCeldaNumero(wb);
                        int filaIdx = GeneradorExcel.agregarEncabezado(hoja, wb, titulo, "", LocalDate.now(), headers.length);
                        GeneradorExcel.crearFilaEncabezado(hoja, wb, filaIdx++, headers);
                        for (ReporteCatalogo item : datosCatalogo) {
                            Row fila = hoja.createRow(filaIdx++);
                            GeneradorExcel.crearCelda(fila, 0, item.getTitulo(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 1, item.getAutores(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 2, item.getCategorias(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 3, item.getIsbn(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 4, item.getEditorial(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 5, item.getAnio(), estiloNum);
                            GeneradorExcel.crearCelda(fila, 6, item.getTotalCopias(), estiloNum);
                            GeneradorExcel.crearCelda(fila, 7, item.getCopiasDisponibles(), estiloNum);
                        }
                        GeneradorExcel.ajustarAnchoColumnas(hoja, headers.length);
                        GeneradorExcel.guardarLibro(wb, finalFilePath);
                    } else {
                        Document doc = GeneradorPDF.crearDocumento(finalFilePath);
                        GeneradorPDF.agregarEncabezado(doc, titulo, "", LocalDate.now());
                        float[] anchos = {3, 2, 2, 1.5f, 1.5f, 0.5f, 0.5f, 0.5f};
                        Table tabla = GeneradorPDF.crearTabla(anchos);
                        for(String h : headers) tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader(h));
                        for (ReporteCatalogo item : datosCatalogo) {
                            tabla.addCell(GeneradorPDF.crearCelda(item.getTitulo()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getAutores()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getCategorias()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getIsbn()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getEditorial()));
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getAnio()), TextAlignment.RIGHT));
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getTotalCopias()), TextAlignment.RIGHT));
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getCopiasDisponibles()), TextAlignment.RIGHT));
                        }
                        doc.add(tabla);
                        doc.close();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try { get(); JOptionPane.showMessageDialog(ReportesOperativosForm.this, "Reporte exportado con éxito a:\n" + finalFilePath); }
                    catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(ReportesOperativosForm.this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            };
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            exportWorker.execute();
        }
    }

    /**
     * Lógica específica para exportar el reporte "Préstamos"
     */
    private void exportarReportePrestamos(String formato) {
        if (datosPrestamos == null || datosPrestamos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String extension = formato.equals("excel") ? "xlsx" : "pdf";
        fileChooser.setSelectedFile(new File("reporte_prestamos." + extension));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith("." + extension)) filePath += "." + extension;

            final String finalFilePath = filePath;
            String titulo = "Reporte de Préstamos (Últimos 30 días)";

            SwingWorker<Void, Void> exportWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String[] headers = {"ID", "Cliente", "Título", "Copia", "Fecha Préstamo", "Fecha Venc.", "Fecha Dev.", "Estado"};
                    if (formato.equals("excel")) {
                        XSSFWorkbook wb = GeneradorExcel.crearLibro();
                        Sheet hoja = GeneradorExcel.crearHoja(wb, "Prestamos");
                        CellStyle estiloCelda = GeneradorExcel.crearEstiloCelda(wb);
                        CellStyle estiloNum = GeneradorExcel.crearEstiloCeldaNumero(wb);
                        int filaIdx = GeneradorExcel.agregarEncabezado(hoja, wb, titulo, "", LocalDate.now(), headers.length);
                        GeneradorExcel.crearFilaEncabezado(hoja, wb, filaIdx++, headers);
                        for (Prestamos p : datosPrestamos) {
                            Row fila = hoja.createRow(filaIdx++);
                            GeneradorExcel.crearCelda(fila, 0, p.getId(), estiloNum);
                            GeneradorExcel.crearCelda(fila, 1, p.getNombreCliente(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 2, p.getTitulo(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 3, p.getCodigoBarra(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 4, p.getFechaPrestamoUtc() != null ? p.getFechaPrestamoUtc().format(ReporteBase.FORMATO_FECHA) : "", estiloCelda);
                            GeneradorExcel.crearCelda(fila, 5, p.getFechaVencimientoUtc() != null ? p.getFechaVencimientoUtc().format(ReporteBase.FORMATO_FECHA) : "", estiloCelda);
                            GeneradorExcel.crearCelda(fila, 6, p.getFechaDevolucionUtc() != null ? p.getFechaDevolucionUtc().format(ReporteBase.FORMATO_FECHA) : "", estiloCelda);
                            GeneradorExcel.crearCelda(fila, 7, p.getEstado(), estiloCelda);
                        }
                        GeneradorExcel.ajustarAnchoColumnas(hoja, headers.length);
                        GeneradorExcel.guardarLibro(wb, finalFilePath);
                    } else {
                        Document doc = GeneradorPDF.crearDocumento(finalFilePath);
                        GeneradorPDF.agregarEncabezado(doc, titulo, "", LocalDate.now());
                        float[] anchos = {1, 3, 3, 2, 2, 2, 2, 1};
                        Table tabla = GeneradorPDF.crearTabla(anchos);
                        for(String h : headers) tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader(h));
                        for (Prestamos p : datosPrestamos) {
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(p.getId()), TextAlignment.RIGHT));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getNombreCliente()));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getTitulo()));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getCodigoBarra()));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getFechaPrestamoUtc() != null ? p.getFechaPrestamoUtc().format(ReporteBase.FORMATO_FECHA) : "", TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getFechaVencimientoUtc() != null ? p.getFechaVencimientoUtc().format(ReporteBase.FORMATO_FECHA) : "", TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getFechaDevolucionUtc() != null ? p.getFechaDevolucionUtc().format(ReporteBase.FORMATO_FECHA) : "", TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(p.getEstado()));
                        }
                        doc.add(tabla);
                        doc.close();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try { get(); JOptionPane.showMessageDialog(ReportesOperativosForm.this, "Reporte exportado con éxito a:\n" + finalFilePath); }
                    catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(ReportesOperativosForm.this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            };
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            exportWorker.execute();
        }
    }
}