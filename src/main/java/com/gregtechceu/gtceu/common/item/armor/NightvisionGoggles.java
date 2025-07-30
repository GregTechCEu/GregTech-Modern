package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.utils.input.IKeyPressedListener;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NightvisionGoggles extends ArmorLogicSuite implements IKeyPressedListener {

    public NightvisionGoggles(int energyPerUse, long capacity, int voltageTier, ArmorItem.Type slot) {
        super(energyPerUse, capacity, voltageTier, slot);
    }

    @Override
    public void onEquip(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
    }

    @Override
    public void onUnequip(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
    }

    @Override
    public void onKeyPressed(ServerPlayer player, SyncedKeyMapping keyPressed, boolean isDown) {
        if (!isDown) return; // Only handle when key is pressed, not released
        toggleNightVision(player);
    }

    private void toggleNightVision(Player player) {
        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (itemStack.isEmpty()) return;
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item == null) return;

        CompoundTag data = itemStack.getOrCreateTag();
        boolean nightVision = data.contains("nightVision") && data.getBoolean("nightVision");

        nightVision = !nightVision;
        if (item.getCharge() < ArmorUtils.MIN_NIGHTVISION_CHARGE) {
            nightVision = false;
            player.displayClientMessage(Component.translatable("metaarmor.nms.nightvision.error"), true);
        } else {
            player.displayClientMessage(Component
                    .translatable("metaarmor.nms.nightvision." + (nightVision ? "enabled" : "disabled")), true);
        }
        data.putBoolean("nightVision", nightVision);
    }

    @Override
    public void onArmorTick(Level world, @NotNull Player player, @NotNull ItemStack itemStack) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item == null) {
            return;
        }
        CompoundTag data = itemStack.getOrCreateTag();
        int nightVisionTimer = data.contains("nightVisionTimer") ? data.getInt("nightVisionTimer") :
                ArmorUtils.NIGHTVISION_DURATION;
        if (type == ArmorItem.Type.HELMET) {
            if (data.contains("nightVision") && data.getBoolean("nightVision")) {
                player.removeEffect(MobEffects.BLINDNESS);
                if (nightVisionTimer <= ArmorUtils.NIGHT_VISION_RESET) {
                    nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;
                    player.addEffect(
                            new MobEffectInstance(MobEffects.NIGHT_VISION, ArmorUtils.NIGHTVISION_DURATION, 0, true,
                                    false));
                    item.discharge((energyPerUse), this.tier, true, false, false);
                }
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }

        if (nightVisionTimer > 0) nightVisionTimer--;
        data.putInt("nightVisionTimer", nightVisionTimer);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/nightvision_goggles.png");
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        if (type == ArmorItem.Type.HELMET) {
            CompoundTag nbtData = itemStack.getOrCreateTag();
            boolean nv = nbtData.getBoolean("nightVision");
            if (nv) {
                lines.add(Component.translatable("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(Component.translatable("metaarmor.message.nightvision.disabled"));
            }
        }
    }
}
