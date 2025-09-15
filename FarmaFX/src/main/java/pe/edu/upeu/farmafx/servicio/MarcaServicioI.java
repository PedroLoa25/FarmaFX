package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Marca;

import java.util.List;

public interface MarcaServicioI {
    List<Marca> listAll();
    List<Marca> listActive();
    Marca create(String nombre) throws Exception;
    Marca update(int idMarca, String nombre, Estado estado) throws Exception;
    void toggleEstado(int idMarca) throws Exception;
    boolean existsByNombre(String nombre);
    Marca findById(int idMarca);
}