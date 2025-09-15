package pe.edu.upeu.farmafx.modelo;

import pe.edu.upeu.farmafx.enums.Estado;

public class Promocion {
    private int idPromocion;
    private String nombre; // Ej: "Oferta 2x1 Panadol" o "20% Dcto. en Vitaminas"
    private double precioOriginal;
    private double precioOferta;
    private Estado estado = Estado.ACTIVO;

    //<editor-fold desc="Getters y Setters">
    public int getIdPromocion() {
        return idPromocion;
    }

    public void setIdPromocion(int idPromocion) {
        this.idPromocion = idPromocion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecioOriginal() {
        return precioOriginal;
    }

    public void setPrecioOriginal(double precioOriginal) {
        this.precioOriginal = precioOriginal;
    }

    public double getPrecioOferta() {
        return precioOferta;
    }

    public void setPrecioOferta(double precioOferta) {
        this.precioOferta = precioOferta;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    //</editor-fold>
}