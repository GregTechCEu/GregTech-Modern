package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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
        GTArmor data = stack.get(GTDataComponents.ARMOR_DATA);
        if (data == null) {
            return;
        }
        byte toggleTimer = data.toggleTimer();
        boolean hoverMode = data.hover();
        boolean jetpackEnabled = data.enabled();

        String messageKey = null;
        if (toggleTimer == 0) {
            if (KeyBind.JETPACK_ENABLE.isKeyDown(player)) {
                jetpackEnabled = !jetpackEnabled;
                messageKey = "metaarmor.jetpack.flight." + (jetpackEnabled ? "enable" : "disable");
                final boolean finalEnabled = jetpackEnabled;
                stack.update(GTDataComponents.ARMOR_DATA, new GTArmor(), data1 -> data1.setEnabled(finalEnabled));
            } else if (KeyBind.ARMOR_HOVER.isKeyDown(player)) {
                hoverMode = !hoverMode;
                messageKey = "metaarmor.jetpack.hover." + (hoverMode ? "enable" : "disable");
                final boolean finalHover = hoverMode;
                stack.update(GTDataComponents.ARMOR_DATA, new GTArmor(), data1 -> data1.setHover(finalHover));
            }

            if (messageKey != null) {
                toggleTimer = 5;
                if (!world.isClientSide) player.displayClientMessage(Component.translatable(messageKey), true);
            }
        }

        if (toggleTimer > 0) toggleTimer--;

        if (toggleTimer > 0) toggleTimer--;

        final boolean finalHover = hoverMode;
        final byte finalToggleTimer = toggleTimer;
        final boolean finalEnabled = jetpackEnabled;
        stack.update(GTDataComponents.ARMOR_DATA, new GTArmor(),
                data1 -> data1.setHover(finalHover)
                        .setToggleTimer(finalToggleTimer)
                        .setEnabled(finalEnabled));

        performFlying(player, jetpackEnabled, hoverMode, stack);
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
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
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
        GTArmor data = item.get(GTDataComponents.ARMOR_DATA);
        if (data != null) {
            Component status = data.enabled() ?
                    Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            Component result = Component.translatable("metaarmor.hud.engine_enabled", status);
            this.HUD.newString(result);

            status = data.hover() ?
                    Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            result = Component.translatable("metaarmor.hud.hover_mode", status);
            this.HUD.newString(result);
        }
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        GTArmor data = itemStack.get(GTDataComponents.ARMOR_DATA);
        if (data != null) {
            Component state = data.enabled() ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            lines.add(Component.translatable("metaarmor.hud.engine_enabled", state));

            state = Component.translatable("metaarmor.hud.status.disabled");
            if (data.hover())
                state = Component.translatable("metaarmor.hud.status.enabled");
            lines.add(Component.translatable("metaarmor.hud.hover_mode", state));
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
