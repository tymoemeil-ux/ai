package com.ares.core.gui.clickgui;

import com.ares.Ares;
import com.ares.core.gui.theme.Theme;
import com.ares.core.gui.widgets.Animation;
import com.ares.core.gui.widgets.BindWidget;
import com.ares.core.gui.widgets.BooleanWidget;
import com.ares.core.gui.widgets.ColorWidget;
import com.ares.core.gui.widgets.ModeWidget;
import com.ares.core.gui.widgets.SliderWidget;
import com.ares.core.gui.widgets.StringWidget;
import com.ares.core.gui.widgets.Widget;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BindSetting;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.setting.NumberSetting;
import com.ares.core.setting.Setting;
import com.ares.core.setting.StringSetting;
import com.ares.core.util.math.ColorUtil;
import com.ares.core.util.math.MathUtil;
import com.ares.core.util.render.RenderUtil2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * ClickGUI w stylu Aoba: kategorie po lewej, moduly w srodku, ustawienia po prawej.
 */
public final class ClickGuiScreen extends Screen {

    private static final int SIDEBAR_WIDTH = 116;
    private static final int MODULES_WIDTH = 210;
    private static final int HEADER_HEIGHT = 34;
    private static final int FOOTER_HEIGHT = 24;
    private static final int ROW_HEIGHT = 26;

    private final List<Widget> widgets = new ArrayList<>();
    private final List<Object> rows = new ArrayList<>();
    private final Map<Module, Animation> moduleAnimations = new HashMap<>();
    private final Map<ModuleCategory, Animation> categoryAnimations = new HashMap<>();

    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private Module selectedModule;
    private String search = "";
    private boolean searching;

    private double moduleScroll;
    private double moduleScrollTarget;
    private double settingsScroll;
    private double settingsScrollTarget;
    private double openAnimation;

    private double frameX;
    private double frameY;
    private double frameWidth;
    private double frameHeight;

    public ClickGuiScreen() {
        super(Text.literal("Ares ClickGUI"));
    }

    private List<Module> visibleModules() {
        if (search.isEmpty()) return Ares.get().modules().byCategory(selectedCategory);
        return Ares.get().modules().search(search);
    }

    @Override
    protected void init() {
        super.init();
        frameWidth = Math.min(760, width - 60);
        frameHeight = Math.min(470, height - 60);
        frameX = (width - frameWidth) / 2.0;
        frameY = (height - frameHeight) / 2.0;
        rebuildSettings();
    }

    /** Buduje liste widgetow dla panelu ustawien. */
    private void rebuildSettings() {
        widgets.clear();
        rows.clear();
        if (selectedModule == null) return;

        double panelX = frameX + SIDEBAR_WIDTH + MODULES_WIDTH + 16;
        double panelWidth = frameWidth - SIDEBAR_WIDTH - MODULES_WIDTH - 28;

        for (Map.Entry<String, List<Setting<?>>> entry : selectedModule.groups().entrySet()) {
            boolean anyVisible = false;
            for (Setting<?> setting : entry.getValue()) {
                if (setting.isVisible()) {
                    anyVisible = true;
                    break;
                }
            }
            if (!anyVisible) continue;

            rows.add(entry.getKey());
            for (Setting<?> setting : entry.getValue()) {
                if (!setting.isVisible()) continue;
                Widget widget = widgetFor(setting, panelX, 0, panelWidth);
                if (widget == null) continue;
                widgets.add(widget);
                rows.add(widget);
            }
        }
    }

    private Widget widgetFor(Setting<?> setting, double x, double y, double width) {
        if (setting instanceof BoolSetting bool) return new BooleanWidget(bool, x, y, width);
        if (setting instanceof NumberSetting<?> number) {
            Widget widget = new SliderWidget((NumberSetting<? extends Number>) number, x, y, width);
            return widget;
        }
        if (setting instanceof ModeSetting<?> mode) {
            return new ModeWidget((ModeSetting<? extends Enum<?>>) mode, x, y, width);
        }
        if (setting instanceof ColorSetting color) return new ColorWidget(color, x, y, width);
        if (setting instanceof StringSetting string) return new StringWidget(string, x, y, width);
        if (setting instanceof BindSetting bind) return new BindWidget(bind, x, y, width);
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnimation = Math.min(1, openAnimation + delta * 0.15f);

        // tlo
        RenderUtil2D.rect(context, 0, 0, width, height, 0xB40A0C12);
        if (this.client != null && this.client.world != null) {
            renderInGameBackground(context);
        }

        double anim = MathUtil.easeOutExpo(openAnimation);
        double x = frameX + (1 - anim) * 0;
        double y = frameY + (1 - anim) * 30;

        // ramka
        RenderUtil2D.shadow(context, x, y, frameWidth, frameHeight, 5);
        RenderUtil2D.roundedRect(context, x, y, frameWidth, frameHeight, Theme.get().radius, Theme.get().background);
        RenderUtil2D.roundedOutline(context, x, y, frameWidth, frameHeight, Theme.get().radius, Theme.get().outline);

        // naglowek z gradientem
        RenderUtil2D.roundedRect(context, x, y, frameWidth, HEADER_HEIGHT, Theme.get().radius, Theme.get().panel);
        RenderUtil2D.horizontalGradient(context, x + 1, y, frameWidth - 2, 2, Theme.get().accent, Theme.get().accentSecondary);

        String title = Ares.NAME + " §7v" + Ares.VERSION;
        RenderUtil2D.text(context, title, x + 12, y + 8, 0xFFF2F4FA, false);
        String subtitle = Ares.get().modules().size() + " modulow";
        RenderUtil2D.text(context, subtitle, x + frameWidth - 12 - RenderUtil2D.textWidth(subtitle), y + 18,
                Theme.get().textDim, true);

        drawCategories(context, mouseX, mouseY, x, y + HEADER_HEIGHT);
        drawModules(context, mouseX, mouseY, x + SIDEBAR_WIDTH, y + HEADER_HEIGHT);
        drawSettings(context, mouseX, mouseY, x + SIDEBAR_WIDTH + MODULES_WIDTH, y + HEADER_HEIGHT);
        drawFooter(context, mouseX, mouseY, x, y + frameHeight - FOOTER_HEIGHT);
    }

    private void drawCategories(DrawContext context, int mouseX, int mouseY, double x, double y) {
        double height = frameHeight - HEADER_HEIGHT - FOOTER_HEIGHT;
        RenderUtil2D.rect(context, x + SIDEBAR_WIDTH, y + 8, 1, height - 16, Theme.get().outline);

        int index = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            double rowY = y + 10 + index * ROW_HEIGHT;
            boolean selected = category == selectedCategory;
            boolean hovered = mouseX >= x + 6 && mouseX <= x + SIDEBAR_WIDTH - 6
                    && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 4;

            Animation animation = categoryAnimations.computeIfAbsent(category, k -> new Animation(selected ? 1 : 0));
            animation.update(selected || hovered, 0.2);

            RenderUtil2D.roundedRect(context, x + 6, rowY, SIDEBAR_WIDTH - 12, ROW_HEIGHT - 4, 4,
                    ColorUtil.withAlpha(Theme.get().panelLight, (int) (200 + 55 * animation.value())));

            if (selected) {
                RenderUtil2D.rect(context, x + 6, rowY, 2, ROW_HEIGHT - 4, Theme.get().accent);
            }

            RenderUtil2D.text(context, iconFor(category), x + 14, rowY + 5,
                    selected ? Theme.get().accent : Theme.get().textDim, false);
            RenderUtil2D.text(context, category.displayName(), x + 30, rowY + 6,
                    selected ? 0xFFF2F4FA : Theme.get().textDim, true);

            String count = String.valueOf(Ares.get().modules().byCategory(category).size());
            RenderUtil2D.text(context, count, x + SIDEBAR_WIDTH - 16 - RenderUtil2D.textWidth(count), rowY + 6,
                    Theme.get().textDim, true);
            index++;
        }
    }

    private String iconFor(ModuleCategory category) {
        return switch (category) {
            case COMBAT -> "⚔";
            case MOVEMENT -> "➤";
            case RENDER -> "◉";
            case UTILITY -> "⚒";
            case CLIENT -> "★";
            case HUD -> "▤";
        };
    }

    private void drawModules(DrawContext context, int mouseX, int mouseY, double x, double y) {
        double height = frameHeight - HEADER_HEIGHT - FOOTER_HEIGHT;

        // wyszukiwarka
        double searchX = x + 8;
        double searchY = y + 8;
        double searchWidth = MODULES_WIDTH - 16;
        RenderUtil2D.roundedRect(context, searchX, searchY, searchWidth, 20, 4,
                searching ? 0xFF242A36 : Theme.get().panelLight);
        String searchText = search.isEmpty() && !searching ? "Szukaj..." : search + (searching ? "_" : "");
        RenderUtil2D.text(context, searchText, searchX + 6, searchY + 6,
                search.isEmpty() ? Theme.get().textDim : Theme.get().text, true);

        double listY = searchY + 28;
        double listHeight = height - 46;

        moduleScroll += (moduleScrollTarget - moduleScroll) * 0.25;

        List<Module> modules = visibleModules();
        int maxVisible = (int) (listHeight / (ROW_HEIGHT - 2));
        moduleScrollTarget = Math.max(0, Math.min(moduleScrollTarget, Math.max(0, modules.size() - maxVisible) * (ROW_HEIGHT - 2)));

        RenderUtil2D.scissor(context, x, listY, MODULES_WIDTH, listHeight, () -> {
            int index = 0;
            for (Module module : modules) {
                double rowY = listY + index * (ROW_HEIGHT - 2) - moduleScroll;
                if (rowY < listY - ROW_HEIGHT || rowY > listY + listHeight) {
                    index++;
                    continue;
                }

                boolean hovered = mouseX >= x + 6 && mouseX <= x + MODULES_WIDTH - 6
                        && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 4;
                boolean selected = module == selectedModule;

                Animation animation = moduleAnimations.computeIfAbsent(module, k -> new Animation(module.isEnabled() ? 1 : 0));
                animation.update(module.isEnabled(), 0.2);

                int background = com.ares.core.util.math.MathUtil.lerpColor(
                        hovered ? 0xFF1F2430 : Theme.get().panelLight,
                        ColorUtil.withAlpha(Theme.get().accent, 90), animation.value() * 0.6);
                RenderUtil2D.roundedRect(context, x + 6, rowY, MODULES_WIDTH - 12, ROW_HEIGHT - 4, 4, background);

                if (selected) {
                    RenderUtil2D.roundedOutline(context, x + 6, rowY, MODULES_WIDTH - 12, ROW_HEIGHT - 4, 4,
                            Theme.get().accent);
                }

                RenderUtil2D.text(context, module.name(), x + 14, rowY + 7,
                        module.isEnabled() ? 0xFFF2F4FA : Theme.get().textDim, true);

                if (module.info() != null) {
                    String info = module.info();
                    RenderUtil2D.text(context, info,
                            x + MODULES_WIDTH - 14 - RenderUtil2D.textWidth(info), rowY + 7,
                            Theme.get().textDim, true);
                }
                index++;
            }
        });

        drawScrollbar(context, x + MODULES_WIDTH - 4, listY, 2, listHeight,
                modules.size(), maxVisible, moduleScroll, (ROW_HEIGHT - 2));
    }

    private void drawSettings(DrawContext context, int mouseX, int mouseY, double x, double y) {
        double width = frameWidth - SIDEBAR_WIDTH - MODULES_WIDTH - 16;
        double height = frameHeight - HEADER_HEIGHT - FOOTER_HEIGHT;

        RenderUtil2D.rect(context, x + 2, y + 8, 1, height - 16, Theme.get().outline);

        if (selectedModule == null) {
            String message = "Wybierz modul";
            RenderUtil2D.centeredText(context, message, x + width / 2.0, y + height / 2.0, Theme.get().textDim, true);
            return;
        }

        RenderUtil2D.text(context, selectedModule.name(), x + 12, y + 10, 0xFFF2F4FA, true);

        double toggleWidth = 46;
        double toggleX = x + width - toggleWidth - 12;
        boolean toggleHovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth
                && mouseY >= y + 6 && mouseY <= y + 22;
        RenderUtil2D.roundedRect(context, toggleX, y + 6, toggleWidth, 16, 4,
                selectedModule.isEnabled() ? ColorUtil.withAlpha(Theme.get().accent, 200)
                        : (toggleHovered ? 0xFF242A36 : 0xFF1F2430));
        RenderUtil2D.centeredText(context, selectedModule.isEnabled() ? "ON" : "OFF",
                toggleX + toggleWidth / 2.0, y + 10, 0xFFF2F4FA, true);

        double listY = y + 30;
        double listHeight = height - 46;

        settingsScroll += (settingsScrollTarget - settingsScroll) * 0.25;

        double contentHeight = 0;
        for (Object row : rows) {
            if (row instanceof String) contentHeight += 16;
            else if (row instanceof Widget widget) contentHeight += widget.height + 4;
        }
        settingsScrollTarget = Math.max(0, Math.min(settingsScrollTarget, Math.max(0, contentHeight - listHeight + 20)));

        RenderUtil2D.scissor(context, x, listY, width, listHeight, () -> {
            double offset = -settingsScroll;
            for (Object row : rows) {
                if (row instanceof String group) {
                    RenderUtil2D.text(context, group.toUpperCase(), x + 12, listY + offset, Theme.get().accent, true);
                    offset += 16;
                } else if (row instanceof Widget widget) {
                    widget.setPosition(x + 8, listY + offset);
                    widget.render(context, mouseX, mouseY, 1);
                    offset += widget.height + 4;
                }
            }
        });

        drawScrollbar(context, x + width - 4, listY, 2, listHeight,
                (int) (contentHeight / 20), (int) (listHeight / 20), settingsScroll, 20);
    }

    private void drawFooter(DrawContext context, int mouseX, int mouseY, double x, double y) {
        RenderUtil2D.rect(context, x + 10, y, frameWidth - 20, 1, Theme.get().outline);
        String description = selectedModule == null
                ? "Najedz na modul, aby zobaczyc opis"
                : selectedModule.description();
        RenderUtil2D.text(context, description, x + 12, y + 8, Theme.get().textDim, true);
    }

    private void drawScrollbar(DrawContext context, double x, double y, double width, double height,
                               int items, int visible, double scroll, double rowHeight) {
        if (items <= visible || visible <= 0) return;
        double trackHeight = height;
        double thumbHeight = Math.max(20, trackHeight * ((double) visible / items));
        double maxScroll = (items - visible) * rowHeight;
        double progress = maxScroll <= 0 ? 0 : scroll / maxScroll;
        double thumbY = y + (trackHeight - thumbHeight) * MathUtil.clamp(progress, 0, 1);

        RenderUtil2D.roundedRect(context, x, y, width, trackHeight, 1, 0x20FFFFFF);
        RenderUtil2D.roundedRect(context, x, thumbY, width, thumbHeight, 1,
                ColorUtil.withAlpha(Theme.get().accent, 160));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // wyszukiwarka
        double searchX = frameX + SIDEBAR_WIDTH + 8;
        double searchY = frameY + HEADER_HEIGHT + 8;
        if (mouseX >= searchX && mouseX <= searchX + MODULES_WIDTH - 16
                && mouseY >= searchY && mouseY <= searchY + 20) {
            searching = true;
            return true;
        }
        searching = false;

        // kategorie
        int index = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            double rowY = frameY + HEADER_HEIGHT + 10 + index * ROW_HEIGHT;
            if (mouseX >= frameX + 6 && mouseX <= frameX + SIDEBAR_WIDTH - 6
                    && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 4) {
                selectedCategory = category;
                selectedModule = null;
                moduleScrollTarget = 0;
                rebuildSettings();
                return true;
            }
            index++;
        }

        // moduly
        double listY = frameY + HEADER_HEIGHT + 36;
        List<Module> modules = visibleModules();
        int moduleIndex = 0;
        for (Module module : modules) {
            double rowY = listY + moduleIndex * (ROW_HEIGHT - 2) - moduleScroll;
            if (mouseX >= frameX + SIDEBAR_WIDTH + 6 && mouseX <= frameX + SIDEBAR_WIDTH + MODULES_WIDTH - 6
                    && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 4) {
                if (button == 0) {
                    selectedModule = module;
                    settingsScrollTarget = 0;
                    rebuildSettings();
                } else if (button == 1) {
                    module.toggle();
                }
                return true;
            }
            moduleIndex++;
        }

        // przelacznik modulu
        if (selectedModule != null) {
            double settingsX = frameX + SIDEBAR_WIDTH + MODULES_WIDTH;
            double settingsWidth = frameWidth - SIDEBAR_WIDTH - MODULES_WIDTH - 16;
            double toggleX = settingsX + settingsWidth - 46 - 12;
            if (mouseX >= toggleX && mouseX <= toggleX + 46
                    && mouseY >= frameY + HEADER_HEIGHT + 6 && mouseY <= frameY + HEADER_HEIGHT + 22) {
                selectedModule.toggle();
                Ares.get().config().markDirty();
                return true;
            }
        }

        for (Widget widget : widgets) {
            widget.mouseClicked(mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) widget.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Widget widget : widgets) widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double direction = -verticalAmount * 12;
        double settingsX = frameX + SIDEBAR_WIDTH + MODULES_WIDTH;
        if (mouseX > settingsX) settingsScrollTarget += direction;
        else moduleScrollTarget += direction;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searching) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searching = false;
                return true;
            }
            return true;
        }

        for (Widget widget : widgets) widget.keyPressed(keyCode, scanCode, modifiers);

        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (searching) {
            if (character != '\r' && character != '\n') search += character;
            return true;
        }
        for (Widget widget : widgets) widget.charTyped(character, modifiers);
        return super.charTyped(character, modifiers);
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
