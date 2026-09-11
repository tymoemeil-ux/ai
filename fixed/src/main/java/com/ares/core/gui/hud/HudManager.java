package com.ares.core.gui.hud;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render2DEvent;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/** Zarzadza elementami HUDu. */
public final class HudManager {

    private final List<HudElement> elements = new ArrayList<>();

    public HudManager() {
        elements.add(new HudElements.Watermark(6, 6));
        elements.add(new HudElements.ModuleList(6, 40));
        elements.add(new HudElements.Coordinates(6, 60));
        elements.add(new HudElements.Fps(6, 78));
        elements.add(new HudElements.Ping(60, 78));
        elements.add(new HudElements.Speed(6, 96));
        elements.add(new HudElements.Armor(6, 114));
        elements.add(new HudElements.Totems(6, 140));
        elements.add(new HudElements.Potions(6, 158));
        elements.add(new HudElements.Keystrokes(6, 200));
        elements.add(new HudElements.TargetHud(120, 200));
        elements.add(new HudElements.Compass(200, 6));
        elements.add(new HudElements.Radar(300, 6));
        elements.add(new HudElements.Notifications(300, 160));
        elements.add(new HudElements.Inventory(140, 300));
    }

    public List<HudElement> elements() {
        return elements;
    }

    public HudElement byName(String name) {
        for (HudElement element : elements) {
            if (element.name().equalsIgnoreCase(name)) return element;
        }
        return null;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (com.ares.Ares.get().mc().currentScreen instanceof com.ares.core.gui.hud.HudEditorScreen) return;
        render(event.context(), event.tickDelta(), event.width(), event.height());
    }

    public void render(net.minecraft.client.gui.DrawContext context, float tickDelta, int width, int height) {
        com.ares.Ares.get().notifications().update();
        for (HudElement element : elements) {
            if (!element.enabled()) continue;
            // Uwaga: nie nadpisujemy pozycji elementu (kiedys byla tu "klamrowana" do ekranu,
            // przez co elementy uciekaly w lewy gorny rog i psuly zapisany uklad).
            double x = element.x();
            double y = element.y();
            com.ares.core.util.render.RenderUtil2D.scaled(context, x, y, element.scale(), () -> {
                element.drawBackground(context);
                element.render(context, tickDelta);
            });
        }
    }

    public void save(JsonObject root) {
        for (HudElement element : elements) {
            JsonObject data = new JsonObject();
            data.addProperty("x", element.x());
            data.addProperty("y", element.y());
            data.addProperty("enabled", element.enabled());
            data.addProperty("scale", element.scale());
            data.addProperty("background", element.background());
            root.add(element.name(), data);
        }
    }

    public void load(JsonObject root) {
        for (HudElement element : elements) {
            JsonObject data = root.getAsJsonObject(element.name());
            if (data == null) continue;
            if (data.has("x")) element.setPosition(data.get("x").getAsDouble(), element.y());
            if (data.has("y")) element.setPosition(element.x(), data.get("y").getAsDouble());
            if (data.has("enabled")) element.setEnabled(data.get("enabled").getAsBoolean());
            if (data.has("scale")) element.setScale(data.get("scale").getAsDouble());
            if (data.has("background")) element.setBackground(data.get("background").getAsBoolean());
        }
    }
}
