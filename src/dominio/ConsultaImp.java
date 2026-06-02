package dominio;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import conexion.ConexionAPI;
import dto.RespuestaSimple;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import pojo.Consulta;
import pojo.RespuestaHTTP;
import utilidad.Constantes;

public class ConsultaImp {

    public static RespuestaSimple registrarConsulta(Consulta consulta) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(consulta);

            RespuestaHTTP http = ConexionAPI.peticionBody(
                    Constantes.URL_WS + "consulta/registrar",
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

    public static RespuestaSimple modificarConsulta(Consulta consulta) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(consulta);

            RespuestaHTTP http = ConexionAPI.peticionBody(
                    Constantes.URL_WS + "consulta/modificar",
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

    public static List<Consulta> buscarConsultasPaciente(int idPaciente) {
        List<Consulta> consultas = leerConsultas(
                Constantes.URL_WS + "consulta/buscar?idPaciente=" + idPaciente
        );

        if (consultas == null || consultas.isEmpty()) {
            consultas = leerConsultas(Constantes.URL_WS + "consulta/buscar/" + idPaciente);
        }

        return consultas != null ? consultas : new ArrayList<>();
    }

    public static List<Consulta> buscarConsultas(String criterio, int idMedico, boolean esAdministrador) {
        try {
            if (criterio == null) {
                criterio = "";
            }

            String url = Constantes.URL_WS
                    + "consulta/buscar?criterio=" + URLEncoder.encode(criterio, "UTF-8");

            if (!esAdministrador) {
                url += "&idMedico=" + idMedico;
            }

            List<Consulta> consultas = leerConsultas(url);
            return consultas != null ? consultas : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private static List<Consulta> leerConsultas(String url) {
        try {
            RespuestaHTTP http = ConexionAPI.peticionGET(url);

            if (http.getCodigo() == 200) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Consulta>>(){}.getType();
                return gson.fromJson(http.getContenido(), listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static RespuestaSimple cancelarConsulta(int idConsulta) {
        RespuestaSimple respuesta = new RespuestaSimple();
        respuesta.setError(true);

        try {
            RespuestaHTTP http = ConexionAPI.peticionSinBody(
                    Constantes.URL_WS + "consulta/cancelar/" + idConsulta,
                    Constantes.PETICION_PUT
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
