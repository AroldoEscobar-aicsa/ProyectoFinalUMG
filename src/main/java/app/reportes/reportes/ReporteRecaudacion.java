package app.reportes.reportes;

import app.dao.*;
import app.model.*;
import app.reportes.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de Recaudación por período.
 * Muestra los ingresos de caja (cobros de multas) en un rango de fechas.
 */
public class ReporteRecaudacion extends ReporteBase {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<CajaMovimiento> movimientos;
    private double totalRecaudado;

    private final CajaMovimientoDAO cajaMovimientoDAO;
    private final ClienteDAO clienteDAO;

    public ReporteRecaudacion(LocalDate fechaInicio, LocalDate fechaFin) {
        super("Reporte de Recaudación");
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.subtitulo = String.format("Período: %s al %s",
                formatearFecha(fechaInicio),
                formatearFecha(fechaFin));

        this.cajaMovimientoDAO = new CajaMovimientoDAO();
        this.clienteDAO = new ClienteDAO();
    }

    /**
     * Carga los datos del reporte desde la base de datos.
     */
    private void cargarDatos() throws Exception {
        // Obtener movimientos de tipo ENTRADA (ingresos) en el período
        this.movimientos = cajaMovimientoDAO.getMovimientosPorRangoFechas(fechaInicio, fechaFin, "ENTRADA");

        // Calcular total
        this.totalRecaudado = movimientos.stream()
                .mapToDouble(CajaMovimiento::getMonto)
                .sum();
    }

    @Override
    public File generarPDF(String rutaDestino) throws Exception {
        cargarDatos();

        String nombreArchivo = generarNombreArchivo("Reporte_Recaudacion", "pdf");
        String rutaCompleta = rutaDestino + File.separator + nombreArchivo;

        Document document = GeneradorPDF.crearDocumento(rutaCompleta);

        // Encabezado
        GeneradorPDF.agregarEncabezado(document, titulo, subtitulo, fechaGeneracion);

        // Resumen ejecutivo
        String resumen = String.format(
                "Total de ingresos registrados: %d movimientos\n" +
                        "Monto total recaudado: %s\n" +
                        "Promedio por movimiento: %s",
                movimientos.size(),
                formatearMonto(totalRecaudado),
                formatearMonto(movimientos.isEmpty() ? 0 : totalRecaudado / movimientos.size())
        );
        GeneradorPDF.agregarResumen(document, "Resumen Ejecutivo", resumen);

        // Tabla de movimientos
        if (!movimientos.isEmpty()) {
            float[] anchos = {1.5f, 3f, 2f, 1.5f};
            Table tabla = GeneradorPDF.crearTabla(anchos);

            // Headers
            tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Fecha/Hora"));
            tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Concepto"));
            tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("ID Multa"));
            tabla.addHeaderCell(GeneradorPDF.crearCeldaHeader("Monto"));

            // Datos
            boolean impar = true;
            for (CajaMovimiento mov : movimientos) {
                String fecha = mov.getCreadoUtc().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                String concepto = mov.getConcepto();
                String idMulta = mov.getIdMulta() != null ? String.valueOf(mov.getIdMulta()) : "-";
                String monto = formatearMonto(mov.getMonto());

                GeneradorPDF.agregarFilaAlternada(tabla, impar, fecha, concepto, idMulta, monto);
                impar = !impar;
            }

            // Fila de total
            tabla.addCell(GeneradorPDF.crearCeldaTotal(""));
            tabla.addCell(GeneradorPDF.crearCeldaTotal(""));
            tabla.addCell(GeneradorPDF.crearCeldaTotal("TOTAL:"));
            tabla.addCell(GeneradorPDF.crearCeldaTotal(formatearMonto(totalRecaudado)));

            document.add(tabla);
        } else {
            document.add(new com.itextpdf.layout.element.Paragraph("No se encontraron movimientos en el período especificado.")
                    .setFontSize(10)
                    .setItalic());
        }

        // Pie de página
        GeneradorPDF.agregarPiePagina(document,
                "Reporte generado por Sistema Integral de Gestión de Biblioteca");

        document.close();
        return new File(rutaCompleta);
    }

    @Override
    public File generarExcel(String rutaDestino) throws Exception {
        cargarDatos();

        String nombreArchivo = generarNombreArchivo("Reporte_Recaudacion", "xlsx");
        String rutaCompleta = rutaDestino + File.separator + nombreArchivo;

        XSSFWorkbook workbook = GeneradorExcel.crearLibro();
        Sheet hoja = GeneradorExcel.crearHoja(workbook, "Recaudación");

        // Encabezado
        int filaActual = GeneradorExcel.agregarEncabezado(hoja, workbook, titulo, subtitulo, fechaGeneracion, 5);

        // Resumen
        Row filaResumen = hoja.createRow(filaActual++);
        filaResumen.createCell(0).setCellValue("Total movimientos:");
        filaResumen.createCell(1).setCellValue(movimientos.size());

        Row filaTotal = hoja.createRow(filaActual++);
        filaTotal.createCell(0).setCellValue("Total recaudado:");
        Cell celdaTotal = filaTotal.createCell(1);
        celdaTotal.setCellValue(totalRecaudado);
        celdaTotal.setCellStyle(GeneradorExcel.crearEstiloMoneda(workbook));

        filaActual++; // Línea en blanco

        // Headers de la tabla
        GeneradorExcel.crearFilaEncabezado(hoja, workbook, filaActual++,
                "Fecha/Hora", "Concepto", "ID Sesión", "ID Multa", "Monto");

        // Estilos
        CellStyle estiloCelda = GeneradorExcel.crearEstiloCelda(workbook);
        CellStyle estiloNumero = GeneradorExcel.crearEstiloCeldaNumero(workbook);
        CellStyle estiloMoneda = GeneradorExcel.crearEstiloMoneda(workbook);

        // Datos
        for (CajaMovimiento mov : movimientos) {
            Row fila = hoja.createRow(filaActual++);

            String fecha = mov.getCreadoUtc().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            GeneradorExcel.crearCelda(fila, 0, fecha, estiloCelda);
            GeneradorExcel.crearCelda(fila, 1, mov.getConcepto(), estiloCelda);
            GeneradorExcel.crearCelda(fila, 2, mov.getIdCajaSesion(), estiloNumero);

            if (mov.getIdMulta() != null) {
                GeneradorExcel.crearCelda(fila, 3, mov.getIdMulta(), estiloNumero);
            } else {
                GeneradorExcel.crearCelda(fila, 3, "-", estiloCelda);
            }

            GeneradorExcel.crearCelda(fila, 4, mov.getMonto(), estiloMoneda);
        }

        // Fila de total
        Row filaGrandTotal = hoja.createRow(filaActual);
        filaGrandTotal.createCell(0);
        filaGrandTotal.createCell(1);
        filaGrandTotal.createCell(2);
        Cell celdaTituloTotal = filaGrandTotal.createCell(3);
        celdaTituloTotal.setCellValue("TOTAL:");
        CellStyle estiloTotal = GeneradorExcel.crearEstiloTotal(workbook);
        celdaTituloTotal.setCellStyle(estiloTotal);

        Cell celdaMontoTotal = filaGrandTotal.createCell(4);
        celdaMontoTotal.setCellValue(totalRecaudado);
        celdaMontoTotal.setCellStyle(estiloTotal);

        // Ajustar anchos
        GeneradorExcel.ajustarAnchoColumnas(hoja, 5);

        return GeneradorExcel.guardarLibro(workbook, rutaCompleta);
    }

    // Getters
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public double getTotalRecaudado() {
        return totalRecaudado;
    }
}