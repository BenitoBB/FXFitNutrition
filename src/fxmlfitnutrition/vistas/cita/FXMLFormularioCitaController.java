package fxmlfitnutrition.vistas.cita;

import dominio.CitaImp;
import dominio.MedicoImp;
import dominio.PacienteImp;
import dto.RSPacientes;
import dto.RespuestaSimple;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pojo.Cita;
import pojo.CitaDetalle;
import pojo.Medico;
import pojo.Paciente;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;

public class FXMLFormularioCitaController implements Initializable {

    @FXML private Label lbTitulo;
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private VBox vbMedico;
    @FXML private ComboBox<Medico> cbMedico;
    @FXML private DatePicker dpFechaCita;
    @FXML private ComboBox<String> cbHora;
    @FXML private VBox vbEstatus;
    @FXML private ComboBox<String> cbEstatus;
    @FXML private TextArea taObservaciones;
    @FXML private Label lbMensajeError;
    @FXML private Button btnCancelar;
    @FXML private Button btnAgendar;

    private NotificacionOperacion observador;
    private CitaDetalle citaEdicion;
    private boolean modoEdicion;

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarParaEdicion(CitaDetalle cita) {
        this.citaEdicion = cita;
        this.modoEdicion = true;

        lbTitulo.setText("Modificar Cita");
        btnAgendar.setText("Guardar");
        cbPaciente.setDisable(true);
        cbMedico.setDisable(true);
        dpFechaCita.setDisable(true);
        cbHora.setDisable(true);
        vbEstatus.setVisible(true);
        vbEstatus.setManaged(true);

        precargarDatosCita();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarPacientes();
        cargarHoras();
        cargarEstatusEditables();
        configurarSegunRol();
        Utilidades.permitirLetrasNumeros(taObservaciones);
    }

    private void cargarPacientes() {
        RSPacientes respuesta = PacienteImp.buscarPacientes(
                "",
                Sesion.getIdMedicoParaFiltro(),
                Sesion.isEsAdministrador()
        );
        if (!respuesta.isError() && respuesta.getPacientes() != null) {
            ObservableList<Paciente> pacientes = FXCollections.observableArrayList(respuesta.getPacientes());
            cbPaciente.setItems(pacientes);
        }
    }

    private void cargarHoras() {
        ObservableList<String> horas = FXCollections.observableArrayList();
        for (int h = 7; h <= 20; h++) {
            horas.add(String.format("%02d:00", h));
            if (h < 20) {
                horas.add(String.format("%02d:30", h));
            }
        }
        horas.add("20:30");
        cbHora.setItems(horas);
    }

    private void configurarSegunRol() {
        if (Sesion.isEsAdministrador()) {
            List<Medico> medicos = MedicoImp.buscarMedicos("", true);
            if (medicos != null) {
                cbMedico.setItems(FXCollections.observableArrayList(medicos));
            }
        } else {
            vbMedico.setVisible(false);
            vbMedico.setManaged(false);
        }
    }

    private void cargarEstatusEditables() {
        cbEstatus.setItems(FXCollections.observableArrayList("Confirmada", "Reagendada", "Asistida"));
    }

    @FXML
    private void clicAgendar(ActionEvent event) {
        lbMensajeError.setVisible(false);

        if (modoEdicion && !esCitaEditable(citaEdicion)) {
            mostrarAlertaValidacion("Solo se pueden modificar citas Confirmadas o Reagendadas.");
            return;
        }

        if (cbPaciente.getValue() == null || (!modoEdicion && (dpFechaCita.getValue() == null || cbHora.getValue() == null))) {
            mostrarAlertaValidacion("Por favor completa los campos obligatorios (*).");
            return;
        }

        if (modoEdicion && cbEstatus.getValue() == null) {
            mostrarAlertaValidacion("Por favor selecciona el estatus de la cita.");
            return;
        }

        if (!modoEdicion && Sesion.isEsAdministrador() && cbMedico.getValue() == null) {
            mostrarAlertaValidacion("Por favor selecciona un medico.");
            return;
        }

        if (!modoEdicion) {
            LocalDate manana = LocalDate.now().plusDays(1);
            if (dpFechaCita.getValue().isBefore(manana)) {
                mostrarAlertaValidacion("La cita debe programarse con al menos 1 dia de antelacion.");
                return;
            }

            if (!esHoraValida(cbHora.getValue())) {
                mostrarAlertaValidacion("Selecciona un horario valido entre 07:00 y 20:30 en bloques de 30 minutos.");
                return;
            }
        }

        Cita cita = construirCitaFormulario();
        if (modoEdicion && !hayCambiosEnEdicion(cita)) {
            return;
        }

        RespuestaSimple respuesta = modoEdicion
                ? CitaImp.modificarCita(cita)
                : CitaImp.crearCita(cita);

        if (!respuesta.isError()) {
            String titulo = modoEdicion ? "Cita Modificada" : "Cita Agendada";
            String mensaje = modoEdicion ? "La cita se ha modificado exitosamente." : "La cita se ha programado exitosamente.";
            Utilidades.mostrarAlertaSimple(titulo, mensaje, Alert.AlertType.INFORMATION);
            if (observador != null) {
                observador.notificarOperacionGuardar();
            }
            cerrarVentana();
        } else {
            Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicCancelar(ActionEvent event) {
        cerrarVentana();
    }

    private Cita construirCitaFormulario() {
        Cita cita = new Cita();
        if (modoEdicion) {
            cita.setIdCita(citaEdicion.getIdCita());
        }

        cita.setIdPaciente(cbPaciente.getValue().getIdPaciente());
        if (!modoEdicion) {
            cita.setFechaCita(dpFechaCita.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            cita.setHoraCita(normalizarHoraParaApi(cbHora.getValue()));
        }
        cita.setObservaciones(taObservaciones.getText() != null ? taObservaciones.getText().trim() : "");
        if (modoEdicion) {
            cita.setEstatus(cbEstatus.getValue());
        }

        if (modoEdicion) {
            cita.setIdMedico(citaEdicion.getIdMedico());
        } else if (Sesion.isEsAdministrador()) {
            cita.setIdMedico(cbMedico.getValue().getIdMedico());
        } else {
            cita.setIdMedico(Sesion.getMedicoSesion().getIdMedico());
        }

        return cita;
    }

    private void precargarDatosCita() {
        if (citaEdicion == null) {
            return;
        }

        seleccionarPacienteCita();
        seleccionarMedicoCita();

        if (citaEdicion.getFechaCita() != null && !citaEdicion.getFechaCita().trim().isEmpty()) {
            dpFechaCita.setValue(LocalDate.parse(citaEdicion.getFechaCita()));
        }

        cbHora.setValue(formatearHoraVista(citaEdicion.getHoraCita()));
        configurarEstatusParaCitaActual();
        cbEstatus.setValue(citaEdicion.getEstatus());
        taObservaciones.setText(citaEdicion.getObservaciones() != null ? citaEdicion.getObservaciones() : "");
    }

    private void configurarEstatusParaCitaActual() {
        if ("Reagendada".equals(citaEdicion.getEstatus())) {
            cbEstatus.setItems(FXCollections.observableArrayList("Reagendada", "Confirmada", "Asistida"));
        } else {
            cbEstatus.setItems(FXCollections.observableArrayList("Confirmada", "Asistida"));
        }
    }

    private void seleccionarPacienteCita() {
        Paciente paciente = buscarPacientePorId(citaEdicion.getIdPaciente());
        if (paciente == null) {
            paciente = new Paciente();
            paciente.setIdPaciente(citaEdicion.getIdPaciente());
            paciente.setNombre(citaEdicion.getPacienteNombre());
            paciente.setPrimerApellido(citaEdicion.getPacientePrimerApellido());
            paciente.setSegundoApellido(citaEdicion.getPacienteSegundoApellido());
            cbPaciente.getItems().add(paciente);
        }
        cbPaciente.setValue(paciente);
    }

    private void seleccionarMedicoCita() {
        if (!Sesion.isEsAdministrador()) {
            return;
        }

        Medico medico = buscarMedicoPorId(citaEdicion.getIdMedico());
        if (medico == null) {
            medico = new Medico();
            medico.setIdMedico(citaEdicion.getIdMedico());
            medico.setNombre(citaEdicion.getMedicoNombre());
            medico.setPrimerApellido(citaEdicion.getMedicoPrimerApellido());
            medico.setSegundoApellido(citaEdicion.getMedicoSegundoApellido());
            cbMedico.getItems().add(medico);
        }
        cbMedico.setValue(medico);
    }

    private Paciente buscarPacientePorId(int idPaciente) {
        for (Paciente paciente : cbPaciente.getItems()) {
            if (paciente.getIdPaciente() == idPaciente) {
                return paciente;
            }
        }
        return null;
    }

    private Medico buscarMedicoPorId(int idMedico) {
        for (Medico medico : cbMedico.getItems()) {
            if (medico.getIdMedico() == idMedico) {
                return medico;
            }
        }
        return null;
    }

    private boolean esCitaEditable(CitaDetalle cita) {
        return cita != null
                && ("Confirmada".equals(cita.getEstatus()) || "Reagendada".equals(cita.getEstatus()));
    }

    private boolean esHoraValida(String hora) {
        return cbHora.getItems().contains(formatearHoraVista(hora));
    }

    private boolean hayCambiosEnEdicion(Cita cita) {
        if (citaEdicion == null) {
            return false;
        }

        String observacionesActuales = normalizarTexto(citaEdicion.getObservaciones());
        String estatusActual = normalizarTexto(citaEdicion.getEstatus());

        return !observacionesActuales.equals(normalizarTexto(cita.getObservaciones()))
                || !estatusActual.equals(normalizarTexto(cita.getEstatus()));
    }

    private String formatearHoraVista(String hora) {
        if (hora == null) {
            return null;
        }
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    private String normalizarHoraParaApi(String hora) {
        String horaVista = formatearHoraVista(hora);
        return horaVista != null && horaVista.length() == 5 ? horaVista + ":00" : horaVista;
    }

    private String normalizarTexto(String texto) {
        return texto != null ? texto.trim() : "";
    }

    private void mostrarAlertaValidacion(String mensaje) {
        Utilidades.mostrarAlertaSimple("Validacion", mensaje, Alert.AlertType.WARNING);
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
