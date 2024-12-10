package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class ResearchManager {

    public static final String RESEARCH_NBT_TAG = "assembly_line_research";
    public static final String RESEARCH_ID_NBT_TAG = "research_id";
    public static final String RESEARCH_TYPE_NBT_TAG = "research_type";

    @NotNull
    public static ItemStack getDefaultScannerItem() {
        return GTItems.TOOL_DATA_STICK.asStack();
    }

    @NotNull
    public static ItemStack getDefaultResearchStationItem(int cwut) {
        if (cwut > 32) {
            return GTItems.TOOL_DATA_MODULE.asStack();
        }
        return GTItems.TOOL_DATA_ORB.asStack();
    }

    private ResearchManager() {}

    /**
     * @param stackCompound the compound contained on the ItemStack to write to
     * @param researchId    the research id
     */
    public static void writeResearchToNBT(@NotNull CompoundTag stackCompound, @NotNull String researchId,
                                          GTRecipeType recipeType) {
        CompoundTag compound = new CompoundTag();
        compound.putString(RESEARCH_ID_NBT_TAG, researchId);
        compound.putString(RESEARCH_TYPE_NBT_TAG, recipeType.registryName.toString());
        stackCompound.put(RESEARCH_NBT_TAG, compound);
    }

    /**
     * @param stack the ItemStack to read from
     * @return the research id
     */
    @Nullable
    public static Pair<GTRecipeType, String> readResearchId(@NotNull ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (!hasResearchTag(compound)) return null;

        CompoundTag researchCompound = compound.getCompound(RESEARCH_NBT_TAG);
        String researchId = researchCompound.getString(RESEARCH_ID_NBT_TAG);
        ResourceLocation researchRecipeType = ResourceLocation
                .tryParse(researchCompound.getString(RESEARCH_TYPE_NBT_TAG));
        return researchId.isEmpty() || researchRecipeType == null ? null :
                Pair.of(GTRegistries.RECIPE_TYPES.get(researchRecipeType), researchId);
    }

    /**
     * @param stack      the stack to check
     * @param isDataBank if the caller is a Data Bank. Pass "true" here if your use-case does not matter for this check.
     * @return if the stack is a data item
     */
    public static boolean isStackDataItem(@NotNull ItemStack stack, boolean isDataBank) {
        if (stack.getItem() instanceof IComponentItem metaItem) {
            for (IItemComponent behaviour : metaItem.getComponents()) {
                if (behaviour instanceof IDataItem dataItem) {
                    return !dataItem.requireDataBank() || isDataBank;
                }
            }
        }
        return false;
    }

    /**
     * @param stack the stack to check
     * @return if the stack has the research CompoundTag
     */
    public static boolean hasResearchTag(@NotNull ItemStack stack) {
        return hasResearchTag(stack.getTag());
    }

    /**
     * @param compound the compound to check
     * @return if the tag has the research CompoundTag
     */
    private static boolean hasResearchTag(@Nullable CompoundTag compound) {
        if (compound == null || compound.isEmpty()) return false;
        return compound.contains(RESEARCH_NBT_TAG, Tag.TAG_COMPOUND);
    }

    /**
     * Create the default research recipe
     *
     * @param builder the builder to retrieve recipe info from
     */
    public static void createDefaultResearchRecipe(@NotNull GTRecipeBuilder builder,
                                                   Consumer<FinishedRecipe> provider) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return;

        for (GTRecipeBuilder.ResearchRecipeEntry entry : builder.researchRecipeEntries()) {
            createDefaultResearchRecipe(builder.recipeType, entry.researchId(), entry.researchStack(),
                    entry.dataStack(), entry.duration(), entry.EUt(), entry.CWUt(), provider);
        }
    }

    public static void createDefaultResearchRecipe(@NotNull GTRecipeType recipeType, @NotNull String researchId,
                                                   @NotNull ItemStack researchItem, @NotNull ItemStack dataItem,
                                                   int duration, int EUt, int CWUt, Consumer<FinishedRecipe> provider) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return;

        CompoundTag compound = dataItem.getOrCreateTag();
        writeResearchToNBT(compound, researchId, recipeType);

        if (CWUt > 0) {
            GTRecipeTypes.RESEARCH_STATION_RECIPES.recipeBuilder(FormattingUtil.toLowerCaseUnderscore(researchId))
                    .inputItems(dataItem.getItem())
                    .inputItems(researchItem)
                    .outputItems(dataItem)
                    .EUt(EUt)
                    .CWUt(CWUt)
                    .totalCWU(duration)
                    .save(provider);
        } else {
            GTRecipeTypes.SCANNER_RECIPES.recipeBuilder(FormattingUtil.toLowerCaseUnderscore(researchId))
                    .inputItems(dataItem.getItem())
                    .inputItems(researchItem)
                    .outputItems(dataItem)
                    .duration(duration)
                    .EUt(EUt)
                    .researchScan(true)
                    .save(provider);
        }
    }

    public static class DataStickCopyScannerLogic implements GTRecipeType.ICustomRecipeLogic {

        private static final int EUT = 2;
        private static final int DURATION = 100;

        @Override
        public GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
            var itemInputs = holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).stream()
                    .filter(IItemHandlerModifiable.class::isInstance)
                    .map(IItemHandlerModifiable.class::cast)
                    .toArray(IItemHandlerModifiable[]::new);
            var inputs = new CombinedInvWrapper(itemInputs);
            if (inputs.getSlots() > 1) {
                // try the data recipe both ways, prioritizing overwriting the first
                GTRecipe recipe = createDataRecipe(inputs.getStackInSlot(0), inputs.getStackInSlot(1));
                if (recipe != null) return recipe;

                return createDataRecipe(inputs.getStackInSlot(1), inputs.getStackInSlot(0));
            }
            return null;
        }

        private GTRecipe createDataRecipe(@NotNull ItemStack first, @NotNull ItemStack second) {
            CompoundTag compound = second.getTag();
            if (compound == null) return null;

            // Both must be data items
            if (!isStackDataItem(first, true)) return null;
            if (!isStackDataItem(second, true)) return null;

            ItemStack output = first.copy();
            output.setTag(compound.copy());
            return GTRecipeTypes.SCANNER_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                    .inputItems(first)
                    .notConsumable(second)
                    .outputItems(output)
                    .duration(DURATION).EUt(EUT)
                    .buildRawRecipe();
        }

        @Override
        public void buildRepresentativeRecipes() {
            ItemStack copiedStick = GTItems.TOOL_DATA_STICK.asStack();
            copiedStick.setHoverName(Component.translatable("gtceu.scanner.copy_stick_from"));
            ItemStack emptyStick = GTItems.TOOL_DATA_STICK.asStack();
            emptyStick.setHoverName(Component.translatable("gtceu.scanner.copy_stick_empty"));
            ItemStack resultStick = GTItems.TOOL_DATA_STICK.asStack();
            resultStick.setHoverName(Component.translatable("gtceu.scanner.copy_stick_to"));
            var recipe = GTRecipeTypes.SCANNER_RECIPES
                    .recipeBuilder("copy_" + GTStringUtils.itemStackToString(copiedStick))
                    .inputItems(emptyStick)
                    .notConsumable(copiedStick)
                    .outputItems(resultStick)
                    .duration(DURATION).EUt(EUT)
                    .buildRawRecipe();
            GTRecipeTypes.SCANNER_RECIPES.addToMainCategory(recipe);
        }
    }
}
