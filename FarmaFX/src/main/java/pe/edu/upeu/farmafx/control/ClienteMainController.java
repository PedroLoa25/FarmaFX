package pe.edu.upeu.farmafx.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import pe.edu.upeu.farmafx.modelo.Usuario;

import java.util.Optional;

public class ClienteMainController {

    @FXML private BorderPane root;
    @FXML private Label dniLabel;
    @FXML private Label rolLabel;

    private javafx.scene.Node homeContent;
    private Runnable onLogoutAction;
    private Runnable onExitAction;

    /**
     * Inicializa los datos del controlador después de cargarlo.
     * @param usuario El usuario que ha iniciado sesión.
     * @param onLogout La acción a ejecutar al cerrar sesión.
     * @param onExit La acción a ejecutar al salir de la aplicación.
     */
    public void initData(Usuario usuario, Runnable onLogout, Runnable onExit) {
        this.onLogoutAction = onLogout;
        this.onExitAction = onExit;

        if (dniLabel != null) dniLabel.setText(usuario.getDni());
        if (rolLabel != null) rolLabel.setText(String.valueOf(usuario.getRol()));
    }

    @FXML
    public void initialize() {
        if (root != null) homeContent = root.getCenter();
    }

    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Deseas cerrar sesión?");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            if (onLogoutAction != null) onLogoutAction.run();
        }
    }

    @FXML
    public void onExit() {
        if (onExitAction != null) onExitAction.run();
    }

    @FXML
    public void onAbout() {
        new Alert(Alert.AlertType.INFORMATION, "FarmaFX\nVersión 0.1\nDesarrollado por: PedroLoa25").showAndWait();
    }

    @FXML public void onOpenCategorias() { loadInCenter("/fxml/categorias.fxml"); }
    @FXML public void onOpenMarcas() { loadInCenter("/fxml/marcas.fxml"); }
    @FXML public void onOpenProductos() { loadInCenter("/fxml/productos.fxml"); }

    private void loadInCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            Object ctl = loader.getController();
            if (ctl instanceof SupportsClose sc) {
                sc.setOnClose(() -> root.setCenter(homeContent));
            }
            root.setCenter(view);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar la vista:\n" + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }
}