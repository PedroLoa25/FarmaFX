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
import pe.edu.upeu.farmafx.utils.TableViewUtils;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class ProductoController implements SupportsClose {

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> estadoFilter;   // "Todos", "Activos", "Inactivos"
    @FXML private ComboBox<String> ordenCombo;      // "ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓", "Precio ↑", "Precio ↓", "Stock ↑", "Stock ↓"
    @FXML private Button refreshBtn;

    @FXML private TableView<Producto> tabla;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colNombre;
    @FXML private TableColumn<Producto, String>  colMarca;
    @FXML private TableColumn<Producto, String>  colCategoria;
    @FXML private TableColumn<Producto, String>  colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String>  colEstado;

    private final ProductoServicioI servicio = ProductoServicioImp.getInstance();
    private final ObservableList<Producto> data = FXCollections.observableArrayList();
    private FilteredList<Producto> filtered;

    private Runnable onClose = () -> {};
    @Override public void setOnClose(Runnable r) { this.onClose = (r != null) ? r : () -> {}; }

    @FXML
    public void initialize() {
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

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

        estadoFilter.getItems().setAll("Todos", "Activos", "Inactivos");
        estadoFilter.setValue("Todos");
        estadoFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());

        ordenCombo.getItems().setAll("ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓", "Precio ↑", "Precio ↓", "Stock ↑", "Stock ↓");
        ordenCombo.setValue("ID ↑");
        ordenCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applySort());

        searchField.textProperty().addListener((obs, a, b) -> applyFilters());

        refreshBtn.setOnAction(e -> onRefresh());

        onRefresh();
    }

    private void applyFilters() {
        final String q = normalizeForSearch(searchField.getText());
        final String estadoSel = estadoFilter.getValue();

        filtered.setPredicate(p -> {
            if (p == null) return false;

            // Estado (del producto). Nota: la visibilidad al cliente debería considerar marca/categoría activas en la capa servicio.
            if (Objects.equals(estadoSel, "Activos") && !p.getEstado().isActivo()) return false;
            if (Objects.equals(estadoSel, "Inactivos") && p.getEstado().isActivo()) return false;

            // Búsqueda: nombre, marca, categoría
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
        // Limpia búsqueda y filtros
        if (searchField != null) searchField.clear();
        if (estadoFilter != null) estadoFilter.setValue("Todos");
        if (ordenCombo != null) ordenCombo.setValue("ID ↑");

        // Recarga
        data.setAll(servicio.listAll());

        // Orden por defecto
        data.sort(Comparator.comparing(Producto::getIdProducto));

        // Reaplicar filtro
        applyFilters();

        tabla.getSelectionModel().clearSelection();
    }

    @FXML
    public void onSalir() {
        onClose.run();
    }
}