package com.ares.core.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lekki, refleksyjny event bus z priorytetami.
 */
public final class EventBus {

    private static final class Listener {
        final Object owner;
        final Method method;
        final int priority;
        final boolean receiveCancelled;

        Listener(Object owner, Method method, int priority, boolean receiveCancelled) {
            this.owner = owner;
            this.method = method;
            this.priority = priority;
            this.receiveCancelled = receiveCancelled;
        }

        void invoke(Event event) {
            try {
                method.invoke(owner, event);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Listener>> listeners = new HashMap<>();

    private EventBus() {
    }

    public static EventBus get() {
        return INSTANCE;
    }

    public void register(Object owner) {
        if (owner == null) return;
        Class<?> type = owner.getClass();
        for (Method method : type.getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) continue;
            if (method.getParameterCount() != 1) continue;
            if (!Event.class.isAssignableFrom(method.getParameterTypes()[0])) continue;
            method.setAccessible(true);

            Class<?> eventType = method.getParameterTypes()[0];
            List<Listener> list = listeners.computeIfAbsent(eventType, k -> new ArrayList<>());
            boolean duplicate = false;
            for (Listener existing : list) {
                if (existing.owner == owner && existing.method.getName().equals(method.getName())) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) continue;
            list.add(new Listener(owner, method, annotation.priority(), annotation.receiveCancelled()));
            list.sort(Comparator.comparingInt((Listener l) -> l.priority).reversed());
        }
    }

    public void unregister(Object owner) {
        listeners.values().forEach(list -> list.removeIf(l -> l.owner == owner));
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> T post(T event) {
        // sluchacze zarejestrowani na klase bazowa (np. TickEvent) dostaja tez eventy pochodne
        // (np. TickEvent.Client), wiec zbieramy listenery z calej hierarchii.
        List<Listener> list = new ArrayList<>();
        for (Class<?> type = event.getClass(); type != null && Event.class.isAssignableFrom(type);
             type = type.getSuperclass()) {
            List<Listener> found = listeners.get(type);
            if (found != null) list.addAll(found);
        }
        if (list.isEmpty()) return event;
        list.sort(Comparator.comparingInt((Listener l) -> l.priority).reversed());
        for (Listener listener : new ArrayList<>(list)) {
            if (event.isCancelled() && !listener.receiveCancelled) continue;
            listener.invoke(event);
        }
        return event;
    }
}
