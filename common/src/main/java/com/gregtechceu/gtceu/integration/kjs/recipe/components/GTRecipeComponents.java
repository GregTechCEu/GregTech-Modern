package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class GTRecipeComponents {
    public static final RecipeComponent<CompoundTag> TAG = new RecipeComponent<>() {
        @Override
        public String componentType() {
            return "nbt";
        }

        @Override
        public Class<?> componentClass() {
            return CompoundTag.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, CompoundTag value) {
            return NBTUtils.toJson(value);
        }

        @Override
        public CompoundTag read(RecipeJS recipe, Object from) {
            return NBTUtils.toTagCompound(from);
        }
    };
    public static final RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new RecipeComponent<>() {
        @Override
        public String componentType() {
            return "resource_location";
        }

        @Override
        public Class<?> componentClass() {
            return ResourceLocation.class;
        }

        @Override
        public TypeDescJS constructorDescription(DescriptionContext ctx) {
            return TypeDescJS.STRING;
        }

        @Override
        public JsonElement write(RecipeJS recipe, ResourceLocation value) {
            return new JsonPrimitive(value.toString());
        }

        @Override
        public ResourceLocation read(RecipeJS recipe, Object from) {
            return from instanceof CharSequence c ? ResourceLocation.tryParse(c.toString()) : ResourceLocation.tryParse(String.valueOf(from));
        }

        @Override
        public String toString() {
            return componentType();
        }
    };

    public static final RecipeComponent<RecipeCondition> RECIPE_CONDITION = new RecipeComponent<>() {
        @Override
        public String componentType() {
            return "recipe_condition";
        }

        @Override
        public Class<?> componentClass() {
            return RecipeCondition.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, RecipeCondition value) {
            JsonObject object = new JsonObject();
            object.addProperty("type", GTRegistries.RECIPE_CONDITIONS.getKey(value.getClass()));
            object.add("data", value.serialize());
            return object;
        }

        @Override
        public RecipeCondition read(RecipeJS recipe, Object from) {
            if (from instanceof CharSequence) {
                var conditionKey = from.toString();
                var clazz = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (clazz != null) {
                    return RecipeCondition.create(clazz);
                }
            } if (from instanceof JsonPrimitive primitive) {
                var conditionKey = primitive.getAsString();
                var clazz = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (clazz != null) {
                    return RecipeCondition.create(clazz);
                }
            } else if (from instanceof JsonObject jsonObject) {
                var conditionKey = GsonHelper.getAsString(jsonObject, "type", "");
                var clazz = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (clazz != null) {
                    RecipeCondition condition = RecipeCondition.create(clazz);
                    if (condition != null) {
                        return condition.deserialize(GsonHelper.getAsJsonObject(jsonObject, "data", new JsonObject()));
                    }
                }
            } else if (from instanceof Tag tag) {
                return read(recipe, NBTUtils.toJson(tag));
            }
            return null;
        }

        @Override
        public String toString() {
            return componentType();
        }
    };


    public static final ContentJS<InputItem> ITEM_IN = new ContentJS<>(ItemComponents.INPUT, GTRecipeCapabilities.ITEM, false);
    public static final ContentJS<OutputItem> ITEM_OUT = new ContentJS<>(ItemComponents.OUTPUT, GTRecipeCapabilities.ITEM, true);
    public static final ContentJS<InputFluid> FLUID_IN = new ContentJS<>(FluidComponents.INPUT, GTRecipeCapabilities.FLUID, false);
    public static final ContentJS<OutputFluid> FLUID_OUT = new ContentJS<>(FluidComponents.OUTPUT, GTRecipeCapabilities.FLUID, true);
    public static final ContentJS<Long> EU_IN = new ContentJS<>(NumberComponent.ANY_LONG, GTRecipeCapabilities.EU, false);
    public static final ContentJS<Long> EU_OUT = new ContentJS<>(NumberComponent.ANY_LONG, GTRecipeCapabilities.EU, true);
    public static final ContentJS<Float> SU_IN = new ContentJS<>(NumberComponent.ANY_FLOAT, GTRecipeCapabilities.SU, false);
    public static final ContentJS<Float> SU_OUT = new ContentJS<>(NumberComponent.ANY_FLOAT, GTRecipeCapabilities.SU, true);


    public static final CapabilityMapComponent IN = new CapabilityMapComponent(false);
    public static final CapabilityMapComponent TICK_IN = new CapabilityMapComponent(false);
    public static final CapabilityMapComponent OUT = new CapabilityMapComponent(true);
    public static final CapabilityMapComponent TICK_OUT = new CapabilityMapComponent(true);

    public static final Map<RecipeCapability<?>, Pair<ContentJS<?>, ContentJS<?>>> VALID_CAPS = new IdentityHashMap<>();

    static {
        VALID_CAPS.put(GTRecipeCapabilities.ITEM, Pair.of(ITEM_IN, ITEM_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.FLUID, Pair.of(FLUID_IN, FLUID_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.EU, Pair.of(EU_IN, EU_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.SU, Pair.of(SU_IN, SU_OUT));

        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddons().forEach(addon -> addon.registerRecipeKeys(event));
        VALID_CAPS.putAll(event.getRegisteredKeys());
        Set<RecipeCapability<?>> addedCaps = event.getRegisteredKeys().keySet();
        Set<RecipeCapability<?>> registeredCaps = GTRegistries.RECIPE_CAPABILITIES.values();
        registeredCaps.removeAll(addedCaps);
    }

}
