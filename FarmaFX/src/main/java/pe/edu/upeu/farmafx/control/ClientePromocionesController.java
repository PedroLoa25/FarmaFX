package pe.edu.upeu.farmafx.control;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pe.edu.upeu.farmafx.modelo.Promocion;
import pe.edu.upeu.farmafx.servicio.PromocionServicioI;
import pe.edu.upeu.farmafx.servicio.PromocionServicioImp;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ClientePromocionesController {

    @FXML
    private VBox promocionesContainer;

    private final PromocionServicioI promocionServicio = PromocionServicioImp.getInstance();
    private final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    @FXML
    public void initialize() {
        crearVistaPromociones();
    }

    private void crearVistaPromociones() {
        List<Promocion> promociones = promocionServicio.listActive();

        if (!promociones.isEmpty()) {
            VBox heroContainer = new VBox();
            crearTarjetaHeroe(promociones.get(0), heroContainer);
            promocionesContainer.getChildren().add(heroContainer);

            if (promociones.size() > 1) {
                Label exploreLabel = new Label("Explorar más ofertas");
                exploreLabel.setFont(Font.font("System", FontWeight.BOLD, 18.0));

                TilePane promocionesTilePane = new TilePane(20, 20);
                promocionesTilePane.setPadding(new Insets(10, 0, 0, 0));
                for (int i = 1; i < promociones.size(); i++) {
                    promocionesTilePane.getChildren().add(crearTarjetaNormal(promociones.get(i)));
                }

                ScrollPane exploreScrollPane = new ScrollPane(promocionesTilePane);
                exploreScrollPane.setFitToWidth(true);
                exploreScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                exploreScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                VBox.setVgrow(exploreScrollPane, Priority.ALWAYS);

                promocionesContainer.getChildren().addAll(new Separator(), exploreLabel, exploreScrollPane);
            }
        }
    }

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
        VBox.setVgrow(nombreLabel, Priority.ALWAYS);
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
}