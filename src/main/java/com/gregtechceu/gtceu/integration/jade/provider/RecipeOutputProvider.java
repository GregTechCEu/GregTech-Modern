package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
                    CompoundTag itemTag;
                    if (item.content instanceof IntProviderIngredient provider) {
                        // don't roll for output but do copy for chance and batch
                        IntProviderIngredient chanced = provider;
                        if (item.chance < item.maxChance) {
                            double countD = (double) runs *
                                    function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                            chanced = (IntProviderIngredient) ItemRecipeCapability.CAP.copyWithModifier(provider,
                                    ContentModifier.multiplier(countD));
                        }
                        itemTag = (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, chanced.toJson());
                    } else {
                        var stacks = ItemRecipeCapability.CAP.of(item.content).getItems();
                        if (stacks.length == 0 || stacks[0].isEmpty()) continue;
                        var stack = stacks[0];
                        itemTag = new CompoundTag();
                        GTUtil.saveItemStack(stack, itemTag);
                        if (item.chance < item.maxChance) {
                            int count = stack.getCount();
                            double countD = (double) count * runs *
                                    function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                            count = Math.max(1, (int) Math.round(countD));
                            itemTag.putInt("Count", count);
                        }
                    }
                    itemTags.add(itemTag);
                }

                if (!itemTags.isEmpty()) {
                    data.put("OutputItems", itemTags);
                }

                ListTag fluidTags = new ListTag();
                for (var fluid : fluidContents) {
                    CompoundTag fluidTag;
                    if (fluid.content instanceof IntProviderFluidIngredient provider) {
                        // don't bother rolling output for nothing
                        IntProviderFluidIngredient chanced = provider;
                        if (fluid.chance < fluid.maxChance) {
                            double countD = (double) runs *
                                    function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                            chanced = (IntProviderFluidIngredient) FluidRecipeCapability.CAP.copyWithModifier(provider,
                                    ContentModifier.multiplier(countD));
                        }
                        fluidTag = chanced.toNBT();
                    } else {
                        FluidStack[] stacks = FluidRecipeCapability.CAP.of(fluid.content).getStacks();
                        if (stacks.length == 0) continue;
                        if (stacks[0].isEmpty()) continue;
                        var stack = stacks[0];
                        fluidTag = new CompoundTag();
                        stack.writeToNBT(fluidTag);

                        if (fluid.chance < fluid.maxChance) {
                            int amount = stacks[0].getAmount();
                            double amountD = (double) amount * runs *
                                    function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                            amount = Math.max(1, (int) Math.round(amountD));
                            fluidTag.putInt("Amount", amount);
                        }
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
            List<Ingredient> outputItems = new ArrayList<>();
            if (capData.contains("OutputItems", Tag.TAG_LIST)) {
                ListTag itemTags = capData.getList("OutputItems", Tag.TAG_COMPOUND);
                if (!itemTags.isEmpty()) {
                    for (Tag tag : itemTags) {
                        if (tag instanceof CompoundTag tCompoundTag) {
                            if (tCompoundTag.contains("count_provider")) {
                                var ingredient = IntProviderIngredient.SERIALIZER
                                        .parse((JsonObject) NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tCompoundTag));
                                outputItems.add(ingredient);
                            } else {
                                var stack = GTUtil.loadItemStack(tCompoundTag);
                                if (!stack.isEmpty()) {
                                    outputItems.add(SizedIngredient.create(stack));
                                }
                            }
                        }
                    }
                }
            }
            List<FluidIngredient> outputFluids = new ArrayList<>();
            if (capData.contains("OutputFluids", Tag.TAG_LIST)) {
                ListTag fluidTags = capData.getList("OutputFluids", Tag.TAG_COMPOUND);
                for (Tag tag : fluidTags) {
                    if (tag instanceof CompoundTag tCompoundTag) {
                        if (tCompoundTag.contains("count_provider")) {
                            var ingredient = IntProviderFluidIngredient.fromNBT(tCompoundTag);
                            outputFluids.add(ingredient);
                        } else {
                            var stack = FluidStack.loadFluidStackFromNBT(tCompoundTag);
                            if (!stack.isEmpty()) {
                                outputFluids.add(FluidIngredient.of(stack));
                            }
                        }
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

    private void addItemTooltips(ITooltip iTooltip, List<Ingredient> outputItems) {
        IElementHelper helper = iTooltip.getElementHelper();
        for (Ingredient itemOutput : outputItems) {
            if (itemOutput != null && !itemOutput.isEmpty()) {
                ItemStack item;
                MutableComponent text = CommonComponents.space();
                if (itemOutput instanceof IntProviderIngredient provider) {
                    item = provider.getInner().getItems()[0];
                    text = text.append(Component.translatable("gtceu.gui.content.range",
                            String.valueOf(provider.getCountProvider().getMinValue()),
                            String.valueOf(provider.getCountProvider().getMaxValue())));
                } else {
                    item = itemOutput.getItems()[0];
                    text.append(String.valueOf(item.getCount()));
                    item.setCount(1);
                }
                text.append(Component.translatable("gtceu.gui.content.times_item",
                        getItemName(item))
                        .withStyle(ChatFormatting.WHITE));

                iTooltip.add(helper.smallItem(item));
                iTooltip.append(text);
            }
        }
    }

    private void addFluidTooltips(ITooltip iTooltip, List<FluidIngredient> outputFluids) {
        for (FluidIngredient fluidOutput : outputFluids) {
            if (fluidOutput != null && !fluidOutput.isEmpty()) {
                FluidStack stack;
                MutableComponent text = CommonComponents.space();
                if (fluidOutput instanceof IntProviderFluidIngredient provider) {
                    stack = provider.getInner().getStacks()[0];
                    text.append(Component.translatable("gtceu.gui.content.range",
                            FluidTextHelper.getUnicodeMillibuckets(provider.getCountProvider().getMinValue(), true),
                            FluidTextHelper.getUnicodeMillibuckets(provider.getCountProvider().getMaxValue(), true)));
                } else {
                    stack = fluidOutput.getStacks()[0];
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
