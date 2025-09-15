package pe.edu.upeu.farmafx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.servicio.CategoriaServicioI;
import pe.edu.upeu.farmafx.servicio.CategoriaServicioImp;
import pe.edu.upeu.farmafx.utils.TableViewUtils;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.util.Comparator;
import java.util.Objects;

public class CategoriaController implements SupportsClose {

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> estadoFilter;   // "Todos", "Activos", "Inactivos"
    @FXML private ComboBox<String> ordenCombo;      // "ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓"
    @FXML private Button refreshBtn;

    @FXML private TextField nombreField;
    @FXML private CheckBox activoCheck;
    @FXML private Button nuevoBtn;
    @FXML private Button guardarBtn;
    @FXML private Button salirBtn;

    @FXML private TableView<Categoria> tabla;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colEstado;

    private final CategoriaServicioI servicio = CategoriaServicioImp.getInstance();
    private final ObservableList<Categoria> data = FXCollections.observableArrayList();
    private FilteredList<Categoria> filtered;

    private Integer selectedId = null;
    private enum Mode { NONE, CREAR, EDITAR }
    private Mode mode = Mode.NONE;

    private Runnable onClose = () -> {};
    @Override public void setOnClose(Runnable r) { this.onClose = (r != null) ? r : () -> {}; }

    @FXML
    public void initialize() {
        // Columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getEstado().isActivo() ? "ACTIVO" : "INACTIVO"
        ));

        // Items: FilteredList (sin SortedList)
        filtered = new FilteredList<>(data, it -> true);
        tabla.setItems(filtered);

        // Bloquear cabeceras (orden/reorden) con helper general
        TableViewUtils.lockColumns(tabla);

        // Selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                mode = Mode.EDITAR;
                selectedId = sel.getIdCategoria();
                nombreField.setText(sel.getNombre());
                activoCheck.setSelected(sel.getEstado().isActivo());
            }
            validarFormulario();
        });

        // Validación
        guardarBtn.setDisable(true);
        nombreField.textProperty().addListener((o, a, b) -> validarFormulario());
        activoCheck.selectedProperty().addListener((o, a, b) -> validarFormulario());

        // Filtros UI
        estadoFilter.getItems().setAll("Todos", "Activos", "Inactivos");
        estadoFilter.setValue("Todos");
        estadoFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());

        ordenCombo.getItems().setAll("ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓");
        ordenCombo.setValue("ID ↑");
        ordenCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applySort());

        // Búsqueda
        searchField.textProperty().addListener((obs, a, b) -> applyFilters());

        // Refresh
        refreshBtn.setOnAction(e -> onRefresh());

        // Cargar datos
        onRefresh();
    }

    private void validarFormulario() {
        String n = ValidacionUtils.normalizeBasic(nombreField.getText());
        boolean okNombre = n.length() >= 3 && n.length() <= 50;
        boolean modoActivo = mode != Mode.NONE;
        guardarBtn.setDisable(!(okNombre && modoActivo));
    }

    private void applyFilters() {
        final String q = ValidacionUtils.normalizeForSearch(searchField.getText());
        final String estadoSel = estadoFilter.getValue();

        filtered.setPredicate(cat -> {
            if (cat == null) return false;

            // Filtro estado
            if (Objects.equals(estadoSel, "Activos") && !cat.getEstado().isActivo()) return false;
            if (Objects.equals(estadoSel, "Inactivos") && cat.getEstado().isActivo()) return false;

            // Búsqueda por nombre (contiene, sin tildes/símbolos)
            String nom = ValidacionUtils.normalizeForSearch(cat.getNombre());
            return q.isBlank() || nom.contains(q);
        });
    }

    private void applySort() {
        String sel = ordenCombo.getValue();
        if (sel == null) sel = "ID ↑";
        switch (sel) {
            case "ID ↓" -> data.sort(Comparator.comparing(Categoria::getIdCategoria).reversed());
            case "Nombre ↑" -> data.sort(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER));
            case "Nombre ↓" -> data.sort(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER).reversed());
            default -> data.sort(Comparator.comparing(Categoria::getIdCategoria)); // "ID ↑"
        }
    }

    @FXML
    public void onNuevo() {
        clearForm();
        mode = Mode.CREAR;
        validarFormulario();
        nombreField.requestFocus();
    }

    @FXML
    public void onGuardar() {
        String nombre = ValidacionUtils.normalizeBasic(nombreField.getText());
        Estado estado = activoCheck.isSelected() ? Estado.ACTIVO : Estado.INACTIVO;

        try {
            ValidacionUtils.validarNombre(nombre, 3, 50);
            if (mode == Mode.CREAR) {
                // Crear respetando el check
                Categoria nueva = servicio.create(nombre);
                if (!activoCheck.isSelected()) {
                    servicio.toggleEstado(nueva.getIdCategoria());
                }
                new Alert(Alert.AlertType.INFORMATION, "Categoría creada").showAndWait();
            } else if (mode == Mode.EDITAR) {
                if (selectedId == null) throw new Exception("No hay selección para editar.");
                servicio.update(selectedId, nombre, estado);
                new Alert(Alert.AlertType.INFORMATION, "Categoría actualizada").showAndWait();
            } else {
                return;
            }
            clearForm();
            onRefresh();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void onRefresh() {
        // Limpiar búsqueda y filtros
        if (searchField != null) searchField.clear();
        if (estadoFilter != null) estadoFilter.setValue("Todos");
        if (ordenCombo != null) ordenCombo.setValue("ID ↑");

        // Recargar datos
        data.setAll(servicio.listAll());

        // Orden por defecto
        data.sort(Comparator.comparing(Categoria::getIdCategoria));

        // Reaplicar filtros (estado/búsqueda)
        applyFilters();

        tabla.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        nombreField.clear();
        activoCheck.setSelected(true);
        tabla.getSelectionModel().clearSelection();
        selectedId = null;
        mode = Mode.NONE;
        validarFormulario();
    }

    @FXML
    public void onSalir() {
        onClose.run();
    }
}