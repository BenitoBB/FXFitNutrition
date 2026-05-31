/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package fxmlfitnutrition;

import dominio.AutenticacionImp;
import dto.RSAutenticacionMedico;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utilidad.Sesion;
import utilidad.Utilidades;
import utilidad.Validaciones;

/**
 * FXML Controller class – Pantalla de Inicio de Sesión (CARD FX-02)
 *
 * @author julia
 */
public class FXMLInicioSesionController implements Initializable {

    @FXML private TextField tfNoPersonal;
    @FXML private PasswordField pfContrasena;
    @FXML private Button btnIniciarSesion;
    @FXML private Label lbMensajeError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Limpiar mensaje de error al inicio
        lbMensajeError.setVisible(false);
    }

    @FXML
    private void clicIniciarSesion(ActionEvent event) {
        lbMensajeError.setVisible(false);

        String noPersonal = tfNoPersonal.getText();
        String contrasena = pfContrasena.getText();

        // Validación 1: Campos vacíos
        if (Validaciones.esVacio(noPersonal) || Validaciones.esVacio(contrasena)) {
            mostrarError("El número de personal y la contraseña son obligatorios.");
            return;
        }

        // Validación 2: Formato del número de personal (máx 9, alfanumérico)
        if (!Validaciones.esAlfanumericoConLongitudMaxima(noPersonal, 9)) {
            mostrarError("Número de personal inválido (máximo 9 caracteres alfanuméricos).");
            return;
        }

        // Consumir API
        RSAutenticacionMedico respuesta = AutenticacionImp.loginMedico(
            noPersonal.trim(), contrasena
        );

        if (!respuesta.isError() && respuesta.getMedico() != null) {
            // Login exitoso: guardar sesión
            Sesion.setMedicoSesion(respuesta.getMedico());
            Sesion.setEsAdministrador(respuesta.isEsAdministrador() || respuesta.getMedico().esAdmin());

            Utilidades.mostrarAlertaSimple(
                "Bienvenido",
                "Hola, " + respuesta.getMedico().getNombreCompleto()
                    + (respuesta.isEsAdministrador() ? " (Administrador)" : " (Médico)"),
                Alert.AlertType.INFORMATION
            );

            // Abrir Menu Principal
            try {
                Stage stageActual = (Stage) btnIniciarSesion.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("FXMLMenuPrincipal.fxml"));
                Scene escena = new Scene(root);
                Stage nuevoStage = new Stage();
                nuevoStage.setScene(escena);
                nuevoStage.setTitle("Menú Principal - FitNutrition");
                nuevoStage.show();
                stageActual.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Login fallido: mostrar el error de la API
            mostrarError(respuesta.getMensaje() != null ? respuesta.getMensaje() : "No fue posible iniciar sesion.");
        }
    }

    private void mostrarError(String mensaje) {
        lbMensajeError.setVisible(false);
        Utilidades.mostrarAlertaSimple("Inicio de sesion", mensaje, Alert.AlertType.ERROR);
    }
}
