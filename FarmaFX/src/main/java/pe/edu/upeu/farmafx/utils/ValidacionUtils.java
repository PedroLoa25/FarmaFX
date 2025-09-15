package pe.edu.upeu.farmafx.utils;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ValidacionUtils {

    private static final Pattern DNI_REGEX = Pattern.compile("^\\d{8}$");
    // Min 8, mayúscula, minúscula, dígito, símbolo permitido.
    private static final Pattern STRONG_PASS_REGEX =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!.,:;?*_\\-]).{8,}$");

    private ValidacionUtils() {}

    // --------- Validaciones existentes ---------
    public static boolean isValidDni(String dni) {
        if (dni == null) return false;
        String d = dni.trim();
        return DNI_REGEX.matcher(d).matches();
    }

    public static boolean isStrongPassword(String p) {
        if (p == null) return false;
        return STRONG_PASS_REGEX.matcher(p).matches();
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // --------- NUEVO: utilidades de nombres/cadenas ---------
    /** Normaliza básico: trim y colapsa espacios internos múltiples a uno. */
    public static String normalizeBasic(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    /**
     * Clave normalizada para comparar nombres de forma case-insensitive
     * y sin acentos, colapsando espacios.
     */
    public static String normalizedKey(String s) {
        String base = normalizeBasic(s);
        // Quita acentos
        String noAccents = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT);
    }

    /** Valida que el nombre tenga longitud entre min y max (tras normalizeBasic). */
    public static void validarNombre(String nombre, int min, int max) throws Exception {
        String n = normalizeBasic(nombre);
        if (n.isEmpty() || n.length() < min || n.length() > max) {
            throw new Exception("El nombre debe tener entre " + min + " y " + max + " caracteres.");
        }
    }

    // --------- Anticipando Producto ---------
    public static boolean isValidPrecio(BigDecimal precio) {
        return precio != null && precio.scale() <= 2 && precio.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isValidStock(Integer stock) {
        return stock != null && stock >= 0;
    }
}