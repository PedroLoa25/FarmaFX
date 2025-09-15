package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Promocion;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromocionServicioImp implements PromocionServicioI {

    private static PromocionServicioImp INSTANCE;
    public static PromocionServicioImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PromocionServicioImp();
        }
        return INSTANCE;
    }

    private final List<Promocion> data = new ArrayList<>();
    private int seq = 1;

    private PromocionServicioImp() {
        // Datos de ejemplo predefinidos
        try {
            create("20% Dcto. Paracetamol 500mg", 15.50, 12.40);
            create("Oferta Kit Antigripal", 35.00, 29.90);
            Promocion p = create("Shampoo Anticaspa ¡Lleva 2, Paga 1!", 25.90, 25.90);
            // Ejemplo de una promoción inactiva por defecto
            toggleEstado(p.getIdPromocion());
        } catch (Exception ignored) {}
    }

    @Override
    public List<Promocion> listAll() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public List<Promocion> listActive() {
        List<Promocion> r = new ArrayList<>();
        for (Promocion p : data) {
            if (p.getEstado().isActivo()) r.add(p);
        }
        return r;
    }

    @Override
    public Promocion create(String nombre, double precioOriginal, double precioOferta) throws Exception {
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 5, 100);
        if (existsByNombre(n)) {
            throw new Exception("El nombre de la promoción ya existe.");
        }
        if (precioOriginal < 0 || precioOferta < 0) {
            throw new Exception("Los precios no pueden ser negativos.");
        }
        if (precioOferta > precioOriginal) {
            throw new Exception("El precio de oferta no puede ser mayor al original.");
        }

        Promocion p = new Promocion();
        p.setIdPromocion(seq++);
        p.setNombre(n);
        p.setPrecioOriginal(precioOriginal);
        p.setPrecioOferta(precioOferta);
        p.setEstado(Estado.ACTIVO);
        data.add(p);
        return p;
    }

    @Override
    public Promocion update(int idPromocion, String nombre, double precioOriginal, double precioOferta, Estado estado) throws Exception {
        Promocion p = findById(idPromocion);
        if (p == null) throw new Exception("Promoción no encontrada.");

        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 5, 100);
        if (!equalsByNombre(p.getNombre(), n) && existsByNombre(n)) {
            throw new Exception("El nombre de la promoción ya existe.");
        }
        if (precioOriginal < 0 || precioOferta < 0) {
            throw new Exception("Los precios no pueden ser negativos.");
        }
        if (precioOferta > precioOriginal) {
            throw new Exception("El precio de oferta no puede ser mayor al original.");
        }

        p.setNombre(n);
        p.setPrecioOriginal(precioOriginal);
        p.setPrecioOferta(precioOferta);
        p.setEstado(estado == null ? p.getEstado() : estado);
        return p;
    }

    @Override
    public void toggleEstado(int idPromocion) throws Exception {
        Promocion p = findById(idPromocion);
        if (p == null) throw new Exception("Promoción no encontrada.");
        p.setEstado(p.getEstado().isActivo() ? Estado.INACTIVO : Estado.ACTIVO);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String key = ValidacionUtils.normalizedKey(nombre);
        for (Promocion p : data) {
            if (ValidacionUtils.normalizedKey(p.getNombre()).equals(key)) return true;
        }
        return false;
    }

    private boolean equalsByNombre(String a, String b) {
        return ValidacionUtils.normalizedKey(a).equals(ValidacionUtils.normalizedKey(b));
    }

    @Override
    public Promocion findById(int idPromocion) {
        for (Promocion p : data) {
            if (p.getIdPromocion() == idPromocion) return p;
        }
        return null;
    }
}