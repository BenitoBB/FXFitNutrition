package pojo;

import java.util.List;

public class CategoriaConAlimentos {
    private int idCategoria;
    private String nombreCategoria;
    private List<AlimentoEnDieta> alimentos;

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public List<AlimentoEnDieta> getAlimentos() {
        return alimentos;
    }

    public void setAlimentos(List<AlimentoEnDieta> alimentos) {
        this.alimentos = alimentos;
    }

    @Override
    public String toString() {
        return nombreCategoria != null ? nombreCategoria : "Categoria #" + idCategoria;
    }
}
