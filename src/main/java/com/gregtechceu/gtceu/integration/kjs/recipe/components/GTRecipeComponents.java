package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.integration.kjs.recipe.KJSHelpers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.fluid.*;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.mod.util.NBTUtils;
import org.jetbrains.annotations.Nullable;

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
                var ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
                var condition = RecipeCondition.CODEC.parse(ops, jsonObject).result();
                if (condition.isPresent()) {
                    return condition.get();
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
            return "fluid_ingredient";
        }

        @Override
        public Class<?> componentClass() {
            return FluidIngredientJS.class;
        }

        @Override
        public boolean isInput(RecipeJS recipe, FluidIngredientJS value, ReplacementMatch match) {
            return match instanceof FluidLike m && value.matches(m);
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
            return ExtendedOutputItem.of(from, recipe);
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
    public static final RecipeComponent<EnergyStack.WithIO> ENERGY_STACK = new RecipeComponent<>() {

        @Override
        public String componentType() {
            return "energy_stack";
        }

        @Override
        public Class<?> componentClass() {
            return EnergyStack.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, EnergyStack.WithIO value) {
            return EnergyStack.WithIO.CODEC.encodeStart(JsonOps.INSTANCE, value).result().orElse(null);
        }

        @Override
        public EnergyStack.WithIO read(RecipeJS recipe, Object from) {
            return KJSHelpers.parseIOEnergyStack(from);
        }
    };

    public static final ContentJS<InputItem> ITEM_IN = new ContentJS<>(ItemComponents.INPUT, GTRecipeCapabilities.ITEM,
            false);
    public static final ContentJS<ExtendedOutputItem> ITEM_OUT = new ContentJS<>(EXTENDED_OUTPUT,
            GTRecipeCapabilities.ITEM, true);
    public static final ContentJS<FluidIngredientJS> FLUID_IN = new ContentJS<>(FLUID_INGREDIENT,
            GTRecipeCapabilities.FLUID, false);
    public static final ContentJS<FluidIngredientJS> FLUID_OUT = new ContentJS<>(FLUID_INGREDIENT,
            GTRecipeCapabilities.FLUID, true);
    public static final ContentJS<EnergyStack.WithIO> EU_IN = new ContentJS<>(ENERGY_STACK, GTRecipeCapabilities.EU,
            false);
    public static final ContentJS<EnergyStack.WithIO> EU_OUT = new ContentJS<>(ENERGY_STACK, GTRecipeCapabilities.EU,
            true);
    public static final ContentJS<Integer> CWU_IN = new ContentJS<>(NumberComponent.INT, GTRecipeCapabilities.CWU,
            false);
    public static final ContentJS<Integer> CWU_OUT = new ContentJS<>(NumberComponent.INT, GTRecipeCapabilities.CWU,
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
        VALID_CAPS.put(GTRecipeCapabilities.CWU, Pair.of(CWU_IN, CWU_OUT));

        KJSRecipeKeyEvent event = new KJSRecipeKeyEvent();
        AddonFinder.getAddons().forEach(addon -> addon.registerRecipeKeys(event));
        VALID_CAPS.putAll(event.getRegisteredKeys());
    }

    public record FluidIngredientJS(FluidIngredient ingredient) implements InputFluid, OutputFluid {

        public FluidIngredientJS(FluidStack stack) {
            this(FluidIngredient.of(stack));
        }

        public FluidIngredientJS(TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
            this(FluidIngredient.of(tag, amount, nbt));
        }

        public FluidIngredientJS(Fluid fluid, int amount, @Nullable CompoundTag nbt) {
            this(FluidIngredient.of(fluid, amount, nbt));
        }

        @Override
        public long kjs$getAmount() {
            return ingredient.getAmount();
        }

        @Override
        public FluidIngredientJS kjs$copy(long amount) {
            FluidIngredient ingredient1 = ingredient.copy();
            ingredient1.setAmount((int) amount);
            return new FluidIngredientJS(ingredient1);
        }

        @Override
        public boolean matches(FluidLike other) {
            if (other instanceof FluidStackJS stackJS) {
                FluidStack stack = new FluidStack(stackJS.getFluid(), (int) stackJS.getAmount(), stackJS.getNbt());
                return ingredient.test(stack);
            } else if (other instanceof FluidStack stack) {
                return ingredient.test(stack);
            }
            return other.matches(this);
        }

        public static FluidIngredientJS of(Object o) {
            if (o instanceof FluidIngredientJS ingredientJS) {
                return ingredientJS;
            } else if (o instanceof IntProviderFluidIngredient ingredient) {
                return new FluidIngredientJS(ingredient.copy());
            } else if (o instanceof FluidIngredient ingredient) {
                return new FluidIngredientJS(ingredient);
            } else if (o instanceof JsonElement json) {
                return new FluidIngredientJS(FluidIngredient.fromJson(json));
            } else if (o instanceof Tag nbt) {
                JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt);
                return new FluidIngredientJS(FluidIngredient.fromJson(json));
            } else if (o instanceof FluidStack stack) {
                return new FluidIngredientJS(stack);
            } else if (o instanceof FluidStackJS stackJS) {
                return new FluidIngredientJS(stackJS.getFluid(), (int) stackJS.getAmount(), stackJS.getNbt());
            } else if (o instanceof CharSequence || o instanceof ResourceLocation) {
                var s = o.toString();

                if (s.isEmpty() || s.equals("-") || s.equals("empty") || s.equals("minecraft:empty")) {
                    return new FluidIngredientJS(FluidIngredient.EMPTY);
                }

                boolean isTag = false;
                if (s.startsWith("#")) {
                    s = s.substring(1);
                    isTag = true;
                }
                var split = s.split(" ", 3);
                ResourceLocation id = new ResourceLocation(split[0]);
                int amount = UtilsJS.parseInt(split.length >= 2 ? split[1] : "", FluidType.BUCKET_VOLUME);
                CompoundTag nbt = null;
                if (split.length == 3) {
                    try {
                        nbt = TagParser.parseTag(split[2]);
                    } catch (CommandSyntaxException ignored) {}
                }

                if (isTag) {
                    return new FluidIngredientJS(TagKey.create(Registries.FLUID, id), amount, nbt);
                } else {
                    return new FluidIngredientJS(BuiltInRegistries.FLUID.get(id), amount, nbt);
                }
            }

            var list = ListJS.of(o);
            if (list != null && !list.isEmpty()) {
                List<FluidStack> stacks = new ArrayList<>();
                for (var object : list) {
                    FluidStackJS stackJS = FluidStackJS.of(object);
                    stacks.add(new FluidStack(stackJS.getFluid(), (int) stackJS.getAmount(), stackJS.getNbt()));
                }
                return new FluidIngredientJS(FluidIngredient.of(stacks));
            } else {
                FluidStackJS stackJS = FluidStackJS.of(o);
                return new FluidIngredientJS(stackJS.getFluid(), (int) stackJS.getAmount(), stackJS.getNbt());
            }
        }
    }
}
