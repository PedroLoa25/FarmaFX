package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarcaServicioImp implements MarcaServicioI {

    private static MarcaServicioImp INSTANCE;
    public static MarcaServicioImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MarcaServicioImp();
        }
        return INSTANCE;
    }

    private final List<Marca> data = new ArrayList<>();
    private int seq = 1;

    private MarcaServicioImp() {
        try {
            create("Bayer");
            create("Pfizer");
            create("Genfar");
        } catch (Exception ignored) {}
    }

    @Override
    public List<Marca> listAll() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public List<Marca> listActive() {
        List<Marca> r = new ArrayList<>();
        for (Marca m : data) {
            if (m.getEstado().isActivo()) r.add(m);
        }
        return r;
    }

    @Override
    public Marca create(String nombre) throws Exception {
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 3, 50);
        if (existsByNombre(n)) {
            throw new Exception("El nombre de marca ya existe.");
        }
        Marca m = new Marca();
        m.setIdMarca(seq++);
        m.setNombre(n);
        m.setEstado(Estado.ACTIVO);
        data.add(m);
        return m;
    }

    @Override
    public Marca update(int idMarca, String nombre, Estado estado) throws Exception {
        Marca m = findById(idMarca);
        if (m == null) throw new Exception("Marca no encontrada.");
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 3, 50);
        if (!equalsByNombre(m.getNombre(), n) && existsByNombre(n)) {
            throw new Exception("El nombre de marca ya existe.");
        }
        m.setNombre(n);
        m.setEstado(estado == null ? m.getEstado() : estado);
        return m;
    }

    @Override
    public void toggleEstado(int idMarca) throws Exception {
        Marca m = findById(idMarca);
        if (m == null) throw new Exception("Marca no encontrada.");
        m.setEstado(m.getEstado().isActivo() ? Estado.INACTIVO : Estado.ACTIVO);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String key = ValidacionUtils.normalizedKey(nombre);
        for (Marca m : data) {
            if (ValidacionUtils.normalizedKey(m.getNombre()).equals(key)) return true;
        }
        return false;
    }

    private boolean equalsByNombre(String a, String b) {
        return ValidacionUtils.normalizedKey(a).equals(ValidacionUtils.normalizedKey(b));
    }

    @Override
    public Marca findById(int idMarca) {
        for (Marca m : data) {
            if (m.getIdMarca() == idMarca) return m;
        }
        return null;
    }
}