package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
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

    public static final RecipeComponent<CompoundTag> TAG = new RecipeComponent<>() {

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
            return "compound_tag";
        }
    };
    public static final RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new RecipeComponent<>() {

        @Override
        public Codec<ResourceLocation> codec() {
            return ResourceLocation.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(ResourceLocation.class);
        }

        @Override
        public String toString() {
            return "resource_location";
        }
    };
    public static final RecipeComponent<RecipeCapability<?>> RECIPE_CAPABILITY = new RecipeComponent<>() {

        @Override
        public String componentType() {
            return "recipe_capability";
        }

        @Override
        public Class<?> componentClass() {
            return RecipeCapability.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.STRING;
        }

        @Override
        public JsonElement write(RecipeJS recipe, RecipeCapability<?> value) {
            return new JsonPrimitive(GTRegistries.RECIPE_CAPABILITIES.getKey(value));
        }

        @Override
        public RecipeCapability<?> read(RecipeJS recipe, Object from) {
            if (from instanceof RecipeCapability<?> capability) {
                return capability;
            }
            return from instanceof CharSequence c ? GTRegistries.RECIPE_CAPABILITIES.get(c.toString()) :
                    GTRegistries.RECIPE_CAPABILITIES.get(String.valueOf(from));
        }

        @Override
        public String toString() {
            return componentType();
        }
    };
    public static final RecipeComponent<ChanceLogic> CHANCE_LOGIC = new RecipeComponent<>() {

        @Override
        public String componentType() {
            return "chance_logic";
        }

        @Override
        public Class<?> componentClass() {
            return ChanceLogic.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.STRING;
        }

        @Override
        public JsonElement write(RecipeJS recipe, ChanceLogic value) {
            return new JsonPrimitive(GTRegistries.CHANCE_LOGICS.getKey(value));
        }

        @Override
        public ChanceLogic read(RecipeJS recipe, Object from) {
            if (from instanceof ChanceLogic capability) {
                return capability;
            }
            return from instanceof CharSequence c ? GTRegistries.CHANCE_LOGICS.get(c.toString()) :
                    GTRegistries.CHANCE_LOGICS.get(String.valueOf(from));
        }

        @Override
        public String toString() {
            return componentType();
        }
    };

    public static final RecipeComponent<RecipeCondition> RECIPE_CONDITION = new RecipeComponent<>() {

        @Override
        public Codec<RecipeCondition> codec() {
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
    };

    public static final ContentJS<SizedIngredient> ITEM = new ContentJS<>(SizedIngredientComponent.NESTED,
            GTRecipeCapabilities.ITEM);
    public static final ContentJS<SizedFluidIngredient> FLUID = new ContentJS<>(SizedFluidIngredientComponent.NESTED,
            GTRecipeCapabilities.FLUID);
    public static final ContentJS<Long> EU = new ContentJS<>(NumberComponent.LONG, GTRecipeCapabilities.EU);
    public static final ContentJS<Float> SU = new ContentJS<>(NumberComponent.FLOAT, GTRecipeCapabilities.SU);
    public static final ContentJS<Integer> CWU = new ContentJS<>(NumberComponent.INT, GTRecipeCapabilities.CWU);

    public static final CapabilityMapComponent IN = new CapabilityMapComponent(false);
    public static final CapabilityMapComponent TICK_IN = new CapabilityMapComponent(true);
    public static final CapabilityMapComponent OUT = new CapabilityMapComponent(false);
    public static final CapabilityMapComponent TICK_OUT = new CapabilityMapComponent(true);

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
        VALID_CAPS.put(GTRecipeCapabilities.SU, SU);
        VALID_CAPS.put(GTRecipeCapabilities.CWU, CWU);

        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddons().forEach(addon -> addon.registerRecipeKeys(event));
        VALID_CAPS.putAll(event.getRegisteredKeys());
    }
}
