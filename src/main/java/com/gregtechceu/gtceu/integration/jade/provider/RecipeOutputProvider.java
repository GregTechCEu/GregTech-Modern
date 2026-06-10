package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
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
import net.minecraft.nbt.NbtOps;
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
            data.putBoolean("Working", recipeLogic.isWorking());
            var recipe = recipeLogic.getLastRecipe();
            if (recipe != null) {
                int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
                int chanceTier = recipeTier + recipe.ocLevel;
                var function = recipe.getType().getChanceFunction();
                var itemContents = recipe.getOutputContents(ItemRecipeCapability.CAP);
                var fluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);
                int runs = recipe.getTotalRuns();

                ListTag itemTags = new ListTag();
                for (var item : itemContents) {
                    ItemStack stack;
                    CompoundTag itemTag = new CompoundTag();
                    if (item instanceof RangedItemIngredient ranged) {
                        var stacks = ranged.getInner().getItems();
                        if (stacks.length == 0 || stacks[0].isEmpty()) continue;
                        stack = stacks[0].copyWithCount(1);
                        itemTag.putInt("MinCount", ranged.getMinCount());
                        itemTag.putInt("MaxCount", ranged.getCount());
                    } else {
                        var stacks = item.getItems();
                        if (stacks.length == 0 || stacks[0].isEmpty()) continue;
                        stack = stacks[0].copy();
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
                    if (fluid instanceof RangedFluidIngredient ranged) {
                        FluidStack[] stacks = ranged.getInner().getFluids();
                        if (stacks.length == 0 || stacks[0].isEmpty()) continue;
                        stack = stacks[0].copy();
                        stack.setAmount(ranged.getAmount());
                        fluidTag.putInt("MinAmount", ranged.getMinAmount());
                        fluidTag.putInt("MaxAmount", ranged.getAmount());
                    } else {
                        FluidStack[] stacks = fluid.getFluids();
                        if (stacks.length == 0) continue;
                        if (stacks[0].isEmpty()) continue;
                        stack = stacks[0].copy();
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
            List<ItemStack> outputItems = new ArrayList<>();
            if (capData.contains("OutputItems", Tag.TAG_LIST)) {
                ListTag itemTags = capData.getList("OutputItems", Tag.TAG_COMPOUND);
                if (!itemTags.isEmpty()) {
                    for (Tag tag : itemTags) {
                        if (tag instanceof CompoundTag tCompoundTag) {
                            var stack = GTUtil.loadItemStack(tCompoundTag);
                            if (!stack.isEmpty()) outputItems.add(stack);
                        }
                    }
                }
            }
            List<FluidStack> outputFluids = new ArrayList<>();
            if (capData.contains("OutputFluids", Tag.TAG_LIST)) {
                ListTag fluidTags = capData.getList("OutputFluids", Tag.TAG_COMPOUND);
                for (Tag tag : fluidTags) {
                    if (tag instanceof CompoundTag tCompoundTag) {
                        var stack = FluidStack.loadFluidStackFromNBT(tCompoundTag);
                        if (!stack.isEmpty()) outputFluids.add(stack);
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

    private void addItemTooltips(ITooltip iTooltip, List<ItemStack> outputItems) {
        IElementHelper helper = iTooltip.getElementHelper();
        for (ItemStack itemOutput : outputItems) {
            if (itemOutput != null && !itemOutput.isEmpty()) {
                ItemStack item = itemOutput.copy();
                MutableComponent text = CommonComponents.space();
                CompoundTag tag = item.getTag();
                if (tag != null && tag.contains("MinCount") && tag.contains("MaxCount")) {
                    text.append(Component.translatable("gtceu.gui.content.range",
                            tag.getInt("MinCount"), tag.getInt("MaxCount")));
                } else {
                    text.append(String.valueOf(item.getCount()));
                }
                item.setCount(1);
                text.append(Component.translatable("gtceu.gui.content.times_item",
                        getItemName(item))
                        .withStyle(ChatFormatting.WHITE));

                iTooltip.add(helper.smallItem(item));
                iTooltip.append(text);
            }
        }
    }

    private void addFluidTooltips(ITooltip iTooltip, List<FluidStack> outputFluids) {
        for (FluidStack fluidOutput : outputFluids) {
            if (fluidOutput != null && !fluidOutput.isEmpty()) {
                FluidStack stack = fluidOutput.copy();
                MutableComponent text = CommonComponents.space();
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("MinAmount") && tag.contains("MaxAmount")) {
                    text.append(Component.translatable("gtceu.gui.content.range",
                            FluidTextHelper.getUnicodeMillibuckets(tag.getInt("MinAmount"), true),
                            FluidTextHelper.getUnicodeMillibuckets(tag.getInt("MaxAmount"), true)));
                } else {
                    text.append(FluidTextHelper.getUnicodeMillibuckets(stack.getAmount(), true));
                }
                text.append(CommonComponents.space())
                        .append(getFluidName(stack))
                        .withStyle(ChatFormatting.WHITE);

                iTooltip.add(GTElementHelper.smallFluid(getFluid(stack)));
                iTooltip.append(text);
            }
        }
    }

    private Component getItemName(ItemStack stack) {
        return stack.getDisplayName().copy().withStyle(ChatFormatting.WHITE);
    }

    private Component getFluidName(FluidStack stack) {
        return ComponentUtils.wrapInSquareBrackets(stack.getDisplayName()).withStyle(ChatFormatting.WHITE);
    }

    private JadeFluidObject getFluid(FluidStack stack) {
        return JadeFluidObject.of(stack.getFluid(), stack.getAmount());
    }
}
