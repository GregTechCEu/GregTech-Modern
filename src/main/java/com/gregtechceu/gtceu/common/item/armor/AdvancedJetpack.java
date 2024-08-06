package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class AdvancedJetpack extends Jetpack {

    public AdvancedJetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(Level world, Player player, @NotNull ItemStack stack) {
        IElectricItem cont = GTCapabilityHelper.getElectricItem(stack);
        if (cont == null) {
            return;
        }
        CompoundTag data = stack.getOrCreateTag();
        boolean hoverMode = data.contains("hover") && data.getBoolean("hover");
        byte toggleTimer = data.contains("toggleTimer") ? data.getByte("toggleTimer") : 0;
        boolean jetpackEnabled = !data.contains("enabled") || data.getBoolean("enabled");

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            hoverMode = !hoverMode;
            toggleTimer = 5;
            data.putBoolean("hover", hoverMode);
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("metaarmor.jetpack.hover." + (hoverMode ? "enable" : "disable")), true);
            }
        }

        if (toggleTimer == 0 && KeyBind.JETPACK_ENABLE.isKeyDown(player)) {
            jetpackEnabled = !jetpackEnabled;
            toggleTimer = 5;
            data.putBoolean("enabled", jetpackEnabled);
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("metaarmor.jetpack.flight." + (jetpackEnabled ? "enable" : "disable")),
                        true);
            }
        }

        performFlying(player, jetpackEnabled, hoverMode, stack);

        if (toggleTimer > 0) toggleTimer--;

        data.putBoolean("hover", hoverMode);
        data.putBoolean("enabled", jetpackEnabled);
        data.putByte("toggleTimer", toggleTimer);
    }

    @Override
    public double getSprintEnergyModifier() {
        return 2.5D;
    }

    @Override
    public double getSprintSpeedModifier() {
        return 1.3D;
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.34D;
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.03D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.13D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.48D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.14D;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.CLOUD;
    }

    @Override
    public float getFallDamageReduction() {
        return 2.0f;
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/advanced_jetpack.png");
    }
}
