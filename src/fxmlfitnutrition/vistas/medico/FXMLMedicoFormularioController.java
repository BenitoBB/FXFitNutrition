package fxmlfitnutrition.vistas.medico;

import dominio.DireccionImp;
import dominio.MedicoImp;
import dto.Respuesta;
import java.io.File;
import java.nio.file.Files;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pojo.Direccion;
import pojo.Domicilio;
import pojo.Medico;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLMedicoFormularioController implements Initializable {

    @FXML private Label lbTitulo;
    @FXML private Label lbSubtitulo;
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
    @FXML private Label lbFotografia;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ToggleGroup tgSexo;
    private NotificacionOperacion observador;
    private Medico medicoEdicion;
    private List<Direccion> coloniasActuales;
    private byte[] fotografiaSeleccionada;

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarParaEdicion(Medico medico) {
        this.medicoEdicion = medico;
        lbTitulo.setText("Editar medico");
        lbSubtitulo.setText("Modifique los datos del usuario medico seleccionado");
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

        tfCedula.setText(medico.getCedulaProfesional() != null ? medico.getCedulaProfesional() : "");
        tfNoPersonal.setText(medico.getNoPersonal() != null ? medico.getNoPersonal() : "");
        chkAdmin.setSelected(medico.getEsAdministrador() == 1);
        actualizarEstadoCedulaAdministrador();

        tfContrasena.setPromptText("Dejar vacio para no cambiar");
        tfRepetirContrasena.setPromptText("Dejar vacio para no cambiar");
        lbFotografia.setText(medico.getFotografia() != null && !medico.getFotografia().isEmpty() ? "Fotografia actual registrada" : "Sin fotografia seleccionada");

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
        configurarFiltrosEntrada();
        configurarAdministrador();

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

    private void configurarFiltrosEntrada() {
        Utilidades.permitirSoloLetras(tfNombre);
        Utilidades.permitirSoloLetras(tfPrimerApellido);
        Utilidades.permitirSoloLetras(tfSegundoApellido);
        Utilidades.permitirLetrasNumerosSinEspeciales(tfNumero);
        Utilidades.limitarLongitud(tfCedula, 8);
        Utilidades.permitirLetrasNumerosSinEspeciales(tfCedula);
        Utilidades.permitirLetrasNumerosSinEspeciales(tfNoPersonal);
    }

    private void configurarAdministrador() {
        chkAdmin.selectedProperty().addListener((observable, oldValue, newValue) -> actualizarEstadoCedulaAdministrador());
        actualizarEstadoCedulaAdministrador();
    }

    private void actualizarEstadoCedulaAdministrador() {
        boolean esAdmin = chkAdmin != null && chkAdmin.isSelected();
        tfCedula.setDisable(esAdmin);
        tfCedula.setPromptText(esAdmin ? "No aplica para administrador" : "Exactamente 8 caracteres");
        if (esAdmin) {
            tfCedula.clear();
        }
    }

    private void buscarDireccionPorCodigoPostal(String codigoPostal) {
        coloniasActuales = DireccionImp.obtenerDireccionPorCodigoPostal(codigoPostal);
        cbColonia.getItems().clear();
        tfCiudad.setText("");
        tfEstado.setText("");

        if (coloniasActuales != null && !coloniasActuales.isEmpty()) {
            cbColonia.getItems().addAll(coloniasActuales);
            cbColonia.setValue(coloniasActuales.get(0));
        } else {
            Utilidades.mostrarAlertaSimple("Sin resultados", "No se encontraron colonias para el codigo postal ingresado.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void clicSeleccionarFotografia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar fotografia del medico");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg")
        );
        File archivo = fileChooser.showOpenDialog(btnGuardar.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            fotografiaSeleccionada = Files.readAllBytes(archivo.toPath());
            lbFotografia.setText(archivo.getName());
        } catch (Exception e) {
            fotografiaSeleccionada = null;
            lbFotografia.setText("Sin fotografia seleccionada");
            Utilidades.mostrarAlertaSimple("Fotografia", "No fue posible leer la imagen seleccionada.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        if (!validarCampos()) {
            return;
        }

        Medico medico = (medicoEdicion != null) ? medicoEdicion : new Medico();
        medico.setNombre(tfNombre.getText().trim());
        medico.setPrimerApellido(tfPrimerApellido.getText().trim());
        medico.setSegundoApellido(tfSegundoApellido.getText().trim());
        medico.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
        medico.setSexo(rbMasculino.isSelected() ? "M" : "F");
        medico.setCedulaProfesional(chkAdmin.isSelected() ? "NOAPLICA" : tfCedula.getText().trim());
        medico.setNoPersonal(tfNoPersonal.getText().trim());
        medico.setEsAdministrador(chkAdmin.isSelected() ? 1 : 0);

        String pass = tfContrasena.getText().trim();
        if (!pass.isEmpty()) {
            medico.setContrasena(pass);
        } else if (medicoEdicion == null) {
            mostrarError("La contrasena es obligatoria para nuevos registros.");
            return;
        }

        Direccion coloniaSeleccionada = cbColonia.getValue();
        if (coloniaSeleccionada == null) {
            mostrarError("Seleccione una colonia valida.");
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
                mostrarError("Error al actualizar la direccion: " + respDir.getMensaje());
                return;
            }
            medico.setIdDomicilio(medicoEdicion.getDomicilio().getIdDomicilio());
        } else {
            Respuesta respDir = DireccionImp.registrarDireccion(dirEnvio);
            if (respDir.isError()) {
                mostrarError("Error al registrar la direccion: " + respDir.getMensaje());
                return;
            }
            Integer idDireccion = obtenerEnteroRespuesta(respDir.getValor());
            if (idDireccion == null) {
                idDireccion = respDir.getIdDireccion() != null ? respDir.getIdDireccion() : respDir.getIdDomicilio();
            }
            if (idDireccion == null || idDireccion <= 0) {
                mostrarError("Error al recuperar el ID de la direccion registrada.");
                return;
            }
            medico.setIdDomicilio(idDireccion);
        }

        Respuesta respuesta = medicoEdicion != null ? MedicoImp.editarMedico(medico) : MedicoImp.registrarMedico(medico);

        if (!respuesta.isError()) {
            int idMedico = medicoEdicion != null ? medicoEdicion.getIdMedico() : obtenerIdMedicoRegistrado(respuesta);
            if (fotografiaSeleccionada != null && idMedico > 0) {
                Respuesta respuestaFoto = MedicoImp.subirFotografia(idMedico, fotografiaSeleccionada);
                if (respuestaFoto.isError()) {
                    Utilidades.mostrarAlertaSimple("Fotografia", respuestaFoto.getMensaje(), Alert.AlertType.WARNING);
                }
            }

            Utilidades.mostrarAlertaSimple("Exito", respuesta.getMensaje(), Alert.AlertType.INFORMATION);
            if (observador != null) {
                observador.notificarOperacionGuardar();
            }
            cerrarVentana();
        } else {
            mostrarError(respuesta.getMensaje());
        }
    }

    private boolean validarCampos() {
        if (tfNombre.getText().trim().isEmpty()
                || tfPrimerApellido.getText().trim().isEmpty()
                || dpFechaNacimiento.getValue() == null
                || (!rbMasculino.isSelected() && !rbFemenino.isSelected())
                || tfNoPersonal.getText().trim().isEmpty()
                || tfCalle.getText().trim().isEmpty()
                || tfNumero.getText().trim().isEmpty()
                || tfCodigoPostal.getText().trim().isEmpty()) {
            mostrarError("Por favor, llena todos los campos obligatorios.");
            return false;
        }

        if (!chkAdmin.isSelected() && tfCedula.getText().trim().isEmpty()) {
            mostrarError("La cedula profesional es obligatoria para usuarios medicos.");
            return false;
        }

        if (!chkAdmin.isSelected() && tfCedula.getText().trim().length() != 8) {
            mostrarError("La cedula profesional debe tener exactamente 8 caracteres.");
            return false;
        }

        String pass = tfContrasena.getText();
        String repass = tfRepetirContrasena.getText();
        if (!pass.equals(repass)) {
            mostrarError("La contrasena y su confirmacion deben coincidir.");
            return false;
        }
        return true;
    }

    private int obtenerIdMedicoRegistrado(Respuesta respuesta) {
        Integer id = obtenerEnteroRespuesta(respuesta.getValor());
        return id != null ? id : 0;
    }

    private Integer obtenerEnteroRespuesta(Object valor) {
        if (valor == null) {
            return null;
        }
        try {
            String idStr = String.valueOf(valor);
            if (idStr.contains("value=")) {
                idStr = idStr.replaceAll(".*value=([^}]+)}.*", "$1").trim();
            }
            if (idStr.endsWith(".0")) {
                idStr = idStr.substring(0, idStr.length() - 2);
            }
            return Integer.parseInt(idStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void mostrarError(String mensaje) {
        Utilidades.mostrarAlertaSimple("Validacion", mensaje, Alert.AlertType.WARNING);
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
