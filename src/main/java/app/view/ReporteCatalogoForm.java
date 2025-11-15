package app.view;

import app.dao.ReporteDAO; // Importamos el DAO que ya creamos
import app.model.ReporteCatalogo; // Importamos el Modelo que ya creamos

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Este formulario (JDialog) muestra el reporte de Catálogo/Inventario.
 * Utiliza un SwingWorker para cargar los datos de forma asíncrona (sin congelar).
 */
public class ReporteCatalogoForm extends JDialog {

    private JTable tablaReporte;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstado;
    private ReporteDAO reporteDAO;

    public ReporteCatalogoForm(Frame owner) {
        super(owner, "Reporte: Catálogo de Libros (Inventario)", true);

        this.reporteDAO = new ReporteDAO();

        inicializarComponentes();
        cargarDatosAsync(); // Carga de datos asíncrona
    }

    private void inicializarComponentes() {
        setSize(1000, 600); // Más grande, este reporte tiene muchas columnas
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Reporte de Catálogo de Libros (Inventario)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setBorder(new EmptyBorder(5, 0, 15, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // --- La Tabla ---
        String[] columnas = {
                "Título", "Autores", "Categorías", "ISBN",
                "Editorial", "Año", "Total Copias", "Disponibles"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReporte = new JTable(modeloTabla);
        tablaReporte.setRowHeight(24);
        tablaReporte.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Ajustar anchos de columna (opcional, pero recomendado)
        tablaReporte.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Importante para scroll horizontal
        TableColumnModel tcm = tablaReporte.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(250); // Título
        tcm.getColumn(1).setPreferredWidth(200); // Autores
        tcm.getColumn(2).setPreferredWidth(150); // Categorías
        tcm.getColumn(3).setPreferredWidth(120); // ISBN
        tcm.getColumn(4).setPreferredWidth(120); // Editorial
        tcm.getColumn(5).setPreferredWidth(60);  // Año
        tcm.getColumn(6).setPreferredWidth(90);  // Total Copias
        tcm.getColumn(7).setPreferredWidth(90);  // Disponibles

        // Ponemos la tabla en un JScrollPane (permite scroll vertical Y horizontal)
        JScrollPane scrollPane = new JScrollPane(tablaReporte);
        add(scrollPane, BorderLayout.CENTER);

        // --- Barra de estado inferior ---
        lblEstado = new JLabel("Cargando reporte de catálogo, por favor espere...");
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblEstado.setBorder(new EmptyBorder(10, 0, 0, 0));
        add(lblEstado, BorderLayout.SOUTH);
    }

    /**
     * Carga de datos con SwingWorker.
     */
    private void cargarDatosAsync() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<List<ReporteCatalogo>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<ReporteCatalogo> doInBackground() throws Exception {
                // Esta es la consulta más pesada
                return reporteDAO.getReporteCatalogoCompleto();
            }

            @Override
            protected void done() {
                try {
                    List<ReporteCatalogo> resultados = get();
                    modeloTabla.setRowCount(0);

                    for (ReporteCatalogo item : resultados) {
                        modeloTabla.addRow(new Object[]{
                                item.getTitulo(),
                                item.getAutores(),
                                item.getCategorias(),
                                item.getIsbn(),
                                item.getEditorial(),
                                item.getAnio(),
                                item.getTotalCopias(),
                                item.getCopiasDisponibles()
                        });
                    }

                    lblEstado.setText("Reporte generado con éxito. " + resultados.size() + " libros encontrados.");

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    lblEstado.setText("Error al generar el reporte de catálogo.");
                    JOptionPane.showMessageDialog(
                            ReporteCatalogoForm.this,
                            "No se pudo cargar el reporte: " + e.getMessage(),
                            "Error de Reporte",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        worker.execute();
    }
}