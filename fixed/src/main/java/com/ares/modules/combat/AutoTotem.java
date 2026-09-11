package com.ares.modules.combat;

import com.ares.core.event.EventHandler;
import com.ares.core.event.events.TickEvent;
import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.IntSetting;
import com.ares.core.setting.FloatSetting;
import com.ares.core.setting.ModeSetting;
import com.ares.core.util.Wrapper;
import com.ares.core.util.player.InventoryUtil;
import com.ares.core.util.player.PlayerUtil;
import com.ares.core.util.timer.TickTimer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/** Auto Totem - automatycznie przenosi totem do offhanda. */
public final class AutoTotem extends Module {

    public enum Mode { ALWAYS, HEALTH, SMART }

    private final ModeSetting<Mode> mode = add(new ModeSetting<>("Mode", "Kiedy przelaczac", Mode.SMART).group("General"));
    private final FloatSetting healthThreshold = add(new FloatSetting("Health", "Ponizej ilu HP przelaczac (tryb Health)", 14f, 1f, 36f).group("General"));
    private final IntSetting delay = add(new IntSetting("Delay", "Opuznienie miedzy akcjami (ticki)", 1, 0, 20).group("General"));
    private final BoolSetting strict = add(new BoolSetting("Strict", "Dzialaj tylko gdy offhand jest pusty", false).group("General"));
    private final BoolSetting noOffhandItemFirst = add(new BoolSetting("Fill Empty", "Wypelniaj pusty offhand nawet w trybie Health", true).group("General"));
    private final BoolSetting notify = add(new BoolSetting("Notify", "Powiadom na czacie gdy brakuje totemow", false).group("General"));
    private final IntSetting minCount = add(new IntSetting("Min Count", "Minimalna liczba totemow aby dzialac", 1, 1, 10).group("General"));

    private final TickTimer timer = new TickTimer();
    private boolean warned;

    public AutoTotem() {
        super("Auto Totem", "Automatycznie zaklada totem niezniszczalnosci do offhanda", ModuleCategory.COMBAT);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (!Wrapper.nullCheck() || !PlayerUtil.isAlive()) return;
        timer.increment();
        if (!timer.passed(delay.get())) return;

        Item offhand = InventoryUtil.offhand().getItem();
        if (offhand == Items.TOTEM_OF_UNDYING) return;

        int count = InventoryUtil.count(Items.TOTEM_OF_UNDYING);
        if (count < minCount.get()) {
            if (notify.get() && !warned) {
                com.ares.Ares.get().notifications().send("Auto Totem", "Brak totemow w ekwipunku!", 3000);
                warned = true;
            }
            return;
        }
        warned = false;

        boolean emptyOffhand = InventoryUtil.offhand().isEmpty();
        boolean lowHealth = PlayerUtil.health() <= healthThreshold.get();

        boolean shouldRun = switch (mode.get()) {
            case ALWAYS -> true;
            case HEALTH -> lowHealth || (noOffhandItemFirst.get() && emptyOffhand);
            case SMART -> emptyOffhand || lowHealth || InventoryUtil.offhand().getItem() != Items.SHIELD;
        };

        if (!shouldRun) return;
        if (strict.get() && !emptyOffhand) return;

        int slot = InventoryUtil.findItem(Items.TOTEM_OF_UNDYING);
        if (slot == -1 || slot == InventoryUtil.OFFHAND_SLOT) return;

        if (slot < 9) {
            InventoryUtil.moveHotbarToOffhand(slot);
        } else {
            InventoryUtil.moveToOffhand(slot);
        }
        timer.reset();
    }
}
