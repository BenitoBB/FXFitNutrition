package dominio;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import conexion.ConexionAPI;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import pojo.Medico;
import pojo.RespuestaHTTP;
import dto.Respuesta;
import dto.RQBajaMedico;
import utilidad.Constantes;

public class MedicoImp {
    
    public static List<Medico> buscarMedicos(String criterio, boolean esAdministrador) {
        List<Medico> medicos = new ArrayList<>();
        try {
            int esAdmin = esAdministrador ? 1 : 0;
            String url = Constantes.URL_WS + "medico/buscar?criterio=" + (criterio != null ? criterio : "") + "&esAdministrador=" + esAdmin;
            
            RespuestaHTTP http = ConexionAPI.peticionGET(url);
            
            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Medico>>(){}.getType();
                medicos = gson.fromJson(http.getContenido(), listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medicos;
    }

    public static Respuesta registrarMedico(Medico medico) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(medico);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "medico/registrar", 
                Constantes.PETICION_POST, 
                jsonBody, 
                Constantes.APPLICATION_JSON
            );
            if (http.getCodigo() == 200 || http.getCodigo() == 201) {
                respuesta = gson.fromJson(http.getContenido(), Respuesta.class);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        return respuesta;
    }

    public static Respuesta editarMedico(Medico medico) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(medico);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "medico/editar", 
                Constantes.PETICION_PUT, 
                jsonBody, 
                Constantes.APPLICATION_JSON
            );
            if (http.getCodigo() == 200) {
                respuesta = gson.fromJson(http.getContenido(), Respuesta.class);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        return respuesta;
    }
    public static Respuesta subirFotografia(int idMedico, byte[] fotografia) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        try {
            RespuestaHTTP http = ConexionAPI.peticionPUTImagen(
                Constantes.URL_WS + "medico/subir-fotografia/" + idMedico,
                fotografia
            );
            Gson gson = new Gson();
            if (http.getCodigo() == 200) {
                respuesta = gson.fromJson(http.getContenido(), Respuesta.class);
            } else {
                respuesta.setMensaje(Constantes.MSJ_ERROR_PETICION);
            }
        } catch (Exception e) {
            respuesta.setMensaje(Constantes.MSJ_DEFAULT);
            e.printStackTrace();
        }
        return respuesta;
    }

    
    public static Respuesta darDeBajaMedico(RQBajaMedico rq) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(rq);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "medico/baja", 
                Constantes.PETICION_POST, 
                jsonBody, 
                Constantes.APPLICATION_JSON
            );
            if (http.getCodigo() == 200) {
                respuesta = gson.fromJson(http.getContenido(), Respuesta.class);
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
