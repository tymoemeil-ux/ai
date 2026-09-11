package com.ares.core.gui.hud;

import com.ares.Ares;
import com.ares.core.gui.theme.Theme;
import com.ares.core.util.math.ColorUtil;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Edytor HUDu: przeciaganie elementow, skalowanie, wlaczanie / wylaczanie. */
public final class HudEditorScreen extends Screen {

    private HudElement dragging;
    private double dragX;
    private double dragY;

    public HudEditorScreen() {
        super(Text.literal("Ares HUD Editor"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil2D.rect(context, 0, 0, width, height, 0x900A0C12);

        // siatka
        for (int x = 0; x < width; x += 20) {
            RenderUtil2D.rect(context, x, 0, 1, height, 0x10FFFFFF);
        }
        for (int y = 0; y < height; y += 20) {
            RenderUtil2D.rect(context, 0, y, width, 1, 0x10FFFFFF);
        }

        for (HudElement element : Ares.get().hud().elements()) {
            if (!element.enabled()) continue;
            element.render(context, delta);

            boolean hovered = element.isHovered(mouseX, mouseY);
            RenderUtil2D.roundedOutline(context, element.x() - 4, element.y() - 4,
                    element.width() + 8, element.height() + 8, 3,
                    hovered ? Theme.get().accent : ColorUtil.withAlpha(Theme.get().accent, 80));
            RenderUtil2D.text(context, element.name(), element.x() - 4, element.y() - 14,
                    element.enabled() ? Theme.get().accent : Theme.get().textDim, true);
        }

        String hint = "Przeciagnij: LPM  |  PPM: tlo  |  Scroll: skala  |  ESC: zamknij";
        RenderUtil2D.text(context, hint, 8, height - 14, Theme.get().textDim, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (HudElement element : Ares.get().hud().elements()) {
            if (!element.isHovered(mouseX, mouseY)) continue;

            if (button == 0) {
                dragging = element;
                dragX = mouseX - element.x();
                dragY = mouseY - element.y();
            } else if (button == 1) {
                element.setBackground(!element.background());
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            double x = Math.max(0, Math.min(width - dragging.width(), mouseX - dragX));
            double y = Math.max(0, Math.min(height - dragging.height(), mouseY - dragY));

            // przyciaganie do siatki co 5 px
            x = Math.round(x / 5.0) * 5;
            y = Math.round(y / 5.0) * 5;

            dragging.setPosition(x, y);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudElement element : Ares.get().hud().elements()) {
            if (!element.isHovered(mouseX, mouseY)) continue;
            double scale = element.scale() + (verticalAmount > 0 ? -0.1 : 0.1);
            element.setScale(Math.max(0.5, Math.min(3, scale)));
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        Ares.get().save();
        super.close();
    }
}
