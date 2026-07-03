package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ingredient.IChancedIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.fluid.RangedFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.RangedItemIngredient;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.FluidTextHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeOutputProvider extends CapabilityBlockProvider<RecipeLogic> {

    public RecipeOutputProvider() {
        super(GTCEu.id("recipe_output_info"));
    }

    @Override
    protected @Nullable RecipeLogic getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getRecipeLogic(level, pos, side);
    }

    @Override
    protected void write(CompoundTag data, RecipeLogic recipeLogic) {
        if (recipeLogic.isWorking()) {
            data.putBoolean("Working", true);
            var recipe = recipeLogic.getLastRecipe();
            if (recipe != null) {
                int recipeTier = recipe.tier;
                int chanceTier = recipeTier + recipe.ocLevel;
                var function = recipe.getType().getChanceFunction();
                var itemContents = recipe.getOutputContents(ItemRecipeCapability.CAP);
                var fluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);
                int runs = recipe.getTotalRuns();

                ListTag itemTags = new ListTag();
                for (var item : itemContents) {
                    ItemStack stack;
                    CompoundTag itemTag = new CompoundTag();
                    RangedItemIngredient ranged;
                    if (item instanceof RangedItemIngredient ranged1) {
                        ranged = ranged1;
                    } else {
                        ranged = item.isChanced() && item.getInner() instanceof RangedItemIngredient ranged1 ? ranged1 :
                                null;
                    }
                    if (ranged != null) {
                        stack = copyFirst(ranged.getInner().getItems());
                        if (stack.isEmpty()) continue;
                        itemTag.putInt("MinCount", ranged.getMinCount());
                        itemTag.putInt("MaxCount", ranged.getCount());
                    } else {
                        stack = copyFirst(item.getItems());
                        if (stack.isEmpty()) continue;
                    }
                    GTUtil.saveItemStack(stack, itemTag);
                    if (item.isChanced()) {
                        int count = Math.max(1, (int) Math.round((double) stack.getCount() * runs *
                                function.getBoostedChance(item.getChance(), recipeTier, chanceTier) /
                                IChancedIngredient.MAX_CHANCE));
                        itemTag.putInt("Count", count);
                    }
                    itemTags.add(itemTag);
                }

                if (!itemTags.isEmpty()) {
                    data.put("OutputItems", itemTags);
                }

                ListTag fluidTags = new ListTag();
                for (var fluid : fluidContents) {
                    FluidStack stack;
                    CompoundTag fluidTag = new CompoundTag();
                    RangedFluidIngredient ranged;
                    if (fluid instanceof RangedFluidIngredient ranged1) {
                        ranged = ranged1;
                    } else {
                        ranged = fluid.isChanced() && fluid.getInner() instanceof RangedFluidIngredient ranged1 ?
                                ranged1 : null;
                    }
                    if (ranged != null) {
                        stack = copyFirst(ranged.getInner().getFluids());
                        if (stack.isEmpty()) continue;
                        fluidTag.putInt("MinAmount", ranged.getMinAmount());
                        fluidTag.putInt("MaxAmount", ranged.getAmount());
                    } else {
                        stack = copyFirst(fluid.getFluids());
                        if (stack.isEmpty()) continue;
                    }
                    stack.writeToNBT(fluidTag);
                    if (fluid.isChanced()) {
                        int amount = Math.max(1, (int) Math.round((double) stack.getAmount() * runs *
                                function.getBoostedChance(fluid.getChance(), recipeTier, chanceTier) /
                                IChancedIngredient.MAX_CHANCE));
                        fluidTag.putInt("Amount", amount);
                    }
                    fluidTags.add(fluidTag);
                }

                if (!fluidTags.isEmpty()) {
                    data.put("OutputFluids", fluidTags);
                }
            }
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (capData.getBoolean("Working")) {
            List<ItemOutput> outputItems = new ArrayList<>();
            if (capData.contains("OutputItems", Tag.TAG_LIST)) {
                ListTag itemTags = capData.getList("OutputItems", Tag.TAG_COMPOUND);
                for (Tag tag : itemTags) {
                    if (tag instanceof CompoundTag itemTag) {
                        var stack = GTUtil.loadItemStack(itemTag);
                        if (!stack.isEmpty()) outputItems.add(new ItemOutput(stack, itemTag));
                    }
                }
            }
            List<FluidOutput> outputFluids = new ArrayList<>();
            if (capData.contains("OutputFluids", Tag.TAG_LIST)) {
                ListTag fluidTags = capData.getList("OutputFluids", Tag.TAG_COMPOUND);
                for (Tag tag : fluidTags) {
                    if (tag instanceof CompoundTag fluidTag) {
                        var stack = FluidStack.loadFluidStackFromNBT(fluidTag);
                        if (!stack.isEmpty()) outputFluids.add(new FluidOutput(stack, fluidTag));
                    }
                }
            }
            if (!outputItems.isEmpty() || !outputFluids.isEmpty()) {
                tooltip.add(Component.translatable("gtceu.top.recipe_output"));
            }
            addItemTooltips(tooltip, outputItems);
            addFluidTooltips(tooltip, outputFluids);
        }
    }

    private void addItemTooltips(ITooltip iTooltip, List<ItemOutput> outputItems) {
        IElementHelper helper = iTooltip.getElementHelper();
        for (ItemOutput itemOutput : outputItems) {
            ItemStack item = itemOutput.stack().copy();
            if (!item.isEmpty()) {
                MutableComponent text = CommonComponents.space();
                CompoundTag tag = itemOutput.tag();
                if (tag.contains("MinCount", Tag.TAG_INT) && tag.contains("MaxCount", Tag.TAG_INT)) {
                    text.append(Component.translatable("gtceu.gui.content.range",
                            tag.getInt("MinCount"), tag.getInt("MaxCount")));
                } else {
                    text.append(String.valueOf(item.getCount()));
                }
                item.setCount(1);
                text.append(Component.translatable("gtceu.gui.content.times_item",
                        item.getDisplayName().copy().withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.WHITE));

                iTooltip.add(helper.smallItem(item));
                iTooltip.append(text);
            }
        }
    }

    private void addFluidTooltips(ITooltip iTooltip, List<FluidOutput> outputFluids) {
        for (FluidOutput fluidOutput : outputFluids) {
            FluidStack stack = fluidOutput.stack().copy();
            if (!stack.isEmpty()) {
                MutableComponent text = CommonComponents.space();
                CompoundTag tag = fluidOutput.tag();
                if (tag.contains("MinAmount", Tag.TAG_INT) && tag.contains("MaxAmount", Tag.TAG_INT)) {
                    text.append(Component.translatable("gtceu.gui.content.range",
                            FluidTextHelper.getUnicodeMillibuckets(tag.getInt("MinAmount"), true),
                            FluidTextHelper.getUnicodeMillibuckets(tag.getInt("MaxAmount"), true)));
                } else {
                    text.append(FluidTextHelper.getUnicodeMillibuckets(stack.getAmount(), true));
                }
                text.append(CommonComponents.space())
                        .append(ComponentUtils.wrapInSquareBrackets(stack.getDisplayName())
                                .withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.WHITE);

                iTooltip.add(GTElementHelper
                        .smallFluid(JadeFluidObject.of(stack.getFluid(), stack.getAmount(), stack.getTag())));
                iTooltip.append(text);
            }
        }
    }

    private static ItemStack copyFirst(ItemStack[] stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) return stack.copy();
        }
        return ItemStack.EMPTY;
    }

    private static FluidStack copyFirst(FluidStack[] stacks) {
        for (FluidStack stack : stacks) {
            if (!stack.isEmpty()) return stack.copy();
        }
        return FluidStack.EMPTY;
    }

    private record ItemOutput(ItemStack stack, CompoundTag tag) {}

    private record FluidOutput(FluidStack stack, CompoundTag tag) {}
}
