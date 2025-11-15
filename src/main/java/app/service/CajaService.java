package app.service;

import app.dao.*;
import app.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de lógica de negocio para el módulo de Caja.
 * Coordina operaciones entre CajaSesion, CajaMovimientos y Multas.
 */
public class CajaService {

    private final CajaSesionDAO cajaSesionDAO;
    private final CajaMovimientoDAO cajaMovimientoDAO;
    private final MultaDAO multaDAO;
    private final ClienteDAO clienteDAO;

    public CajaService() {
        this.cajaSesionDAO = new CajaSesionDAO();
        this.cajaMovimientoDAO = new CajaMovimientoDAO();
        this.multaDAO = new MultaDAO();
        this.clienteDAO = new ClienteDAO();
    }

    // ========== OPERACIONES DE SESIÓN ==========

    /**
     * Abre una nueva caja para el usuario.
     * Valida que no tenga una caja abierta previamente.
     */
    public CajaSesion abrirCaja(int idUsuario) throws SQLException {
        if (cajaSesionDAO.tieneCajaAbierta(idUsuario)) {
            throw new IllegalStateException("Ya existe una caja abierta para este usuario.");
        }

        return cajaSesionDAO.abrirCaja(idUsuario);
    }

    /**
     * Cierra la caja del usuario.
     * Calcula el arqueo: diferencia entre lo contado y lo esperado.
     */
    public ResumenArqueo cerrarCaja(int idCajaSesion, double montoRealContado, String observacion) throws SQLException {
        // Obtener la sesión
        CajaSesion sesion = cajaSesionDAO.buscarPorId(idCajaSesion);
        if (sesion == null) {
            throw new IllegalArgumentException("Sesión de caja no encontrada.");
        }

        if (!"ABIERTA".equals(sesion.getEstado())) {
            throw new IllegalStateException("La caja ya está cerrada.");
        }

        // Calcular totales del sistema
        double totalSistema = cajaMovimientoDAO.calcularTotalSesion(idCajaSesion);
        double diferencia = montoRealContado - totalSistema;

        // Crear observación con arqueo
        String obsCompleta = String.format(
                "Arqueo - Sistema: Q%.2f | Real: Q%.2f | Diferencia: Q%.2f",
                totalSistema, montoRealContado, diferencia
        );

        if (observacion != null && !observacion.trim().isEmpty()) {
            obsCompleta += " | Notas: " + observacion;
        }

        // Cerrar la sesión
        boolean cerrada = cajaSesionDAO.cerrarCaja(idCajaSesion, obsCompleta);

        if (!cerrada) {
            throw new SQLException("No se pudo cerrar la caja.");
        }

        // Retornar resumen
        ResumenArqueo resumen = new ResumenArqueo();
        resumen.setIdCajaSesion(idCajaSesion);
        resumen.setMontoSistema(totalSistema);
        resumen.setMontoRealContado(montoRealContado);
        resumen.setDiferencia(diferencia);
        resumen.setObservacion(obsCompleta);

        return resumen;
    }

    /**
     * Obtiene la caja abierta del usuario (si existe).
     */
    public CajaSesion getCajaAbierta(int idUsuario) throws SQLException {
        return cajaSesionDAO.getCajaAbierta(idUsuario);
    }

    /**
     * Verifica si el usuario tiene caja abierta.
     */
    public boolean tieneCajaAbierta(int idUsuario) throws SQLException {
        return cajaSesionDAO.tieneCajaAbierta(idUsuario);
    }

    // ========== OPERACIONES DE COBRO ==========

    /**
     * Registra el pago de una multa en la caja actual del usuario.
     * Actualiza la multa a PAGADA y reduce la mora del cliente.
     */
    public boolean cobrarMulta(int idMulta, int idUsuario) throws SQLException {
        // Verificar que el usuario tenga caja abierta
        CajaSesion cajaAbierta = getCajaAbierta(idUsuario);
        if (cajaAbierta == null) {
            throw new IllegalStateException("Debe abrir una caja antes de realizar cobros.");
        }

        // Obtener la multa
        Multa multa = multaDAO.buscarPorId(idMulta);
        if (multa == null) {
            throw new IllegalArgumentException("Multa no encontrada.");
        }

        if (!"PENDIENTE".equalsIgnoreCase(multa.getEstado())) {
            throw new IllegalStateException("Solo se pueden cobrar multas pendientes.");
        }

        // Registrar el movimiento de entrada
        CajaMovimiento movimiento = new CajaMovimiento();
        movimiento.setIdCajaSesion(cajaAbierta.getId());
        movimiento.setTipo("ENTRADA");
        movimiento.setConcepto("Pago Multa #" + idMulta + " - Cliente #" + multa.getIdCliente());
        movimiento.setMonto(multa.getMontoCalculado());
        movimiento.setIdMulta(idMulta);

        boolean movimientoCreado = cajaMovimientoDAO.reistrar(movimiento);
        if (!movimientoCreado) {
            throw new SQLException("No se pudo registrar el movimiento de caja.");
        }

        // Actualizar la multa a PAGADA
        boolean multaActualizada = multaDAO.actualizarPago(idMulta, multa.getMontoCalculado(), "PAGADA");
        if (!multaActualizada) {
            throw new SQLException("No se pudo actualizar el estado de la multa.");
        }

        // Reducir la mora acumulada del cliente
        Cliente cliente = clienteDAO.buscarPorId(multa.getIdCliente());
        if (cliente != null) {
            double nuevaMora = Math.max(0, cliente.getMoraAcumulada() - multa.getMontoCalculado());
            clienteDAO.actualizarMoraAcumulada(cliente.getId(), nuevaMora);
        }

        return true;
    }

    /**
     * Cobra múltiples multas en una sola operación.
     */
    public int cobrarMultasMultiples(List<Integer> idsMultas, int idUsuario) throws SQLException {
        int cobradas = 0;

        for (Integer idMulta : idsMultas) {
            try {
                if (cobrarMulta(idMulta, idUsuario)) {
                    cobradas++;
                }
            } catch (Exception e) {
                // Continuar con las siguientes aunque una falle
                System.err.println("Error cobrando multa " + idMulta + ": " + e.getMessage());
            }
        }

        return cobradas;
    }

    // ========== CONSULTAS Y REPORTES ==========

    /**
     * Obtiene el resumen del día actual para el usuario.
     */
    public ResumenCajaDia getResumenDelDia(int idUsuario) throws SQLException {
        CajaSesion cajaAbierta = getCajaAbierta(idUsuario);

        ResumenCajaDia resumen = new ResumenCajaDia();

        if (cajaAbierta != null) {
            resumen.setCajaAbierta(true);
            resumen.setIdCajaSesion(cajaAbierta.getId());

            List<CajaMovimiento> movimientos = cajaMovimientoDAO.getMovimientosPorSesion(cajaAbierta.getId());
            resumen.setMovimientos(movimientos);

            double totalEntradas = movimientos.stream()
                    .filter(m -> "ENTRADA".equals(m.getTipo()))
                    .mapToDouble(CajaMovimiento::getMonto)
                    .sum();

            double totalSalidas = movimientos.stream()
                    .filter(m -> "SALIDA".equals(m.getTipo()))
                    .mapToDouble(CajaMovimiento::getMonto)
                    .sum();

            resumen.setTotalEntradas(totalEntradas);
            resumen.setTotalSalidas(totalSalidas);
            resumen.setTotalCaja(totalEntradas - totalSalidas);
        } else {
            resumen.setCajaAbierta(false);
        }

        return resumen;
    }

    /**
     * Obtiene el historial de sesiones del usuario.
     */
    public List<CajaSesion> getHistorialSesiones(int idUsuario, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return cajaSesionDAO.getSesionesPorUsuarioYFecha(idUsuario, fechaInicio, fechaFin);
    }

    /**
     * Calcula el total recaudado en un rango de fechas.
     */
    public double getTotalRecaudado(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        return cajaMovimientoDAO.getTotalRecaudado(fechaInicio, fechaFin);
    }

    // ========== CLASES INTERNAS PARA REPORTES ==========

    /**
     * Clase para encapsular el resultado del arqueo de caja.
     */
    public static class ResumenArqueo {
        private int idCajaSesion;
        private double montoSistema;
        private double montoRealContado;
        private double diferencia;
        private String observacion;

        // Getters y Setters
        public int getIdCajaSesion() { return idCajaSesion; }
        public void setIdCajaSesion(int idCajaSesion) { this.idCajaSesion = idCajaSesion; }

        public double getMontoSistema() { return montoSistema; }
        public void setMontoSistema(double montoSistema) { this.montoSistema = montoSistema; }

        public double getMontoRealContado() { return montoRealContado; }
        public void setMontoRealContado(double montoRealContado) { this.montoRealContado = montoRealContado; }

        public double getDiferencia() { return diferencia; }
        public void setDiferencia(double diferencia) { this.diferencia = diferencia; }

        public String getObservacion() { return observacion; }
        public void setObservacion(String observacion) { this.observacion = observacion; }
    }

    /**
     * Clase para el resumen del día de caja.
     */
    public static class ResumenCajaDia {
        private boolean cajaAbierta;
        private int idCajaSesion;
        private List<CajaMovimiento> movimientos;
        private double totalEntradas;
        private double totalSalidas;
        private double totalCaja;

        // Getters y Setters
        public boolean isCajaAbierta() { return cajaAbierta; }
        public void setCajaAbierta(boolean cajaAbierta) { this.cajaAbierta = cajaAbierta; }

        public int getIdCajaSesion() { return idCajaSesion; }
        public void setIdCajaSesion(int idCajaSesion) { this.idCajaSesion = idCajaSesion; }

        public List<CajaMovimiento> getMovimientos() { return movimientos; }
        public void setMovimientos(List<CajaMovimiento> movimientos) { this.movimientos = movimientos; }

        public double getTotalEntradas() { return totalEntradas; }
        public void setTotalEntradas(double totalEntradas) { this.totalEntradas = totalEntradas; }

        public double getTotalSalidas() { return totalSalidas; }
        public void setTotalSalidas(double totalSalidas) { this.totalSalidas = totalSalidas; }

        public double getTotalCaja() { return totalCaja; }
        public void setTotalCaja(double totalCaja) { this.totalCaja = totalCaja; }
    }
}