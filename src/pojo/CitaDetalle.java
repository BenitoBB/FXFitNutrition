package pojo;

public class CitaDetalle extends Cita {
    private String pacienteNombre;
    private String pacientePrimerApellido;
    private String pacienteSegundoApellido;
    private String pacienteEmail;
    private String medicoNombre;
    private String medicoPrimerApellido;
    private String medicoSegundoApellido;

    public CitaDetalle() {
        super();
    }

    public String getPacienteNombre() {
        return pacienteNombre;
    }

    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }

    public String getPacientePrimerApellido() {
        return pacientePrimerApellido;
    }

    public void setPacientePrimerApellido(String pacientePrimerApellido) {
        this.pacientePrimerApellido = pacientePrimerApellido;
    }

    public String getPacienteSegundoApellido() {
        return pacienteSegundoApellido;
    }

    public void setPacienteSegundoApellido(String pacienteSegundoApellido) {
        this.pacienteSegundoApellido = pacienteSegundoApellido;
    }

    public String getPacienteEmail() {
        return pacienteEmail;
    }

    public void setPacienteEmail(String pacienteEmail) {
        this.pacienteEmail = pacienteEmail;
    }

    public String getMedicoNombre() {
        return medicoNombre;
    }

    public void setMedicoNombre(String medicoNombre) {
        this.medicoNombre = medicoNombre;
    }

    public String getMedicoPrimerApellido() {
        return medicoPrimerApellido;
    }

    public void setMedicoPrimerApellido(String medicoPrimerApellido) {
        this.medicoPrimerApellido = medicoPrimerApellido;
    }

    public String getMedicoSegundoApellido() {
        return medicoSegundoApellido;
    }

    public void setMedicoSegundoApellido(String medicoSegundoApellido) {
        this.medicoSegundoApellido = medicoSegundoApellido;
    }

    public String getNombrePacienteCompleto() {
        return construirNombreCompleto(pacienteNombre, pacientePrimerApellido, pacienteSegundoApellido);
    }

    public String getNombreMedicoCompleto() {
        return construirNombreCompleto(medicoNombre, medicoPrimerApellido, medicoSegundoApellido);
    }

    private String construirNombreCompleto(String nombre, String primerApellido, String segundoApellido) {
        StringBuilder nombreCompleto = new StringBuilder();
        agregarParteNombre(nombreCompleto, nombre);
        agregarParteNombre(nombreCompleto, primerApellido);
        agregarParteNombre(nombreCompleto, segundoApellido);
        return nombreCompleto.toString();
    }

    private void agregarParteNombre(StringBuilder nombreCompleto, String parte) {
        if (parte != null && !parte.trim().isEmpty()) {
            if (nombreCompleto.length() > 0) {
                nombreCompleto.append(" ");
            }
            nombreCompleto.append(parte.trim());
        }
    }

    @Override
    public String toString() {
        String hora = getHoraCita() != null && getHoraCita().length() >= 5 ? getHoraCita().substring(0, 5) : "";
        return getFechaCita() + " " + hora + " - " + getNombrePacienteCompleto();
    }
}
