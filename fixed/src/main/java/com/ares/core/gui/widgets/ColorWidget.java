package com.ares.core.gui.widgets;

import com.ares.core.gui.theme.Theme;
import com.ares.core.setting.ColorSetting;
import com.ares.core.util.math.ColorUtil;
import com.ares.core.util.render.RenderUtil2D;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

/** Wybor koloru: okienko z HSV i alpha. */
public final class ColorWidget extends Widget {

    private final ColorSetting setting;
    private boolean expanded;
    private boolean draggingHue;
    private boolean draggingPicker;
    private boolean draggingAlpha;

    public ColorWidget(ColorSetting setting, double x, double y, double width) {
        super(x, y, width, 20);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        RenderUtil2D.roundedRect(context, x, y, width, 20, 4,
                hovered ? 0xFF1F2430 : Theme.get().panelLight);

        RenderUtil2D.text(context, setting.name(), x + 6, y + 6, Theme.get().text, true);

        RenderUtil2D.roundedRect(context, x + width - 26, y + 4, 20, 12, 3, setting.get());
        RenderUtil2D.roundedOutline(context, x + width - 26, y + 4, 20, 12, 3, 0x40FFFFFF);

        if (!expanded) {
            height = 20;
            return;
        }

        double panelHeight = 96;
        height = 20 + panelHeight;
        RenderUtil2D.roundedRect(context, x + 2, y + 22, width - 4, panelHeight, 4, Theme.get().panel);

        float[] hsb = Color.RGBtoHSB(setting.red(), setting.green(), setting.blue(), null);

        // kwadrat SV
        double squareX = x + 8;
        double squareY = y + 28;
        double squareSize = 60;
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                int color = Color.HSBtoRGB(hsb[0], i / 31f, 1 - j / 31f);
                RenderUtil2D.rect(context, squareX + (squareSize / 32.0) * i, squareY + (squareSize / 32.0) * j,
                        squareSize / 32.0 + 1, squareSize / 32.0 + 1, 0xFF000000 | color);
            }
        }
        RenderUtil2D.roundedOutline(context, squareX, squareY, squareSize, squareSize, 3, 0x30FFFFFF);

        double cursorX = squareX + hsb[1] * squareSize;
        double cursorY = squareY + (1 - hsb[2]) * squareSize;
        RenderUtil2D.roundedRect(context, cursorX - 2, cursorY - 2, 4, 4, 2, 0xFFE8EAF2);

        // pasek hue
        double hueX = x + 76;
        for (int i = 0; i < 32; i++) {
            int color = Color.HSBtoRGB(i / 31f, 1, 1);
            RenderUtil2D.rect(context, hueX, squareY + (squareSize / 32.0) * i, 10, squareSize / 32.0 + 1, 0xFF000000 | color);
        }
        RenderUtil2D.rect(context, hueX, squareY + hsb[0] * squareSize - 1, 10, 3, 0xFFE8EAF2);

        // alpha
        double alphaX = x + 94;
        RenderUtil2D.roundedRect(context, alphaX, squareY, 10, squareSize, 2, 0xFFFFFFFF);
        double alphaHeight = squareSize * (setting.alpha() / 255.0);
        RenderUtil2D.roundedRect(context, alphaX, squareY + squareSize - alphaHeight, 10, alphaHeight, 2,
                setting.get());
        RenderUtil2D.roundedOutline(context, alphaX, squareY, 10, squareSize, 2, 0x30FFFFFF);

        // rainbow toggle
        double toggleY = squareY + squareSize + 6;
        RenderUtil2D.roundedRect(context, x + 8, toggleY, width - 16, 14, 3,
                setting.isRainbow() ? ColorUtil.withAlpha(Theme.get().accent, 120) : 0xFF1F2430);
        RenderUtil2D.text(context, "Rainbow", x + 12, toggleY + 3, Theme.get().text, true);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x + width - 26 && mouseX <= x + width - 6 && mouseY >= y + 4 && mouseY <= y + 16) {
            expanded = !expanded;
            return;
        }
        if (!expanded) return;

        double squareX = x + 8;
        double squareY = y + 28;
        double squareSize = 60;

        if (mouseX >= squareX && mouseX <= squareX + squareSize && mouseY >= squareY && mouseY <= squareY + squareSize) {
            draggingPicker = true;
            updatePicker(mouseX, mouseY);
        } else if (mouseX >= x + 76 && mouseX <= x + 86 && mouseY >= squareY && mouseY <= squareY + squareSize) {
            draggingHue = true;
            updateHue(mouseY);
        } else if (mouseX >= x + 94 && mouseX <= x + 104 && mouseY >= squareY && mouseY <= squareY + squareSize) {
            draggingAlpha = true;
            updateAlpha(mouseY);
        } else if (mouseY >= squareY + squareSize + 6 && mouseY <= squareY + squareSize + 20) {
            setting.setRainbow(!setting.isRainbow());
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingHue = false;
        draggingPicker = false;
        draggingAlpha = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingPicker) updatePicker(mouseX, mouseY);
        if (draggingHue) updateHue(mouseY);
        if (draggingAlpha) updateAlpha(mouseY);
    }

    private void updatePicker(double mouseX, double mouseY) {
        double squareX = x + 8;
        double squareY = y + 28;
        double squareSize = 60;
        float saturation = (float) Math.max(0, Math.min(1, (mouseX - squareX) / squareSize));
        float brightness = (float) (1 - Math.max(0, Math.min(1, (mouseY - squareY) / squareSize)));

        float[] hsb = Color.RGBtoHSB(setting.red(), setting.green(), setting.blue(), null);
        int rgb = Color.HSBtoRGB(hsb[0], saturation, brightness);
        setting.set((setting.alpha() << 24) | (rgb & 0x00FFFFFF));
    }

    private void updateHue(double mouseY) {
        double squareY = y + 28;
        double squareSize = 60;
        float hue = (float) Math.max(0, Math.min(1, (mouseY - squareY) / squareSize));
        float[] hsb = Color.RGBtoHSB(setting.red(), setting.green(), setting.blue(), null);
        int rgb = Color.HSBtoRGB(hue, hsb[1], hsb[2]);
        setting.set((setting.alpha() << 24) | (rgb & 0x00FFFFFF));
    }

    private void updateAlpha(double mouseY) {
        double squareY = y + 28;
        double squareSize = 60;
        double fraction = 1 - Math.max(0, Math.min(1, (mouseY - squareY) / squareSize));
        setting.set(ColorUtil.withAlpha(setting.get(), (int) (fraction * 255)));
    }
}
