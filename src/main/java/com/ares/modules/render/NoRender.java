package com.ares.modules.render;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;

/** No Render - wylacza wybrane elementy renderu gry. */
public final class NoRender extends Module {

    private final BoolSetting fire = add(new BoolSetting("Fire", "Ukryj ogien", true).group("General"));
    private final BoolSetting pumpkin = add(new BoolSetting("Pumpkin", "Ukryj dynie na glowie", true).group("General"));
    private final BoolSetting hurtCam = add(new BoolSetting("Hurt Camera", "Brak trzesienia przy obrazeniach", true).group("General"));
    private final BoolSetting bossBar = add(new BoolSetting("Boss Bar", "Ukryj pasek bossa", false).group("General"));
    private final BoolSetting scoreboard = add(new BoolSetting("Scoreboard", "Ukryj scoreboard", false).group("General"));
    private final BoolSetting potionIcons = add(new BoolSetting("Potion Icons", "Ukryj ikony efektow", false).group("General"));
    private final BoolSetting weather = add(new BoolSetting("Weather", "Ukryj deszcz/snieg", true).group("General"));
    private final BoolSetting fog = add(new BoolSetting("Fog", "Ukryj mgle", false).group("General"));
    private final BoolSetting explosion = add(new BoolSetting("Explosions", "Ukryj czasteczki wybuchow", false).group("General"));
    private final BoolSetting particles = add(new BoolSetting("Particles", "Ukryj czasteczki", false).group("General"));
    private final BoolSetting totem = add(new BoolSetting("Totem Animation", "Brak animacji totemu", false).group("General"));

    public NoRender() {
        super("No Render", "Wylacza wybrane efekty wizualne", ModuleCategory.RENDER);
    }

    public boolean fire() {
        return isEnabled() && fire.get();
    }

    public boolean pumpkin() {
        return isEnabled() && pumpkin.get();
    }

    public boolean hurtCam() {
        return isEnabled() && hurtCam.get();
    }

    public boolean bossBar() {
        return isEnabled() && bossBar.get();
    }

    public boolean scoreboard() {
        return isEnabled() && scoreboard.get();
    }

    public boolean potionIcons() {
        return isEnabled() && potionIcons.get();
    }

    public boolean weather() {
        return isEnabled() && weather.get();
    }

    public boolean fog() {
        return isEnabled() && fog.get();
    }

    public boolean explosion() {
        return isEnabled() && explosion.get();
    }

    public boolean particles() {
        return isEnabled() && particles.get();
    }

    public boolean totem() {
        return isEnabled() && totem.get();
    }
}
