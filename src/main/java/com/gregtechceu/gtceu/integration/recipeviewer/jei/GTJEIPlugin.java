package com.gregtechceu.gtceu.integration.recipeviewer.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein.GTBedrockFluidInfoCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein.GTBedrockOreInfoCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.orevein.GTOreVeinInfoCategory;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.recipe.GTRecipeJEICategory;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.subtype.CircuitSubtypeInterpreter;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.subtype.MaterialSubtypeInterpreter;
import com.gregtechceu.gtceu.integration.recipeviewer.jei.subtype.PotionFluidSubtypeInterpreter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JeiPlugin
public class GTJEIPlugin implements IModPlugin {

    @Getter
    private static IJeiRuntime runtime = null;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return GTCEu.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (!GTCEu.Mods.isJEILoaded()) return;

        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoJeiCategory(jeiHelpers));
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addRecipeCategories(new GTOreProcessingJeiCategory(jeiHelpers));
        registry.addRecipeCategories(new GTOreVeinInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTBedrockFluidInfoCategory(jeiHelpers));
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addRecipeCategories(new GTBedrockOreInfoCategory(jeiHelpers));
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (category.shouldRegisterDisplays()) {
                // registry.addRecipeCategories(new GTRecipeJEICategory(jeiHelpers, category));
            }
        }
        registry.addRecipeCategories(new ProgrammedCircuitJeiCategory(jeiHelpers));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (!GTCEu.Mods.isJEILoaded()) return;

        GTRecipeJEICategory.registerRecipeCatalysts(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingJeiCategory.registerRecipeCatalysts(registration);
        GTOreVeinInfoCategory.registerRecipeCatalysts(registration);
        GTBedrockFluidInfoCategory.registerRecipeCatalysts(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipeCatalysts(registration);
        registration.addRecipeCatalyst(GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack(),
                GTRecipeJEICategory.TYPES.apply(GTRecipeTypes.CHEMICAL_RECIPES.getCategory()));
        registration.addRecipeCatalyst(IntCircuitBehaviour.stack(0), ProgrammedCircuitJeiCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (GTCEu.Mods.isEMILoaded()) return;
        GTCEu.LOGGER.info("JEI register");
        MultiblockInfoJeiCategory.registerRecipes(registration);
        GTRecipeJEICategory.registerRecipes(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingJeiCategory.registerRecipes(registration);
        GTOreVeinInfoCategory.registerRecipes(registration);
        GTBedrockFluidInfoCategory.registerRecipes(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipes(registration);
        registration.addRecipes(ProgrammedCircuitJeiCategory.RECIPE_TYPE,
                List.of(new ProgrammedCircuitJeiCategory.GTProgrammedCircuitWrapper()));
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        if (!GTCEu.Mods.isJEILoaded()) return;

        registration.registerSubtypeInterpreter(GTItems.PROGRAMMED_CIRCUIT.asItem(),
                CircuitSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(GTItems.TURBINE_ROTOR.asItem(), MaterialSubtypeInterpreter.INSTANCE);
    }

    @Override
    public <T> void registerFluidSubtypes(@NotNull ISubtypeRegistration registration,
                                          @NotNull IPlatformFluidHelper<T> platformFluidHelper) {
        if (!GTCEu.Mods.isJEILoaded()) return;

        PotionFluidSubtypeInterpreter interpreter = PotionFluidSubtypeInterpreter.INSTANCE;
        PotionFluid potionFluid = GTFluids.POTION.get();
        registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, potionFluid.getSource(), interpreter);
        registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, potionFluid.getFlowing(), interpreter);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        if (!GTCEu.Mods.isJEILoaded()) return;

        Collection<FluidStack> potionFluids = new ArrayList<>(BuiltInRegistries.POTION.size());
        BuiltInRegistries.POTION.holders().forEach(potion -> {
            FluidStack potionFluid = PotionFluid.of(1000, potion);
            potionFluids.add(potionFluid);
        });
        registration.addExtraIngredients(NeoForgeTypes.FLUID_STACK, potionFluids);
    }
}
