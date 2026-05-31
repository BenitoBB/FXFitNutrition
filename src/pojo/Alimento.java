package pojo;

public class Alimento {
    private int idAlimento;
    private String nombreAlimento;
    private String porcion;
    private double caloriasPorcion;

    public int getIdAlimento() {
        return idAlimento;
    }

    public void setIdAlimento(int idAlimento) {
        this.idAlimento = idAlimento;
    }

    public String getNombreAlimento() {
        return nombreAlimento;
    }

    public void setNombreAlimento(String nombreAlimento) {
        this.nombreAlimento = nombreAlimento;
    }

    public String getPorcion() {
        return porcion;
    }

    public void setPorcion(String porcion) {
        this.porcion = porcion;
    }

    public double getCaloriasPorcion() {
        return caloriasPorcion;
    }

    public void setCaloriasPorcion(double caloriasPorcion) {
        this.caloriasPorcion = caloriasPorcion;
    }

    @Override
    public String toString() {
        return nombreAlimento != null ? nombreAlimento : "Alimento #" + idAlimento;
    }
}
