package com.ares.core.gui.hud;

import com.ares.core.gui.theme.Theme;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;

/** Bazowy element HUDu (mozna wlaczac, przesuwac i skalowac). */
public abstract class HudElement {

    public enum Alignment { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER }

    private final String name;
    private double x;
    private double y;
    private double width = 60;
    private double height = 20;
    private double scale = 1.0;
    private boolean enabled = true;
    private boolean background = false;

    protected HudElement(String name, double x, double y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public abstract void render(DrawContext context, float tickDelta);

    public String name() {
        return name;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    protected void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double scale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean background() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public Alignment alignment(int screenWidth, int screenHeight) {
        boolean left = x + width / 2.0 < screenWidth / 2.0;
        boolean top = y + height / 2.0 < screenHeight / 2.0;
        if (top && left) return Alignment.TOP_LEFT;
        if (top) return Alignment.TOP_RIGHT;
        if (left) return Alignment.BOTTOM_LEFT;
        return Alignment.BOTTOM_RIGHT;
    }

    /** Rysuje tlo panelu w stylu Aoba. */
    protected void drawBackground(DrawContext context) {
        if (!background) return;
        RenderUtil2D.roundedRect(context, x - 3, y - 3, width + 6, height + 6,
                Theme.get().radius, Theme.get().panel);
        RenderUtil2D.roundedOutline(context, x - 3, y - 3, width + 6, height + 6,
                Theme.get().radius, Theme.get().outline);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x - 3 && mouseX <= x + width + 3 && mouseY >= y - 3 && mouseY <= y + height + 3;
    }
}
