/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import pojo.RespuestaHTTP;
import utilidad.Constantes;
import utilidad.Utilidades;

/**
 *
 * @author julia
 */
public class ConexionAPI {
    //TODO
    public static RespuestaHTTP peticionGET(String URL) {
        RespuestaHTTP respuesta = new RespuestaHTTP();
        try {
            URL urlWS = new URL(URL);
            HttpURLConnection conexionHTTP = (HttpURLConnection) urlWS.openConnection();
            int codigo = conexionHTTP.getResponseCode();
            if (codigo == HttpURLConnection.HTTP_OK) {
                respuesta.setContenido(Utilidades.streamToString(conexionHTTP.getInputStream()));
            }
            respuesta.setCodigo(codigo);
        } catch (MalformedURLException e) {
            respuesta.setCodigo(Constantes.ERROR_MALFORMED_URL);
            respuesta.setContenido(e.getMessage());
        } catch (IOException e) {
            respuesta.setCodigo(Constantes.ERROR_PETICION);
            respuesta.setContenido(e.getMessage());  
        }
        return respuesta;
    }
    
    public static RespuestaHTTP peticionBody(String URL, String metodoHTTP, String parametros, String contentType){
        RespuestaHTTP respuesta = new RespuestaHTTP();
        try {
            URL urlWS = new URL(URL);
            HttpURLConnection conexionHTTP = (HttpURLConnection) urlWS.openConnection();
            conexionHTTP.setRequestMethod(metodoHTTP);
            conexionHTTP.setRequestProperty("Content-Type", contentType);
            conexionHTTP.setDoOutput(true);
            OutputStream os = conexionHTTP.getOutputStream();
            os.write(parametros.getBytes());
            os.flush();
            os.close();
            int codigo = conexionHTTP.getResponseCode(); 
            if (codigo == HttpURLConnection.HTTP_OK) {
                respuesta.setContenido(Utilidades.streamToString(conexionHTTP.getInputStream()));
            }
            respuesta.setCodigo(codigo);
        } catch (MalformedURLException e) {
            respuesta.setCodigo(Constantes.ERROR_MALFORMED_URL);
            respuesta.setContenido(e.getMessage());
        } catch (IOException e) {
            respuesta.setCodigo(Constantes.ERROR_PETICION);
            respuesta.setContenido(e.getMessage());  
        }
        return respuesta;
    }
    
    public static RespuestaHTTP peticionSinBody(String URL, String metodoHTTP) {
        RespuestaHTTP respuesta = new RespuestaHTTP();
        try {
            URL urlWS = new URL(URL);
            HttpURLConnection conexionHTTP = (HttpURLConnection) urlWS.openConnection();
            conexionHTTP.setRequestMethod(metodoHTTP);
            int codigo = conexionHTTP.getResponseCode();
            if (codigo == HttpURLConnection.HTTP_OK) {
                respuesta.setContenido(Utilidades.streamToString(conexionHTTP.getInputStream()));
            }
            respuesta.setCodigo(codigo);
        } catch (MalformedURLException e) {
            respuesta.setCodigo(Constantes.ERROR_MALFORMED_URL);
            respuesta.setContenido(e.getMessage());
        } catch (IOException e) {
            respuesta.setCodigo(Constantes.ERROR_PETICION);
            respuesta.setContenido(e.getMessage());  
        }
        return respuesta;
    }
    
    public static RespuestaHTTP peticionPUTImagen(String URL, byte[] datosImagen) {
        RespuestaHTTP respuesta = new RespuestaHTTP();
        try {
            URL urlWS = new URL(URL);
            HttpURLConnection conexionHTTP = (HttpURLConnection) urlWS.openConnection();
            conexionHTTP.setRequestMethod("PUT");
            
            conexionHTTP.setRequestProperty("Content-Type", "application/octet-stream");
            conexionHTTP.setDoOutput(true);
            
            OutputStream os = conexionHTTP.getOutputStream();
            os.write(datosImagen);
            os.flush();
            os.close();
            
            int codigo = conexionHTTP.getResponseCode();
            if (codigo == HttpURLConnection.HTTP_OK) {
                respuesta.setContenido(Utilidades.streamToString(conexionHTTP.getInputStream()));
            }
            respuesta.setCodigo(codigo);
            
        } catch (MalformedURLException e) {
            respuesta.setCodigo(Constantes.ERROR_MALFORMED_URL);
            respuesta.setContenido(e.getMessage());
        } catch (IOException e) {
            respuesta.setCodigo(Constantes.ERROR_PETICION);
            respuesta.setContenido(e.getMessage());
        }
        return respuesta;
    }
}