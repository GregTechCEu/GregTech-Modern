package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.ResearchRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.common.recipe.condition.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMap;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ExtendedOutputItem;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface GTRecipeSchema {

    @SuppressWarnings({ "unused", "UnusedReturnValue" })
    @Accessors(chain = true, fluent = true)
    class GTRecipeJS extends RecipeJS {

        @Setter
        public boolean perTick;
        @Setter
        public int chance = ChanceLogic.getMaxChancedValue();
        @Setter
        public int maxChance = ChanceLogic.getMaxChancedValue();
        @Setter
        public int tierChanceBoost = 0;
        @Setter
        public boolean isFuel = false;
        @Getter
        private ResourceLocation idWithoutType;
        @Setter
        public Consumer<GTRecipeJS> onSave;
        @Getter
        private final Collection<GTRecipeBuilder.ResearchRecipeEntry> researchRecipeEntries = new ArrayList<>();
        private boolean generatingRecipes = true;

        @HideFromJS
        @Override
        public GTRecipeJS id(ResourceLocation _id) {
            this.idWithoutType = new ResourceLocation(
                    _id.getNamespace().equals("minecraft") ? this.type.id.getNamespace() : _id.getNamespace(),
                    _id.getPath());
            this.id = new ResourceLocation(idWithoutType.getNamespace(),
                    "%s/%s".formatted(this.type.id.getPath(), idWithoutType.getPath()));
            return this;
        }

        public <T> GTRecipeJS input(RecipeCapability<T> capability, Object... obj) {
            CapabilityMap map;
            if (perTick) {
                if (getValue(ALL_TICK_INPUTS) == null) setValue(ALL_TICK_INPUTS, new CapabilityMap());
                map = getValue(ALL_TICK_INPUTS);
            } else {
                if (getValue(ALL_INPUTS) == null) setValue(ALL_INPUTS, new CapabilityMap());
                map = getValue(ALL_INPUTS);
            }
            if (map != null) {
                for (Object object : obj) {
                    map.add(capability, new Content(object, chance, maxChance, tierChanceBoost, null, null));
                }
            }
            save();
            return this;
        }

        public <T> GTRecipeJS output(RecipeCapability<T> capability, Object... obj) {
            CapabilityMap map;
            if (perTick) {
                if (getValue(ALL_TICK_OUTPUTS) == null) setValue(ALL_TICK_OUTPUTS, new CapabilityMap());
                map = getValue(ALL_TICK_OUTPUTS);
            } else {
                if (getValue(ALL_OUTPUTS) == null) setValue(ALL_OUTPUTS, new CapabilityMap());
                map = getValue(ALL_OUTPUTS);
            }
            if (map != null) {
                for (Object object : obj) {
                    map.add(capability, new Content(object, chance, maxChance, tierChanceBoost, null, null));
                }
            }
            save();
            return this;
        }

        public GTRecipeJS addCondition(RecipeCondition condition) {
            setValue(CONDITIONS, ArrayUtils.add(getValue(CONDITIONS), condition));
            save();
            return this;
        }

        public GTRecipeJS inputEU(long eu) {
            return input(EURecipeCapability.CAP, eu);
        }

        public GTRecipeJS EUt(long eu) {
            var lastPerTick = perTick;
            perTick = true;
            if (eu > 0) {
                inputEU(eu);
            } else if (eu < 0) {
                outputEU(-eu);
            }
            perTick = lastPerTick;
            return this;
        }

        public GTRecipeJS outputEU(long eu) {
            return output(EURecipeCapability.CAP, eu);
        }

        public GTRecipeJS inputCWU(int cwu) {
            return input(CWURecipeCapability.CAP, cwu);
        }

        public GTRecipeJS CWUt(int cwu) {
            var lastPerTick = perTick;
            perTick = true;
            if (cwu > 0) {
                inputCWU(cwu);
            } else if (cwu < 0) {
                outputCWU(cwu);
            }
            perTick = lastPerTick;
            return this;
        }

        public GTRecipeJS totalCWU(int cwu) {
            this.durationIsTotalCWU(true);
            this.hideDuration(true);
            this.setValue(GTRecipeSchema.DURATION, (long) cwu);
            return this;
        }

        public GTRecipeJS outputCWU(int cwu) {
            return output(CWURecipeCapability.CAP, cwu);
        }

        public GTRecipeJS itemInputs(InputItem... inputs) {
            return inputItems(inputs);
        }

        public GTRecipeJS itemInput(UnificationEntry input) {
            return inputItems(input);
        }

        public GTRecipeJS itemInput(UnificationEntry input, int count) {
            return inputItems(input, count);
        }

        public GTRecipeJS inputItems(InputItem... inputs) {
            return input(ItemRecipeCapability.CAP, (Object[]) inputs);
        }

        public GTRecipeJS inputItems(ItemStack... inputs) {
            for (ItemStack itemStack : inputs) {
                if (itemStack.isEmpty()) {
                    GTCEu.LOGGER.error("gt recipe {} input items is empty", id);
                }
            }
            return input(ItemRecipeCapability.CAP,
                    Arrays.stream(inputs)
                            .map(stack -> InputItem.of(SizedIngredient.create(
                                    stack.hasTag() ? NBTIngredient.createNBTIngredient(stack) : Ingredient.of(stack),
                                    stack.getCount()), stack.getCount()))
                            .toArray());
        }

        public GTRecipeJS inputItems(TagKey<Item> tag, int amount) {
            return inputItems(InputItem.of(SizedIngredient.create(tag, amount)));
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

        public GTRecipeJS inputItems(TagPrefix orePrefix, Material material, int count) {
            return inputItems(ChemicalHelper.getTag(orePrefix, material), count);
        }

        public GTRecipeJS inputItems(MachineDefinition machine) {
            return inputItems(machine, 1);
        }

        public GTRecipeJS inputItems(MachineDefinition machine, int count) {
            return inputItems(machine.asStack(count));
        }

        public GTRecipeJS itemOutputs(ExtendedOutputItem... outputs) {
            return outputItems(outputs);
        }

        public GTRecipeJS itemOutput(UnificationEntry unificationEntry) {
            return outputItems(unificationEntry.tagPrefix, unificationEntry.material);
        }

        public GTRecipeJS itemOutput(UnificationEntry unificationEntry, int count) {
            return outputItems(unificationEntry.tagPrefix, unificationEntry.material, count);
        }

        public GTRecipeJS outputItems(ExtendedOutputItem... outputs) {
            for (ExtendedOutputItem itemStack : outputs) {
                if (itemStack.isEmpty()) {
                    GTCEu.LOGGER.error("gt recipe {} output items is empty", id);
                }
            }
            return output(ItemRecipeCapability.CAP, (Object[]) outputs);
        }

        public GTRecipeJS outputItems(Item input, int amount) {
            return outputItems(ExtendedOutputItem.of(new ItemStack(input, amount)));
        }

        public GTRecipeJS outputItems(Item input) {
            return outputItems(ExtendedOutputItem.of(new ItemStack(input)));
        }

        public GTRecipeJS outputItems(TagPrefix orePrefix, Material material) {
            return outputItems(orePrefix, material, 1);
        }

        public GTRecipeJS outputItems(TagPrefix orePrefix, Material material, int count) {
            return outputItems(ExtendedOutputItem.of(ChemicalHelper.get(orePrefix, material, count)));
        }

        public GTRecipeJS outputItems(MachineDefinition machine) {
            return outputItems(machine, 1);
        }

        public GTRecipeJS outputItems(MachineDefinition machine, int count) {
            return outputItems(new ExtendedOutputItem(machine.asStack(count)));
        }

        public GTRecipeJS itemOutputsRanged(ExtendedOutputItem ingredient, int min, int max) {
            return outputItemsRanged(ingredient.ingredient.getInner(), min, max);
        }

        public GTRecipeJS outputItemsRanged(Ingredient ingredient, int min, int max) {
            return output(ItemRecipeCapability.CAP, new IntProviderIngredient(ingredient, UniformInt.of(min, max)));
        }

        public GTRecipeJS outputItemsRanged(ItemStack stack, int min, int max) {
            return outputItemsRanged(Ingredient.of(stack), min, max);
        }

        public GTRecipeJS outputItemsRanged(TagPrefix orePrefix, Material material, int min, int max) {
            return outputItemsRanged(ChemicalHelper.get(orePrefix, material), min, max);
        }

        public GTRecipeJS notConsumable(InputItem itemStack) {
            int lastChance = this.chance;
            this.chance = 0;
            inputItems(itemStack);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumable(TagPrefix orePrefix, Material material) {
            int lastChance = this.chance;
            this.chance = 0;
            inputItems(orePrefix, material);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumableFluid(GTRecipeComponents.FluidIngredientJS fluid) {
            int lastChance = this.chance;
            this.chance = 0;
            inputFluids(fluid);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS circuit(int configuration) {
            return notConsumable(InputItem.of(IntCircuitIngredient.circuitInput(configuration), 1));
        }

        public GTRecipeJS chancedInput(InputItem stack, int chance, int tierChanceBoost) {
            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), chance, new Throwable());
                return this;
            }
            int lastChance = this.chance;
            int lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance;
            this.tierChanceBoost = tierChanceBoost;
            inputItems(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedFluidInput(GTRecipeComponents.FluidIngredientJS stack, int chance,
                                            int tierChanceBoost) {
            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), chance, new Throwable());
                return this;
            }
            int lastChance = this.chance;
            int lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance;
            this.tierChanceBoost = tierChanceBoost;
            inputFluids(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedOutput(ExtendedOutputItem stack, int chance, int tierChanceBoost) {
            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), chance, new Throwable());
                return this;
            }
            int lastChance = this.chance;
            int lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance;
            this.tierChanceBoost = tierChanceBoost;
            outputItems(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
            return chancedOutput(ExtendedOutputItem.of(ChemicalHelper.get(tag, mat)), chance, tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
            return chancedOutput(ExtendedOutputItem.of(ChemicalHelper.get(tag, mat, count)), chance, tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(ExtendedOutputItem stack, String fraction, int tierChanceBoost) {
            if (stack.isEmpty()) {
                return this;
            }

            String[] split = fraction.split("/");
            if (split.length > 2) {
                GTCEu.LOGGER.error(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"{}\".",
                        fraction, new Throwable());
                return this;
            }

            int chance;
            int maxChance;

            if (split.length == 1) {
                try {
                    chance = (int) Double.parseDouble(split[0]);
                } catch (NumberFormatException e) {
                    GTCEu.LOGGER.error(
                            "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"{}\".",
                            fraction, new Throwable());
                    return this;
                }
                return chancedOutput(stack, chance, tierChanceBoost);
            }
            try {
                chance = Integer.parseInt(split[0]);
                maxChance = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                GTCEu.LOGGER.error(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"{}\".",
                        fraction, new Throwable());
                return this;
            }

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), chance, new Throwable());
                return this;
            }
            if (chance >= maxChance || maxChance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), maxChance, new Throwable());
                return this;
            }

            int scalar = Math.floorDiv(ChanceLogic.getMaxChancedValue(), maxChance);
            chance *= scalar;
            maxChance *= scalar;

            int lastChance = this.chance;
            int lastMaxChance = this.maxChance;
            int lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance;
            this.maxChance = maxChance;
            this.tierChanceBoost = tierChanceBoost;
            outputItems(stack);
            this.chance = lastChance;
            this.maxChance = lastMaxChance;
            this.tierChanceBoost = lastTierChanceBoost;

            return this;
        }

        public GTRecipeJS chancedOutput(TagPrefix prefix, Material material, int count, String fraction,
                                        int tierChanceBoost) {
            return chancedOutput(ExtendedOutputItem.of(ChemicalHelper.get(prefix, material, count)), fraction,
                    tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(TagPrefix prefix, Material material, String fraction, int tierChanceBoost) {
            return chancedOutput(prefix, material, 1, fraction, tierChanceBoost);
        }

        public GTRecipeJS chancedFluidOutput(FluidStackJS stack, int chance, int tierChanceBoost) {
            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), chance, new Throwable());
                return this;
            }
            int lastChance = this.chance;
            int lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance;
            this.tierChanceBoost = tierChanceBoost;
            outputFluids(stack);
            this.chance = lastChance;
            this.tierChanceBoost = lastTierChanceBoost;
            return this;
        }

        public GTRecipeJS chancedFluidOutput(FluidStackJS stack, String fraction, int tierChanceBoost) {
            if (stack.getAmount() == 0) {
                return this;
            }

            String[] split = fraction.split("/");
            if (split.length > 2) {
                GTCEu.LOGGER.error(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"{}\".",
                        fraction, new Throwable());
                return this;
            }

            int chance;
            int maxChance;

            if (split.length == 1) {
                try {
                    chance = (int) Double.parseDouble(split[0]);
                } catch (NumberFormatException e) {
                    GTCEu.LOGGER.error(
                            "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"{}\".",
                            fraction, new Throwable());
                    return this;
                }
                return chancedFluidOutput(stack, chance, tierChanceBoost);
            }

            try {
                chance = Integer.parseInt(split[0]);
                maxChance = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                GTCEu.LOGGER.error(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"{}\".",
                        fraction, new Throwable());
                return this;
            }

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Chance cannot be less or equal to 0 or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), chance, new Throwable());
                return this;
            }
            if (chance >= maxChance || maxChance > ChanceLogic.getMaxChancedValue()) {
                GTCEu.LOGGER.error("Max Chance cannot be less or equal to Chance or more than {}. Actual: {}.",
                        ChanceLogic.getMaxChancedValue(), maxChance, new Throwable());
                return this;
            }

            int scalar = Math.floorDiv(ChanceLogic.getMaxChancedValue(), maxChance);
            chance *= scalar;
            maxChance *= scalar;

            int lastChance = this.chance;
            int lastMaxChance = this.maxChance;
            int lastTierChanceBoost = this.tierChanceBoost;
            this.chance = chance;
            this.maxChance = maxChance;
            this.tierChanceBoost = tierChanceBoost;
            outputFluids(stack);
            this.chance = lastChance;
            this.maxChance = lastMaxChance;
            this.tierChanceBoost = lastTierChanceBoost;

            return this;
        }

        public GTRecipeJS chancedOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(OUTPUT_CHANCE_LOGICS) == null) setValue(OUTPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(OUTPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTRecipeJS chancedItemOutputLogic(ChanceLogic logic) {
            return chancedOutputLogic(ItemRecipeCapability.CAP, logic);
        }

        public GTRecipeJS chancedFluidOutputLogic(ChanceLogic logic) {
            return chancedOutputLogic(FluidRecipeCapability.CAP, logic);
        }

        public GTRecipeJS chancedInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(INPUT_CHANCE_LOGICS) == null) setValue(INPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(INPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTRecipeJS chancedItemInputLogic(ChanceLogic logic) {
            return chancedInputLogic(ItemRecipeCapability.CAP, logic);
        }

        public GTRecipeJS chancedFluidInputLogic(ChanceLogic logic) {
            return chancedInputLogic(FluidRecipeCapability.CAP, logic);
        }

        public GTRecipeJS chancedTickOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(TICK_OUTPUT_CHANCE_LOGICS) == null) setValue(TICK_OUTPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(TICK_OUTPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTRecipeJS chancedTickInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(TICK_INPUT_CHANCE_LOGICS) == null) setValue(TICK_INPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(TICK_INPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTRecipeJS inputFluids(GTRecipeComponents.FluidIngredientJS... inputs) {
            return input(FluidRecipeCapability.CAP, (Object[]) inputs);
        }

        public GTRecipeJS outputFluids(FluidStackJS... outputs) {
            return output(FluidRecipeCapability.CAP, (Object[]) outputs);
        }

        public GTRecipeJS inputStress(float stress) {
            return input(StressRecipeCapability.CAP, stress);
        }

        public GTRecipeJS outputStress(float stress) {
            return output(StressRecipeCapability.CAP, stress);
        }

        //////////////////////////////////////
        // ********** DATA ***********//
        //////////////////////////////////////
        public GTRecipeJS addData(String key, Tag data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).put(key, data);
            save();
            return this;
        }

        @HideFromJS
        public GTRecipeJS addData(String key, int data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putInt(key, data);
            save();
            return this;
        }

        @HideFromJS
        public GTRecipeJS addData(String key, long data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putLong(key, data);
            save();
            return this;
        }

        public GTRecipeJS addDataString(String key, String data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putString(key, data);
            save();
            return this;
        }

        @HideFromJS
        public GTRecipeJS addData(String key, float data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putFloat(key, data);
            save();
            return this;
        }

        public GTRecipeJS addDataNumber(String key, double data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putDouble(key, data);
            save();
            return this;
        }

        public GTRecipeJS addDataBool(String key, boolean data) {
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
            return addData("solder_multiplier", multiplier);
        }

        public GTRecipeJS disableDistilleryRecipes(boolean flag) {
            return addDataBool("disable_distillery", flag);
        }

        public GTRecipeJS fusionStartEU(long eu) {
            return addData("eu_to_start", eu);
        }

        public GTRecipeJS researchScan(boolean isScan) {
            return addDataBool("scan_for_research", isScan);
        }

        public GTRecipeJS durationIsTotalCWU(boolean durationIsTotalCWU) {
            return addDataBool("duration_is_total_cwu", durationIsTotalCWU);
        }

        public GTRecipeJS hideDuration(boolean hideDuration) {
            return addDataBool("hide_duration", hideDuration);
        }

        //////////////////////////////////////
        // ******* CONDITIONS ********//
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

        public GTRecipeJS environmentalHazard(MedicalCondition condition, boolean reverse) {
            return addCondition(new EnvironmentalHazardCondition(condition).setReverse(reverse));
        }

        public GTRecipeJS environmentalHazard(MedicalCondition condition) {
            return environmentalHazard(condition, false);
        }

        private boolean applyResearchProperty(ResearchData.ResearchEntry researchEntry) {
            if (!ConfigHolder.INSTANCE.machines.enableResearch) return false;
            if (researchEntry == null) {
                GTCEu.LOGGER.error("Assembly Line Research Entry cannot be empty.", new IllegalArgumentException());
                return false;
            }

            if (!generatingRecipes) {
                GTCEu.LOGGER.error("Cannot generate recipes when using researchWithoutRecipe()",
                        new IllegalArgumentException());
                return false;
            }

            if (getValue(CONDITIONS) == null) setValue(CONDITIONS, new RecipeCondition[0]);
            ResearchCondition condition = Arrays.stream(this.getValue(CONDITIONS))
                    .filter(ResearchCondition.class::isInstance).findAny().map(ResearchCondition.class::cast)
                    .orElse(null);
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
        public GTRecipeJS researchWithoutRecipe(@NotNull String researchId) {
            return researchWithoutRecipe(researchId, ResearchManager.getDefaultScannerItem());
        }

        /**
         * Does not generate a research recipe.
         *
         * @param researchId the researchId for the recipe
         * @param dataStack  the stack to hold the data. Must have the {@link IDataItem} behavior.
         * @return this
         */
        public GTRecipeJS researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
            applyResearchProperty(new ResearchData.ResearchEntry(researchId, dataStack));
            this.generatingRecipes = false;
            return this;
        }

        /**
         * Generates a research recipe for the Scanner.
         */
        public GTRecipeJS scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
            GTRecipeBuilder.ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder())
                    .build();
            if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId(), entry.dataStack()))) {
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
        public GTRecipeJS scannerResearch(@NotNull ItemStack researchStack) {
            return scannerResearch(b -> b.researchStack(researchStack));
        }

        /**
         * Generates a research recipe for the Research Station.
         */
        public GTRecipeJS stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
            GTRecipeBuilder.ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.StationRecipeBuilder())
                    .build();
            if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId(), entry.dataStack()))) {
                this.researchRecipeEntries.add(entry);
            }
            return this;
        }

        /*
         * KubeJS overrides
         */

        @Override
        public @Nullable Recipe<?> createRecipe() {
            if (onSave != null) {
                onSave.accept(this);
            }
            return super.createRecipe();
        }

        public InputItem readInputItem(Object from) {
            if (from instanceof SizedIngredient ingr) {
                return InputItem.of(ingr.getInner(), ingr.getAmount());
            } else if (from instanceof JsonObject jsonObject) {
                if (!jsonObject.has("type") ||
                        !jsonObject.get("type").getAsString().equals(SizedIngredient.TYPE.toString())) {
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
            if (from instanceof ExtendedOutputItem outputItem) {
                return outputItem;
            } else if (from instanceof OutputItem outputItem) {
                return outputItem;
            } else if (from instanceof SizedIngredient ingredient) {
                if (ingredient.getInner() instanceof IntProviderIngredient intProvider) {
                    return new ExtendedOutputItem(intProvider, 1);
                }
                return OutputItem.of(ingredient.getInner().getItems()[0], Double.NaN);
            } else if (from instanceof IntProviderIngredient ingredient) {
                return new ExtendedOutputItem(ingredient, 1);
            } else if (from instanceof JsonObject jsonObject) {
                float chance = 1.0f;
                if (jsonObject.has("chance")) {
                    chance = jsonObject.get("chance").getAsFloat();
                }
                if (jsonObject.has("content")) {
                    jsonObject = jsonObject.getAsJsonObject("content");
                }
                var ingredient = Ingredient.fromJson(jsonObject);
                return OutputItem.of(ingredient.getItems()[0], chance);
            }
            return OutputItem.of(from);
        }

        @Override
        public JsonElement writeOutputItem(OutputItem value) {
            if (value instanceof ExtendedOutputItem extended) {
                if (extended.ingredient.getInner() instanceof IntProviderIngredient intProvider) {
                    return intProvider.toJson();
                }
                return extended.ingredient.toJson();
            }
            return SizedIngredient.create(value.item).toJson();
        }

        @Override
        public JsonElement writeInputFluid(InputFluid value) {
            var fluid = ((FluidStackJS) value).getFluidStack();
            return FluidIngredient.of((int) fluid.getAmount(), fluid.getFluid()).toJson();
        }

        @Override
        public InputFluid readInputFluid(Object from) {
            return super.readInputFluid(from);
        }
    }

    RecipeKey<ResourceLocation> ID = GTRecipeComponents.RESOURCE_LOCATION.key("id");
    RecipeKey<Long> DURATION = TimeComponent.TICKS.key("duration").optional(100L);
    RecipeKey<CompoundTag> DATA = GTRecipeComponents.TAG.key("data").optional((CompoundTag) null);
    RecipeKey<RecipeCondition[]> CONDITIONS = GTRecipeComponents.RECIPE_CONDITION.asArray().key("recipeConditions")
            .defaultOptional();
    RecipeKey<Boolean> IS_FUEL = BooleanComponent.BOOLEAN.key("isFuel").optional(false);

    RecipeKey<CapabilityMap> ALL_INPUTS = GTRecipeComponents.IN.key("inputs").defaultOptional();
    RecipeKey<CapabilityMap> ALL_TICK_INPUTS = GTRecipeComponents.TICK_IN.key("tickInputs").defaultOptional();

    RecipeKey<CapabilityMap> ALL_OUTPUTS = GTRecipeComponents.OUT.key("outputs").defaultOptional();
    RecipeKey<CapabilityMap> ALL_TICK_OUTPUTS = GTRecipeComponents.TICK_OUT.key("tickOutputs").defaultOptional();

    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> INPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("inputChanceLogics").defaultOptional();
    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> OUTPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("outputChanceLogics").defaultOptional();
    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> TICK_INPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("tickInputChanceLogics").defaultOptional();
    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> TICK_OUTPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("tickOutputChanceLogics").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(GTRecipeJS.class, GTRecipeJS::new, DURATION, DATA, CONDITIONS,
            ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS,
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS,
            IS_FUEL)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);
}
