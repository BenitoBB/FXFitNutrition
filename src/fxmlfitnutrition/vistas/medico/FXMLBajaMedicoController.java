package fxmlfitnutrition.vistas.medico;

import dominio.MedicoImp;
import dominio.PacienteImp;
import dto.RQBajaMedico;
import dto.RSPacientes;
import dto.Respuesta;
import java.net.URL;
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
import javafx.stage.Stage;
import pojo.Medico;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLBajaMedicoController implements Initializable {

    @FXML private Label lbMedicoBaja;
    @FXML private Label lbCantidadPacientes;
    @FXML private ComboBox<Medico> cbMedicoReceptor;
    @FXML private Button btnConfirmarBaja;
    @FXML private Button btnCancelar;

    private Medico medicoBaja;
    private List<Medico> medicosReceptores;
    private NotificacionOperacion observador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void inicializarValores(Medico medicoBaja, List<Medico> medicosReceptores, NotificacionOperacion observador) {
        this.medicoBaja = medicoBaja;
        this.medicosReceptores = medicosReceptores;
        this.observador = observador;

        lbMedicoBaja.setText("Médico a dar de baja: " + medicoBaja.getNombreCompleto());
        cbMedicoReceptor.setItems(FXCollections.observableArrayList(medicosReceptores));
        
        cargarCantidadPacientes();
    }

    private void cargarCantidadPacientes() {
        RSPacientes rs = PacienteImp.buscarPacientes("", medicoBaja.getIdMedico(), false);
        if (!rs.isError() && rs.getPacientes() != null) {
            lbCantidadPacientes.setText("Pacientes a reasignar: " + rs.getPacientes().size());
        } else {
            lbCantidadPacientes.setText("Pacientes a reasignar: Desconocido");
        }
    }

    @FXML
    private void clicConfirmarBaja(ActionEvent event) {
        Medico medicoReceptor = cbMedicoReceptor.getValue();
        if (medicoReceptor == null) {
            Utilidades.mostrarAlertaSimple("Atención", "Debe seleccionar un médico receptor para los pacientes.", Alert.AlertType.WARNING);
            return;
        }

        boolean confirmar = Utilidades.mostrarAlertaConfirmacion(
                "Confirmar Baja", 
                "¿Está seguro de dar de baja al médico " + medicoBaja.getNombreCompleto() + 
                " y reasignar sus pacientes a " + medicoReceptor.getNombreCompleto() + "?"
        );

        if (confirmar) {
            RQBajaMedico rq = new RQBajaMedico();
            rq.setIdMedicoBaja(medicoBaja.getIdMedico());
            rq.setIdMedicoNuevo(medicoReceptor.getIdMedico());
            rq.setEsAdministrador(1);

            Respuesta respuesta = MedicoImp.darDeBajaMedico(rq);
            if (!respuesta.isError()) {
                Utilidades.mostrarAlertaSimple("Éxito", respuesta.getMensaje(), Alert.AlertType.INFORMATION);
                if (observador != null) {
                    observador.notificarOperacionGuardar();
                }
                cerrarVentana();
            } else {
                Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
            }
        }
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
