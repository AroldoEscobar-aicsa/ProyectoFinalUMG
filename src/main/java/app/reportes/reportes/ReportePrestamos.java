package app.reportes.reportes;

import app.dao.PrestamosDAO;
import app.model.Prestamos;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Reporte de Préstamos por Período.
 * Muestra todos los préstamos en un rango de fechas con opción de filtrar por estado.
 * Genera PDF y Excel.
 */
public class ReportePrestamos {

    private final PrestamosDAO prestamosDAO;
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Directorio donde se guardarán los reportes
    private static final String DIRECTORIO_REPORTES = "reportes";

    public ReportePrestamos() {
        this.prestamosDAO = new PrestamosDAO();

        // Crear directorio si no existe
        File dir = new File(DIRECTORIO_REPORTES);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Genera el reporte de préstamos en PDF.
     *
     * @param fechaInicio Fecha inicial del período
     * @param fechaFin Fecha final del período
     * @param estadoFiltro Estado a filtrar (null = todos, "ACTIVO", "CERRADO", "ATRASADO")
     * @return Archivo PDF generado
     */
    public File generarPDF(LocalDate fechaInicio, LocalDate fechaFin, String estadoFiltro) throws Exception {
        // Obtener datos
        List<Prestamos> prestamos = prestamosDAO.listarPrestamosPorPeriodo(fechaInicio, fechaFin, estadoFiltro);

        if (prestamos.isEmpty()) {
            throw new Exception("No se encontraron préstamos para el período seleccionado.");
        }

        // Crear nombre de archivo
        String nombreArchivo = String.format("Reporte_Prestamos_%s_a_%s",
                fechaInicio.format(formatoFecha).replace("/", "-"),
                fechaFin.format(formatoFecha).replace("/", "-"));

        if (estadoFiltro != null && !estadoFiltro.trim().isEmpty()) {
            nombreArchivo += "_" + estadoFiltro;
        }

        File archivoPDF = new File(DIRECTORIO_REPORTES, nombreArchivo + ".pdf");

        // Generar PDF
        PdfWriter writer = new PdfWriter(archivoPDF);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Título
            String tituloTexto = "Reporte de Préstamos";
            if (estadoFiltro != null && !estadoFiltro.trim().isEmpty()) {
                tituloTexto += " - Estado: " + estadoFiltro;
            }

            Paragraph titulo = new Paragraph(tituloTexto)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(titulo);

            // Subtítulo con período
            String subtituloTexto = String.format("Período: %s al %s",
                    fechaInicio.format(formatoFecha),
                    fechaFin.format(formatoFecha));

            Paragraph subtitulo = new Paragraph(subtituloTexto)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(subtitulo);

            // Fecha de generación
            Paragraph fechaGen = new Paragraph("Generado: " + LocalDateTime.now().format(formatoFechaHora))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(fechaGen);

            // Estadísticas generales
            agregarEstadisticas(document, prestamos);

            // Tabla de préstamos
            crearTablaPrestamos(document, prestamos);

            // Pie de página con totales
            agregarResumenFinal(document, prestamos);

        } finally {
            document.close();
        }

        return archivoPDF;
    }

    /**
     * Genera el reporte de préstamos en Excel.
     */
    public File generarExcel(LocalDate fechaInicio, LocalDate fechaFin, String estadoFiltro) throws Exception {
        // Obtener datos
        List<Prestamos> prestamos = prestamosDAO.listarPrestamosPorPeriodo(fechaInicio, fechaFin, estadoFiltro);

        if (prestamos.isEmpty()) {
            throw new Exception("No se encontraron préstamos para el período seleccionado.");
        }

        // Crear nombre de archivo
        String nombreArchivo = String.format("Reporte_Prestamos_%s_a_%s",
                fechaInicio.format(formatoFecha).replace("/", "-"),
                fechaFin.format(formatoFecha).replace("/", "-"));

        if (estadoFiltro != null && !estadoFiltro.trim().isEmpty()) {
            nombreArchivo += "_" + estadoFiltro;
        }

        File archivoExcel = new File(DIRECTORIO_REPORTES, nombreArchivo + ".xlsx");

        // Crear workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Préstamos");

        try {
            // Estilos
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloCelda = crearEstiloCelda(workbook);
            CellStyle estiloNumerico = crearEstiloCeldaNumerica(workbook);

            int rowNum = 0;

            // Título
            Row filaTitulo = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell celdaTitulo = filaTitulo.createCell(0);
            String tituloTexto = "Reporte de Préstamos";
            if (estadoFiltro != null && !estadoFiltro.trim().isEmpty()) {
                tituloTexto += " - Estado: " + estadoFiltro;
            }
            celdaTitulo.setCellValue(tituloTexto);
            celdaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Período
            Row filaPeriodo = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell celdaPeriodo = filaPeriodo.createCell(0);
            celdaPeriodo.setCellValue(String.format("Período: %s al %s",
                    fechaInicio.format(formatoFecha),
                    fechaFin.format(formatoFecha)));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 8));

            // Estadísticas
            rowNum = agregarEstadisticasExcel(sheet, rowNum, prestamos, estiloCelda);

            // Espacio
            rowNum++;

            // Encabezados
            Row filaEncabezado = sheet.createRow(rowNum++);
            String[] encabezados = {
                    "ID", "Cliente", "Código Cliente", "Libro", "Código Barra",
                    "Fecha Préstamo", "Vencimiento", "Devolución", "Estado", "Multa (Q)"
            };

            for (int i = 0; i < encabezados.length; i++) {
                org.apache.poi.ss.usermodel.Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            // Datos
            for (Prestamos p : prestamos) {
                Row fila = sheet.createRow(rowNum++);

                fila.createCell(0).setCellValue(p.getId());
                fila.createCell(1).setCellValue(p.getNombreCliente());
                fila.createCell(2).setCellValue(p.getCodigoCliente());
                fila.createCell(3).setCellValue(p.getTitulo());
                fila.createCell(4).setCellValue(p.getCodigoBarra());

                fila.createCell(5).setCellValue(
                        p.getFechaPrestamoUtc() != null ?
                                p.getFechaPrestamoUtc().format(formatoFecha) : "-"
                );

                fila.createCell(6).setCellValue(
                        p.getFechaVencimientoUtc() != null ?
                                p.getFechaVencimientoUtc().format(formatoFecha) : "-"
                );

                fila.createCell(7).setCellValue(
                        p.getFechaDevolucionUtc() != null ?
                                p.getFechaDevolucionUtc().format(formatoFecha) : "Pendiente"
                );

                fila.createCell(8).setCellValue(p.getEstado());

                org.apache.poi.ss.usermodel.Cell celdaMulta = fila.createCell(9);
                celdaMulta.setCellValue(p.getMultaCalculada());
                celdaMulta.setCellStyle(estiloNumerico);

                // Aplicar estilos
                for (int i = 0; i < 9; i++) {
                    fila.getCell(i).setCellStyle(estiloCelda);
                }
            }

            // Totales
            rowNum++;
            Row filaTotales = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell celdaTextoTotal = filaTotales.createCell(8);
            celdaTextoTotal.setCellValue("Total Multas:");
            celdaTextoTotal.setCellStyle(estiloEncabezado);

            org.apache.poi.ss.usermodel.Cell celdaTotal = filaTotales.createCell(9);
            double totalMultas = prestamos.stream()
                    .mapToDouble(Prestamos::getMultaCalculada)
                    .sum();
            celdaTotal.setCellValue(totalMultas);
            celdaTotal.setCellStyle(estiloNumerico);

            // Ajustar anchos de columna
            for (int i = 0; i < encabezados.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(archivoExcel)) {
                workbook.write(fos);
            }

        } finally {
            workbook.close();
        }

        return archivoExcel;
    }

    // ========== MÉTODOS AUXILIARES PARA PDF ==========

    private void agregarEstadisticas(Document document, List<Prestamos> prestamos) {
        int totalPrestamos = prestamos.size();
        long activos = prestamos.stream().filter(p -> "ACTIVO".equals(p.getEstado())).count();
        long atrasados = prestamos.stream().filter(p -> "ATRASADO".equals(p.getEstado())).count();
        long cerrados = prestamos.stream().filter(p -> "CERRADO".equals(p.getEstado())).count();
        long cancelados = prestamos.stream().filter(p -> "CANCELADO".equals(p.getEstado())).count();

        double totalMultas = prestamos.stream()
                .mapToDouble(Prestamos::getMultaCalculada)
                .sum();

        String estadisticas = String.format(
                "Total de Préstamos: %d | Activos: %d | Atrasados: %d | Cerrados: %d | Cancelados: %d | Total Multas: Q%.2f",
                totalPrestamos, activos, atrasados, cerrados, cancelados, totalMultas
        );

        Paragraph parrafoStats = new Paragraph(estadisticas)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);

        document.add(parrafoStats);
    }

    private void crearTablaPrestamos(Document document, List<Prestamos> prestamos) {
        // Crear tabla con 10 columnas
        float[] anchos = {5, 12, 8, 15, 8, 10, 10, 10, 8, 8};
        Table tabla = new Table(UnitValue.createPercentArray(anchos));
        tabla.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        agregarCeldaEncabezado(tabla, "ID");
        agregarCeldaEncabezado(tabla, "Cliente");
        agregarCeldaEncabezado(tabla, "Código");
        agregarCeldaEncabezado(tabla, "Libro");
        agregarCeldaEncabezado(tabla, "Cód. Barra");
        agregarCeldaEncabezado(tabla, "Préstamo");
        agregarCeldaEncabezado(tabla, "Vencimiento");
        agregarCeldaEncabezado(tabla, "Devolución");
        agregarCeldaEncabezado(tabla, "Estado");
        agregarCeldaEncabezado(tabla, "Multa (Q)");

        // Datos
        for (Prestamos p : prestamos) {
            agregarCelda(tabla, String.valueOf(p.getId()));
            agregarCelda(tabla, p.getNombreCliente());
            agregarCelda(tabla, p.getCodigoCliente());
            agregarCelda(tabla, p.getTitulo());
            agregarCelda(tabla, p.getCodigoBarra());

            agregarCelda(tabla,
                    p.getFechaPrestamoUtc() != null ?
                            p.getFechaPrestamoUtc().format(formatoFecha) : "-"
            );

            agregarCelda(tabla,
                    p.getFechaVencimientoUtc() != null ?
                            p.getFechaVencimientoUtc().format(formatoFecha) : "-"
            );

            agregarCelda(tabla,
                    p.getFechaDevolucionUtc() != null ?
                            p.getFechaDevolucionUtc().format(formatoFecha) : "Pendiente"
            );

            agregarCelda(tabla, p.getEstado());
            agregarCelda(tabla, String.format("Q%.2f", p.getMultaCalculada()));
        }

        document.add(tabla);
    }

    // Métodos auxiliares para crear celdas en PDF
    private void agregarCeldaEncabezado(Table tabla, String texto) {
        com.itextpdf.layout.element.Cell celda = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(texto))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9);
        tabla.addCell(celda);
    }

    private void agregarCelda(Table tabla, String texto) {
        com.itextpdf.layout.element.Cell celda = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(texto))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.LEFT);
        tabla.addCell(celda);
    }

    private void agregarResumenFinal(Document document, List<Prestamos> prestamos) {
        double totalMultas = prestamos.stream()
                .mapToDouble(Prestamos::getMultaCalculada)
                .sum();

        Paragraph resumen = new Paragraph(
                String.format("\nTotal de Préstamos: %d | Total Multas Generadas: Q%.2f",
                        prestamos.size(), totalMultas)
        )
                .setFontSize(10)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10);

        document.add(resumen);
    }

    // ========== MÉTODOS AUXILIARES PARA EXCEL ==========

    private int agregarEstadisticasExcel(Sheet sheet, int rowNum, List<Prestamos> prestamos, CellStyle estilo) {
        rowNum++; // Espacio

        int totalPrestamos = prestamos.size();
        long activos = prestamos.stream().filter(p -> "ACTIVO".equals(p.getEstado())).count();
        long atrasados = prestamos.stream().filter(p -> "ATRASADO".equals(p.getEstado())).count();
        long cerrados = prestamos.stream().filter(p -> "CERRADO".equals(p.getEstado())).count();
        long cancelados = prestamos.stream().filter(p -> "CANCELADO".equals(p.getEstado())).count();

        Row fila1 = sheet.createRow(rowNum++);
        fila1.createCell(0).setCellValue("Total Préstamos:");
        fila1.createCell(1).setCellValue(totalPrestamos);

        Row fila2 = sheet.createRow(rowNum++);
        fila2.createCell(0).setCellValue("Activos:");
        fila2.createCell(1).setCellValue(activos);
        fila2.createCell(2).setCellValue("Atrasados:");
        fila2.createCell(3).setCellValue(atrasados);

        Row fila3 = sheet.createRow(rowNum++);
        fila3.createCell(0).setCellValue("Cerrados:");
        fila3.createCell(1).setCellValue(cerrados);
        fila3.createCell(2).setCellValue("Cancelados:");
        fila3.createCell(3).setCellValue(cancelados);

        return rowNum;
    }

    // Métodos para crear estilos de Excel (duplicados localmente para independencia)
    private CellStyle crearEstiloTitulo(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }

    private CellStyle crearEstiloCelda(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        short colorGris = IndexedColors.GREY_25_PERCENT.getIndex();
        estilo.setBottomBorderColor(colorGris);
        estilo.setTopBorderColor(colorGris);
        estilo.setLeftBorderColor(colorGris);
        estilo.setRightBorderColor(colorGris);
        return estilo;
    }

    private CellStyle crearEstiloCeldaNumerica(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        short colorGris = IndexedColors.GREY_25_PERCENT.getIndex();
        estilo.setBottomBorderColor(colorGris);
        estilo.setTopBorderColor(colorGris);
        estilo.setLeftBorderColor(colorGris);
        estilo.setRightBorderColor(colorGris);
        estilo.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return estilo;
    }

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Genera ambos reportes (PDF y Excel) y retorna sus rutas.
     */
    public File[] generarAmbosReportes(LocalDate fechaInicio, LocalDate fechaFin, String estadoFiltro) throws Exception {
        File pdf = generarPDF(fechaInicio, fechaFin, estadoFiltro);
        File excel = generarExcel(fechaInicio, fechaFin, estadoFiltro);
        return new File[]{pdf, excel};
    }
}