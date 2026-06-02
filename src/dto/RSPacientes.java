package dto;

import java.util.List;
import pojo.Paciente;

public class RSPacientes {
    private boolean error;
    private String mensaje;
    private List<Paciente> pacientes; // Asumiendo que el backend envía la lista en la llave "pacientes"

    public RSPacientes() {}

    public boolean isError() { return error; }
    public void setError(boolean error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<Paciente> getPacientes() { return pacientes; }
    public void setPacientes(List<Paciente> pacientes) { this.pacientes = pacientes; }
}
