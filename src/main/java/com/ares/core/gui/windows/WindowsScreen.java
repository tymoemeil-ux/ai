package com.ares.core.gui.windows;

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
import com.ares.core.util.render.RenderUtil2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Alternatywny interfejs: przesuwane okna dla kazdej kategorii. */
public final class WindowsScreen extends Screen {

    private final List<Window> windows = new ArrayList<>();
    private final Map<Module, Animation> animations = new HashMap<>();
    private Window dragging;
    private double dragX;
    private double dragY;

    public WindowsScreen() {
        super(Text.literal("Ares Windows"));
    }

    @Override
    protected void init() {
        super.init();
        windows.clear();

        int index = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            double x = 20 + index * 150;
            double y = 30 + (index % 3) * 40;
            windows.add(new Window(category, x, y, 140));
            index++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil2D.rect(context, 0, 0, width, height, 0xA80A0C12);

        for (Window window : windows) {
            renderWindow(context, window, mouseX, mouseY);
        }

        String hint = "LPM: przelacz  |  PPM: rozwin ustawienia  |  Przeciagnij pasek aby przesunac";
        RenderUtil2D.text(context, hint, 10, height - 14, Theme.get().textDim, true);
    }

    private void renderWindow(DrawContext context, Window window, int mouseX, int mouseY) {
        List<Module> modules = Ares.get().modules().byCategory(window.category);
        window.expandedModules.removeIf(module -> !module.isEnabled());

        double header = 22;
        double contentHeight = 0;
        for (Module module : modules) {
            contentHeight += 20;
            if (window.expandedModules.contains(module)) {
                for (Setting<?> setting : module.settings()) {
                    if (!setting.isVisible()) continue;
                    contentHeight += settingHeight(setting);
                }
            }
        }

        double windowHeight = header + contentHeight + 4;

        RenderUtil2D.shadow(context, window.x, window.y, window.width, windowHeight, 4);
        RenderUtil2D.roundedRect(context, window.x, window.y, window.width, windowHeight, Theme.get().radius,
                Theme.get().background);
        RenderUtil2D.roundedOutline(context, window.x, window.y, window.width, windowHeight, Theme.get().radius,
                Theme.get().outline);

        RenderUtil2D.horizontalGradient(context, window.x, window.y, window.width, header,
                Theme.get().accent, Theme.get().accentSecondary);
        RenderUtil2D.text(context, window.category.displayName(), window.x + 6, window.y + 7, 0xFF101018, false);

        double y = window.y + header + 2;
        for (Module module : modules) {
            boolean hovered = mouseX >= window.x && mouseX <= window.x + window.width
                    && mouseY >= y && mouseY <= y + 18;

            Animation animation = animations.computeIfAbsent(module, k -> new Animation(module.isEnabled() ? 1 : 0));
            animation.update(module.isEnabled(), 0.2);

            int background = com.ares.core.util.math.MathUtil.lerpColor(
                    hovered ? 0xFF1F2430 : 0x00000000,
                    ColorUtil.withAlpha(Theme.get().accent, 90), animation.value());
            RenderUtil2D.roundedRect(context, window.x + 3, y, window.width - 6, 18, 3, background);

            RenderUtil2D.text(context, module.name(), window.x + 8, y + 5,
                    module.isEnabled() ? 0xFFF2F4FA : Theme.get().textDim, true);

            y += 20;

            if (window.expandedModules.contains(module)) {
                for (Setting<?> setting : module.settings()) {
                    if (!setting.isVisible()) continue;
                    Widget widget = widgetFor(setting, window.x + 6, y, window.width - 12);
                    if (widget == null) continue;
                    widget.render(context, mouseX, mouseY, 1);
                    y += widget.height + 2;
                }
            }
        }
    }

    private double settingHeight(Setting<?> setting) {
        if (setting instanceof NumberSetting<?>) return 32;
        return 22;
    }

    private Widget widgetFor(Setting<?> setting, double x, double y, double width) {
        if (setting instanceof BoolSetting bool) return new BooleanWidget(bool, x, y, width);
        if (setting instanceof NumberSetting<?> number) return new SliderWidget((NumberSetting<? extends Number>) number, x, y, width);
        if (setting instanceof ModeSetting<?> mode) return new ModeWidget((ModeSetting<? extends Enum<?>>) mode, x, y, width);
        if (setting instanceof ColorSetting color) return new ColorWidget(color, x, y, width);
        if (setting instanceof StringSetting string) return new StringWidget(string, x, y, width);
        if (setting instanceof BindSetting bind) return new BindWidget(bind, x, y, width);
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // okna od gory (ostatnie rysowane) do dolu
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            double header = 22;
            if (mouseX >= window.x && mouseX <= window.x + window.width
                    && mouseY >= window.y && mouseY <= window.y + header) {
                dragging = window;
                dragX = mouseX - window.x;
                dragY = mouseY - window.y;
                windows.remove(i);
                windows.add(0, window);
                return true;
            }

            double y = window.y + header + 2;
            for (Module module : Ares.get().modules().byCategory(window.category)) {
                if (mouseX >= window.x && mouseX <= window.x + window.width && mouseY >= y && mouseY <= y + 18) {
                    if (button == 0) module.toggle();
                    else if (button == 1) {
                        if (window.expandedModules.contains(module)) window.expandedModules.remove(module);
                        else window.expandedModules.add(module);
                    }
                    Ares.get().config().markDirty();
                    return true;
                }
                y += 20;

                if (window.expandedModules.contains(module)) {
                    for (Setting<?> setting : module.settings()) {
                        if (!setting.isVisible()) continue;
                        Widget widget = widgetFor(setting, window.x + 6, y, window.width - 12);
                        if (widget == null) continue;
                        if (mouseX >= widget.x && mouseX <= widget.x + widget.width
                                && mouseY >= widget.y && mouseY <= widget.y + widget.height) {
                            widget.mouseClicked(mouseX, mouseY, button);
                            Ares.get().config().markDirty();
                            return true;
                        }
                        y += widget.height + 2;
                    }
                }
            }
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
            dragging.x = Math.max(0, Math.min(width - dragging.width, mouseX - dragX));
            dragging.y = Math.max(0, Math.min(height - 30, mouseY - dragY));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
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

    private static final class Window {
        private final ModuleCategory category;
        private final List<Module> expandedModules = new ArrayList<>();
        private double x;
        private double y;
        private final double width;

        Window(ModuleCategory category, double x, double y, double width) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
        }
    }
}
