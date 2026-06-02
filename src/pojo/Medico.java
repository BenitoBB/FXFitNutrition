package pojo;

public class Medico {

    private int idMedico;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String fechaNacimiento;
    private String sexo;
    private int idDomicilio;
    private String noPersonal;
    private String cedulaProfesional;
    private String contrasena;
    private String fotografia; // Base64
    private int esAdministrador;
    private int estatus;
    
    // Campos extra para mostrar la dirección en listas (vienen planos del JSON del API)
    private String direccionCompleta;
    private String calle;
    private String numero;
    private Integer idColonia;
    private String nombreColonia;
    private String codigoPostal;
    private String ciudad;
    private String estado;
    
    private Domicilio domicilio;

    public Medico() {
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public int getIdDomicilio() {
        return idDomicilio;
    }

    public void setIdDomicilio(int idDomicilio) {
        this.idDomicilio = idDomicilio;
    }

    public String getNoPersonal() {
        return noPersonal;
    }

    public void setNoPersonal(String noPersonal) {
        this.noPersonal = noPersonal;
    }

    public String getCedulaProfesional() {
        return cedulaProfesional;
    }

    public void setCedulaProfesional(String cedulaProfesional) {
        this.cedulaProfesional = cedulaProfesional;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }

    public int getEsAdministrador() {
        return esAdministrador;
    }

    public void setEsAdministrador(int esAdministrador) {
        this.esAdministrador = esAdministrador;
    }

    /** Helper: interpreta 1 como true, 0 como false */
    public boolean esAdmin() {
        return esAdministrador == 1;
    }

    public int getEstatus() {
        return estatus;
    }

    public void setEstatus(int estatus) {
        this.estatus = estatus;
    }

    public String getNombreCompleto() {
        String completo = nombre + " " + primerApellido;
        if (segundoApellido != null && !segundoApellido.isEmpty()) {
            completo += " " + segundoApellido;
        }
        return completo;
    }

    public Domicilio getDomicilio() {
        if (domicilio == null && (calle != null || idColonia != null || idDomicilio > 0)) {
            domicilio = new Domicilio();
            domicilio.setIdDomicilio(idDomicilio);
            domicilio.setCalle(calle);
            domicilio.setNumero(numero);
            if (idColonia != null) {
                domicilio.setIdColonia(idColonia);
            }
            domicilio.setColonia(nombreColonia);
            domicilio.setCodigoPostal(codigoPostal);
            domicilio.setCiudad(ciudad);
            domicilio.setEstado(estado);
        }
        return domicilio;
    }
    public void setDomicilio(Domicilio domicilio) { this.domicilio = domicilio; }

    public String getDireccionCompleta() {
        if (direccionCompleta != null && !direccionCompleta.trim().isEmpty()) {
            return direccionCompleta;
        }
        if (domicilio != null) {
            return domicilio.getDireccionCompleta();
        }
        return "Sin dirección";
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
