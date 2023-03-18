package com.gregtechceu.gtceu.api.data.damagesource;


import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class DamageSources {

    private static final DamageSource EXPLOSION = new DamageSource("explosion").setExplosion();
    private static final DamageSource HEAT = new DamageSource("heat").setIsFire();
    private static final DamageSource FROST = new DamageSource("frost");
    private static final DamageSource CHEMICAL = new DamageSource("chemical").bypassArmor();
    private static final DamageSource ELECTRIC = new DamageSource("electric");
    private static final DamageSource RADIATION = new DamageSource("radiation").bypassArmor();
    private static final DamageSource TURBINE = new DamageSource("turbine");

    public static DamageSource getExplodingDamage() {
        return EXPLOSION;
    }

    public static DamageSource getHeatDamage() {
        return HEAT;
    }

    public static DamageSource getFrostDamage() {
        return FROST;
    }

    public static DamageSource getChemicalDamage() {
        return CHEMICAL;
    }

    public static DamageSource getElectricDamage() {
        return ELECTRIC;
    }

    public static DamageSource getRadioactiveDamage() {
        return RADIATION;
    }

    public static DamageSource getTurbineDamage() {
        return TURBINE;
    }

    // accessed via ASM
    @SuppressWarnings("unused")
    public static DamageSource getPlayerDamage(@Nullable Player source) {
        ItemStack stack = source != null ? source.getMainHandItem() : ItemStack.EMPTY;
        // TODO GT TOOL
//        if (!stack.isEmpty() && stack.getItem() instanceof IGTTool) {
//            IGTTool tool = (IGTTool) stack.getItem();
//            return new DamageSourceTool("player", source, String.format("death.attack.%s", tool.getId()));
//        }
        return new EntityDamageSource("player", source);
    }

    // accessed via ASM
    @SuppressWarnings("unused")
    public static DamageSource getMobDamage(@Nullable LivingEntity source) {
        ItemStack stack = source != null ? source.getItemBySlot(EquipmentSlot.MAINHAND) : ItemStack.EMPTY;
        // TODO GT TOOL
//        if (!stack.isEmpty() && stack.getItem() instanceof IGTTool) {
//            IGTTool tool = (IGTTool) stack.getItem();
//            return new DamageSourceTool("mob", source, String.format("death.attack.%s", tool.getId()));
//        }
        return new EntityDamageSource("mob", source);
    }
}
