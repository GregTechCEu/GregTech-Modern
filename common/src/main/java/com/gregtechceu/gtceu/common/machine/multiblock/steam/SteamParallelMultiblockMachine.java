package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SteamParallelMultiblockMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    private static final int MAX_PARALLELS = 8;

    // if in millibuckets, this is 0.5, Meaning 2mb of steam -> 1 EU
    private static final double CONVERSION_RATE = FluidHelper.getBucket() / 2000.0D;

    public SteamParallelMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
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
                    capabilitiesProxy.put(IO.IN, EURecipeCapability.CAP, List.of(new SteamEnergyRecipeHandler(tank, CONVERSION_RATE)));
                    return;
                }
            }
        }
    }

    @Nullable
    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        int duration = recipe.duration;
        var eut = RecipeHelper.getInputEUt(recipe);
        var parallelRecipe = tryParallel(recipe, 1, MAX_PARALLELS);
        if (parallelRecipe != null) recipe = parallelRecipe;

        // we remove tick inputs, as our "cost" is just steam now, just stored as EU/t
        // also set the duration to just 1.5x the original, instead of fully multiplied
        recipe.duration = (int) (duration * 1.5);
        eut = (long) Math.min(32, Math.ceil(eut * 1.33));
        recipe.tickInputs.clear();
        recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 1.0f, null, null)));
        return recipe;
    }

    private GTRecipe tryParallel(GTRecipe original, int min, int max) {
        if (min > max) return null;

        int mid = (min + max) / 2;

        GTRecipe copied = tryCopy(original, ContentModifier.multiplier(mid));
        if (!copied.matchRecipe(this)) {
            // tried too many
            return tryParallel(original, min, mid - 1);
        } else {
            // at max parallels
            if (mid == max) {
                return copied;
            }
            // matches, but try to do more
            GTRecipe tryMore = tryParallel(original, mid + 1, max);
            return tryMore != null ? tryMore : copied;
        }
    }

    // modified recipe.copy(), which doesn't worry about tick inputs or duration,
    // as duration is fixed, and tick inputs are converted to steam cost
    private static GTRecipe tryCopy(GTRecipe original, ContentModifier modifier) {
        var copied = original.copy();
        GTRecipe.modifyContents(copied.inputs, modifier);
        GTRecipe.modifyContents(copied.outputs, modifier);
        GTRecipe.modifyContents(copied.tickOutputs, modifier);
        return copied;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            var handlers = capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP);
            if (handlers != null && handlers.size() > 0 && handlers.get(0) instanceof SteamEnergyRecipeHandler steamHandler) {
                if (steamHandler.getCapacity() > 0) {
                    long steamStored = steamHandler.getStored();
                    textList.add(Component.translatable("gtceu.multiblock.steam.steam_stored", steamStored, steamHandler.getCapacity()));
                }
            }

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
                textList.add(Component.translatable("gtceu.multiblock.parallel", MAX_PARALLELS));
                textList.add(Component.translatable("gtceu.multiblock.progress", currentProgress));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isHasNotEnoughEnergy()) {
                textList.add(Component.translatable("gtceu.multiblock.steam.low_steam").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }
        }
    }

    @Override
    public IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(156)
                .clickHandler(this::handleDisplayClick));
        return new ModularUI(176, 216, this, entityPlayer)
                .background(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks))
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 7, 134, true));
    }
}
