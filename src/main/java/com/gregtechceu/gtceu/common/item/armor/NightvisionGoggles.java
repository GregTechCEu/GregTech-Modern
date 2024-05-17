package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    public void onArmorTick(Level world, @NotNull Player player, @NotNull ItemStack itemStack) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item == null) {
            return;
        }
        GTArmor data = itemStack.getOrDefault(GTDataComponents.ARMOR_DATA, new GTArmor());
        byte toggleTimer = data.toggleTimer();
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(itemStack.getItem())) {
            disableNightVision(world, player, false);
        }
        if (type == ArmorItem.Type.HELMET) {
            boolean nightvision = data.nightVision();
            if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
                toggleTimer = 5;
                if (!nightvision && item.getCharge() >= energyPerUse) {
                    nightvision = true;
                    if (!world.isClientSide)
                        player.displayClientMessage(Component.translatable("metaarmor.message.nightvision.enabled"),
                                true);
                } else if (nightvision) {
                    nightvision = false;
                    disableNightVision(world, player, true);
                } else {
                    if (!world.isClientSide) {
                        player.displayClientMessage(Component.translatable("metaarmor.message.nightvision.error"),
                                true);
                    }
                }

                if (!world.isClientSide) {
                    final boolean finalNightvision = nightvision;
                    itemStack.update(GTDataComponents.ARMOR_DATA, new GTArmor(),
                            data1 -> data1.setNightVision(finalNightvision));
                }
            }

            if (nightvision && !world.isClientSide && item.getCharge() >= energyPerUse) {
                player.removeEffect(MobEffects.BLINDNESS);
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 999999, 0, true, false));
                item.discharge((energyPerUse), this.tier, true, false, false);
            }

            if (toggleTimer > 0) --toggleTimer;

            final byte finalToggleTimer = toggleTimer;
            itemStack.update(GTDataComponents.ARMOR_DATA, new GTArmor(),
                    data1 -> data1.setToggleTimer(finalToggleTimer));
        }
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
            GTArmor data = itemStack.getOrDefault(GTDataComponents.ARMOR_DATA, new GTArmor());
            boolean nv = data.nightVision();
            if (nv) {
                lines.add(Component.translatable("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(Component.translatable("metaarmor.message.nightvision.disabled"));
            }
        }
    }
}
