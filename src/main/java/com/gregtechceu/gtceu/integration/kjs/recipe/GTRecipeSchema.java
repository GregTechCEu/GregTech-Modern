package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.ResearchRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.recipe.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMap;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import com.gregtechceu.gtceu.utils.ResearchManager;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface GTRecipeSchema {

    @SuppressWarnings({ "unused", "UnusedReturnValue" })
    @Accessors(chain = true, fluent = true)
    class GTKubeRecipe extends KubeRecipe {

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
        public Consumer<GTKubeRecipe> onSave;
        @Getter
        private final Collection<GTRecipeBuilder.ResearchRecipeEntry> researchRecipeEntries = new ArrayList<>();
        private boolean generatingRecipes = true;

        @HideFromJS
        @Override
        public GTKubeRecipe id(KubeResourceLocation _id) {
            this.idWithoutType = ResourceLocation.fromNamespaceAndPath(
                    _id.wrapped().getNamespace().equals("kubejs") ? this.type.id.getNamespace() :
                            _id.wrapped().getNamespace(),
                    _id.wrapped().getPath());
            this.id = ResourceLocation.fromNamespaceAndPath(idWithoutType.getNamespace(),
                    "%s/%s".formatted(this.type.id.getPath(), idWithoutType.getPath()));
            return this;
        }

        public <T> GTKubeRecipe input(RecipeCapability<T> capability, Object... obj) {
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

        public <T> GTKubeRecipe output(RecipeCapability<T> capability, Object... obj) {
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

        public GTKubeRecipe addCondition(RecipeCondition condition) {
            if (getValue(CONDITIONS) == null) setValue(CONDITIONS, new ArrayList<>());
            getValue(CONDITIONS).add(condition);
            save();
            return this;
        }

        public GTKubeRecipe inputEU(long eu) {
            return input(EURecipeCapability.CAP, eu);
        }

        public GTKubeRecipe EUt(long eu) {
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

        public GTKubeRecipe outputEU(long eu) {
            return output(EURecipeCapability.CAP, eu);
        }

        public GTKubeRecipe inputCWU(int cwu) {
            return input(CWURecipeCapability.CAP, cwu);
        }

        public GTKubeRecipe CWUt(int cwu) {
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

        public GTKubeRecipe totalCWU(int cwu) {
            this.durationIsTotalCWU(true);
            this.hideDuration(true);
            this.setValue(GTRecipeSchema.DURATION, new TickDuration(cwu));
            return this;
        }

        public GTKubeRecipe outputCWU(int cwu) {
            return output(CWURecipeCapability.CAP, cwu);
        }

        public GTKubeRecipe inputItems(SizedIngredient... inputs) {
            return input(ItemRecipeCapability.CAP, (Object[]) inputs);
        }

        public GTKubeRecipe outputItems(SizedIngredient... outputs) {
            return output(ItemRecipeCapability.CAP, (Object[]) outputs);
        }

        public GTKubeRecipe inputFluids(SizedFluidIngredient... inputs) {
            return input(FluidRecipeCapability.CAP, (Object[]) inputs);
        }

        public GTKubeRecipe itemOutputsRanged(SizedIngredient ingredient, int min, int max) {
            return output(ItemRecipeCapability.CAP, new SizedIngredient(
                    new IntProviderIngredient(ingredient.ingredient(), UniformInt.of(min, max)).toVanilla(), 1));
        }

        public GTKubeRecipe outputItemsRanged(Ingredient ingredient, int min, int max) {
            return output(ItemRecipeCapability.CAP,
                    new SizedIngredient(new IntProviderIngredient(ingredient, UniformInt.of(min, max)).toVanilla(), 1));
        }

        public GTKubeRecipe outputItemsRanged(ItemStack stack, int min, int max) {
            return output(ItemRecipeCapability.CAP,
                    new IntProviderIngredient(Ingredient.of(stack), UniformInt.of(min, max)));
        }

        public GTKubeRecipe outputItemsRanged(TagPrefix orePrefix, Material material, int min, int max) {
            return outputItemsRanged(ChemicalHelper.get(orePrefix, material, 1), min, max);
        }

        public GTKubeRecipe notConsumableItem(SizedIngredient itemStack) {
            int lastChance = this.chance;
            this.chance = 0;
            inputItems(itemStack);
            this.chance = lastChance;
            return this;
        }

        public GTKubeRecipe notConsumableItem(TagPrefix orePrefix, Material material) {
            int lastChance = this.chance;
            this.chance = 0;
            inputItems(SizedIngredient.of(ChemicalHelper.get(orePrefix, material).getItem(), 1));
            this.chance = lastChance;
            return this;
        }

        public GTKubeRecipe circuit(int configuration) {
            return notConsumableItem(
                    new SizedIngredient(IntCircuitIngredient.circuitInput(configuration).toVanilla(), 1));
        }

        public GTKubeRecipe chancedInput(SizedIngredient stack, int chance, int tierChanceBoost) {
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

        @HideFromJS
        public GTKubeRecipe chancedOutput(SizedIngredient stack, int chance, int tierChanceBoost) {
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

        public GTKubeRecipe chancedFluidInput(SizedFluidIngredient stack, int chance,
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

        public GTKubeRecipe chancedOutput(TagPrefix tag, Material mat, int chance, int tierChanceBoost) {
            return chancedOutput(SizedIngredient.of(ChemicalHelper.get(tag, mat).getItem(), 1), chance,
                    tierChanceBoost);
        }

        public GTKubeRecipe chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
            return chancedOutput(SizedIngredient.of(ChemicalHelper.get(tag, mat).getItem(), count), chance,
                    tierChanceBoost);
        }

        public GTKubeRecipe chancedOutput(SizedIngredient stack, String fraction, int tierChanceBoost) {
            if (stack.count() == 0 || stack.ingredient().isEmpty()) {
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

        public GTKubeRecipe chancedOutput(TagPrefix prefix, Material material, int count, String fraction,
                                          int tierChanceBoost) {
            return chancedOutput(SizedIngredient.of(ChemicalHelper.get(prefix, material).getItem(), count), fraction,
                    tierChanceBoost);
        }

        public GTKubeRecipe chancedOutput(TagPrefix prefix, Material material, String fraction, int tierChanceBoost) {
            return chancedOutput(prefix, material, 1, fraction, tierChanceBoost);
        }

        public GTKubeRecipe chancedFluidOutput(SizedFluidIngredient stack, int chance, int tierChanceBoost) {
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

        public GTKubeRecipe chancedFluidOutput(SizedFluidIngredient stack, double chance, double tierChanceBoost) {
            return chancedFluidOutput(stack, (int) chance, (int) tierChanceBoost);
        }

        public GTKubeRecipe chancedFluidOutput(SizedFluidIngredient stack, String fraction, int tierChanceBoost) {
            if (stack.amount() == 0) {
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

        public GTKubeRecipe chancedOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(OUTPUT_CHANCE_LOGICS) == null) setValue(OUTPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(OUTPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTKubeRecipe chancedItemOutputLogic(ChanceLogic logic) {
            return chancedOutputLogic(ItemRecipeCapability.CAP, logic);
        }

        public GTKubeRecipe chancedFluidOutputLogic(ChanceLogic logic) {
            return chancedOutputLogic(FluidRecipeCapability.CAP, logic);
        }

        public GTKubeRecipe chancedInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(INPUT_CHANCE_LOGICS) == null) setValue(INPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(INPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTKubeRecipe chancedItemInputLogic(ChanceLogic logic) {
            return chancedInputLogic(ItemRecipeCapability.CAP, logic);
        }

        public GTKubeRecipe chancedFluidInputLogic(ChanceLogic logic) {
            return chancedInputLogic(FluidRecipeCapability.CAP, logic);
        }

        public GTKubeRecipe chancedTickOutputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(TICK_OUTPUT_CHANCE_LOGICS) == null) setValue(TICK_OUTPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(TICK_OUTPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTKubeRecipe chancedTickInputLogic(RecipeCapability<?> cap, ChanceLogic logic) {
            if (getValue(TICK_INPUT_CHANCE_LOGICS) == null) setValue(TICK_INPUT_CHANCE_LOGICS, new HashMap<>());
            getValue(TICK_INPUT_CHANCE_LOGICS).put(cap, logic);
            save();
            return this;
        }

        public GTKubeRecipe outputFluids(FluidStack... outputs) {
            return output(FluidRecipeCapability.CAP, (Object[]) outputs);
        }

        @HideFromJS
        public GTKubeRecipe outputFluids(SizedFluidIngredient... outputs) {
            return output(FluidRecipeCapability.CAP, (Object[]) outputs);
        }

        public GTKubeRecipe inputStress(float stress) {
            return input(StressRecipeCapability.CAP, stress);
        }

        public GTKubeRecipe outputStress(float stress) {
            return output(StressRecipeCapability.CAP, stress);
        }

        //////////////////////////////////////
        // ********** DATA ***********//
        //////////////////////////////////////
        public GTKubeRecipe addData(String key, Tag data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).put(key, data);
            save();
            return this;
        }

        @HideFromJS
        public GTKubeRecipe addData(String key, int data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putInt(key, data);
            save();
            return this;
        }

        @HideFromJS
        public GTKubeRecipe addData(String key, long data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putLong(key, data);
            save();
            return this;
        }

        public GTKubeRecipe addDataString(String key, String data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putString(key, data);
            save();
            return this;
        }

        @HideFromJS
        public GTKubeRecipe addData(String key, float data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putFloat(key, data);
            save();
            return this;
        }

        public GTKubeRecipe addDataNumber(String key, double data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putDouble(key, data);
            save();
            return this;
        }

        public GTKubeRecipe addDataBool(String key, boolean data) {
            if (getValue(DATA) == null) setValue(DATA, new CompoundTag());
            getValue(DATA).putBoolean(key, data);
            save();
            return this;
        }

        public GTKubeRecipe blastFurnaceTemp(int blastTemp) {
            return addData("ebf_temp", blastTemp);
        }

        public GTKubeRecipe explosivesAmount(int explosivesAmount) {
            return addData("explosives_amount", explosivesAmount);
        }

        public GTKubeRecipe explosivesType(ItemStack explosivesType) {
            return addData("explosives_type", explosivesType.save(Platform.getFrozenRegistry()));
        }

        public GTKubeRecipe solderMultiplier(int multiplier) {
            return addData("solder_multiplier", multiplier);
        }

        public GTKubeRecipe disableDistilleryRecipes(boolean flag) {
            return addDataBool("disable_distillery", flag);
        }

        public GTKubeRecipe fusionStartEU(long eu) {
            return addData("eu_to_start", eu);
        }

        public GTKubeRecipe researchScan(boolean isScan) {
            return addDataBool("scan_for_research", isScan);
        }

        public GTKubeRecipe durationIsTotalCWU(boolean durationIsTotalCWU) {
            return addDataBool("duration_is_total_cwu", durationIsTotalCWU);
        }

        public GTKubeRecipe hideDuration(boolean hideDuration) {
            return addDataBool("hide_duration", hideDuration);
        }

        //////////////////////////////////////
        // ******* CONDITIONS ********//
        //////////////////////////////////////

        public GTKubeRecipe cleanroom(CleanroomType cleanroomType) {
            return addCondition(new CleanroomCondition(cleanroomType));
        }

        public GTKubeRecipe dimension(ResourceLocation dimension, boolean reverse) {
            return addCondition(new DimensionCondition(dimension).setReverse(reverse));
        }

        public GTKubeRecipe dimension(ResourceLocation dimension) {
            return dimension(dimension, false);
        }

        public GTKubeRecipe biome(ResourceLocation biome, boolean reverse) {
            return addCondition(new BiomeCondition(biome).setReverse(reverse));
        }

        public GTKubeRecipe biome(ResourceLocation biome) {
            return biome(biome, false);
        }

        public GTKubeRecipe rain(float level, boolean reverse) {
            return addCondition(new RainingCondition(level).setReverse(reverse));
        }

        public GTKubeRecipe rain(float level) {
            return rain(level, false);
        }

        public GTKubeRecipe thunder(float level, boolean reverse) {
            return addCondition(new ThunderCondition(level).setReverse(reverse));
        }

        public GTKubeRecipe thunder(float level) {
            return thunder(level, false);
        }

        public GTKubeRecipe posY(int min, int max, boolean reverse) {
            return addCondition(new PositionYCondition(min, max).setReverse(reverse));
        }

        public GTKubeRecipe posY(int min, int max) {
            return posY(min, max, false);
        }

        public GTKubeRecipe rpm(float rpm, boolean reverse) {
            return addCondition(new RPMCondition(rpm).setReverse(reverse));
        }

        public GTKubeRecipe rpm(float rpm) {
            return rpm(rpm, false);
        }

        public GTKubeRecipe environmentalHazard(MedicalCondition condition, boolean reverse) {
            return addCondition(new EnvironmentalHazardCondition(condition).setReverse(reverse));
        }

        public GTKubeRecipe environmentalHazard(MedicalCondition condition) {
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

            if (getValue(CONDITIONS) == null) setValue(CONDITIONS, List.of());
            ResearchCondition condition = this.getValue(CONDITIONS).stream()
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
        public GTKubeRecipe researchWithoutRecipe(@NotNull String researchId) {
            return researchWithoutRecipe(researchId, ResearchManager.getDefaultScannerItem());
        }

        /**
         * Does not generate a research recipe.
         *
         * @param researchId the researchId for the recipe
         * @param dataStack  the stack to hold the data. Must have the {@link IDataItem} behavior.
         * @return this
         */
        public GTKubeRecipe researchWithoutRecipe(@NotNull String researchId, @NotNull ItemStack dataStack) {
            applyResearchProperty(new ResearchData.ResearchEntry(researchId, dataStack));
            this.generatingRecipes = false;
            return this;
        }

        /**
         * Generates a research recipe for the Scanner.
         */
        public GTKubeRecipe scannerResearch(UnaryOperator<ResearchRecipeBuilder.ScannerRecipeBuilder> research) {
            GTRecipeBuilder.ResearchRecipeEntry entry = research.apply(new ResearchRecipeBuilder.ScannerRecipeBuilder())
                    .build();
            if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId(), entry.dataStack()))) {
                this.researchRecipeEntries.add(entry);
            }
            return this;
        }

        /**
         * Generates a research recipe for the Research Station.
         */
        public GTKubeRecipe stationResearch(UnaryOperator<ResearchRecipeBuilder.StationRecipeBuilder> research) {
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
        public @Nullable RecipeHolder<?> createRecipe() {
            if (onSave != null) {
                onSave.accept(this);
            }
            return super.createRecipe();
        }
    }

    RecipeKey<ResourceLocation> ID = GTRecipeComponents.RESOURCE_LOCATION.key("id", ComponentRole.OTHER);
    RecipeKey<TickDuration> DURATION = TimeComponent.TICKS.key("duration", ComponentRole.OTHER)
            .optional(new TickDuration(100));
    RecipeKey<CompoundTag> DATA = GTRecipeComponents.TAG.key("data", ComponentRole.OTHER).optional((CompoundTag) null);
    RecipeKey<List<RecipeCondition>> CONDITIONS = GTRecipeComponents.RECIPE_CONDITION.asList()
            .key("recipeConditions", ComponentRole.OTHER)
            .defaultOptional();
    RecipeKey<Boolean> IS_FUEL = BooleanComponent.BOOLEAN.key("isFuel", ComponentRole.OTHER).optional(false);

    RecipeKey<CapabilityMap> ALL_INPUTS = GTRecipeComponents.IN.key("inputs", ComponentRole.INPUT).defaultOptional();
    RecipeKey<CapabilityMap> ALL_TICK_INPUTS = GTRecipeComponents.TICK_IN.key("tickInputs", ComponentRole.INPUT)
            .defaultOptional();

    RecipeKey<CapabilityMap> ALL_OUTPUTS = GTRecipeComponents.OUT.key("outputs", ComponentRole.OUTPUT)
            .defaultOptional();
    RecipeKey<CapabilityMap> ALL_TICK_OUTPUTS = GTRecipeComponents.TICK_OUT.key("tickOutputs", ComponentRole.OUTPUT)
            .defaultOptional();

    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> INPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("inputChanceLogics", ComponentRole.OTHER).defaultOptional();
    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> OUTPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("outputChanceLogics", ComponentRole.OTHER).defaultOptional();
    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> TICK_INPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("tickInputChanceLogics", ComponentRole.OTHER).defaultOptional();
    RecipeKey<Map<RecipeCapability<?>, ChanceLogic>> TICK_OUTPUT_CHANCE_LOGICS = GTRecipeComponents.CHANCE_LOGIC_MAP
            .key("tickOutputChanceLogics", ComponentRole.OTHER).defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(DURATION, DATA, CONDITIONS, ALL_INPUTS,
            ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS,
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS,
            IS_FUEL)
            .factory(new KubeRecipeFactory(GTCEu.id("recipe"), GTKubeRecipe.class, GTKubeRecipe::new))
            .constructor(new IDRecipeConstructor());
}
