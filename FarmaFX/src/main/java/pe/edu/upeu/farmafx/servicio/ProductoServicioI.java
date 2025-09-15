package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.modelo.Producto;

import java.util.List;

public interface ProductoServicioI {
    List<Producto> listAll();
    List<Producto> listActive();
    Producto create(String nombre, double precio, int stock, Marca marca, Categoria categoria) throws Exception;
    Producto update(Integer idProducto, String nombre, double precio, int stock, Estado estado, Marca marca, Categoria categoria) throws Exception;
    void toggleEstado(Integer idProducto) throws Exception;
    boolean existsByNombre(String nombre);
    Producto findById(Integer idProducto);
}