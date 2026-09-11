package com.ares.core.gui.widgets;

import com.ares.core.gui.theme.Theme;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.render.RenderUtil2D;
import net.minecraft.client.gui.DrawContext;

/** Przelacznik trybu (enum) - lewy klik w przod, prawy w tyl. */
public final class ModeWidget extends Widget {

    private final ModeSetting<? extends Enum<?>> setting;
    private boolean expanded;
    private final Animation animation = new Animation(0);

    public ModeWidget(ModeSetting<? extends Enum<?>> setting, double x, double y, double width) {
        super(x, y, width, 20);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        RenderUtil2D.roundedRect(context, x, y, width, height, 4,
                hovered ? 0xFF1F2430 : Theme.get().panelLight);

        RenderUtil2D.text(context, setting.name(), x + 6, y + 6, Theme.get().text, true);

        String value = setting.get().name();
        double valueWidth = RenderUtil2D.textWidth(value);
        RenderUtil2D.roundedRect(context, x + width - valueWidth - 14, y + 3, valueWidth + 10, 14, 3,
                com.ares.core.util.math.ColorUtil.withAlpha(Theme.get().accent, 70));
        RenderUtil2D.text(context, value, x + width - valueWidth - 9, y + 6, 0xFFE8EAF2, true);

        if (expanded) {
            int index = 0;
            for (Enum<?> value1 : setting.values()) {
                double itemY = y + height + index * 16;
                RenderUtil2D.roundedRect(context, x + 4, itemY, width - 8, 16, 3, Theme.get().panel);
                RenderUtil2D.text(context, value1.name(), x + 10, itemY + 4,
                        value1 == setting.get() ? Theme.get().accent : Theme.get().textDim, true);
                index++;
            }
            setHeight(height + setting.values().size() * 16);
        } else {
            setHeight(20);
        }
        animation.update(expanded, 0.3);
    }

    private void setHeight(double height) {
        this.height = height;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (expanded) {
            int index = 0;
            for (Enum<?> value : setting.values()) {
                double itemY = y + 20 + index * 16;
                if (mouseX >= x + 4 && mouseX <= x + width - 4 && mouseY >= itemY && mouseY <= itemY + 16) {
                    set(value);
                    expanded = false;
                    return;
                }
                index++;
            }
        }

        if (isHovered((int) mouseX, (int) mouseY)) {
            if (button == 1) {
                setting.cycle(false);
            } else {
                expanded = !expanded;
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void set(Enum<?> value) {
        ((ModeSetting) setting).set(value);
    }
}
