package com.ares.core.macro;

/** Makro: klawisz -> komenda lub wiadomosc. */
public final class Macro {
    private int key = -1;
    private String action = "";

    public Macro() {
    }

    public Macro(int key, String action) {
        this.key = key;
        this.action = action;
    }

    public int key() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String action() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
