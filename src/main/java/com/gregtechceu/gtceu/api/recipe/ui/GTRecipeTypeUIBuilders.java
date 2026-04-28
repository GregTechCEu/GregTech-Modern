package com.gregtechceu.gtceu.api.recipe.ui;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidHolderSetList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemEntryHandler;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GTRecipeTypeUIBuilders {

    private GTRecipeTypeUIBuilders() {}

    public static void addRockBreakerFluidSlots(GTRecipe recipe, Object widgetGroup) {
        var group = (WidgetGroup) widgetGroup;
        List<HolderSet<Fluid>> fluids = new ArrayList<>();
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof AdjacentFluidCondition adjacentFluid) {
                fluids.addAll(adjacentFluid.getOrInitFluids(recipe));
            }
        }
        if (fluids.isEmpty()) {
            return;
        }

        int xOffset = 35;
        int yOffset = 0;
        int i = 0;
        for (HolderSet<Fluid> set : fluids) {
            if (set.size() == 0) {
                continue;
            }
            List<FluidEntryList> slots = Collections.singletonList(FluidHolderSetList.of(set, 1000));
            TankWidget tank = new TankWidget(new CycleFluidEntryHandler(slots),
                    group.getSize().width - 30 - xOffset, group.getSize().height - 30 + yOffset,
                    false, false)
                    .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false);
            group.addWidget(tank);

            i++;
            xOffset = 20 * (2 - (i % 3)) - 5;
            yOffset = 20 * (i / 3);
        }
    }

    public static void addElectricBlastFurnaceCoilSlot(GTRecipe recipe, Object widgetGroup) {
        addHeatingCoilSlot(recipe, widgetGroup, 32);
    }

    public static void addAlloyBlastSmelterCoilSlot(GTRecipe recipe, Object widgetGroup) {
        addHeatingCoilSlot(recipe, widgetGroup, 40);
    }

    public static void addFusionEUToStartLabel(GTRecipe recipe, Object widgetGroup) {
        FusionReactorMachine.addEUToStartLabel(recipe, (WidgetGroup) widgetGroup);
    }

    private static void addHeatingCoilSlot(GTRecipe recipe, Object widgetGroup, int bottomOffset) {
        var group = (WidgetGroup) widgetGroup;
        int temp = recipe.data.getIntOr("ebf_temp", 0);
        List<List<ItemStack>> items = new ArrayList<>();
        items.add(GTCEuAPI.HEATING_COILS.entrySet().stream()
                .filter(coil -> coil.getKey().getCoilTemperature() >= temp)
                .map(coil -> new ItemStack(coil.getValue().get())).toList());
        group.addWidget(new SlotWidget(CycleItemEntryHandler.createFromStacks(items), 0,
                group.getSize().width - 25, group.getSize().height - bottomOffset, false, false));
    }
}
