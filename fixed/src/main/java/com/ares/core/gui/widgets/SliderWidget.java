package com.ares.core.gui.widgets;

import com.ares.core.gui.theme.Theme;
import com.ares.core.setting.NumberSetting;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;

/** Suwak wartosci liczbowej. */
public final class SliderWidget extends Widget {

    private final NumberSetting<? extends Number> setting;
    private boolean dragging;

    public SliderWidget(NumberSetting<? extends Number> setting, double x, double y, double width) {
        super(x, y, width, 30);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        RenderUtil2D.roundedRect(context, x, y, width, height, 4,
                hovered ? 0xFF1F2430 : Theme.get().panelLight);

        String name = setting.name();
        String value = format(setting.get());
        RenderUtil2D.text(context, name, x + 6, y + 4, Theme.get().text, true);
        RenderUtil2D.text(context, value, x + width - 6 - RenderUtil2D.textWidth(value), y + 4,
                Theme.get().accent, true);

        double barX = x + 6;
        double barY = y + 18;
        double barWidth = width - 12;
        double fraction = setting.fraction();

        RenderUtil2D.roundedRect(context, barX, barY + 2, barWidth, 4, 2, 0xFF3A3F4B);
        RenderUtil2D.roundedRect(context, barX, barY + 2, barWidth * fraction, 4, 2, Theme.get().accent);

        double knobX = barX + barWidth * fraction - 3;
        RenderUtil2D.roundedRect(context, knobX, barY, 6, 10, 3, 0xFFE8EAF2);
    }

    private String format(Number number) {
        double value = number.doubleValue();
        if (value == Math.floor(value) && Math.abs(value) < 1000) return String.valueOf(number.intValue());
        return String.format("%.2f", value);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY)) {
            dragging = true;
            update(mouseX);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) update(mouseX);
    }

    private void update(double mouseX) {
        double barX = x + 6;
        double barWidth = width - 12;
        double fraction = (mouseX - barX) / barWidth;
        setting.setFromFraction(Math.max(0, Math.min(1, fraction)));
    }
}
