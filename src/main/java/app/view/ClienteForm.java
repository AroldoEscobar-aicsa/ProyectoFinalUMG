package app.view;

import app.dao.ClienteDAO;
import app.model.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ClienteForm extends JFrame {

    private final ClienteDAO dao = new ClienteDAO();

    private JTextField txtId, txtCodigo, txtNombres, txtApellidos, txtNit, txtTelefono, txtEmail;
    private JCheckBox chkActivo;
    private JComboBox<String> cboEstado; // ACTIVO / BLOQUEADO
    private JSpinner spBloqueadoHasta;   // fecha/hora opcional

    private JButton btnNuevo, btnGuardar, btnEditar, btnEliminar, btnBloquear, btnDesbloquear, btnCerrar, btnRefrescar;
    private JTable tabla;
    private DefaultTableModel modelo;

    private boolean modoEdicion = false;

    public ClienteForm() {
        setTitle("Gestión de Clientes / Lectores");
        setSize(960, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        cargarTabla();
        limpiar();
    }

    private void initUI() {
        JPanel datos = new JPanel(new GridLayout(6, 4, 8, 8));
        datos.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        txtId = new JTextField(); txtId.setEnabled(false);
        txtCodigo = new JTextField();
        txtNombres = new JTextField();
        txtApellidos = new JTextField();
        txtNit = new JTextField();
        txtTelefono = new JTextField();
        txtEmail = new JTextField();

        chkActivo = new JCheckBox("Activo", true);
        cboEstado = new JComboBox<>(new String[]{"ACTIVO", "BLOQUEADO"});
        spBloqueadoHasta = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spBloqueadoHasta, "yyyy-MM-dd HH:mm");
        spBloqueadoHasta.setEditor(editor);

        datos.add(new JLabel("ID:"));                datos.add(txtId);
        datos.add(new JLabel("Código: *"));          datos.add(txtCodigo);

        datos.add(new JLabel("Nombres: *"));         datos.add(txtNombres);
        datos.add(new JLabel("Apellidos: *"));       datos.add(txtApellidos);

        datos.add(new JLabel("NIT (opc.):"));        datos.add(txtNit);
        datos.add(new JLabel("Teléfono (opc.):"));   datos.add(txtTelefono);

        datos.add(new JLabel("Email: *"));           datos.add(txtEmail);
        datos.add(new JLabel("IsActive:"));          datos.add(chkActivo);

        datos.add(new JLabel("Estado:"));            datos.add(cboEstado);
        datos.add(new JLabel("Bloqueado hasta:"));   datos.add(spBloqueadoHasta);

        add(datos, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Código", "Nombres", "Apellidos", "Email", "Teléfono", "NIT", "Activo", "Estado", "BloqueadoHasta"};
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

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botones
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Desactivar");
        btnBloquear = new JButton("Bloquear");
        btnDesbloquear = new JButton("Desbloquear");
        btnRefrescar = new JButton("Refrescar");
        btnCerrar = new JButton("Cerrar");

        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        acciones.add(btnBloquear);
        acciones.add(btnDesbloquear);
        acciones.add(btnRefrescar);
        acciones.add(btnCerrar);
        add(acciones, BorderLayout.SOUTH);

        // Eventos
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEditar.addActionListener(e -> cargarSeleccionado());
        btnEliminar.addActionListener(e -> eliminar());
        btnBloquear.addActionListener(e -> bloquear());
        btnDesbloquear.addActionListener(e -> desbloquear());
        btnRefrescar.addActionListener(e -> cargarTabla());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void limpiar() {
        txtId.setText("");
        txtCodigo.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtNit.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        chkActivo.setSelected(true);
        cboEstado.setSelectedItem("ACTIVO");
        spBloqueadoHasta.setValue(new Date());
        modoEdicion = false;
        txtCodigo.requestFocus();
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0);
            List<Cliente> lista = dao.listarTodos();
            for (Cliente c : lista) {
                modelo.addRow(new Object[]{
                        c.getId(),
                        c.getCodigo(),
                        c.getNombres(),
                        c.getApellidos(),
                        c.getEmail(),
                        c.getTelefono(),
                        c.getNit(),
                        c.isActivo() ? "Sí" : "No",
                        c.getEstado(),
                        c.getBloqueadoHastaUtc()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        // Validaciones
        if (txtCodigo.getText().trim().isEmpty() ||
                txtNombres.getText().trim().isEmpty() ||
                txtApellidos.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Campos obligatorios: Código, Nombres, Apellidos, Email",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Cliente c = new Cliente();
            c.setCodigo(txtCodigo.getText().trim());
            c.setNombres(txtNombres.getText().trim());
            c.setApellidos(txtApellidos.getText().trim());
            c.setNit(blankToNull(txtNit.getText()));
            c.setTelefono(blankToNull(txtTelefono.getText()));
            c.setEmail(txtEmail.getText().trim());
            c.setActivo(chkActivo.isSelected());
            c.setEstado(cboEstado.getSelectedItem().toString());

            Date d = (Date) spBloqueadoHasta.getValue();
            LocalDateTime hasta = d == null ? null :
                    LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            c.setBloqueadoHastaUtc(hasta);

            if (modoEdicion) {
                c.setId(Integer.parseInt(txtId.getText()));
                dao.actualizar(c);
                JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente.");
            } else {
                // unicidad de código
                if (dao.existeCodigo(c.getCodigo())) {
                    JOptionPane.showMessageDialog(this, "El código ya existe. Use uno diferente.", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dao.crear(c);
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente. ID=" + c.getId());
                modoEdicion = true;
                txtId.setText(String.valueOf(c.getId()));
            }
            cargarTabla();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente en la tabla.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int id = (int) modelo.getValueAt(row, 0);
            Cliente c = dao.buscarPorId(id);
            if (c == null) { JOptionPane.showMessageDialog(this, "Cliente no encontrado."); return; }

            txtId.setText(String.valueOf(c.getId()));
            txtCodigo.setText(c.getCodigo());
            txtNombres.setText(c.getNombres());
            txtApellidos.setText(c.getApellidos());
            txtNit.setText(nullToBlank(c.getNit()));
            txtTelefono.setText(nullToBlank(c.getTelefono()));
            txtEmail.setText(nullToBlank(c.getEmail()));
            chkActivo.setSelected(c.isActivo());
            cboEstado.setSelectedItem(c.getEstado() == null ? "ACTIVO" : c.getEstado());

            Date d = c.getBloqueadoHastaUtc() == null ? new Date()
                    : Date.from(c.getBloqueadoHastaUtc().atZone(ZoneId.systemDefault()).toInstant());
            spBloqueadoHasta.setValue(d);

            modoEdicion = true;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) modelo.getValueAt(row, 0);
        int op = JOptionPane.showConfirmDialog(this, "¿Desactivar este cliente (IsActive=0)?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                dao.eliminarLogico(id);
                JOptionPane.showMessageDialog(this, "Cliente desactivado.");
                cargarTabla();
                limpiar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al desactivar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void bloquear() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para bloquear.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int id = (int) modelo.getValueAt(row, 0);
            Date d = (Date) spBloqueadoHasta.getValue();
            LocalDateTime hasta = d == null ? null :
                    LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            dao.bloquear(id, hasta);
            JOptionPane.showMessageDialog(this, "Cliente bloqueado.");
            cargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al bloquear: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desbloquear() {
        int row = tabla.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente para desbloquear.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            int id = (int) modelo.getValueAt(row, 0);
            dao.desbloquear(id);
            JOptionPane.showMessageDialog(this, "Cliente desbloqueado.");
            cargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al desbloquear: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String nullToBlank(String s) { return s == null ? "" : s; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteForm().setVisible(true));
    }
}
