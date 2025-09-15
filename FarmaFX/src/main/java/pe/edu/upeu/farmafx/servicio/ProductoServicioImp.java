package pe.edu.upeu.farmafx.servicio;

import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.modelo.Producto;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductoServicioImp implements ProductoServicioI {

    private static ProductoServicioImp INSTANCE;
    public static ProductoServicioImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProductoServicioImp();
        }
        return INSTANCE;
    }

    private final List<Producto> data = new ArrayList<>();
    private int seq = 1;

    // Referenciar servicios para datos de Marca y Categoria
    private final MarcaServicioI marcaServicio = MarcaServicioImp.getInstance();
    private final CategoriaServicioI categoriaServicio = CategoriaServicioImp.getInstance();

    private ProductoServicioImp() {
        // Inicializar con algunos productos de muestra
        try {
            // Obtener algunas marcas y categorías existentes para productos de ejemplo
            List<Marca> marcas = marcaServicio.listActive();
            List<Categoria> categorias = categoriaServicio.listActive();
            
            if (!marcas.isEmpty() && !categorias.isEmpty()) {
                create("Paracetamol 500mg", 15.50, 100, marcas.get(0), categorias.get(0));
                create("Ibuprofeno 400mg", 18.75, 75, marcas.get(0), categorias.get(0));
                create("Shampoo Anticaspa", 25.90, 50, marcas.size() > 1 ? marcas.get(1) : marcas.get(0), categorias.size() > 1 ? categorias.get(1) : categorias.get(0));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public List<Producto> listAll() {
        return Collections.unmodifiableList(data);
    }

    @Override
    public List<Producto> listActive() {
        List<Producto> r = new ArrayList<>();
        for (Producto p : data) {
            if (p.getEstado().isActivo()) r.add(p);
        }
        return r;
    }

    @Override
    public Producto create(String nombre, double precio, int stock, Marca marca, Categoria categoria) throws Exception {
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 3, 100);
        
        if (existsByNombre(n)) {
            throw new Exception("El nombre de producto ya existe.");
        }
        
        if (precio < 0) {
            throw new Exception("El precio no puede ser negativo.");
        }
        
        if (stock < 0) {
            throw new Exception("El stock no puede ser negativo.");
        }
        
        if (marca == null) {
            throw new Exception("La marca es requerida.");
        }
        
        if (categoria == null) {
            throw new Exception("La categoría es requerida.");
        }
        
        Producto p = new Producto();
        p.setIdProducto(seq++);
        p.setNombre(n);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setEstado(Estado.ACTIVO);
        p.setMarca(marca);
        p.setCategoria(categoria);
        data.add(p);
        return p;
    }

    @Override
    public Producto update(Integer idProducto, String nombre, double precio, int stock, Estado estado, Marca marca, Categoria categoria) throws Exception {
        Producto p = findById(idProducto);
        if (p == null) throw new Exception("Producto no encontrado.");
        
        String n = ValidacionUtils.normalizeBasic(nombre);
        ValidacionUtils.validarNombre(n, 3, 100);
        
        if (!equalsByNombre(p.getNombre(), n) && existsByNombre(n)) {
            throw new Exception("El nombre de producto ya existe.");
        }
        
        if (precio < 0) {
            throw new Exception("El precio no puede ser negativo.");
        }
        
        if (stock < 0) {
            throw new Exception("El stock no puede ser negativo.");
        }
        
        if (marca == null) {
            throw new Exception("La marca es requerida.");
        }
        
        if (categoria == null) {
            throw new Exception("La categoría es requerida.");
        }
        
        p.setNombre(n);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setEstado(estado == null ? p.getEstado() : estado);
        p.setMarca(marca);
        p.setCategoria(categoria);
        return p;
    }

    @Override
    public void toggleEstado(Integer idProducto) throws Exception {
        Producto p = findById(idProducto);
        if (p == null) throw new Exception("Producto no encontrado.");
        p.setEstado(p.getEstado().isActivo() ? Estado.INACTIVO : Estado.ACTIVO);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String key = ValidacionUtils.normalizedKey(nombre);
        for (Producto p : data) {
            if (ValidacionUtils.normalizedKey(p.getNombre()).equals(key)) return true;
        }
        return false;
    }

    private boolean equalsByNombre(String a, String b) {
        return ValidacionUtils.normalizedKey(a).equals(ValidacionUtils.normalizedKey(b));
    }

    @Override
    public Producto findById(Integer idProducto) {
        if (idProducto == null) return null;
        for (Producto p : data) {
            if (idProducto.equals(p.getIdProducto())) return p;
        }
        return null;
    }
}