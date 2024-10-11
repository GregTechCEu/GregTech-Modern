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
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamParallelMultiblockMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    public int maxParallels = ConfigHolder.INSTANCE.machines.steamMultiParallelAmount;

    // if in millibuckets, this is 0.5, Meaning 2mb of steam -> 1 EU
    private static final double CONVERSION_RATE = 0.5D;

    public SteamParallelMultiblockMachine(IMachineBlockEntity holder) {
        this(holder, ConfigHolder.INSTANCE.machines.steamMultiParallelAmount);
    }

    public SteamParallelMultiblockMachine(IMachineBlockEntity holder, int parallelAmount) {
        super(holder);
        maxParallels = parallelAmount;
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
                            .add(new SteamEnergyRecipeHandler(tank, CONVERSION_RATE));
                    return;
                }
            }
        }
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                          @NotNull OCResult result) {
        if (machine instanceof SteamParallelMultiblockMachine steamMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV) {
                return null;
            }
            int duration = recipe.duration;
            var eut = RecipeHelper.getInputEUt(recipe);
            var parallelRecipe = GTRecipeModifiers.accurateParallel(machine, recipe, steamMachine.maxParallels, false);

            // we remove tick inputs, as our "cost" is just steam now, just stored as EU/t
            // also set the duration to just 1.5x the original, instead of fully multiplied
            result.init((long) Math.min(32, Math.ceil(eut * 1.33)), (int) (duration * 1.5), parallelRecipe.getSecond(),
                    params.getOcAmount());
            return recipe;
        }
        return null;
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
