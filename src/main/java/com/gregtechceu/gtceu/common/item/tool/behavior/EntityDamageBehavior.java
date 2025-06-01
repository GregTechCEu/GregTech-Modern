package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Add to tools to have them deal bonus damage to specific mobs.
 * Pass null for the mobType parameter to ignore the tooltip.
 */
public class EntityDamageBehavior implements IToolBehavior {

    private final List<EntityDamageFunction> shouldDoBonusList = new ArrayList<>();
    private final String mobType;

    public EntityDamageBehavior(float bonus, Class<?>... entities) {
        this(null, bonus, entities);
    }

    public EntityDamageBehavior(Object2FloatMap<Class<?>> entityDamageMap) {
        this(null, entityDamageMap);
    }

    public EntityDamageBehavior(String mobType, float bonus, Class<?>... entities) {
        this.mobType = mobType;
        for (Class<?> entity : entities) {
            shouldDoBonusList.add(damageForClass(entity, bonus));
        }
    }

    public EntityDamageBehavior(String mobType, Object2FloatMap<Class<?>> entityDamageMap) {
        this.mobType = mobType;
        for (var entry : Object2FloatMaps.fastIterable(entityDamageMap)) {
            Class<?> entity = entry.getKey();
            float bonus = entry.getFloatValue();
            shouldDoBonusList.add(damageForClass(entity, bonus));
        }
    }

    @Override
    public void hitEntity(@NotNull ItemStack stack, @NotNull LivingEntity target,
                          @NotNull LivingEntity attacker) {
        float damageBonus = getDamageBonus(target);
        if (damageBonus != 0f) {
            DamageSource source = attacker instanceof Player player ?
                    attacker.damageSources().playerAttack(player) : attacker.damageSources().mobAttack(attacker);
            target.hurt(source, damageBonus);
        }
    }

    private float getDamageBonus(@NotNull LivingEntity target) {
        for (EntityDamageFunction func : shouldDoBonusList) {
            float f = func.damageFor(target);
            if (f > 0) return f;
        }
        return 0f;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        if (mobType != null && !mobType.isEmpty()) {
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.damage_boost",
                    Component.translatable("item.gtceu.tool.behavior.damage_boost_" + mobType)));
        }
    }

    @FunctionalInterface
    private interface EntityDamageFunction {

        float damageFor(LivingEntity e);
    }

    static EntityDamageFunction damageForClass(Class<?> clazz, float damage) {
        return e -> clazz.isAssignableFrom(e.getClass()) ? damage : 0;
    }
}
