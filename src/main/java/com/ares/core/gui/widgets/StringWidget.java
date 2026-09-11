package com.ares.core.gui.widgets;

import com.ares.core.gui.theme.Theme;
import com.ares.core.setting.StringSetting;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Pole tekstowe. */
public final class StringWidget extends Widget {

    private final StringSetting setting;
    private boolean focused;

    public StringWidget(StringSetting setting, double x, double y, double width) {
        super(x, y, width, 22);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        RenderUtil2D.roundedRect(context, x, y, width, height, 4,
                focused ? 0xFF242A36 : (hovered ? 0xFF1F2430 : Theme.get().panelLight));

        RenderUtil2D.text(context, setting.name(), x + 6, y + 3, Theme.get().textDim, true);

        String value = setting.get();
        String display = focused ? value + "_" : value;
        RenderUtil2D.roundedRect(context, x + 6, y + 13, width - 12, 7, 2, 0xFF0E1015);
        RenderUtil2D.text(context, display, x + 8, y + 13, Theme.get().text, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        focused = isHovered((int) mouseX, (int) mouseY);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            String value = setting.get();
            if (!value.isEmpty()) setting.set(value.substring(0, value.length() - 1));
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
        }
    }

    @Override
    public void charTyped(char character, int modifiers) {
        if (!focused) return;
        if (character == '\r' || character == '\n') return;
        setting.set(setting.get() + character);
    }
}
