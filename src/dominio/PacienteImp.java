package dominio;

import com.google.gson.Gson;
import conexion.ConexionAPI;
import dto.RSPacientes;
import dto.RSRegistroPaciente;
import dto.RespuestaSimple;
import pojo.Paciente;
import pojo.RespuestaHTTP;
import utilidad.Constantes;
import java.util.ArrayList;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import com.google.gson.reflect.TypeToken;

public class PacienteImp {
    
    public static RSRegistroPaciente registrarPaciente(Paciente paciente) {
        RSRegistroPaciente respuesta = new RSRegistroPaciente();
        respuesta.setError(true);
        
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(paciente);
            
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "paciente/registrar", 
                Constantes.PETICION_POST, 
                jsonBody, 
                Constantes.APPLICATION_JSON
            );
            
            if (http.getCodigo() == 200 || http.getCodigo() == 201) {
                respuesta = gson.fromJson(http.getContenido(), RSRegistroPaciente.class);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        
        return respuesta;
    }
    
    public static RSRegistroPaciente editarPaciente(Paciente paciente) {
        RSRegistroPaciente respuesta = new RSRegistroPaciente();
        respuesta.setError(true);
        
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(paciente);
            
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "paciente/editar", 
                Constantes.PETICION_PUT, 
                jsonBody, 
                Constantes.APPLICATION_JSON
            );
            
            if (http.getCodigo() == 200) {
                respuesta = gson.fromJson(http.getContenido(), RSRegistroPaciente.class);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        
        return respuesta;
    }
    
    public static RSPacientes buscarPacientes(String criterio, int idMedico, boolean esAdministrador) {
        RSPacientes respuesta = new RSPacientes();
        respuesta.setError(true);
        
        try {
            // Si es nulo lo pasamos como cadena vacía
            if (criterio == null) {
                criterio = "";
            }
            
            // Construimos la URL
            // Si es administrador, no enviamos el idMedico o lo mandamos en 0 (depende de la API, asumimos que si no se manda, o si esAdmin, el backend lo ignora. Aquí enviamos todo explícito)
            String url = Constantes.URL_WS + "paciente/buscar?criterio=" + URLEncoder.encode(criterio, "UTF-8");
            if (!esAdministrador) {
                url += "&idMedico=" + idMedico;
            }
            
            RespuestaHTTP http = ConexionAPI.peticionGET(url);
            
            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Paciente>>(){}.getType();
                ArrayList<Paciente> pacientes = gson.fromJson(http.getContenido(), listType);
                respuesta.setError(false);
                respuesta.setPacientes(pacientes);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        
        return respuesta;
    }
    
    public static RespuestaSimple darDeBajaPaciente(int idPaciente) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);
        
        try {
            RespuestaHTTP http = ConexionAPI.peticionSinBody(
                Constantes.URL_WS + "paciente/baja/" + idPaciente, 
                "DELETE"
            );
            
            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                respuesta = gson.fromJson(http.getContenido(), RespuestaSimple.class);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        
        return respuesta;
    }
}
