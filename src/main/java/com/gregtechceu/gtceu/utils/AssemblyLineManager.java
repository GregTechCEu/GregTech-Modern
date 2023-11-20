package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class AssemblyLineManager {

    public static final String RESEARCH_NBT_TAG = "assemblylineResearch";
    public static final String RESEARCH_ID_NBT_TAG = "researchId";

    @Nonnull
    public static ItemStack getDefaultScannerItem() {
        return GTItems.TOOL_DATA_STICK.asStack();
    }

    @Nonnull
    public static ItemStack getDefaultResearchStationItem(int cwut) {
        if (cwut > 32) {
            return GTItems.TOOL_DATA_MODULE.asStack();
        }
        return GTItems.TOOL_DATA_ORB.asStack();
    }

    private AssemblyLineManager() {}

    @ApiStatus.Internal
    public static void registerScannerLogic() {
        GTRecipeType.registerCustomScannerLogic(new DataStickCopyScannerLogic());
    }

    /**
     * @param stackCompound the compound contained on the ItemStack to write to
     * @param researchId    the research id
     */
    public static void writeResearchToNBT(@Nonnull CompoundTag stackCompound, @Nonnull String researchId) {
        CompoundTag compound = new CompoundTag();
        compound.putString(RESEARCH_ID_NBT_TAG, researchId);
        stackCompound.put(RESEARCH_NBT_TAG, compound);
    }

    /**
     * @param stack the ItemStack to read from
     * @return the research id
     */
    @Nullable
    public static String readResearchId(@Nonnull ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (!hasResearchTag(compound)) return null;

        CompoundTag researchCompound = compound.getCompound(RESEARCH_NBT_TAG);
        String researchId = researchCompound.getString(RESEARCH_ID_NBT_TAG);
        return researchId.isEmpty() ? null : researchId;
    }

    /**
     * @param stack      the stack to check
     * @param isDataBank if the caller is a Data Bank. Pass "true" here if your use-case does not matter for this check.
     * @return if the stack is a data item
     */
    public static boolean isStackDataItem(@Nonnull ItemStack stack, boolean isDataBank) {
        if (stack.getItem() instanceof ComponentItem metaItem) {
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
    public static boolean hasResearchTag(@Nonnull ItemStack stack) {
        return hasResearchTag(stack.getTag());
    }

    /**
     * @param compound the compound to check
     * @return if the tag has  the research CompoundTag
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
    public static void createDefaultResearchRecipe(@Nonnull GTRecipeBuilder builder, Consumer<FinishedRecipe> provider) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return;

        for (GTRecipeBuilder.ResearchRecipeEntry entry : builder.recipeEntries()) {
            createDefaultResearchRecipe(entry.researchId(), entry.researchStack(), entry.dataStack(), entry.duration(), entry.EUt(), entry.CWUt(), provider);
        }
    }

    public static void createDefaultResearchRecipe(@Nonnull String researchId, @Nonnull ItemStack researchItem, @Nonnull ItemStack dataItem, int duration, int EUt, int CWUt, Consumer<FinishedRecipe> provider) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return;

        CompoundTag compound = dataItem.getOrCreateTag();
        writeResearchToNBT(compound, researchId);

        if (CWUt > 0) {
            GTRecipeTypes.RESEARCH_STATION_RECIPES.recipeBuilder(researchId)
                    .inputItems(dataItem.getItem())
                    .inputItems(researchItem)
                    .outputItems(dataItem)
                    .EUt(EUt)
                    .CWUt(CWUt)
                    .totalCWU(duration)
                    .save(provider);
        } else {
            GTRecipeTypes.SCANNER_RECIPES.recipeBuilder(researchId)
                    .inputItems(dataItem.getItem())
                    .inputItems(researchItem)
                    .outputItems(dataItem)
                    .duration(duration)
                    .EUt(EUt)
                    .researchScan(true)
                    .save(provider);
        }
    }

    public static class DataStickCopyScannerLogic implements GTRecipeType.ICustomScannerLogic {

        private static final int EUT = 2;
        private static final int DURATION = 100;

        @Override
        public GTRecipe createCustomRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
            if (inputs.size() > 1) {
                // try the data recipe both ways, prioritizing overwriting the first
                GTRecipe recipe = createDataRecipe(inputs.get(0), inputs.get(1));
                if (recipe != null) return recipe;

                createDataRecipe(inputs.get(1), inputs.get(0));
            }
            return null;
        }

        private GTRecipe createDataRecipe(@Nonnull ItemStack first, @Nonnull ItemStack second) {
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
                    .duration(DURATION).EUt(EUT).buildRawRecipe();
        }

        @Nullable
        @Override
        public List<GTRecipe> getRepresentativeRecipes() {
            ItemStack copiedStick = GTItems.TOOL_DATA_STICK.asStack();
            copiedStick.setHoverName(Component.translatable("gtceu.scanner.copy_stick_from"));
            ItemStack emptyStick = GTItems.TOOL_DATA_STICK.asStack();
            emptyStick.setHoverName(Component.translatable("gtceu.scanner.copy_stick_empty"));
            ItemStack resultStick = GTItems.TOOL_DATA_STICK.asStack();
            resultStick.setHoverName(Component.translatable("gtceu.scanner.copy_stick_to"));
            return Collections.singletonList(
                    GTRecipeTypes.SCANNER_RECIPES.recipeBuilder("copy_" + GTStringUtils.itemStackToString(copiedStick))
                            .inputItems(emptyStick)
                            .notConsumable(copiedStick)
                            .outputItems(resultStick)
                            .duration(DURATION).EUt(EUT)
                            .buildRawRecipe());
        }
    }
}