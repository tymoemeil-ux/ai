package com.ares.core.event.events;

import com.ares.core.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Wywolywany po wyrenderowaniu HUDu (2D).
 */
public class Render2DEvent extends Event {
    private final DrawContext context;
    private final float tickDelta;
    private final int width;
    private final int height;

    public Render2DEvent(DrawContext context, RenderTickCounter counter, int width, int height) {
        this.context = context;
        this.tickDelta = counter == null ? 1.0F : counter.getTickProgress(true);
        this.width = width;
        this.height = height;
    }

    public DrawContext context() {
        return context;
    }

    public float tickDelta() {
        return tickDelta;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
