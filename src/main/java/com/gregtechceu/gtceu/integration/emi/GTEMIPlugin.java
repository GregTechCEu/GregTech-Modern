package com.gregtechceu.gtceu.integration.emi;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.emi.circuit.GTProgrammedCircuitCategory;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.emi.oreprocessing.GTOreProcessingEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTBedrockFluidEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTBedrockOreEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTOreVeinEmiCategory;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipeHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeEMICategory;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.List;

@EmiEntrypoint
public class GTEMIPlugin implements EmiPlugin {

    @ApiStatus.Internal
    public static List<MachineDefinition> SORTED_MACHINES = null;

    @Override
    public void register(EmiRegistry registry) {
        if (SORTED_MACHINES == null) {
            SORTED_MACHINES = GTRegistries.MACHINES.stream()
                    .sorted(SORT_MACHINES_BY_TIER)
                    .toList();
        }

        // Categories
        registry.addCategory(MultiblockInfoEmiCategory.CATEGORY);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addCategory(GTOreProcessingEmiCategory.CATEGORY);
        registry.addCategory(GTOreVeinEmiCategory.CATEGORY);
        registry.addCategory(GTBedrockFluidEmiCategory.CATEGORY);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addCategory(GTBedrockOreEmiCategory.CATEGORY);
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (category.shouldRegisterDisplays()) {
                registry.addCategory(GTRecipeEMICategory.CATEGORIES.apply(category));
            }
        }
        registry.addRecipeHandler(ModularUIContainer.MENUTYPE, new GTEmiRecipeHandler());
        registry.addCategory(GTProgrammedCircuitCategory.CATEGORY);

        // Recipes
        MultiblockInfoEmiCategory.registerDisplays(registry);
        GTRecipeEMICategory.registerDisplays(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingEmiCategory.registerDisplays(registry);
        GTOreVeinEmiCategory.registerDisplays(registry);
        GTBedrockFluidEmiCategory.registerDisplays(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreEmiCategory.registerDisplays(registry);
        GTProgrammedCircuitCategory.registerDisplays(registry);

        // workstations
        GTRecipeEMICategory.registerWorkStations(registry);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingEmiCategory.registerWorkStations(registry);
        GTOreVeinEmiCategory.registerWorkStations(registry);
        GTBedrockFluidEmiCategory.registerWorkStations(registry);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreEmiCategory.registerWorkStations(registry);
        registry.addWorkstation(GTRecipeEMICategory.CATEGORIES.apply(GTRecipeTypes.CHEMICAL_RECIPES.getCategory()),
                EmiStack.of(GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack()));

        // Comparators
        registry.setDefaultComparison(GTItems.TURBINE_ROTOR.asItem(), Comparison.compareComponents());

        registry.setDefaultComparison(GTItems.PROGRAMMED_CIRCUIT.asItem(), Comparison.compareComponents());
        registry.removeEmiStacks(EmiStack.of(GTItems.PROGRAMMED_CIRCUIT.asStack()));
        registry.addEmiStack(EmiStack.of(IntCircuitBehaviour.stack(0)));
        registry.addWorkstation(GTProgrammedCircuitCategory.CATEGORY, EmiStack.of(IntCircuitBehaviour.stack(0)));

        Comparison potionComparison = Comparison.compareData(
                stack -> stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
        PotionFluid potionFluid = GTFluids.POTION.get();
        registry.setDefaultComparison(potionFluid.getSource(), potionComparison);
        registry.setDefaultComparison(potionFluid.getFlowing(), potionComparison);

        BuiltInRegistries.POTION.holders().forEach(potion -> {
            FluidStack stack = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);
            registry.addEmiStack(EmiStack.of(stack.getFluid(), stack.getComponentsPatch()));
        });
    }

    public static final Comparator<MachineDefinition> SORT_MACHINES_BY_TIER = (a, b) -> {
        boolean isAMulti = a instanceof MultiblockMachineDefinition;
        boolean isBMulti = b instanceof MultiblockMachineDefinition;
        if (isAMulti && !isBMulti) {
            return 1;
        } else if (!isAMulti && isBMulti) {
            return -1;
        } else {
            return a.getTier() - b.getTier();
        }
    };
}
