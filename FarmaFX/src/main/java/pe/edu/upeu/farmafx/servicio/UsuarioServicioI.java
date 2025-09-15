package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.modelo.Usuario;

public interface UsuarioServicioI {
    Usuario authenticate(String dni, String rawPassword) throws Exception;
    Usuario register(String dni, String rawPassword) throws Exception;
    boolean existsByDni(String dni);
}