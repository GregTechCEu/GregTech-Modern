package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
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
                ListTag itemTags = new ListTag();
                for (var stack : RecipeHelper.getOutputItems(recipe)) {
                    if (stack != null && !stack.isEmpty()) {
                        var itemTag = new CompoundTag();

                        GTUtil.saveItemStack(stack, itemTag);
                        itemTags.add(itemTag);
                    }
                }
                if (!itemTags.isEmpty()) {
                    data.put("OutputItems", itemTags);
                }
                ListTag fluidTags = new ListTag();
                for (var stack : RecipeHelper.getOutputFluids(recipe)) {
                    if (stack != null && !stack.isEmpty()) {
                        var fluidTag = new CompoundTag();
                        stack.writeToNBT(fluidTag);
                        fluidTags.add(fluidTag);
                    }
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
                            if (!stack.isEmpty()) {
                                outputItems.add(stack);
                            }
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
                        if (!stack.isEmpty()) {
                            outputFluids.add(stack);
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

    private void addItemTooltips(ITooltip iTooltip, List<ItemStack> outputItems) {
        IElementHelper helper = iTooltip.getElementHelper();
        for (ItemStack itemOutput : outputItems) {
            if (itemOutput != null && !itemOutput.isEmpty()) {
                int count = itemOutput.getCount();
                itemOutput.setCount(1);
                iTooltip.add(helper.smallItem(itemOutput));
                Component text = Component.literal(" ")
                        .append(String.valueOf(count))
                        .append("× ")
                        .append(getItemName(itemOutput))
                        .withStyle(ChatFormatting.WHITE);
                iTooltip.append(text);
            }
        }
    }

    private void addFluidTooltips(ITooltip iTooltip, List<FluidStack> outputFluids) {
        for (FluidStack fluidOutput : outputFluids) {
            if (fluidOutput != null && !fluidOutput.isEmpty()) {
                iTooltip.add(GTElementHelper.smallFluid(getFluid(fluidOutput)));
                Component text = Component.literal(" ")
                        .append(FluidTextHelper.getUnicodeMillibuckets(fluidOutput.getAmount(), true))
                        .append(" ")
                        .append(getFluidName(fluidOutput))
                        .withStyle(ChatFormatting.WHITE);
                iTooltip.append(text);

            }
        }
    }

    private Component getItemName(ItemStack stack) {
        return ComponentUtils.wrapInSquareBrackets(stack.getItem().getDescription()).withStyle(ChatFormatting.WHITE);
    }

    private Component getFluidName(FluidStack stack) {
        return ComponentUtils.wrapInSquareBrackets(stack.getDisplayName()).withStyle(ChatFormatting.WHITE);
    }

    private JadeFluidObject getFluid(FluidStack stack) {
        return JadeFluidObject.of(stack.getFluid(), stack.getAmount());
    }
}
