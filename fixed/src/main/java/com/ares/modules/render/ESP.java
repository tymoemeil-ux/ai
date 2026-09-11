package com.ares.modules.render;

import com.ares.Ares;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.ColorSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.math.ColorUtil;
import com.ares.core.util.render.RenderUtil3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

/** ESP - podswietlanie bytow przez sciany. */
public final class ESP extends Module {

    public enum Mode { BOX, OUTLINE, FILLED }

    private final ModeSetting<Mode> mode = add(new ModeSetting<>("Mode", "Styl ESP", Mode.BOX).group("General"));
    private final BoolSetting players = add(new BoolSetting("Players", "Gracze", true).group("Targets"));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", "Moby", false).group("Targets"));
    private final BoolSetting friends = add(new BoolSetting("Friends", "Pokazuj znajomych", true).group("Targets"));
    private final BoolSetting healthColor = add(new BoolSetting("Health Color", "Kolor zalezny od HP", true).group("General"));
    private final ColorSetting enemyColor = add(new ColorSetting("Enemy Color", "Kolor wroga", 0xFF22D3EE).group("Colors"));
    private final ColorSetting friendColor = add(new ColorSetting("Friend Color", "Kolor znajomego", 0xFF22C55E).group("Colors"));
    private final ColorSetting mobColor = add(new ColorSetting("Mob Color", "Kolor moba", 0xFFF59E0B).group("Colors"));
    private final FloatSetting range = add(new FloatSetting("Range", "Zasieg ESP", 128f, 8f, 256f).group("General"));

    public ESP() {
        super("ESP", "Podswietla byty przez sciany", ModuleCategory.RENDER);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!Wrapper.nullCheck()) return;

        for (Entity entity : Wrapper.world().getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == Wrapper.player()) continue;
            if (!living.isAlive()) continue;
            if (living.squaredDistanceTo(Wrapper.player()) > range.get() * range.get()) continue;

            boolean isPlayer = living instanceof PlayerEntity;
            if (isPlayer && !players.get()) continue;
            if (!isPlayer && !mobs.get()) continue;

            int color;
            if (isPlayer && Ares.get().friends().isFriend((PlayerEntity) living)) {
                color = friendColor.get();
                if (!friends.get()) continue;
            } else if (isPlayer) {
                color = healthColor.get()
                        ? ColorUtil.healthColor(living.getHealth(), living.getMaxHealth())
                        : enemyColor.get();
            } else {
                color = mobColor.get();
            }

            Box box = living.getBoundingBox().offset(-event.cameraPos().x, -event.cameraPos().y, -event.cameraPos().z);

            switch (mode.get()) {
                case BOX -> {
                    RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, ColorUtil.withAlpha(color, 35));
                    RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), box, color);
                }
                case OUTLINE -> RenderUtil3D.drawBoxOutline(event.matrices(), event.consumers(), box, color);
                case FILLED -> RenderUtil3D.drawFilledBox(event.matrices(), event.consumers(), box, ColorUtil.withAlpha(color, 70));
            }
        }
    }
}
