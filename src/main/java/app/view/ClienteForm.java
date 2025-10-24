package app.view;

import app.dao.ClienteDAO;
import app.model.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Formulario para la Gestión de Clientes (Módulo C).
 * Sigue la estructura del ejemplo AutorForm.
 */
public class ClienteForm extends JFrame {

    // 1. Instancia del DAO
    private ClienteDAO clienteDAO = new ClienteDAO();

    // 2. Componentes de UI
    private JTextField txtId, txtNombres, txtApellidos, txtNit, txtTelefono, txtEmail;
    private JComboBox<String> cboEstado;
    private JButton btnNuevo, btnGuardar, btnEditar, btnEliminar, btnCerrar;
    private JTable tablaClientes;
    private DefaultTableModel modelo;

    // 3. Estado
    private boolean modoEdicion = false;

    public ClienteForm() {
        setTitle("Gestión de Clientes / Lectores");
        // Ajustamos el tamaño para que quepan los nuevos campos
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ===== Panel de datos =====
        // Aumentamos las filas del GridLayout para los campos del Cliente
        JPanel panelDatos = new JPanel(new GridLayout(7, 2, 5, 5));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        panelDatos.add(new JLabel("ID:"));
        txtId = new JTextField();
        txtId.setEnabled(false); // El ID no se edita
        panelDatos.add(txtId);

        panelDatos.add(new JLabel("Nombres:"));
        txtNombres = new JTextField();
        panelDatos.add(txtNombres);

        panelDatos.add(new JLabel("Apellidos:"));
        txtApellidos = new JTextField();
        panelDatos.add(txtApellidos);

        panelDatos.add(new JLabel("NIT (Opcional):"));
        txtNit = new JTextField();
        panelDatos.add(txtNit);

        panelDatos.add(new JLabel("Teléfono (Opcional):"));
        txtTelefono = new JTextField();
        panelDatos.add(txtTelefono);

        panelDatos.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelDatos.add(txtEmail);

        panelDatos.add(new JLabel("Estado:"));
        cboEstado = new JComboBox<>(new String[]{"Activo", "Bloqueado"});
        panelDatos.add(cboEstado);

        add(panelDatos, BorderLayout.NORTH);

        // ===== Tabla =====
        // Ajustamos las columnas de la tabla al modelo Cliente
        String[] columnas = {"ID", "Nombres", "Apellidos", "Email", "Teléfono", "NIT", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            // Hacemos que la tabla no sea editable directamente
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaClientes = new JTable(modelo);
        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // ===== Botones (Idénticos al ejemplo) =====
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnCerrar = new JButton("Cerrar");
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCerrar);
        add(panelBotones, BorderLayout.SOUTH);

        // ===== Eventos =====
        btnNuevo.addActionListener(e -> limpiarCampos());
        btnGuardar.addActionListener(e -> guardarCliente());
        // El botón Editar ahora se llama 'cargarSeleccionado'
        btnEditar.addActionListener(e -> cargarSeleccionado());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnCerrar.addActionListener(e -> dispose()); // Cierra solo esta ventana

        // Evento de doble clic en la tabla (idéntico al ejemplo)
        tablaClientes.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                // Doble clic
                if (evt.getClickCount() == 2) {
                    cargarSeleccionado();
                }
            }
        });

        // Carga inicial de datos
        cargarTabla();
        limpiarCampos(); // Inicia en modo "Nuevo"
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtNit.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        cboEstado.setSelectedItem("Activo");

        txtNombres.requestFocus(); // Pone el foco en el primer campo
        modoEdicion = false;

        // Habilitar/deshabilitar botones
        btnGuardar.setEnabled(true);
        btnEditar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private void cargarTabla() {
        try {
            modelo.setRowCount(0); // Limpia la tabla
            // Usamos el método del DAO
            List<Cliente> lista = clienteDAO.listarClientesActivos();

            for (Cliente c : lista) {
                // Añadimos las columnas en el orden definido
                modelo.addRow(new Object[]{
                        c.getIdCliente(),
                        c.getNombres(),
                        c.getApellidos(),
                        c.getEmail(),
                        c.getTelefono(),
                        c.getNit(),
                        c.getEstado() // "Activo" o "Bloqueado"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void guardarCliente() {
        // 1. Validaciones
        String nombres = txtNombres.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email = txtEmail.getText().trim();

        if (nombres.isEmpty() || apellidos.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombres, Apellidos y Email son obligatorios.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombres.requestFocus();
            return;
        }

        // 2. Crear el objeto Cliente
        Cliente c = new Cliente();
        c.setNombres(nombres);
        c.setApellidos(apellidos);
        c.setEmail(email);
        c.setNit(txtNit.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setEstado(cboEstado.getSelectedItem().toString());
        // La fecha de registro y 'eliminado' se manejan en el DAO/BD

        try {
            if (modoEdicion) {
                // 3a. Actualizar (Modo Edición)
                c.setIdCliente(Integer.parseInt(txtId.getText()));
                clienteDAO.actualizarCliente(c);
                JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente.");
            } else {
                // 3b. Registrar (Modo Nuevo)
                clienteDAO.registrarCliente(c);
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente.");
            }

            // 4. Refrescar
            limpiarCampos();
            cargarTabla();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar cliente: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: ID inválido.",
                    "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccionado() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Cargamos los datos desde el modelo de la tabla
        txtId.setText(modelo.getValueAt(fila, 0).toString());
        txtNombres.setText(modelo.getValueAt(fila, 1).toString());
        txtApellidos.setText(modelo.getValueAt(fila, 2).toString());
        txtEmail.setText(modelo.getValueAt(fila, 3).toString());
        txtTelefono.setText(modelo.getValueAt(fila, 4).toString());
        txtNit.setText(modelo.getValueAt(fila, 5).toString());
        cboEstado.setSelectedItem(modelo.getValueAt(fila, 6).toString());

        modoEdicion = true;

        // Deshabilitar botones para evitar clics accidentales
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
    }

    private void eliminarCliente() {
        int fila = tablaClientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para eliminar.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());

        // Confirmación (Req. 89)
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar (baja lógica) a este cliente?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                // Usamos el método de eliminación lógica del DAO
                clienteDAO.eliminarCliente(id);
                JOptionPane.showMessageDialog(this, "Cliente eliminado (baja lógica) correctamente.");
                cargarTabla();
                limpiarCampos();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar cliente: " + ex.getMessage(),
                        "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}