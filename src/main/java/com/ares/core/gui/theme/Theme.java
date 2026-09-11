package com.ares.core.gui.theme;

/** Kolory i metryka interfejsu (styl Aoba). */
public final class Theme {

    public int background = 0xF20C0E14;
    public int panel = 0xF2161920;
    public int panelLight = 0xF21F242E;
    public int hover = 0x24FFFFFF;
    public int accent = 0xFF7C5CFF;
    public int accentSecondary = 0xFF22D3EE;
    public int text = 0xFFE8EAF2;
    public int textDim = 0xFF8A90A6;
    public int outline = 0x2AFFFFFF;
    public int enabled = 0xFF22C55E;
    public int disabled = 0x40FFFFFF;
    public int shadow = 0x80000000;
    public int scrollbar = 0x60FFFFFF;

    public float radius = 6f;
    public float panelPadding = 6f;

    private static final Theme INSTANCE = new Theme();

    public static Theme get() {
        return INSTANCE;
    }

    public int accentGradient(double t) {
        return com.ares.core.util.math.MathUtil.lerpColor(accent, accentSecondary, Math.max(0, Math.min(1, t)));
    }
}
