package pe.edu.upeu.farmafx.control;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import pe.edu.upeu.farmafx.modelo.Promocion;
import pe.edu.upeu.farmafx.modelo.Usuario; // Importa la clase Usuario
import pe.edu.upeu.farmafx.servicio.PromocionServicioI;
import pe.edu.upeu.farmafx.servicio.PromocionServicioImp;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ClienteMainController { // Quitamos "implements SupportsClose" porque initData lo reemplaza

    @FXML private BorderPane mainContent;
    @FXML private TilePane promocionesContainer;

    // Servicios y helpers
    private final PromocionServicioI promocionServicio = PromocionServicioImp.getInstance();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    // Variables de estado
    private Usuario usuario;
    private Runnable onLogoutAction;
    private Runnable onExitAction;

    // --- MÉTODO CORREGIDO ---
    // Este es el método que borré por error. Ahora gestiona todo.
    public void initData(Usuario usuario, Runnable onLogout, Runnable onExit) {
        this.usuario = usuario;
        this.onLogoutAction = onLogout;
        this.onExitAction = onExit;

        // Ahora que tenemos los datos, cargamos las promociones
        cargarPromociones();
    }
    // --- FIN DE CORRECCIÓN ---

    @FXML
    public void initialize() {
        // El método initialize ahora puede estar vacío, ya que initData()
        // se encarga de arrancar la lógica principal.
    }

    private void cargarPromociones() {
        if (promocionesContainer == null) return;

        promocionesContainer.getChildren().clear();
        List<Promocion> promocionesActivas = promocionServicio.listActive();

        for (Promocion promo : promocionesActivas) {
            VBox card = crearTarjetaPromocion(promo);
            promocionesContainer.getChildren().add(card);
        }
    }

    private VBox crearTarjetaPromocion(Promocion promocion) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 160);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 2);");
        card.setPadding(new javafx.geometry.Insets(15));

        Label nombreLabel = new Label(promocion.getNombre());
        nombreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nombreLabel.setWrapText(true);
        nombreLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Text precioOriginalText = new Text(moneyFormat.format(promocion.getPrecioOriginal()));
        precioOriginalText.setStrikethrough(true);
        precioOriginalText.setFill(Color.GRAY);
        precioOriginalText.setFont(Font.font(12));

        Label precioOfertaLabel = new Label(moneyFormat.format(promocion.getPrecioOferta()));
        precioOfertaLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        precioOfertaLabel.setTextFill(Color.web("#d32f2f"));

        card.getChildren().addAll(nombreLabel, precioOriginalText, precioOfertaLabel);
        return card;
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