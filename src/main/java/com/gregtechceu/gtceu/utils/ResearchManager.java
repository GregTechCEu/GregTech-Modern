package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;
import com.gregtechceu.gtceu.common.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

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
     * @param stack      the stack to check
     * @param isDataBank if the caller is a Data Bank. Pass "true" here if your use-case does not matter for this check.
     * @return if the stack is a data item
     */
    public static boolean isStackDataItem(@NotNull ItemStack stack, boolean isDataBank) {
        @Nullable
        Boolean dataItem = stack.get(GTDataComponents.DATA_ITEM);
        return dataItem != null && !dataItem || isDataBank;
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

        dataItem.set(GTDataComponents.RESEARCH_ITEM, new ResearchItem(researchId, recipeType));

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

    public record ResearchItem(String researchId, GTRecipeType recipeType) implements TooltipProvider {

        // spotless:off
        public static final Codec<ResearchItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("research_id").forGetter(ResearchItem::researchId),
                GTRecipeSerializer.GT_RECIPE_TYPE_CODEC.fieldOf("research_type").forGetter(ResearchItem::recipeType)
        ).apply(instance, ResearchItem::new));
        public static final StreamCodec<ByteBuf, ResearchItem> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ResearchItem::researchId,
                GTRecipeSerializer.GT_RECIPE_TYPE_STREAM_CODEC, ResearchItem::recipeType,
                ResearchItem::new);

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
            Collection<GTRecipe> recipes = recipeType().getDataStickEntry(researchId());
            if (recipes != null && !recipes.isEmpty()) {
                tooltipAdder.accept(Component.translatable("behavior.data_item.assemblyline.title"));
                Collection<ItemStack> added = new ObjectOpenHashSet<>();
                outer:
                for (GTRecipe recipe : recipes) {
                    ItemStack output = ItemRecipeCapability.CAP
                            .of(recipe.getOutputContents(ItemRecipeCapability.CAP).getFirst().content).getItems()[0];
                    for (var item : added) {
                        if (output.is(item.getItem())) continue outer;
                    }
                    if (added.add(output)) {
                        tooltipAdder.accept(
                                Component.translatable("behavior.data_item.assemblyline.data",
                                        output.getDisplayName()));
                    }
                }
            }
        }
        // spotless:on
    }

    public static class DataStickCopyScannerLogic implements GTRecipeType.ICustomRecipeLogic {

        private static final int EUT = 2;
        private static final int DURATION = 100;

        @Override
        public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
            var itemInputs = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).stream()
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

        private @Nullable GTRecipe createDataRecipe(@NotNull ItemStack first, @NotNull ItemStack second) {
            ResearchItem researchItem = second.get(GTDataComponents.RESEARCH_ITEM);
            if (researchItem == null) return null;

            // Both must be data items
            if (!isStackDataItem(first, true)) return null;
            if (!isStackDataItem(second, true)) return null;

            ItemStack output = first.copy();
            output.set(GTDataComponents.RESEARCH_ITEM, researchItem);
            return GTRecipeTypes.SCANNER_RECIPES.recipeBuilder(GTStringUtils.itemStackToString(output))
                    .inputItems(first)
                    .notConsumable(second)
                    .outputItems(output)
                    .duration(DURATION).EUt(EUT)
                    .build();
        }

        @Override
        public void buildRepresentativeRecipes() {
            ItemStack copiedStick = GTItems.TOOL_DATA_STICK.asStack();
            copiedStick.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.scanner.copy_stick_from"));
            ItemStack emptyStick = GTItems.TOOL_DATA_STICK.asStack();
            emptyStick.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.scanner.copy_stick_empty"));
            ItemStack resultStick = GTItems.TOOL_DATA_STICK.asStack();
            resultStick.set(DataComponents.CUSTOM_NAME, Component.translatable("gtceu.scanner.copy_stick_to"));

            GTRecipe recipe = GTRecipeTypes.SCANNER_RECIPES
                    .recipeBuilder("copy_" + GTStringUtils.itemStackToString(copiedStick))
                    .inputItems(emptyStick)
                    .notConsumable(copiedStick)
                    .outputItems(resultStick)
                    .duration(DURATION).EUt(EUT)
                    .build();
            // for EMI to detect it's a synthetic recipe (not ever in JSON)
            recipe.setId(recipe.getId().withPrefix("/"));
            GTRecipeTypes.SCANNER_RECIPES.addToMainCategory(recipe);
        }
    }
}
