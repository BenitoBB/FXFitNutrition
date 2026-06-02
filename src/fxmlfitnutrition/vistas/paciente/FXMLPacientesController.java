package fxmlfitnutrition.vistas.paciente;

import fxmlfitnutrition.vistas.paciente.FXMLFormularioPacienteController;
import dominio.PacienteImp;
import dto.RSPacientes;
import dto.RespuestaSimple;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.Paciente;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;

public class FXMLPacientesController implements Initializable, NotificacionOperacion {

    @FXML private TextField tfBuscar;
    @FXML private Button btnRegistrar;
    @FXML private TableView<Paciente> tvPacientes;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, String> colCorreo;
    @FXML private TableColumn<Paciente, String> colFechaNacimiento;
    @FXML private TableColumn<Paciente, String> colDireccion;
    @FXML private TableColumn<Paciente, String> colEstatus;
    @FXML private TableColumn<Paciente, Void> colHistorial;
    @FXML private Button btnEditar;
    @FXML private Button btnDarDeBaja;

    private ObservableList<Paciente> listaObservablePacientes;

    private void abrirHistorialPaciente(Paciente paciente) {
        if (paciente == null) {
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxmlfitnutrition/vistas/consulta/FXMLConsultasPaciente.fxml"));
            Parent root = fxmlLoader.load();

            fxmlfitnutrition.vistas.consulta.FXMLConsultasPacienteController controlador = fxmlLoader.getController();
            controlador.inicializarPaciente(paciente);

            Stage stage = new Stage();
            stage.setTitle("Historial de Consultas");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar el historial de consultas.", Alert.AlertType.ERROR);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarDatosTabla("");

        // Listener para la barra de búsqueda
        tfBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 2 || newValue.isEmpty()) {
                cargarDatosTabla(newValue);
            }
        });
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNombreCompleto())
        );
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFechaNacimiento.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccionCompleta"));
        colEstatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEstatus() == 1 ? "Activo" : "Inactivo")
        );
        configurarColumnaHistorial();
        
        listaObservablePacientes = FXCollections.observableArrayList();
        tvPacientes.setItems(listaObservablePacientes);
    }

    private void configurarColumnaHistorial() {
        colHistorial.setCellFactory(columna -> new TableCell<Paciente, Void>() {
            private final Button btnHistorial = new Button("Ver");

            {
                btnHistorial.setOnAction(event -> {
                    Paciente paciente = getTableView().getItems().get(getIndex());
                    abrirHistorialPaciente(paciente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnHistorial);
            }
        });
    }

    private void cargarDatosTabla(String criterio) {
        RSPacientes respuesta = PacienteImp.buscarPacientes(
                criterio,
                Sesion.getIdMedicoParaFiltro(),
                Sesion.isEsAdministrador()
        );

        if (!respuesta.isError()) {
            listaObservablePacientes.clear();
            if (respuesta.getPacientes() != null) {
                listaObservablePacientes.addAll(respuesta.getPacientes());
            }
        } else {
            Utilidades.mostrarAlertaSimple(
                "Error", 
                "Hubo un error al buscar pacientes: " + respuesta.getMensaje(), 
                Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void clicRegistrar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioPaciente.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioPacienteController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);

            Stage stage = new Stage();
            stage.setTitle("Registrar Nuevo Paciente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar la ventana de registro.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicEditar(ActionEvent event) {
        Paciente pacienteSeleccionado = tvPacientes.getSelectionModel().getSelectedItem();
        if (pacienteSeleccionado != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioPaciente.fxml"));
                Parent root = fxmlLoader.load();

                FXMLFormularioPacienteController controlador = fxmlLoader.getController();
                controlador.inicializarValores(this);
                controlador.inicializarParaEdicion(pacienteSeleccionado);

                Stage stage = new Stage();
                stage.setTitle("Editar Paciente");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar la ventana de edición.", Alert.AlertType.ERROR);
            }
        } else {
            Utilidades.mostrarAlertaSimple("Atención", "Por favor selecciona un paciente de la tabla.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void clicDarDeBaja(ActionEvent event) {
        Paciente pacienteSeleccionado = tvPacientes.getSelectionModel().getSelectedItem();
        if (pacienteSeleccionado != null) {
            boolean confirmar = Utilidades.mostrarAlertaConfirmacion(
                "Confirmar Baja", 
                "¿Estás seguro de dar de baja al paciente " + pacienteSeleccionado.getNombre() + "?"
            );
            
            if (confirmar) {
                RespuestaSimple respuesta = PacienteImp.darDeBajaPaciente(pacienteSeleccionado.getIdPaciente());
                if (!respuesta.isError()) {
                    Utilidades.mostrarAlertaSimple("Éxito", respuesta.getMensaje(), Alert.AlertType.INFORMATION);
                    cargarDatosTabla("");
                } else {
                    Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
                }
            }
        } else {
            Utilidades.mostrarAlertaSimple("Atención", "Por favor selecciona un paciente de la tabla.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void clicVerHistorial(ActionEvent event) {
        Paciente pacienteSeleccionado = tvPacientes.getSelectionModel().getSelectedItem();
        if (pacienteSeleccionado != null) {
            abrirHistorialPaciente(pacienteSeleccionado);
        } else {
            Utilidades.mostrarAlertaSimple("Atención", "Por favor selecciona un paciente de la tabla.", Alert.AlertType.WARNING);
        }
    }

    @Override
    public void notificarOperacionGuardar() {
        cargarDatosTabla("");
    }
}
