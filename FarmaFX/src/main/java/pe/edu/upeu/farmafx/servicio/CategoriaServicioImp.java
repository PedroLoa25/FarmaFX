package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoriaServicioImp implements CategoriaServicioI {

    private static CategoriaServicioImp INSTANCE;
    public static CategoriaServicioImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CategoriaServicioImp();
        }
        return INSTANCE;
    }

    private final List<Categoria> data = new ArrayList<>();
    private int seq = 1;

    private CategoriaServicioImp() {
        try {
            create("Analgésicos");
            create("Higiene");
            create("Suplementos");
        } catch (Exception ignored) {}
    }

    @Override
    public List<Categoria> listAll() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public List<Categoria> listActive() {
        List<Categoria> r = new ArrayList<>();
        for (Categoria c : data) {
            if (c.getEstado().isActivo()) r.add(c);
        }
        return r;
    }

    @Override
    public Categoria create(String nombre) throws Exception {
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 3, 50);
        if (existsByNombre(n)) {
            throw new Exception("El nombre de categoría ya existe.");
        }
        Categoria c = new Categoria();
        c.setIdCategoria(seq++);
        c.setNombre(n);
        c.setEstado(Estado.ACTIVO);
        data.add(c);
        return c;
    }

    @Override
    public Categoria update(int idCategoria, String nombre, Estado estado) throws Exception {
        Categoria c = findById(idCategoria);
        if (c == null) throw new Exception("Categoría no encontrada.");
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 3, 50);
        if (!equalsByNombre(c.getNombre(), n) && existsByNombre(n)) {
            throw new Exception("El nombre de categoría ya existe.");
        }
        c.setNombre(n);
        c.setEstado(estado == null ? c.getEstado() : estado);
        return c;
    }

    @Override
    public void toggleEstado(int idCategoria) throws Exception {
        Categoria c = findById(idCategoria);
        if (c == null) throw new Exception("Categoría no encontrada.");
        c.setEstado(c.getEstado().isActivo() ? Estado.INACTIVO : Estado.ACTIVO);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String key = ValidacionUtils.normalizedKey(nombre);
        for (Categoria c : data) {
            if (ValidacionUtils.normalizedKey(c.getNombre()).equals(key)) return true;
        }
        return false;
    }

    private boolean equalsByNombre(String a, String b) {
        return ValidacionUtils.normalizedKey(a).equals(ValidacionUtils.normalizedKey(b));
    }

    @Override
    public Categoria findById(int idCategoria) {
        for (Categoria c : data) {
            if (c.getIdCategoria() == idCategoria) return c;
        }
        return null;
    }
}