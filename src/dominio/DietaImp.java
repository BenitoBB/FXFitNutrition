package dominio;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import conexion.ConexionAPI;
import dto.RQCrearDieta;
import dto.RQModificarDieta;
import dto.RespuestaSimple;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import pojo.Dieta;
import pojo.DietaDetalle;
import pojo.RespuestaHTTP;
import utilidad.Constantes;

public class DietaImp {

    public static RespuestaSimple crearDieta(RQCrearDieta dieta) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(dieta);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                    Constantes.URL_WS + "dieta/crear",
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

    public static DietaDetalle obtenerDetalle(int idDieta) {
        try {
            RespuestaHTTP http = ConexionAPI.peticionGET(Constantes.URL_WS + "dieta/" + idDieta);
            if (http.getCodigo() == 200 && http.getContenido() != null && http.getContenido().trim().startsWith("{")) {
                Gson gson = new Gson();
                DietaDetalle detalle = gson.fromJson(http.getContenido(), DietaDetalle.class);
                return detalle != null && detalle.getIdDieta() > 0 ? detalle : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static RespuestaSimple modificarDieta(RQModificarDieta dieta) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(dieta);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                    Constantes.URL_WS + "dieta/modificar",
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

    public static RespuestaSimple eliminarDieta(int idDieta) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            RespuestaHTTP http = ConexionAPI.peticionSinBody(
                    Constantes.URL_WS + "dieta/eliminar/" + idDieta,
                    Constantes.PETICION_DELETE
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
