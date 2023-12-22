package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.misc.InputHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class Jetpack extends ArmorLogicSuite implements IJetpack {
    protected ArmorUtils.ModularHUD HUD;

    @Environment(EnvType.CLIENT)
    public Jetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, ArmorItem.Type.CHESTPLATE);
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide() && this.shouldDrawHUD()) {
            //noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void onArmorTick(Level level, Entity entity, @Nonnull ItemStack stack) {
        Player player = (Player) entity;
        CompoundTag data = stack.getOrCreateTag();
        byte toggleTimer = 0;
        boolean hover = false;
        if (data.contains("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.contains("hover")) hover = data.getBoolean("hover");

        if (toggleTimer == 0 && InputHandler.isHoldingDown(player)) {
            hover = !hover;
            toggleTimer = 5;
            data.putBoolean("hover", hover);
            if (!level.isClientSide()) {
                if (hover)
                    player.sendSystemMessage(Component.translatable("metaarmor.jetpack.hover.enable"));
                else
                    player.sendSystemMessage(Component.translatable("metaarmor.jetpack.hover.disable"));
            }
        }

        performFlying(player, hover, stack);

        if (toggleTimer > 0) toggleTimer--;

        data.putBoolean("hover", hover);
        data.putByte("toggleTimer", toggleTimer);
        // player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public boolean canUseEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.canUse(amount);
    }

    @Override
    public void drainEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return;
        container.discharge(amount, tier, true, false, false);
    }

    @Override
    public boolean hasEnergy(@Nonnull ItemStack stack) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.getCharge() > 0;
    }

    private static IElectricItem getIElectricItem(@Nonnull ItemStack stack) {
        return GTCapabilityHelper.getElectricItem(stack);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, ArmorItem.Type slot, String type) {
        return "gregtech:textures/armor/jetpack.png";
    }

    //@Override
    // public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
    //     return new ArmorProperties(0, 0, 0);
    // }

    @Environment(EnvType.CLIENT)
    @Override
    public void drawHUD(ItemStack item) {
        addCapacityHUD(item, this.HUD);
        CompoundTag data = item.getOrCreateTag();
        if (data.contains("hover")) {
            String status = String.valueOf((data.getBoolean("hover") ? Component.translatable("metaarmor.hud.status.enabled") : Component.translatable("metaarmor.hud.status.disabled")));
            Component result = Component.translatable("metaarmor.hud.hover_mode", status);
            this.HUD.newString(result);
        }
        this.HUD.draw(RenderSystem.getModelViewStack());
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        CompoundTag data = itemStack.getOrCreateTag();
        String status = String.valueOf(Component.translatable("metaarmor.hud.status.disabled"));
        if (data.contains("hover")) {
            if (data.getBoolean("hover"))
                status = String.valueOf(Component.translatable("metaarmor.hud.status.enabled"));
        }
        lines.add(Component.translatable("metaarmor.hud.hover_mode", status));
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
    public SimpleParticleType getParticle() {
        return IJetpack.super.getParticle();
    }
}
