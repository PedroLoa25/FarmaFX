package pe.edu.upeu.farmafx.modelo;

import pe.edu.upeu.farmafx.enums.Estado;

public class Marca {
    private int idMarca;
    private String nombre;
    private Estado estado = Estado.ACTIVO;

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
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