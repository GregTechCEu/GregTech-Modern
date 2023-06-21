package com.gregtechceu.gtceu.data.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.NBTToJsonConverter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class GTRecipeBuilder {

    public final Map<RecipeCapability<?>, List<Content>> input = new HashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> tickInput = new HashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> output = new HashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> tickOutput = new HashMap<>();
    public CompoundTag data = new CompoundTag();
    public final List<RecipeCondition> conditions = new ArrayList<>();
    @Setter
    public ResourceLocation id;
    @Setter
    public GTRecipeType recipeType;
    @Setter
    public int duration = 100;
    @Setter
    public boolean perTick;
    @Setter
    public String slotName;
    @Setter
    public String uiName;
    @Setter
    public float chance = 1;
    @Setter
    public boolean isFuel = false;
    @Setter
    public BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSave;


    public GTRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        this.id = id;
        this.recipeType = recipeType;
    }

    public GTRecipeBuilder copy(String id) {
        return copy(GTCEu.id(id));
    }

    public GTRecipeBuilder copy(ResourceLocation id) {
        GTRecipeBuilder copy = new GTRecipeBuilder(id, this.recipeType);
        this.input.forEach((k, v) -> copy.input.put(k, new ArrayList<>(v)));
        this.output.forEach((k, v) -> copy.output.put(k, new ArrayList<>(v)));
        this.tickInput.forEach((k, v) -> copy.tickInput.put(k, new ArrayList<>(v)));
        this.tickOutput.forEach((k, v) -> copy.tickOutput.put(k, new ArrayList<>(v)));
        copy.conditions.addAll(this.conditions);
        copy.data = this.data.copy();
        copy.duration = this.duration;
        copy.chance = this.chance;
        copy.perTick = this.perTick;
        copy.isFuel = this.isFuel;
        copy.uiName = this.uiName;
        copy.slotName = this.slotName;
        copy.onSave = this.onSave;
        return copy;
    }

    public GTRecipeBuilder copyFrom(GTRecipeBuilder builder) {
        return builder.copy(builder.id).onSave(null).recipeType(recipeType);
    }

    public <T> GTRecipeBuilder input(RecipeCapability<T> capability, T... obj) {
        (perTick ? tickInput : input).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder inputs(RecipeCapability<T> capability, Object... obj) {
        (perTick ? tickInput : input).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder outputs(RecipeCapability<T> capability, Object... obj) {
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, slotName, uiName)).toList());
        return this;
    }

    public GTRecipeBuilder addCondition(RecipeCondition condition) {
        conditions.add(condition);
        return this;
    }

    public GTRecipeBuilder inputEU(long eu) {
        return input(EURecipeCapability.CAP, eu);
    }

    public GTRecipeBuilder EUt(long eu) {
        var lastPerTick = perTick;
        perTick = true;
        if (eu > 0) {
            tickInput.remove(EURecipeCapability.CAP);
            inputEU(eu);
        } else if (eu < 0) {
            tickOutput.remove(EURecipeCapability.CAP);
            outputEU(-eu);
        }
        perTick = lastPerTick;
        return this;
    }

    public GTRecipeBuilder outputEU(long eu) {
        return output(EURecipeCapability.CAP, eu);
    }

    // for kjs
    public GTRecipeBuilder itemInputs(Ingredient... inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder itemInput(UnificationEntry input) {
        return inputItems(input);
    }

    public GTRecipeBuilder itemInput(UnificationEntry input, int count) {
        return inputItems(input, count);
    }

    public GTRecipeBuilder inputItems(Ingredient... inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder inputItems(ItemStack... inputs) {
        for (ItemStack itemStack : inputs) {
            if (itemStack.isEmpty()) {
                LDLib.LOGGER.error("gt recipe {} input items is empty", id);
                throw new IllegalArgumentException(id + ": input items is empty");
            }
        }
        return input(ItemRecipeCapability.CAP, Arrays.stream(inputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
        return inputItems(SizedIngredient.create(tag, amount));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag) {
        return inputItems(SizedIngredient.create(tag, 1));
    }

    public GTRecipeBuilder inputItems(Item input, int amount) {
        return inputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilder inputItems(Item input) {
        return inputItems(Ingredient.of(input));
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input) {
        return inputItems(Ingredient.of(input.get()));
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input, int amount) {
        return inputItems(new ItemStack(input.get(), amount));
    }

    public GTRecipeBuilder inputItems(TagPrefix orePrefix, Material material) {
        return inputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilder inputItems(UnificationEntry input) {
        return inputItems(input.tagPrefix, input.material, 1);
    }

    public GTRecipeBuilder inputItems(UnificationEntry input, int count) {
        return inputItems(input.tagPrefix, input.material, count);
    }

    public GTRecipeBuilder inputItems(TagPrefix orePrefix, Material material, int count) {
        TagKey<Item> tag = ChemicalHelper.getTag(orePrefix, material);
        if (tag == null) {
            return inputItems(ChemicalHelper.get(orePrefix, material, count));
        }
        return inputItems(tag, count);
    }

    public GTRecipeBuilder inputItems(MachineDefinition machine) {
        return inputItems(machine, 1);
    }

    public GTRecipeBuilder inputItems(MachineDefinition machine, int count) {
        return inputItems(machine.asStack(count));
    }

    // for kjs
    public GTRecipeBuilder itemOutputs(ItemStack... outputs) {
        return outputItems(outputs);
    }

    public GTRecipeBuilder itemOutput(UnificationEntry unificationEntry) {
        return outputItems(unificationEntry.tagPrefix, unificationEntry.material);
    }

    public GTRecipeBuilder itemOutput(UnificationEntry unificationEntry, int count) {
        return outputItems(unificationEntry.tagPrefix, unificationEntry.material, count);
    }

    public GTRecipeBuilder outputItems(ItemStack... outputs) {
        for (ItemStack itemStack : outputs) {
            if (itemStack.isEmpty()) {
                LDLib.LOGGER.error("gt recipe {} output items is empty", id);
                throw new IllegalArgumentException(id + ": output items is empty");
            }
        }
        return output(ItemRecipeCapability.CAP, Arrays.stream(outputs).map(SizedIngredient::create).toArray(Ingredient[]::new));
    }

    public GTRecipeBuilder outputItems(Item input, int amount) {
        return outputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilder outputItems(Item input) {
        return outputItems(new ItemStack(input));
    }

    public GTRecipeBuilder outputItems(Supplier<? extends Item> input) {
        return outputItems(new ItemStack(input.get()));
    }

    public GTRecipeBuilder outputItems(Supplier<? extends Item> input, int amount) {
        return outputItems(new ItemStack(input.get(), amount));
    }

    public GTRecipeBuilder outputItems(TagPrefix orePrefix, Material material) {
        return outputItems(orePrefix, material, 1);
    }

    public GTRecipeBuilder outputItems(TagPrefix orePrefix, Material material, int count) {
        return outputItems(ChemicalHelper.get(orePrefix, material, count));
    }

    public GTRecipeBuilder outputItems(MachineDefinition machine) {
        return outputItems(machine, 1);
    }

    public GTRecipeBuilder outputItems(MachineDefinition machine, int count) {
        return outputItems(machine.asStack(count));
    }

    public GTRecipeBuilder notConsumable(ItemStack itemStack) {
        float lastChance = this.chance;
        this.chance = 0;
        inputItems(itemStack);
        this.chance = lastChance;
        return this;
    }
    
    public GTRecipeBuilder notConsumable(Item item) {
        float lastChance = this.chance;
        this.chance = 0;
        inputItems(item);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder notConsumable(Supplier<? extends Item> item) {
        float lastChance = this.chance;
        this.chance = 0;
        inputItems(item);
        this.chance = lastChance;
        return this;
    }
    
    public GTRecipeBuilder notConsumable(TagPrefix orePrefix, Material material) {
        float lastChance = this.chance;
        this.chance = 0;
        inputItems(orePrefix, material);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder circuitMeta(int configuration) {
        return notConsumable(IntCircuitBehaviour.stack(configuration));
    }

    public GTRecipeBuilder chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        // todo tier chance boost
        float lastChance = this.chance;
        this.chance = chance / 10000f;
        outputItems(stack);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
    }

    public GTRecipeBuilder inputFluids(FluidStack... inputs) {
        return input(FluidRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder outputFluids(FluidStack... outputs) {
        return output(FluidRecipeCapability.CAP, outputs);
    }

    public GTRecipeBuilder inputStress(float stress) {
        return input(StressRecipeCapability.CAP, stress);
    }

    public GTRecipeBuilder outputStress(float stress) {
        return output(StressRecipeCapability.CAP, stress);
    }

    //////////////////////////////////////
    //**********     DATA    ***********//
    //////////////////////////////////////
    public GTRecipeBuilder addData(String key, Tag data) {
        this.data.put(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, int data) {
        this.data.putInt(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, String data) {
        this.data.putString(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, Float data) {
        this.data.putFloat(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, boolean data) {
        this.data.putBoolean(key, data);
        return this;
    }

    public GTRecipeBuilder blastFurnaceTemp(int blastTemp) {
        return addData("ebf_temp", blastTemp);
    }

    public GTRecipeBuilder explosivesAmount(int explosivesAmount) {
        return addData("explosives_amount", explosivesAmount);
    }

    public GTRecipeBuilder explosivesType(ItemStack explosivesType) {
        return addData("explosives_type", explosivesType.save(new CompoundTag()));
    }

    public GTRecipeBuilder solderMultiplier(int multiplier) {
        return addData("solderMultiplier", multiplier);
    }

    //////////////////////////////////////
    //*******     CONDITIONS    ********//
    //////////////////////////////////////

    public GTRecipeBuilder cleanroom(CleanroomType sterileCleanroom) {
        return this;
    }

    public GTRecipeBuilder dimension(ResourceLocation dimension, boolean reverse) {
        return addCondition(new DimensionCondition(dimension).setReverse(reverse));
    }

    public GTRecipeBuilder dimension(ResourceLocation dimension) {
        return dimension(dimension, false);
    }

    public GTRecipeBuilder biome(ResourceLocation biome, boolean reverse) {
        return addCondition(new BiomeCondition(biome).setReverse(reverse));
    }

    public GTRecipeBuilder biome(ResourceLocation biome) {
        return biome(biome, false);
    }

    public GTRecipeBuilder rain(float level, boolean reverse) {
        return addCondition(new RainingCondition(level).setReverse(reverse));
    }

    public GTRecipeBuilder rain(float level) {
        return rain(level, false);
    }

    public GTRecipeBuilder thunder(float level, boolean reverse) {
        return addCondition(new ThunderCondition(level).setReverse(reverse));
    }

    public GTRecipeBuilder thunder(float level) {
        return thunder(level, false);
    }

    public GTRecipeBuilder posY(int min, int max, boolean reverse) {
        return addCondition(new PositionYCondition(min, max).setReverse(reverse));
    }

    public GTRecipeBuilder posY(int min, int max) {
        return posY(min, max, false);
    }

    public GTRecipeBuilder rpm(float rpm, boolean reverse) {
        return addCondition(new RPMCondition(rpm).setReverse(reverse));
    }

    public GTRecipeBuilder rpm(float rpm) {
        return rpm(rpm, false);
    }

    public void toJson(JsonObject json) {
        json.addProperty("type", recipeType.registryName.toString());
        json.addProperty("duration", Math.abs(duration));
        if (data != null && !data.isEmpty()) {
            json.add("data", NBTToJsonConverter.getObject(data));
        }
        if (!input.isEmpty()) {
            json.add("inputs", capabilitiesToJson(input));
        }
        if (!output.isEmpty()) {
            json.add("outputs", capabilitiesToJson(output));
        }
        if (!tickInput.isEmpty()) {
            json.add("tickInputs", capabilitiesToJson(tickInput));
        }
        if (!tickOutput.isEmpty()) {
            json.add("tickOutputs", capabilitiesToJson(tickOutput));
        }
        if (!conditions.isEmpty()) {
            JsonArray array = new JsonArray();
            for (RecipeCondition condition : conditions) {
                JsonObject cond = new JsonObject();
                cond.addProperty("type", GTRegistries.RECIPE_CONDITIONS.getKey(condition.getClass()));
                cond.add("data", condition.serialize());
                array.add(cond);
            }
            json.add("recipeConditions", array);
        }
        if (isFuel) {
            json.addProperty("isFuel", true);
        }
    }

    public JsonObject capabilitiesToJson(Map<RecipeCapability<?>, List<Content>> contents) {
        JsonObject jsonObject = new JsonObject();
        contents.forEach((cap, list) -> {
            JsonArray contentsJson = new JsonArray();
            for (Content content : list) {
                contentsJson.add(cap.serializer.toJsonContent(content));
            }
            jsonObject.add(GTRegistries.RECIPE_CAPABILITIES.getKey(cap), contentsJson);
        });
        return jsonObject;
    }

    public FinishedRecipe build() {
        return new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject pJson) {
                toJson(pJson);
            }

            @Override
            public ResourceLocation getId() {
                return new ResourceLocation(id.getNamespace(), recipeType.registryName.getPath() + "/" + id.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return GTRecipeSerializer.SERIALIZER;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        };
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        if (onSave != null) {
            onSave.accept(this, consumer);
        }
        consumer.accept(build());
    }

    //////////////////////////////////////
    //*******     Quick Query    *******//
    //////////////////////////////////////
    public long EUt() {
        if (!tickInput.containsKey(EURecipeCapability.CAP)) return 0;
        if (tickInput.get(EURecipeCapability.CAP).isEmpty()) return 0;
        return EURecipeCapability.CAP.of(tickInput.get(EURecipeCapability.CAP).get(0).content);
    }

    public int getSolderMultiplier() {
        return data.getInt("solderMultiplier");
    }

}
