package com.ares;

import com.ares.core.command.CommandManager;
import com.ares.core.config.ConfigManager;
import com.ares.core.event.EventBus;
import com.ares.core.event.EventHandler;
import com.ares.core.event.events.Render2DEvent;
import com.ares.core.event.events.Render3DEvent;
import com.ares.core.event.events.TickEvent;
import com.ares.core.friend.FriendManager;
import com.ares.core.gui.hud.HudManager;
import com.ares.core.macro.MacroManager;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.notification.NotificationManager;
import com.ares.core.target.PopCounter;
import com.ares.core.util.Wrapper;
import com.ares.modules.client.ClickGuiModule;
import com.ares.modules.client.ColorsModule;
import com.ares.modules.client.FriendsModule;
import com.ares.modules.client.HudEditorModule;
import com.ares.modules.client.HudModule;
import com.ares.modules.client.MacrosModule;
import com.ares.modules.combat.*;
import com.ares.modules.movement.*;
import com.ares.modules.render.*;
import com.ares.modules.utility.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Glowny singleton klienta Ares.
 */
public final class Ares {

    public static final String NAME = "Ares";
    public static final String VERSION = "1.0.0";

    private static final Ares INSTANCE = new Ares();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final EventBus eventBus = EventBus.get();
    private final com.ares.core.module.ModuleManager modules = new com.ares.core.module.ModuleManager();
    private final FriendManager friends = new FriendManager();
    private final PopCounter popCounter = new PopCounter();
    private final NotificationManager notifications = new NotificationManager();
    private final MacroManager macros = new MacroManager();
    private final HudManager hud = new HudManager();
    private final ConfigManager config = new ConfigManager();
    private final CommandManager commands = new CommandManager();

    private final Map<Integer, Boolean> keyStates = new HashMap<>();

    private Path directory;
    private float timerValue = 1f;
    private int ticks;
    private int fps;
    private long lastFrameTime = System.currentTimeMillis();
    private int frameCounter;

    private Ares() {
    }

    public static Ares get() {
        return INSTANCE;
    }

    public MinecraftClient mc() {
        return mc;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public com.ares.core.module.ModuleManager modules() {
        return modules;
    }

    public FriendManager friends() {
        return friends;
    }

    public PopCounter popCounter() {
        return popCounter;
    }

    public NotificationManager notifications() {
        return notifications;
    }

    public MacroManager macros() {
        return macros;
    }

    public HudManager hud() {
        return hud;
    }

    public ConfigManager config() {
        return config;
    }

    public CommandManager commands() {
        return commands;
    }

    public float timerValue() {
        return timerValue;
    }

    public void setTimerValue(float value) {
        // ograniczamy, zeby Timer nigdy nie wygenerowal petli ktora zawiesi gre
        this.timerValue = Math.max(0.1f, Math.min(10f, value));
    }

    public int fps() {
        return fps;
    }

    public int ping() {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return 0;
        PlayerListEntry entry = mc.player == null ? null : handler.getPlayerListEntry(mc.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    /** Obecny cel najwazniejszego modulu bojowego (uzywane przez TargetHUD). */
    public LivingEntity target() {
        AutoCrystal crystal = modules.get(AutoCrystal.class);
        if (crystal != null && crystal.isEnabled() && crystal.target() != null) return crystal.target();
        AutoAnchor anchor = modules.get(AutoAnchor.class);
        if (anchor != null && anchor.isEnabled()) return null;
        return null;
    }

    public void init(Path configDirectory) {
        this.directory = configDirectory;

        registerModules();

        friends.init(configDirectory);
        macros.init(configDirectory);
        config.init(configDirectory);
        boolean firstRun = !java.nio.file.Files.exists(configDirectory.resolve("ares.json"));
        try {
            config.load();
        } catch (Throwable t) {
            System.err.println("[" + NAME + "] Nie udalo sie wczytac configu, startuje z domyslnymi: " + t);
        }
        if (firstRun) applyDefaultPreset();

        eventBus.register(this);
        eventBus.register(popCounter);
        eventBus.register(hud);

        commands.register();
    }

    private void registerModules() {
        // COMBAT
        modules.register(new AutoCrystal());
        modules.register(new AutoAnchor());
        modules.register(new AnchorAura());
        modules.register(new AutoTotem());
        modules.register(new Offhand());
        modules.register(new AutoArmor());
        modules.register(new KillAura());
        modules.register(new Criticals());
        modules.register(new Surround());
        modules.register(new SelfTrap());
        modules.register(new AutoTrap());
        modules.register(new HoleFiller());
        modules.register(new AutoCity());
        modules.register(new AutoWeb());
        modules.register(new Burrow());
        modules.register(new AutoAnvil());
        modules.register(new AutoEXP());
        modules.register(new AutoGap());
        modules.register(new AutoMine());
        modules.register(new Quiver());

        // MOVEMENT
        modules.register(new Speed());
        modules.register(new Sprint());
        modules.register(new NoSlow());
        modules.register(new Velocity());
        modules.register(new Fly());
        modules.register(new ElytraFly());
        modules.register(new Step());
        modules.register(new Jesus());
        modules.register(new Scaffold());
        modules.register(new SafeWalk());
        modules.register(new InventoryMove());
        modules.register(new TimerModule());
        modules.register(new AutoJump());
        modules.register(new NoFall());
        modules.register(new AntiVoid());
        modules.register(new NoPush());

        // RENDER
        modules.register(new ESP());
        modules.register(new Tracers());
        modules.register(new Nametags());
        modules.register(new Chams());
        modules.register(new Fullbright());
        modules.register(new NoRender());
        modules.register(new XRay());
        modules.register(new HoleESP());
        modules.register(new BlockHighlight());
        modules.register(new Freecam());
        modules.register(new Zoom());
        modules.register(new Breadcrumbs());
        modules.register(new Trajectories());
        modules.register(new ItemESP());
        modules.register(new LogoutSpots());
        modules.register(new VoidESP());
        modules.register(new ViewClip());
        modules.register(new NoWeather());
        modules.register(new TimeChanger());
        modules.register(new CustomFOV());

        // UTILITY
        modules.register(new AutoEat());
        modules.register(new AutoTool());
        modules.register(new InventoryCleaner());
        modules.register(new ChestStealer());
        modules.register(new FastPlace());
        modules.register(new NoRotate());
        modules.register(new AutoRespawn());
        modules.register(new ChatSuffix());
        modules.register(new Spammer());
        modules.register(new AntiAFK());
        modules.register(new XCarry());
        modules.register(new Reach());
        modules.register(new Hitboxes());
        modules.register(new AutoLog());
        modules.register(new Notifier());
        modules.register(new MiddleClickFriend());
        modules.register(new HotbarRefill());

        // CLIENT / HUD
        modules.register(new ClickGuiModule());
        modules.register(new HudModule());
        modules.register(new HudEditorModule());
        modules.register(new FriendsModule());
        modules.register(new MacrosModule());
        modules.register(new ColorsModule());
    }

    /**
     * Pierwsze uruchomienie: od razu wlaczamy zestaw CrystalPvP,
     * zeby nie trzeba bylo recznie klikac kazdego modulu.
     */
    private void applyDefaultPreset() {
        enable(com.ares.modules.combat.AutoTotem.class);
        enable(com.ares.modules.combat.Offhand.class);
        enable(com.ares.modules.combat.Surround.class);
        enable(com.ares.modules.combat.AutoCrystal.class);
        enable(com.ares.modules.combat.KillAura.class);
        enable(com.ares.modules.combat.AutoArmor.class);
    }

    private void enable(Class<? extends Module> type) {
        Module module = modules.get(type);
        if (module != null) module.setEnabled(true);
    }

    /**
     * Glowny tick klienta - wywolywany BEZPOSREDNIO z AresMod (nie przez event bus),
     * bo wczesniej ta metoda byla sluchaczem TickEvent i sama publikowala TickEvent,
     * co przy Timerze > 1 konczylo sie nieskonczona rekurencja (StackOverflowError).
     */
    public void tick() {
        ticks++;
        frameCounter++;
        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= 1000) {
            fps = frameCounter;
            frameCounter = 0;
            lastFrameTime = now;
        }

        handleKeybinds();

        if (Wrapper.nullCheck()) {
            // NoRotate: zapamietaj katy, moduly moga je zmienic podczas akcji
            com.ares.modules.utility.NoRotate noRotate = modules.get(com.ares.modules.utility.NoRotate.class);
            boolean restoreRotation = noRotate != null && noRotate.isEnabled();
            if (restoreRotation) noRotate.update();

            // Timer: dodatkowe ticki - twardo ograniczone, zeby nigdy nie zapetlic
            int extra = Math.max(0, Math.min(10, (int) Math.floor(timerValue) - 1));
            for (int i = 0; i <= extra; i++) {
                eventBus.post(new TickEvent.Client());
            }

            if (restoreRotation) noRotate.restore();
        }

        if (config.isDirty() && ticks % 100 == 0) save();
    }

    private void handleKeybinds() {
        if (mc.getWindow() == null) return;
        long handle = mc.getWindow().getHandle();

        for (Module module : modules.all()) {
            int bind = module.bind();
            if (bind == -1) continue; // -1 = brak bindu, <= -100 = przycisk myszy

            boolean pressed;
            if (bind >= 0) {
                pressed = InputUtil.isKeyPressed(handle, bind);
            } else {
                int button = Math.abs(bind) - 100;
                pressed = org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, button)
                        == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            }

            boolean previous = keyStates.getOrDefault(bind, false);
            if (pressed && !previous) {
                module.toggle();
                if (macros.has(bind)) macros.run(bind);
            }
            keyStates.put(bind, pressed);
        }
    }

    public void openClickGui() {
        mc.setScreen(new com.ares.core.gui.clickgui.ClickGuiScreen());
    }

    public void openHudEditor() {
        mc.setScreen(new com.ares.core.gui.hud.HudEditorScreen());
    }

    public void openWindowsGui() {
        mc.setScreen(new com.ares.core.gui.windows.WindowsScreen());
    }

    public void save() {
        config.save();
        friends.save();
        macros.save();
    }

    public void postRender2D(Render2DEvent event) {
        eventBus.post(event);
    }

    public void postRender3D(Render3DEvent event) {
        eventBus.post(event);
    }

    public int ticks() {
        return ticks;
    }

    public Path directory() {
        return directory;
    }

    public Vec3d cameraPos() {
        return mc.player == null ? Vec3d.ZERO : mc.player.getPos();
    }

    public ModuleCategory[] categories() {
        return ModuleCategory.values();
    }
}
