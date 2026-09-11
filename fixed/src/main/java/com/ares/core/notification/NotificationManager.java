package com.ares.core.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Proste powiadomienia wyswietlane na HUDzie. */
public final class NotificationManager {

    public static final class Notification {
        public final String title;
        public final String message;
        public final long created;
        public final long duration;

        public Notification(String title, String message, long duration) {
            this.title = title;
            this.message = message;
            this.created = System.currentTimeMillis();
            this.duration = duration;
        }

        public double progress() {
            double t = (System.currentTimeMillis() - created) / (double) duration;
            return Math.max(0, Math.min(1, t));
        }

        public boolean expired() {
            return System.currentTimeMillis() - created > duration;
        }
    }

    private final List<Notification> notifications = new ArrayList<>();

    public void send(String title, String message, long duration) {
        notifications.add(new Notification(title, message, duration));
        if (notifications.size() > 6) notifications.remove(0);
    }

    public void update() {
        Iterator<Notification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().expired()) iterator.remove();
        }
    }

    public List<Notification> active() {
        return notifications;
    }
}
