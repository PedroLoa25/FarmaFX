package pe.edu.upeu.farmafx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import pe.edu.upeu.farmafx.modelo.Categoria;
import pe.edu.upeu.farmafx.modelo.Producto;
import pe.edu.upeu.farmafx.modelo.Promocion;
import pe.edu.upeu.farmafx.modelo.Usuario;
import pe.edu.upeu.farmafx.servicio.*;
import pe.edu.upeu.farmafx.utils.ValidacionUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ClienteMainController {

    // --- Elementos FXML ---
    @FXML private BorderPane mainContent;
    @FXML private VBox viewContainer; // Contenedor principal que cambiará de contenido
    @FXML private TextField searchField;
    @FXML private ComboBox<Categoria> categoryComboBox;
    @FXML private Button refreshButton;

    // --- Servicios y Helpers ---
    private final PromocionServicioI promocionServicio = PromocionServicioImp.getInstance();
    private final CategoriaServicioI categoriaServicio = CategoriaServicioImp.getInstance();
    private final ProductoServicioI productoServicio = ProductoServicioImp.getInstance();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    // --- Listas de datos ---
    private final ObservableList<Promocion> masterPromocionList = FXCollections.observableArrayList();
    private final ObservableList<Producto> masterProductoList = FXCollections.observableArrayList();
    private FilteredList<Producto> filteredProductList;

    // --- Nodos de las Vistas (para cambiar entre promociones y productos) ---
    private Node promocionesView;
    private Node productosView;

    // --- Variables de Estado ---
    private Usuario usuario;
    private Runnable onLogoutAction;
    private Runnable onExitAction;

    public void initData(Usuario usuario, Runnable onLogout, Runnable onExit) {
        this.usuario = usuario;
        this.onLogoutAction = onLogout;
        this.onExitAction = onExit;

        // Carga inicial de datos maestros
        masterPromocionList.setAll(promocionServicio.listActive());
        masterProductoList.setAll(productoServicio.listActive());
        filteredProductList = new FilteredList<>(masterProductoList, p -> true);

        // Crear las vistas y configurar los filtros
        crearVistaPromociones();
        crearVistaProductos();
        setupFilters();

        // Mostrar la vista de promociones por defecto
        mostrarVistaPromociones();
    }

    @FXML
    public void initialize() {
        // La inicialización se delega a initData
    }

    private void setupFilters() {
        // Cargar categorías en el ComboBox
        ObservableList<Categoria> categorias = FXCollections.observableArrayList(categoriaServicio.listActive());
        categorias.add(0, null); // "Todas las categorías"
        categoryComboBox.setItems(categorias);
        personalizarComboBoxCategorias(); // Llama al método que arregla el display

        // Listeners para activar el modo búsqueda
        searchField.textProperty().addListener((o, oldVal, newVal) -> aplicarFiltrosProductos());
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> aplicarFiltrosProductos());
    }

    // --- LÓGICA DE CAMBIO DE VISTAS ---

    private void mostrarVistaPromociones() {
        if (viewContainer.getChildren().size() > 1) {
            viewContainer.getChildren().remove(1);
        }
        viewContainer.getChildren().add(promocionesView);
    }

    private void mostrarVistaProductos() {
        if (viewContainer.getChildren().size() > 1) {
            viewContainer.getChildren().remove(1);
        }
        viewContainer.getChildren().add(productosView);
    }

    @FXML
    public void onRefresh() {
        // Limpia filtros y vuelve a la vista de promociones
        searchField.clear();
        categoryComboBox.getSelectionModel().selectFirst(); // Resetea a "Todas las categorías"
        mostrarVistaPromociones();
    }

    // --- LÓGICA DE FILTRADO DE PRODUCTOS ---

    private void aplicarFiltrosProductos() {
        String filtroBusqueda = ValidacionUtils.normalizeForSearch(searchField.getText());
        Categoria filtroCategoria = categoryComboBox.getValue();

        // Si ambos filtros están vacíos, volvemos a la vista de promociones
        if (filtroBusqueda.isEmpty() && filtroCategoria == null) {
            mostrarVistaPromociones();
            return;
        }

        // Si hay algún filtro, cambiamos a la vista de productos
        mostrarVistaProductos();

        filteredProductList.setPredicate(producto -> {
            boolean coincideBusqueda = filtroBusqueda.isEmpty() ||
                    ValidacionUtils.normalizeForSearch(producto.getNombre()).contains(filtroBusqueda);

            boolean coincideCategoria = filtroCategoria == null ||
                    filtroCategoria.equals(producto.getCategoria());

            return coincideBusqueda && coincideCategoria;
        });
    }

    // --- MÉTODOS PARA CREAR LAS VISTAS ---

    private void crearVistaPromociones() {
        VBox container = new VBox(15);
        VBox.setVgrow(container, javafx.scene.layout.Priority.ALWAYS);

        if (!masterPromocionList.isEmpty()) {
            VBox heroContainer = new VBox(); // Contenedor para la promo grande
            crearTarjetaHeroe(masterPromocionList.get(0), heroContainer);
            container.getChildren().add(heroContainer);

            if (masterPromocionList.size() > 1) {
                Label exploreLabel = new Label("Explorar más ofertas");
                exploreLabel.setFont(Font.font("System", FontWeight.BOLD, 18.0));

                TilePane promocionesTilePane = new TilePane(20, 20);
                promocionesTilePane.setPadding(new Insets(10, 0, 0, 0));
                for (int i = 1; i < masterPromocionList.size(); i++) {
                    promocionesTilePane.getChildren().add(crearTarjetaNormal(masterPromocionList.get(i)));
                }

                ScrollPane exploreScrollPane = new ScrollPane(promocionesTilePane);
                exploreScrollPane.setFitToWidth(true);
                exploreScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                exploreScrollPane.setStyle("-fx-background-color: transparent;");
                VBox.setVgrow(exploreScrollPane, javafx.scene.layout.Priority.ALWAYS);

                container.getChildren().addAll(new Separator(), exploreLabel, exploreScrollPane);
            }
        }
        this.promocionesView = container;
    }

    private void crearVistaProductos() {
        TableView<Producto> tablaProductos = new TableView<>(filteredProductList);
        VBox.setVgrow(tablaProductos, javafx.scene.layout.Priority.ALWAYS);

        // **COLUMNA ID AÑADIDA**
        TableColumn<Producto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colId.setPrefWidth(60);

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(350);

        TableColumn<Producto, String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getMarca().getNombre()));
        colMarca.setPrefWidth(150);

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getCategoria().getNombre()));
        colCategoria.setPrefWidth(150);

        TableColumn<Producto, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(moneyFormat.format(p.getValue().getPrecio())));
        colPrecio.setPrefWidth(100);

        tablaProductos.getColumns().setAll(colId, colNombre, colMarca, colCategoria, colPrecio);
        this.productosView = tablaProductos;
    }

    // --- MÉTODOS PARA CREAR TARJETAS (CÓDIGO COMPLETO) ---

    private void crearTarjetaHeroe(Promocion promocion, VBox container) {
        container.setAlignment(Pos.CENTER);
        container.setMinHeight(180.0);
        container.setStyle("-fx-background-color: linear-gradient(to right, #4A00E0, #8E2DE2); -fx-background-radius: 12;");
        container.setPadding(new Insets(20, 30, 20, 30));
        VBox.setMargin(container, new Insets(10, 0, 0, 0));

        Label nombreLabel = new Label(promocion.getNombre());
        nombreLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        nombreLabel.setTextFill(Color.WHITE);
        nombreLabel.setWrapText(true);
        nombreLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        TextFlow preciosFlow = new TextFlow();
        preciosFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Text precioOriginalText = new Text(moneyFormat.format(promocion.getPrecioOriginal()));
        precioOriginalText.setFont(Font.font(18));
        precioOriginalText.setFill(Color.LIGHTGRAY);
        precioOriginalText.setStrikethrough(true);

        Text precioOfertaText = new Text(" ¡AHORA! " + moneyFormat.format(promocion.getPrecioOferta()));
        precioOfertaText.setFont(Font.font("System", FontWeight.BOLD, 32));
        precioOfertaText.setFill(Color.WHITE);

        preciosFlow.getChildren().addAll(precioOriginalText, precioOfertaText);
        container.getChildren().addAll(nombreLabel, preciosFlow);
        VBox.setMargin(preciosFlow, new Insets(10, 0, 0, 0));
    }

    private VBox crearTarjetaNormal(Promocion promocion) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 180);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);");
        card.setPadding(new Insets(15));

        Label nombreLabel = new Label(promocion.getNombre());
        nombreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nombreLabel.setWrapText(true);
        nombreLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        VBox.setVgrow(nombreLabel, javafx.scene.layout.Priority.ALWAYS);
        nombreLabel.setMaxHeight(Double.MAX_VALUE);
        nombreLabel.setAlignment(Pos.CENTER);

        Text precioOriginalText = new Text(moneyFormat.format(promocion.getPrecioOriginal()));
        precioOriginalText.setStrikethrough(true);
        precioOriginalText.setFill(Color.GRAY);

        Label precioOfertaLabel = new Label(moneyFormat.format(promocion.getPrecioOferta()));
        precioOfertaLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        precioOfertaLabel.setTextFill(Color.web("#d32f2f"));

        card.getChildren().addAll(nombreLabel, precioOriginalText, precioOfertaLabel);
        return card;
    }

    private void personalizarComboBoxCategorias() {
        // Esta es la parte clave para que el ComboBox muestre el nombre y no el package
        Callback<ListView<Categoria>, ListCell<Categoria>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Todas las categorías" : item.getNombre());
            }
        };
        categoryComboBox.setCellFactory(cellFactory);
        categoryComboBox.setButtonCell(cellFactory.call(null));
    }

    // --- Métodos de Acción del Menú ---
    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas cerrar sesión?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (onLogoutAction != null) onLogoutAction.run();
        }
    }

    @FXML
    public void onAbout() {
        new Alert(Alert.AlertType.INFORMATION, "FarmaFX\nVersión 0.1\nDesarrollado por: PedroLoa25").showAndWait();
    }

    @FXML
    public void onExit() {
        if (onExitAction != null) {
            onExitAction.run();
        }
    }
}