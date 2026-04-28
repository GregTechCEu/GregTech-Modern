package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProspectorScannerBehavior implements IItemUIFactory, IInteractionItem, IAddInformation {

    private final int radius;
    private final long cost;
    private final ProspectorMode<?>[] modes;

    public ProspectorScannerBehavior(int radius, long cost, ProspectorMode<?>... modes) {
        this.radius = radius + 1;
        this.modes = Arrays.stream(modes).filter(Objects::nonNull).toArray(ProspectorMode[]::new);
        this.cost = cost;
    }

    @NotNull
    public ProspectorMode<?> getMode(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return modes[0];
        }
        return modes[stack.getOrDefault(GTDataComponents.SCANNER_MODE, (byte) 0) % modes.length];
    }

    public void setNextMode(ItemStack stack) {
        stack.update(GTDataComponents.SCANNER_MODE, (byte) 0, mode -> (byte) ((mode + 1) % modes.length));
    }

    public boolean drainEnergy(@NotNull ItemStack stack, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;

        int amount = Math.round(cost * (ConfigHolder.INSTANCE.machines.prospectorEnergyUseMultiplier / 100F));

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public InteractionResult use(ItemStack item, Level level, Player player,
                                 InteractionHand usedHand) {
        if (player.isShiftKeyDown() && modes.length > 1) {
            if (!level.isClientSide()) {
                setNextMode(item);
                var mode = getMode(item);
                player.sendSystemMessage(Component.translatable(mode.unlocalizedName));
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(item);
        }
        if (!player.isCreative() && !drainEnergy(item, true)) {
            player.sendSystemMessage(Component.translatable("behavior.prospector.not_enough_energy"));
            return InteractionResult.SUCCESS.heldItemTransformedTo(item);
        }
        return IItemUIFactory.super.use(item, level, player, usedHand);
    }

    @Override
    public Object createUI(GTHeldItemUIHolder holder, Player entityPlayer) {
        return ProspectorScannerBehaviorUI.create(holder, entityPlayer, radius, getMode(holder.getHeld()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("metaitem.prospector.tooltip.radius", radius));
        tooltipComponents.add(Component.translatable("metaitem.prospector.tooltip.modes"));
        for (ProspectorMode<?> mode : modes) {
            tooltipComponents.add(Component.literal(" -").append(Component.translatable(mode.unlocalizedName))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
    }
}
