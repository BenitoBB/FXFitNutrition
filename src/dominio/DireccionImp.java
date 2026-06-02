package dominio;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import conexion.ConexionAPI;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import pojo.Direccion;
import pojo.RespuestaHTTP;
import dto.Respuesta;
import utilidad.Constantes;

/**
 * Consume el endpoint GET /api/direccion/obtener-direccion-codigo-postal/{cp}
 * y devuelve la lista de colonias asociadas a ese código postal.
 */
public class DireccionImp {

    public static List<Direccion> obtenerDireccionPorCodigoPostal(String codigoPostal) {
        List<Direccion> colonias = new ArrayList<>();

        try {
            RespuestaHTTP http = ConexionAPI.peticionGET(
                Constantes.URL_WS + "direccion/obtener-direccion-codigo-postal/" + codigoPostal
            );

            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                Type tipoLista = new TypeToken<ArrayList<Direccion>>(){}.getType();
                colonias = gson.fromJson(http.getContenido(), tipoLista);
                if (colonias == null) {
                    colonias = new ArrayList<>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return colonias;
    }

    public static Respuesta registrarDireccion(Direccion direccion) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(direccion);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "direccion/crear-direccion", 
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

    public static Respuesta editar(Direccion direccion) {
        Respuesta respuesta = new Respuesta();
        respuesta.setError(true);
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(direccion);
            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "direccion/editar", 
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
}
