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

public class ProductoController {

    // Filtros
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> estadoFilter;
    // --- INICIO DE CAMBIOS ---
    @FXML private ComboBox<Marca> marcaFilterCombo;
    @FXML private ComboBox<Categoria> categoriaFilterCombo;
    // --- FIN DE CAMBIOS ---
    @FXML private ComboBox<String> ordenCombo;
    @FXML private Button refreshBtn;

    // Formulario
    @FXML private TextField nombreField;
    @FXML private TextField precioField;
    @FXML private TextField stockField;
    @FXML private ComboBox<Marca> marcaCombo;
    @FXML private ComboBox<Categoria> categoriaCombo;
    @FXML private CheckBox activoCheck;
    @FXML private Button nuevoBtn;
    @FXML private Button guardarBtn;

    // Tabla
    @FXML private TableView<Producto> tabla;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colNombre;
    @FXML private TableColumn<Producto, String>  colMarca;
    @FXML private TableColumn<Producto, String>  colCategoria;
    @FXML private TableColumn<Producto, String>  colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String>  colEstado;

    private AdminMainController mainController;

    private final ProductoServicioI servicio = ProductoServicioImp.getInstance();
    private final MarcaServicioI marcaServicio = MarcaServicioImp.getInstance();
    private final CategoriaServicioI categoriaServicio = CategoriaServicioImp.getInstance();

    private final ObservableList<Producto> data = FXCollections.observableArrayList();
    private FilteredList<Producto> filtered;

    private Integer editingId = null;
    private enum Mode { NONE, CREAR, EDITAR }
    private Mode mode = Mode.NONE;

    public void setMainController(AdminMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        precioField.setTextFormatter(new TextFormatter<>(ValidacionUtils.createDoubleFilter()));
        stockField.setTextFormatter(new TextFormatter<>(ValidacionUtils.createIntegerFilter()));

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
        ordenCombo.getItems().setAll("ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓", "Precio ↑", "Precio ↓", "Stock ↑", "Stock ↓");

        searchField.textProperty().addListener((obs, a, b) -> applyFilters());
        estadoFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());
        ordenCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applySort());
        refreshBtn.setOnAction(e -> onRefresh());

        // --- INICIO DE CAMBIOS ---
        // Configurar ComboBox de filtros (Marca y Categoría)
        setupFilterComboBoxes();
        marcaFilterCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());
        categoriaFilterCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());
        // --- FIN DE CAMBIOS ---

        // Formulario: combos de marca y categoría
        setupFormComboBoxes();

        // Selección en tabla => modo EDITAR
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, a, sel) -> {
            if (sel == null) {
                clearForm();
            } else {
                mode = Mode.EDITAR;
                loadToForm(sel);
            }
            validarFormulario();
        });

        // Validación de formulario y estado del botón Guardar
        guardarBtn.setDisable(true);
        nombreField.textProperty().addListener((o, a, b) -> validarFormulario());
        precioField.textProperty().addListener((o, a, b) -> validarFormulario());
        stockField.textProperty().addListener((o, a, b) -> validarFormulario());
        marcaCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> validarFormulario());
        categoriaCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> validarFormulario());
        activoCheck.selectedProperty().addListener((o, a, b) -> validarFormulario());

        // Cargar datos iniciales
        onRefresh();
    }

    private void validarFormulario() {
        boolean modoActivo = mode != Mode.NONE;
        String n = ValidacionUtils.normalizeBasic(nombreField.getText());
        boolean okNombre = n.length() >= 3 && n.length() <= 50;
        boolean okPrecio = ValidacionUtils.isPositiveDouble(precioField.getText());
        boolean okStock = ValidacionUtils.isPositiveInteger(stockField.getText());
        boolean okMarca = marcaCombo.getValue() != null;
        boolean okCategoria = categoriaCombo.getValue() != null;
        boolean habilitar = modoActivo && okNombre && okPrecio && okStock && okMarca && okCategoria;
        guardarBtn.setDisable(!habilitar);
    }

    private void applyFilters() {
        final String q = ValidacionUtils.normalizeForSearch(searchField.getText());
        final String estadoSel = estadoFilter.getValue();
        // --- INICIO DE CAMBIOS ---
        final Marca marcaSel = marcaFilterCombo.getValue();
        final Categoria catSel = categoriaFilterCombo.getValue();
        // --- FIN DE CAMBIOS ---

        filtered.setPredicate(p -> {
            if (p == null) return false;

            // Filtro estado
            if (Objects.equals(estadoSel, "Activos") && !p.getEstado().isActivo()) return false;
            if (Objects.equals(estadoSel, "Inactivos") && p.getEstado().isActivo()) return false;

            // --- INICIO DE CAMBIOS ---
            // Filtro Marca (si hay una marca seleccionada, el producto debe tener esa marca)
            if (marcaSel != null && !marcaSel.equals(p.getMarca())) return false;
            // Filtro Categoría
            if (catSel != null && !catSel.equals(p.getCategoria())) return false;
            // --- FIN DE CAMBIOS ---

            // Búsqueda por texto
            String nom = ValidacionUtils.normalizeForSearch(p.getNombre());
            String mar = ValidacionUtils.normalizeForSearch(p.getMarca() != null ? p.getMarca().getNombre() : "");
            String cat = ValidacionUtils.normalizeForSearch(p.getCategoria() != null ? p.getCategoria().getNombre() : "");

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

    @FXML
    public void onRefresh() {
        // Recargar combos (por si cambian marcas/categorías activas)
        // --- INICIO DE CAMBIOS ---
        // Ahora se recargan tanto los combos de formulario como los de filtro
        ObservableList<Marca> marcasActivas = FXCollections.observableArrayList(marcaServicio.listActive());
        ObservableList<Categoria> categoriasActivas = FXCollections.observableArrayList(categoriaServicio.listActive());

        marcaCombo.setItems(marcasActivas);
        categoriaCombo.setItems(categoriasActivas);

        ObservableList<Marca> todasMarcas = FXCollections.observableArrayList(marcaServicio.listAll());
        todasMarcas.add(0, null); // Opción "Todas"
        marcaFilterCombo.setItems(todasMarcas);

        ObservableList<Categoria> todasCategorias = FXCollections.observableArrayList(categoriaServicio.listAll());
        todasCategorias.add(0, null); // Opción "Todas"
        categoriaFilterCombo.setItems(todasCategorias);
        // --- FIN DE CAMBIOS ---

        // Limpiar filtros
        if (searchField != null) searchField.clear();
        if (estadoFilter != null) estadoFilter.setValue("Todos");
        if (ordenCombo != null) ordenCombo.setValue("ID ↑");
        // --- INICIO DE CAMBIOS ---
        if (marcaFilterCombo != null) marcaFilterCombo.setValue(null); // Resetea a "Todas"
        if (categoriaFilterCombo != null) categoriaFilterCombo.setValue(null); // Resetea a "Todas"
        // --- FIN DE CAMBIOS ---

        // Recargar datos
        data.setAll(servicio.listAll());
        data.sort(Comparator.comparing(Producto::getIdProducto));
        applyFilters();

        tabla.getSelectionModel().clearSelection();
        clearForm();
    }

    @FXML private void onNuevo() {
        tabla.getSelectionModel().clearSelection();
        clearForm();
        mode = Mode.CREAR;
        validarFormulario();
        nombreField.requestFocus();
    }

    @FXML private void onGuardar() {
        try {
            String nombre = safeTrim(nombreField.getText());
            double precio = Double.parseDouble(safeTrim(precioField.getText()));
            int stock = Integer.parseInt(safeTrim(stockField.getText()));
            Marca marca = marcaCombo.getValue();
            Categoria categoria = categoriaCombo.getValue();
            Estado estado = (activoCheck.isSelected() ? Estado.ACTIVO : Estado.INACTIVO);

            if (nombre.isEmpty()) { throw new Exception("Ingresa el nombre del producto."); }
            if (marca == null) { throw new Exception("Selecciona una marca."); }
            if (categoria == null) { throw new Exception("Selecciona una categoría."); }

            if (mode == Mode.CREAR) {
                Producto nuevo = servicio.create(nombre, precio, stock, marca, categoria);
                if (!activoCheck.isSelected()) {
                    servicio.toggleEstado(nuevo.getIdProducto());
                }
                alert(Alert.AlertType.INFORMATION, "Producto creado correctamente.");
            } else if (mode == Mode.EDITAR) {
                if (editingId == null) { throw new Exception("No hay selección para editar."); }
                servicio.update(editingId, nombre, precio, stock, estado, marca, categoria);
                alert(Alert.AlertType.INFORMATION, "Producto actualizado correctamente.");
            } else {
                return;
            }
            onRefresh();
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
        if (p.getMarca() != null) marcaCombo.getSelectionModel().select(p.getMarca());
        else marcaCombo.getSelectionModel().clearSelection();
        if (p.getCategoria() != null) categoriaCombo.getSelectionModel().select(p.getCategoria());
        else categoriaCombo.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        editingId = null;
        if (nombreField != null) nombreField.clear();
        if (precioField != null) precioField.clear();
        if (stockField != null) stockField.clear();
        if (activoCheck != null) activoCheck.setSelected(true);
        if (marcaCombo != null) marcaCombo.getSelectionModel().clearSelection();
        if (categoriaCombo != null) categoriaCombo.getSelectionModel().clearSelection();
        mode = Mode.NONE;
        validarFormulario();
    }

    // --- INICIO DE CAMBIOS ---
    private void setupFilterComboBoxes() {
        // Marca
        marcaFilterCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todas las Marcas" : item.getNombre());
            }
        });
        marcaFilterCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Marca item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todas las Marcas" : item.getNombre());
            }
        });

        // Categoría
        categoriaFilterCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todas las Categorías" : item.getNombre());
            }
        });
        categoriaFilterCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todas las Categorías" : item.getNombre());
            }
        });
    }
    // --- FIN DE CAMBIOS ---

    private void setupFormComboBoxes() {
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
    }

    private static String safeTrim(String s) { return s == null ? "" : s.trim(); }
    private void alert(Alert.AlertType type, String msg) { new Alert(type, msg).showAndWait(); }
    @FXML
    public void onSalir() {
        // 5. MODIFICA ESTE MÉTODO
        if (mainController != null) {
            mainController.restoreHomeContent();
        }
    }
}