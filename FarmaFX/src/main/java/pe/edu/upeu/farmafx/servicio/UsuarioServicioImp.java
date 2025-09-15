package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.ArrayList;
import java.util.List;

public class UsuarioServicioImp implements UsuarioServicioI {

    private static UsuarioServicioImp INSTANCE;

    public static UsuarioServicioImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UsuarioServicioImp();
        }
        return INSTANCE;
    }

    private final List<Usuario> usuarios = new ArrayList<>();
    private int seq = 1;

    private UsuarioServicioImp() {
        try {
            Usuario admin = register("00000000", "Admin123$");
            admin.setRol(RolUsuario.ADMIN);
        } catch (Exception ignored) {}
    }

    @Override
    public Usuario authenticate(String dni, String rawPassword) throws Exception {
        String d = (dni == null) ? "" : dni.trim();
        Usuario u = findByDni(d);
        if (u == null) {
            throw new Exception("Usuario no encontrado");
        }
        String h = hash(rawPassword);
        if (!h.equals(u.getPasswordHash())) {
            throw new ErrorCredenciales("Credenciales inválidas");
        }
        return u;
    }

    @Override
    public Usuario register(String dni, String rawPassword) throws Exception {
        String d = (dni == null) ? "" : dni.trim();
        if (!ValidacionUtils.isValidDni(d)) {
            throw new Exception("DNI inválido (deben ser 8 dígitos).");
        }
        if (existsByDni(d)) {
            throw new Exception("El DNI ya está registrado.");
        }
        if (!ValidacionUtils.isStrongPassword(rawPassword)) {
            throw new Exception("La contraseña debe tener 8+ caracteres, mayúscula, minúscula, dígito y símbolo.");
        }

        Usuario u = new Usuario();
        u.setId(seq++);
        u.setDni(d);
        u.setPasswordHash(hash(rawPassword));
        u.setRol(RolUsuario.CLIENTE);

        usuarios.add(u);
        return u;
    }

    @Override
    public boolean existsByDni(String dni) {
        String d = (dni == null) ? "" : dni.trim();
        return findByDni(d) != null;
    }

    private Usuario findByDni(String d) {
        for (Usuario u : usuarios) {
            if (u.getDni().equals(d)) {
                return u;
            }
        }
        return null;
    }

    private String hash(String raw) {
        // Demo: cambiar por BCrypt más adelante
        return "H::" + raw;
    }

    public static class ErrorCredenciales extends Exception {
        public ErrorCredenciales(String msg) { super(msg); }
    }
}