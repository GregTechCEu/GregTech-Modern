package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;

public class GTRecipeComponents {
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
        public JsonElement write(RecipeJS recipe, RecipeCapability<?> value) {
            return new JsonPrimitive(value.name);
        }

        @Override
        public RecipeCapability<?> read(RecipeJS recipe, Object from) {
            if (from instanceof RecipeCapability<?> cap) return cap;
            else if (from instanceof CharSequence) return GTRegistries.RECIPE_CAPABILITIES.get(from.toString());
            else if (from instanceof JsonElement json) return GTRegistries.RECIPE_CAPABILITIES.get(json.getAsString());
            return GTRegistries.RECIPE_CAPABILITIES.get(String.valueOf(from));
        }
    };

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


    /**
     * These 2 are strictly required on all components
     */
    public static final RecipeKey<Float> CHANCE = NumberComponent.floatRange(0, 1).key("chance");
    public static final RecipeKey<Float> TIER_CHANCE_BOOST = NumberComponent.floatRange(0, 1).key("tierChanceBoost");


    public static final RecipeKey<InputItem> ITEM_KEY = ItemComponents.INPUT.key("content");
    public static final RecipeKey<InputFluid> FLUID_KEY = FluidComponents.INPUT.key("content");
    public static final RecipeKey<Long> EU_KEY = NumberComponent.ANY_LONG.key("content");
    public static final RecipeKey<Float> STRESS_KEY = NumberComponent.ANY_FLOAT.key("content");

    public static final RecipeComponentBuilder ITEM = RecipeComponent.builder()
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(ITEM_KEY);

    public static final RecipeComponentBuilder FLUID = RecipeComponent.builder()
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(FLUID_KEY);

    public static final RecipeComponentBuilder EU = RecipeComponent.builder()
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(EU_KEY);

    public static final RecipeComponentBuilder STRESS = RecipeComponent.builder()
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(STRESS_KEY);

    public static final RecipeKey<RecipeComponentBuilder.RCBHolder[][]> ITEM_ARRAY_KEY = ITEM.asArray().key("item");
    public static final RecipeKey<RecipeComponentBuilder.RCBHolder[][]> FLUID_ARRAY_KEY = FLUID.asArray().key("fluid");
    public static final RecipeKey<RecipeComponentBuilder.RCBHolder[][]> EU_ARRAY_KEY = EU.asArray().key("eu");
    public static final RecipeKey<RecipeComponentBuilder.RCBHolder[][]> STRESS_ARRAY_KEY = STRESS.asArray().key("su");

    public static final RecipeComponentBuilder ALL_IN = RecipeComponent.builder()
            .add(ITEM_ARRAY_KEY)
            .add(FLUID_ARRAY_KEY)
            .add(EU_ARRAY_KEY)
            .add(STRESS_ARRAY_KEY)
            .inputRole();

    public static final RecipeComponentBuilder ALL_OUT = RecipeComponent.builder()
            .add(ITEM_ARRAY_KEY)
            .add(FLUID_ARRAY_KEY)
            .add(EU_ARRAY_KEY)
            .add(STRESS_ARRAY_KEY)
            .outputRole();

    public static final Map<RecipeCapability<?>, RecipeKey<?>> SINGLE_KEY = new HashMap<>();
    public static final Map<RecipeCapability<?>, RecipeKey<RecipeComponentBuilder.RCBHolder[][]>> ARRAY_KEY = new HashMap<>();

    public static final Map<RecipeKey<RecipeComponentBuilder.RCBHolder[][]>, Integer> INVERSE_LOOKUP = new HashMap<>();

    static {
        SINGLE_KEY.put(ItemRecipeCapability.CAP, ITEM_KEY);
        SINGLE_KEY.put(FluidRecipeCapability.CAP, FLUID_KEY);
        SINGLE_KEY.put(EURecipeCapability.CAP, EU_KEY);
        SINGLE_KEY.put(StressRecipeCapability.CAP, STRESS_KEY);

        ARRAY_KEY.put(ItemRecipeCapability.CAP, ITEM_ARRAY_KEY);
        ARRAY_KEY.put(FluidRecipeCapability.CAP, FLUID_ARRAY_KEY);
        ARRAY_KEY.put(EURecipeCapability.CAP, EU_ARRAY_KEY);
        ARRAY_KEY.put(StressRecipeCapability.CAP, STRESS_ARRAY_KEY);

        INVERSE_LOOKUP.put(ITEM_ARRAY_KEY, 0);
        INVERSE_LOOKUP.put(FLUID_ARRAY_KEY, 1);
        INVERSE_LOOKUP.put(EU_ARRAY_KEY, 2);
        INVERSE_LOOKUP.put(STRESS_ARRAY_KEY, 3);

        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddons().forEach(addon -> addon.registerRecipeKeys(event));

        int index = 4;
        for (Map.Entry<RecipeCapability<?>, RecipeComponent<?>> entry : event.getRegisteredKeys().entrySet()) {
            RecipeCapability<?> key = entry.getKey();

            RecipeKey<?> recipeKey = entry.getValue().key("content");
            SINGLE_KEY.put(key, recipeKey);

            RecipeKey<RecipeComponentBuilder.RCBHolder[][]> object = RecipeComponent.builder()
                    .add(CHANCE)
                    .add(TIER_CHANCE_BOOST)
                    .add(recipeKey)
                    .asArray()
                    .key(key.name);
            ARRAY_KEY.put(key, object);
            ALL_IN.add(object);
            ALL_OUT.add(object);
            INVERSE_LOOKUP.put(object, index++);
        }
    }

}
