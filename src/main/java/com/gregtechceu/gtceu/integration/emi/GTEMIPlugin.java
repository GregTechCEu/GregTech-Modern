package com.gregtechceu.gtceu.integration.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.emi.circuit.GTProgrammedCircuitCategory;
import com.gregtechceu.gtceu.integration.emi.multipage.MultiblockInfoEmiCategory;
import com.gregtechceu.gtceu.integration.emi.oreprocessing.GTOreProcessingEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTBedrockFluidEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTBedrockOreEmiCategory;
import com.gregtechceu.gtceu.integration.emi.orevein.GTOreVeinEmiCategory;
import com.gregtechceu.gtceu.integration.emi.recipe.Ae2PatternTerminalHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipeHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeEMICategory;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.fluids.FluidStack;

import appeng.menu.me.items.PatternEncodingTermMenu;
import de.mari_023.ae2wtlib.wet.WETMenu;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;

@EmiEntrypoint
public class GTEMIPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
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
        if (GTCEu.Mods.isAE2Loaded()) {
            registry.addRecipeHandler(PatternEncodingTermMenu.TYPE, new Ae2PatternTerminalHandler<>());
        }
        if (GTCEu.isModLoaded(GTValues.MODID_AE2WTLIB)) {
            registry.addRecipeHandler(WETMenu.TYPE, new Ae2PatternTerminalHandler<>());
        }
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
        registry.setDefaultComparison(GTItems.TURBINE_ROTOR.asItem(), Comparison.compareNbt());

        registry.setDefaultComparison(GTItems.PROGRAMMED_CIRCUIT.asItem(), Comparison.compareNbt());
        registry.removeEmiStacks(EmiStack.of(GTItems.PROGRAMMED_CIRCUIT.asStack()));
        registry.addEmiStack(EmiStack.of(IntCircuitBehaviour.stack(0)));
        registry.addWorkstation(GTProgrammedCircuitCategory.CATEGORY, EmiStack.of(IntCircuitBehaviour.stack(0)));

        Comparison potionComparison = Comparison.compareData(stack -> PotionUtils.getPotion(stack.getNbt()));
        PotionFluid potionFluid = GTFluids.POTION.get();
        registry.setDefaultComparison(potionFluid.getSource(), potionComparison);
        registry.setDefaultComparison(potionFluid.getFlowing(), potionComparison);

        for (Potion potion : BuiltInRegistries.POTION) {
            FluidStack stack = PotionFluidHelper.getFluidFromPotion(potion, PotionFluidHelper.BOTTLE_AMOUNT);
            registry.addEmiStack(EmiStack.of(stack.getFluid(), stack.getTag()));
        }
    }
}
