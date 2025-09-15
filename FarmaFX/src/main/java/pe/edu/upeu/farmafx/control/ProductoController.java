package pe.edu.upeu.farmafx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Producto;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.servicio.ProductoServicioI;
import pe.edu.upeu.farmafx.servicio.ProductoServicioImp;
import pe.edu.upeu.farmafx.servicio.MarcaServicioI;
import pe.edu.upeu.farmafx.servicio.MarcaServicioImp;
import pe.edu.upeu.farmafx.servicio.CategoriaServicioI;
import pe.edu.upeu.farmafx.servicio.CategoriaServicioImp;
import pe.edu.upeu.farmafx.utils.TableViewUtils;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class ProductoController implements SupportsClose {

    // Filtros (ya los tenías)
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> estadoFilter;   // "Todos", "Activos", "Inactivos"
    @FXML private ComboBox<String> ordenCombo;      // "ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓", "Precio ↑", "Precio ↓", "Stock ↑", "Stock ↓"
    @FXML private Button refreshBtn;

    // Formulario simple (nuevo)
    @FXML private TextField nombreField;
    @FXML private TextField precioField;
    @FXML private TextField stockField;
    @FXML private ComboBox<Marca> marcaCombo;
    @FXML private ComboBox<Categoria> categoriaCombo;
    @FXML private CheckBox activoCheck;
    @FXML private Button nuevoBtn;
    @FXML private Button guardarBtn;

    // Tabla (ya la tenías)
    @FXML private TableView<Producto> tabla;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colNombre;
    @FXML private TableColumn<Producto, String>  colMarca;
    @FXML private TableColumn<Producto, String>  colCategoria;
    @FXML private TableColumn<Producto, String>  colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String>  colEstado;

    private final ProductoServicioI servicio = ProductoServicioImp.getInstance();
    private final MarcaServicioI marcaServicio = MarcaServicioImp.getInstance();
    private final CategoriaServicioI categoriaServicio = CategoriaServicioImp.getInstance();

    private final ObservableList<Producto> data = FXCollections.observableArrayList();
    private FilteredList<Producto> filtered;

    // Control de edición: si es null => Nuevo, si no => Editar este ID
    private Integer editingId = null;

    private Runnable onClose = () -> {};
    @Override public void setOnClose(Runnable r) { this.onClose = (r != null) ? r : () -> {}; }

    @FXML
    public void initialize() {
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        // Tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getMarca() != null ? c.getValue().getMarca().getNombre() : ""
        ));
        colCategoria.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCategoria() != null ? c.getValue().getCategoria().getNombre() : ""
        ));
        colPrecio.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                money.format(c.getValue().getPrecio())
        ));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getEstado().isActivo() ? "ACTIVO" : "INACTIVO"
        ));

        filtered = new FilteredList<>(data, it -> true);
        tabla.setItems(filtered);
        TableViewUtils.lockColumns(tabla);

        // Filtros
        estadoFilter.getItems().setAll("Todos", "Activos", "Inactivos");
        estadoFilter.setValue("Todos");
        estadoFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());

        ordenCombo.getItems().setAll("ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓", "Precio ↑", "Precio ↓", "Stock ↑", "Stock ↓");
        ordenCombo.setValue("ID ↑");
        ordenCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applySort());

        searchField.textProperty().addListener((obs, a, b) -> applyFilters());
        refreshBtn.setOnAction(e -> onRefresh());

        // Formulario: combos de marca y categoría
        marcaCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? "" : item.getNombre());
            }
        });
        marcaCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? "" : item.getNombre());
            }
        });

        categoriaCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? "" : item.getNombre());
            }
        });
        categoriaCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item==null ? "" : item.getNombre());
            }
        });

        // Selección en tabla => carga al formulario
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, a, sel) -> {
            if (sel == null) {
                clearForm();
            } else {
                loadToForm(sel);
            }
        });

        // Cargar datos iniciales
        onRefresh();
    }

    private void applyFilters() {
        final String q = normalizeForSearch(searchField.getText());
        final String estadoSel = estadoFilter.getValue();

        filtered.setPredicate(p -> {
            if (p == null) return false;

            if (Objects.equals(estadoSel, "Activos") && !p.getEstado().isActivo()) return false;
            if (Objects.equals(estadoSel, "Inactivos") && p.getEstado().isActivo()) return false;

            String nom = normalizeForSearch(p.getNombre());
            String mar = normalizeForSearch(p.getMarca() != null ? p.getMarca().getNombre() : "");
            String cat = normalizeForSearch(p.getCategoria() != null ? p.getCategoria().getNombre() : "");

            if (q.isBlank()) return true;
            return nom.contains(q) || mar.contains(q) || cat.contains(q);
        });
    }

    private void applySort() {
        String sel = ordenCombo.getValue();
        if (sel == null) sel = "ID ↑";
        switch (sel) {
            case "ID ↓" -> data.sort(Comparator.comparing(Producto::getIdProducto).reversed());
            case "Nombre ↑" -> data.sort(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER));
            case "Nombre ↓" -> data.sort(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER).reversed());
            case "Precio ↑" -> data.sort(Comparator.comparingDouble(Producto::getPrecio));
            case "Precio ↓" -> data.sort(Comparator.comparingDouble(Producto::getPrecio).reversed());
            case "Stock ↑" -> data.sort(Comparator.comparingInt(Producto::getStock));
            case "Stock ↓" -> data.sort(Comparator.comparingInt(Producto::getStock).reversed());
            default -> data.sort(Comparator.comparing(Producto::getIdProducto));
        }
    }

    private static String normalizeForSearch(String s) {
        String k = ValidacionUtils.normalizedKey(s == null ? "" : s);
        return k.replaceAll("[^a-z0-9\\s]", "");
    }

    @FXML
    public void onRefresh() {
        // Recargar combos (por si cambian marcas/categorías activas)
        marcaCombo.getItems().setAll(marcaServicio.listActive());
        categoriaCombo.getItems().setAll(categoriaServicio.listActive());

        // Limpia filtros
        if (searchField != null) searchField.clear();
        if (estadoFilter != null) estadoFilter.setValue("Todos");
        if (ordenCombo != null) ordenCombo.setValue("ID ↑");

        // Recarga de datos
        data.setAll(servicio.listAll());
        data.sort(Comparator.comparing(Producto::getIdProducto));
        applyFilters();

        tabla.getSelectionModel().clearSelection();
        clearForm();
    }

    // Formulario: Nuevo
    @FXML
    private void onNuevo() {
        tabla.getSelectionModel().clearSelection();
        clearForm();
        nombreField.requestFocus();
    }

    // Formulario: Guardar (crea o actualiza)
    @FXML
    private void onGuardar() {
        try {
            String nombre = safeTrim(nombreField.getText());
            if (nombre.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Ingresa el nombre");
                return;
            }
            double precio = Double.parseDouble(safeTrim(precioField.getText()));
            int stock = Integer.parseInt(safeTrim(stockField.getText()));
            Marca marca = marcaCombo.getValue();
            Categoria categoria = categoriaCombo.getValue();
            Estado estado = (activoCheck.isSelected() ? Estado.ACTIVO : Estado.INACTIVO);

            if (editingId == null) {
                // Crear (SIN estado, porque create(...) recibe 5 parámetros)
                servicio.create(nombre, precio, stock, marca, categoria);
            } else {
                // Actualizar (con estado)
                servicio.update(editingId, nombre, precio, stock, estado, marca, categoria);
            }

            onRefresh();
            alert(Alert.AlertType.INFORMATION, "Producto guardado correctamente.");
        } catch (NumberFormatException nfe) {
            alert(Alert.AlertType.WARNING, "Verifica los valores numéricos (precio y stock).");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void loadToForm(Producto p) {
        editingId = p.getIdProducto();
        nombreField.setText(p.getNombre());
        precioField.setText(String.valueOf(p.getPrecio()));
        stockField.setText(String.valueOf(p.getStock()));
        activoCheck.setSelected(p.getEstado().isActivo());

        // Seleccionar marca/categoría si están en las listas (pueden ser null)
        if (p.getMarca() != null) {
            marcaCombo.getSelectionModel().select(p.getMarca());
        } else {
            marcaCombo.getSelectionModel().clearSelection();
        }
        if (p.getCategoria() != null) {
            categoriaCombo.getSelectionModel().select(p.getCategoria());
        } else {
            categoriaCombo.getSelectionModel().clearSelection();
        }
    }

    private void clearForm() {
        editingId = null;
        if (nombreField != null) nombreField.clear();
        if (precioField != null) precioField.clear();
        if (stockField != null) stockField.clear();
        if (activoCheck != null) activoCheck.setSelected(true);
        if (marcaCombo != null) marcaCombo.getSelectionModel().clearSelection();
        if (categoriaCombo != null) categoriaCombo.getSelectionModel().clearSelection();
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }

    private void alert(Alert.AlertType type, String msg) { new Alert(type, msg).showAndWait(); }

    @FXML
    public void onSalir() { onClose.run(); }
}