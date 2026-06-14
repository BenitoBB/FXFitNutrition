package fxmlfitnutrition.vistas.consulta;

import dominio.CitaImp;
import dominio.ConsultaImp;
import dominio.DietaImp;
import dominio.PacienteImp;
import dto.RSPacientes;
import dto.RespuestaSimple;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pojo.Cita;
import pojo.CitaDetalle;
import pojo.Consulta;
import pojo.Dieta;
import pojo.Paciente;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;

public class FXMLFormularioConsultaController implements Initializable {

    @FXML private Spinner<Double> spPeso;
    @FXML private Spinner<Double> spTalla;
    @FXML private Label lbIMC;
    @FXML private TextField tfEstatura;
    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<Cita> cbCitaAsociada;
    @FXML private ComboBox<Dieta> cbDietaAsignada;
    @FXML private TextArea taObservaciones;
    @FXML private Button btnRegistrar;
    @FXML private Button btnCancelar;

    private NotificacionOperacion observador;
    private Consulta consultaEdicion;
    private boolean modoEdicion;

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarPaciente(Paciente paciente) {
        if (paciente == null) {
            return;
        }
        seleccionarPacientePorId(paciente.getIdPaciente());
        if (cbPaciente.getValue() == null) {
            cbPaciente.getItems().add(paciente);
            cbPaciente.setValue(paciente);
        }
        precargarMedicionesPaciente(paciente);
    }

    public void inicializarParaEdicion(Consulta consulta) {
        this.consultaEdicion = consulta;
        this.modoEdicion = true;

        btnRegistrar.setText("Guardar");
        cbPaciente.setDisable(true);
        cbCitaAsociada.setDisable(true);

        precargarConsulta();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarSpinners();
        cargarPacientes();
        cargarCitas(null);
        cargarDietas();
        configurarListeners();
        configurarFiltrosEntrada();
        calcularIMC();
    }

    private void configurarSpinners() {
        spPeso.setValueFactory(crearValueFactory(0.0, 500.0, 0.0, 0.1, 1));
        spTalla.setValueFactory(crearValueFactory(0.0, 3.0, 0.0, 0.01, 2));
        limpiarCamposNuevaConsulta();
    }

    private SpinnerValueFactory.DoubleSpinnerValueFactory crearValueFactory(
            double minimo, double maximo, double valorInicial, double paso, int decimales) {
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(minimo, maximo, valorInicial, paso);
        valueFactory.setConverter(new StringConverter<Double>() {
            @Override
            public String toString(Double valor) {
                return valor != null && valor > 0
                        ? String.format("%." + decimales + "f", valor)
                        : "";
            }

            @Override
            public Double fromString(String texto) {
                if (texto == null || texto.trim().isEmpty()) {
                    return valueFactory.getValue() != null ? valueFactory.getValue() : 0.0;
                }
                try {
                    return Double.parseDouble(texto.trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    return valueFactory.getValue() != null ? valueFactory.getValue() : 0.0;
                }
            }
        });
        return valueFactory;
    }

    private void limpiarCamposNuevaConsulta() {
        spPeso.getEditor().clear();
        spTalla.getEditor().clear();
        if (tfEstatura != null) {
            tfEstatura.clear();
        }
        lbIMC.setText("0.00");
        cbCitaAsociada.getSelectionModel().clearSelection();
        cbDietaAsignada.getSelectionModel().clearSelection();
        taObservaciones.clear();
    }

    private void cargarPacientes() {
        RSPacientes respuesta = PacienteImp.buscarPacientes(
                "",
                Sesion.getIdMedicoParaFiltro(),
                Sesion.isEsAdministrador()
        );
        if (!respuesta.isError() && respuesta.getPacientes() != null) {
            cbPaciente.setItems(FXCollections.observableArrayList(respuesta.getPacientes()));
        }
    }

    private void cargarCitas(Paciente pacienteSeleccionado) {
        List<Cita> citas = new ArrayList<>();
        agregarCitasPorEstatus(citas, "Confirmada");
        agregarCitasPorEstatus(citas, "Reagendada");
        if (pacienteSeleccionado != null) {
            List<Cita> filtradas = new ArrayList<>();
            for (Cita cita : citas) {
                if (cita.getIdPaciente() == pacienteSeleccionado.getIdPaciente()) {
                    filtradas.add(cita);
                }
            }
            citas = filtradas;
        }
        cbCitaAsociada.setItems(FXCollections.observableArrayList(citas));
        cbCitaAsociada.getSelectionModel().clearSelection();
    }
    private void agregarCitasPorEstatus(List<Cita> citas, String estatus) {
        List<CitaDetalle> citasEstatus = CitaImp.buscarCitas(
                "",
                Sesion.getIdMedicoParaFiltro(),
                Sesion.isEsAdministrador(),
                estatus
        );
        if (citasEstatus != null) {
            citas.addAll(citasEstatus);
        }
    }

    private void cargarDietas() {
        cbDietaAsignada.setItems(FXCollections.observableArrayList(DietaImp.obtenerTodas()));
    }

    private void configurarListeners() {
        spPeso.valueProperty().addListener((observable, oldValue, newValue) -> calcularIMC());
        spTalla.valueProperty().addListener((observable, oldValue, newValue) -> calcularIMC());
        spPeso.getEditor().textProperty().addListener((observable, oldValue, newValue) -> calcularIMC());
        spTalla.getEditor().textProperty().addListener((observable, oldValue, newValue) -> calcularIMC());

        cbPaciente.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !modoEdicion) {
                precargarMedicionesPaciente(newValue);
                cargarCitas(newValue);
            }
        });

        cbCitaAsociada.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                seleccionarPacientePorId(newValue.getIdPaciente());
            }
        });
    }

    private void configurarFiltrosEntrada() {
        Utilidades.permitirSoloNumerosDecimales(spPeso.getEditor());
        Utilidades.permitirSoloNumerosDecimales(spTalla.getEditor());
        Utilidades.permitirLetrasNumeros(taObservaciones);
    }

    @FXML
    private void clicRegistrar(ActionEvent event) {
        Double peso = leerDouble(spPeso);
        Double talla = leerDouble(spTalla);

        if (cbPaciente.getValue() == null) {
            Utilidades.mostrarAlertaSimple("Validacion", "Selecciona un paciente.", Alert.AlertType.WARNING);
            return;
        }

        if (peso == null || peso <= 0) {
            Utilidades.mostrarAlertaSimple("Validacion", "El peso debe ser mayor a 0.", Alert.AlertType.WARNING);
            return;
        }

        if (talla == null || talla <= 0) {
            Utilidades.mostrarAlertaSimple("Validacion", "La talla debe ser mayor a 0 para calcular el IMC.", Alert.AlertType.WARNING);
            return;
        }

        Consulta consulta = construirConsulta(peso, talla);
        RespuestaSimple respuesta = modoEdicion
                ? ConsultaImp.modificarConsulta(consulta)
                : ConsultaImp.registrarConsulta(consulta);

        if (!respuesta.isError()) {
            String titulo = modoEdicion ? "Consulta Modificada" : "Consulta Registrada";
            String mensaje = modoEdicion ? "La consulta se modifico correctamente." : "La consulta se registro correctamente.";
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

    private Consulta construirConsulta(double peso, double talla) {
        Consulta consulta = new Consulta();
        if (modoEdicion) {
            consulta.setIdConsulta(consultaEdicion.getIdConsulta());
        }
        consulta.setIdPaciente(cbPaciente.getValue().getIdPaciente());
        consulta.setIdMedico(obtenerIdMedicoConsulta());
        consulta.setIdCita(cbCitaAsociada.getValue() != null ? cbCitaAsociada.getValue().getIdCita() : null);
        consulta.setFechaConsulta(obtenerFechaHoraConsulta());
        consulta.setPeso(peso);
        consulta.setTalla(talla);
        consulta.setImc(calcularValorIMC(peso, talla));
        consulta.setIdDieta(cbDietaAsignada.getValue() != null ? cbDietaAsignada.getValue().getIdDieta() : null);
        consulta.setObservaciones(taObservaciones.getText() != null ? taObservaciones.getText().trim() : "");
        return consulta;
    }

    private int obtenerIdMedicoConsulta() {
        if (modoEdicion) {
            return consultaEdicion.getIdMedico();
        }
        if (cbCitaAsociada.getValue() != null) {
            return cbCitaAsociada.getValue().getIdMedico();
        }
        if (Sesion.requiereFiltroPorMedico()) {
            return Sesion.getIdMedicoSesion();
        }
        return cbPaciente.getValue().getIdMedico();
    }

    private void precargarConsulta() {
        if (consultaEdicion == null) {
            return;
        }

        seleccionarPacientePorId(consultaEdicion.getIdPaciente());
        cargarCitas(cbPaciente.getValue());
        seleccionarCitaPorId(consultaEdicion.getIdCita());
        seleccionarDietaPorId(consultaEdicion.getIdDieta());

        spPeso.getValueFactory().setValue(consultaEdicion.getPeso());
        spTalla.getValueFactory().setValue(consultaEdicion.getTalla());
        spPeso.getEditor().setText(String.format("%.1f", consultaEdicion.getPeso()));
        spTalla.getEditor().setText(String.format("%.2f", consultaEdicion.getTalla()));
        taObservaciones.setText(consultaEdicion.getObservaciones() != null ? consultaEdicion.getObservaciones() : "");
        calcularIMC();
    }

    private void seleccionarCitaPorId(Integer idCita) {
        if (idCita == null) {
            return;
        }
        for (Cita cita : cbCitaAsociada.getItems()) {
            if (cita.getIdCita() == idCita) {
                cbCitaAsociada.setValue(cita);
                return;
            }
        }

        Cita citaAsociada = CitaImp.buscarCitaPorId(idCita);
        if (citaAsociada != null && citaAsociada.getIdCita() > 0) {
            cbCitaAsociada.getItems().add(citaAsociada);
            cbCitaAsociada.setValue(citaAsociada);
        }
    }

    private void seleccionarDietaPorId(Integer idDieta) {
        if (idDieta == null) {
            return;
        }
        for (Dieta dieta : cbDietaAsignada.getItems()) {
            if (dieta.getIdDieta() == idDieta) {
                cbDietaAsignada.setValue(dieta);
                return;
            }
        }
    }

    private void seleccionarPacientePorId(int idPaciente) {
        for (Paciente paciente : cbPaciente.getItems()) {
            if (paciente.getIdPaciente() == idPaciente) {
                cbPaciente.setValue(paciente);
                return;
            }
        }
    }

    private void precargarMedicionesPaciente(Paciente paciente) {
        if (paciente == null || modoEdicion) {
            return;
        }

        if (paciente.getPeso() != null && paciente.getPeso() > 0) {
            spPeso.getValueFactory().setValue(paciente.getPeso().doubleValue());
            spPeso.getEditor().setText(String.format("%.1f", paciente.getPeso()));
        }

        Float talla = paciente.getEstatura() != null && paciente.getEstatura() > 0
                ? paciente.getEstatura()
                : paciente.getTalla();
        if (talla != null && talla > 0) {
            spTalla.getValueFactory().setValue(talla.doubleValue());
            spTalla.getEditor().setText(String.format("%.2f", talla));
        }

        calcularIMC();
    }

    private void calcularIMC() {
        Double peso = leerDouble(spPeso);
        Double talla = leerDouble(spTalla);
        if (tfEstatura != null) {
            tfEstatura.setText(talla != null ? String.format("%.2f m", talla) : "");
        }

        if (peso == null || talla == null || talla == 0.0) {
            lbIMC.setText("0.00");
            return;
        }

        lbIMC.setText(String.format("%.2f", calcularValorIMC(peso, talla)));
    }

    private double calcularValorIMC(double peso, double talla) {
        return peso / (talla * talla);
    }

    private String obtenerFechaHoraConsulta() {
        if (cbCitaAsociada.getValue() != null
                && cbCitaAsociada.getValue().getFechaCita() != null
                && cbCitaAsociada.getValue().getHoraCita() != null) {
            String hora = cbCitaAsociada.getValue().getHoraCita();
            if (hora.length() == 5) {
                hora += ":00";
            }
            return cbCitaAsociada.getValue().getFechaCita() + " " + hora;
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaRedondeada = redondearABloqueMediaHora(ahora.toLocalTime());
        return ahora.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + " "
                + horaRedondeada.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private LocalTime redondearABloqueMediaHora(LocalTime hora) {
        int minuto = hora.getMinute() < 30 ? 0 : 30;
        return LocalTime.of(hora.getHour(), minuto);
    }

    private Double leerDouble(Spinner<Double> spinner) {
        String texto = spinner.getEditor().getText();
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
