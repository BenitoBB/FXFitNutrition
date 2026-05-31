package utilidad;

import pojo.Medico;

/**
 * Singleton que mantiene la sesión activa del médico logueado.
 * Se limpia al cerrar sesión.
 */
public class Sesion {
    private static Medico medicoSesion;
    private static boolean esAdministrador;

    public static Medico getMedicoSesion() {
        return medicoSesion;
    }

    public static void setMedicoSesion(Medico medico) {
        medicoSesion = medico;
    }

    public static boolean isEsAdministrador() {
        return esAdministrador;
    }

    public static void setEsAdministrador(boolean admin) {
        esAdministrador = admin;
    }

    public static boolean requiereFiltroPorMedico() {
        return medicoSesion != null && !esAdministrador;
    }

    public static int getIdMedicoSesion() {
        return medicoSesion != null ? medicoSesion.getIdMedico() : 0;
    }

    public static int getIdMedicoParaFiltro() {
        return requiereFiltroPorMedico() ? getIdMedicoSesion() : 0;
    }

    public static void cerrarSesion() {
        medicoSesion = null;
        esAdministrador = false;
    }
}
