package dominio;

import com.google.gson.Gson;
import conexion.ConexionAPI;
import dto.RSAutenticacionMedico;
import java.util.HashMap;
import java.util.Map;
import pojo.RespuestaHTTP;
import utilidad.Constantes;

/**
 * Lógica cliente para autenticación.
 * Consume POST /api/medico/login y parsea la respuesta.
 */
public class AutenticacionImp {

    public static RSAutenticacionMedico loginMedico(String noPersonal, String contrasena) {
        RSAutenticacionMedico respuesta = new RSAutenticacionMedico();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            Map<String, String> credenciales = new HashMap<>();
            credenciales.put("noPersonal", noPersonal);
            credenciales.put("contrasena", contrasena);
            String jsonBody = gson.toJson(credenciales);

            RespuestaHTTP http = ConexionAPI.peticionBody(
                Constantes.URL_WS + "medico/login",
                Constantes.PETICION_POST,
                jsonBody,
                Constantes.APPLICATION_JSON
            );

            if (http.getCodigo() == 200 && http.getContenido() != null) {
                respuesta = gson.fromJson(http.getContenido(), RSAutenticacionMedico.class);
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
