package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.StressRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ingredient.NBTIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.*;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import com.lowdragmc.lowdraglib.LDLib;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface GTRecipeSchema {
    @Accessors(chain = true, fluent = true)
    class GTRecipeJS extends RecipeJS {
        @Setter
        public boolean perTick;
        @Setter
        public float chance = 1;
        @Setter
        public float tierChanceBoost = 0;

        @HideFromJS
        @Override
        public GTRecipeJS id(ResourceLocation _id) {
            this.id = new ResourceLocation(_id.getNamespace(), "%s/%s".formatted(this.type.id.getPath(), _id.getPath()));
            return this;
        }

        public <T> GTRecipeJS input(String capability, T... obj) {
            Map<String, Object> map;
            if (perTick)  {
                if (getValue(ALL_TICK_INPUTS) == null) setValue(ALL_TICK_INPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_TICK_INPUTS);
            } else {
                if (getValue(ALL_INPUTS) == null) setValue(ALL_INPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_INPUTS);
            }
            if (map != null) {
                HashMap<String, Object>[] array = UtilsJS.cast(map.computeIfAbsent(capability, c -> new HashMap[0]));
                for (T object : obj) {
                    HashMap<String, Object> entry = new HashMap<>();
                    entry.put(GTRecipeComponents.CHANCE.name, chance);
                    entry.put(GTRecipeComponents.TIER_CHANCE_BOOST.name, tierChanceBoost);
                    entry.put("content", object);
                    array = ArrayUtils.add(array, entry);
                }
                map.put(capability, array);
            }
            return this;
        }

        public <T> GTRecipeJS input(RecipeCapability<T> capability, T... obj) {
            Map<String, Object> map;
            if (perTick)  {
                if (getValue(ALL_TICK_INPUTS) == null) setValue(ALL_TICK_INPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_TICK_INPUTS);
            } else {
                if (getValue(ALL_INPUTS) == null) setValue(ALL_INPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_INPUTS);
            }
            if (map != null) {
                HashMap<String, Object>[] array = UtilsJS.cast(map.computeIfAbsent(capability.name, c -> new HashMap[0]));
                for (T object : obj) {
                    HashMap<String, Object> entry = new HashMap<>();
                    entry.put(GTRecipeComponents.CHANCE.name, chance);
                    entry.put(GTRecipeComponents.TIER_CHANCE_BOOST.name, tierChanceBoost);
                    entry.put("content", capability.of(object));
                    array = ArrayUtils.add(array, entry);
                }
                map.put(capability.name, array);
            }
            return this;
        }

        public <T> GTRecipeJS output(String capability, T... obj) {
            Map<String, Object> map;
            if (perTick)  {
                if (getValue(ALL_TICK_OUTPUTS) == null) setValue(ALL_TICK_OUTPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_TICK_OUTPUTS);
            } else {
                if (getValue(ALL_OUTPUTS) == null) setValue(ALL_OUTPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_OUTPUTS);
            }
            if (map != null) {
                HashMap<String, Object>[] array = UtilsJS.cast(map.computeIfAbsent(capability, c -> new HashMap[0]));
                for (T object : obj) {
                    HashMap<String, Object> entry = new HashMap<>();
                    entry.put(GTRecipeComponents.CHANCE.name, chance);
                    entry.put(GTRecipeComponents.TIER_CHANCE_BOOST.name, tierChanceBoost);
                    entry.put("content", object);
                    array = ArrayUtils.add(array, entry);
                }
                map.put(capability, array);
            }
            save();
            return this;
        }

        public <T> GTRecipeJS output(RecipeCapability<T> capability, T... obj) {
            Map<String, Object> map;
            if (perTick)  {
                if (getValue(ALL_TICK_OUTPUTS) == null) setValue(ALL_TICK_OUTPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_TICK_OUTPUTS);
            } else {
                if (getValue(ALL_OUTPUTS) == null) setValue(ALL_OUTPUTS, new HashMap<String, HashMap<String, Object>>());
                map = getValue(ALL_OUTPUTS);
            }
            if (map != null) {
                HashMap<String, Object>[] array = UtilsJS.cast(map.computeIfAbsent(capability.name, c -> new HashMap[0]));
                for (T object : obj) {
                    HashMap<String, Object> entry = new HashMap<>();
                    entry.put(GTRecipeComponents.CHANCE.name, chance);
                    entry.put(GTRecipeComponents.TIER_CHANCE_BOOST.name, tierChanceBoost);
                    entry.put("content", capability.of(object));
                    array = ArrayUtils.add(array, entry);
                }
                map.put(capability.name, array);
            }
            save();
            return this;
        }

        public GTRecipeJS addCondition(RecipeCondition condition) {
            if (getValue(CONDITIONS) == null) setValue(CONDITIONS, new RecipeCondition[0]);
            setValue(CONDITIONS, ArrayUtils.add(getValue(CONDITIONS), condition));
            save();
            return this;
        }

        public GTRecipeJS inputEU(long eu) {
            input(EURecipeCapability.CAP, eu);
            return this;
        }

        public GTRecipeJS EUt(long eu) {
            var lastPerTick = perTick;
            perTick = true;
            if (eu > 0) {
                if (getValue(ALL_TICK_INPUTS) != null) {
                    getValue(ALL_TICK_INPUTS).remove(EURecipeCapability.CAP.name);
                }
                inputEU(eu);
            } else if (eu < 0) {
                if (getValue(ALL_TICK_OUTPUTS) != null) {
                    getValue(ALL_TICK_OUTPUTS).remove(EURecipeCapability.CAP.name);
                }
                getValue(ALL_TICK_OUTPUTS).remove(EURecipeCapability.CAP.name);
                outputEU(-eu);
            }
            perTick = lastPerTick;
            return this;
        }

        public GTRecipeJS outputEU(long eu) {
            return output(EURecipeCapability.CAP, eu);
        }

        // for kjs
        public GTRecipeJS itemInputs(InputItem... inputs) {
            return input("item", inputs);
        }

        public GTRecipeJS itemInput(UnificationEntry input) {
            return inputItems(input);
        }

        public GTRecipeJS itemInput(UnificationEntry input, int count) {
            return inputItems(input, count);
        }

        public GTRecipeJS inputItems(InputItem... inputs) {
            return input("item", inputs);
        }

        public GTRecipeJS inputItems(ItemStack... inputs) {
            for (ItemStack itemStack : inputs) {
                if (itemStack.isEmpty()) {
                    GTCEu.LOGGER.error("gt recipe {} input items is empty", id);
                    throw new IllegalArgumentException(id + ": input items is empty");
                }
            }
            return input("item", Arrays.stream(inputs).map(stack -> InputItem.of(SizedIngredient.create(stack.hasTag() ? NBTIngredient.createNBTIngredient(stack) : Ingredient.of(stack), stack.getCount()), stack.getCount())).toArray(InputItem[]::new));
        }

        public GTRecipeJS inputItems(TagKey<Item> tag, int amount) {
            return inputItems(InputItem.of(Ingredient.of(tag), amount));
        }

        public GTRecipeJS inputItems(TagKey<Item> tag) {
            return inputItems(InputItem.of(Ingredient.of(tag), 1));
        }

        public GTRecipeJS inputItems(Item input, int amount) {
            return inputItems(new ItemStack(input, amount));
        }

        public GTRecipeJS inputItems(Item input) {
            return inputItems(InputItem.of(Ingredient.of(input), 1));
        }

        public GTRecipeJS inputItems(Supplier<? extends Item> input) {
            return inputItems(InputItem.of(Ingredient.of(input.get()), 1));
        }

        public GTRecipeJS inputItems(Supplier<? extends Item> input, int amount) {
            return inputItems(new ItemStack(input.get(), amount));
        }

        public GTRecipeJS inputItems(TagPrefix orePrefix, Material material) {
            return inputItems(orePrefix, material, 1);
        }

        public GTRecipeJS inputItems(UnificationEntry input) {
            return inputItems(input.tagPrefix, input.material, 1);
        }

        public GTRecipeJS inputItems(UnificationEntry input, int count) {
            return inputItems(input.tagPrefix, input.material, count);
        }

        public GTRecipeJS inputItems(TagPrefix orePrefix, @Nullable Material material, int count) {
            return inputItems(ChemicalHelper.getTag(orePrefix, material), count);
        }

        public GTRecipeJS inputItems(MachineDefinition machine) {
            return inputItems(machine, 1);
        }

        public GTRecipeJS inputItems(MachineDefinition machine, int count) {
            return inputItems(machine.asStack(count));
        }

        // for kjs
        public GTRecipeJS itemOutputs(ItemStack... outputs) {
            return outputItems(outputs);
        }

        public GTRecipeJS itemOutput(UnificationEntry unificationEntry) {
            return outputItems(unificationEntry.tagPrefix, unificationEntry.material);
        }

        public GTRecipeJS itemOutput(UnificationEntry unificationEntry, int count) {
            return outputItems(unificationEntry.tagPrefix, unificationEntry.material, count);
        }

        public GTRecipeJS outputItems(ItemStack... outputs) {
            for (ItemStack itemStack : outputs) {
                if (itemStack.isEmpty()) {
                    LDLib.LOGGER.error("gt recipe {} output items is empty", id);
                    throw new IllegalArgumentException(id + ": output items is empty");
                }
            }
            return output("item", Arrays.stream(outputs).map(InputItem::of).toArray(InputItem[]::new));
        }

        public GTRecipeJS outputItems(Item input, int amount) {
            return outputItems(new ItemStack(input, amount));
        }

        public GTRecipeJS outputItems(Item input) {
            return outputItems(new ItemStack(input));
        }

        public GTRecipeJS outputItems(Supplier<? extends Item> input) {
            return outputItems(new ItemStack(input.get()));
        }

        public GTRecipeJS outputItems(Supplier<? extends Item> input, int amount) {
            return outputItems(new ItemStack(input.get(), amount));
        }

        public GTRecipeJS outputItems(TagPrefix orePrefix, Material material) {
            return outputItems(orePrefix, material, 1);
        }

        public GTRecipeJS outputItems(TagPrefix orePrefix, Material material, int count) {
            return outputItems(ChemicalHelper.get(orePrefix, material, count));
        }

        public GTRecipeJS outputItems(MachineDefinition machine) {
            return outputItems(machine, 1);
        }

        public GTRecipeJS outputItems(MachineDefinition machine, int count) {
            return outputItems(machine.asStack(count));
        }

        public GTRecipeJS notConsumable(ItemStack itemStack) {
            float lastChance = this.chance;
            this.chance = 0;
            inputItems(itemStack);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumable(Item item) {
            float lastChance = this.chance;
            this.chance = 0;
            inputItems(item);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumable(Supplier<? extends Item> item) {
            float lastChance = this.chance;
            this.chance = 0;
            inputItems(item);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumable(TagPrefix orePrefix, Material material) {
            float lastChance = this.chance;
            this.chance = 0;
            inputItems(orePrefix, material);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS circuit(int configuration) {
            return notConsumable(IntCircuitBehaviour.stack(configuration));
        }

        public GTRecipeJS chancedInput(ItemStack stack, int chance, int tierChanceBoost) {
            // todo finish tier chance boost, make it actually used in places
            float lastChance = this.chance;
            float lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance / 10000f;
            this.tierChanceBoost = tierChanceBoost / 10000f;
            inputItems(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedInput(FluidStackJS stack, int chance, int tierChanceBoost) {
            float lastChance = this.chance;
            float lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance / 10000f;
            this.tierChanceBoost = tierChanceBoost / 10000f;
            inputFluids(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
            float lastChance = this.chance;
            float lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance / 10000f;
            this.tierChanceBoost = tierChanceBoost / 10000f;
            outputItems(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedOutput(FluidStackJS stack, int chance, int tierChanceBoost) {
            float lastChance = this.chance;
            float lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance / 10000f;
            this.tierChanceBoost = tierChanceBoost / 10000f;
            outputFluids(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
            return chancedOutput(ChemicalHelper.get(tag, mat), chance, tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
            return chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
        }

        public GTRecipeJS inputFluids(FluidStackJS... inputs) {
            return input("fluid", inputs);
        }

        public GTRecipeJS outputFluids(FluidStackJS... outputs) {
            return output("fluid", outputs);
        }

        public GTRecipeJS inputStress(float stress) {
            return input(StressRecipeCapability.CAP, stress);
        }

        public GTRecipeJS outputStress(float stress) {
            return output(StressRecipeCapability.CAP, stress);
        }

        //////////////////////////////////////
        //**********     DATA    ***********//
        //////////////////////////////////////
        public GTRecipeJS addData(String key, Tag data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).put(key, data);
            save();
            return this;
        }

        public GTRecipeJS addData(String key, int data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putInt(key, data);
            save();
            return this;
        }

        public GTRecipeJS addData(String key, String data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putString(key, data);
            save();
            return this;
        }

        public GTRecipeJS addData(String key, Float data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putFloat(key, data);
            save();
            return this;
        }

        public GTRecipeJS addData(String key, boolean data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putBoolean(key, data);
            save();
            return this;
        }

        public GTRecipeJS blastFurnaceTemp(int blastTemp) {
            return addData("ebf_temp", blastTemp);
        }

        public GTRecipeJS explosivesAmount(int explosivesAmount) {
            return addData("explosives_amount", explosivesAmount);
        }

        public GTRecipeJS explosivesType(ItemStack explosivesType) {
            return addData("explosives_type", explosivesType.save(new CompoundTag()));
        }

        public GTRecipeJS solderMultiplier(int multiplier) {
            return addData("solderMultiplier", multiplier);
        }

        //////////////////////////////////////
        //*******     CONDITIONS    ********//
        //////////////////////////////////////

        public GTRecipeJS cleanroom(CleanroomType cleanroomType) {
            return addCondition(new CleanroomCondition(cleanroomType));
        }

        public GTRecipeJS dimension(ResourceLocation dimension, boolean reverse) {
            return addCondition(new DimensionCondition(dimension).setReverse(reverse));
        }

        public GTRecipeJS dimension(ResourceLocation dimension) {
            return dimension(dimension, false);
        }

        public GTRecipeJS biome(ResourceLocation biome, boolean reverse) {
            return addCondition(new BiomeCondition(biome).setReverse(reverse));
        }

        public GTRecipeJS biome(ResourceLocation biome) {
            return biome(biome, false);
        }

        public GTRecipeJS rain(float level, boolean reverse) {
            return addCondition(new RainingCondition(level).setReverse(reverse));
        }

        public GTRecipeJS rain(float level) {
            return rain(level, false);
        }

        public GTRecipeJS thunder(float level, boolean reverse) {
            return addCondition(new ThunderCondition(level).setReverse(reverse));
        }

        public GTRecipeJS thunder(float level) {
            return thunder(level, false);
        }

        public GTRecipeJS posY(int min, int max, boolean reverse) {
            return addCondition(new PositionYCondition(min, max).setReverse(reverse));
        }

        public GTRecipeJS posY(int min, int max) {
            return posY(min, max, false);
        }

        public GTRecipeJS rpm(float rpm, boolean reverse) {
            return addCondition(new RPMCondition(rpm).setReverse(reverse));
        }

        public GTRecipeJS rpm(float rpm) {
            return rpm(rpm, false);
        }

        /*
         * KubeJS overrides
         */

        public InputItem readInputItem(Object from) {
            if(from instanceof SizedIngredient ingr) {
                return InputItem.of(ingr.getInner(), ingr.getAmount());
            } else if(from instanceof JsonObject jsonObject) {
                if (!jsonObject.has("type") || !jsonObject.get("type").getAsString().equals(SizedIngredient.TYPE.toString())) {
                    return InputItem.of(from);
                }
                var sizedIngredient = SizedIngredient.fromJson(jsonObject);
                return InputItem.of(sizedIngredient.getInner(), sizedIngredient.getAmount());
            }
            return InputItem.of(from);
        }

        public JsonElement writeInputItem(InputItem value) {
            return SizedIngredient.create(value.ingredient, value.count).toJson();
        }

        @Override
        public OutputItem readOutputItem(Object from) {
            if(from instanceof SizedIngredient ingredient) {
                return OutputItem.of(ingredient.getInner().getItems()[0], Double.NaN);
            } else if(from instanceof JsonObject jsonObject) {
                if (jsonObject.has("content")) {
                    jsonObject = jsonObject.getAsJsonObject("content");
                }
                var ingredient = SizedIngredient.fromJson(jsonObject);
                return OutputItem.of(ingredient.getInner().getItems()[0], Double.NaN);
            }
            return OutputItem.of(from);
        }

        @Override
        public JsonElement writeOutputItem(OutputItem value) {
            return SizedIngredient.create(value.item).toJson();
        }
    }

    RecipeKey<ResourceLocation> ID = GTRecipeComponents.RESOURCE_LOCATION.key("id");
    RecipeKey<Long> DURATION = TimeComponent.TICKS.key("duration").optional(100L);
    RecipeKey<CompoundTag> DATA = GTRecipeComponents.TAG.key("data").optional((CompoundTag) null).exclude();
    RecipeKey<RecipeCondition[]> CONDITIONS = GTRecipeComponents.RECIPE_CONDITION.asArray().key("recipeConditions").defaultOptional().exclude();
    RecipeKey<Boolean> IS_FUEL = BooleanComponent.BOOLEAN.key("isFuel").optional(false).exclude();

    RecipeKey<Map<String, Object>> ALL_INPUTS = GTRecipeComponents.ALL_ANY.key("inputs").defaultOptional();
    RecipeKey<Map<String, Object>> ALL_TICK_INPUTS = GTRecipeComponents.ALL_ANY.key("tickInputs").defaultOptional();

    RecipeKey<Map<String, Object>> ALL_OUTPUTS = GTRecipeComponents.ALL_ANY.key("outputs").defaultOptional();
    RecipeKey<Map<String, Object>> ALL_TICK_OUTPUTS = GTRecipeComponents.ALL_ANY.key("tickOutputs").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(GTRecipeJS.class, GTRecipeJS::new, DURATION, DATA, CONDITIONS, ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS, IS_FUEL)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID);

}

