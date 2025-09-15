package pe.edu.upeu.farmafx.modelo;

import pe.edu.upeu.farmafx.enums.Estado;

public class Categoria {
    private int idCategoria;
    private String nombre;
    private Estado estado = Estado.ACTIVO;

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}