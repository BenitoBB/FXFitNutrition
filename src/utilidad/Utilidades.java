/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidad;

/**
 *
 * @author julia
 */
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputControl;
import javafx.stage.StageStyle;

public class Utilidades {
    public static String streamToString(InputStream input) throws IOException{
    BufferedReader in = new BufferedReader(new InputStreamReader(input));
    String inputLine;
    StringBuffer respuestaEntrada = new StringBuffer();
    while( (inputLine = in.readLine()) != null){
     respuestaEntrada.append(inputLine);
    }
    in.close();
    return respuestaEntrada.toString();
   }
    
   public static void mostrarAlertaSimple(String titulo, String mensaje, Alert.AlertType tipoAlerta) {
        Alert alerta = new Alert(tipoAlerta);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null); //quita el encabezado
        alerta.setContentText(mensaje);
        alerta.initStyle(StageStyle.UTILITY); //estilo simple sin icono de app

        alerta.showAndWait(); // Muestra la alerta y espera a que el usuario la cierre
    }
   
   public static boolean mostrarAlertaConfirmacion(String titulo, String contenido) {
       Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
       alerta.setTitle(titulo);
       alerta.setHeaderText(null); 
       alerta.setContentText(contenido);
       alerta.initStyle(StageStyle.UTILITY); 

       Optional<ButtonType> btnSeleccion =  alerta.showAndWait();
       
       return (btnSeleccion.get() == ButtonType.OK);
       
   }
   
    public static String capitalizarTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return texto;
        }
        
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder textoFormateado = new StringBuilder();
        
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                textoFormateado.append(palabra.substring(0, 1).toUpperCase())
                               .append(palabra.substring(1).toLowerCase())
                               .append(" ");
            }
        }
        return textoFormateado.toString().trim();
    }

    public static void permitirSoloLetras(TextInputControl campo) {
        aplicarFiltroTexto(campo, "[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ\\s]");
    }

    public static void permitirSoloNumerosDecimales(TextInputControl campo) {
        if (campo == null) {
            return;
        }
        campo.textProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo == null) {
                return;
            }
            String filtrado = nuevo.replaceAll("[^0-9.]", "");
            int primerPunto = filtrado.indexOf('.');
            if (primerPunto >= 0) {
                filtrado = filtrado.substring(0, primerPunto + 1)
                        + filtrado.substring(primerPunto + 1).replace(".", "");
            }
            if (!nuevo.equals(filtrado)) {
                campo.setText(filtrado);
            }
        });
    }

    public static void permitirLetrasNumeros(TextInputControl campo) {
        aplicarFiltroTexto(campo, "[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9\\s.,;:()\\-]");
    }

    public static void permitirBusquedaNombreCorreo(TextInputControl campo) {
        aplicarFiltroTexto(campo, "[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9\\s@._\\-]");
    }

    private static void aplicarFiltroTexto(TextInputControl campo, String patronInvalido) {
        if (campo == null) {
            return;
        }
        campo.textProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo == null) {
                return;
            }
            String filtrado = nuevo.replaceAll(patronInvalido, "");
            if (!nuevo.equals(filtrado)) {
                campo.setText(filtrado);
            }
        });
    }
}
