package pojo;

public class Domicilio {
    private int idDomicilio;
    private int idDireccion; // Backend uses Direccion.idDireccion
    private String calle;
    private String numero;
    private int idColonia;
    
    // Campos extra para el formulario
    private Object codigoPostal; // Can be Integer from backend or String from client
    private String colonia;
    private String ciudad;
    private String municipio;
    private String estado;

    public Domicilio() {}

    public int getIdDomicilio() { 
        // If idDomicilio is 0 but idDireccion has a value, use idDireccion
        return idDomicilio > 0 ? idDomicilio : idDireccion; 
    }
    public void setIdDomicilio(int idDomicilio) { this.idDomicilio = idDomicilio; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getIdColonia() { return idColonia; }
    public void setIdColonia(int idColonia) { this.idColonia = idColonia; }

    public String getCodigoPostal() { 
        return codigoPostal != null ? String.valueOf(codigoPostal) : "";
    }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getColonia() { return colonia; }
    public void setColonia(String colonia) { this.colonia = colonia; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getDireccionCompleta() {
        if (calle == null || calle.isEmpty()) return "Sin dirección";
        return calle + " " + numero + ", " + (colonia != null ? colonia : "") + ", C.P. " + getCodigoPostal() + ", " + (ciudad != null ? ciudad : "") + ", " + (estado != null ? estado : "");
    }
}
