package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
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
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GTRecipeComponents {
    public static final RecipeComponentWithParent<InputItem> ITEM_OUT = new RecipeComponentWithParent<InputItem>() {
        @Override
        public RecipeComponent<InputItem> parentComponent() {
            return ItemComponents.INPUT;
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.OUTPUT;
        }

        @Override
        public boolean isOutput(RecipeJS recipe, InputItem value, ReplacementMatch match) {
            if (match instanceof ItemMatch m) {
                return !value.isEmpty() && m.contains(value.ingredient);
            }

            return false;
        }

        @Override
        public @Nullable JsonElement write(RecipeJS recipe, InputItem value) {
            JsonElement element = RecipeComponentWithParent.super.write(recipe, value);
            if (element instanceof JsonObject object) {
                object.addProperty("type", SizedIngredient.TYPE.toString());
                object.addProperty("fabric:type", SizedIngredient.TYPE.toString());
                object.addProperty("count", value.count);
            }
            return element;
        }

        @Override
        public InputItem replaceOutput(RecipeJS recipe, InputItem original, ReplacementMatch match, OutputReplacement with) {
            return isOutput(recipe, original, match) ? read(recipe, replaceOutput(recipe, match, original, with)) : original;
        }

        public InputItem replaceOutput(RecipeJS recipe, ReplacementMatch match, InputItem original, OutputReplacement with) {
            if (with instanceof OutputItem output) {
                return original.withCount(output.getCount());
            }

            return original;
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


    public static final RecipeComponentBuilder ITEM = new RecipeComponentBuilder(2)
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(ItemComponents.INPUT.key("content"));

    public static final RecipeComponentBuilder FLUID = new RecipeComponentBuilder(2)
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(FluidComponents.INPUT.key("content"));

    public static final RecipeComponentBuilder EU = new RecipeComponentBuilder(2)
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(NumberComponent.ANY_LONG.key("content"));

    public static final RecipeComponentBuilder STRESS = new RecipeComponentBuilder(2)
            .add(CHANCE)
            .add(TIER_CHANCE_BOOST)
            .add(NumberComponent.ANY_FLOAT.key("content"));

    public static RecipeComponentBuilder ALL_ANY = new RecipeComponentBuilder(4)
            .add(ITEM.asArray().key("item"))
            .add(FLUID.asArray().key("fluid"))
            .add(EU.asArray().key("eu"))
            .add(STRESS.asArray().key("su"));

    static {
        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddons().forEach(addon -> addon.registerRecipeKeys(event));

        for (RecipeKey<Map<String, Object>> key : event.getRegisteredKeys()) {
            ALL_ANY.add(key);
        }
    }

}
