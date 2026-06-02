package pojo;

public class Consulta {
    private int idConsulta;
    private int idPaciente;
    private int idMedico;
    private Integer idCita;
    private String fechaConsulta;
    private double peso;
    private double talla;
    private Double imc;
    private Integer idDieta;
    private String nombreDieta;
    private int cancelada;
    private String observaciones;

    public Consulta() {
    }

    public int getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(int idConsulta) {
        this.idConsulta = idConsulta;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
    }

    public String getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(String fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getFechaConsultaCorta() {
        if (fechaConsulta == null || fechaConsulta.trim().isEmpty()) {
            return "";
        }
        return fechaConsulta.length() >= 10 ? fechaConsulta.substring(0, 10) : fechaConsulta;
    }

    public String getHoraConsulta() {
        if (fechaConsulta == null || fechaConsulta.trim().isEmpty()) {
            return "";
        }
        String texto = fechaConsulta.trim();
        int separador = texto.indexOf(' ');
        if (separador < 0 || texto.length() < separador + 6) {
            return "";
        }
        return texto.substring(separador + 1, separador + 6);
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getTalla() {
        return talla;
    }

    public void setTalla(double talla) {
        this.talla = talla;
    }

    public Double getImc() {
        return imc;
    }

    public void setImc(Double imc) {
        this.imc = imc;
    }

    public Integer getIdDieta() {
        return idDieta;
    }

    public void setIdDieta(Integer idDieta) {
        this.idDieta = idDieta;
    }

    public String getNombreDieta() {
        return nombreDieta;
    }

    public void setNombreDieta(String nombreDieta) {
        this.nombreDieta = nombreDieta;
    }

    public int getCancelada() {
        return cancelada;
    }

    public void setCancelada(int cancelada) {
        this.cancelada = cancelada;
    }

    public boolean isActiva() {
        return cancelada == 0;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
