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

    @FXML
    private void clicModificar(ActionEvent event) {
        CitaDetalle citaSeleccionada = tvCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Por favor selecciona una cita de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (!esCitaModificable(citaSeleccionada)) {
            Utilidades.mostrarAlertaSimple(
                    "Atencion",
                    "Solo se pueden modificar citas con estatus Confirmada o Reagendada.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        abrirFormularioEdicion(citaSeleccionada);
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        CitaDetalle citaSeleccionada = tvCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Por favor selecciona una cita de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (!esCitaModificable(citaSeleccionada)) {
            Utilidades.mostrarAlertaSimple(
                    "Atencion",
                    "Solo se pueden cancelar citas con estatus Confirmada o Reagendada.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        if (!confirmarCancelacion(citaSeleccionada)) {
            return;
        }

        Optional<String> motivo = solicitarMotivoCancelacion();
        if (!motivo.isPresent()) {
            return;
        }

        cancelarCita(citaSeleccionada, motivo.get());
    }

    @FXML
    private void clicReagendar(ActionEvent event) {
        CitaDetalle citaSeleccionada = tvCitas.getSelectionModel().getSelectedItem();
        if (citaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Por favor selecciona una cita de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        if (!esCitaReagendable(citaSeleccionada)) {
            Utilidades.mostrarAlertaSimple(
                    "Atencion",
                    "Solo se pueden reagendar citas con estatus Cancelada.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        abrirModalReagendar(citaSeleccionada);
    }

    private boolean esCitaModificable(CitaDetalle cita) {
        return cita != null
                && ("Confirmada".equals(cita.getEstatus()) || "Reagendada".equals(cita.getEstatus()));
    }

    private boolean esCitaReagendable(CitaDetalle cita) {
        return cita != null && "Cancelada".equals(cita.getEstatus());
    }

    private void actualizarEstadoAcciones() {
        CitaDetalle citaSeleccionada = tvCitas.getSelectionModel().getSelectedItem();
        boolean accionPermitida = esCitaModificable(citaSeleccionada);
        btnModificar.setDisable(!accionPermitida);
        btnCancelar.setDisable(!accionPermitida);
        btnReagendar.setDisable(!esCitaReagendable(citaSeleccionada));
    }

    private boolean confirmarCancelacion(CitaDetalle cita) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cancelar Cita");
        alerta.setHeaderText("Confirmar cancelacion");
        alerta.setContentText("Se cancelara la cita de " + cita.getNombrePacienteCompleto()
                + " del " + cita.getFechaCita() + " a las " + formatearHoraVista(cita.getHoraCita()) + ".");

        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    private Optional<String> solicitarMotivoCancelacion() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Motivo de Cancelacion");
        dialog.setHeaderText("Motivo opcional");
        dialog.setContentText("Motivo:");
        return dialog.showAndWait();
    }

    private void cancelarCita(CitaDetalle citaSeleccionada, String motivoCancelacion) {
        Cita cita = new Cita();
        cita.setIdCita(citaSeleccionada.getIdCita());
        cita.setMotivoCancelacion(motivoCancelacion != null ? motivoCancelacion.trim() : "");

        dto.RespuestaSimple respuesta = CitaImp.cancelarCita(cita);
        if (!respuesta.isError()) {
            citaSeleccionada.setEstatus("Cancelada");
            citaSeleccionada.setMotivoCancelacion(cita.getMotivoCancelacion());
            tvCitas.refresh();
            actualizarEstadoAcciones();
            Utilidades.mostrarAlertaSimple("Cita Cancelada", "La cita se ha cancelado correctamente.", Alert.AlertType.INFORMATION);
        } else {
            Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
        }
    }

    private String formatearHoraVista(String hora) {
        if (hora == null) {
            return "";
        }
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    private void abrirModalReagendar(CitaDetalle citaSeleccionada) {
        Stage stage = new Stage();
        stage.setTitle("Reagendar Cita");
        stage.initModality(Modality.APPLICATION_MODAL);

        DatePicker dpNuevaFecha = new DatePicker();
        ComboBox<String> cbNuevaHora = new ComboBox<>();
        cbNuevaHora.getItems().addAll(generarHorasDisponibles());

        GridPane formulario = new GridPane();
        formulario.setHgap(15);
        formulario.setVgap(15);
        formulario.setStyle("-fx-padding: 20;");
        formulario.add(new Label("Nueva fecha *"), 0, 0);
        formulario.add(dpNuevaFecha, 1, 0);
        formulario.add(new Label("Nueva hora *"), 0, 1);
        formulario.add(cbNuevaHora, 1, 1);

        Button btnGuardar = new Button("Guardar");
        btnGuardar.getStyleClass().add("button-primary");
        Button btnCerrar = new Button("Cancelar");

        HBox acciones = new HBox(12, btnCerrar, btnGuardar);
        acciones.setStyle("-fx-alignment: center-right; -fx-padding: 0 20 20 20;");

        javafx.scene.layout.VBox contenedor = new javafx.scene.layout.VBox(10, formulario, acciones);
        URL hojaEstilos = getClass().getResource("/fxmlfitnutrition/css/light-modern.css");
        if (hojaEstilos != null) {
            contenedor.getStylesheets().add(hojaEstilos.toExternalForm());
        }

        btnCerrar.setOnAction(event -> stage.close());
        btnGuardar.setOnAction(event -> guardarReagenda(citaSeleccionada, dpNuevaFecha, cbNuevaHora, stage));

        stage.setScene(new Scene(contenedor, 430, 190));
        stage.showAndWait();
    }

    private ObservableList<String> generarHorasDisponibles() {
        ObservableList<String> horas = FXCollections.observableArrayList();
        for (int h = 7; h <= 20; h++) {
            horas.add(String.format("%02d:00", h));
            if (h < 20) {
                horas.add(String.format("%02d:30", h));
            }
        }
        horas.add("20:30");
        return horas;
    }

    private void guardarReagenda(CitaDetalle citaSeleccionada, DatePicker dpNuevaFecha, ComboBox<String> cbNuevaHora, Stage stage) {
        if (dpNuevaFecha.getValue() == null || cbNuevaHora.getValue() == null) {
            Utilidades.mostrarAlertaSimple("Validacion", "Selecciona la nueva fecha y hora.", Alert.AlertType.WARNING);
            return;
        }

        if (dpNuevaFecha.getValue().isBefore(LocalDate.now().plusDays(1))) {
            Utilidades.mostrarAlertaSimple("Validacion", "La cita debe programarse con al menos 1 dia de antelacion.", Alert.AlertType.WARNING);
            return;
        }

        if (!generarHorasDisponibles().contains(cbNuevaHora.getValue())) {
            Utilidades.mostrarAlertaSimple("Validacion", "Selecciona un horario valido entre 07:00 y 20:30 en bloques de 30 minutos.", Alert.AlertType.WARNING);
            return;
        }

        Cita cita = new Cita();
        cita.setIdCita(citaSeleccionada.getIdCita());
        cita.setFechaCita(dpNuevaFecha.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        cita.setHoraCita(cbNuevaHora.getValue() + ":00");

        dto.RespuestaSimple respuesta = CitaImp.reagendarCita(cita);
        if (!respuesta.isError()) {
            citaSeleccionada.setFechaCita(cita.getFechaCita());
            citaSeleccionada.setHoraCita(cita.getHoraCita());
            citaSeleccionada.setEstatus("Reagendada");
            citaSeleccionada.setMotivoCancelacion(null);
            tvCitas.refresh();
            actualizarEstadoAcciones();
            stage.close();
            Utilidades.mostrarAlertaSimple("Cita Reagendada", "La cita se ha reagendado correctamente.", Alert.AlertType.INFORMATION);
        } else {
            Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
        }
    }

    private void abrirFormularioEdicion(CitaDetalle cita) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioCita.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioCitaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            controlador.inicializarParaEdicion(cita);

            Stage stage = new Stage();
            stage.setTitle("Modificar Cita");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar la ventana de modificacion.", Alert.AlertType.ERROR);
        }
    }

    @Override
    public void notificarOperacionGuardar() {
        cargarDatosTabla();
    }
}
