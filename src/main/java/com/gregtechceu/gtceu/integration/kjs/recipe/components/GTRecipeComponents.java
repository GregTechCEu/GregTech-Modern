package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.recipe.GTRecipeCapabilities;

import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.fluid.FluidLike;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.ListJS;

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
        public JsonElement write(KubeRecipe recipe, CompoundTag value) {
            return NBTUtils.toJson(value);
        }

        @Override
        public CompoundTag read(KubeRecipe recipe, Object from) {
            return NBTUtils.toTagCompound(null, from);
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
        public JsonElement write(KubeRecipe recipe, ResourceLocation value) {
            return new JsonPrimitive(value.toString());
        }

        @Override
        public ResourceLocation read(KubeRecipe recipe, Object from) {
            return from instanceof CharSequence c ? ResourceLocation.tryParse(c.toString()) :
                    ResourceLocation.tryParse(String.valueOf(from));
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
        public JsonElement write(KubeRecipe recipe, RecipeCondition value) {
            JsonObject object = new JsonObject();
            object.addProperty("type", GTRegistries.RECIPE_CONDITIONS.getKey(value.getType()));
            object.add("data", value.serialize());
            return object;
        }

        @Override
        public RecipeCondition read(KubeRecipe recipe, Object from) {
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
        public boolean isInput(KubeRecipe recipe, FluidIngredientJS value, ReplacementMatch match) {
            return match instanceof FluidLike m && value.matches(m);
        }

        @Override
        public JsonElement write(KubeRecipe recipe, FluidIngredientJS value) {
            return SizedFluidIngredient.NESTED_CODEC.encodeStart(JsonOps.INSTANCE, value.ingredient).getOrThrow();
        }

        @Override
        public FluidIngredientJS read(KubeRecipe recipe, Object from) {
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
        public boolean isOutput(KubeRecipe recipe, FluidIngredientJS value, ReplacementMatch match) {
            return match instanceof FluidLike m && value.matches(m);
        }

        @Override
        public JsonElement write(KubeRecipe recipe, FluidIngredientJS value) {
            return SizedFluidIngredient.NESTED_CODEC.encodeStart(JsonOps.INSTANCE, value.ingredient).getOrThrow();
        }

        @Override
        public FluidIngredientJS read(KubeRecipe recipe, Object from) {
            return FluidIngredientJS.of(from);
        }
    };

    public static final ContentJS<InputItem> ITEM_IN = new ContentJS<>(ItemComponents.INPUT, GTRecipeCapabilities.ITEM,
            false);
    public static final ContentJS<OutputItem> ITEM_OUT = new ContentJS<>(ItemComponents.OUTPUT,
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

        private final SizedFluidIngredient ingredient;

        public FluidIngredientJS(SizedFluidIngredient ingredient) {
            this.ingredient = ingredient;
        }

        @Override
        public long kjs$getAmount() {
            return ingredient.amount();
        }

        @Override
        public FluidIngredientJS kjs$copy(long amount) {
            SizedFluidIngredient ingredient1 = new SizedFluidIngredient(ingredient.ingredient(), (int) amount);
            return new FluidIngredientJS(ingredient1);
        }

        @Override
        public boolean matches(FluidLike other) {
            // if (other instanceof FluidStack fluidStack) {
            //     // TODO fix nbt once KubeJS 1.21 is out
            //     return ingredient.test(fluidStack);
            // }
            return other.matches(this);
        }

        public static FluidIngredientJS of(Object o) {
            if (o instanceof FluidIngredientJS ingredientJS) {
                return ingredientJS;
            } else if (o instanceof SizedFluidIngredient ingredient) {
                return new FluidIngredientJS(ingredient);
            } else if (o instanceof JsonElement json) {
                return new FluidIngredientJS(SizedFluidIngredient.NESTED_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            } else if (o instanceof FluidStack fluidStack) {
                return new FluidIngredientJS(SizedFluidIngredient.of(fluidStack));
            }

            var list = ListJS.of(o);
            if (list != null && !list.isEmpty()) {
                List<FluidStack> stacks = new ArrayList<>();
                for (var object : list) {
                    FluidStack stack = FluidWrapper.wrap(object);
                    stacks.add(stack);
                }
                return new FluidIngredientJS(new SizedFluidIngredient(FluidIngredient.of(stacks.toArray(FluidStack[]::new)),
                        stacks.get(0).getAmount()));
            } else {
                FluidStack stack = FluidWrapper.wrap(o);
                // TODO fix nbt once KubeJS 1.20.5 is out
                return new FluidIngredientJS(SizedFluidIngredient.of(stack));
            }
        }
    }
}
