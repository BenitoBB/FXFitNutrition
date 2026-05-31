package dto;

import java.util.List;

public class RQModificarDieta {
    private int idDieta;
    private String nombreDieta;
    private String observaciones;
    private List<String> categoriasAgregar;
    private List<Integer> categoriasEliminar;
    private List<RQAlimentoEnCategoria> alimentosAgregar;
    private List<RQAlimentoEnCategoria> alimentosEliminar;

    public int getIdDieta() {
        return idDieta;
    }

    public void setIdDieta(int idDieta) {
        this.idDieta = idDieta;
    }

    public String getNombreDieta() {
        return nombreDieta;
    }

    public void setNombreDieta(String nombreDieta) {
        this.nombreDieta = nombreDieta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<String> getCategoriasAgregar() {
        return categoriasAgregar;
    }

    public void setCategoriasAgregar(List<String> categoriasAgregar) {
        this.categoriasAgregar = categoriasAgregar;
    }

    public List<Integer> getCategoriasEliminar() {
        return categoriasEliminar;
    }

    public void setCategoriasEliminar(List<Integer> categoriasEliminar) {
        this.categoriasEliminar = categoriasEliminar;
    }

    public List<RQAlimentoEnCategoria> getAlimentosAgregar() {
        return alimentosAgregar;
    }

    public void setAlimentosAgregar(List<RQAlimentoEnCategoria> alimentosAgregar) {
        this.alimentosAgregar = alimentosAgregar;
    }

    public List<RQAlimentoEnCategoria> getAlimentosEliminar() {
        return alimentosEliminar;
    }

    public void setAlimentosEliminar(List<RQAlimentoEnCategoria> alimentosEliminar) {
        this.alimentosEliminar = alimentosEliminar;
    }
}
