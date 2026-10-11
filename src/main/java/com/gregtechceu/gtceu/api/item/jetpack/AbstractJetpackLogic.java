package com.gregtechceu.gtceu.api.item.jetpack;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractJetpackLogic implements IJetpackLogic {

    private static final String ENABLED_TAG = "enabled";
    private static final String HOVER_MODE_TAG = "hover";

    private final JetpackStats stats;
    private final @Nullable JetpackStats hoverStats;

    protected AbstractJetpackLogic(JetpackStats stats, @Nullable JetpackStats hoverStats) {
        this.stats = stats;
        this.hoverStats = hoverStats;
    }

    @Override
    public JetpackStats getJetpackStats(JetpackUseContext context) {
        return isHoverModeEnabled(context.stack()) && hoverStats != null ? hoverStats : stats;
    }

    @Override
    public boolean isFlightEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(ENABLED_TAG) || tag.getBoolean(ENABLED_TAG);
    }

    @Override
    public void setFlightEnabled(ItemStack stack, boolean flightEnabled) {
        stack.getOrCreateTag().putBoolean(ENABLED_TAG, flightEnabled);
    }

    @Override
    public boolean supportsHoverMode(ItemStack stack) {
        return hoverStats != null;
    }

    @Override
    public boolean isHoverModeEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return supportsHoverMode(stack) && tag != null && tag.getBoolean(HOVER_MODE_TAG);
    }

    @Override
    public void setHoverModeEnabled(ItemStack stack, boolean hoverModeEnabled) {
        if (supportsHoverMode(stack)) {
            stack.getOrCreateTag().putBoolean(HOVER_MODE_TAG, hoverModeEnabled);
        }
    }

    private static Component getStatusComponent(boolean enabled) {
        return Component.translatable(enabled ? "metaarmor.hud.status.enabled" : "metaarmor.hud.status.disabled");
    }

    public void addInformation(ItemStack stack, List<Component> tooltipComponents) {
        tooltipComponents.add(Component.translatable("metaarmor.hud.engine_enabled",
                getStatusComponent(isFlightEnabled(stack))));
        if (supportsHoverMode(stack)) {
            tooltipComponents.add(Component.translatable("metaarmor.hud.hover_mode",
                    getStatusComponent(isHoverModeEnabled(stack))));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addHUDLines(ItemStack stack, ArmorUtils.ModularHUD hud) {
        hud.newString(Component.translatable("metaarmor.hud.engine_enabled",
                getStatusComponent(isFlightEnabled(stack))));
        if (supportsHoverMode(stack)) {
            hud.newString(Component.translatable("metaarmor.hud.hover_mode",
                    getStatusComponent(isHoverModeEnabled(stack))));
        }
    }
}
