package pe.edu.upeu.farmafx.utils;

import javafx.scene.control.TextFormatter;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class ValidacionUtils {

    private static final Pattern DNI_REGEX = Pattern.compile("^\\d{8}$");
    private static final Pattern STRONG_PASS_REGEX =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!.,:;?*_\\-]).{8,}$");

    private ValidacionUtils() {}

    public static boolean isValidDni(String dni) {
        if (dni == null) return false;
        String d = dni.trim();
        return DNI_REGEX.matcher(d).matches();
    }

    public static boolean isStrongPassword(String p) {
        if (p == null) return false;
        return STRONG_PASS_REGEX.matcher(p).matches();
    }

    public static boolean isPositiveInteger(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Integer.parseInt(text.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveDouble(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Double.parseDouble(text.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static UnaryOperator<TextFormatter.Change> createIntegerFilter() {
        return change -> {
            String newText = change.getControlNewText();
            if (newText.matches("^\\d*$")) { // Solo permite dígitos
                return change;
            }
            return null;
        };
    }

    public static UnaryOperator<TextFormatter.Change> createDoubleFilter() {
        return change -> {
            String newText = change.getControlNewText();
            // Permite dígitos y opcionalmente un punto decimal
            if (newText.matches("^\\d*\\.?\\d*$")) {
                return change;
            }
            return null;
        };
    }

    public static String normalizeBasic(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    public static String normalizedKey(String s) {
        String base = normalizeBasic(s);
        // Quita acentos
        String noAccents = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT);
    }

    public static void validarNombre(String nombre, int min, int max) throws Exception {
        String n = normalizeBasic(nombre);
        if (n.isEmpty() || n.length() < min || n.length() > max) {
            throw new Exception("El nombre debe tener entre " + min + " y " + max + " caracteres.");
        }
    }

    public static boolean isValidPrecio(BigDecimal precio) {
        return precio != null && precio.scale() <= 2 && precio.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isValidStock(Integer stock) {
        return stock != null && stock >= 0;
    }

    public static String normalizeForSearch(String s) {
        if (s == null) return "";
        // Usa la clave normalizada (minúsculas, sin acentos)
        String key = normalizedKey(s);
        // Elimina todo lo que no sea letra, número o espacio
        return key.replaceAll("[^a-z0-9\\s]", "");
    }
}