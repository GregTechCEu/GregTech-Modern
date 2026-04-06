package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.gui.widget.ProspectingMapWidget;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (stack.isEmpty()) {
            return this.modes[0];
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return this.modes[0];
        }
        return this.modes[tag.getInt("Mode") % this.modes.length];
    }

    public void setNextMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Mode", (tag.getInt("Mode") + 1) % this.modes.length);
    }

    public boolean drainEnergy(@NotNull ItemStack stack, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;

        int amount = Math.round(this.cost * (ConfigHolder.INSTANCE.machines.prospectorEnergyUseMultiplier / 100F));

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && this.modes.length > 1) {
            if (!level.isClientSide) {
                setNextMode(heldItem);
                ProspectorMode<?> mode = getMode(heldItem);
                player.sendSystemMessage(Component.translatable(mode.unlocalizedName));
            }
            return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
        }
        if (!player.isCreative() && !drainEnergy(heldItem, true)) {
            player.sendSystemMessage(Component.translatable("behavior.prospector.not_enough_energy"));
            return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
        }
        return IItemUIFactory.super.use(item, level, player, usedHand);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        ProspectorMode<?> mode = this.getMode(entityPlayer.getItemInHand(InteractionHand.MAIN_HAND));
        ProspectingMapWidget<?> map = new ProspectingMapWidget<>(4, 4, 332 - 8, 200 - 8, radius, mode, 1);
        return new ModularUI(332, 200, holder, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(map)
                .widget(new SwitchWidget(-20, 4, 18, 18, (cd, pressed) -> map.setDarkMode(pressed))
                        .setSupplier(map::isDarkMode)
                        .setTexture(
                                new GuiTextureGroup(GuiTextures.BUTTON,
                                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy()
                                                .getSubTexture(0, 0.5, 1, 0.5).scale(0.8f)),
                                new GuiTextureGroup(GuiTextures.BUTTON,
                                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy()
                                                .getSubTexture(0, 0, 1, 0.5).scale(0.8f))));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("behavior.prospector.tooltip.radius", this.radius));
        tooltipComponents.add(Component.translatable("behavior.prospector.tooltip.modes"));
        for (ProspectorMode<?> mode : this.modes) {
            tooltipComponents.add(Component.literal(" -")
                    .append(Component.translatable(mode.unlocalizedName))
                    .withStyle(ChatFormatting.RED));
        }
    }
}
