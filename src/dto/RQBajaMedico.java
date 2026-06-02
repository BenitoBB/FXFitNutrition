package dto;

public class RQBajaMedico {
    private Integer idMedicoBaja;
    private Integer idMedicoNuevo;
    private Integer esAdministrador;

    public RQBajaMedico() {
    }

    public Integer getIdMedicoBaja() {
        return idMedicoBaja;
    }

    public void setIdMedicoBaja(Integer idMedicoBaja) {
        this.idMedicoBaja = idMedicoBaja;
    }

    public Integer getIdMedicoNuevo() {
        return idMedicoNuevo;
    }

    public void setIdMedicoNuevo(Integer idMedicoNuevo) {
        this.idMedicoNuevo = idMedicoNuevo;
    }

    public Integer getEsAdministrador() {
        return esAdministrador;
    }

    public void setEsAdministrador(Integer esAdministrador) {
        this.esAdministrador = esAdministrador;
    }
}
