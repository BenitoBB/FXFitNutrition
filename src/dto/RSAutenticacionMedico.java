package dto;

import pojo.Medico;

/**
 * Respuesta del endpoint POST /api/medico/login.
 * Mapea exactamente la estructura JSON que retorna RSAutenticacionMedico del backend.
 */
public class RSAutenticacionMedico {
    private boolean error;
    private String mensaje;
    private Medico medico;
    private boolean esAdministrador;

    public RSAutenticacionMedico() {}

    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public boolean isEsAdministrador() { return esAdministrador; }
    public void setEsAdministrador(boolean esAdministrador) { this.esAdministrador = esAdministrador; }
}
