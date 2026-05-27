
package utilidad;


public class Constantes {
    public static final String URL_WS = "http://localhost:8080/PacketWorldAPI/webresources/";
    public static final int ERROR_MALFORMED_URL = 1001;
    public static final int ERROR_PETICION = 1002;

    public static final String MSJ_DEFAULT = "Lo sentimos hay problemas para obtener la información en este momento, por favor intentelo más tarde.";
    public static final String MSJ_ERROR_URL = "Lo sentimos su solicitud no se puede realizar, por favor intentelo más tarde.";
    public static final String MSJ_ERROR_PETICION = "Lo sentimos tenemos problemas de conexión en este momento, por favor intentelo más tarde.";
   
    //Llaves Hash
    public static final String KEY_ERROR = "error";
    public static final String KEY_MENSAJE = "mensaje";
    public static final String KEY_LISTA = "lista_valores";
    public static final String KEY_OBJETO = "objeto";
    
    public static final String PETICION_GET = "GET";
    public static final String PETICION_POST = "POST";
    public static final String PETICION_PUT = "PUT";
    public static final String PETICION_DELETE = "DELETE";
    
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
}
