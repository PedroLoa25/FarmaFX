package pe.edu.upeu.farmafx.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public final class Toast {

    private Toast() {}

    public static void show(Window owner, String message) {
        show(owner, message, Type.INFO, Duration.seconds(2.5));
    }
    public static void show(Node node, String message) {
        Window w = getWindow(node);
        if (w != null) show(w, message);
    }

    public static void success(Window owner, String message) {
        show(owner, message, Type.SUCCESS, Duration.seconds(2.5));
    }
    public static void success(Node node, String message) {
        Window w = getWindow(node);
        if (w != null) success(w, message);
    }

    public static void info(Window owner, String message) {
        show(owner, message, Type.INFO, Duration.seconds(2.5));
    }
    public static void info(Node node, String message) {
        Window w = getWindow(node);
        if (w != null) info(w, message);
    }

    public static void error(Window owner, String message) {
        show(owner, message, Type.ERROR, Duration.seconds(3.5));
    }
    public static void error(Node node, String message) {
        Window w = getWindow(node);
        if (w != null) error(w, message);
    }

    private enum Type { INFO, SUCCESS, ERROR }

    private static Window getWindow(Node n) {
        return (n != null && n.getScene() != null) ? n.getScene().getWindow() : null;
    }

    private static void show(Window owner, String message, Type type, Duration duration) {
        if (owner == null) return;

        Label content = new Label(message);
        content.setTextFill(Color.WHITE);
        content.setPadding(new Insets(10, 14, 10, 14));
        content.setAlignment(Pos.CENTER_LEFT);
        content.setStyle("-fx-background-radius: 8; -fx-font-size: 13px; " + backgroundFor(type));
        content.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.35)));
        content.setOpacity(0);

        Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.getContent().add(content);

        // Mostrar primero para tener ancho/alto correctos y luego reposicionar
        popup.show(owner);

        double w = content.getWidth();
        double h = content.getHeight();
        double x = owner.getX() + (owner.getWidth() - w) / 2.0;
        double y = owner.getY() + owner.getHeight() - h - 80.0;

        popup.setX(x);
        popup.setY(y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), content);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition stay = new PauseTransition(duration);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), content);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> popup.hide());

        new SequentialTransition(fadeIn, stay, fadeOut).play();
    }

    private static String backgroundFor(Type t) {
        switch (t) {
            case SUCCESS:
                return "-fx-background-color: rgba(16,185,129,0.95);";   // verde
            case ERROR:
                return "-fx-background-color: rgba(239,68,68,0.95);";    // rojo
            default:
                return "-fx-background-color: rgba(59,130,246,0.95);";   // azul
        }
    }
}