package fxmlfitnutrition.vistas.medico;

import dominio.DireccionImp;
import dominio.MedicoImp;
import dto.Respuesta;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import pojo.Direccion;
import pojo.Domicilio;
import pojo.Medico;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;
import utilidad.Validaciones;

public class FXMLMedicoFormularioController implements Initializable {

    @FXML private TextField tfNombre;
    @FXML private TextField tfPrimerApellido;
    @FXML private TextField tfSegundoApellido;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private RadioButton rbMasculino;
    @FXML private RadioButton rbFemenino;
    
    @FXML private TextField tfCodigoPostal;
    @FXML private TextField tfEstado;
    @FXML private TextField tfCiudad;
    @FXML private ComboBox<Direccion> cbColonia;
    @FXML private TextField tfCalle;
    @FXML private TextField tfNumero;
    
    @FXML private TextField tfCedula;
    @FXML private TextField tfNoPersonal;
    @FXML private PasswordField tfContrasena;
    @FXML private PasswordField tfRepetirContrasena;
    @FXML private CheckBox chkAdmin;
    
    @FXML private Label lbMensajeError;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ToggleGroup tgSexo;
    private NotificacionOperacion observador;
    private Medico medicoEdicion;
    private List<Direccion> coloniasActuales;

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarParaEdicion(Medico medico) {
        this.medicoEdicion = medico;
        tfNombre.setText(medico.getNombre());
        tfPrimerApellido.setText(medico.getPrimerApellido());
        tfSegundoApellido.setText(medico.getSegundoApellido() != null ? medico.getSegundoApellido() : "");
        
        if (medico.getFechaNacimiento() != null && !medico.getFechaNacimiento().isEmpty()) {
            dpFechaNacimiento.setValue(java.time.LocalDate.parse(medico.getFechaNacimiento()));
        }
        
        if ("M".equals(medico.getSexo())) {
            rbMasculino.setSelected(true);
        } else if ("F".equals(medico.getSexo())) {
            rbFemenino.setSelected(true);
        }
        
        tfCedula.setText(medico.getCedulaProfesional());
        tfNoPersonal.setText(medico.getNoPersonal() != null ? medico.getNoPersonal() : "");
        chkAdmin.setSelected(medico.getEsAdministrador() == 1);
        
        // No se pre-llena contraseña
        tfContrasena.setPromptText("Dejar vacío para no cambiar");
        tfRepetirContrasena.setPromptText("Dejar vacío para no cambiar");

        if (medico.getDomicilio() != null) {
            Domicilio dom = medico.getDomicilio();
            tfCalle.setText(dom.getCalle());
            tfNumero.setText(dom.getNumero());

            if (dom.getCodigoPostal() != null && !dom.getCodigoPostal().isEmpty()) {
                tfCodigoPostal.setText(dom.getCodigoPostal());
                buscarDireccionPorCodigoPostal(dom.getCodigoPostal());
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
        tgSexo = new ToggleGroup();
        rbMasculino.setToggleGroup(tgSexo);
        rbFemenino.setToggleGroup(tgSexo);
        
        tfCodigoPostal.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
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
                    cbColonia.getItems().clear();
                    tfCiudad.setText("");
                    tfEstado.setText("");
                    coloniasActuales = null;
                }
            }
        });

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

    private void buscarDireccionPorCodigoPostal(String codigoPostal) {
        coloniasActuales = DireccionImp.obtenerDireccionPorCodigoPostal(codigoPostal);
        cbColonia.getItems().clear();
        tfCiudad.setText("");
        tfEstado.setText("");

        if (coloniasActuales != null && !coloniasActuales.isEmpty()) {
            cbColonia.getItems().addAll(coloniasActuales);
            // Auto-seleccionar la primera colonia para llenar ciudad/estado automáticamente
            cbColonia.setValue(coloniasActuales.get(0));
        } else {
            Utilidades.mostrarAlertaSimple("Sin resultados", "No se encontraron colonias para el código postal ingresado.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        lbMensajeError.setVisible(false);
        if (!validarCampos()) {
            mostrarError("Por favor, llene todos los campos obligatorios.");
            return;
        }

        Medico medico = (medicoEdicion != null) ? medicoEdicion : new Medico();
        medico.setNombre(tfNombre.getText().trim());
        medico.setPrimerApellido(tfPrimerApellido.getText().trim());
        medico.setSegundoApellido(tfSegundoApellido.getText().trim());
        medico.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        medico.setSexo(rbMasculino.isSelected() ? "M" : "F");
        medico.setCedulaProfesional(tfCedula.getText().trim());
        medico.setNoPersonal(tfNoPersonal.getText().trim());
        medico.setEsAdministrador(chkAdmin.isSelected() ? 1 : 0);
        
        String pass = tfContrasena.getText().trim();
        if (!pass.isEmpty()) {
            medico.setContrasena(pass);
        } else if (medicoEdicion == null) {
            mostrarError("La contraseña es obligatoria para nuevos registros.");
            return;
        }

        Direccion coloniaSeleccionada = cbColonia.getValue();
        if (coloniaSeleccionada == null) {
            mostrarError("Seleccione una colonia válida.");
            return;
        }

        Direccion dirEnvio = new Direccion();
        dirEnvio.setCalle(tfCalle.getText().trim());
        dirEnvio.setNumero(tfNumero.getText().trim());
        dirEnvio.setIdColonia(coloniaSeleccionada.getIdColonia());
        
        if (medicoEdicion != null && medicoEdicion.getDomicilio() != null && medicoEdicion.getDomicilio().getIdDomicilio() > 0) {
            dirEnvio.setIdDireccion(medicoEdicion.getDomicilio().getIdDomicilio());
            Respuesta respDir = DireccionImp.editar(dirEnvio);
            if (respDir.isError()) {
                mostrarError("Error al actualizar la dirección: " + respDir.getMensaje());
                return;
            }
            medico.setIdDomicilio(medicoEdicion.getDomicilio().getIdDomicilio());
        } else {
            Respuesta respDir = DireccionImp.registrarDireccion(dirEnvio);
            if (respDir.isError()) {
                mostrarError("Error al registrar la dirección: " + respDir.getMensaje());
                return;
            }
            try {
                int idDireccionGen = -1;
                if (respDir.getValor() != null && !String.valueOf(respDir.getValor()).equals("null")) {
                    String idStr = String.valueOf(respDir.getValor());
                    // Manejar formato extraño del JSON {type=string, value=41}
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

                medico.setIdDomicilio(idDireccionGen);
                if (medico.getDomicilio() != null) {
                    medico.getDomicilio().setIdDomicilio(idDireccionGen);
                }
            } catch (Exception e) {
                mostrarError("Error al recuperar el ID de la dirección registrada.");
                return;
            }
        }

        Respuesta respuesta;
        if (medicoEdicion != null) {
            respuesta = MedicoImp.editarMedico(medico);
        } else {
            respuesta = MedicoImp.registrarMedico(medico);
        }

        if (!respuesta.isError()) {
            Utilidades.mostrarAlertaSimple("Éxito", respuesta.getMensaje(), Alert.AlertType.INFORMATION);
            if (observador != null) observador.notificarOperacionGuardar();
            cerrarVentana();
        } else {
            mostrarError(respuesta.getMensaje());
        }
    }

    private boolean validarCampos() {
        if (tfNombre.getText().trim().isEmpty() || tfPrimerApellido.getText().trim().isEmpty() ||
            dpFechaNacimiento.getValue() == null || (!rbMasculino.isSelected() && !rbFemenino.isSelected()) ||
            tfCedula.getText().trim().isEmpty() || tfNoPersonal.getText().trim().isEmpty() || 
            tfCalle.getText().trim().isEmpty() || tfNumero.getText().trim().isEmpty() || 
            tfCodigoPostal.getText().trim().isEmpty()) {
            return false;
        }
        String pass = tfContrasena.getText();
        String repass = tfRepetirContrasena.getText();
        if (!pass.equals(repass)) {
            mostrarError("Las contraseñas no coinciden.");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        lbMensajeError.setText(mensaje);
        lbMensajeError.setVisible(true);
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
