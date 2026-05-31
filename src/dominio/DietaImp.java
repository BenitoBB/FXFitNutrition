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

}
