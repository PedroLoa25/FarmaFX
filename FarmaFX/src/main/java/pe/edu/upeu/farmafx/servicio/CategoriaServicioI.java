package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Categoria;

import java.util.List;

public interface CategoriaServicioI {
    List<Categoria> listAll();
    List<Categoria> listActive();
    Categoria create(String nombre) throws Exception;
    Categoria update(int idCategoria, String nombre, Estado estado) throws Exception;
    void toggleEstado(int idCategoria) throws Exception;
    boolean existsByNombre(String nombre);
    Categoria findById(int idCategoria);
}