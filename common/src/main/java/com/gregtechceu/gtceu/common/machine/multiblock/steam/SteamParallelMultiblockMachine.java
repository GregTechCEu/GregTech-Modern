package com.gregtechceu.gtceu.common.machine.multiblock.steam;

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
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamParallelMultiblockMachine extends WorkableMultiblockMachine implements IDisplayUIMachine {

    public static final int MAX_PARALLELS = 8;

    // if in millibuckets, this is 0.5, Meaning 2mb of steam -> 1 EU
    private static final double CONVERSION_RATE = FluidHelper.getBucket() * 2 / 1000.0D;

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
                    if (!capabilitiesProxy.contains(IO.IN, EURecipeCapability.CAP)) {
                        capabilitiesProxy.put(IO.IN, EURecipeCapability.CAP, new ArrayList<>());
                    }
                    capabilitiesProxy.get(IO.IN, EURecipeCapability.CAP).add(new SteamEnergyRecipeHandler(tank, CONVERSION_RATE));
                    return;
                }
            }
        }
    }

    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        int duration = recipe.duration;
        var eut = RecipeHelper.getInputEUt(recipe);
        var result = GTRecipeModifiers.accurateParallel(machine, recipe, MAX_PARALLELS, false).getA();
        recipe = result == recipe ? result.copy() : result;

        // we remove tick inputs, as our "cost" is just steam now, just stored as EU/t
        // also set the duration to just 1.5x the original, instead of fully multiplied
        recipe.duration = (int) (duration * 1.5);
        eut = (long) Math.min(32, Math.ceil(eut * 1.33));
        recipe.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 1.0f, 0.0f, null, null)));
        return recipe;
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

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.steam.low_steam").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
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
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks), 7, 134, true));
    }
}
