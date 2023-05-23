package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.*;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtlib.GTLib;
import com.gregtechceu.gtlib.side.fluid.FluidStack;
import com.gregtechceu.gtlib.utils.NBTToJsonConverter;
import dev.latvian.mods.kubejs.recipe.*;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GTRecipeBuilderJS extends RecipeJS {
    private GTRecipeBuilder backingBuilder;

    @Override
    public void create(RecipeArguments args) {
        this.id = ResourceLocation.tryParse(args.getString(1, null));
        backingBuilder = GTRecipeTypes.get(args.getString(0, null)).recipeBuilder(this.getOrCreateId());
    }

    @Override
    public void deserialize() {
        String recipeType = GsonHelper.getAsString(json, "recipe_type");
        int duration = json.has("duration") ? GsonHelper.getAsInt(json, "duration") : 100;
        Component component = json.has("text") ? Component.translatable(GsonHelper.getAsString(json, "text")) : null;
        CompoundTag data = new CompoundTag();
        try {
            if (json.has("data"))
                data = TagParser.parseTag(json.get("data").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<RecipeCapability<?>, List<Content>> inputs = GTRecipeSerializer.SERIALIZER.capabilitiesFromJson(json.has("inputs") ? json.getAsJsonObject("inputs") : new JsonObject());
        Map<RecipeCapability<?>, List<Content>> tickInputs = GTRecipeSerializer.SERIALIZER.capabilitiesFromJson(json.has("tickInputs") ? json.getAsJsonObject("tickInputs") : new JsonObject());
        Map<RecipeCapability<?>, List<Content>> outputs = GTRecipeSerializer.SERIALIZER.capabilitiesFromJson(json.has("outputs") ? json.getAsJsonObject("outputs") : new JsonObject());
        Map<RecipeCapability<?>, List<Content>> tickOutputs = GTRecipeSerializer.SERIALIZER.capabilitiesFromJson(json.has("tickOutputs") ? json.getAsJsonObject("tickOutputs") : new JsonObject());
        List<RecipeCondition> conditions = new ArrayList<>();
        JsonArray conditionsJson = json.has("recipeConditions") ? json.getAsJsonArray("recipeConditions") : new JsonArray();
        for (JsonElement jsonElement : conditionsJson) {
            if (jsonElement instanceof JsonObject jsonObject) {
                var conditionKey = GsonHelper.getAsString(jsonObject, "type", "");
                var clazz = GTRegistries.RECIPE_CONDITIONS.get(conditionKey);
                if (clazz != null) {
                    RecipeCondition condition = RecipeCondition.create(clazz);
                    if (condition != null) {
                        conditions.add(condition.deserialize(GsonHelper.getAsJsonObject(jsonObject, "data", new JsonObject())));
                    }
                }
            }
        }
        boolean isFuel = GsonHelper.getAsBoolean(json, "isFuel", false);
        backingBuilder = new GTRecipeBuilder(id, (GTRecipeType) Registry.RECIPE_TYPE.get(GTCEu.appendId(recipeType)))
                .duration(duration)
                .isFuel(isFuel);
        backingBuilder.data = data;
        backingBuilder.input.putAll(inputs);
        backingBuilder.tickInput.putAll(tickInputs);
        backingBuilder.output.putAll(outputs);
        backingBuilder.tickOutput.putAll(tickOutputs);
        backingBuilder.conditions.addAll(conditions);
    }

    @Override
    public void serialize() {
        json.addProperty("recipe_type", backingBuilder.recipeType.registryName.toString());
        json.addProperty("duration", Math.abs(backingBuilder.duration));
        if (backingBuilder.data != null && !backingBuilder.data.isEmpty()) {
            json.add("data", NBTToJsonConverter.getObject(backingBuilder.data));
        }
        if (serializeInputs) {
            if (!backingBuilder.input.isEmpty()) {
                json.add("inputs", backingBuilder.capabilitiesToJson(backingBuilder.input));
            }
            if (!backingBuilder.tickInput.isEmpty()) {
                json.add("tickInputs", backingBuilder.capabilitiesToJson(backingBuilder.tickInput));
            }
        }
        if (serializeOutputs) {
            if (!backingBuilder.output.isEmpty()) {
                json.add("outputs", backingBuilder.capabilitiesToJson(backingBuilder.output));
            }
            if (!backingBuilder.tickOutput.isEmpty()) {
                json.add("tickOutputs", backingBuilder.capabilitiesToJson(backingBuilder.tickOutput));
            }
        }

        if (!backingBuilder.conditions.isEmpty()) {
            JsonArray array = new JsonArray();
            for (RecipeCondition condition : backingBuilder.conditions) {
                JsonObject cond = new JsonObject();
                cond.addProperty("type", GTRegistries.RECIPE_CONDITIONS.getKey(condition.getClass()));
                cond.add("data", condition.serialize());
                array.add(cond);
            }
            json.add("recipeConditions", array);
        }
        if (backingBuilder.isFuel) {
            json.addProperty("isFuel", true);
        }
    }

    public <T> GTRecipeBuilderJS duration(int duration) {
        backingBuilder.duration(duration);
        return this;
    }

    public <T> GTRecipeBuilderJS perTick(boolean perTick) {
        backingBuilder.perTick(perTick);
        return this;
    }

    public <T> GTRecipeBuilderJS slotName(String slotName) {
        backingBuilder.slotName(slotName);
        return this;
    }

    public <T> GTRecipeBuilderJS uiName(String uiName) {
        backingBuilder.uiName(uiName);
        return this;
    }

    public <T> GTRecipeBuilderJS chance(float chance) {
        backingBuilder.chance(chance);
        return this;
    }

    public <T> GTRecipeBuilderJS input(RecipeCapability<T> capability, T... obj) {
        backingBuilder.input(capability, obj);
        return this;
    }

    public <T> GTRecipeBuilderJS output(RecipeCapability<T> capability, T... obj) {
        backingBuilder.output(capability, obj);
        return this;
    }

    public <T> GTRecipeBuilderJS inputs(RecipeCapability<T> capability, Object... obj) {
        backingBuilder.inputs(capability, obj);
        return this;
    }

    public <T> GTRecipeBuilderJS outputs(RecipeCapability<T> capability, Object... obj) {
        backingBuilder.outputs(capability, obj);
        return this;
    }

    public GTRecipeBuilderJS addCondition(RecipeCondition condition) {
        backingBuilder.addCondition(condition);
        return this;
    }

    public GTRecipeBuilderJS inputEU(long eu) {
        return input(EURecipeCapability.CAP, eu);
    }

    public GTRecipeBuilderJS EUt(long eu) {
        backingBuilder.EUt(eu);
        return this;
    }

    public GTRecipeBuilderJS outputEU(long eu) {
        return output(EURecipeCapability.CAP, eu);
    }

    // for kjs
    public GTRecipeBuilderJS itemInputs(Ingredient... inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilderJS itemInput(UnificationEntry input) {
        return inputItems(input);
    }

    public GTRecipeBuilderJS itemInput(UnificationEntry input, int count) {
        return inputItems(input, count);
    }

    public GTRecipeBuilderJS inputItems(Ingredient... inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilderJS inputItems(ItemStack... inputs) {
        for (ItemStack itemStack : inputs) {
            if (itemStack.isEmpty()) {
                GTLib.LOGGER.error("gt recipe {} input items is empty", id);
                throw new IllegalArgumentException(id + ": input items is empty");
            }
        }
        return input(ItemRecipeCapability.CAP, Arrays.stream(inputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public GTRecipeBuilderJS inputItems(TagKey<Item> tag, int amount) {
        return inputItems(SizedIngredient.create(tag, amount));
    }

    public GTRecipeBuilderJS inputItems(TagKey<Item> tag) {
        return inputItems(SizedIngredient.create(tag, 1));
    }

    public GTRecipeBuilderJS inputItems(Item input, int amount) {
        return inputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilderJS inputItems(Item input) {
        return inputItems(Ingredient.of(input));
    }

    public GTRecipeBuilderJS inputItems(Supplier<? extends Item> input) {
        return inputItems(Ingredient.of(input.get()));
    }

    public GTRecipeBuilderJS inputItems(Supplier<? extends Item> input, int amount) {
        return inputItems(new ItemStack(input.get(), amount));
    }

    public GTRecipeBuilderJS inputItems(TagPrefix orePrefix, Material material) {
        return inputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilderJS inputItems(UnificationEntry input) {
        return inputItems(input.tagPrefix, input.material, 1);
    }

    public GTRecipeBuilderJS inputItems(UnificationEntry input, int count) {
        return inputItems(input.tagPrefix, input.material, count);
    }

    public GTRecipeBuilderJS inputItems(TagPrefix orePrefix, @Nullable Material material, int count) {
        return inputItems(ChemicalHelper.getTag(orePrefix, material), count);
    }

    public GTRecipeBuilderJS inputItems(MachineDefinition machine) {
        return inputItems(machine, 1);
    }

    public GTRecipeBuilderJS inputItems(MachineDefinition machine, int count) {
        return inputItems(machine.asStack(count));
    }

    // for kjs
    public GTRecipeBuilderJS itemOutputs(ItemStack... outputs) {
        return outputItems(outputs);
    }

    public GTRecipeBuilderJS itemOutput(UnificationEntry unificationEntry) {
        return outputItems(unificationEntry.tagPrefix, unificationEntry.material);
    }

    public GTRecipeBuilderJS itemOutput(UnificationEntry unificationEntry, int count) {
        return outputItems(unificationEntry.tagPrefix, unificationEntry.material, count);
    }

    public GTRecipeBuilderJS outputItems(ItemStack... outputs) {
        for (ItemStack itemStack : outputs) {
            if (itemStack.isEmpty()) {
                GTLib.LOGGER.error("gt recipe {} output items is empty", id);
                throw new IllegalArgumentException(id + ": output items is empty");
            }
        }
        return output(ItemRecipeCapability.CAP, Arrays.stream(outputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public GTRecipeBuilderJS outputItems(Item input, int amount) {
        return outputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilderJS outputItems(Item input) {
        return outputItems(new ItemStack(input));
    }

    public GTRecipeBuilderJS outputItems(Supplier<? extends Item> input) {
        return outputItems(new ItemStack(input.get()));
    }

    public GTRecipeBuilderJS outputItems(Supplier<? extends Item> input, int amount) {
        return outputItems(new ItemStack(input.get(), amount));
    }

    public GTRecipeBuilderJS outputItems(TagPrefix orePrefix, Material material) {
        return outputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilderJS outputItems(TagPrefix orePrefix, Material material, int count) {
        return outputItems(ChemicalHelper.get(orePrefix, material, count));
    }

    public GTRecipeBuilderJS outputItems(MachineDefinition machine) {
        return outputItems(machine, 1);
    }

    public GTRecipeBuilderJS outputItems(MachineDefinition machine, int count) {
        return outputItems(machine.asStack(count));
    }

    public GTRecipeBuilderJS notConsumable(ItemStack itemStack) {
        backingBuilder.notConsumable(itemStack);
        return this;
    }

    public GTRecipeBuilderJS notConsumable(Item item) {
        backingBuilder.notConsumable(item);
        return this;
    }

    public GTRecipeBuilderJS notConsumable(Supplier<? extends Item> item) {
        backingBuilder.notConsumable(item);
        return this;
    }

    public GTRecipeBuilderJS notConsumable(TagPrefix orePrefix, Material material) {
        backingBuilder.notConsumable(orePrefix, material);
        return this;
    }

    public GTRecipeBuilderJS circuitMeta(int configuration) {
        return notConsumable(IntCircuitBehaviour.stack(configuration));
    }

    public GTRecipeBuilderJS chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        backingBuilder.chancedOutput(stack, chance, tierChanceBoost);
        return this;
    }

    public GTRecipeBuilderJS chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat), chance, tierChanceBoost);
    }

    public GTRecipeBuilderJS chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
    }

    public GTRecipeBuilderJS inputFluids(FluidStack... inputs) {
        return input(FluidRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilderJS outputFluids(FluidStack... outputs) {
        return output(FluidRecipeCapability.CAP, outputs);
    }

    public GTRecipeBuilderJS inputStress(float stress) {
        return input(StressRecipeCapability.CAP, stress);
    }

    public GTRecipeBuilderJS outputStress(float stress) {
        return output(StressRecipeCapability.CAP, stress);
    }

    //////////////////////////////////////
    //**********     DATA    ***********//
    //////////////////////////////////////
    public GTRecipeBuilderJS addData(String key, Tag data) {
        backingBuilder.addData(key, data);
        return this;
    }

    public GTRecipeBuilderJS addData(String key, int data) {
        backingBuilder.addData(key, data);
        return this;
    }

    public GTRecipeBuilderJS addData(String key, String data) {
        backingBuilder.addData(key, data);
        return this;
    }

    public GTRecipeBuilderJS addData(String key, Float data) {
        backingBuilder.addData(key, data);
        return this;
    }

    public GTRecipeBuilderJS addData(String key, boolean data) {
        backingBuilder.addData(key, data);
        return this;
    }

    public GTRecipeBuilderJS blastFurnaceTemp(int blastTemp) {
        return addData("ebf_temp", blastTemp);
    }

    public GTRecipeBuilderJS explosivesAmount(int explosivesAmount) {
        return addData("explosives_amount", explosivesAmount);
    }

    public GTRecipeBuilderJS explosivesType(ItemStack explosivesType) {
        return addData("explosives_type", explosivesType.save(new CompoundTag()));
    }

    public GTRecipeBuilderJS solderMultiplier(int multiplier) {
        return addData("solderMultiplier", multiplier);
    }

    //////////////////////////////////////
    //*******     CONDITIONS    ********//
    //////////////////////////////////////

    public GTRecipeBuilderJS cleanroom(CleanroomType sterileCleanroom) {
        return this;
    }

    public GTRecipeBuilderJS dimension(ResourceLocation dimension, boolean reverse) {
        return addCondition(new DimensionCondition(dimension).setReverse(reverse));
    }

    public GTRecipeBuilderJS dimension(ResourceLocation dimension) {
        return dimension(dimension, false);
    }

    public GTRecipeBuilderJS biome(ResourceLocation biome, boolean reverse) {
        return addCondition(new BiomeCondition(biome).setReverse(reverse));
    }

    public GTRecipeBuilderJS biome(ResourceLocation biome) {
        return biome(biome, false);
    }

    public GTRecipeBuilderJS rain(float level, boolean reverse) {
        return addCondition(new RainingCondition(level).setReverse(reverse));
    }

    public GTRecipeBuilderJS rain(float level) {
        return rain(level, false);
    }

    public GTRecipeBuilderJS thunder(float level, boolean reverse) {
        return addCondition(new ThunderCondition(level).setReverse(reverse));
    }

    public GTRecipeBuilderJS thunder(float level) {
        return thunder(level, false);
    }

    public GTRecipeBuilderJS posY(int min, int max, boolean reverse) {
        return addCondition(new PositionYCondition(min, max).setReverse(reverse));
    }

    public GTRecipeBuilderJS posY(int min, int max) {
        return posY(min, max, false);
    }

    public GTRecipeBuilderJS rpm(float rpm, boolean reverse) {
        return addCondition(new RPMCondition(rpm).setReverse(reverse));
    }

    public GTRecipeBuilderJS rpm(float rpm) {
        return rpm(rpm, false);
    }

    @Override
    public boolean hasInput(IngredientMatch match) {
        for (var item : backingBuilder.input.get(ItemRecipeCapability.CAP)) {
            Ingredient in = ItemRecipeCapability.CAP.of(item);
            if (match.contains(in)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean replaceInput(IngredientMatch match, Ingredient with, ItemInputTransformer transformer) {
        boolean changed = false;

        var itemInputs = backingBuilder.input.get(ItemRecipeCapability.CAP);
        if (replaceInputScan(match, with, transformer, itemInputs)) {
            return true;
        }
        itemInputs = backingBuilder.tickInput.get(ItemRecipeCapability.CAP);
        if (replaceInputScan(match, with, transformer, itemInputs)) {
            return true;
        }

        return false;
    }

    public boolean replaceInputScan(IngredientMatch match, Ingredient with, ItemInputTransformer transformer, List<Content> stuff) {
        for (Content content : stuff) {
            Ingredient in = ItemRecipeCapability.CAP.of(content);
            if (match.contains(in)) {
                content.content = transformer.transform(this, match, in, with);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasOutput(IngredientMatch match) {
        for (var item : backingBuilder.output.get(ItemRecipeCapability.CAP)) {
            Ingredient in = ItemRecipeCapability.CAP.of(item);
            if (match.contains(in)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean replaceOutput(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer) {
        var itemOutputs = backingBuilder.output.get(ItemRecipeCapability.CAP);
        if (replaceOutputScan(match, with, transformer, itemOutputs)) {
            return true;
        }
        itemOutputs = backingBuilder.tickOutput.get(ItemRecipeCapability.CAP);
        if (replaceOutputScan(match, with, transformer, itemOutputs)) {
            return true;
        }

        return false;
    }

    public boolean replaceOutputScan(IngredientMatch match, ItemStack with, ItemOutputTransformer transformer, List<Content> stuff) {
        for (Content content : stuff) {
            Ingredient in = ItemRecipeCapability.CAP.of(content);
            if (match.contains(in)) {
                content.content = transformer.transform(this, match, in.getItems()[0], with);
                return true;
            }
        }
        return false;
    }

}
