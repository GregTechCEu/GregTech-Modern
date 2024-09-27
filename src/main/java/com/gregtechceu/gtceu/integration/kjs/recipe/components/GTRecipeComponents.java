package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.fluid.FluidLike;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.rhino.mod.util.NBTUtils;

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
            return from instanceof CharSequence c ? ResourceLocation.tryParse(c.toString()) :
                    ResourceLocation.tryParse(String.valueOf(from));
        }

        @Override
        public String toString() {
            return componentType();
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
            object.addProperty("type", GTRegistries.RECIPE_CONDITIONS.getKey(value.getType()));
            object.add("data", value.serialize());
            return object;
        }

        @Override
        public RecipeCondition read(RecipeJS recipe, Object from) {
            if (from instanceof CharSequence) {
                var conditionKey = from.toString();
                var type = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (type != null) {
                    return type.factory.createDefault();
                }
            }
            if (from instanceof JsonPrimitive primitive) {
                var conditionKey = primitive.getAsString();
                var type = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (type != null) {
                    return type.factory.createDefault();
                }
            } else if (from instanceof JsonObject jsonObject) {
                var conditionKey = GsonHelper.getAsString(jsonObject, "type", "");
                var type = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (type != null) {
                    RecipeCondition condition = type.factory.createDefault();
                    if (condition != null) {
                        return condition.deserialize(jsonObject);
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
    public static final RecipeComponent<FluidIngredientJS> FLUID_INGREDIENT = new RecipeComponent<>() {

        @Override
        public String componentType() {
            return "input_fluid";
        }

        @Override
        public Class<?> componentClass() {
            return FluidIngredientJS.class;
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.INPUT;
        }

        @Override
        public boolean isInput(RecipeJS recipe, FluidIngredientJS value, ReplacementMatch match) {
            return match instanceof FluidLike m && value.matches(m);
        }

        @Override
        public JsonElement write(RecipeJS recipe, FluidIngredientJS value) {
            return value.ingredient.toJson();
        }

        @Override
        public FluidIngredientJS read(RecipeJS recipe, Object from) {
            return FluidIngredientJS.of(from);
        }
    };
    public static final RecipeComponent<FluidIngredientJS> FLUID_INGREDIENT_OUT = new RecipeComponent<>() {

        @Override
        public String componentType() {
            return "output_fluid";
        }

        @Override
        public Class<?> componentClass() {
            return FluidIngredientJS.class;
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.OUTPUT;
        }

        @Override
        public boolean isOutput(RecipeJS recipe, FluidIngredientJS value, ReplacementMatch match) {
            return match instanceof FluidLike m && value.matches(m);
        }

        @Override
        public JsonElement write(RecipeJS recipe, FluidIngredientJS value) {
            return value.ingredient.toJson();
        }

        @Override
        public FluidIngredientJS read(RecipeJS recipe, Object from) {
            return FluidIngredientJS.of(from);
        }
    };
    public static final RecipeComponent<ExtendedOutputItem> EXTENDED_OUTPUT = new RecipeComponent<>() {

        @Override
        public String componentType() {
            return "extended_output_item";
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.OUTPUT;
        }

        @Override
        public Class<?> componentClass() {
            return OutputItem.class;
        }

        @Override
        public boolean hasPriority(RecipeJS recipe, Object from) {
            return recipe.outputItemHasPriority(from);
        }

        @Override
        public JsonElement write(RecipeJS recipe, ExtendedOutputItem value) {
            return recipe.writeOutputItem(value);
        }

        @Override
        public ExtendedOutputItem read(RecipeJS recipe, Object from) {
            if (from instanceof IntProviderIngredient intProvider) {
                return new ExtendedOutputItem(intProvider, 1);
            }
            return ExtendedOutputItem.fromOutputItem(recipe.readOutputItem(from));
        }

        @Override
        public boolean isOutput(RecipeJS recipe, ExtendedOutputItem value, ReplacementMatch match) {
            return match instanceof ItemMatch m && !value.isEmpty() && m.contains(value.ingredient);
        }

        @Override
        public ExtendedOutputItem replaceOutput(RecipeJS recipe, ExtendedOutputItem original, ReplacementMatch match,
                                                OutputReplacement with) {
            return isOutput(recipe, original, match) ? read(recipe, with.replaceOutput(recipe, match, original)) :
                    original;
        }

        @Override
        public String checkEmpty(RecipeKey<ExtendedOutputItem> key, ExtendedOutputItem value) {
            if (value.isEmpty()) {
                return "Ingredient '" + key.name + "' can't be empty!";
            }

            return "";
        }

        @Override
        public String toString() {
            return componentType();
        }
    };

    public static final ContentJS<InputItem> ITEM_IN = new ContentJS<>(ItemComponents.INPUT, GTRecipeCapabilities.ITEM,
            false);
    public static final ContentJS<ExtendedOutputItem> ITEM_OUT = new ContentJS<>(EXTENDED_OUTPUT,
            GTRecipeCapabilities.ITEM, true);
    public static final ContentJS<FluidIngredientJS> FLUID_IN = new ContentJS<>(FLUID_INGREDIENT,
            GTRecipeCapabilities.FLUID, false);
    public static final ContentJS<FluidIngredientJS> FLUID_OUT = new ContentJS<>(FLUID_INGREDIENT_OUT,
            GTRecipeCapabilities.FLUID, true);
    public static final ContentJS<Long> EU_IN = new ContentJS<>(NumberComponent.ANY_LONG, GTRecipeCapabilities.EU,
            false);
    public static final ContentJS<Long> EU_OUT = new ContentJS<>(NumberComponent.ANY_LONG, GTRecipeCapabilities.EU,
            true);
    public static final ContentJS<Float> SU_IN = new ContentJS<>(NumberComponent.ANY_FLOAT, GTRecipeCapabilities.SU,
            false);
    public static final ContentJS<Float> SU_OUT = new ContentJS<>(NumberComponent.ANY_FLOAT, GTRecipeCapabilities.SU,
            true);
    public static final ContentJS<Integer> CWU_IN = new ContentJS<>(NumberComponent.ANY_INT, GTRecipeCapabilities.CWU,
            false);
    public static final ContentJS<Integer> CWU_OUT = new ContentJS<>(NumberComponent.ANY_INT, GTRecipeCapabilities.CWU,
            true);

    public static final CapabilityMapComponent IN = new CapabilityMapComponent(false);
    public static final CapabilityMapComponent TICK_IN = new CapabilityMapComponent(false);
    public static final CapabilityMapComponent OUT = new CapabilityMapComponent(true);
    public static final CapabilityMapComponent TICK_OUT = new CapabilityMapComponent(true);

    public static final RecipeComponent<Map<RecipeCapability<?>, ChanceLogic>> CHANCE_LOGIC_MAP = new JavaMapRecipeComponent<>(
            RECIPE_CAPABILITY, CHANCE_LOGIC);

    /**
     * First in pair is in, second is out
     */
    public static final Map<RecipeCapability<?>, Pair<ContentJS<?>, ContentJS<?>>> VALID_CAPS = new IdentityHashMap<>();

    static {
        VALID_CAPS.put(GTRecipeCapabilities.ITEM, Pair.of(ITEM_IN, ITEM_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.FLUID, Pair.of(FLUID_IN, FLUID_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.EU, Pair.of(EU_IN, EU_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.SU, Pair.of(SU_IN, SU_OUT));
        VALID_CAPS.put(GTRecipeCapabilities.CWU, Pair.of(CWU_IN, CWU_OUT));

        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddons().forEach(addon -> addon.registerRecipeKeys(event));
        VALID_CAPS.putAll(event.getRegisteredKeys());
    }

    public static class FluidIngredientJS implements InputFluid, OutputFluid {

        private final FluidIngredient ingredient;

        public FluidIngredientJS(FluidIngredient ingredient) {
            this.ingredient = ingredient;
        }

        @Override
        public long kjs$getAmount() {
            return ingredient.getAmount();
        }

        @Override
        public FluidIngredientJS kjs$copy(long amount) {
            FluidIngredient ingredient1 = ingredient.copy();
            ingredient1.setAmount(amount);
            return new FluidIngredientJS(ingredient1);
        }

        @Override
        public boolean matches(FluidLike other) {
            if (other instanceof FluidStackJS fluidStack) {
                return ingredient
                        .test(FluidStack.create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getNbt()));
            }
            return other.matches(this);
        }

        public static FluidIngredientJS of(Object o) {
            if (o instanceof FluidIngredientJS ingredientJS) {
                return ingredientJS;
            } else if (o instanceof FluidIngredient ingredient) {
                return new FluidIngredientJS(ingredient);
            } else if (o instanceof JsonElement json) {
                return new FluidIngredientJS(FluidIngredient.fromJson(json));
            } else if (o instanceof FluidStackJS fluidStackJS) {
                return new FluidIngredientJS(FluidIngredient.of(
                        FluidStack.create(fluidStackJS.getFluid(), fluidStackJS.getAmount(), fluidStackJS.getNbt())));
            }

            var list = ListJS.of(o);
            if (list != null && !list.isEmpty()) {
                List<FluidStack> stacks = new ArrayList<>();
                for (var object : list) {
                    FluidStackJS stackJS = FluidStackJS.of(object);
                    stacks.add(FluidStack.create(stackJS.getFluid(), stackJS.getAmount(), stackJS.getNbt()));
                }
                return new FluidIngredientJS(FluidIngredient.of(stacks.toArray(FluidStack[]::new)));
            } else {
                FluidStackJS stackJS = FluidStackJS.of(o);
                return new FluidIngredientJS(FluidIngredient
                        .of(FluidStack.create(stackJS.getFluid(), stackJS.getAmount(), stackJS.getNbt())));
            }
        }
    }
}
