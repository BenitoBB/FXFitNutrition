package utilidad;

/**
 * Validaciones reutilizables del cliente JavaFX.
 */
public class Validaciones {

    public static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static boolean esAlfanumericoConLongitudMaxima(String texto, int maxLength) {
        if (texto == null) return false;
        String limpio = texto.trim();
        return limpio.length() <= maxLength && limpio.matches("[a-zA-Z0-9]+");
    }

    public static boolean esEmailValido(String email) {
        if (email == null) return false;
        return email.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null) return false;
        return telefono.trim().matches("^[0-9]{10}$");
    }
}
