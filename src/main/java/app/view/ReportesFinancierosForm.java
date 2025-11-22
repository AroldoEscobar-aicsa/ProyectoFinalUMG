package app.view;

// === IMPORTS DE LÓGICA (DAOs y Modelos) ===
import app.dao.ReporteDAO;
import app.dao.CajaMovimientoDAO; // ¡Ahora sí lo usamos!
import app.model.ReporteClienteMoroso;
import app.model.CajaMovimiento;     // ¡Y su modelo!

// === IMPORTS DE TU FRAMEWORK DE REPORTES ===
import app.reportes.GeneradorExcel;
import app.reportes.GeneradorPDF;
import app.reportes.ReporteBase; // Lo usamos por sus constantes

// === IMPORTS DE SWING (VISTA) ===
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

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
 * Formulario "Todo en Uno" para Reportes Financieros.
 * ¡VERSIÓN V2! - Implementa la pestaña de Recaudación.
 */
public class ReportesFinancierosForm extends JDialog {

    private JTabbedPane tabbedPane;
    private ReporteDAO reporteDAO;
    private CajaMovimientoDAO cajaDAO; // ¡Ahora sí lo usamos!

    // --- Pestaña 1: Clientes Morosos ---
    private JTable tablaMorosos;
    private DefaultTableModel modeloMorosos;
    private JLabel lblEstadoMorosos;
    private List<ReporteClienteMoroso> datosMorosos;

    // --- Pestaña 2: Recaudación ---
    private JTable tablaRecaudacion;
    private DefaultTableModel modeloRecaudacion;
    private JLabel lblEstadoRecaudacion;
    private List<CajaMovimiento> datosRecaudacion;

    // Control para Lazy Loading
    private Set<Integer> pestañasCargadas;

    public ReportesFinancierosForm(Frame owner) {
        super(owner, "Reportes Financieros", true);
        this.reporteDAO = new ReporteDAO();
        this.cajaDAO = new CajaMovimientoDAO(); // ¡Inicializado!
        this.pestañasCargadas = new HashSet<>();

        setSize(900, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();

        // 1. Crear y añadir la pestaña de "Clientes Morosos"
        tabbedPane.addTab("Clientes Morosos (Deudas)", crearPanelMorosos());

        // 2. Crear y añadir la pestaña de "Recaudación"
        tabbedPane.addTab("Recaudación (Ingresos)", crearPanelRecaudacion());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Añadir el Listener para Carga Perezosa
        tabbedPane.addChangeListener(e -> {
            int indiceSeleccionado = tabbedPane.getSelectedIndex();
            if (!pestañasCargadas.contains(indiceSeleccionado)) {
                cargarDatosPestaña(indiceSeleccionado);
            }
        });

        // 4. Cargar SÓLO la primera pestaña (índice 0) al inicio
        cargarDatosPestaña(0);
    }

    /**
     * Método central que decide qué SwingWorker lanzar
     */
    private void cargarDatosPestaña(int index) {
        if (pestañasCargadas.contains(index)) return;
        pestañasCargadas.add(index);

        switch (index) {
            case 0:
                cargarDatosPestañaMorosos();
                break;
            case 1:
                cargarDatosPestañaRecaudacion();
                break;
        }
    }

    // ==================================================================
    // === PESTAÑA 1: CLIENTES MOROSOS
    // ==================================================================

    private JPanel crearPanelMorosos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Nombre Completo", "Email", "Teléfono", "Multas Pendientes", "Monto Total Deuda"};
        modeloMorosos = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMorosos = new JTable(modeloMorosos);
        tablaMorosos.setRowHeight(24);
        tablaMorosos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(tablaMorosos), BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnExportarPDF);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        lblEstadoMorosos = new JLabel("Cargando reporte 'Clientes Morosos', por favor espere...");
        panelSur.add(lblEstadoMorosos, BorderLayout.SOUTH);
        panel.add(panelSur, BorderLayout.SOUTH);

        btnExportarExcel.addActionListener(e -> exportarReporteMorosos("excel"));
        btnExportarPDF.addActionListener(e -> exportarReporteMorosos("pdf"));

        return panel;
    }

    private void cargarDatosPestañaMorosos() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblEstadoMorosos.setText("Cargando reporte 'Clientes Morosos', por favor espere...");

        SwingWorker<List<ReporteClienteMoroso>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReporteClienteMoroso> doInBackground() throws Exception {
                return reporteDAO.getReporteClientesMorosos();
            }
            @Override
            protected void done() {
                try {
                    datosMorosos = get();
                    modeloMorosos.setRowCount(0);
                    for (ReporteClienteMoroso item : datosMorosos) {
                        modeloMorosos.addRow(new Object[]{
                                item.getNombreCompleto(),
                                item.getEmail(),
                                item.getTelefono(),
                                item.getTotalMultasPendientes(),
                                String.format("Q %.2f", item.getMontoTotalDeuda())
                        });
                    }
                    lblEstadoMorosos.setText("Reporte 'Clientes Morosos' generado con éxito.");
                } catch (Exception e) {
                    lblEstadoMorosos.setText("Error al cargar reporte: " + e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    // ==================================================================
    // === PESTAÑA 2: RECAUDACIÓN (¡Implementada!)
    // ==================================================================

    private JPanel crearPanelRecaudacion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columnas = {"ID Mov.", "ID Sesión", "Fecha/Hora", "Concepto", "Monto", "ID Multa"};
        modeloRecaudacion = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaRecaudacion = new JTable(modeloRecaudacion);
        tablaRecaudacion.setRowHeight(24);
        tablaRecaudacion.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panel.add(new JScrollPane(tablaRecaudacion), BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnExportarPDF = new JButton("Exportar a PDF");
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnExportarPDF);
        panelSur.add(panelBotones, BorderLayout.NORTH);

        lblEstadoRecaudacion = new JLabel("Seleccione esta pestaña para cargar la recaudación de los últimos 30 días.");
        panelSur.add(lblEstadoRecaudacion, BorderLayout.SOUTH);
        panel.add(panelSur, BorderLayout.SOUTH);

        btnExportarExcel.addActionListener(e -> exportarReporteRecaudacion("excel"));
        btnExportarPDF.addActionListener(e -> exportarReporteRecaudacion("pdf"));

        return panel;
    }

    private void cargarDatosPestañaRecaudacion() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblEstadoRecaudacion.setText("Cargando recaudación de los últimos 30 días...");

        SwingWorker<List<CajaMovimiento>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CajaMovimiento> doInBackground() throws Exception {
                // ¡Usamos el DAO de CajaMovimiento!
                LocalDate fechaFin = LocalDate.now();
                LocalDate fechaInicio = fechaFin.minusDays(30);
                // Solo traemos las 'ENTRADA'
                return cajaDAO.getMovimientosPorRangoFechas(fechaInicio, fechaFin, "ENTRADA");
            }

            @Override
            protected void done() {
                try {
                    datosRecaudacion = get(); // Guardamos los datos
                    modeloRecaudacion.setRowCount(0);
                    double total = 0;
                    for (CajaMovimiento item : datosRecaudacion) {
                        modeloRecaudacion.addRow(new Object[]{
                                item.getIdMovimiento(),
                                item.getIdCajaSesion(),
                                item.getCreadoUtc().format(ReporteBase.FORMATO_FECHA),
                                item.getConcepto(),
                                String.format("Q %.2f", item.getMonto()),
                                item.getIdMulta() != null ? item.getIdMulta() : "N/A"
                        });
                        total += item.getMonto();
                    }
                    lblEstadoRecaudacion.setText("Reporte generado. Recaudación total (30 días): Q " + String.format("%.2f", total));
                } catch (Exception e) {
                    lblEstadoRecaudacion.setText("Error al cargar reporte: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }

    // ==================================================================
    // === LÓGICA DE EXPORTACIÓN
    // ==================================================================

    private void exportarReporteMorosos(String formato) {
        if (datosMorosos == null || datosMorosos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String extension = formato.equals("excel") ? "xlsx" : "pdf";
        fileChooser.setSelectedFile(new File("reporte_morosos." + extension));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith("." + extension)) filePath += "." + extension;

            final String finalFilePath = filePath;
            String titulo = "Reporte de Clientes Morosos";

            SwingWorker<Void, Void> exportWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String[] headers = {"Nombre Completo", "Email", "Teléfono", "Multas Pendientes", "Monto Total Deuda"};
                    if (formato.equals("excel")) {
                        XSSFWorkbook wb = GeneradorExcel.crearLibro();
                        Sheet hoja = GeneradorExcel.crearHoja(wb, "Clientes Morosos");
                        CellStyle estiloCelda = GeneradorExcel.crearEstiloCelda(wb);
                        CellStyle estiloMoneda = GeneradorExcel.crearEstiloMoneda(wb);
                        CellStyle estiloNum = GeneradorExcel.crearEstiloCeldaNumero(wb);

                        int filaIdx = GeneradorExcel.agregarEncabezado(hoja, wb, titulo, "", LocalDate.now(), headers.length);
                        GeneradorExcel.crearFilaEncabezado(hoja, wb, filaIdx++, headers);

                        for (ReporteClienteMoroso item : datosMorosos) {
                            Row fila = hoja.createRow(filaIdx++);
                            GeneradorExcel.crearCelda(fila, 0, item.getNombreCompleto(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 1, item.getEmail(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 2, item.getTelefono(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 3, item.getTotalMultasPendientes(), estiloNum);
                            GeneradorExcel.crearCelda(fila, 4, item.getMontoTotalDeuda(), estiloMoneda);
                        }
                        GeneradorExcel.ajustarAnchoColumnas(hoja, headers.length);
                        GeneradorExcel.guardarLibro(wb, finalFilePath);
                    } else {
                        Document doc = GeneradorPDF.crearDocumento(finalFilePath);
                        GeneradorPDF.agregarEncabezado(doc, titulo, "", LocalDate.now());
                        float[] anchos = {3, 3, 2, 1, 2};
                        Table tabla = GeneradorPDF.crearTabla(anchos);
                        for(String h : headers) tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader(h));

                        for (ReporteClienteMoroso item : datosMorosos) {
                            tabla.addCell(GeneradorPDF.crearCelda(item.getNombreCompleto()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getEmail()));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getTelefono(), TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getTotalMultasPendientes()), TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(String.format("Q %.2f", item.getMontoTotalDeuda()), TextAlignment.RIGHT));
                        }
                        doc.add(tabla);

                        GeneradorPDF.agregarPiePagina(doc, "OLA");
                        doc.close();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try { get(); JOptionPane.showMessageDialog(ReportesFinancierosForm.this, "Reporte exportado con éxito a:\n" + finalFilePath); }
                    catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(ReportesFinancierosForm.this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            };
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            exportWorker.execute();
        }
    }

    /**
     * Lógica específica para exportar el reporte "Recaudación"
     */
    private void exportarReporteRecaudacion(String formato) {
        if (datosRecaudacion == null || datosRecaudacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String extension = formato.equals("excel") ? "xlsx" : "pdf";
        fileChooser.setSelectedFile(new File("reporte_recaudacion." + extension));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith("." + extension)) filePath += "." + extension;

            final String finalFilePath = filePath;
            String titulo = "Reporte de Recaudación (Últimos 30 días)";

            SwingWorker<Void, Void> exportWorker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String[] headers = {"ID Mov.", "ID Sesión", "Fecha/Hora", "Concepto", "Monto", "ID Multa"};
                    if (formato.equals("excel")) {
                        XSSFWorkbook wb = GeneradorExcel.crearLibro();
                        Sheet hoja = GeneradorExcel.crearHoja(wb, "Recaudacion");
                        CellStyle estiloCelda = GeneradorExcel.crearEstiloCelda(wb);
                        CellStyle estiloMoneda = GeneradorExcel.crearEstiloMoneda(wb);
                        CellStyle estiloNum = GeneradorExcel.crearEstiloCeldaNumero(wb);

                        int filaIdx = GeneradorExcel.agregarEncabezado(hoja, wb, titulo, "", LocalDate.now(), headers.length);
                        GeneradorExcel.crearFilaEncabezado(hoja, wb, filaIdx++, headers);

                        double total = 0.0;
                        for (CajaMovimiento item : datosRecaudacion) {
                            Row fila = hoja.createRow(filaIdx++);
                            GeneradorExcel.crearCelda(fila, 0, item.getIdMovimiento(), estiloNum);
                            GeneradorExcel.crearCelda(fila, 1, item.getIdCajaSesion(), estiloNum);
                            GeneradorExcel.crearCelda(fila, 2, item.getCreadoUtc().format(ReporteBase.FORMATO_FECHA), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 3, item.getConcepto(), estiloCelda);
                            GeneradorExcel.crearCelda(fila, 4, item.getMonto(), estiloMoneda);
                            // Convertimos el Integer a String para que coincida con "N/A"
                            GeneradorExcel.crearCelda(fila, 5, item.getIdMulta() != null ? item.getIdMulta().toString() : "N/A", estiloCelda);
                            total += item.getMonto();
                        }

                        // Fila de Total
                        Row filaTotal = hoja.createRow(filaIdx++);
                        GeneradorExcel.crearCelda(filaTotal, 3, "TOTAL RECAUDADO:", GeneradorExcel.crearEstiloTotal(wb));
                        GeneradorExcel.crearCelda(filaTotal, 4, total, GeneradorExcel.crearEstiloTotal(wb));

                        GeneradorExcel.ajustarAnchoColumnas(hoja, headers.length);
                        GeneradorExcel.guardarLibro(wb, finalFilePath);
                    } else {
                        Document doc = GeneradorPDF.crearDocumento(finalFilePath);
                        GeneradorPDF.agregarEncabezado(doc, titulo, "", LocalDate.now());
                        float[] anchos = {1, 1, 2, 4, 2, 1};
                        Table tabla = GeneradorPDF.crearTabla(anchos);
                        for(String h : headers) tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader(h));

                        double total = 0.0;
                        for (CajaMovimiento item : datosRecaudacion) {
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getIdMovimiento()), TextAlignment.RIGHT));
                            tabla.addCell(GeneradorPDF.crearCelda(String.valueOf(item.getIdCajaSesion()), TextAlignment.RIGHT));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getCreadoUtc().format(ReporteBase.FORMATO_FECHA), TextAlignment.CENTER));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getConcepto()));
                            tabla.addCell(GeneradorPDF.crearCelda(String.format("Q %.2f", item.getMonto()), TextAlignment.RIGHT));
                            tabla.addCell(GeneradorPDF.crearCelda(item.getIdMulta() != null ? item.getIdMulta().toString() : "N/A", TextAlignment.CENTER));
                            total += item.getMonto();
                        }

                        // Fila de Total
                        tabla.addCell(GeneradorPDF.crearCeldaTotal("").setMargin(0).setPadding(0).setBorder(null));
                        tabla.addCell(GeneradorPDF.crearCeldaTotal("").setMargin(0).setPadding(0).setBorder(null));
                        tabla.addCell(GeneradorPDF.crearCeldaTotal("").setMargin(0).setPadding(0).setBorder(null));
                        tabla.addCell(GeneradorPDF.crearCeldaTotal("TOTAL RECAUDADO:"));
                        tabla.addCell(GeneradorPDF.crearCeldaTotal(String.format("Q %.2f", total)));
                        tabla.addCell(GeneradorPDF.crearCeldaTotal("").setMargin(0).setPadding(0).setBorder(null));

                        doc.add(tabla);
                        GeneradorPDF.agregarPiePagina(doc,"DIOS ME AMA");
                        doc.close();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try { get(); JOptionPane.showMessageDialog(ReportesFinancierosForm.this, "Reporte exportado con éxito a:\n" + finalFilePath); }
                    catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(ReportesFinancierosForm.this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            };
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            exportWorker.execute();
        }
    }
}