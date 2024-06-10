package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.common.recipe.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class GTRecipeBuilder {

    public final Map<RecipeCapability<?>, List<Content>> input = new HashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> tickInput = new HashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> output = new HashMap<>();
    public final Map<RecipeCapability<?>, List<Content>> tickOutput = new HashMap<>();
    @NotNull
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
    public float tierChanceBoost = 0;
    @Setter
    public boolean isFuel = false;
    @Setter
    public BiConsumer<GTRecipeBuilder, RecipeOutput> onSave;

    public GTRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        this.id = id;
        this.recipeType = recipeType;
    }

    public GTRecipeBuilder(GTRecipe toCopy, GTRecipeType recipeType) {
        this.recipeType = recipeType;
        toCopy.inputs.forEach((k, v) -> this.input.put(k, new ArrayList<>(v)));
        toCopy.outputs.forEach((k, v) -> this.output.put(k, new ArrayList<>(v)));
        toCopy.tickInputs.forEach((k, v) -> this.tickInput.put(k, new ArrayList<>(v)));
        toCopy.tickOutputs.forEach((k, v) -> this.tickOutput.put(k, new ArrayList<>(v)));
        this.conditions.addAll(toCopy.conditions);
        this.data = toCopy.data.copy();
        this.duration = toCopy.duration;
        this.isFuel = toCopy.isFuel;
    }

    public static GTRecipeBuilder of(ResourceLocation id, GTRecipeType recipeType) {
        return new GTRecipeBuilder(id, recipeType);
    }

    public static GTRecipeBuilder ofRaw() {
        return new GTRecipeBuilder(GTCEu.id("raw"), GTRecipeTypes.DUMMY_RECIPES);
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
                .map(o -> new Content(o, chance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder output(RecipeCapability<T> capability, T... obj) {
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder inputs(RecipeCapability<T> capability, Object... obj) {
        (perTick ? tickInput : input).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, tierChanceBoost, slotName, uiName)).toList());
        return this;
    }

    public <T> GTRecipeBuilder outputs(RecipeCapability<T> capability, Object... obj) {
        (perTick ? tickOutput : output).computeIfAbsent(capability, c -> new ArrayList<>()).addAll(Arrays.stream(obj)
                .map(capability::of)
                .map(o -> new Content(o, chance, tierChanceBoost, slotName, uiName)).toList());
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

    public GTRecipeBuilder inputCWU(int cwu) {
        return input(CWURecipeCapability.CAP, cwu);
    }

    public GTRecipeBuilder CWUt(int cwu) {
        var lastPerTick = perTick;
        perTick = true;
        if (cwu > 0) {
            tickInput.remove(CWURecipeCapability.CAP);
            inputCWU(cwu);
        } else if (cwu < 0) {
            tickOutput.remove(CWURecipeCapability.CAP);
            outputCWU(cwu);
        }
        perTick = lastPerTick;
        return this;
    }

    public GTRecipeBuilder totalCWU(int cwu) {
        this.durationIsTotalCWU(true);
        this.hideDuration(true);
        this.duration(cwu);
        return this;
    }

    public GTRecipeBuilder outputCWU(int cwu) {
        return output(CWURecipeCapability.CAP, cwu);
    }

    public GTRecipeBuilder inputItems(SizedIngredient... inputs) {
        return input(ItemRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder inputItems(Ingredient input) {
        return input(ItemRecipeCapability.CAP, new SizedIngredient(input, 1));
    }

    public GTRecipeBuilder inputItems(Ingredient... inputs) {
        return input(ItemRecipeCapability.CAP,
                Arrays.stream(inputs).map(input -> new SizedIngredient(input, 1)).toArray(SizedIngredient[]::new));
    }

    public GTRecipeBuilder inputItems(ItemStack input) {
        if (input.isEmpty()) {
            GTCEu.LOGGER.error("gt recipe {} input items is empty", id);
            throw new IllegalArgumentException(id + ": input items is empty");
        }
        if (input.getComponents().isEmpty() && !input.getComponents().equals(input.getPrototype())) {
            return input(ItemRecipeCapability.CAP, SizedIngredient.of(input.getItem(), input.getCount()));
        } else {
            return input(ItemRecipeCapability.CAP,
                    new SizedIngredient(DataComponentIngredient.of(true, input), input.getCount()));
        }
    }

    public GTRecipeBuilder inputItems(ItemStack... inputs) {
        for (ItemStack itemStack : inputs) {
            if (itemStack.isEmpty()) {
                GTCEu.LOGGER.error("gt recipe {} input items is empty", id);
                throw new IllegalArgumentException(id + ": input items is empty");
            }
        }
        return input(ItemRecipeCapability.CAP, Arrays.stream(inputs).map(stack -> {
            if (stack.getComponents().isEmpty()) {
                return SizedIngredient.of(stack.getItem(), stack.getCount());
            } else {
                return new SizedIngredient(DataComponentIngredient.of(true, stack), stack.getCount());
            }
        }).toArray(SizedIngredient[]::new));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag, int amount) {
        return inputItems(SizedIngredient.of(tag, amount));
    }

    public GTRecipeBuilder inputItems(TagKey<Item> tag) {
        return inputItems(tag, 1);
    }

    public GTRecipeBuilder inputItems(Item input, int amount) {
        return inputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilder inputItems(Item input) {
        return inputItems(SizedIngredient.of(input, 1));
    }

    public GTRecipeBuilder inputItems(Supplier<? extends Item> input) {
        return inputItems(input.get());
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

    public GTRecipeBuilder outputItems(ItemStack output) {
        if (output.isEmpty()) {
            GTCEu.LOGGER.error("gt recipe {} output items is empty", id);
            throw new IllegalArgumentException(id + ": output items is empty");
        }
        return output(ItemRecipeCapability.CAP,
                new SizedIngredient(DataComponentIngredient.of(true, output), output.getCount()));
    }

    public GTRecipeBuilder outputItems(ItemStack... outputs) {
        for (ItemStack itemStack : outputs) {
            if (itemStack.isEmpty()) {
                GTCEu.LOGGER.error("gt recipe {} output items is empty", id);
                throw new IllegalArgumentException(id + ": output items is empty");
            }
        }
        return output(ItemRecipeCapability.CAP,
                Arrays.stream(outputs)
                        .map(item -> new SizedIngredient(DataComponentIngredient.of(true, item), item.getCount()))
                        .toArray(SizedIngredient[]::new));
    }

    public GTRecipeBuilder outputItems(Item input, int amount) {
        return outputItems(new ItemStack(input, amount));
    }

    public GTRecipeBuilder outputItems(Item input) {
        return outputItems(new ItemStack(input));
    }

    public GTRecipeBuilder outputItems(Supplier<? extends ItemLike> input) {
        return outputItems(new ItemStack(input.get().asItem()));
    }

    public GTRecipeBuilder outputItems(Supplier<? extends ItemLike> input, int amount) {
        return outputItems(new ItemStack(input.get().asItem(), amount));
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

    public GTRecipeBuilder notConsumable(SizedIngredient ingredient) {
        float lastChance = this.chance;
        this.chance = 0;
        inputItems(ingredient);
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

    public GTRecipeBuilder notConsumableFluid(FluidStack fluid) {
        chancedInput(fluid, 0, 0);
        return this;
    }

    public GTRecipeBuilder notConsumableFluid(SizedFluidIngredient ingredient) {
        float lastChance = this.chance;
        this.chance = 0;
        inputFluids(ingredient);
        this.chance = lastChance;
        return this;
    }

    public GTRecipeBuilder circuitMeta(int configuration) {
        return notConsumable(new SizedIngredient(IntCircuitIngredient.circuitInput(configuration).toVanilla(), 1));
    }

    public GTRecipeBuilder chancedInput(ItemStack stack, int chance, int tierChanceBoost) {
        float lastChance = this.chance;
        float lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance / 10000f;
        this.tierChanceBoost = tierChanceBoost / 10000f;
        inputItems(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedInput(FluidStack stack, int chance, int tierChanceBoost) {
        float lastChance = this.chance;
        float lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance / 10000f;
        this.tierChanceBoost = tierChanceBoost / 10000f;
        inputFluids(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        float lastChance = this.chance;
        float lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance / 10000f;
        this.tierChanceBoost = tierChanceBoost / 10000f;
        outputItems(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedOutput(FluidStack stack, int chance, int tierChanceBoost) {
        float lastChance = this.chance;
        float lastTierChanceBoost = this.tierChanceBoost;
        this.chance = chance / 10000f;
        this.tierChanceBoost = tierChanceBoost / 10000f;
        outputFluids(stack);
        this.chance = lastChance;
        this.tierChanceBoost = lastTierChanceBoost;
        return this;
    }

    public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat), chance, tierChanceBoost);
    }

    public GTRecipeBuilder chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
        return chancedOutput(ChemicalHelper.get(tag, mat, count), chance, tierChanceBoost);
    }

    public GTRecipeBuilder inputFluids(FluidStack input) {
        return input(FluidRecipeCapability.CAP, SizedFluidIngredient.of(
                TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(input.getFluid()).getPath()), input.getAmount()));
    }

    public GTRecipeBuilder inputFluids(FluidStack... inputs) {
        return input(FluidRecipeCapability.CAP, Arrays.stream(inputs).map(fluid -> {
            return SizedFluidIngredient.of(
                    TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()),
                    fluid.getAmount());
        }).toArray(SizedFluidIngredient[]::new));
    }

    public GTRecipeBuilder inputFluids(SizedFluidIngredient... inputs) {
        return input(FluidRecipeCapability.CAP, inputs);
    }

    public GTRecipeBuilder outputFluids(FluidStack output) {
        return output(FluidRecipeCapability.CAP, SizedFluidIngredient.of(output));
    }

    public GTRecipeBuilder outputFluids(FluidStack... outputs) {
        return output(FluidRecipeCapability.CAP,
                Arrays.stream(outputs).map(SizedFluidIngredient::of).toArray(SizedFluidIngredient[]::new));
    }

    public GTRecipeBuilder outputFluids(SizedFluidIngredient... outputs) {
        return output(FluidRecipeCapability.CAP, outputs);
    }

    public GTRecipeBuilder inputStress(float stress) {
        return input(StressRecipeCapability.CAP, stress);
    }

    public GTRecipeBuilder outputStress(float stress) {
        return output(StressRecipeCapability.CAP, stress);
    }

    //////////////////////////////////////
    // ********** DATA ***********//
    //////////////////////////////////////
    public GTRecipeBuilder addData(String key, Tag data) {
        this.data.put(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, int data) {
        this.data.putInt(key, data);
        return this;
    }

    public GTRecipeBuilder addData(String key, long data) {
        this.data.putLong(key, data);
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
        return inputItems(new ItemStack(Blocks.TNT, explosivesAmount));
    }

    public GTRecipeBuilder explosivesType(ItemStack explosivesType) {
        return inputItems(explosivesType);
    }

    public GTRecipeBuilder solderMultiplier(int multiplier) {
        return addData("solder_multiplier", multiplier);
    }

    public GTRecipeBuilder disableDistilleryRecipes(boolean flag) {
        return addData("disable_distillery", flag);
    }

    public GTRecipeBuilder fusionStartEU(long eu) {
        return addData("eu_to_start", eu);
    }

    public GTRecipeBuilder researchScan(boolean isScan) {
        return addData("scan_for_research", isScan);
    }

    public GTRecipeBuilder durationIsTotalCWU(boolean durationIsTotalCWU) {
        return addData("duration_is_total_cwu", durationIsTotalCWU);
    }

    public GTRecipeBuilder hideDuration(boolean hideDuration) {
        return addData("hide_duration", hideDuration);
    }

    //////////////////////////////////////
    // ******* CONDITIONS ********//
    //////////////////////////////////////

    public GTRecipeBuilder cleanroom(CleanroomType cleanroomType) {
        return addCondition(new CleanroomCondition(cleanroomType));
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

    private boolean applyResearchProperty(ResearchData.ResearchEntry researchEntry) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return false;
        if (researchEntry == null) {
            GTCEu.LOGGER.error("Research Entry cannot be empty.", new IllegalArgumentException());
            return false;
        }

        if (!generatingRecipes) {
            GTCEu.LOGGER.error("Cannot generate recipes when using researchWithoutRecipe()",
                    new IllegalArgumentException());
            return false;
        }

        ResearchCondition condition = this.conditions.stream().filter(ResearchCondition.class::isInstance).findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (condition != null) {
            condition.data.add(researchEntry);
        } else {
            condition = new ResearchCondition();
            condition.data.add(researchEntry);
            this.addCondition(condition);
        }
        return true;
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @return this
     */
    public GTRecipeBuilder researchWithoutRecipe(@NotNull String researchId) {
        return researchWithoutRecipe(researchId, ResearchManager.getDefaultScannerItem());
    }

    /**
     * Does not generate a research recipe.
     *
     * @param researchId the researchId for the recipe
     * @param dataStack  the stack to hold the data. Must have the {@link IDataItem} behavior.
     * @return this
     */
    public GTRecipeBuilder researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
        applyResearchProperty(new ResearchData.ResearchEntry(researchId, dataStack));
        this.generatingRecipes = false;
        return this;
    }

    /**
     * Generates a research recipe for the Scanner.
     */
    public GTRecipeBuilder scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder()).build();
        if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.researchRecipeEntries.add(entry);
        }
        return this;
    }

    /**
     * Generates a research recipe for the Scanner. All values are defaults other than the research stack.
     *
     * @param researchStack the stack to use for research
     * @return this
     */
    public GTRecipeBuilder scannerResearch(@NotNull ItemStack researchStack) {
        return scannerResearch(b -> b.researchStack(researchStack));
    }

    /**
     * Generates a research recipe for the Research Station.
     */
    public GTRecipeBuilder stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
        ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.StationRecipeBuilder()).build();
        if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId, entry.dataStack))) {
            this.researchRecipeEntries.add(entry);
        }
        return this;
    }

    public GTRecipe build() {
        return new GTRecipe(this.recipeType, this.input, this.output, this.tickInput, this.tickOutput, this.conditions,
                List.of(), this.data, this.duration, this.isFuel);
    }

    public void save(RecipeOutput consumer) {
        if (onSave != null) {
            onSave.accept(this, consumer);
        }
        ResearchCondition condition = this.conditions.stream().filter(ResearchCondition.class::isInstance).findAny()
                .map(ResearchCondition.class::cast).orElse(null);
        if (condition != null) {
            for (ResearchData.ResearchEntry entry : condition.data) {
                this.recipeType.addDataStickEntry(entry.getResearchId(), buildRecipe());
            }
        }
        consumer.accept(
                ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                        recipeType.registryName.getPath() + "/" + id.getPath()),
                build(), null);
    }

    public GTRecipe buildRecipe() {
        return new GTRecipe(recipeType, input, output, tickInput, tickOutput, conditions, List.of(), data, duration,
                isFuel);
    }

    //////////////////////////////////////
    // ******* Quick Query *******//
    //////////////////////////////////////
    public long EUt() {
        if (!tickInput.containsKey(EURecipeCapability.CAP)) return 0;
        if (tickInput.get(EURecipeCapability.CAP).isEmpty()) return 0;
        return EURecipeCapability.CAP.of(tickInput.get(EURecipeCapability.CAP).get(0).content);
    }

    public int getSolderMultiplier() {
        if (data.contains("solderMultiplier")) {
            return Math.max(1, data.getInt("solderMultiplier"));
        }
        return Math.max(1, data.getInt("solder_multiplier"));
    }

    /**
     * An entry for an autogenerated research recipe for producing a data item containing research data.
     * 
     * @param researchId    the id of the research to store
     * @param researchStack the stack to scan for research
     * @param dataStack     the stack to contain the data
     * @param duration      the duration of the recipe
     * @param EUt           the EUt of the recipe
     * @param CWUt          how much computation per tick this recipe needs if in Research Station
     */
    public record ResearchRecipeEntry(
                                      @NotNull String researchId,
                                      @NotNull ItemStack researchStack,
                                      @NotNull ItemStack dataStack,
                                      int duration,
                                      int EUt,
                                      int CWUt) {}
}
