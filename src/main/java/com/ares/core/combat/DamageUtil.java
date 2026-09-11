package com.ares.core.combat;

import com.ares.core.util.Wrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Kalkulator obrazen od krysztalow i anchorow (kopia logiki wybuchow vanilli). */
public final class DamageUtil {

    private DamageUtil() {
    }

    private static final float CRYSTAL_POWER = 6.0f;
    private static final float ANCHOR_POWER = 5.0f;

    /** Obrazenia krysztalu dla danego bytu. */
    public static float crystal(Entity target, Vec3d crystalPos) {
        return crystal(target, crystalPos, false);
    }

    public static float crystal(Entity target, Vec3d crystalPos, boolean ignoreTerrain) {
        return explosionDamage(target, crystalPos, CRYSTAL_POWER, ignoreTerrain);
    }

    /** Obrazenia anchoru (wybuch o sile 5). */
    public static float anchor(Entity target, Vec3d anchorPos) {
        return explosionDamage(target, anchorPos, ANCHOR_POWER, false);
    }

    public static float explosionDamage(Entity target, Vec3d source, float power, boolean ignoreTerrain) {
        if (target == null || source == null) return 0;
        if (target instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())) return 0;
        if (!(target instanceof LivingEntity living) || living.isDead()) return 0;

        double diameter = power * 2.0;
        Vec3d bodyCenter = target.getPos().add(0, target.getBoundingBox().getLengthY() * 0.5, 0);
        double distance = Math.sqrt(bodyCenter.squaredDistanceTo(source));
        if (distance > diameter) return 0;

        double exposure = exposure(source, target, ignoreTerrain);
        double impact = (1.0 - (distance / diameter)) * exposure;
        float raw = (float) ((impact * impact + impact) / 2.0 * 7.0 * diameter + 1.0);
        return getDamageLeft(raw, living);
    }

    /**
     * Ekspozycja bytu na wybuch (0..1) - dokladnie tak jak w net.minecraft.world.explosion.Explosion.
     */
    public static float exposure(Vec3d source, Entity entity, boolean ignoreTerrain) {
        if (Wrapper.world() == null) return 1;
        if (ignoreTerrain) return 1;

        Box box = entity.getBoundingBox();
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d >= 0) || !(e >= 0) || !(f >= 0)) return 0;

        int hits = 0;
        int misses = 0;
        for (float x = 0; x <= 1.0f; x += d) {
            for (float y = 0; y <= 1.0f; y += e) {
                for (float z = 0; z <= 1.0f; z += f) {
                    double px = MathHelper.lerp(x, box.minX, box.maxX);
                    double py = MathHelper.lerp(y, box.minY, box.maxY);
                    double pz = MathHelper.lerp(z, box.minZ, box.maxZ);
                    Vec3d point = new Vec3d(px + g, py, pz + h);
                    if (Wrapper.world().raycast(new RaycastContext(point, source,
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                        misses++;
                    }
                    hits++;
                }
            }
        }
        return hits == 0 ? 0 : (float) misses / hits;
    }

    /** Finalne obrazenia po pancerzu, enchantach i efektach. */
    public static float getDamageLeft(float damage, LivingEntity entity) {
        damage = applyArmor(damage, entity);
        damage = applyEnchantments(damage, entity);
        damage = applyEffects(damage, entity);
        return Math.max(0, damage);
    }

    private static float applyArmor(float damage, LivingEntity entity) {
        if (entity == null) return damage;
        float armor = entity.getArmor();
        float toughness = armorToughness(entity);
        float f = 2.0f + toughness / 4.0f;
        float f1 = MathHelper.clamp(armor - damage / f, armor * 0.2f, 20.0f);
        return damage * (1.0f - f1 / 25.0f);
    }

    private static float toughnessOf(net.minecraft.item.Item item) {
        if (item == net.minecraft.item.Items.NETHERITE_HELMET
                || item == net.minecraft.item.Items.NETHERITE_CHESTPLATE
                || item == net.minecraft.item.Items.NETHERITE_LEGGINGS
                || item == net.minecraft.item.Items.NETHERITE_BOOTS) return 3f;
        if (item == net.minecraft.item.Items.DIAMOND_HELMET
                || item == net.minecraft.item.Items.DIAMOND_CHESTPLATE
                || item == net.minecraft.item.Items.DIAMOND_LEGGINGS
                || item == net.minecraft.item.Items.DIAMOND_BOOTS) return 2f;
        return 0f;
    }

    /** Twardosc pancerza policzona z zalozonych czesci (bez API atrybutow). */
    public static float armorToughness(LivingEntity entity) {
        float toughness = 0;
        for (net.minecraft.entity.EquipmentSlot slot : new net.minecraft.entity.EquipmentSlot[]{
                net.minecraft.entity.EquipmentSlot.FEET, net.minecraft.entity.EquipmentSlot.LEGS,
                net.minecraft.entity.EquipmentSlot.CHEST, net.minecraft.entity.EquipmentSlot.HEAD}) {
            ItemStack stack = entity.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            net.minecraft.item.Item item = stack.getItem();
            toughness += toughnessOf(item);
        }
        return toughness;
    }

    private static float applyEnchantments(float damage, LivingEntity entity) {
        List<ItemStack> armorItems = new ArrayList<>();
        for (net.minecraft.entity.EquipmentSlot slot : new net.minecraft.entity.EquipmentSlot[]{
                net.minecraft.entity.EquipmentSlot.FEET, net.minecraft.entity.EquipmentSlot.LEGS,
                net.minecraft.entity.EquipmentSlot.CHEST, net.minecraft.entity.EquipmentSlot.HEAD}) {
            ItemStack stack = entity.getEquippedStack(slot);
            if (!stack.isEmpty()) armorItems.add(stack);
        }
        int blast = 0;
        int protection = 0;
        for (ItemStack stack : armorItems) {
            blast += EnchantmentHelper.getLevel(Enchantments.BLAST_PROTECTION, stack);
            protection += EnchantmentHelper.getLevel(Enchantments.PROTECTION, stack);
        }
        blast = Math.min(blast, 20);
        protection = Math.min(protection, 20);

        float reduction = MathHelper.clamp((float) blast, 0, 20) / 25.0f;
        float protReduction = MathHelper.clamp((float) protection, 0, 20) / 25.0f * 0.75f;
        damage *= (1.0f - Math.min(0.8f, reduction + protReduction));
        return Math.max(0, damage);
    }

    private static float applyEffects(float damage, LivingEntity entity) {
        if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
            net.minecraft.entity.effect.StatusEffectInstance effect = entity.getStatusEffect(StatusEffects.RESISTANCE);
            int level = effect == null ? 0 : effect.getAmplifier() + 1;
            damage *= Math.max(0, 1.0f - (level * 0.2f));
        }
        return Math.max(0, damage);
    }

    /** Czy obrazenia zabilyby cel (zdrowie + absorpcja). */
    public static boolean isLethal(float damage, LivingEntity entity) {
        return damage >= entity.getHealth() + entity.getAbsorptionAmount();
    }

    /** Zdrowie celu wraz z absorpcja. */
    public static float totalHealth(LivingEntity entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }
}
