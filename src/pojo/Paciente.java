package pojo;

public class Paciente {
    private int idPaciente;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String fechaNacimiento;
    private String sexo;
    private Float peso;
    private Float estatura;
    private Float talla;
    private String email;
    private String telefono;
    private Integer idDomicilio;
    private String codigoAcceso;
    private String fotografia;
    private int idMedico;
    private int estatus;
    
    private Domicilio domicilio;

    public Paciente() {}

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Float getPeso() { return peso; }
    public void setPeso(Float peso) { this.peso = peso; }

    public Float getEstatura() { return estatura; }
    public void setEstatura(Float estatura) { this.estatura = estatura; }

    public Float getTalla() { return talla; }
    public void setTalla(Float talla) { this.talla = talla; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getIdDomicilio() { return idDomicilio; }
    public void setIdDomicilio(Integer idDomicilio) { this.idDomicilio = idDomicilio; }

    public String getCodigoAcceso() { return codigoAcceso; }
    public void setCodigoAcceso(String codigoAcceso) { this.codigoAcceso = codigoAcceso; }

    public String getFotografia() { return fotografia; }
    public void setFotografia(String fotografia) { this.fotografia = fotografia; }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

    public int getEstatus() { return estatus; }
    public void setEstatus(int estatus) { this.estatus = estatus; }

    public Domicilio getDomicilio() { return domicilio; }
    public void setDomicilio(Domicilio domicilio) { this.domicilio = domicilio; }
    
    public String getNombreCompleto() {
        String completo = nombre + " " + primerApellido;
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            completo += " " + segundoApellido;
        }
        return completo;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
