package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.data.recipe.GTRecipeCapabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import dev.latvian.mods.kubejs.recipe.component.*;

import java.util.*;

public class GTRecipeComponents {

    // spotless:off
    public static final RecipeComponent<CompoundTag> NBT_TAG = NbtTagComponent.NBT_TAG.instance();
    public static final RecipeComponent<ResourceLocation> RESOURCE_LOCATION = ResourceLocationComponent.RESOURCE_LOCATION.instance();
    public static final RecipeComponent<RecipeCapability<?>> RECIPE_CAPABILITY = RecipeCapabilityComponent.RECIPE_CAPABILITY.instance();
    public static final RecipeComponent<ChanceLogic> CHANCE_LOGIC = ChanceLogicComponent.CHANCE_LOGIC.instance();
    public static final RecipeComponent<RecipeCondition<?>> RECIPE_CONDITION = RecipeConditionComponent.RECIPE_CONDITION.instance();

    public static final RecipeComponent<EnergyStack.WithIO> ENERGY_STACK = EnergyStackComponent.ENERGY_STACK.instance();

    public static final ContentJS<SizedIngredient> ITEM = ContentJS.create(SizedIngredientComponent.SIZED_INGREDIENT, GTRecipeCapabilities.ITEM);
    public static final ContentJS<SizedFluidIngredient> FLUID = ContentJS.create(SizedFluidIngredientComponent.NESTED, GTRecipeCapabilities.FLUID);
    public static final ContentJS<EnergyStack.WithIO> EU = ContentJS.create(EnergyStackComponent.ENERGY_STACK, GTRecipeCapabilities.EU);
    public static final ContentJS<Integer> CWU = ContentJS.create(NumberComponent.NON_NEGATIVE_INT, GTRecipeCapabilities.CWU);

    public static final RecipeComponent<Map<RecipeCapability<?>, ChanceLogic>> CHANCE_LOGIC_MAP = new JavaMapRecipeComponent<>(RECIPE_CAPABILITY, CHANCE_LOGIC);
    // spotless:on

    /**
     * First in pair is in, second is out
     */
    public static final Map<RecipeCapability<?>, ContentJS<?>> VALID_CAPS = new IdentityHashMap<>();

    static {
        VALID_CAPS.put(GTRecipeCapabilities.ITEM, ITEM);
        VALID_CAPS.put(GTRecipeCapabilities.FLUID, FLUID);
        VALID_CAPS.put(GTRecipeCapabilities.EU, EU);
        VALID_CAPS.put(GTRecipeCapabilities.CWU, CWU);

        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddonList().forEach(addon -> addon.registerRecipeKeys(event));
        VALID_CAPS.putAll(event.getRegisteredKeys());
    }
}
