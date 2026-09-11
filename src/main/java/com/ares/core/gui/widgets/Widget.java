package com.ares.core.gui.widgets;

import net.minecraft.client.gui.DrawContext;

/** Bazowy element GUI. */
public abstract class Widget {

    public double x;
    public double y;
    public double width;
    public double height;

    protected Widget(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    public void charTyped(char character, int modifiers) {
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int height() {
        return (int) height;
    }
}
