package fxmlfitnutrition.vistas.medico;

import dominio.MedicoImp;
import dto.RQBajaMedico;
import dto.Respuesta;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.Medico;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLMedicosController implements Initializable, NotificacionOperacion {

    @FXML private TableView<Medico> tvMedicos;
    @FXML private TableColumn<Medico, String> colNombre;
    @FXML private TableColumn<Medico, String> colCedula;
    @FXML private TableColumn<Medico, String> colDireccion;
    @FXML private TableColumn<Medico, String> colAdmin;
    @FXML private TextField tfBuscar;

    private ObservableList<Medico> listaMedicos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarDatosTabla("");

        tfBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 2 || newValue.isEmpty()) {
                cargarDatosTabla(newValue);
            }
        });
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreCompleto()));
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedulaProfesional"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccionCompleta"));
        colAdmin.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().esAdmin() ? "Sí" : "No"));
        
        listaMedicos = FXCollections.observableArrayList();
        tvMedicos.setItems(listaMedicos);
    }

    private void cargarDatosTabla(String criterio) {
        List<Medico> medicos = MedicoImp.buscarMedicos(criterio, true);
        listaMedicos.clear();
        if (medicos != null) {
            listaMedicos.addAll(medicos);
        }
    }

    @FXML
    private void clicNuevo(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    private void clicEditar(ActionEvent event) {
        Medico medico = tvMedicos.getSelectionModel().getSelectedItem();
        if (medico != null) {
            abrirFormulario(medico);
        } else {
            Utilidades.mostrarAlertaSimple("Atención", "Selecciona un médico de la tabla.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void clicBaja(ActionEvent event) {
        Medico medicoSeleccionado = tvMedicos.getSelectionModel().getSelectedItem();
        if (medicoSeleccionado != null) {
            boolean confirmar = Utilidades.mostrarAlertaConfirmacion(
                "Confirmar Baja", 
                "¿Estás seguro de dar de baja al médico " + medicoSeleccionado.getNombre() + "?"
            );
            
            if (confirmar) {
                List<Medico> medicosDisponibles = new ArrayList<>(listaMedicos);
                medicosDisponibles.remove(medicoSeleccionado);
                
                if (medicosDisponibles.isEmpty()) {
                    Utilidades.mostrarAlertaSimple("Error", "No hay otros médicos para reasignar a sus pacientes. Es necesario tener al menos otro médico activo.", Alert.AlertType.ERROR);
                    return;
                }

                ChoiceDialog<Medico> dialog = new ChoiceDialog<>(medicosDisponibles.get(0), medicosDisponibles);
                dialog.setTitle("Reasignación de pacientes");
                dialog.setHeaderText("El médico a dar de baja podría tener pacientes asignados.");
                dialog.setContentText("Selecciona el médico al que se le reasignarán los pacientes:");
                
                Optional<Medico> resultado = dialog.showAndWait();
                if (resultado.isPresent()) {
                    Medico medicoReasignacion = resultado.get();
                    RQBajaMedico rq = new RQBajaMedico();
                    rq.setIdMedicoBaja(medicoSeleccionado.getIdMedico());
                    rq.setIdMedicoNuevo(medicoReasignacion.getIdMedico());
                    rq.setEsAdministrador(1); 

                    Respuesta respuesta = MedicoImp.darDeBajaMedico(rq);
                    if (!respuesta.isError()) {
                        Utilidades.mostrarAlertaSimple("Éxito", respuesta.getMensaje(), Alert.AlertType.INFORMATION);
                        cargarDatosTabla(tfBuscar.getText());
                    } else {
                        Utilidades.mostrarAlertaSimple("Error", respuesta.getMensaje(), Alert.AlertType.ERROR);
                    }
                }
            }
        } else {
            Utilidades.mostrarAlertaSimple("Atención", "Por favor selecciona un médico de la tabla.", Alert.AlertType.WARNING);
        }
    }

    private void abrirFormulario(Medico medico) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLMedicoFormulario.fxml"));
            Parent root = fxmlLoader.load();
            
            FXMLMedicoFormularioController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            if (medico != null) {
                controlador.inicializarParaEdicion(medico);
            }

            Stage stage = new Stage();
            stage.setTitle(medico != null ? "Editar Médico" : "Registrar Médico");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo abrir el formulario.", Alert.AlertType.ERROR);
        }
    }

    @Override
    public void notificarOperacionGuardar() {
        cargarDatosTabla("");
    }
}
