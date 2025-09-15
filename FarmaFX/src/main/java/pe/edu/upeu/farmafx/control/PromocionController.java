package pe.edu.upeu.farmafx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import pe.edu.upeu.farmafx.enums.Estado;
import pe.edu.upeu.farmafx.modelo.Promocion;
import pe.edu.upeu.farmafx.servicio.PromocionServicioI;
import pe.edu.upeu.farmafx.servicio.PromocionServicioImp;
import pe.edu.upeu.farmafx.utils.TableViewUtils;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class PromocionController {

    // Filtros
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> estadoFilter;
    @FXML private ComboBox<String> ordenCombo;
    @FXML private Button refreshBtn;

    // Formulario
    @FXML private TextField nombreField;
    @FXML private TextField precioOriginalField;
    @FXML private TextField precioOfertaField;
    @FXML private CheckBox activoCheck;
    @FXML private Button nuevoBtn;
    @FXML private Button guardarBtn;

    // Tabla
    @FXML private TableView<Promocion> tabla;
    @FXML private TableColumn<Promocion, Integer> colId;
    @FXML private TableColumn<Promocion, String> colNombre;
    @FXML private TableColumn<Promocion, String> colPrecioOriginal;
    @FXML private TableColumn<Promocion, String> colPrecioOferta;
    @FXML private TableColumn<Promocion, String> colEstado;

    private AdminMainController mainController;

    private final PromocionServicioI servicio = PromocionServicioImp.getInstance();
    private final ObservableList<Promocion> data = FXCollections.observableArrayList();
    private FilteredList<Promocion> filtered;

    private Integer editingId = null;
    private enum Mode { NONE, CREAR, EDITAR }
    private Mode mode = Mode.NONE;

    public void setMainController(AdminMainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        precioOriginalField.setTextFormatter(new TextFormatter<>(ValidacionUtils.createDoubleFilter()));
        precioOfertaField.setTextFormatter(new TextFormatter<>(ValidacionUtils.createDoubleFilter()));

        // Tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idPromocion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecioOriginal.setCellValueFactory(c -> new ReadOnlyStringWrapper(money.format(c.getValue().getPrecioOriginal())));
        colPrecioOferta.setCellValueFactory(c -> new ReadOnlyStringWrapper(money.format(c.getValue().getPrecioOferta())));
        colEstado.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEstado().isActivo() ? "ACTIVO" : "INACTIVO"));

        filtered = new FilteredList<>(data, it -> true);
        tabla.setItems(filtered);
        TableViewUtils.lockColumns(tabla);

        // Filtros
        estadoFilter.getItems().setAll("Todos", "Activos", "Inactivos");
        ordenCombo.getItems().setAll("ID ↑", "ID ↓", "Nombre ↑", "Nombre ↓", "Precio Oferta ↑", "Precio Oferta ↓");

        searchField.textProperty().addListener((obs, a, b) -> applyFilters());
        estadoFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyFilters());
        ordenCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applySort());
        refreshBtn.setOnAction(e -> onRefresh());

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
        precioOriginalField.textProperty().addListener((o, a, b) -> validarFormulario());
        precioOfertaField.textProperty().addListener((o, a, b) -> validarFormulario());
        activoCheck.selectedProperty().addListener((o, a, b) -> validarFormulario());

        onRefresh();
    }

    private void validarFormulario() {
        boolean modoActivo = mode != Mode.NONE;
        String n = ValidacionUtils.normalizeBasic(nombreField.getText());
        boolean okNombre = n.length() >= 5 && n.length() <= 100;
        boolean okPrecioOrig = ValidacionUtils.isPositiveDouble(precioOriginalField.getText());
        boolean okPrecioOfer = ValidacionUtils.isPositiveDouble(precioOfertaField.getText());
        boolean habilitar = modoActivo && okNombre && okPrecioOrig && okPrecioOfer;
        guardarBtn.setDisable(!habilitar);
    }

    private void applyFilters() {
        final String q = ValidacionUtils.normalizeForSearch(searchField.getText());
        final String estadoSel = estadoFilter.getValue();

        filtered.setPredicate(p -> {
            if (p == null) return false;
            if (Objects.equals(estadoSel, "Activos") && !p.getEstado().isActivo()) return false;
            if (Objects.equals(estadoSel, "Inactivos") && p.getEstado().isActivo()) return false;
            if (q.isBlank()) return true;

            String nom = ValidacionUtils.normalizeForSearch(p.getNombre());
            return nom.contains(q);
        });
    }

    private void applySort() {
        String sel = ordenCombo.getValue();
        if (sel == null) sel = "ID ↑";
        switch (sel) {
            case "ID ↓" -> data.sort(Comparator.comparing(Promocion::getIdPromocion).reversed());
            case "Nombre ↑" -> data.sort(Comparator.comparing(Promocion::getNombre, String.CASE_INSENSITIVE_ORDER));
            case "Nombre ↓" -> data.sort(Comparator.comparing(Promocion::getNombre, String.CASE_INSENSITIVE_ORDER).reversed());
            case "Precio Oferta ↑" -> data.sort(Comparator.comparingDouble(Promocion::getPrecioOferta));
            case "Precio Oferta ↓" -> data.sort(Comparator.comparingDouble(Promocion::getPrecioOferta).reversed());
            default -> data.sort(Comparator.comparing(Promocion::getIdPromocion));
        }
    }

    @FXML
    public void onRefresh() {
        if (searchField != null) searchField.clear();
        if (estadoFilter != null) estadoFilter.setValue("Todos");
        if (ordenCombo != null) ordenCombo.setValue("ID ↑");

        data.setAll(servicio.listAll());
        data.sort(Comparator.comparing(Promocion::getIdPromocion));
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
            double precioOrig = Double.parseDouble(safeTrim(precioOriginalField.getText()));
            double precioOfer = Double.parseDouble(safeTrim(precioOfertaField.getText()));
            Estado estado = (activoCheck.isSelected() ? Estado.ACTIVO : Estado.INACTIVO);

            if (mode == Mode.CREAR) {
                servicio.create(nombre, precioOrig, precioOfer);
            } else if (mode == Mode.EDITAR) {
                if (editingId == null) throw new Exception("No hay selección para editar.");
                servicio.update(editingId, nombre, precioOrig, precioOfer, estado);
            } else {
                return;
            }
            alert(Alert.AlertType.INFORMATION, "Promoción guardada correctamente.");
            onRefresh();
        } catch (NumberFormatException nfe) {
            alert(Alert.AlertType.WARNING, "Verifica los valores numéricos de los precios.");
        } catch (Exception e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void loadToForm(Promocion p) {
        editingId = p.getIdPromocion();
        nombreField.setText(p.getNombre());
        precioOriginalField.setText(String.valueOf(p.getPrecioOriginal()));
        precioOfertaField.setText(String.valueOf(p.getPrecioOferta()));
        activoCheck.setSelected(p.getEstado().isActivo());
    }



    private void clearForm() {
        editingId = null;
        if (nombreField != null) nombreField.clear();
        if (precioOriginalField != null) precioOriginalField.clear();
        if (precioOfertaField != null) precioOfertaField.clear();
        if (activoCheck != null) activoCheck.setSelected(true);
        mode = Mode.NONE;
        validarFormulario();
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