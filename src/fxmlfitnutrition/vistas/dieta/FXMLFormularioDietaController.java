package fxmlfitnutrition.vistas.dieta;

import dominio.AlimentoImp;
import dominio.DietaImp;
import dto.RQAlimentoEnCategoria;
import dto.RQCrearDieta;
import dto.RQModificarDieta;
import dto.RespuestaSimple;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pojo.Alimento;
import pojo.AlimentoEnDieta;
import pojo.CategoriaConAlimentos;
import pojo.DietaDetalle;
import utilidad.NotificacionOperacion;
import utilidad.Sesion;
import utilidad.Utilidades;

public class FXMLFormularioDietaController implements Initializable {

    @FXML private Label lbTitulo;
    @FXML private TextField tfNombreDieta;
    @FXML private TextArea taObservaciones;
    @FXML private VBox vbCategorias;
    @FXML private VBox vbEdicionAlimentos;
    @FXML private ComboBox<CategoriaConAlimentos> cbCategoriaEdicion;
    @FXML private TableView<AlimentoEnDieta> tvAlimentosCategoria;
    @FXML private TableColumn<AlimentoEnDieta, String> colAlimento;
    @FXML private TableColumn<AlimentoEnDieta, String> colPorcion;
    @FXML private TableColumn<AlimentoEnDieta, Double> colCantidad;
    @FXML private TableColumn<AlimentoEnDieta, String> colCalorias;
    @FXML private TextField tfBuscarAlimento;
    @FXML private ComboBox<Alimento> cbAlimento;
    @FXML private Spinner<Double> spCantidad;
    @FXML private Label lbTotalCalorias;
    @FXML private Button btnAgregarCategoria;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private NotificacionOperacion observador;
    private boolean modoEdicion;
    private DietaDetalle dietaEdicion;
    private ObservableList<AlimentoEnDieta> alimentosCategoria;
    private final List<Integer> categoriasEliminar = new ArrayList<>();
    private final List<RQAlimentoEnCategoria> alimentosAgregar = new ArrayList<>();
    private final List<RQAlimentoEnCategoria> alimentosEliminar = new ArrayList<>();

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarParaEdicion(DietaDetalle dieta) {
        this.modoEdicion = true;
        this.dietaEdicion = dieta;

        lbTitulo.setText("Modificar Dieta");
        btnGuardar.setText("Guardar");
        vbEdicionAlimentos.setVisible(true);
        vbEdicionAlimentos.setManaged(true);
        tfNombreDieta.setText(dieta.getNombreDieta());
        taObservaciones.setText(dieta.getObservaciones() != null ? dieta.getObservaciones() : "");

        vbCategorias.getChildren().clear();
        cbCategoriaEdicion.getItems().clear();
        if (dieta.getCategorias() != null) {
            for (CategoriaConAlimentos categoria : dieta.getCategorias()) {
                agregarFilaCategoria(categoria.getNombreCategoria(), categoria);
                cbCategoriaEdicion.getItems().add(categoria);
            }
        }
        if (!cbCategoriaEdicion.getItems().isEmpty()) {
            cbCategoriaEdicion.getSelectionModel().selectFirst();
        }
        actualizarTotalVisual();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablaAlimentos();
        configurarSpinner();
        configurarListeners();
        configurarFiltrosEntrada();
        agregarFilaCategoria("Desayuno", null);
        agregarFilaCategoria("Comida", null);
        agregarFilaCategoria("Cena", null);
    }

    private void configurarTablaAlimentos() {
        colAlimento.setCellValueFactory(new PropertyValueFactory<>("nombreAlimento"));
        colPorcion.setCellValueFactory(new PropertyValueFactory<>("porcion"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCalorias.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("%.2f", cellData.getValue().getCaloriasTotales())
        ));
        alimentosCategoria = FXCollections.observableArrayList();
        tvAlimentosCategoria.setItems(alimentosCategoria);
    }

    private void configurarSpinner() {
        spCantidad.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 999.0, 1.0, 0.5));
    }

    private void configurarListeners() {
        cbCategoriaEdicion.valueProperty().addListener((observable, oldValue, newValue) -> cargarAlimentosCategoria(newValue));
        tfBuscarAlimento.setOnKeyReleased(event -> cargarCatalogoAlimentos(tfBuscarAlimento.getText()));
        cargarCatalogoAlimentos("");
    }

    private void configurarFiltrosEntrada() {
        Utilidades.permitirSoloLetras(tfNombreDieta);
        Utilidades.permitirLetrasNumeros(taObservaciones);
        Utilidades.permitirSoloLetras(tfBuscarAlimento);
    }

    @FXML
    private void clicAgregarCategoria(ActionEvent event) {
        agregarFilaCategoria("", null);
    }

    private void cargarCatalogoAlimentos(String criterio) {
        List<Alimento> alimentos = AlimentoImp.buscarAlimentos(criterio);
        cbAlimento.setItems(FXCollections.observableArrayList(alimentos));
        if (!alimentos.isEmpty()) {
            cbAlimento.getSelectionModel().selectFirst();
        } else if (criterio != null && criterio.trim().length() >= 2) {
            Utilidades.mostrarAlertaSimple("Busqueda", "No se encontraron alimentos con ese criterio.", Alert.AlertType.INFORMATION);
        } else {
            cbAlimento.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void clicAgregarAlimento(ActionEvent event) {
        CategoriaConAlimentos categoria = cbCategoriaEdicion.getValue();
        Alimento alimento = cbAlimento.getValue();
        if (categoria == null || categoria.getIdCategoria() <= 0) {
            Utilidades.mostrarAlertaSimple("Validacion", "Selecciona una categoria existente para agregar alimentos.", Alert.AlertType.WARNING);
            return;
        }
        if (alimento == null) {
            Utilidades.mostrarAlertaSimple("Validacion", "Busca y selecciona un alimento.", Alert.AlertType.WARNING);
            return;
        }

        double cantidad = spCantidad.getValue() != null ? spCantidad.getValue() : 0.0;
        if (cantidad <= 0) {
            Utilidades.mostrarAlertaSimple("Validacion", "La cantidad debe ser mayor a 0.", Alert.AlertType.WARNING);
            return;
        }

        if (contieneAlimento(categoria, alimento.getIdAlimento())) {
            Utilidades.mostrarAlertaSimple("Validacion", "Ese alimento ya esta en la categoria seleccionada.", Alert.AlertType.WARNING);
            return;
        }

        AlimentoEnDieta alimentoEnDieta = new AlimentoEnDieta();
        alimentoEnDieta.setIdAlimento(alimento.getIdAlimento());
        alimentoEnDieta.setNombreAlimento(alimento.getNombreAlimento());
        alimentoEnDieta.setPorcion(alimento.getPorcion());
        alimentoEnDieta.setCaloriasPorcion(alimento.getCaloriasPorcion());
        alimentoEnDieta.setCantidad(cantidad);
        categoria.getAlimentos().add(alimentoEnDieta);
        alimentosCategoria.add(alimentoEnDieta);

        RQAlimentoEnCategoria rq = new RQAlimentoEnCategoria();
        rq.setIdCategoria(categoria.getIdCategoria());
        rq.setIdAlimento(alimento.getIdAlimento());
        rq.setCantidad(cantidad);
        alimentosAgregar.add(rq);
        quitarAlimentoPendienteEliminacion(categoria.getIdCategoria(), alimento.getIdAlimento());
        actualizarTotalVisual();
    }

    @FXML
    private void clicQuitarAlimento(ActionEvent event) {
        CategoriaConAlimentos categoria = cbCategoriaEdicion.getValue();
        AlimentoEnDieta alimento = tvAlimentosCategoria.getSelectionModel().getSelectedItem();
        if (categoria == null || alimento == null) {
            Utilidades.mostrarAlertaSimple("Atencion", "Selecciona un alimento de la categoria.", Alert.AlertType.WARNING);
            return;
        }

        categoria.getAlimentos().remove(alimento);
        alimentosCategoria.remove(alimento);
        quitarAlimentoPendienteAgregar(categoria.getIdCategoria(), alimento.getIdAlimento());

        RQAlimentoEnCategoria rq = new RQAlimentoEnCategoria();
        rq.setIdCategoria(categoria.getIdCategoria());
        rq.setIdAlimento(alimento.getIdAlimento());
        alimentosEliminar.add(rq);
        actualizarTotalVisual();
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        RespuestaSimple respuesta = modoEdicion ? guardarEdicion() : guardarNuevo();
        if (respuesta == null) {
            return;
        }

        if (!respuesta.isError()) {
            Utilidades.mostrarAlertaSimple(
                    modoEdicion ? "Dieta Modificada" : "Dieta Registrada",
                    modoEdicion ? "La dieta se modifico correctamente." : "La dieta se registro correctamente.",
                    Alert.AlertType.INFORMATION
            );
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

    private RespuestaSimple guardarNuevo() {
        RQCrearDieta dieta = construirDietaNueva();
        return dieta != null ? DietaImp.crearDieta(dieta) : null;
    }

    private RespuestaSimple guardarEdicion() {
        RQModificarDieta dieta = construirDietaEdicion();
        return dieta != null ? DietaImp.modificarDieta(dieta) : null;
    }

    private void agregarFilaCategoria(String nombreCategoria, CategoriaConAlimentos categoriaExistente) {
        HBox fila = new HBox(10.0);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setUserData(categoriaExistente);

        TextField tfCategoria = new TextField();
        tfCategoria.setPromptText("Ej. Desayuno");
        tfCategoria.setText(nombreCategoria);
        tfCategoria.setPrefHeight(36.0);
        Utilidades.permitirSoloLetras(tfCategoria);
        HBox.setHgrow(tfCategoria, Priority.ALWAYS);
        if (categoriaExistente != null) {
            tfCategoria.setDisable(true);
        }

        Button btnEliminar = new Button("Eliminar Horario");
        btnEliminar.setPrefHeight(36.0);
        btnEliminar.getStyleClass().add("button-danger");
        btnEliminar.setOnAction(event -> eliminarFilaCategoria(fila));

        fila.getChildren().addAll(tfCategoria, btnEliminar);
        vbCategorias.getChildren().add(fila);
    }

    private void eliminarFilaCategoria(HBox fila) {
        if (vbCategorias.getChildren().size() == 1) {
            Utilidades.mostrarAlertaSimple("Validacion", "La dieta debe tener al menos una categoria de horario.", Alert.AlertType.WARNING);
            return;
        }

        Object userData = fila.getUserData();
        if (userData instanceof CategoriaConAlimentos) {
            CategoriaConAlimentos categoria = (CategoriaConAlimentos) userData;
            categoriasEliminar.add(categoria.getIdCategoria());
            cbCategoriaEdicion.getItems().remove(categoria);
        }
        vbCategorias.getChildren().remove(fila);
        actualizarTotalVisual();
    }

    private RQCrearDieta construirDietaNueva() {
        String nombreDieta = obtenerNombreDieta();
        if (nombreDieta == null) {
            return null;
        }

        List<String> categorias = obtenerCategoriasFormulario(false);
        if (!validarCategorias(categorias)) {
            return null;
        }

        int idMedico = Sesion.getIdMedicoSesion();
        if (idMedico <= 0) {
            Utilidades.mostrarAlertaSimple("Sesion", "No se pudo identificar al medico de la sesion.", Alert.AlertType.ERROR);
            return null;
        }

        RQCrearDieta dieta = new RQCrearDieta();
        dieta.setNombreDieta(nombreDieta);
        dieta.setObservaciones(taObservaciones.getText() != null ? taObservaciones.getText().trim() : "");
        dieta.setIdMedico(idMedico);
        dieta.setCategorias(categorias);
        return dieta;
    }

    private RQModificarDieta construirDietaEdicion() {
        String nombreDieta = obtenerNombreDieta();
        if (nombreDieta == null) {
            return null;
        }

        List<String> todasCategorias = obtenerCategoriasFormulario(true);
        if (!validarCategorias(todasCategorias)) {
            return null;
        }

        RQModificarDieta dieta = new RQModificarDieta();
        dieta.setIdDieta(dietaEdicion.getIdDieta());
        dieta.setNombreDieta(nombreDieta);
        dieta.setObservaciones(taObservaciones.getText() != null ? taObservaciones.getText().trim() : "");
        dieta.setCategoriasAgregar(obtenerCategoriasNuevas());
        dieta.setCategoriasEliminar(categoriasEliminar);
        dieta.setAlimentosAgregar(alimentosAgregar);
        dieta.setAlimentosEliminar(alimentosEliminar);
        return dieta;
    }

    private String obtenerNombreDieta() {
        String nombreDieta = tfNombreDieta.getText() != null ? tfNombreDieta.getText().trim() : "";
        if (nombreDieta.isEmpty()) {
            Utilidades.mostrarAlertaSimple("Validacion", "El nombre de la dieta es obligatorio.", Alert.AlertType.WARNING);
            return null;
        }
        return nombreDieta;
    }

    private List<String> obtenerCategoriasFormulario(boolean incluirExistentes) {
        List<String> categorias = new ArrayList<>();
        for (javafx.scene.Node nodo : vbCategorias.getChildren()) {
            HBox fila = (HBox) nodo;
            if (!incluirExistentes && fila.getUserData() != null) {
                continue;
            }
            TextField tfCategoria = (TextField) fila.getChildren().get(0);
            categorias.add(tfCategoria.getText() != null ? tfCategoria.getText().trim() : "");
        }
        return categorias;
    }

    private List<String> obtenerCategoriasNuevas() {
        List<String> categorias = new ArrayList<>();
        for (javafx.scene.Node nodo : vbCategorias.getChildren()) {
            HBox fila = (HBox) nodo;
            if (fila.getUserData() == null) {
                TextField tfCategoria = (TextField) fila.getChildren().get(0);
                String categoria = tfCategoria.getText() != null ? tfCategoria.getText().trim() : "";
                if (!categoria.isEmpty()) {
                    categorias.add(categoria);
                }
            }
        }
        return categorias;
    }

    private boolean validarCategorias(List<String> categorias) {
        if (categorias.isEmpty()) {
            Utilidades.mostrarAlertaSimple("Validacion", "Agrega al menos una categoria de horario.", Alert.AlertType.WARNING);
            return false;
        }

        for (String categoria : categorias) {
            if (categoria == null || categoria.trim().isEmpty()) {
                Utilidades.mostrarAlertaSimple("Validacion", "El nombre de cada categoria de horario es obligatorio.", Alert.AlertType.WARNING);
                return false;
            }
        }

        Set<String> nombres = new LinkedHashSet<>();
        for (String categoria : categorias) {
            if (!nombres.add(categoria.toLowerCase())) {
                Utilidades.mostrarAlertaSimple("Validacion", "No se pueden registrar categorias de horario repetidas.", Alert.AlertType.WARNING);
                return false;
            }
        }
        return true;
    }

    private void cargarAlimentosCategoria(CategoriaConAlimentos categoria) {
        alimentosCategoria.clear();
        if (categoria != null) {
            if (categoria.getAlimentos() == null) {
                categoria.setAlimentos(new ArrayList<>());
            }
            alimentosCategoria.addAll(categoria.getAlimentos());
        }
    }

    private boolean contieneAlimento(CategoriaConAlimentos categoria, int idAlimento) {
        if (categoria.getAlimentos() == null) {
            categoria.setAlimentos(new ArrayList<>());
        }
        for (AlimentoEnDieta alimento : categoria.getAlimentos()) {
            if (alimento.getIdAlimento() == idAlimento) {
                return true;
            }
        }
        return false;
    }

    private void quitarAlimentoPendienteAgregar(int idCategoria, int idAlimento) {
        for (int i = alimentosAgregar.size() - 1; i >= 0; i--) {
            RQAlimentoEnCategoria item = alimentosAgregar.get(i);
            if (item.getIdCategoria() == idCategoria && item.getIdAlimento() == idAlimento) {
                alimentosAgregar.remove(i);
            }
        }
    }

    private void quitarAlimentoPendienteEliminacion(int idCategoria, int idAlimento) {
        for (int i = alimentosEliminar.size() - 1; i >= 0; i--) {
            RQAlimentoEnCategoria item = alimentosEliminar.get(i);
            if (item.getIdCategoria() == idCategoria && item.getIdAlimento() == idAlimento) {
                alimentosEliminar.remove(i);
            }
        }
    }

    private void actualizarTotalVisual() {
        double total = 0.0;
        if (dietaEdicion != null && dietaEdicion.getCategorias() != null) {
            for (CategoriaConAlimentos categoria : dietaEdicion.getCategorias()) {
                if (categoriasEliminar.contains(categoria.getIdCategoria()) || categoria.getAlimentos() == null) {
                    continue;
                }
                for (AlimentoEnDieta alimento : categoria.getAlimentos()) {
                    total += alimento.getCaloriasTotales();
                }
            }
        }
        lbTotalCalorias.setText(String.format("%.2f kcal", total));
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
