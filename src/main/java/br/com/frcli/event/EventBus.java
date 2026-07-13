package br.com.frcli.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus instance = new EventBus();
    private final Map<Class<? extends RpgEvent>, List<Consumer<RpgEvent>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T extends RpgEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add((Consumer<RpgEvent>) listener);
    }

    public void publish(RpgEvent event) {
        if (event == null) return;
        List<Consumer<RpgEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<RpgEvent> listener : eventListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    System.err.println("[EventBus] Erro ao processar evento " + event.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }
}
