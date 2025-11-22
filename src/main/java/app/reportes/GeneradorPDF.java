package app.reportes;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.time.LocalDate;

/**
 * Utilidades para generación de reportes PDF usando iText 7.
 * Proporciona métodos comunes para crear encabezados, tablas y estilos.
 */
public class GeneradorPDF {

    // Colores corporativos
    private static final DeviceRgb COLOR_PRIMARIO = new DeviceRgb(41, 128, 185); // Azul
    private static final DeviceRgb COLOR_HEADER = new DeviceRgb(52, 73, 94);     // Gris oscuro
    private static final DeviceRgb COLOR_ALTERNO = new DeviceRgb(236, 240, 241);  // Gris claro

    /**
     * Crea un documento PDF básico listo para usar.
     */
    public static Document crearDocumento(String rutaArchivo) throws Exception {
        File archivo = new File(rutaArchivo);
        archivo.getParentFile().mkdirs(); // Crear directorios si no existen

        PdfWriter writer = new PdfWriter(archivo);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Márgenes
        document.setMargins(50, 50, 50, 50);

        return document;
    }

    /**
     * Agrega el encabezado estándar del reporte.
     */
    public static void agregarEncabezado(Document document, String titulo, String subtitulo, LocalDate fecha) {
        // Título principal
        Paragraph tituloParrafo = new Paragraph(titulo)
                .setFontSize(18)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(tituloParrafo);

        // Nombre de la institución
        Paragraph institucion = new Paragraph("Sistema Integral de Gestión de Biblioteca")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(institucion);

        // Subtítulo (si existe)
        if (subtitulo != null && !subtitulo.isEmpty()) {
            Paragraph subtituloParrafo = new Paragraph(subtitulo)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(subtituloParrafo);
        }

        // Fecha de generación
        Paragraph fechaParrafo = new Paragraph("Fecha: " + ReporteBase.FORMATO_FECHA.format(fecha))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20);
        document.add(fechaParrafo);

        // Línea separadora
        document.add(new Paragraph("\n"));
    }

    /**
     * Crea una tabla con el ancho especificado.
     * @param anchos Array con los anchos relativos de las columnas
     */
    public static Table crearTabla(float[] anchos) {
        Table tabla = new Table(UnitValue.createPercentArray(anchos));
        tabla.setWidth(UnitValue.createPercentValue(100));
        return tabla;
    }

    /**
     * Crea una celda de encabezado con estilo.
     */
    public static Cell crearCeldaHeader(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(10))
                .setBackgroundColor(COLOR_HEADER)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    /**
     * Crea una celda de datos normal.
     */
    public static Cell crearCelda(String texto) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "").setFontSize(9))
                .setPadding(5);
    }

    /**
     * Crea una celda de datos con alineación personalizada.
     */
    public static Cell crearCelda(String texto, TextAlignment alineacion) {
        return new Cell()
                .add(new Paragraph(texto != null ? texto : "").setFontSize(9))
                .setTextAlignment(alineacion)
                .setPadding(5);
    }

    /**
     * Crea una celda de total (resaltada).
     */
    public static Cell crearCeldaTotal(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(10))
                .setBackgroundColor(COLOR_ALTERNO)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8);
    }

    /**
     * Agrega una fila con estilo alternado (para mejorar legibilidad).
     */
    public static void agregarFilaAlternada(Table tabla, boolean esImpar, String... valores) {
        for (String valor : valores) {
            Cell celda = crearCelda(valor);
            if (esImpar) {
                celda.setBackgroundColor(COLOR_ALTERNO);
            }
            tabla.addCell(celda);
        }
    }

    /**
     * Agrega un pie de página con totales o resumen.
     */
    public static void agregarPiePagina(Document document, String texto) {
        // Añadimos una línea en blanco para asegurar la separación de la tabla.
        document.add(new Paragraph("\n"));

        Paragraph pie = new Paragraph(texto)
                .setFontSize(9) // Usamos una fuente más pequeña para el pie de página
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10); // Reducimos el margen a 10 puntos (antes era 20)

        document.add(pie);
    }

    /**
     * Agrega un párrafo de resumen o nota.
     */
    public static void agregarResumen(Document document, String titulo, String contenido) {
        Paragraph tituloResumen = new Paragraph(titulo)
                .setFontSize(12)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(5);
        document.add(tituloResumen);

        Paragraph contenidoResumen = new Paragraph(contenido)
                .setFontSize(10)
                .setMarginBottom(10);
        document.add(contenidoResumen);
    }
}