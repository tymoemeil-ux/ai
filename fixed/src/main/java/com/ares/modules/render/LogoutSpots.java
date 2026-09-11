package com.ares.modules.render;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.render.RenderUtil3D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Logout Spots - zapamietuje miejsca, w ktorych znikneli gracze. */
public final class LogoutSpots extends Module {

    private final IntSetting keepTime = add(new IntSetting("Keep Time", "Jak dlugo trzymac (s)", 60, 5, 600).group("General"));
    private final ColorSetting color = add(new ColorSetting("Color", "Kolor", 0x55FF3366).group("General"));

    private final Map<UUID, Vec3d> previousPositions = new HashMap<>();
    private final Map<UUID, Spot> spots = new HashMap<>();

    public LogoutSpots() {
        super("Logout Spots", "Pokazuje miejsca wylogowania graczy", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck()) return;

        Map<UUID, Vec3d> current = new HashMap<>();
        for (PlayerEntity player : com.ares.core.util.world.EntityUtil.otherPlayers()) {
            current.put(player.getUuid(), player.getPos());
        }

        for (Map.Entry<UUID, Vec3d> entry : previousPositions.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                spots.put(entry.getKey(), new Spot(entry.getValue(), System.currentTimeMillis()));
            }
        }

        previousPositions.clear();
        previousPositions.putAll(current);

        long now = System.currentTimeMillis();
        spots.entrySet().removeIf(entry -> now - entry.getValue().time > keepTime.get() * 1000L);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        Iterator<Spot> iterator = spots.values().iterator();
        while (iterator.hasNext()) {
            Spot spot = iterator.next();
            Box box = new Box(spot.position.x - 0.4, spot.position.y, spot.position.z - 0.4,
                    spot.position.x + 0.4, spot.position.y + 1.8, spot.position.z + 0.4)
                    .offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);
            RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, color.get());
            RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), box, 0xFFFF3366);
        }
    }

    private static final class Spot {
        private final Vec3d position;
        private final long time;

        Spot(Vec3d position, long time) {
            this.position = position;
            this.time = time;
        }
    }
}
