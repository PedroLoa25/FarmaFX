package pe.edu.upeu.farmafx.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pe.edu.upeu.farmafx.enums.RolUsuario;
import pe.edu.upeu.farmafx.modelo.Usuario;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane root;   // BorderPane del layout raíz (main.fxml)
    @FXML private MenuBar menuBar;   // Menú superior

    @FXML
    public void initialize() {

    }

    public void postInit() {
        showLogin();
    }

    private void setMenusVisible(boolean visible) {
        if (menuBar != null) {
            menuBar.setVisible(visible);
            menuBar.setManaged(visible);
        }
    }

    // Carga un FXML y lo coloca en el centro. Devuelve el FXMLLoader para acceder a root y controller.
    private FXMLLoader loadIntoCenter(String fxmlPath) {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = fx.load();
            root.setCenter(view);

            // Si el controlador soporta "Salir", enlaza para volver a Catálogo
            Object ctrl = fx.getController();
            if (ctrl instanceof SupportsClose sc) {
                sc.setOnClose(this::showCatalog);
            }

            return fx;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar la vista: " + fxmlPath, e);
        }
    }

    // Pantallas
    private void showLogin() {
        try {
            // Carga la vista de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();

            // Asigna el callback para cuando el login sea exitoso
            LoginController loginController = loader.getController();
            loginController.setOnLoginSuccess(this::showHome);

            // Reemplaza el contenido de la ventana actual con la vista de login
            root.getScene().setRoot(loginView);

            // Ajusta el tamaño de la ventana para el login
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("FarmaFX - Login");
            stage.setResizable(false);
            stage.setMaximized(false); // Nos aseguramos de que no esté maximizada
            stage.sizeToScene();       // Ajusta el tamaño de la ventana al contenido
            stage.centerOnScreen();    // Centra la ventana

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar la vista de login:\n" + e.getMessage()).showAndWait();
        }
    }

    private void showHome(Usuario u) {
        try {
            String fxml = (u.getRol() == RolUsuario.ADMIN)
                    ? "/fxml/main_admin.fxml"
                    : "/fxml/main_cliente.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent homeView = loader.load();

            // Pasa los datos del usuario al controlador correspondiente
            Object controller = loader.getController();
            if (controller instanceof AdminMainController amc) {
                amc.setUsuario(u);
                // Asigna la acción de logout para volver al login
                amc.setOnLogoutAction(this::showLogin);
            } else if (controller instanceof ClienteMainController cmc) {
                cmc.setUsuario(u);
                // Asigna la acción de logout para volver al login
                cmc.setOnLogoutAction(this::showLogin);
            }

            // Reemplaza el contenido de la ventana con la vista principal
            root.getScene().setRoot(homeView);

            // Ajusta el tamaño de la ventana para la vista principal
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("FarmaFX");
            stage.setResizable(true);
            stage.setMaximized(true); // ¡Ahora sí la maximizamos!
            stage.centerOnScreen();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la vista principal:\n" + e.getMessage()).showAndWait();
        }
    }

    public void showCatalog() {
        setMenusVisible(true);
        FXMLLoader fx = loadIntoCenter("/fxml/catalogo.fxml");

        // Conectar botones del catálogo (no requiere controller propio)
        Parent view = fx.getRoot();
        Button btnCategorias = (Button) view.lookup("#btnCategorias");
        Button btnMarcas     = (Button) view.lookup("#btnMarcas");
        Button btnProductos  = (Button) view.lookup("#btnProductos");

        if (btnCategorias != null) btnCategorias.setOnAction(e -> showCategorias());
        if (btnMarcas     != null) btnMarcas.setOnAction(e -> showMarcas());
        if (btnProductos  != null) btnProductos.setOnAction(e -> showProductos());
    }

    public void showCategorias() {
        setMenusVisible(false);
        loadIntoCenter("/fxml/categorias.fxml");
    }

    public void showMarcas() {
        setMenusVisible(false);
        loadIntoCenter("/fxml/marcas.fxml");
    }

    public void showProductos() {
        setMenusVisible(false);
        loadIntoCenter("/fxml/productos.fxml");
    }

    // Menú
    @FXML private void onMenuCatalogo() { showCatalog(); }
    @FXML private void onMenuCerrarSesion() { showLogin(); }
    @FXML private void onMenuAcercaDe() {
        new Alert(Alert.AlertType.INFORMATION, "FarmaFX\nVersión 1.0").showAndWait();
    }
}