package com.ares.core.gui.hud;

import com.ares.Ares;
import com.ares.core.gui.theme.Theme;
import com.ares.core.module.Module;
import com.ares.core.util.Wrapper;
import com.ares.core.util.math.ColorUtil;
import com.ares.core.util.math.MathUtil;
import com.ares.core.util.render.RenderUtil2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

/** Wszystkie wbudowane elementy HUDu w stylu Aoba. */
public final class HudElements {

    private HudElements() {
    }

    /** Watermark z animowanym gradientem. */
    public static class Watermark extends HudElement {
        public Watermark(double x, double y) {
            super("Watermark", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            String text = Ares.NAME + " §7" + Ares.VERSION;
            double width = RenderUtil2D.textWidth(text) + 8;
            setSize(width, 14);
            RenderUtil2D.horizontalGradient(context, x, y, width, 12, Theme.get().accent, Theme.get().accentSecondary);
            RenderUtil2D.text(context, text, x + 4, y + 2, 0xFF0A0A0F, false);
        }
    }

    /** Lista aktywnych modulow posortowana po szerokosci. */
    public static class ModuleList extends HudElement {
        private boolean right;

        public ModuleList(double x, double y) {
            super("ArrayList", x, y);
        }

        public void setRight(boolean right) {
            this.right = right;
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            List<Module> active = new ArrayList<>();
            for (Module module : Ares.get().modules().all()) {
                if (module.isVisibleInArray()) active.add(module);
            }
            active.sort(Comparator.comparingInt((Module m) -> -RenderUtil2D.textWidth(m.name())).thenComparing(Module::name));

            int screenWidth = context.getScaledWindowWidth();
            double maxWidth = 0;
            int index = 0;
            for (Module module : active) {
                String text = module.name() + (module.info() == null ? "" : " §7" + module.info());
                double textWidth = RenderUtil2D.textWidth(text);
                double drawX = right ? screenWidth - textWidth - 6 : x;
                double drawY = y + index * 12;
                int color = ColorUtil.rainbow(module.colorIndex() * 40L, 0.7f, 1f);

                RenderUtil2D.rect(context, drawX - 2, drawY, textWidth + 4, 11, 0x70080810);
                RenderUtil2D.rect(context, drawX - 3, drawY, 1, 11, color);
                RenderUtil2D.text(context, text, drawX, drawY + 2, color, true);

                maxWidth = Math.max(maxWidth, textWidth + 4);
                index++;
            }
            setSize(maxWidth, Math.max(12, active.size() * 12));
        }
    }

    /** Wspolrzedne XYZ. */
    public static class Coordinates extends HudElement {
        public Coordinates(double x, double y) {
            super("Coordinates", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;
            Vec3d pos = player.getPos();
            String text = String.format("XYZ §7%.1f §f/ §7%.1f §f/ §7%.1f", pos.x, pos.y, pos.z);
            RenderUtil2D.text(context, text, x, y, Theme.get().text, true);
            setSize(RenderUtil2D.textWidth(text), 10);
        }
    }

    /** Licznik FPS. */
    public static class Fps extends HudElement {
        public Fps(double x, double y) {
            super("FPS", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            int fps = Ares.get().fps();
            String text = "FPS §7" + fps;
            RenderUtil2D.text(context, text, x, y, fps >= 60 ? Theme.get().text : Theme.get().text, true);
            setSize(RenderUtil2D.textWidth(text), 10);
        }
    }

    /** Ping i TPS. */
    public static class Ping extends HudElement {
        public Ping(double x, double y) {
            super("Ping", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            String text = "Ping §7" + Ares.get().ping() + "ms";
            RenderUtil2D.text(context, text, x, y, Theme.get().text, true);
            setSize(RenderUtil2D.textWidth(text), 10);
        }
    }

    /** Predkosc w blokach na sekunde. */
    public static class Speed extends HudElement {
        public Speed(double x, double y) {
            super("Speed", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;
            Vec3d velocity = player.getVelocity();
            double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20;
            String text = String.format("Speed §7%.2f §fb/s", speed);
            RenderUtil2D.text(context, text, x, y, Theme.get().text, true);
            setSize(RenderUtil2D.textWidth(text), 10);
        }
    }

    /** Pancerz i wytrzymalosc. */
    public static class Armor extends HudElement {
        public Armor(double x, double y) {
            super("Armor", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;

            List<ItemStack> items = new ArrayList<>();
            for (net.minecraft.entity.EquipmentSlot slot : new net.minecraft.entity.EquipmentSlot[]{
                    net.minecraft.entity.EquipmentSlot.HEAD, net.minecraft.entity.EquipmentSlot.CHEST,
                    net.minecraft.entity.EquipmentSlot.LEGS, net.minecraft.entity.EquipmentSlot.FEET}) {
                ItemStack stack = player.getEquippedStack(slot);
                if (!stack.isEmpty()) items.add(stack);
            }
            items.add(player.getOffHandStack());
            items.add(player.getMainHandStack());

            int offset = 0;
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;
                context.drawItem(stack, (int) x + offset, (int) y);
                offset += 18;
            }
            setSize(Math.max(18, offset), 18);
        }
    }

    /** Licznik totemow i popniec. */
    public static class Totems extends HudElement {
        public Totems(double x, double y) {
            super("Totems", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            int count = com.ares.core.util.player.InventoryUtil.count(net.minecraft.item.Items.TOTEM_OF_UNDYING);
            String text = "Totems §7" + count;
            RenderUtil2D.text(context, text, x, y, count > 0 ? Theme.get().text : 0xFFFF5555, true);
            setSize(RenderUtil2D.textWidth(text), 10);
        }
    }

    /** Aktywne efekty. */
    public static class Potions extends HudElement {
        public Potions(double x, double y) {
            super("Potions", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;

            int index = 0;
            for (net.minecraft.entity.effect.StatusEffectInstance effect : player.getStatusEffects()) {
                if (!effect.shouldShowParticles()) continue;
                String name = effect.getEffectType().value().getName().getString();
                int level = effect.getAmplifier() + 1;
                int seconds = effect.getDuration() / 20;
                String text = String.format("%s %d §7(%02d:%02d)", name, level, seconds / 60, seconds % 60);
                RenderUtil2D.text(context, text, x, y + index * 11, Theme.get().text, true);
                setSize(Math.max(width(), RenderUtil2D.textWidth(text)), (index + 1) * 11);
                index++;
            }
            if (index == 0) setSize(20, 10);
        }
    }

    /** Powiadomienia. */
    public static class Notifications extends HudElement {
        public Notifications(double x, double y) {
            super("Notifications", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            List<com.ares.core.notification.NotificationManager.Notification> notifications =
                    Ares.get().notifications().active();
            int index = 0;
            for (com.ares.core.notification.NotificationManager.Notification notification : notifications) {
                double progress = notification.progress();
                int alpha = (int) (255 * (1 - MathUtil.clamp((progress - 0.8) / 0.2, 0, 1)));
                int width = 130;
                RenderUtil2D.roundedRect(context, x, y + index * 30, width, 26, 4,
                        ColorUtil.withAlpha(Theme.get().panel, alpha));
                RenderUtil2D.rect(context, x, y + index * 30, 2, 26, ColorUtil.withAlpha(Theme.get().accent, alpha));
                RenderUtil2D.text(context, notification.title, x + 6, y + index * 30 + 4,
                        ColorUtil.withAlpha(Theme.get().accent, alpha), true);
                RenderUtil2D.text(context, notification.message, x + 6, y + index * 30 + 14,
                        ColorUtil.withAlpha(Theme.get().text, alpha), true);
                index++;
            }
            setSize(130, Math.max(10, notifications.size() * 30));
        }
    }

    /** Cel: hp, dystans, pancerz. */
    public static class TargetHud extends HudElement {
        public TargetHud(double x, double y) {
            super("TargetHUD", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            net.minecraft.entity.LivingEntity target = Ares.get().target();
            if (target == null || !target.isAlive()) {
                setSize(0, 0);
                return;
            }

            int width = 120;
            int height = 46;
            setSize(width, height);

            RenderUtil2D.roundedRect(context, x, y, width, height, 5, Theme.get().panel);
            RenderUtil2D.roundedOutline(context, x, y, width, height, 5, Theme.get().outline);

            String name = target.getName().getString();
            float health = target.getHealth() + target.getAbsorptionAmount();
            float maxHealth = target.getMaxHealth();
            double distance = Wrapper.player() == null ? 0 : Wrapper.player().distanceTo(target);

            RenderUtil2D.text(context, name, x + 6, y + 5, Theme.get().text, true);

            // pasek zdrowia
            double barWidth = width - 12;
            RenderUtil2D.rect(context, x + 6, y + 20, barWidth, 5, 0x40000000);
            RenderUtil2D.rect(context, x + 6, y + 20, barWidth * MathUtil.clamp(health / maxHealth, 0, 1), 5,
                    ColorUtil.healthColor(health, maxHealth));
            RenderUtil2D.text(context, String.format("%.1f HP", health), x + 6, y + 28, Theme.get().textDim, true);
            RenderUtil2D.text(context, String.format("%.1fm", distance), x + width - 6 - RenderUtil2D.textWidth(String.format("%.1fm", distance)),
                    y + 28, Theme.get().textDim, true);

            int pops = Ares.get().popCounter().pops(target.getUuid());
            if (pops > 0) {
                RenderUtil2D.text(context, "pops: " + pops, x + 6, y + 36, 0xFFFFCC00, true);
            }
        }
    }

    /** Kompas. */
    public static class Compass extends HudElement {
        public Compass(double x, double y) {
            super("Compass", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;

            int width = 90;
            setSize(width, 14);
            float yaw = MathUtil.wrapDegrees(player.getYaw()) + 180;
            double center = x + width / 2.0;

            RenderUtil2D.rect(context, x, y + 5, width, 1, 0x40FFFFFF);

            String[] directions = {"N", "E", "S", "W"};
            int[] angles = {180, 270, 0, 90};

            for (int i = 0; i < 4; i++) {
                float delta = MathUtil.wrapDegrees(angles[i] - yaw + 180);
                double offset = (delta / 180.0) * (width / 2.0);
                if (Math.abs(offset) > width / 2.0) continue;
                int textWidth = RenderUtil2D.textWidth(directions[i]);
                RenderUtil2D.text(context, directions[i], center + offset - textWidth / 2.0, y,
                        i == 0 ? 0xFFFF5555 : Theme.get().text, true);
            }
            RenderUtil2D.rect(context, center, y + 2, 1, 8, Theme.get().accent);
        }
    }

    /** Prosty radar 2D. */
    public static class Radar extends HudElement {
        public Radar(double x, double y) {
            super("Radar", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            int size = 80;
            setSize(size, size);
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;

            RenderUtil2D.roundedRect(context, x, y, size, size, 5, Theme.get().panel);
            RenderUtil2D.roundedOutline(context, x, y, size, size, 5, Theme.get().outline);

            double centerX = x + size / 2.0;
            double centerY = y + size / 2.0;
            float yaw = (float) Math.toRadians(player.getYaw());
            double range = 50;

            for (net.minecraft.entity.player.PlayerEntity other : com.ares.core.util.world.EntityUtil.otherPlayers()) {
                double dx = other.getX() - player.getX();
                double dz = other.getZ() - player.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > range) continue;

                double angle = Math.atan2(dz, dx) - yaw;
                double pointX = centerX + Math.cos(angle) * (distance / range) * (size / 2.0 - 4);
                double pointY = centerY + Math.sin(angle) * (distance / range) * (size / 2.0 - 4);

                int color = Ares.get().friends().isFriend(other) ? 0xFF22C55E : 0xFFFF5555;
                RenderUtil2D.rect(context, pointX - 1.5, pointY - 1.5, 3, 3, color);
            }

            RenderUtil2D.rect(context, centerX - 1, centerY - 1, 2, 2, 0xFFFFFFFF);
        }
    }

    /** Klawisze WASD. */
    public static class Keystrokes extends HudElement {
        public Keystrokes(double x, double y) {
            super("Keystrokes", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            int size = 18;
            setSize(size * 3 + 8, size * 3 + 8);

            boolean forward = Wrapper.mc().options.forwardKey.isPressed();
            boolean back = Wrapper.mc().options.backKey.isPressed();
            boolean left = Wrapper.mc().options.leftKey.isPressed();
            boolean right = Wrapper.mc().options.rightKey.isPressed();
            boolean jump = Wrapper.mc().options.jumpKey.isPressed();
            boolean sneak = Wrapper.mc().options.sneakKey.isPressed();

            key(context, x + size + 4, y, size, size, "W", forward);
            key(context, x, y + size + 4, size, size, "A", left);
            key(context, x + size + 4, y + size + 4, size, size, "S", back);
            key(context, x + (size + 4) * 2, y + size + 4, size, size, "D", right);
            key(context, x, y + (size + 4) * 2, size * 3 + 8, size / 2 + 4, "Shift", sneak);
            key(context, x + (size + 4) / 2, y + (size + 4) * 2, size * 2, size / 2 + 4, "Jump", jump);
        }

        private void key(DrawContext context, double x, double y, double w, double h, String label, boolean pressed) {
            int fill = pressed ? ColorUtil.withAlpha(Theme.get().accent, 180) : 0x50000000;
            RenderUtil2D.roundedRect(context, x, y, w, h, 3, fill);
            RenderUtil2D.roundedOutline(context, x, y, w, h, 3, Theme.get().outline);
            RenderUtil2D.centeredText(context, label, x + w / 2.0, y + h / 2.0 - 3,
                    pressed ? 0xFF101018 : Theme.get().textDim, false);
        }
    }

    /** Podglad ekwipunku. */
    public static class Inventory extends HudElement {
        public Inventory(double x, double y) {
            super("Inventory", x, y);
        }

        @Override
        public void render(DrawContext context, float tickDelta) {
            ClientPlayerEntity player = Wrapper.player();
            if (player == null) return;

            int slotSize = 16;
            int columns = 9;
            setSize(columns * slotSize, 3 * slotSize);

            int index = 0;
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < columns; column++) {
                    ItemStack stack = player.getInventory().getStack(9 + index);
                    double slotX = x + column * slotSize;
                    double slotY = y + row * slotSize;
                    RenderUtil2D.rect(context, slotX, slotY, slotSize, slotSize,
                            stack.isEmpty() ? 0x30000000 : 0x50FFFFFF);
                    if (!stack.isEmpty()) {
                        context.drawItem(stack, (int) slotX + 1, (int) slotY + 1);
                    }
                    index++;
                }
            }
        }
    }
}
