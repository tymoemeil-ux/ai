package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/** Trajectories - przewiduje tor lotu perel / luku / snowballi. */
public final class Trajectories extends Module {

    private final BoolSetting pearls = add(new BoolSetting("Pearls", "Perly", true).group("General"));
    private final BoolSetting arrows = add(new BoolSetting("Arrows", "Strzaly", true).group("General"));
    private final BoolSetting snowballs = add(new BoolSetting("Snowballs", "Sniezki", true).group("General"));
    private final ColorSetting color = add(new ColorSetting("Color", "Kolor toru", 0xFF22D3EE).group("General"));

    public Trajectories() {
        super("Trajectories", "Pokazuje tor lotu przedmiotow", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        Item item = com.ares.core.util.player.InventoryUtil.mainHand().getItem();
        float velocity = 0;
        float gravity = 0;

        if (item == Items.ENDER_PEARL && pearls.get()) {
            velocity = 1.5f;
            gravity = 0.03f;
        } else if ((item == Items.BOW || item == Items.CROSSBOW) && arrows.get()) {
            velocity = 3f;
            gravity = 0.05f;
        } else if ((item == Items.SNOWBALL || item == Items.EGG) && snowballs.get()) {
            velocity = 1.5f;
            gravity = 0.03f;
        } else {
            return;
        }

        Vec3d start = Wrapper.player().getEyePos()
                .add(Wrapper.player().getRotationVector().multiply(0.4));
        Vec3d motion = Wrapper.player().getRotationVector().multiply(velocity);

        Vec3d previous = start.subtract(event.cameraPos());
        Vec3d position = start;

        for (int i = 0; i < 120; i++) {
            position = position.add(motion);
            motion = new Vec3d(motion.x * 0.99, motion.y * 0.99 - gravity, motion.z * 0.99);

            Vec3d current = position.subtract(event.cameraPos());
            RenderUtil3D.drawLine(event.matrices(), event.consumers(), previous, current, color.get());
            previous = current;
        }
    }
}
