package fxmlfitnutrition.vistas.consulta;

import dominio.ConsultaImp;
import dominio.PacienteImp;
import dto.RSPacientes;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.Consulta;
import pojo.Paciente;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;

public class FXMLConsultasController implements Initializable, NotificacionOperacion {

    @FXML private Button btnRegistrar;
    @FXML private Button btnLimpiarFechas;
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private DatePicker dpFechaConsulta;
    @FXML private TableView<Consulta> tvConsultas;
    @FXML private TableColumn<Consulta, String> colFecha;
    @FXML private TableColumn<Consulta, String> colHora;
    @FXML private TableColumn<Consulta, String> colPaciente;
    @FXML private TableColumn<Consulta, Double> colPeso;
    @FXML private TableColumn<Consulta, Double> colTalla;
    @FXML private TableColumn<Consulta, String> colIMC;
    @FXML private TableColumn<Consulta, String> colDieta;
    @FXML private TableColumn<Consulta, String> colEstatus;
    @FXML private TableColumn<Consulta, String> colObservaciones;

    private final ObservableList<Consulta> consultas = FXCollections.observableArrayList();
    private final List<Consulta> consultasBase = new ArrayList<>();
    private final Set<LocalDate> fechasSeleccionadas = new HashSet<>();
    private final Map<Integer, Paciente> pacientesPorId = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarFiltros();
        configurarCalendarioFechas();
        cargarPacientes();
        cargarConsultas();
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaConsultaCorta"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaConsulta"));
        colPaciente.setCellValueFactory(cellData -> new SimpleStringProperty(obtenerNombrePaciente(cellData.getValue().getIdPaciente())));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colTalla.setCellValueFactory(new PropertyValueFactory<>("talla"));
        colIMC.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImc() != null ? String.format("%.2f", cellData.getValue().getImc()) : ""));
        colDieta.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreDieta() != null ? cellData.getValue().getNombreDieta() : "Sin dieta"));
        colEstatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActiva() ? "Activa" : "Cancelada"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        tvConsultas.setItems(consultas);
    }

    private void configurarFiltros() {
        cbPaciente.valueProperty().addListener((observable, oldValue, newValue) -> cargarConsultas());
        dpFechaConsulta.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                alternarFechaSeleccionada(newValue);
                actualizarEstadoFechas();
                aplicarFiltroFechas();
                dpFechaConsulta.setValue(null);
            }
        });
        btnLimpiarFechas.setDisable(true);
    }

    private void configurarCalendarioFechas() {
        dpFechaConsulta.setDayCellFactory(datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || fecha == null) {
                    setStyle("");
                } else if (fechasSeleccionadas.contains(fecha)) {
                    setStyle("-fx-background-color: #26AA54; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void cargarPacientes() {
        RSPacientes respuesta = PacienteImp.buscarPacientes("", Sesion.getIdMedicoParaFiltro(), Sesion.isEsAdministrador());
        if (!respuesta.isError() && respuesta.getPacientes() != null) {
            pacientesPorId.clear();
            ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
            Paciente todos = crearPacienteTodos();
            pacientes.add(todos);
            for (Paciente paciente : respuesta.getPacientes()) {
                pacientesPorId.put(paciente.getIdPaciente(), paciente);
                pacientes.add(paciente);
            }
            cbPaciente.setItems(pacientes);
            if (cbPaciente.getValue() == null) {
                cbPaciente.setValue(todos);
            }
        }
    }

    private void cargarConsultas() {
        Paciente paciente = obtenerPacienteSeleccionado();
        consultasBase.clear();
        if (paciente != null) {
            consultasBase.addAll(ConsultaImp.buscarConsultasPaciente(paciente.getIdPaciente()));
        } else {
            consultasBase.addAll(ConsultaImp.buscarConsultas("", Sesion.getIdMedicoParaFiltro(), Sesion.isEsAdministrador()));
        }
        aplicarFiltroFechas();
    }

    private void aplicarFiltroFechas() {
        consultas.clear();
        if (fechasSeleccionadas.isEmpty()) {
            consultas.addAll(consultasBase);
            return;
        }
        for (Consulta consulta : consultasBase) {
            LocalDate fecha = obtenerFechaConsulta(consulta);
            if (fecha != null && fechasSeleccionadas.contains(fecha)) {
                consultas.add(consulta);
            }
        }
    }

    @FXML
    private void clicLimpiarFechas(ActionEvent event) {
        fechasSeleccionadas.clear();
        actualizarEstadoFechas();
        aplicarFiltroFechas();
    }

    @FXML
    private void clicRegistrar(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioConsulta.fxml"));
            Parent root = fxmlLoader.load();
            FXMLFormularioConsultaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            controlador.inicializarPaciente(obtenerPacienteSeleccionado());
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

    private void alternarFechaSeleccionada(LocalDate fecha) {
        if (fechasSeleccionadas.contains(fecha)) {
            fechasSeleccionadas.remove(fecha);
        } else {
            fechasSeleccionadas.add(fecha);
        }
    }

    private void actualizarEstadoFechas() {
        if (fechasSeleccionadas.isEmpty()) {
            dpFechaConsulta.setPromptText("Todas las fechas");
            btnLimpiarFechas.setDisable(true);
            return;
        }
        dpFechaConsulta.setPromptText(fechasSeleccionadas.size() == 1 ? "1 fecha seleccionada" : fechasSeleccionadas.size() + " fechas seleccionadas");
        btnLimpiarFechas.setDisable(false);
    }

    private Paciente obtenerPacienteSeleccionado() {
        Paciente paciente = cbPaciente.getValue();
        return paciente != null && paciente.getIdPaciente() > 0 ? paciente : null;
    }

    private Paciente crearPacienteTodos() {
        Paciente paciente = new Paciente();
        paciente.setIdPaciente(0);
        paciente.setNombre("Todos los pacientes");
        paciente.setPrimerApellido("");
        paciente.setSegundoApellido("");
        return paciente;
    }

    private LocalDate obtenerFechaConsulta(Consulta consulta) {
        String fecha = consulta.getFechaConsultaCorta();
        if (fecha == null || fecha.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(fecha);
        } catch (Exception e) {
            return null;
        }
    }

    private String obtenerNombrePaciente(int idPaciente) {
        Paciente paciente = pacientesPorId.get(idPaciente);
        return paciente != null ? paciente.getNombreCompleto() : "Paciente #" + idPaciente;
    }

    @Override
    public void notificarOperacionGuardar() {
        cargarPacientes();
        cargarConsultas();
    }
}