package pe.edu.upeu.farmafx.enums;

public enum Estado {
    ACTIVO,
    INACTIVO;

    public boolean isActivo() {
        return this == ACTIVO;
    }

    public static Estado fromBoolean(boolean activo) {
        return activo ? ACTIVO : INACTIVO;
    }
}