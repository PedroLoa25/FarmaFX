package pe.edu.upeu.farmafx.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Un simple sistema de notificación (Event Bus) para permitir la comunicación
 * entre controladores desacoplados. Usa el patrón Singleton.
 */
public final class Notifier {

    private static Notifier INSTANCE;

    // Constantes para los temas de notificación. Evita errores de tipeo.
    public static final String TOPIC_MARCAS_CHANGED = "marcas_changed";
    public static final String TOPIC_CATEGORIAS_CHANGED = "categorias_changed";

    private final Map<String, List<Runnable>> listeners = new HashMap<>();

    private Notifier() {
        // Inicializa los temas para evitar null checks
        listeners.put(TOPIC_MARCAS_CHANGED, new ArrayList<>());
        listeners.put(TOPIC_CATEGORIAS_CHANGED, new ArrayList<>());
    }

    public static Notifier getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Notifier();
        }
        return INSTANCE;
    }

    /**
     * Suscribe una acción a un tema específico.
     * @param topic El tema al que suscribirse (e.g., TOPIC_MARCAS_CHANGED).
     * @param action La acción (Runnable) a ejecutar cuando se publique en el tema.
     */
    public void subscribe(String topic, Runnable action) {
        listeners.get(topic).add(action);
    }

    /**
     * Publica un mensaje en un tema, ejecutando todas las acciones suscritas.
     * @param topic El tema en el que publicar.
     */
    public void publish(String topic) {
        if (listeners.containsKey(topic)) {
            // Se crea una copia para evitar problemas si un listener modifica la lista
            new ArrayList<>(listeners.get(topic)).forEach(Runnable::run);
        }
    }
}