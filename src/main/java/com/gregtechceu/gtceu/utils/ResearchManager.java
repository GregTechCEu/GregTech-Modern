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
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;

import com.lowdragmc.lowdraglib.misc.ItemTransferList;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class ResearchManager {

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
     * @param stack      the ItemStack to write to
     * @param researchId the research id
     */
    public static void writeResearchToComponent(@NotNull ItemStack stack, @NotNull String researchId,
                                                GTRecipeType recipeType) {
        stack.set(GTDataComponents.RESEARCH_ITEM, new ResearchItem(researchId, recipeType.registryName));
    }

    /**
     * @param stack the ItemStack to read from
     * @return the research id
     */
    @Nullable
    public static Pair<GTRecipeType, String> readResearchId(@NotNull ItemStack stack) {
        if (!stack.has(GTDataComponents.RESEARCH_ITEM)) return null;

        ResearchItem researchItem = stack.get(GTDataComponents.RESEARCH_ITEM);
        ResourceLocation researchRecipeType = researchItem.researchRecipeType;
        return researchItem.researchId.isEmpty() || researchRecipeType == null ? null :
                Pair.of(GTRegistries.RECIPE_TYPES.get(researchRecipeType), researchItem.researchId);
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
        return stack.has(GTDataComponents.RESEARCH_ITEM);
    }

    /**
     * Create the default research recipe
     *
     * @param builder the builder to retrieve recipe info from
     */
    public static void createDefaultResearchRecipe(@NotNull GTRecipeBuilder builder,
                                                   RecipeOutput provider) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return;

        for (GTRecipeBuilder.ResearchRecipeEntry entry : builder.researchRecipeEntries()) {
            createDefaultResearchRecipe(builder.recipeType, entry.researchId(), entry.researchStack(),
                    entry.dataStack(), entry.duration(), entry.EUt(), entry.CWUt(), provider);
        }
    }

    public static void createDefaultResearchRecipe(@NotNull GTRecipeType recipeType, @NotNull String researchId,
                                                   @NotNull ItemStack researchItem, @NotNull ItemStack dataItem,
                                                   int duration, int EUt, int CWUt, RecipeOutput provider) {
        if (!ConfigHolder.INSTANCE.machines.enableResearch) return;

        writeResearchToComponent(dataItem, researchId, recipeType);

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

    public record ResearchItem(String researchId, ResourceLocation researchRecipeType) {

        public static final Codec<ResearchItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("research_id").forGetter(ResearchItem::researchId),
                ResourceLocation.CODEC.fieldOf("research_type").forGetter(ResearchItem::researchRecipeType))
                .apply(instance, ResearchItem::new));
        public static final StreamCodec<ByteBuf, ResearchItem> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ResearchItem::researchId,
                ResourceLocation.STREAM_CODEC, ResearchItem::researchRecipeType,
                ResearchItem::new);
    }

    public static class DataStickCopyScannerLogic implements GTRecipeType.ICustomRecipeLogic {

        private static final int EUT = 2;
        private static final int DURATION = 100;

        @Override
        public GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
            var itemInputs = holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).stream()
                    .filter(IItemHandlerModifiable.class::isInstance).map(IItemHandlerModifiable.class::cast)
                    .toArray(IItemHandlerModifiable[]::new);
            var inputs = new ItemTransferList(itemInputs);
            if (inputs.getSlots() > 1) {
                // try the data recipe both ways, prioritizing overwriting the first
                GTRecipe recipe = createDataRecipe(inputs.getStackInSlot(0), inputs.getStackInSlot(1));
                if (recipe != null) return recipe;

                return createDataRecipe(inputs.getStackInSlot(1), inputs.getStackInSlot(0));
            }
            return null;
        }

        private GTRecipe createDataRecipe(@NotNull ItemStack first, @NotNull ItemStack second) {
            DataComponentPatch components = second.getComponentsPatch();

            // Both must be data items
            if (!isStackDataItem(first, true)) return null;
            if (!isStackDataItem(second, true)) return null;

            ItemStack output = first.copy();
            output.applyComponents(components);
            return GTRecipeTypes.SCANNER_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                    .inputItems(first)
                    .notConsumable(second)
                    .outputItems(output)
                    .duration(DURATION).EUt(EUT).build();
        }

        @Nullable
        @Override
        public List<GTRecipe> getRepresentativeRecipes() {
            ItemStack copiedStick = GTItems.TOOL_DATA_STICK.asStack();
            copiedStick.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.scanner.copy_stick_from"));
            ItemStack emptyStick = GTItems.TOOL_DATA_STICK.asStack();
            emptyStick.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.scanner.copy_stick_empty"));
            ItemStack resultStick = GTItems.TOOL_DATA_STICK.asStack();
            resultStick.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.scanner.copy_stick_to"));
            return Collections.singletonList(
                    GTRecipeTypes.SCANNER_RECIPES
                            .recipeBuilder("copy_" + GTStringUtils.itemStackToString(copiedStick))
                            .inputItems(emptyStick)
                            .notConsumable(copiedStick)
                            .outputItems(resultStick)
                            .duration(DURATION).EUt(EUT)
                            .build());
        }
    }
}
