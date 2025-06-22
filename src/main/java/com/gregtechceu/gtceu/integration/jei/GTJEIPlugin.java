package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.jei.circuit.GTProgrammedCircuitCategory;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory;
import com.gregtechceu.gtceu.integration.jei.oreprocessing.GTOreProcessingInfoCategory;
import com.gregtechceu.gtceu.integration.jei.orevein.GTBedrockFluidInfoCategory;
import com.gregtechceu.gtceu.integration.jei.orevein.GTBedrockOreInfoCategory;
import com.gregtechceu.gtceu.integration.jei.orevein.GTOreVeinInfoCategory;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeJEICategory;
import com.gregtechceu.gtceu.integration.jei.subtype.PotionFluidSubtypeInterpreter;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class GTJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return GTCEu.id("jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (GTCEu.Mods.isREILoaded() || GTCEu.Mods.isEMILoaded()) return;
        GTCEu.LOGGER.info("JEI register categories");
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            registry.addRecipeCategories(new GTOreProcessingInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTOreVeinInfoCategory(jeiHelpers));
        registry.addRecipeCategories(new GTBedrockFluidInfoCategory(jeiHelpers));
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            registry.addRecipeCategories(new GTBedrockOreInfoCategory(jeiHelpers));
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (category.shouldRegisterDisplays()) {
                registry.addRecipeCategories(new GTRecipeJEICategory(jeiHelpers, category));
            }
        }
        registry.addRecipeCategories(new GTProgrammedCircuitCategory(jeiHelpers));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (GTCEu.Mods.isREILoaded() || GTCEu.Mods.isEMILoaded()) return;
        GTRecipeJEICategory.registerRecipeCatalysts(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingInfoCategory.registerRecipeCatalysts(registration);
        GTOreVeinInfoCategory.registerRecipeCatalysts(registration);
        GTBedrockFluidInfoCategory.registerRecipeCatalysts(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipeCatalysts(registration);
        registration.addRecipeCatalyst(GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack(),
                GTRecipeJEICategory.TYPES.apply(GTRecipeTypes.CHEMICAL_RECIPES.getCategory()));
        registration.addRecipeCatalyst(IntCircuitBehaviour.stack(0), GTProgrammedCircuitCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (GTCEu.Mods.isREILoaded() || GTCEu.Mods.isEMILoaded()) return;
        GTCEu.LOGGER.info("JEI register");
        MultiblockInfoCategory.registerRecipes(registration);
        GTRecipeJEICategory.registerRecipes(registration);
        if (!ConfigHolder.INSTANCE.compat.hideOreProcessingDiagrams)
            GTOreProcessingInfoCategory.registerRecipes(registration);
        GTOreVeinInfoCategory.registerRecipes(registration);
        GTBedrockFluidInfoCategory.registerRecipes(registration);
        if (ConfigHolder.INSTANCE.machines.doBedrockOres)
            GTBedrockOreInfoCategory.registerRecipes(registration);
        registration.addRecipes(GTProgrammedCircuitCategory.RECIPE_TYPE,
                List.of(new GTProgrammedCircuitCategory.GTProgrammedCircuitWrapper()));
    }

    @Override
    public void registerIngredients(@NotNull IModIngredientRegistration registry) {
        if (GTCEu.Mods.isREILoaded() || GTCEu.Mods.isEMILoaded()) return;
        GTCEu.LOGGER.info("JEI register ingredients");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(GTItems.PROGRAMMED_CIRCUIT.asItem());
        registration.useNbtForSubtypes(GTItems.TURBINE_ROTOR.asItem());
    }

    @Override
    public <T> void registerFluidSubtypes(ISubtypeRegistration registration,
                                          IPlatformFluidHelper<T> platformFluidHelper) {
        PotionFluidSubtypeInterpreter interpreter = new PotionFluidSubtypeInterpreter();
        PotionFluid potionFluid = GTFluids.POTION.get();
        registration.registerSubtypeInterpreter(ForgeTypes.FLUID_STACK, potionFluid.getSource(), interpreter);
        registration.registerSubtypeInterpreter(ForgeTypes.FLUID_STACK, potionFluid.getFlowing(), interpreter);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        Collection<FluidStack> potionFluids = new ArrayList<>(BuiltInRegistries.POTION.size());
        for (Potion potion : BuiltInRegistries.POTION) {
            FluidStack potionFluid = PotionFluid.of(1000, potion);
            potionFluids.add(potionFluid);
        }
        registration.addExtraIngredients(ForgeTypes.FLUID_STACK, potionFluids);
    }
}
