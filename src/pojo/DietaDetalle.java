package pojo;

import java.util.List;

public class DietaDetalle {
    private int idDieta;
    private String nombreDieta;
    private double totalCalorias;
    private String observaciones;
    private int idMedico;
    private int editable;
    private List<CategoriaConAlimentos> categorias;

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

    public double getTotalCalorias() {
        return totalCalorias;
    }

    public void setTotalCalorias(double totalCalorias) {
        this.totalCalorias = totalCalorias;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public int getEditable() {
        return editable;
    }

    public void setEditable(int editable) {
        this.editable = editable;
    }

    public List<CategoriaConAlimentos> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaConAlimentos> categorias) {
        this.categorias = categorias;
    }
}
