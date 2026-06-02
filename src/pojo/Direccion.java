package pojo;

/**
 * POJO que mapea la respuesta del endpoint
 * GET /api/direccion/obtener-direccion-codigo-postal/{cp}
 * Cada instancia representa una colonia encontrada para ese código postal.
 */
public class Direccion {
    private Integer idColonia;
    private String colonia;
    private String ciudad;
    private String municipio;
    private String estado;
    private Integer codigoPostal;
    
    private Integer idDireccion;
    private String calle;
    private String numero;

    public Direccion() {}

    public Integer getIdColonia() { return idColonia; }
    public void setIdColonia(Integer idColonia) { this.idColonia = idColonia; }

    public String getColonia() { return colonia; }
    public void setColonia(String colonia) { this.colonia = colonia; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(Integer codigoPostal) { this.codigoPostal = codigoPostal; }

    public Integer getIdDireccion() { return idDireccion; }
    public void setIdDireccion(Integer idDireccion) { this.idDireccion = idDireccion; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    @Override
    public String toString() {
        return colonia;
    }
}
