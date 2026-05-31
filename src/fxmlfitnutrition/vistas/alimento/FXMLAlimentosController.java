package fxmlfitnutrition.vistas.alimento;


import dominio.AlimentoImp;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.Alimento;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLAlimentosController implements Initializable, NotificacionOperacion {

    @FXML private TextField tfBuscar;
    @FXML private Button btnRegistrar;
    @FXML private Button btnEditar;
    @FXML private TableView<Alimento> tvAlimentos;
    @FXML private TableColumn<Alimento, String> colNombre;
    @FXML private TableColumn<Alimento, String> colPorcion;
    @FXML private TableColumn<Alimento, Double> colCalorias;

    private ObservableList<Alimento> alimentos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        configurarBusqueda();
        btnEditar.setDisable(true);
        cargarAlimentos("");
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreAlimento"));
        colPorcion.setCellValueFactory(new PropertyValueFactory<>("porcion"));
        colCalorias.setCellValueFactory(new PropertyValueFactory<>("caloriasPorcion"));

        alimentos = FXCollections.observableArrayList();
        tvAlimentos.setItems(alimentos);
        tvAlimentos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnEditar.setDisable(newValue == null);
        });
    }

    private void configurarBusqueda() {
        Utilidades.permitirSoloLetras(tfBuscar);
        tfBuscar.setOnKeyReleased(event -> buscarAlimentosEnTiempoReal());
    }

    @FXML
    private void clicNuevo(ActionEvent event) {
        abrirFormulario(null);
    }

    private void abrirFormulario(Alimento alimento) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioAlimento.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioAlimentoController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            if (alimento != null) {
                controlador.inicializarParaEdicion(alimento);
            }

            Stage stage = new Stage();
            stage.setTitle(alimento != null ? "Editar Alimento" : "Nuevo Alimento");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar el formulario de alimento.", Alert.AlertType.ERROR);
        }
    }

}
