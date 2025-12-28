package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.LongValue;
import com.gregtechceu.gtceu.api.mui.value.sync.*;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamParallelMultiblockMachine extends WorkableMultiblockMachine implements IMuiMachine {

    @Getter
    @Setter
    private int maxParallels = ConfigHolder.INSTANCE.machines.steamMultiParallelAmount;

    @Nullable
    private SteamEnergyRecipeHandler steamEnergy = null;

    // if in millibuckets, this is 2.0, Meaning 2mb of steam -> 1 EU
    public static final double CONVERSION_RATE = 2.0;

    public SteamParallelMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        if (args.length > 0 && args[0] instanceof Integer i) {
            this.maxParallels = i;
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (var part : getParts()) {
            if (!PartAbility.STEAM.isApplicable(part.self().getDefinition().getBlock())) continue;
            var handlers = part.getRecipeHandlers();
            for (var hl : handlers) {
                if (!hl.isValid(IO.IN)) continue;
                for (var fluidHandler : hl.getCapability(FluidRecipeCapability.CAP)) {
                    if (!(fluidHandler instanceof NotifiableFluidTank nft)) continue;
                    if (nft.isFluidValid(0, GTMaterials.Steam.getFluid(1))) {
                        steamEnergy = new SteamEnergyRecipeHandler(nft, getConversionRate());
                        addHandlerList(RecipeHandlerList.of(IO.IN, steamEnergy));
                        return;
                    }
                }
            }
        }
        if (steamEnergy == null) { // No steam hatch found
            onStructureInvalid();
        }
    }

    public double getConversionRate() {
        return CONVERSION_RATE;
    }

    /**
     * Recipe Modifier for <b>Steam Multiblock Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is rejected if tier is greater than LV
     * </p>
     * <p>
     * Recipe is parallelized up to the Multiblock's parallel limit.
     * Then, duration is multiplied by {@code 1.5×} and EUt is multiplied by {@code (8/9) × parallels}, up to a cap of
     * 32 EUt
     * </p>
     *
     * @param machine a {@link SteamParallelMultiblockMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Steam Multiblock Machine and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof SteamParallelMultiblockMachine steamMachine)) {
            return RecipeModifier.nullWrongType(SteamParallelMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV) return ModifierFunction.NULL;

        // Duration = 1.5x base duration
        // EUt (not steam) = (4/3) * (2/3) * parallels * base EUt, up to a max of 32 EUt
        long eut = recipe.getInputEUt().getTotalEU();
        int parallelAmount = ParallelLogic.getParallelAmount(machine, recipe, steamMachine.maxParallels);
        double eutMultiplier = (eut * 0.8888 * parallelAmount <= 32) ? (0.8888 * parallelAmount) : (32.0 / eut);
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallelAmount))
                .outputModifier(ContentModifier.multiplier(parallelAmount))
                .durationMultiplier(1.5)
                .eutMultiplier(eutMultiplier)
                .parallels(parallelAmount)
                .build();
    }

    private LongValue.Dynamic getStored(PanelSyncManager syncManager) {
        LongSyncValue stored = new LongSyncValue(() -> steamEnergy.getStored(), ((ignored) -> {}));
        syncManager.syncValue("stored", stored);
        return new LongValue.Dynamic(stored::getLongValue, stored::setLongValue);
    }

    // @Override
    public void addDisplayText(List<Component> textList, Long stored, Long capacity) {
        // IDisplayUIMachine.super.addDisplayText(textList);
        Style STYLE_WHITE = Style.EMPTY.withColor(ChatFormatting.WHITE);
        if (isFormed()) {
            if (stored != -1 && capacity > 0) {
                textList.add(Component.translatable("gtceu.multiblock.steam.steam_stored", stored,
                        capacity).setStyle(STYLE_WHITE));
            }

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running")
                        .setStyle(STYLE_WHITE));
                if (maxParallels > 1) textList
                        .add(Component.translatable("gtceu.multiblock.parallel", maxParallels).setStyle(STYLE_WHITE));
                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
                double maxInSec = (float) recipeLogic.getDuration() / 20.0f;
                double currentInSec = (float) recipeLogic.getProgress() / 20.0f;
                textList.add(
                        Component.translatable("gtceu.multiblock.progress", String.format("%.2f", (float) currentInSec),
                                String.format("%.2f", (float) maxInSec), currentProgress).setStyle(STYLE_WHITE));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.steam.low_steam")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        } else textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                .setStyle(STYLE_WHITE));
    }

    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ITheme theme = ThemeAPI.INSTANCE.getTheme(getDefinition().getThemeId());
        LongSyncValue stored = new LongSyncValue(() -> (steamEnergy == null) ? -1L : steamEnergy.getStored(),
                (ignored) -> {});
        LongSyncValue capacity = new LongSyncValue(() -> (steamEnergy == null) ? -1L : steamEnergy.getCapacity(),
                (ignored) -> {});
        syncManager.syncValue("stored", stored);
        syncManager.syncValue("capacity", capacity);
        return new ModularPanel(getDefinition().getName())
                // size(200, 172)
                .child(GTMuiWidgets.createTitleBar(getDefinition(), 176,
                        (UITexture) theme.getPanelTheme().getTheme().getBackground()))
                .bindPlayerInventory()
                .child(Flow.row()
                        .coverChildrenHeight()
                        .margin(5)
                        .childPadding(5)
                        .child(Flow.column()
                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                .padding(5)
                                .background(GTGuiTextures.DISPLAY_BRONZE)
                                .height(80)
                                .child(new TextWidget<>(IKey.dynamic(() -> {
                                    List<Component> text = new ArrayList<>();
                                    addDisplayText(text, stored.getValue(), capacity.getValue());
                                    return text.stream()
                                            .map(Component::copy)
                                            .reduce((a, b) -> a.append("\n").append(b))
                                            .orElse(Component.empty());
                                })))));
    }
    /*
     * @Override
     * public IGuiTexture getScreenTexture() {
     * return GuiTextures.DISPLAY_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
     * }
     * 
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
     * screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
     * screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
     * .setMaxWidthLimit(150)
     * .clickHandler(this::handleDisplayClick));
     * return new ModularUI(176, 216, this, entityPlayer)
     * .background(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks))
     * .widget(screen)
     * .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
     * GuiTextures.SLOT_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks), 7, 134,
     * true));
     * }
     */
}
