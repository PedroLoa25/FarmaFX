package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Promocion;

import java.util.List;

public interface PromocionServicioI {
    List<Promocion> listAll();
    List<Promocion> listActive();
    Promocion create(String nombre, double precioOriginal, double precioOferta) throws Exception;
    Promocion update(int idPromocion, String nombre, double precioOriginal, double precioOferta, Estado estado) throws Exception;
    void toggleEstado(int idPromocion) throws Exception;
    boolean existsByNombre(String nombre);
    Promocion findById(int idPromocion);
}
