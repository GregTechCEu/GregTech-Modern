package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamParallelMultiblockMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    public int maxParallels = ConfigHolder.INSTANCE.machines.steamMultiParallelAmount;

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
        var handlers = capabilitiesProxy.get(IO.IN, FluidRecipeCapability.CAP);
        if (handlers == null) return;
        var itr = handlers.iterator();
        while (itr.hasNext()) {
            var handler = itr.next();
            if (handler instanceof NotifiableFluidTank tank) {
                if (tank.isFluidValid(0, GTMaterials.Steam.getFluid(1))) {
                    itr.remove();
                    if (!capabilitiesProxy.contains(IO.IN, EURecipeCapability.CAP)) {
                        capabilitiesProxy.put(IO.IN, EURecipeCapability.CAP, new ArrayList<>());
                    }
                    capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP)
                            .add(new SteamEnergyRecipeHandler(tank, getConversionRate()));
                    return;
                }
            }
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
        long eut = RecipeHelper.getInputEUt(recipe);
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

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            var handlers = capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP);
            if (handlers != null && handlers.size() > 0 &&
                    handlers.get(0) instanceof SteamEnergyRecipeHandler steamHandler) {
                if (steamHandler.getCapacity() > 0) {
                    long steamStored = steamHandler.getStored();
                    textList.add(Component.translatable("gtceu.multiblock.steam.steam_stored", steamStored,
                            steamHandler.getCapacity()));
                }
            }

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                if (maxParallels > 1)
                    textList.add(Component.translatable("gtceu.multiblock.parallel", maxParallels));
                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
                double maxInSec = (float) recipeLogic.getDuration() / 20.0f;
                double currentInSec = (float) recipeLogic.getProgress() / 20.0f;
                textList.add(
                        Component.translatable("gtceu.multiblock.progress", String.format("%.2f", (float) currentInSec),
                                String.format("%.2f", (float) maxInSec), currentProgress));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.steam.low_steam")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }

    @Override
    public IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(150)
                .clickHandler(this::handleDisplayClick));
        return new ModularUI(176, 216, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks))
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
                        GuiTextures.SLOT_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks), 7, 134,
                        true));
    }
}
