package fxmlfitnutrition;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utilidad.Sesion;

/**
 * FXML Controller class - MenÃƒÂº Principal y Control de Roles (CARD FX-03)
 */
public class FXMLMenuPrincipalController implements Initializable {

    @FXML
    private Button btnPacientes;
    @FXML
    private Button btnCitas;
    @FXML
    private Button btnConsultas;
    @FXML
    private Button btnDietas;
    @FXML
    private Button btnAlimentos;
    @FXML
    private Button btnMedicos;
    @FXML
    private Label lbNombreUsuario;
    @FXML
    private Label lbRol;
    @FXML
    private Button btnCerrarSesion;
    @FXML
    private AnchorPane apCentral;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarMenu();
    }
    private void marcarMenuSeleccionado(Button botonSeleccionado) {
        Button[] botonesMenu = {btnPacientes, btnCitas, btnConsultas, btnDietas, btnAlimentos, btnMedicos};
        for (Button boton : botonesMenu) {
            if (boton != null) {
                boton.getStyleClass().remove("menu-button-active");
            }
        }
        if (botonSeleccionado != null && !botonSeleccionado.getStyleClass().contains("menu-button-active")) {
            botonSeleccionado.getStyleClass().add("menu-button-active");
        }
    }



    private void configurarMenu() {
        if (Sesion.getMedicoSesion() != null) {
            lbNombreUsuario.setText(Sesion.getMedicoSesion().getNombreCompleto());

            if (Sesion.isEsAdministrador()) {
                lbRol.setText("Administrador");
                btnMedicos.setVisible(true);
                btnMedicos.setManaged(true);
            } else {
                lbRol.setText("MÃƒÂ©dico");
                btnMedicos.setVisible(false);
                btnMedicos.setManaged(false); // Elimina el espacio del botÃƒÂ³n
            }
        }
    }

    @FXML
    private void clicPacientes(ActionEvent event) {
        try {
            marcarMenuSeleccionado(btnPacientes);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxmlfitnutrition/vistas/paciente/FXMLPacientes.fxml"
                    )
            );

            Parent vista = loader.load();

            apCentral.getChildren().clear();
            apCentral.getChildren().add(vista);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clicCitas(ActionEvent event) {
        try {
            marcarMenuSeleccionado(btnCitas);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlfitnutrition/vistas/cita/FXMLCitas.fxml"));
            Parent vista = loader.load();
            apCentral.getChildren().clear();
            apCentral.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clicConsultas(ActionEvent event) {
        try {
            marcarMenuSeleccionado(btnConsultas);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlfitnutrition/vistas/consulta/FXMLConsultas.fxml"));
            Parent vista = loader.load();
            apCentral.getChildren().clear();
            apCentral.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clicDietas(ActionEvent event) {
        try {
            marcarMenuSeleccionado(btnDietas);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlfitnutrition/vistas/dieta/FXMLDietas.fxml"));
            Parent vista = loader.load();
            apCentral.getChildren().clear();
            apCentral.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clicAlimentos(ActionEvent event) {
        try {
            marcarMenuSeleccionado(btnAlimentos);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlfitnutrition/vistas/alimento/FXMLAlimentos.fxml"));
            Parent vista = loader.load();
            apCentral.getChildren().clear();
            apCentral.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clicMedicos(ActionEvent event) {
        try {
            marcarMenuSeleccionado(btnMedicos);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmlfitnutrition/vistas/medico/FXMLMedicos.fxml"));
            Parent vista = loader.load();
            apCentral.getChildren().clear();
            apCentral.getChildren().add(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clicCerrarSesion(ActionEvent event) {
        Sesion.cerrarSesion();
        try {
            Stage stageActual = (Stage) btnCerrarSesion.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("FXMLInicioSesion.fxml"));
            Scene escena = new Scene(root);
            Stage nuevoStage = new Stage();
            nuevoStage.setScene(escena);
            nuevoStage.setTitle("Inicio de SesiÃƒÂ³n");
            nuevoStage.show();
            stageActual.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

