package fxmlfitnutrition.vistas.paciente;

import dominio.DireccionImp;
import dominio.PacienteImp;
import dto.RSRegistroPaciente;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pojo.Direccion;
import pojo.Domicilio;
import pojo.Paciente;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;
import utilidad.Validaciones;

public class FXMLFormularioPacienteController implements Initializable {

    @FXML private Label lbTitulo;
    @FXML private TextField tfNombre;
    @FXML private TextField tfPrimerApellido;
    @FXML private TextField tfSegundoApellido;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private TextField tfTelefono;
    @FXML private TextField tfCorreo;

    @FXML private TextField tfCalle;
    @FXML private TextField tfNumero;
    @FXML private TextField tfCodigoPostal;
    @FXML private ComboBox<Direccion> cbColonia;
    @FXML private TextField tfCiudad;
    @FXML private TextField tfEstado;

    @FXML private Label lbMensajeError;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardar;

    private NotificacionOperacion observador;
    private Paciente pacienteEdicion;
    private List<Direccion> coloniasActuales;

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarParaEdicion(Paciente paciente) {
        this.pacienteEdicion = paciente;
        lbTitulo.setText("Editar Paciente");

        tfNombre.setText(paciente.getNombre());
        tfPrimerApellido.setText(paciente.getPrimerApellido());
        tfSegundoApellido.setText(paciente.getSegundoApellido());

        if (paciente.getFechaNacimiento() != null && !paciente.getFechaNacimiento().isEmpty()) {
            dpFechaNacimiento.setValue(java.time.LocalDate.parse(paciente.getFechaNacimiento()));
        }

        cbSexo.setValue(paciente.getSexo());
        tfTelefono.setText(paciente.getTelefono());
        tfCorreo.setText(paciente.getEmail());

        if (paciente.getDomicilio() != null) {
            Domicilio dom = paciente.getDomicilio();
            tfCalle.setText(dom.getCalle());
            tfNumero.setText(dom.getNumero());

            if (dom.getCodigoPostal() != null && !dom.getCodigoPostal().isEmpty()) {
                tfCodigoPostal.setText(dom.getCodigoPostal());
                // Trigger postal code lookup to populate cbColonia
                buscarDireccionPorCodigoPostal(dom.getCodigoPostal());
                // Pre-select the patient's colonia
                if (coloniasActuales != null) {
                    for (Direccion d : coloniasActuales) {
                        if (d.getIdColonia() != null && d.getIdColonia() == dom.getIdColonia()) {
                            cbColonia.setValue(d);
                            break;
                        }
                    }
                }
            }

            tfCiudad.setText(dom.getCiudad());
            tfEstado.setText(dom.getEstado());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbSexo.getItems().addAll("M", "F");
        configurarFiltrosEntrada();

        // Listener: when postal code reaches 5 digits, auto-query the API
        tfCodigoPostal.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Only allow digits, max 5
                if (newValue != null && !newValue.matches("\\d*")) {
                    tfCodigoPostal.setText(newValue.replaceAll("[^\\d]", ""));
                    return;
                }
                if (newValue != null && newValue.length() > 5) {
                    tfCodigoPostal.setText(newValue.substring(0, 5));
                    return;
                }

                if (newValue != null && newValue.length() == 5) {
                    buscarDireccionPorCodigoPostal(newValue);
                } else {
                    // Clear fields if less than 5 digits
                    cbColonia.getItems().clear();
                    tfCiudad.setText("");
                    tfEstado.setText("");
                    coloniasActuales = null;
                }
            }
        });

        // Listener: when a colonia is selected, update ciudad and estado
        cbColonia.valueProperty().addListener(new ChangeListener<Direccion>() {
            @Override
            public void changed(ObservableValue<? extends Direccion> observable, Direccion oldValue, Direccion newValue) {
                if (newValue != null) {
                    tfCiudad.setText(newValue.getCiudad() != null ? newValue.getCiudad() : "");
                    tfEstado.setText(newValue.getEstado() != null ? newValue.getEstado() : "");
                }
            }
        });
    }
    private void configurarFiltrosEntrada() {
        Utilidades.permitirSoloLetras(tfNombre);
        Utilidades.permitirSoloLetras(tfPrimerApellido);
        Utilidades.permitirSoloLetras(tfSegundoApellido);
        Utilidades.permitirSoloNumeros(tfTelefono);
        Utilidades.limitarLongitud(tfTelefono, 10);
    }


    /**
     * Calls the API to get colonias for the given postal code and populates the cbColonia ComboBox.
     */
    private void buscarDireccionPorCodigoPostal(String codigoPostal) {
        coloniasActuales = DireccionImp.obtenerDireccionPorCodigoPostal(codigoPostal);

        cbColonia.getItems().clear();
        tfCiudad.setText("");
        tfEstado.setText("");

        if (coloniasActuales != null && !coloniasActuales.isEmpty()) {
            cbColonia.getItems().addAll(coloniasActuales);
            // Auto-select first colonia and fill ciudad/estado
            cbColonia.setValue(coloniasActuales.get(0));
        }
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
// 1. Validaciones
        if (Validaciones.esVacio(tfNombre.getText()) ||
            Validaciones.esVacio(tfPrimerApellido.getText()) ||
            dpFechaNacimiento.getValue() == null ||
            cbSexo.getValue() == null ||
            Validaciones.esVacio(tfTelefono.getText()) ||
            Validaciones.esVacio(tfCorreo.getText()) ||
            Validaciones.esVacio(tfCalle.getText()) ||
            Validaciones.esVacio(tfNumero.getText()) ||
            Validaciones.esVacio(tfCodigoPostal.getText()) ||
            cbColonia.getValue() == null) {

            mostrarError("Por favor completa todos los campos obligatorios (*).");
            return;
        }

        if (!Validaciones.esTelefonoValido(tfTelefono.getText())) {
            mostrarError("El teléfono debe tener exactamente 10 dígitos numéricos.");
            return;
        }

        if (!Validaciones.esEmailValido(tfCorreo.getText())) {
            mostrarError("El formato del correo electrónico es inválido.");
            return;
        }

        // 2. Construir objeto
        Paciente paciente = (pacienteEdicion != null) ? pacienteEdicion : new Paciente();
        paciente.setNombre(tfNombre.getText().trim());
        paciente.setPrimerApellido(tfPrimerApellido.getText().trim());
        paciente.setSegundoApellido(tfSegundoApellido.getText().trim());
        paciente.setFechaNacimiento(dpFechaNacimiento.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        paciente.setSexo(cbSexo.getValue());
        paciente.setTelefono(tfTelefono.getText().trim());
        paciente.setEmail(tfCorreo.getText().trim());
        if (pacienteEdicion == null) {
            paciente.setIdMedico(Sesion.getMedicoSesion().getIdMedico());
        }

        // Build Domicilio with the selected colonia's idColonia
        Domicilio domicilio = (paciente.getDomicilio() != null) ? paciente.getDomicilio() : new Domicilio();
        domicilio.setCalle(tfCalle.getText().trim());
        domicilio.setNumero(tfNumero.getText().trim());
        domicilio.setCodigoPostal(tfCodigoPostal.getText().trim());

        Direccion coloniaSeleccionada = cbColonia.getValue();
        domicilio.setIdColonia(coloniaSeleccionada.getIdColonia());
        domicilio.setColonia(coloniaSeleccionada.getColonia());
        domicilio.setCiudad(coloniaSeleccionada.getCiudad() != null ? coloniaSeleccionada.getCiudad() : "");
        domicilio.setEstado(coloniaSeleccionada.getEstado() != null ? coloniaSeleccionada.getEstado() : "");

        paciente.setDomicilio(domicilio);

        // 3. Registrar o editar Direccion primero
        Direccion dirEnvio = new Direccion();
        dirEnvio.setCalle(tfCalle.getText().trim());
        dirEnvio.setNumero(tfNumero.getText().trim());
        dirEnvio.setIdColonia(coloniaSeleccionada.getIdColonia());
        
        if (pacienteEdicion != null && pacienteEdicion.getDomicilio() != null && pacienteEdicion.getDomicilio().getIdDomicilio() > 0) {
            dirEnvio.setIdDireccion(pacienteEdicion.getDomicilio().getIdDomicilio());
            dto.Respuesta respDir = DireccionImp.editar(dirEnvio);
            if (respDir.isError()) {
                mostrarError("Error al actualizar la dirección: " + respDir.getMensaje());
                return;
            }
            paciente.setIdDomicilio(pacienteEdicion.getDomicilio().getIdDomicilio());
        } else {
            dto.Respuesta respDir = DireccionImp.registrarDireccion(dirEnvio);
            if (respDir.isError()) {
                mostrarError("Error al registrar la dirección: " + respDir.getMensaje());
                return;
            }
            try {
                int idDireccionGen = -1;
                if (respDir.getValor() != null && !String.valueOf(respDir.getValor()).equals("null")) {
                    String idStr = String.valueOf(respDir.getValor());
                    // Manejar formato extraÃƒÂ±o del JSON {type=string, value=41}
                    if (idStr.contains("value=")) {
                        idStr = idStr.replaceAll(".*value=([^}]+)}.*", "$1").trim();
                    }
                    if (idStr.endsWith(".0")) {
                         idStr = idStr.substring(0, idStr.length() - 2);
                    }
                    idDireccionGen = Integer.parseInt(idStr);
                } else if (respDir.getIdDireccion() != null) {
                    idDireccionGen = respDir.getIdDireccion();
                } else if (respDir.getIdDomicilio() != null) {
                    idDireccionGen = respDir.getIdDomicilio();
                } else {
                    throw new Exception("El servidor no devolvió ningún ID de dirección.");
                }

                paciente.setIdDomicilio(idDireccionGen);
                
                if (paciente.getDomicilio() != null) {
                    paciente.getDomicilio().setIdDomicilio(idDireccionGen);
                }
            } catch (Exception e) {
                mostrarError("Error ID: " + e.getMessage() + " | Valor: " + respDir.getValor());
                e.printStackTrace();
                return;
            }
        }

        // 4. Consumir API para Paciente
        RSRegistroPaciente respuesta;
        if (pacienteEdicion != null) {
            respuesta = PacienteImp.editarPaciente(paciente);
        } else {
            respuesta = PacienteImp.registrarPaciente(paciente);
        }

        if (!respuesta.isError()) {
            String nipGenerado = (respuesta.getNip() != null) ? respuesta.getNip() : String.valueOf(respuesta.getValor());
            if (nipGenerado != null && nipGenerado.contains("value=")) {
                nipGenerado = nipGenerado.replaceAll(".*value=([^}]+)}.*", "$1").trim();
            }
            if (nipGenerado != null && nipGenerado.endsWith(".0")) {
                nipGenerado = nipGenerado.substring(0, nipGenerado.length() - 2);
            }
            
            String mensajeExito = (pacienteEdicion != null)
                ? "Los datos del paciente se han actualizado correctamente."
                : "El paciente se registró correctamente.\n\nNIP Generado (entregar al paciente): " + nipGenerado;

            Utilidades.mostrarAlertaSimple(
                (pacienteEdicion != null) ? "Paciente Actualizado" : "Paciente Registrado",
                mensajeExito,
                Alert.AlertType.INFORMATION
            );
            if (observador != null) {
                observador.notificarOperacionGuardar();
            }
            cerrarVentana();
        } else {
            mostrarError(respuesta.getMensaje());
        }
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void mostrarError(String mensaje) {
        Utilidades.mostrarAlertaSimple("Validación", mensaje, Alert.AlertType.WARNING);
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
