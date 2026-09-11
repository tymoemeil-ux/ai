package com.ares.core.gui.widgets;

import com.ares.core.gui.theme.Theme;
import com.ares.core.setting.BoolSetting;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;

/** Przelacznik wlacz / wylacz. */
public final class BooleanWidget extends Widget {

    private final BoolSetting setting;
    private final Animation animation;

    public BooleanWidget(BoolSetting setting, double x, double y, double width) {
        super(x, y, width, 20);
        this.setting = setting;
        this.animation = new Animation(setting.get() ? 1 : 0);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        animation.update(setting.get(), 0.25);
        boolean hovered = isHovered(mouseX, mouseY);

        RenderUtil2D.roundedRect(context, x, y, width, height, 4,
                hovered ? 0xFF1F2430 : Theme.get().panelLight);

        RenderUtil2D.text(context, setting.name(), x + 6, y + 6, Theme.get().text, true);

        double boxSize = 12;
        double boxX = x + width - boxSize - 6;
        double boxY = y + (height - boxSize) / 2;

        int color = com.ares.core.util.math.MathUtil.lerpColor(0xFF3A3F4B, Theme.get().accent, animation.value());
        RenderUtil2D.roundedRect(context, boxX, boxY, boxSize, boxSize, 3, color);

        if (setting.get()) {
            double checkSize = boxSize - 6;
            RenderUtil2D.rect(context, boxX + 3, boxY + 3, checkSize, checkSize, 0xFF0A0A0F);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered((int) mouseX, (int) mouseY)) {
            setting.toggle();
        }
    }
}
