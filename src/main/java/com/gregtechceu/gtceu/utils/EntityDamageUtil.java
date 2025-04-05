package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import org.jetbrains.annotations.NotNull;

public class EntityDamageUtil {

    /**
     * @param entity      the entity to damage
     * @param temperature the temperature of the fluid in the pipe
     * @param multiplier  the multiplier on the damage taken
     * @param maximum     the maximum damage to apply to the entity, use -1 for no maximum
     */
    public static void applyTemperatureDamage(@NotNull LivingEntity entity, int temperature, float multiplier,
                                              int maximum) {
        if (temperature > 320) {
            int damage = (int) ((multiplier * (temperature - 300)) / 50.0F);
            if (maximum > 0) {
                damage = Math.min(maximum, damage);
            }
            applyHeatDamage(entity, damage);
        } else if (temperature < 260) {
            int damage = (int) ((multiplier * (273 - temperature)) / 25.0F);
            if (maximum > 0) {
                damage = Math.min(maximum, damage);
            }
            applyFrostDamage(entity, damage);
        }
    }

    /**
     * @param entity the entity to damage
     * @param damage the damage to apply
     */
    public static void applyHeatDamage(@NotNull LivingEntity entity, int damage) {
        // do not attempt to damage by 0
        if (damage <= 0) return;
        if (!entity.isAlive()) return;
        // fire/lava mobs cannot be burned
        if (entity.getType().is(CustomTags.HEAT_IMMUNE))
            return;
        // fire resistance entities cannot be burned
        if (entity.getEffect(MobEffects.FIRE_RESISTANCE) != null) return;

        entity.hurt(GTDamageTypes.HEAT.source(entity.level()), damage);
        // TODO advancements
        // if (entity instanceof ServerPlayer serverPlayer)
        // AdvancementTriggers.HEAT_DEATH.trigger(serverPlayer);
    }

    /**
     * @param entity the entity to damage
     * @param damage the damage to apply
     */
    public static void applyFrostDamage(@NotNull LivingEntity entity, int damage) {
        // do not attempt to damage by 0
        if (damage <= 0) return;
        if (!entity.isAlive()) return;
        // snow/frost mobs cannot be chilled
        if (entity.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES))
            return;
        // frost walker entities cannot be chilled
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.FEET);
        // check for empty in order to force damage to be applied if armor breaks
        if (!stack.isEmpty()) {
            if (stack.getEnchantmentLevel(Enchantments.FROST_WALKER) > 0) {
                stack.hurtAndBreak(1, entity, ent -> ent.broadcastBreakEvent(EquipmentSlot.FEET));
                return;
            }
        }

        entity.hurt(entity.damageSources().freeze(), damage);
        // TODO advancements
        // if (entity instanceof ServerPlayer) {
        // AdvancementTriggers.COLD_DEATH.trigger((ServerPlayer) entity);
        // }
    }

    /**
     * @param entity the entity to damage
     * @param damage the damage to apply
     */
    public static void applyChemicalDamage(@NotNull LivingEntity entity, int damage) {
        // do not attempt to damage by 0
        if (damage <= 0) return;
        if (!entity.isAlive()) return;
        // skeletons cannot breathe in the toxins
        if (entity.getType().is(CustomTags.CHEMICAL_IMMUNE))
            return;

        entity.hurt(GTDamageTypes.CHEMICAL.source(entity.level()), damage);
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, damage * 100, 1));
        // TODO advancements
        // if (entity instanceof ServerPlayer) AdvancementTriggers.CHEMICAL_DEATH.trigger((ServerPlayer) entity);
    }
}
