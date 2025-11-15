package app.reportes;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

/**
 * Utilidades para generación de reportes Excel usando Apache POI.
 * Proporciona métodos comunes para crear hojas, estilos y formatos.
 */
public class GeneradorExcel {

    /**
     * Crea un nuevo libro de Excel con estilos predefinidos.
     */
    public static XSSFWorkbook crearLibro() {
        return new XSSFWorkbook();
    }

    /**
     * Guarda el libro de Excel en la ruta especificada.
     */
    public static File guardarLibro(XSSFWorkbook workbook, String rutaArchivo) throws Exception {
        File archivo = new File(rutaArchivo);
        archivo.getParentFile().mkdirs(); // Crear directorios si no existen

        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            workbook.write(fos);
        }

        return archivo;
    }

    /**
     * Crea una hoja en el libro.
     */
    public static Sheet crearHoja(XSSFWorkbook workbook, String nombre) {
        return workbook.createSheet(nombre);
    }

    /**
     * Crea el estilo para el título principal.
     */
    public static CellStyle crearEstiloTitulo(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();

        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        fuente.setColor(IndexedColors.DARK_BLUE.getIndex());

        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        return estilo;
    }

    /**
     * Crea el estilo para encabezados de tabla.
     */
    public static CellStyle crearEstiloHeader(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();

        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 11);
        fuente.setColor(IndexedColors.WHITE.getIndex());

        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);

        return estilo;
    }

    /**
     * Crea el estilo para celdas de datos normales.
     */
    public static CellStyle crearEstiloCelda(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();

        estilo.setAlignment(HorizontalAlignment.LEFT);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);

        // Usar short directamente para los colores de bordes
        short colorGris = IndexedColors.GREY_25_PERCENT.getIndex();
        estilo.setBottomBorderColor(colorGris);
        estilo.setTopBorderColor(colorGris);
        estilo.setLeftBorderColor(colorGris);
        estilo.setRightBorderColor(colorGris);

        return estilo;
    }

    /**
     * Crea el estilo para celdas de datos numéricos.
     */
    public static CellStyle crearEstiloCeldaNumero(XSSFWorkbook workbook) {
        CellStyle estilo = crearEstiloCelda(workbook);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }

    /**
     * Crea el estilo para celdas de montos monetarios.
     */
    public static CellStyle crearEstiloMoneda(XSSFWorkbook workbook) {
        CellStyle estilo = crearEstiloCeldaNumero(workbook);
        DataFormat formato = workbook.createDataFormat();
        estilo.setDataFormat(formato.getFormat("Q #,##0.00"));
        return estilo;
    }

    /**
     * Crea el estilo para celdas de fecha.
     */
    public static CellStyle crearEstiloFecha(XSSFWorkbook workbook) {
        CellStyle estilo = crearEstiloCelda(workbook);
        DataFormat formato = workbook.createDataFormat();
        estilo.setDataFormat(formato.getFormat("dd/mm/yyyy"));
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    /**
     * Crea el estilo para filas de totales.
     */
    public static CellStyle crearEstiloTotal(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();

        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 11);

        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        estilo.setBorderTop(BorderStyle.MEDIUM);
        estilo.setBorderBottom(BorderStyle.DOUBLE);

        DataFormat formato = workbook.createDataFormat();
        estilo.setDataFormat(formato.getFormat("Q #,##0.00"));

        return estilo;
    }

    /**
     * Agrega el encabezado del reporte (título, subtítulo, fecha).
     */
    public static int agregarEncabezado(Sheet hoja, XSSFWorkbook workbook, String titulo, String subtitulo, LocalDate fecha, int numColumnas) {
        int filaActual = 0;

        // Título principal
        Row filaTitulo = hoja.createRow(filaActual++);
        Cell celdaTitulo = filaTitulo.createCell(0);
        celdaTitulo.setCellValue(titulo);
        celdaTitulo.setCellStyle(crearEstiloTitulo(workbook));
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, numColumnas - 1));

        // Subtítulo
        Row filaSubtitulo = hoja.createRow(filaActual++);
        Cell celdaSubtitulo = filaSubtitulo.createCell(0);
        celdaSubtitulo.setCellValue("Sistema Integral de Gestión de Biblioteca");
        CellStyle estiloSubtitulo = workbook.createCellStyle();
        estiloSubtitulo.setAlignment(HorizontalAlignment.CENTER);
        Font fuenteSubtitulo = workbook.createFont();
        fuenteSubtitulo.setItalic(true);
        fuenteSubtitulo.setFontHeightInPoints((short) 10);
        estiloSubtitulo.setFont(fuenteSubtitulo);
        celdaSubtitulo.setCellStyle(estiloSubtitulo);
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, numColumnas - 1));

        // Línea en blanco
        filaActual++;

        // Fecha de generación
        Row filaFecha = hoja.createRow(filaActual++);
        Cell celdaFecha = filaFecha.createCell(0);
        celdaFecha.setCellValue("Fecha de generación: " + ReporteBase.FORMATO_FECHA.format(fecha));

        // Subtítulo adicional (si existe)
        if (subtitulo != null && !subtitulo.isEmpty()) {
            Row filaSubAdicional = hoja.createRow(filaActual++);
            Cell celdaSubAdicional = filaSubAdicional.createCell(0);
            celdaSubAdicional.setCellValue(subtitulo);
            Font fuenteNegrita = workbook.createFont();
            fuenteNegrita.setBold(true);
            CellStyle estiloSub = workbook.createCellStyle();
            estiloSub.setFont(fuenteNegrita);
            celdaSubAdicional.setCellStyle(estiloSub);
        }

        // Línea en blanco antes de la tabla
        filaActual++;

        return filaActual;
    }

    /**
     * Crea una fila de encabezado con las columnas especificadas.
     */
    public static void crearFilaEncabezado(Sheet hoja, XSSFWorkbook workbook, int numFila, String... columnas) {
        Row fila = hoja.createRow(numFila);
        CellStyle estiloHeader = crearEstiloHeader(workbook);

        for (int i = 0; i < columnas.length; i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(columnas[i]);
            celda.setCellStyle(estiloHeader);
        }
    }

    /**
     * Ajusta automáticamente el ancho de las columnas.
     */
    public static void ajustarAnchoColumnas(Sheet hoja, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            hoja.autoSizeColumn(i);
            // Agregar un poco de padding
            int anchoActual = hoja.getColumnWidth(i);
            hoja.setColumnWidth(i, (int) (anchoActual * 1.1));
        }
    }

    /**
     * Crea una celda con texto.
     */
    public static void crearCelda(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor != null ? valor : "");
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }

    /**
     * Crea una celda con número.
     */
    public static void crearCelda(Row fila, int columna, double valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }

    /**
     * Crea una celda con entero.
     */
    public static void crearCelda(Row fila, int columna, int valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }
}