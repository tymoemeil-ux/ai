package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import com.ares.core.util.world.BlockUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** Void ESP - podswietla dziury do pustki. */
public final class VoidESP extends Module {

    private final IntSetting range = add(new IntSetting("Range", "Promien", 8, 2, 32).group("General"));
    private final ColorSetting color = add(new ColorSetting("Color", "Kolor", 0x44AA22FF).group("General"));

    public VoidESP() {
        super("Void ESP", "Podswietla dziury prowadzace do pustki", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;
        if (Wrapper.world().getBottomY() >= Wrapper.player().getBlockY() - range.get()) return;

        BlockPos center = Wrapper.player().getBlockPos();
        int bottom = Wrapper.world().getBottomY();

        for (int x = -range.get(); x <= range.get(); x++) {
            for (int z = -range.get(); z <= range.get(); z++) {
                boolean empty = true;
                for (int y = center.getY(); y > bottom; y--) {
                    if (!BlockUtil.isAir(new BlockPos(center.getX() + x, y, center.getZ() + z))) {
                        empty = false;
                        break;
                    }
                }
                if (!empty) continue;

                BlockPos pos = new BlockPos(center.getX() + x, bottom + 1, center.getZ() + z);
                Box box = new Box(pos).offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);
                RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, color.get());
            }
        }
    }
}
