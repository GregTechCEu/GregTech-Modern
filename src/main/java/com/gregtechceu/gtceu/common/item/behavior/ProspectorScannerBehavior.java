package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.prospector.ProspectorMapHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProspectorScannerBehavior implements IItemUIHolder, IInteractionItem, IAddInformation {

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
        return modes[stack.getOrDefault(GTDataComponents.SCANNER_MODE, (byte) 0) % modes.length];
    }

    public void setNextMode(ItemStack stack) {
        stack.update(GTDataComponents.SCANNER_MODE, (byte) 0, mode -> (byte) ((mode + 1) % modes.length));
    }

    public boolean drainEnergy(@NotNull ItemStack stack, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;

        int amount = Math.round(this.cost * (ConfigHolder.INSTANCE.machines.prospectorEnergyUseMultiplier / 100F));

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && this.modes.length > 1) {
            if (!level.isClientSide) {
                setNextMode(heldItem);
                ProspectorMode<?> mode = getMode(heldItem);
                player.sendSystemMessage(Component.translatable(mode.unlocalizedName));
            }
            return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
        }
        if (!player.isCreative() && !drainEnergy(item, true)) {
            player.sendSystemMessage(Component.translatable("behavior.prospector.not_enough_energy"));
            return InteractionResultHolder.sidedSuccess(heldItem, level.isClientSide);
        }
        return IItemUIHolder.super.use(item, level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("behavior.prospector.tooltip.radius", this.radius));
        tooltipComponents.add(Component.translatable("behavior.prospector.tooltip.modes"));
        for (ProspectorMode<?> mode : this.modes) {
            tooltipComponents.add(Component.literal(" -")
                    .append(Component.translatable(mode.unlocalizedName))
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> guiData, PanelSyncManager panelSyncManager,
                                   UISettings settings) {
        ProspectorMode<?> mode = getMode(guiData.getPlayer().getItemInHand(InteractionHand.MAIN_HAND));

        StringValue searchValue = new StringValue("");
        DynamicSyncedWidget<?> searchList = new DynamicSyncedWidget<>();

        ProspectorMapHandler<?> mapHandler = new ProspectorMapHandler<>(mode, this.radius, 1, searchValue, searchList,
                panelSyncManager, guiData.getPlayer());

        return ModularPanel.defaultPanel("prospector_scanner", 332, 200)
                .margin(4)
                .child(new ToggleButton()
                        .size(18)
                        .top(4).leftRelAnchor(0f, 1f)
                        .decoration()
                        .stateBackground(GTGuiTextures.PROGRESS_BAR_SOLAR_STEEL)
                        .value(new BoolValue.Dynamic(mapHandler.getTexture()::isDarkMode,
                                mapHandler.getTexture()::setDarkMode)))
                .child(Flow.row()
                        .childPadding(10)
                        .margin(6)
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .child(mapHandler
                                .verticalCenter().left(0))
                        .child(Flow.col()
                                .coverChildrenWidth(136)
                                .top(0).right(0)
                                .child(new TextFieldWidget()
                                        .value(searchValue)
                                        .right(0)
                                        .widthRel(1f).height(16)
                                        .autoUpdateOnChange(true))
                                .child(searchList
                                        .right(0)
                                        .padding(2)
                                        .expanded()
                                        .widthRel(1f)
                                        .background(GTGuiTextures.BACKGROUND_INVERSE))));
    }
}
