package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NightvisionGoggles extends ArmorLogicSuite {

    public NightvisionGoggles(int energyPerUse, long capacity, int voltageTier, ArmorItem.Type slot) {
        super(energyPerUse, capacity, voltageTier, slot);
    }

    @Override
    public void onArmorTick(Level level, @NotNull Player player, @NotNull ItemStack stack) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
        if (item == null) {
            return;
        }
        GTArmor.Mutable data = stack.getOrDefault(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY).toMutable();
        byte toggleTimer = data.toggleTimer();
        int nightVisionTimer = data.nightVisionTimer();
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(stack.getItem())) {
            disableNightVision(level, player, false);
        }
        if (type == ArmorItem.Type.HELMET) {
            boolean nightVision = data.nightVision();
            if (toggleTimer == 0 && SyncedKeyMappings.ARMOR_MODE_SWITCH.isKeyDown(player)) {
                nightVision = !nightVision;
                toggleTimer = 5;
                if (item.getCharge() < ArmorUtils.MIN_NIGHTVISION_CHARGE) {
                    nightVision = false;
                    player.displayClientMessage(Component.translatable("metaarmor.nms.nightvision.error"), true);
                } else {
                    player.displayClientMessage(Component
                            .translatable("metaarmor.nms.nightvision." + (nightVision ? "enabled" : "disabled")), true);
                }
            }

            if (nightVision) {
                player.removeEffect(MobEffects.BLINDNESS);
                float tickRate = level.tickRateManager().tickrate();
                if (nightVisionTimer <= ArmorUtils.NIGHT_VISION_RESET * tickRate) {
                    nightVisionTimer = Mth.floor(ArmorUtils.NIGHTVISION_DURATION * tickRate);
                    player.addEffect(
                            new MobEffectInstance(MobEffects.NIGHT_VISION, nightVisionTimer, 0, true,
                                    false));
                    item.discharge((energyPerUse), this.tier, true, false, false);
                }
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            data.nightVision(nightVision);

        }

        if (nightVisionTimer > 0) nightVisionTimer--;
        if (toggleTimer > 0) toggleTimer--;

        data.nightVisionTimer(nightVisionTimer);
        data.toggleTimer(toggleTimer);
        stack.set(GTDataComponents.ARMOR_DATA, data.toImmutable());
    }

    public static void disableNightVision(@NotNull Level world, Player player, boolean sendMsg) {
        if (!world.isClientSide) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            if (sendMsg)
                player.displayClientMessage(Component.translatable("metaarmor.message.nightvision.disabled"), true);
        }
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        return GTCEu.id("textures/armor/nightvision_goggles.png");
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        if (type == ArmorItem.Type.HELMET) {
            GTArmor data = itemStack.getOrDefault(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY);
            boolean nv = data.nightVision();
            if (nv) {
                lines.add(Component.translatable("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(Component.translatable("metaarmor.message.nightvision.disabled"));
            }
        }
    }
}
