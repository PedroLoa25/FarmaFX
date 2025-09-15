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
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane root;   // BorderPane del layout raíz (main.fxml)
    @FXML private MenuBar menuBar;   // Menú superior

    @FXML
    public void initialize() {
        // Arranca en Login
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
    public void showLogin() {
        setMenusVisible(false);
        FXMLLoader fx = loadIntoCenter("/fxml/login.fxml");

        // Enlazar callback de éxito de login si el controller lo expone
        Object ctrl = fx.getController();
        if (ctrl instanceof LoginController lc) {
            lc.setOnLoginSuccess(this::showHome);
        } else {
            try {
                ctrl.getClass().getMethod("setOnLoginSuccess", Runnable.class)
                        .invoke(ctrl, (Runnable) this::showCatalog);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ex) {
                throw new RuntimeException("No se pudo enlazar login success", ex);
            }
        }

        // Asegura que el login NO esté en pantalla completa (F11)
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setFullScreen(false);   // sale de "pantalla completa"
            stage.setMaximized(true);     // queda "ventana completa"
        });
    }

    // Decide home según rol y reemplaza el root de la Scene
    private void showHome(Usuario u) {
        try {
            String fxml = (u.getRol() == RolUsuario.ADMIN)
                    ? "/fxml/main_admin.fxml"
                    : "/fxml/main_cliente.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();

            Object c = loader.getController();
            if (c instanceof AdminMainController amc) {
                amc.setUsuario(u);
            } else if (c instanceof ClienteMainController cmc) {
                cmc.setUsuario(u);
            }

            // Reemplazar el root en el mismo Stage
            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(view);

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