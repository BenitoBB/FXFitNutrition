package fxmlfitnutrition.vistas.dieta;

import dominio.DietaImp;
import dto.RespuestaSimple;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.AlimentoEnDieta;
import pojo.CategoriaConAlimentos;
import pojo.Dieta;
import pojo.DietaDetalle;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLDietasController implements Initializable, NotificacionOperacion {

    @FXML private Button btnCrear;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private TableView<Dieta> tvDietas;
    @FXML private TableColumn<Dieta, String> colNombreDieta;
    @FXML private TableColumn<Dieta, Double> colTotalCalorias;
    @FXML private TableColumn<Dieta, String> colEditable;
    @FXML private TreeView<String> tvDetalleDieta;

    private ObservableList<Dieta> dietas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        cargarDietas();
    }

    private void configurarTabla() {
        colNombreDieta.setCellValueFactory(new PropertyValueFactory<>("nombreDieta"));
        colTotalCalorias.setCellValueFactory(new PropertyValueFactory<>("totalCalorias"));
        colEditable.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEditable() == 1 ? "Editable" : "Bloqueada"
        ));

        dietas = FXCollections.observableArrayList();
        tvDietas.setItems(dietas);
        tvDietas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean sinSeleccion = newValue == null;
            btnEditar.setDisable(sinSeleccion || newValue.getEditable() == 0);
            btnEliminar.setDisable(sinSeleccion || newValue.getEditable() == 0);
            cargarDetalle(newValue);
        });
    }

    private void cargarDietas() {
        dietas.clear();
        dietas.addAll(DietaImp.obtenerTodas());
    }

    @FXML
    private void clicNuevaDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioDieta.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioDietaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);

            Stage stage = new Stage();
            stage.setTitle("Nueva Dieta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar el formulario de dieta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicEditar(ActionEvent event) {
        Dieta dietaSeleccionada = tvDietas.getSelectionModel().getSelectedItem();
        if (dietaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Selecciona una dieta.", Alert.AlertType.WARNING);
            return;
        }
        if (dietaSeleccionada.getEditable() == 0) {
            Utilidades.mostrarAlertaSimple("Dieta Bloqueada", "La dieta esta asignada a mas de un paciente y no puede editarse.", Alert.AlertType.WARNING);
            return;
        }

        DietaDetalle detalle = DietaImp.obtenerDetalle(dietaSeleccionada.getIdDieta());
        if (detalle == null) {
            Utilidades.mostrarAlertaSimple("Error", "No se pudo obtener el detalle de la dieta.", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFormularioDieta.fxml"));
            Parent root = fxmlLoader.load();

            FXMLFormularioDietaController controlador = fxmlLoader.getController();
            controlador.inicializarValores(this);
            controlador.inicializarParaEdicion(detalle);

            Stage stage = new Stage();
            stage.setTitle("Modificar Dieta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Utilidades.mostrarAlertaSimple("Error", "No se pudo cargar el formulario de dieta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clicEliminar(ActionEvent event) {
        Dieta dietaSeleccionada = tvDietas.getSelectionModel().getSelectedItem();
        if (dietaSeleccionada == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Selecciona una dieta.", Alert.AlertType.WARNING);
            return;
        }

        if (dietaSeleccionada.getEditable() == 0) {
            Utilidades.mostrarAlertaSimple("Dieta Bloqueada", "La dieta esta asignada a mas de un paciente y no puede eliminarse.", Alert.AlertType.WARNING);
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Eliminar Dieta");
        alerta.setHeaderText("Confirmar eliminaci\u00f3n");
        alerta.setContentText("La dieta se eliminara del catalogo si no esta en uso.");
        Optional<ButtonType> respuestaConfirmacion = alerta.showAndWait();
        if (!respuestaConfirmacion.isPresent() || respuestaConfirmacion.get() != ButtonType.OK) {
            return;
        }

        RespuestaSimple respuesta = DietaImp.eliminarDieta(dietaSeleccionada.getIdDieta());
        if (!respuesta.isError()) {
            Utilidades.mostrarAlertaSimple("Dieta Eliminada", "La dieta se elimino correctamente.", Alert.AlertType.INFORMATION);
            cargarDietas();
            tvDetalleDieta.setRoot(null);
        } else {
            String mensaje = respuesta.getMensaje() != null ? respuesta.getMensaje() : "";
            if (mensaje.toLowerCase().contains("consulta") || mensaje.toLowerCase().contains("foreign key")) {
                mensaje = "La dieta esta asignada a consultas y no puede eliminarse.";
            }
            Utilidades.mostrarAlertaSimple("Error", mensaje, Alert.AlertType.ERROR);
        }
    }

    private void cargarDetalle(Dieta dieta) {
        if (dieta == null) {
            tvDetalleDieta.setRoot(null);
            return;
        }

        DietaDetalle detalle = DietaImp.obtenerDetalle(dieta.getIdDieta());
        if (detalle == null) {
            TreeItem<String> raizError = new TreeItem<>("No se pudo cargar el detalle");
            raizError.setExpanded(true);
            tvDetalleDieta.setRoot(raizError);
            return;
        }

        TreeItem<String> raiz = new TreeItem<>(detalle.getNombreDieta()
                + " - " + String.format("%.2f kcal", detalle.getTotalCalorias()));
        raiz.setExpanded(true);

        if (detalle.getCategorias() != null) {
            for (CategoriaConAlimentos categoria : detalle.getCategorias()) {
                TreeItem<String> itemCategoria = new TreeItem<>(categoria.getNombreCategoria());
                itemCategoria.setExpanded(true);

                if (categoria.getAlimentos() == null || categoria.getAlimentos().isEmpty()) {
                    itemCategoria.getChildren().add(new TreeItem<>("Sin alimentos"));
                } else {
                    for (AlimentoEnDieta alimento : categoria.getAlimentos()) {
                        itemCategoria.getChildren().add(new TreeItem<>(
                                alimento.getNombreAlimento()
                                + " | " + alimento.getCantidad()
                                + " x " + alimento.getPorcion()
                                + " | " + String.format("%.2f kcal", alimento.getCaloriasTotales())
                        ));
                    }
                }

                raiz.getChildren().add(itemCategoria);
            }
        }

        tvDetalleDieta.setRoot(raiz);
    }

    @Override
    public void notificarOperacionGuardar() {
        cargarDietas();
    }
}
