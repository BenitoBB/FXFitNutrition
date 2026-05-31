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
}
