package pe.edu.upeu.farmafx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.modelo.Marca;
import pe.edu.upeu.farmafx.modelo.Producto;
import pe.edu.upeu.farmafx.servicio.*;
import pe.edu.upeu.farmafx.utils.TableViewUtils;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ClienteProductosController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private ComboBox<Marca> marcaFilterCombo;
    @FXML private ComboBox<String> ordenFilterCombo;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private TableView<Producto> tablaProductos;

    private final ProductoServicioI productoServicio = ProductoServicioImp.getInstance();
    private final CategoriaServicioI categoriaServicio = CategoriaServicioImp.getInstance();
    private final MarcaServicioI marcaServicio = MarcaServicioImp.getInstance();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    private final ObservableList<Producto> masterProductoList = FXCollections.observableArrayList();
    private FilteredList<Producto> filteredProductList;
    private SortedList<Producto> sortedProductList;

    @FXML
    public void initialize() {
        // Cargar datos
        masterProductoList.setAll(productoServicio.listActive());
        filteredProductList = new FilteredList<>(masterProductoList);
        sortedProductList = new SortedList<>(filteredProductList);
        tablaProductos.setItems(sortedProductList);

        // Configurar UI
        setupFilters();
        setupTable();
        setupListeners();
    }

    private void setupTable() {
        TableViewUtils.lockColumns(tablaProductos);

        TableColumn<Producto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Producto, String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMarca().getNombre()));
        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoria().getNombre()));
        TableColumn<Producto, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(moneyFormat.format(cellData.getValue().getPrecio())));

        tablaProductos.getColumns().setAll(colId, colNombre, colMarca, colCategoria, colPrecio);
    }

    private void setupFilters() {
        configureObjectComboBox(categoryComboBox, categoriaServicio.listActive(), "Todas");
        configureObjectComboBox(marcaFilterCombo, marcaServicio.listActive(), "Todas");
        ordenFilterCombo.getItems().setAll("Relevancia", "Precio: Menor a Mayor", "Precio: Mayor a Menor", "Nombre: A-Z", "Nombre: Z-A");
        ordenFilterCombo.getSelectionModel().selectFirst();

        minPriceField.setTextFormatter(new TextFormatter<>(ValidacionUtils.createDoubleFilter()));
        maxPriceField.setTextFormatter(new TextFormatter<>(ValidacionUtils.createDoubleFilter()));
    }

    private void setupListeners() {
        Runnable filterAction = this::applyFilters;
        searchField.textProperty().addListener((obs, o, n) -> filterAction.run());
        categoryComboBox.valueProperty().addListener((obs, o, n) -> filterAction.run());
        marcaFilterCombo.valueProperty().addListener((obs, o, n) -> filterAction.run());
        minPriceField.textProperty().addListener((obs, o, n) -> filterAction.run());
        maxPriceField.textProperty().addListener((obs, o, n) -> filterAction.run());

        ordenFilterCombo.valueProperty().addListener((obs, o, n) -> applySort());
    }

    private void applyFilters() {
        String texto = ValidacionUtils.normalizeForSearch(searchField.getText());
        Categoria cat = categoryComboBox.getValue();
        Marca mar = marcaFilterCombo.getValue();
        Double min = parseDouble(minPriceField.getText());
        Double max = parseDouble(maxPriceField.getText());

        filteredProductList.setPredicate(p -> {
            boolean txtOk = texto.isEmpty() || ValidacionUtils.normalizeForSearch(p.getNombre()).contains(texto);
            boolean catOk = cat == null || cat.equals(p.getCategoria());
            boolean marOk = mar == null || mar.equals(p.getMarca());
            boolean minOk = min == null || p.getPrecio() >= min;
            boolean maxOk = max == null || p.getPrecio() <= max;
            return txtOk && catOk && marOk && minOk && maxOk;
        });
    }

    private void applySort() {
        String n = ordenFilterCombo.getValue();
        if (n == null) return;
        Comparator<Producto> comparator = switch (n) {
            case "Precio: Menor a Mayor" -> Comparator.comparing(Producto::getPrecio);
            case "Precio: Mayor a Menor" -> Comparator.comparing(Producto::getPrecio).reversed();
            case "Nombre: A-Z" -> Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER);
            case "Nombre: Z-A" -> Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER).reversed();
            default -> null;
        };
        sortedProductList.setComparator(comparator);
    }

    private Double parseDouble(String text) { try { return Double.parseDouble(text); } catch (Exception e) { return null; } }
    private <T> void configureObjectComboBox(ComboBox<T> comboBox, List<T> items, String promptText) {
        ObservableList<T> observableItems = FXCollections.observableArrayList(items);
        observableItems.add(0, null);
        comboBox.setItems(observableItems);
        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? promptText : getObjectName(item));
            }
        });
        comboBox.setButtonCell(comboBox.getCellFactory().call(null));
        comboBox.getSelectionModel().selectFirst();
    }
    private String getObjectName(Object item) {
        try { return (String) item.getClass().getMethod("getNombre").invoke(item); }
        catch (Exception e) { return item.toString(); }
    }
}