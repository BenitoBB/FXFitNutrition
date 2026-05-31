package fxmlfitnutrition.vistas.alimento;

import dominio.AlimentoImp;
import dto.RespuestaSimple;
import java.net.URL;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pojo.Alimento;
import utilidad.NotificacionOperacion;
import utilidad.Utilidades;

public class FXMLFormularioAlimentoController implements Initializable {

    @FXML private Label lbTitulo;
    @FXML private TextField tfNombreAlimento;
    @FXML private ComboBox<String> cbPorcion;
    @FXML private Spinner<Double> spCaloriasPorcion;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private NotificacionOperacion observador;
    private Alimento alimentoEdicion;
    private boolean modoEdicion;

    public void inicializarValores(NotificacionOperacion observador) {
        this.observador = observador;
    }

    public void inicializarParaEdicion(Alimento alimento) {
        if (alimento == null) {
            return;
        }
        this.alimentoEdicion = alimento;
        this.modoEdicion = true;

        lbTitulo.setText("Editar Alimento");
        btnGuardar.setText("Guardar");
        tfNombreAlimento.setText(alimento.getNombreAlimento());
        cbPorcion.setValue(alimento.getPorcion());
        spCaloriasPorcion.getValueFactory().setValue(alimento.getCaloriasPorcion());
        spCaloriasPorcion.getEditor().setText(String.format("%.2f", alimento.getCaloriasPorcion()));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbPorcion.setItems(FXCollections.observableArrayList("Pieza", "Gramos", "Porciones", "Mililitros"));
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 99999.0, 0.01, 1.0);
        valueFactory.setConverter(new StringConverter<Double>() {
            @Override
            public String toString(Double valor) {
                return valor != null && valor > 0 ? String.format("%.2f", valor) : "";
            }

            @Override
            public Double fromString(String texto) {
                if (texto == null || texto.trim().isEmpty()) {
                    return valueFactory.getValue() != null ? valueFactory.getValue() : 0.01;
                }
                try {
                    return Double.parseDouble(texto.trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    return valueFactory.getValue() != null ? valueFactory.getValue() : 0.01;
                }
            }
        });
        spCaloriasPorcion.setValueFactory(valueFactory);
        spCaloriasPorcion.getEditor().setText("");
        Utilidades.permitirSoloLetras(tfNombreAlimento);
        Utilidades.permitirSoloNumerosDecimales(spCaloriasPorcion.getEditor());
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        Alimento alimento = construirAlimento();
        if (alimento == null) {
            return;
        }

        RespuestaSimple respuesta = modoEdicion
                ? AlimentoImp.editarAlimento(alimento)
                : AlimentoImp.registrarAlimento(alimento);
        if (!respuesta.isError()) {
            Utilidades.mostrarAlertaSimple(
                    modoEdicion ? "Alimento Modificado" : "Alimento Registrado",
                    modoEdicion ? "El alimento se modifico correctamente." : "El alimento se registro correctamente.",
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

    private Alimento construirAlimento() {
        String nombre = tfNombreAlimento.getText() != null ? tfNombreAlimento.getText().trim() : "";
        if (nombre.isEmpty()) {
            Utilidades.mostrarAlertaSimple("Validacion", "El nombre del alimento es obligatorio.", Alert.AlertType.WARNING);
            return null;
        }

        String porcion = cbPorcion.getValue();
        if (porcion == null || porcion.trim().isEmpty()) {
            Utilidades.mostrarAlertaSimple("Validacion", "Selecciona una porcion.", Alert.AlertType.WARNING);
            return null;
        }

        Double calorias = leerCalorias();
        if (calorias == null || calorias <= 0.0) {
            Utilidades.mostrarAlertaSimple("Validacion", "Las calorias deben ser un numero decimal mayor a 0.", Alert.AlertType.WARNING);
            return null;
        }

        Alimento alimento = new Alimento();
        if (modoEdicion) {
            alimento.setIdAlimento(alimentoEdicion.getIdAlimento());
        }
        alimento.setNombreAlimento(nombre);
        alimento.setPorcion(porcion);
        alimento.setCaloriasPorcion(calorias);
        return alimento;
    }

    private Double leerCalorias() {
        String texto = spCaloriasPorcion.getEditor().getText();
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
