package dominio;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import conexion.ConexionAPI;
import dto.RespuestaSimple;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import pojo.Cita;
import pojo.CitaDetalle;
import pojo.RespuestaHTTP;
import utilidad.Constantes;

public class CitaImp {
    
    public static RespuestaSimple crearCita(Cita cita) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);
        
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(cita);
            
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "cita/crear", 
                Constantes.PETICION_POST, 
                jsonBody, 
                Constantes.APPLICATION_JSON
            );
            
            if (http.getCodigo() == 200) {
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

    public static RespuestaSimple modificarCita(Cita cita) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(cita);

            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "cita/modificar",
                Constantes.PETICION_PUT,
                jsonBody,
                Constantes.APPLICATION_JSON
            );

            if (http.getCodigo() == 200) {
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

    public static RespuestaSimple cancelarCita(Cita cita) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(cita);

            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "cita/cancelar",
                Constantes.PETICION_PUT,
                jsonBody,
                Constantes.APPLICATION_JSON
            );

            if (http.getCodigo() == 200) {
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

    public static RespuestaSimple reagendarCita(Cita cita) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(cita);

            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "cita/reagendar",
                Constantes.PETICION_PUT,
                jsonBody,
                Constantes.APPLICATION_JSON
            );

            if (http.getCodigo() == 200) {
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

    public static List<CitaDetalle> buscarCitas(String criterio, int idMedico, boolean esAdministrador, String estatus) {
        try {
            if (criterio == null) {
                criterio = "";
            }
            if (estatus == null || "Todos".equals(estatus)) {
                estatus = "";
            }

            String url = Constantes.URL_WS
                    + "cita/buscar?criterio=" + URLEncoder.encode(criterio, "UTF-8")
                    + "&estatus=" + URLEncoder.encode(estatus, "UTF-8");

            if (!esAdministrador) {
                url += "&idMedico=" + idMedico;
            }

            RespuestaHTTP http = ConexionAPI.peticionGET(url);
            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<CitaDetalle>>(){}.getType();
                return gson.fromJson(http.getContenido(), listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Cita buscarCitaPorId(int idCita) {
        try {
            RespuestaHTTP http = ConexionAPI.peticionGET(Constantes.URL_WS + "cita/" + idCita);
            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                return gson.fromJson(http.getContenido(), Cita.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
