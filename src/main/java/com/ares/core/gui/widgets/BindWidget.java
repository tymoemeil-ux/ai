package com.ares.core.gui.widgets;

import com.ares.core.gui.theme.Theme;
import com.ares.core.setting.BindSetting;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Przypisywanie klawisza do ustawienia. */
public final class BindWidget extends Widget {

    private final BindSetting setting;
    private boolean binding;

    public BindWidget(BindSetting setting, double x, double y, double width) {
        super(x, y, width, 20);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        RenderUtil2D.roundedRect(context, x, y, width, height, 4,
                hovered || binding ? 0xFF1F2430 : Theme.get().panelLight);

        RenderUtil2D.text(context, setting.name(), x + 6, y + 6, Theme.get().text, true);

        String value = binding ? "..." : keyName(setting.get());
        double valueWidth = RenderUtil2D.textWidth(value);
        RenderUtil2D.roundedRect(context, x + width - valueWidth - 14, y + 3, valueWidth + 10, 14, 3,
                com.ares.core.util.math.ColorUtil.withAlpha(Theme.get().accent, 70));
        RenderUtil2D.text(context, value, x + width - valueWidth - 9, y + 6, 0xFFE8EAF2, true);
    }

    private String keyName(int key) {
        if (key < 0) return "None";
        try {
            return net.minecraft.client.util.InputUtil.fromKeyCode(key, 0).getTranslationKey()
                    .replace("key.keyboard.", "").replace(".", " ").toUpperCase();
        } catch (Exception e) {
            return String.valueOf(key);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY)) binding = !binding;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!binding) return;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            setting.set(-1);
        } else {
            setting.set(keyCode);
        }
        binding = false;
    }
}
