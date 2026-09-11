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
        int errors;

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
                Throwable cause = t instanceof java.lang.reflect.InvocationTargetException
                        && t.getCause() != null ? t.getCause() : t;
                errors++;
                // Nie zalewamy logu: pelny slad tylko 3 pierwsze razy, pozniej jedna linia.
                if (errors <= 3) {
                    cause.printStackTrace();
                } else if (errors % 500 == 0) {
                    System.err.println("[Ares] " + method.getDeclaringClass().getSimpleName()
                            + "." + method.getName() + ": " + cause + " (x" + errors + ")");
                }
            }
        }
    }

    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Listener>> listeners = new HashMap<>();
    private int depth;

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
        // Zabezpieczenie: jezeli sluchacz znowu publikuje ten sam event (rekurencja),
        // przerywamy po 8 poziomach - inaczej gra konczy StackOverflowError.
        if (depth >= 8) {
            System.err.println("[Ares] Wykryto zapetlenie eventu " + event.getClass().getSimpleName() + " - przerwano.");
            return event;
        }
        list.sort(Comparator.comparingInt((Listener l) -> l.priority).reversed());
        depth++;
        try {
            for (Listener listener : new ArrayList<>(list)) {
                if (event.isCancelled() && !listener.receiveCancelled) continue;
                listener.invoke(event);
            }
        } finally {
            depth--;
        }
        return event;
    }
}
