package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.GTRecipeCapabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.*;

public class GTRecipeComponents {

    public static final RecipeComponent<CompoundTag> TAG = new RecipeComponent<CompoundTag>() {

        @Override
        public Codec<CompoundTag> codec() {
            return CompoundTag.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.RAW_MAP;
        }

        @Override
        public String toString() {
            return "tag";
        }

        @Override
        public RecipeComponentType<CompoundTag> type() {
            return RecipeComponentType.<CompoundTag>unit(ResourceLocation.parse("tag"), this);
        }
    };
    public static final RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new RecipeComponent<ResourceLocation>() {

        @Override
        public Codec<ResourceLocation> codec() {
            return ResourceLocation.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.STRING;
        }

        @Override
        public String toString() {
            return "resource_location";
        }

        @Override
        public RecipeComponentType<ResourceLocation> type() {
            return RecipeComponentType.<ResourceLocation>unit(ResourceLocation.parse("resource_location"), this);
        }
    };
    public static final RecipeComponent<RecipeCapability<?>> RECIPE_CAPABILITY = new RecipeComponent<RecipeCapability<?>>() {

        @Override
        public Codec<RecipeCapability<?>> codec() {
            return RecipeCapability.DIRECT_CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(RecipeCapability.class);
        }

        @Override
        public String toString() {
            return "recipe_capability";
        }

        @Override
        public RecipeComponentType<RecipeCapability<?>> type() {
            return RecipeComponentType.<RecipeCapability<?>>unit(ResourceLocation.parse("recipe_capability"), this);
        }
    };
    public static final RecipeComponent<ChanceLogic> CHANCE_LOGIC = new RecipeComponent<ChanceLogic>() {

        @Override
        public Codec<ChanceLogic> codec() {
            return GTRegistries.CHANCE_LOGICS.byNameCodec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(ChanceLogic.class);
        }

        @Override
        public String toString() {
            return "chance_logic";
        }

        @Override
        public RecipeComponentType<ChanceLogic> type() {
            return RecipeComponentType.<ChanceLogic>unit(ResourceLocation.parse("chance_logic"), this);
        }
    };

    public static final RecipeComponent<RecipeCondition<?>> RECIPE_CONDITION = new RecipeComponent<RecipeCondition<?>>() {

        @Override
        public Codec<RecipeCondition<?>> codec() {
            return RecipeCondition.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(RecipeCondition.class);
        }

        @Override
        public String toString() {
            return "recipe_condition";
        }

        @Override
        public RecipeComponentType<RecipeCondition<?>> type() {
            return RecipeComponentType.<RecipeCondition<?>>unit(ResourceLocation.parse("recipe_condition"), this);
        }
    };

    public static final RecipeComponent<EnergyStack.WithIO> ENERGY_STACK = new RecipeComponent<EnergyStack.WithIO>() {

        @Override
        public Codec<EnergyStack.WithIO> codec() {
            return EnergyStack.WithIO.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(EnergyStack.WithIO.class)
                    .or(TypeInfo.of(EnergyStack.class))
                    .or(TypeInfo.PRIMITIVE_LONG)
                    .or(TypeInfo.LONG);
        }

        @Override
        public String toString() {
            return "energy_stack";
        }

        @Override
        public RecipeComponentType<EnergyStack.WithIO> type() {
            return RecipeComponentType.<EnergyStack.WithIO>unit(ResourceLocation.parse("energy_stack"), this);
        }
    };

    public static final ContentJS<SizedIngredient> ITEM = ContentJS.create(SizedIngredientComponent.SIZED_INGREDIENT,
            GTRecipeCapabilities.ITEM);
    public static final ContentJS<SizedFluidIngredient> FLUID = ContentJS.create(SizedFluidIngredientComponent.NESTED,
            GTRecipeCapabilities.FLUID);
    public static final ContentJS<EnergyStack.WithIO> EU = ContentJS
            .create((RecipeComponentType<EnergyStack.WithIO>) ENERGY_STACK.type(), GTRecipeCapabilities.EU);
    public static final ContentJS<Integer> CWU = ContentJS.create(NumberComponent.NON_NEGATIVE_INT,
            GTRecipeCapabilities.CWU);

    public static final RecipeComponent<Map<RecipeCapability<?>, ChanceLogic>> CHANCE_LOGIC_MAP = new JavaMapRecipeComponent<>(
            RECIPE_CAPABILITY, CHANCE_LOGIC);

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
