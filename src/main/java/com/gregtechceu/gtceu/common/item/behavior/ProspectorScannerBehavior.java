package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.widgets.prospector.ProspectorMapHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;

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

import brachy.modularui.api.IThemeApi;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.DynamicSyncedWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProspectorScannerBehavior implements IItemUIHolder, IInteractionItem, IAddInformation {

    private static final UITexture DARK_MODE_BUTTON_INACTIVE = GTGuiTextures.PROGRESS_BAR_SOLAR_STEEL
            .getSubArea(0, 0.5f, 1, 0.5f);
    private static final UITexture DARK_MODE_BUTTON_ACTIVE = GTGuiTextures.PROGRESS_BAR_SOLAR_STEEL
            .getSubArea(0, 0, 1, 0.5f);

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
        return IItemUIHolder.super.use(item, level, player, usedHand);
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

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> guiData, PanelSyncManager panelSyncManager,
                                   UISettings settings) {
        ProspectorMode<?> mode = getMode(guiData.getPlayer().getItemInHand(InteractionHand.MAIN_HAND));
        final int diameter = radius * 2 - 1;

        StringValue searchValue = new StringValue("");

        DynamicSyncedWidget<?> searchList;

        var panel = ModularPanel.defaultPanel("prospector_scanner", 332, 200)
                .margin(4)
                .child(Flow.col()
                        .leftRel(1.0f)
                        .child(new TextFieldWidget()
                                .value(searchValue)
                                .height(16)
                                .widthRel(1f)
                                .autoUpdateOnChange(true))
                        .child(searchList = new DynamicSyncedWidget<>()));

        ProspectorMapHandler<?> mapHandler = new ProspectorMapHandler<>(mode, radius, 1, searchValue, searchList,
                panelSyncManager);

        panel.child(Flow.col()
                .topRel(0.5f).leftRel(0.0f)
                .size(diameter)
                .margin(2)
                .background(GTGuiTextures.BACKGROUND_INVERSE)
                .child(mapHandler)
                .child(new ButtonWidget<>().widgetTheme(IThemeApi.BUTTON)
                        .top(0).leftRelAnchor(0.0f, 1.0f)
                        .margin(2)
                        .backgroundOverlay(new DynamicDrawable(() -> {
                            if (mapHandler.getTexture().isDarkMode()) {
                                return DARK_MODE_BUTTON_ACTIVE;
                            } else {
                                return DARK_MODE_BUTTON_INACTIVE;
                            }
                        }))
                        .onMousePressed((mouseX, mouseY, button) -> {
                            if (button == 0 || button == 1) {
                                mapHandler.getTexture().toggleDarkMode();
                                return true;
                            }
                            return false;
                        })));

        return panel;
    }
}
