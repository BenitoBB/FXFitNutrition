package fxmlfitnutrition.vistas.consulta;

import dominio.ConsultaImp;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import dto.RespuestaSimple;
import java.util.Optional;
import pojo.Consulta;
import pojo.Paciente;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLConsultasPacienteController implements Initializable, NotificacionOperacion {

    @FXML private Label lbNombrePaciente;
    @FXML private TableView<Consulta> tvConsultas;
    @FXML private TableColumn<Consulta, String> colFecha;
    @FXML private TableColumn<Consulta, String> colHora;
    @FXML private TableColumn<Consulta, Double> colPeso;
    @FXML private TableColumn<Consulta, Double> colTalla;
    @FXML private TableColumn<Consulta, String> colIMC;
    @FXML private TableColumn<Consulta, String> colDieta;
    @FXML private TableColumn<Consulta, String> colEstatus;
    @FXML private TableColumn<Consulta, String> colObservaciones;
    @FXML private Button btnNuevaConsulta;
    @FXML private Button btnModificar;
    @FXML private Button btnCancelarConsulta;

    private Paciente paciente;
    private ObservableList<Consulta> consultas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        btnModificar.setDisable(true);
        btnCancelarConsulta.setDisable(true);
    }

    public void inicializarPaciente(Paciente paciente) {
        this.paciente = paciente;
        lbNombrePaciente.setText("Historial de " + paciente.getNombreCompleto());
        cargarConsultas();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaConsultaCorta"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaConsulta"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colTalla.setCellValueFactory(new PropertyValueFactory<>("talla"));
        colIMC.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getImc() != null ? String.format("%.2f", cellData.getValue().getImc()) : ""
        ));
        colDieta.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getNombreDieta() != null ? cellData.getValue().getNombreDieta() : "Sin dieta"
        ));
        colEstatus.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isActiva() ? "Activa" : "Cancelada"
        ));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        consultas = FXCollections.observableArrayList();
        tvConsultas.setItems(consultas);
        tvConsultas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean sinSeleccion = newValue == null;
            boolean cancelada = newValue != null && !newValue.isActiva();
            btnModificar.setDisable(sinSeleccion || cancelada);
            btnCancelarConsulta.setDisable(sinSeleccion || cancelada);
        });
    }

    private void cargarConsultas() {
        consultas.clear();
        if (paciente == null) {
            return;
        }
        consultas.addAll(ConsultaImp.buscarConsultasPaciente(paciente.getIdPaciente()));
    }

    @FXML
    private void clicNuevaConsulta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioConsulta.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioConsultaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            controlador.inicializarPaciente(paciente);

            Stage stage = new Stage();
            stage.setTitle("Registrar Consulta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar el formulario de consulta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicModificar(ActionEvent event) {
        Consulta consultaSeleccionada = tvConsultas.getSelectionModel().getSelectedItem();
        if (consultaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Selecciona una consulta.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioConsulta.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioConsultaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            controlador.inicializarPaciente(paciente);
            controlador.inicializarParaEdicion(consultaSeleccionada);

            Stage stage = new Stage();
            stage.setTitle("Modificar Consulta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar el formulario de consulta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicCancelarConsulta(ActionEvent event) {
        Consulta consultaSeleccionada = tvConsultas.getSelectionModel().getSelectedItem();
        if (consultaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Selecciona una consulta.", Alert.AlertType.WARNING);
            return;
        }

        if (!consultaSeleccionada.isActiva()) {
            Utilidades.mostrarAlertaSimple("Atencion", "La consulta seleccionada ya esta cancelada.", Alert.AlertType.WARNING);
            return;
        }

        if (!confirmarCancelacion()) {
            return;
        }

        RespuestaSimple respuesta = ConsultaImp.cancelarConsulta(consultaSeleccionada.getIdConsulta());
        if (!respuesta.isError()) {
            consultaSeleccionada.setCancelada(1);
            tvConsultas.refresh();
            btnModificar.setDisable(true);
            btnCancelarConsulta.setDisable(true);
            Utilidades.mostrarAlertaSimple("Consulta Cancelada", "La consulta se cancelo correctamente.", Alert.AlertType.INFORMATION);
        } else {
            Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
        }
    }

    private boolean confirmarCancelacion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cancelar Consulta");
        alerta.setHeaderText("Confirmar cancelacion");
        alerta.setContentText("La consulta se marcara como cancelada y no debera considerarse en el progreso real del paciente.");

        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    @Override
    public void notificarOperacionGuardar() {
        cargarConsultas();
    }
}
