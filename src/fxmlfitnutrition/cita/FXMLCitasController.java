package fxmlfitnutrition.vistas.cita;

import dominio.CitaImp;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.Cita;
import pojo.CitaDetalle;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;

public class FXMLCitasController implements Initializable, NotificacionOperacion {

    @FXML private TextField tfBuscar;
    @FXML private ComboBox<String> cbFiltroEstatus;
    @FXML private TableView<CitaDetalle> tvCitas;
    @FXML private TableColumn<CitaDetalle, String> colFecha;
    @FXML private TableColumn<CitaDetalle, String> colHora;
    @FXML private TableColumn<CitaDetalle, String> colPaciente;
    @FXML private TableColumn<CitaDetalle, String> colMedico;
    @FXML private TableColumn<CitaDetalle, String> colEstatus;
    @FXML private Button btnAgendar;
    @FXML private Button btnModificar;
    @FXML private Button btnCancelar;
    @FXML private Button btnReagendar;

    private ObservableList<CitaDetalle> listaObservableCitas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarFiltroEstatus();
        Utilidades.permitirBusquedaNombreCorreo(tfBuscar);
        cargarDatosTabla();

        tfBuscar.textProperty().addListener((observable, oldValue, newValue) -> cargarDatosTabla());
        cbFiltroEstatus.valueProperty().addListener((observable, oldValue, newValue) -> cargarDatosTabla());
        tvCitas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> actualizarEstadoAcciones()
        );
        actualizarEstadoAcciones();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCita"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaCita"));
        colPaciente.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombrePacienteCompleto())
        );
        colMedico.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreMedicoCompleto())
        );
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));

        listaObservableCitas = FXCollections.observableArrayList();
        tvCitas.setItems(listaObservableCitas);
    }

    private void configurarFiltroEstatus() {
        cbFiltroEstatus.getItems().addAll("Todos", "Confirmada", "Reagendada", "Cancelada", "Asistida", "Ausente");
        cbFiltroEstatus.getSelectionModel().selectFirst();
    }

    private void cargarDatosTabla() {
        String criterio = tfBuscar.getText();
        String estatus = cbFiltroEstatus.getValue();

        List<CitaDetalle> citas = CitaImp.buscarCitas(
                criterio,
                Sesion.getIdMedicoParaFiltro(),
                Sesion.isEsAdministrador(),
                estatus
        );
        listaObservableCitas.clear();
        if (citas != null) {
            listaObservableCitas.addAll(citas);
        } else {
            Utilidades.mostrarAlertaSimple(
                    "Error",
                    "No fue posible consultar las citas.",
                    Alert.AlertType.ERROR
            );
        }
        actualizarEstadoAcciones();
    }

    @FXML
    private void clicAgendarCita(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioCita.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioCitaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);

            Stage stage = new Stage();
            stage.setTitle("Agendar Nueva Cita");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar la ventana de agenda.", Alert.AlertType.ERROR);
        }
    }
}
