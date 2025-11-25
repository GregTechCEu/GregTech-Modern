package com.gregtechceu.gtceu.integration.kjs.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.ResearchData;
import com.gregtechceu.gtceu.api.recipe.ResearchRecipeBuilder;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.recipe.condition.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMap;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ExtendedOutputItem;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
        @Getter
        private ResourceLocation idWithoutType;
        @Setter
        public Consumer<GTRecipeJS> onSave;
        @Getter
        private final Collection<GTRecipeBuilder.ResearchRecipeEntry> researchRecipeEntries = new ArrayList<>();
        private boolean generatingRecipes = true;

        // material stacks that are from already resolved inputs
        public List<MaterialStack> itemMaterialStacks = new ArrayList<>();
        public List<MaterialStack> fluidMaterialStacks = new ArrayList<>();
        // temporary buffer for unresolved item stacks where decomp is found post recipe addition
        public List<ItemStack> tempItemStacks = new ArrayList<>();
        public boolean itemMaterialInfo = false;
        public boolean fluidMaterialInfo = false;
        public boolean removeMaterialInfo = false;

        @HideFromJS
        @Override
        public GTRecipeJS id(ResourceLocation _id) {
            this.idWithoutType = new ResourceLocation(
                    _id.getNamespace().equals("minecraft") ? this.type.id.getNamespace() : _id.getNamespace(),
                    _id.getPath());
            this.id = idWithoutType.withPrefix(this.type.id.getPath() + "/");
            save();
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
                var recipeType = GTRegistries.RECIPE_TYPES.get(this.type.id);
                if (map.get(capability) != null &&
                        map.get(capability).length + obj.length > recipeType.getMaxInputs(capability)) {
                    ConsoleJS.SERVER.warn(String.format(
                            "Trying to add more inputs than RecipeType can support, id: %s, Max %s%sInputs: %s",
                            id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxInputs(capability)));
                }
                for (Object object : obj) {
                    map.add(capability, new Content(object, chance, maxChance, tierChanceBoost));
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
                var recipeType = GTRegistries.RECIPE_TYPES.get(this.type.id);
                if (map.get(capability) != null &&
                        map.get(capability).length + obj.length > recipeType.getMaxOutputs(capability)) {
                    ConsoleJS.SERVER.warn(String.format(
                            "Trying to add more outputs than RecipeType can support, id: %s, Max %s%sOutputs: %s",
                            id, (perTick ? "Tick " : ""), capability.name, recipeType.getMaxOutputs(capability)));
                }
                for (Object object : obj) {
                    map.add(capability, new Content(object, chance, maxChance, tierChanceBoost));
                }
            }
            save();
            return this;
        }

        public GTRecipeJS addCondition(RecipeCondition condition) {
            if (getValue(CONDITIONS) == null) setValue(CONDITIONS, new RecipeCondition[] { condition });
            else setValue(CONDITIONS, ArrayUtils.add(getValue(CONDITIONS), condition));

            save();
            return this;
        }

        public GTRecipeJS category(GTRecipeCategory category) {
            setValue(CATEGORY, category.registryKey);
            save();
            return this;
        }

        public GTRecipeJS inputEU(EnergyStack eu) {
            return input(EURecipeCapability.CAP, eu);
        }

        public GTRecipeJS inputEU(long voltage, long amperage) {
            return inputEU(new EnergyStack(voltage, amperage));
        }

        @SuppressWarnings("ConstantValue")
        public GTRecipeJS EUt(EnergyStack.WithIO eu) {
            if (eu.isEmpty()) {
                throw new RecipeExceptionJS(String.format("EUt can't be explicitly set to 0, id: %s", id));
            }
            if (eu.amperage() < 1) {
                throw new RecipeExceptionJS(String.format("Amperage must be a positive integer, id: %s", id));
            }
            var lastPerTick = perTick;
            perTick = true;
            if (eu.isInput()) {
                inputEU(eu.stack());
            } else if (eu.isOutput()) {
                outputEU(eu.stack());
            }
            perTick = lastPerTick;
            return this;
        }

        public GTRecipeJS EUt(long voltage, long amperage) {
            return EUt(EnergyStack.WithIO.fromVA(voltage, amperage));
        }

        public GTRecipeJS outputEU(EnergyStack eu) {
            return output(EURecipeCapability.CAP, eu);
        }

        public GTRecipeJS outputEU(long voltage, long amperage) {
            return outputEU(new EnergyStack(voltage, amperage));
        }

        public GTRecipeJS inputCWU(int cwu) {
            return input(CWURecipeCapability.CAP, cwu);
        }

        public GTRecipeJS CWUt(int cwu) {
            if (cwu == 0) {
                throw new RecipeExceptionJS(String.format("CWUt can't be explicitly set to 0, id: %s", id));
            }
            var lastPerTick = perTick;
            perTick = true;
            if (cwu > 0) {
                inputCWU(cwu);
            } else if (cwu < 0) {
                outputCWU(-cwu);
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

        public GTRecipeJS itemInput(MaterialEntry input) {
            return inputItems(input);
        }

        public GTRecipeJS itemInput(MaterialEntry input, int count) {
            return inputItems(input, count);
        }

        public GTRecipeJS inputItems(InputItem... inputs) {
            validateItems("input", inputs);

            for (var stack : inputs) {
                // test simple item that have pure singular material stack
                var matStack = ChemicalHelper.getMaterialStack(stack.ingredient.getItems()[0].getItem());
                // test item that has multiple material stacks
                var matInfo = ChemicalHelper.getMaterialInfo(stack.ingredient.getItems()[0].getItem());
                if (chance == maxChance && chance != 0) {
                    if (!matStack.isEmpty()) {
                        itemMaterialStacks.add(matStack.multiply(stack.count));
                    }
                    if (matInfo != null) {
                        for (var ms : matInfo.getMaterials()) {
                            itemMaterialStacks.add(ms.multiply(stack.count));
                        }
                    } else {
                        tempItemStacks.add(stack.ingredient.getItems()[0].copyWithCount(stack.count));
                    }
                }
            }
            return input(ItemRecipeCapability.CAP, (Object[]) inputs);
        }

        public GTRecipeJS inputItems(ItemStack... inputs) {
            validateItems("input", inputs);

            for (ItemStack itemStack : inputs) {
                if (itemStack.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Input items is empty, id: %s", id));
                }
                gatherMaterialInfoFromStacks(itemStack);
            }
            return input(ItemRecipeCapability.CAP,
                    Arrays.stream(inputs)
                            .map(stack -> InputItem.of(
                                    stack.hasTag() ? StrictNBTIngredient.of(stack) : Ingredient.of(stack),
                                    stack.getCount()))
                            .toArray());
        }

        public GTRecipeJS inputItems(TagKey<Item> tag, int amount) {
            return inputItems(InputItem.of(Ingredient.of(tag), amount));
        }

        public GTRecipeJS inputItems(Item input, int amount) {
            return inputItems(new ItemStack(input, amount));
        }

        public GTRecipeJS inputItems(Item input) {
            return inputItems(InputItem.of(Ingredient.of(input), 1));
        }

        public GTRecipeJS inputItems(Supplier<? extends Item> input) {
            return inputItems(input.get());
        }

        public GTRecipeJS inputItems(Supplier<? extends Item> input, int amount) {
            return inputItems(new ItemStack(input.get(), amount));
        }

        public GTRecipeJS inputItems(TagPrefix orePrefix, Material material) {
            return inputItems(orePrefix, material, 1);
        }

        public GTRecipeJS inputItems(MaterialEntry input) {
            return inputItems(input.tagPrefix(), input.material(), 1);
        }

        public GTRecipeJS inputItems(MaterialEntry input, int count) {
            return inputItems(input.tagPrefix(), input.material(), count);
        }

        public GTRecipeJS inputItems(TagPrefix orePrefix, Material material, int count) {
            itemMaterialStacks.add(new MaterialStack(material, orePrefix.getMaterialAmount(material) * count));
            return inputItems(ChemicalHelper.getTag(orePrefix, material), count);
        }

        public GTRecipeJS inputItems(MachineDefinition machine) {
            return inputItems(machine, 1);
        }

        public GTRecipeJS inputItems(MachineDefinition machine, int count) {
            return inputItems(machine.asStack(count));
        }

        public GTRecipeJS itemInputsRanged(ExtendedOutputItem ingredient, int min, int max) {
            return inputItemsRanged(ingredient.ingredient.getInner(), min, max);
        }

        public GTRecipeJS inputItemsRanged(Ingredient ingredient, int min, int max) {
            validateItems("ranged input", ingredient);
            return input(ItemRecipeCapability.CAP, new ExtendedOutputItem(ingredient, 1, UniformInt.of(min, max)));
        }

        public GTRecipeJS inputItemsRanged(ItemStack stack, int min, int max) {
            validateItems("ranged input", stack);
            return input(ItemRecipeCapability.CAP, new ExtendedOutputItem(stack, UniformInt.of(min, max)));
        }

        public GTRecipeJS itemInputsRanged(TagPrefix orePrefix, Material material, int min, int max) {
            return inputItemsRanged(ChemicalHelper.get(orePrefix, material), min, max);
        }

        public GTRecipeJS inputItemNbtPredicate(ItemStack itemStack, NBTPredicate predicate) {
            if (itemStack.isEmpty()) {
                throw new RecipeExceptionJS(String.format("Input items is empty, id: %s", id));
            }
            gatherMaterialInfoFromStacks(itemStack);

            return itemInputs(InputItem.of(new NBTPredicateIngredient(itemStack, predicate), itemStack.getCount()));
        }

        public GTRecipeJS itemOutputs(ExtendedOutputItem... outputs) {
            return outputItems(outputs);
        }

        public GTRecipeJS itemOutput(MaterialEntry materialEntry) {
            return outputItems(materialEntry.tagPrefix(), materialEntry.material());
        }

        public GTRecipeJS itemOutput(MaterialEntry materialEntry, int count) {
            return outputItems(materialEntry.tagPrefix(), materialEntry.material(), count);
        }

        public GTRecipeJS outputItems(ExtendedOutputItem... outputs) {
            validateItems("output", outputs);
            for (ExtendedOutputItem itemStack : outputs) {
                if (itemStack.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Output items is empty, id: %s", id));
                }
            }
            return output(ItemRecipeCapability.CAP, (Object[]) outputs);
        }

        public GTRecipeJS outputItems(Item input, int amount) {
            return outputItems(new ExtendedOutputItem(new ItemStack(input, amount), null));
        }

        public GTRecipeJS outputItems(Item input) {
            return outputItems(new ExtendedOutputItem(new ItemStack(input), null));
        }

        public GTRecipeJS outputItems(TagPrefix orePrefix, Material material) {
            return outputItems(orePrefix, material, 1);
        }

        public GTRecipeJS outputItems(TagPrefix orePrefix, Material material, int count) {
            return outputItems(new ExtendedOutputItem(ChemicalHelper.get(orePrefix, material, count), null));
        }

        public GTRecipeJS outputItems(MachineDefinition machine) {
            return outputItems(machine, 1);
        }

        public GTRecipeJS outputItems(MachineDefinition machine, int count) {
            return outputItems(new ExtendedOutputItem(machine.asStack(count), null));
        }

        public GTRecipeJS itemOutputsRanged(ExtendedOutputItem ingredient, int min, int max) {
            return outputItemsRanged(ingredient.ingredient.getInner(), min, max);
        }

        public GTRecipeJS outputItemsRanged(Ingredient ingredient, int min, int max) {
            validateItems("ranged output", ingredient);
            return output(ItemRecipeCapability.CAP, new ExtendedOutputItem(ingredient, 1, UniformInt.of(min, max)));
        }

        public GTRecipeJS outputItemsRanged(ItemStack stack, int min, int max) {
            validateItems("ranged output", stack);
            return output(ItemRecipeCapability.CAP, new ExtendedOutputItem(stack, UniformInt.of(min, max)));
        }

        public GTRecipeJS outputItemsRanged(TagPrefix orePrefix, Material material, int min, int max) {
            return outputItemsRanged(ChemicalHelper.get(orePrefix, material), min, max);
        }

        public GTRecipeJS notConsumable(InputItem itemStack) {
            validateItems("not consumable", itemStack);

            int lastChance = this.chance;
            this.chance = 0;
            inputItems(itemStack);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumable(TagPrefix orePrefix, Material material) {
            validateItems("not consumable", orePrefix);

            int lastChance = this.chance;
            this.chance = 0;
            inputItems(orePrefix, material);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS notConsumableFluid(GTRecipeComponents.FluidIngredientJS fluid) {
            validateFluids("not consumable", fluid);

            int lastChance = this.chance;
            this.chance = 0;
            inputFluids(fluid);
            this.chance = lastChance;
            return this;
        }

        public GTRecipeJS circuit(int configuration) {
            if (configuration < 0 || configuration > IntCircuitBehaviour.CIRCUIT_MAX) {
                throw new RecipeExceptionJS("Circuit configuration must be in the bounds 0 - 32");
            }
            return notConsumable(InputItem.of(IntCircuitIngredient.of(configuration), 1));
        }

        public GTRecipeJS chancedInput(InputItem stack, int chance, int tierChanceBoost) {
            validateItems("chanced input", stack);

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(
                        String.format("Chance cannot be less or equal to 0 or more than %s, Actual: %s, id: %s",
                                ChanceLogic.getMaxChancedValue(), chance, id));
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
            validateFluids("chanced input", stack);

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(
                        String.format("Chance cannot be less or equal to 0 or more than %s, Actual: %s, id: %s",
                                ChanceLogic.getMaxChancedValue(), chance, id));
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
            validateItems("chanced output", stack);

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(
                        String.format("Chance cannot be less or equal to 0 or more than %s, Actual: %s, id: %s",
                                ChanceLogic.getMaxChancedValue(), chance, id));
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
            return chancedOutput(new ExtendedOutputItem(ChemicalHelper.get(tag, mat), null), chance, tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(TagPrefix tag, Material mat, int count, int chance, int tierChanceBoost) {
            return chancedOutput(new ExtendedOutputItem(ChemicalHelper.get(tag, mat, count), null), chance,
                    tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(ExtendedOutputItem stack, String fraction, int tierChanceBoost) {
            validateItems("chanced output", stack);

            String[] split = fraction.split("/");
            if (split.length > 2) {
                throw new RecipeExceptionJS(String.format(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"%s\".",
                        fraction));
            }

            int chance;
            int maxChance;

            if (split.length == 1) {
                try {
                    chance = (int) Double.parseDouble(split[0]);
                } catch (NumberFormatException e) {
                    throw new RecipeExceptionJS(String.format(
                            "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"%s\".",
                            fraction));
                }
                return chancedOutput(stack, chance, tierChanceBoost);
            }
            try {
                chance = Integer.parseInt(split[0]);
                maxChance = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                throw new RecipeExceptionJS(String.format(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"%s\".",
                        fraction));
            }

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(
                        String.format("Chance cannot be less or equal to 0 or more than %s, Actual: %s, id: %s",
                                ChanceLogic.getMaxChancedValue(), chance, id));
            }
            if (chance >= maxChance || maxChance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(String.format(
                        "Max Chance cannot be less or equal to Chance or more than %s, Actual: %s, id: %s",
                        ChanceLogic.getMaxChancedValue(), maxChance, id));
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
            return chancedOutput(new ExtendedOutputItem(ChemicalHelper.get(prefix, material, count), null),
                    fraction, tierChanceBoost);
        }

        public GTRecipeJS chancedOutput(TagPrefix prefix, Material material, String fraction, int tierChanceBoost) {
            return chancedOutput(prefix, material, 1, fraction, tierChanceBoost);
        }

        public GTRecipeJS chancedFluidOutput(FluidStackJS stack, int chance, int tierChanceBoost) {
            validateFluids("chanced output", stack);

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(
                        String.format("Chance cannot be less or equal to 0 or more than %s, Actual: %s, id: %s",
                                ChanceLogic.getMaxChancedValue(), chance, id));
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
            validateFluids("chanced output", stack);

            if (stack.getAmount() == 0) {
                return this;
            }

            String[] split = fraction.split("/");
            if (split.length > 2) {
                throw new RecipeExceptionJS(String.format(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"%s\".",
                        fraction));
            }

            int chance;
            int maxChance;

            if (split.length == 1) {
                try {
                    chance = (int) Double.parseDouble(split[0]);
                } catch (NumberFormatException e) {
                    throw new RecipeExceptionJS(String.format(
                            "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"%s\".",
                            fraction));
                }
                return chancedFluidOutput(stack, chance, tierChanceBoost);
            }

            try {
                chance = Integer.parseInt(split[0]);
                maxChance = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                throw new RecipeExceptionJS(String.format(
                        "Fraction or number was not parsed correctly! Expected format is \"1/3\" or \"1000\". Actual: \"%s\".",
                        fraction), e);
            }

            if (0 >= chance || chance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(
                        String.format("Chance cannot be less or equal to 0 or more than %s, Actual: %s, id: %s",
                                ChanceLogic.getMaxChancedValue(), chance, id));
            }
            if (chance >= maxChance || maxChance > ChanceLogic.getMaxChancedValue()) {
                throw new RecipeExceptionJS(String.format(
                        "Max Chance cannot be less or equal to Chance or more than %s, Actual: %s, id: %s",
                        ChanceLogic.getMaxChancedValue(), maxChance, id));
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
            validateFluids("input", inputs);

            for (var fluidIng : inputs) {
                for (var stack : fluidIng.ingredient().getStacks()) {
                    var mat = ChemicalHelper.getMaterial(stack.getFluid());
                    if (!mat.isNull()) {
                        fluidMaterialStacks.add(new MaterialStack(mat,
                                ((long) stack.getAmount() * GTValues.M) / GTValues.L));
                    }
                }
            }
            return input(FluidRecipeCapability.CAP, (Object[]) inputs);
        }

        public GTRecipeJS inputFluidsRanged(FluidStackJS input, int min, int max) {
            return inputFluidsRanged(input, UniformInt.of(min, max));
        }

        public GTRecipeJS inputFluidsRanged(FluidStackJS input, IntProvider range) {
            validateFluids("ranged input", input);
            FluidStack stack = new FluidStack(input.getFluid(), (int) input.getAmount(), input.getNbt());
            return input(FluidRecipeCapability.CAP,
                    IntProviderFluidIngredient.of(FluidIngredient.of(stack), range));
        }

        public GTRecipeJS outputFluids(FluidStackJS... outputs) {
            validateFluids("output", outputs);
            return output(FluidRecipeCapability.CAP, (Object[]) outputs);
        }

        public GTRecipeJS outputFluidsRanged(FluidStackJS output, int min, int max) {
            return outputFluidsRanged(output, UniformInt.of(min, max));
        }

        public GTRecipeJS outputFluidsRanged(FluidStackJS output, IntProvider range) {
            validateFluids("ranged output", output);
            FluidStack stack = new FluidStack(output.getFluid(), (int) output.getAmount(), output.getNbt());
            return output(FluidRecipeCapability.CAP,
                    IntProviderFluidIngredient.of(FluidIngredient.of(stack), range));
        }

        //////////////////////////////////////
        // ********** VALIDATION ***********//
        //////////////////////////////////////

        private void validateItems(@NotNull String type, InputItem... items) {
            for (var stack : items) {
                if (stack == null || stack.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s item (recipe ID: %s)", type, id));
                }
            }
        }

        private void validateItems(@NotNull String type, ItemStack... stacks) {
            for (var stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s item (recipe ID: %s)", type, id));
                }
            }
        }

        private void validateItems(@NotNull String type, Ingredient... ingredients) {
            for (var ingredient : ingredients) {
                if (ingredient == null || ingredient.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s item (recipe ID: %s)", type, id));
                }
            }
        }

        private void validateItems(@NotNull String type, OutputItem... items) {
            for (var item : items) {
                if (item == null || item.item == null || item.item.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s item (recipe ID: %s)", type, id));
                }
            }
        }

        private void validateItems(@NotNull String type, TagPrefix... items) {
            for (var item : items) {
                if (item == null || item.isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s item (recipe ID: %s)", type, id));
                }
            }
        }

        private void validateFluids(@NotNull String type, GTRecipeComponents.FluidIngredientJS... fluids) {
            for (var fluid : fluids) {
                if (fluid == null || fluid.ingredient() == null || fluid.ingredient().getStacks() == null) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s fluid (recipe ID: %s)", type, id));
                }

                for (var stack : fluid.ingredient().getStacks()) {
                    if (stack == null || stack.isEmpty()) {
                        throw new RecipeExceptionJS(
                                String.format("Invalid or empty %s fluid (recipe ID: %s)", type, id));
                    }
                }
            }
        }

        private void validateFluids(@NotNull String type, FluidStackJS... stacks) {
            for (var stack : stacks) {
                if (stack == null || stack.getFluidStack() == null || stack.getFluidStack().isEmpty()) {
                    throw new RecipeExceptionJS(String.format("Invalid or empty %s fluid (recipe ID: %s)", type, id));
                }
            }
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
            return biome(ResourceKey.create(Registries.BIOME, biome), reverse);
        }

        public GTRecipeJS biome(ResourceLocation biome) {
            return biome(biome, false);
        }

        public GTRecipeJS biome(ResourceKey<Biome> biome, boolean reverse) {
            return addCondition(new BiomeCondition(biome).setReverse(reverse));
        }

        public GTRecipeJS biome(ResourceKey<Biome> biome) {
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

        public GTRecipeJS environmentalHazard(MedicalCondition condition, boolean reverse) {
            return addCondition(new EnvironmentalHazardCondition(condition).setReverse(reverse));
        }

        public GTRecipeJS environmentalHazard(MedicalCondition condition) {
            return environmentalHazard(condition, false);
        }

        public GTRecipeJS adjacentFluids(Fluid... fluids) {
            return adjacentFluids(false, fluids);
        }

        public GTRecipeJS adjacentFluids(boolean isReverse, Fluid... fluids) {
            return addCondition(AdjacentFluidCondition.fromFluids(fluids).setReverse(isReverse));
        }

        public GTRecipeJS adjacentFluid(Fluid... fluids) {
            return adjacentFluid(false, fluids);
        }

        public GTRecipeJS adjacentFluid(boolean isReverse, Fluid... fluids) {
            return addCondition(AdjacentFluidCondition.fromFluids(fluids).setReverse(isReverse));
        }

        public GTRecipeJS adjacentFluid(ResourceLocation... tagNames) {
            return adjacentFluid(false, tagNames);
        }

        public GTRecipeJS adjacentFluid(boolean isReverse, ResourceLocation... tagNames) {
            List<TagKey<Fluid>> tags = Arrays.stream(tagNames)
                    .map(id -> TagKey.create(Registries.FLUID, id))
                    .toList();
            return addCondition(AdjacentFluidCondition.fromTags(tags).setReverse(isReverse));
        }

        public GTRecipeJS adjacentFluidTag(ResourceLocation... tagNames) {
            return adjacentFluidTag(false, tagNames);
        }

        public GTRecipeJS adjacentFluidTag(boolean isReverse, ResourceLocation... tagNames) {
            List<TagKey<Fluid>> tags = Arrays.stream(tagNames)
                    .map(id -> TagKey.create(Registries.FLUID, id))
                    .toList();
            return addCondition(AdjacentFluidCondition.fromTags(tags).setReverse(isReverse));
        }

        public GTRecipeJS adjacentBlocks(Block... blocks) {
            return adjacentBlocks(false, blocks);
        }

        public GTRecipeJS adjacentBlocks(boolean isReverse, Block... blocks) {
            return addCondition(AdjacentBlockCondition.fromBlocks(blocks).setReverse(isReverse));
        }

        public GTRecipeJS adjacentBlock(Block... blocks) {
            return adjacentBlock(false, blocks);
        }

        public GTRecipeJS adjacentBlock(boolean isReverse, Block... blocks) {
            return addCondition(AdjacentBlockCondition.fromBlocks(blocks).setReverse(isReverse));
        }

        public GTRecipeJS adjacentBlockTag(ResourceLocation... tagNames) {
            return adjacentBlockTag(false, tagNames);
        }

        public GTRecipeJS adjacentBlockTag(boolean isReverse, ResourceLocation... tagNames) {
            List<TagKey<Block>> tags = Arrays.stream(tagNames)
                    .map(id -> TagKey.create(Registries.BLOCK, id))
                    .toList();
            return addCondition(AdjacentBlockCondition.fromTags(tags).setReverse(isReverse));
        }

        public GTRecipeJS adjacentBlock(ResourceLocation... tagNames) {
            return adjacentBlock(false, tagNames);
        }

        public GTRecipeJS adjacentBlock(boolean isReverse, ResourceLocation... tagNames) {
            List<TagKey<Block>> tags = Arrays.stream(tagNames)
                    .map(id -> TagKey.create(Registries.BLOCK, id))
                    .toList();
            return addCondition(AdjacentBlockCondition.fromTags(tags).setReverse(isReverse));
        }

        public GTRecipeJS daytime(boolean isNight) {
            return addCondition(new DaytimeCondition().setReverse(isNight));
        }

        public GTRecipeJS daytime() {
            return daytime(false);
        }

        public GTRecipeJS heraclesQuest(String questId, boolean isReverse) {
            if (!GTCEu.Mods.isHeraclesLoaded()) {
                throw new RecipeExceptionJS("Heracles not loaded!");
            }
            if (questId.isEmpty()) {
                throw new RecipeExceptionJS(String.format("Quest ID cannot be empty for recipe %s", this.id));
            }
            return addCondition(new HeraclesQuestCondition(isReverse, questId));
        }

        public GTRecipeJS heraclesQuest(String questId) {
            return heraclesQuest(questId, false);
        }

        public GTRecipeJS gameStage(String stageName) {
            return gameStage(stageName, false);
        }

        public GTRecipeJS gameStage(String stageName, boolean isReverse) {
            if (!GTCEu.Mods.isGameStagesLoaded()) {
                throw new RecipeExceptionJS("GameStages is not loaded, ignoring recipe condition");
            }
            return addCondition(new GameStageCondition(isReverse, stageName));
        }

        public GTRecipeJS ftbQuest(String questId, boolean isReverse) {
            if (!GTCEu.Mods.isFTBQuestsLoaded()) {
                throw new RecipeExceptionJS("FTBQuests is not loaded!");
            }
            if (questId.isEmpty()) {
                throw new RecipeExceptionJS(String.format("Quest ID cannot be empty for recipe %s", this.id));
            }
            long qID = QuestObjectBase.parseCodeString(questId);
            if (qID == 0L) {
                throw new RecipeExceptionJS(String.format("Quest %s not found for recipe %s", questId, this.id));
            }
            return addCondition(new FTBQuestCondition(isReverse, qID));
        }

        public GTRecipeJS ftbQuest(String questId) {
            return ftbQuest(questId, false);
        }

        private boolean applyResearchProperty(ResearchData.ResearchEntry researchEntry) {
            if (!ConfigHolder.INSTANCE.machines.enableResearch) return false;
            if (researchEntry == null) {
                throw new RecipeExceptionJS("Assembly Line Research Entry cannot be empty.",
                        new IllegalArgumentException());
            }

            if (!generatingRecipes) {
                throw new RecipeExceptionJS("Cannot generate recipes when using researchWithoutRecipe()",
                        new IllegalStateException());
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
                    .build(this.id);
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
                    .build(this.id);
            if (applyResearchProperty(new ResearchData.ResearchEntry(entry.researchId(), entry.dataStack()))) {
                this.researchRecipeEntries.add(entry);
            }
            return this;
        }

        public GTRecipeJS addMaterialInfo(boolean item) {
            this.itemMaterialInfo = item;
            return this;
        }

        public GTRecipeJS addMaterialInfo(boolean item, boolean fluid) {
            this.itemMaterialInfo = item;
            this.fluidMaterialInfo = fluid;
            return this;
        }

        public GTRecipeJS removePreviousMaterialInfo() {
            this.removeMaterialInfo = true;
            return this;
        }

        private void gatherMaterialInfoFromStacks(ItemStack itemStack) {
            // test simple item that have pure singular material stack
            var matStack = ChemicalHelper.getMaterialStack(itemStack);
            // test item that has multiple material stacks
            var matInfo = ChemicalHelper.getMaterialInfo(itemStack);
            if (chance == maxChance && chance != 0) {
                if (!matStack.isEmpty()) {
                    itemMaterialStacks.add(matStack.multiply(itemStack.getCount()));
                }
                if (matInfo != null) {
                    for (var ms : matInfo.getMaterials()) {
                        itemMaterialStacks.add(ms.multiply(itemStack.getCount()));
                    }
                } else {
                    tempItemStacks.add(itemStack);
                }
            }
        }

        /*
         * KubeJS overrides
         */

        @Override
        public ResourceLocation getOrCreateId() {
            boolean wasNull = id == null;

            super.getOrCreateId();
            if (wasNull) {
                idWithoutType = id.withPath(p -> StringUtils.substringAfter(p, '/'));
            }
            return id;
        }

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
            if (value.ingredient instanceof SizedIngredient sized) return sized.toJson();
            else return SizedIngredient.create(value.ingredient, value.count).toJson();
        }

        @Override
        public OutputItem readOutputItem(Object from) {
            if (from instanceof ExtendedOutputItem outputItem) {
                return outputItem;
            } else if (from instanceof OutputItem outputItem) {
                return outputItem;
            } else if (from instanceof Ingredient ingredient) {
                return ExtendedOutputItem.of(ingredient, 1);
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
            if (value.rolls != null) {
                return IntProviderIngredient.of(value.item, value.rolls).toJson();
            } else if (value instanceof ExtendedOutputItem extended) {
                if (extended.ingredient.getInner() instanceof IntProviderIngredient intProvider) {
                    return intProvider.toJson();
                }
                return extended.ingredient.toJson();
            }
            return SizedIngredient.create(value.item).toJson();
        }

        @Override
        public InputFluid readInputFluid(Object from) {
            return GTRecipeComponents.FluidIngredientJS.of(from);
        }

        @Override
        public JsonElement writeInputFluid(InputFluid value) {
            if (value instanceof GTRecipeComponents.FluidIngredientJS ing) {
                return ing.ingredient().toJson();
            }

            var fluid = ((FluidStackJS) value).getFluidStack();
            return FluidIngredient.of(fluid.getFluid(), (int) fluid.getAmount(), fluid.getTag()).toJson();
        }

        @Override
        public OutputFluid readOutputFluid(Object from) {
            return GTRecipeComponents.FluidIngredientJS.of(from);
        }

        @Override
        public JsonElement writeOutputFluid(OutputFluid value) {
            if (value instanceof GTRecipeComponents.FluidIngredientJS ing) {
                return ing.ingredient().toJson();
            } else if (value instanceof FluidIngredient ingredient) {
                return ingredient.toJson();
            }

            var fluid = ((FluidStackJS) value).getFluidStack();
            return FluidIngredient.of(fluid.getFluid(), (int) fluid.getAmount(), fluid.getTag()).toJson();
        }
    }

    RecipeKey<ResourceLocation> ID = GTRecipeComponents.RESOURCE_LOCATION.key("id");
    RecipeKey<Long> DURATION = TimeComponent.TICKS.key("duration").optional(100L);
    RecipeKey<CompoundTag> DATA = GTRecipeComponents.TAG.key("data").optional((CompoundTag) null);
    RecipeKey<RecipeCondition[]> CONDITIONS = GTRecipeComponents.RECIPE_CONDITION.asArray().key("recipeConditions")
            .optional(new RecipeCondition[0]);
    RecipeKey<ResourceLocation> CATEGORY = GTRecipeComponents.RESOURCE_LOCATION.key("category").defaultOptional();

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
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS, CATEGORY)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(RecipeConstructor.Factory.DEFAULT)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS)
            .uniqueId(GTRecipeSchema::makeDefaultRecipeId);

    static @Nullable String makeDefaultRecipeId(RecipeJS recipe) {
        String outputId = resolveRecipeIdFromOutputs(recipe, recipe.getValue(ALL_OUTPUTS));
        if (outputId == null) {
            outputId = resolveRecipeIdFromOutputs(recipe, recipe.getValue(ALL_TICK_OUTPUTS));
        }
        if (outputId == null) {
            return null;
        }
        return RecipeSchema.normalizeId(outputId).replace('/', '_');
    }

    private static @Nullable String resolveRecipeIdFromOutputs(RecipeJS recipe, @Nullable CapabilityMap map) {
        if (map == null || map.isEmpty()) return null;

        String item = parseItemOutputId(recipe, map);
        if (item != null) return item;
        else return parseFluidOutputId(recipe, map);
    }

    private static @Nullable String parseItemOutputId(RecipeJS recipe, CapabilityMap map) {
        var outputs = map.get(ItemRecipeCapability.CAP);
        if (outputs != null && outputs.length > 0) {
            var output = GTRecipeComponents.ITEM_OUT.baseComponent().read(recipe, outputs[0].content);
            var id = output.item.getItemHolder().unwrapKey();
            if (id.isPresent()) {
                return id.get().location().getPath();
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private static @Nullable String parseFluidOutputId(RecipeJS recipe, CapabilityMap map) {
        var outputs = map.get(FluidRecipeCapability.CAP);
        if (outputs != null && outputs.length > 0) {
            var output = GTRecipeComponents.FLUID_OUT.baseComponent().read(recipe, outputs[0].content);
            var fluids = output.ingredient().getStacks();
            if (fluids.length == 0) return null;

            var id = fluids[0].getFluid().builtInRegistryHolder().unwrapKey();
            if (id.isPresent()) {
                return id.get().location().getPath();
            }
        }
        return null;
    }
}
