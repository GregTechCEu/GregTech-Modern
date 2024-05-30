package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Jetpack extends ArmorLogicSuite implements IJetpack {

    @OnlyIn(Dist.CLIENT)
    protected ArmorUtils.ModularHUD HUD;

    public Jetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, ArmorItem.Type.CHESTPLATE);
        if (Platform.isClient() && this.shouldDrawHUD()) {
            // noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void onArmorTick(Level world, Player player, @NotNull ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag();
        byte toggleTimer = 0;
        boolean hover = false;
        if (data.contains("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.contains("hover")) hover = data.getBoolean("hover");

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            hover = !hover;
            toggleTimer = 5;
            data.putBoolean("hover", hover);
            if (!world.isClientSide) {
                if (hover) player.displayClientMessage(Component.translatable("metaarmor.jetpack.hover.enable"), true);
                else player.displayClientMessage(Component.translatable("metaarmor.jetpack.hover.disable"), true);
            }
        }

        performFlying(player, hover, stack);

        if (toggleTimer > 0) toggleTimer--;

        data.putBoolean("hover", hover);
        data.putByte("toggleTimer", toggleTimer);
    }

    @Override
    public boolean canUseEnergy(@NotNull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.canUse(amount);
    }

    @Override
    public void drainEnergy(@NotNull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return;
        container.discharge(amount, tier, true, false, false);
    }

    @Override
    public boolean hasEnergy(@NotNull ItemStack stack) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.getCharge() > 0;
    }

    @Nullable
    private static IElectricItem getIElectricItem(@NotNull ItemStack stack) {
        return GTCapabilityHelper.getElectricItem(stack);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/jetpack.png");
    }

    /*
     * @Override
     * public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
     * double damage, ArmorItem.Type equipmentSlot) {
     * return new ArmorProperties(0, 0, 0);
     * }
     */

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(ItemStack item, GuiGraphics guiGraphics) {
        addCapacityHUD(item, this.HUD);
        CompoundTag data = item.getTag();
        if (data != null) {
            if (data.contains("hover")) {
                Component status = (data.getBoolean("hover") ? Component.translatable("metaarmor.hud.status.enabled") :
                        Component.translatable("metaarmor.hud.status.disabled"));
                Component result = Component.translatable("metaarmor.hud.hover_mode", status);
                this.HUD.newString(result);
            }
        }
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        CompoundTag data = itemStack.getTag();
        if (data != null) {
            Component status = Component.translatable("metaarmor.hud.status.disabled");
            if (data.contains("hover")) {
                if (data.getBoolean("hover"))
                    status = Component.translatable("metaarmor.hud.status.enabled");
            }
            lines.add(Component.translatable("metaarmor.hud.hover_mode", status));
        }
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.1D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.12D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.3D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.08D;
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.SMOKE;
    }
}
