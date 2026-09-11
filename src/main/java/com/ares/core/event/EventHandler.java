package com.ares.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metoda oznaczona ta adnotacja zostanie automatycznie zarejestrowana w EventBusie.
 * Metoda musi przyjmowac dokladnie jeden parametr typu rozszerzajacego {@link Event}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    /** Wyzszy priorytet = wczesniejsze wywolanie. */
    int priority() default 0;

    /** Czy handler ma byc wywolywany nawet gdy event zostal anulowany. */
    boolean receiveCancelled() default false;
}
