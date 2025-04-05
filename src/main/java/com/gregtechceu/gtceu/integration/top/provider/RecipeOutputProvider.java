package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.integration.top.element.FluidStackElement;
import com.gregtechceu.gtceu.integration.top.element.FluidStyle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeOutputProvider extends CapabilityInfoProvider<RecipeLogic> {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("recipe_output_info");
    }

    @Nullable
    @Override
    protected RecipeLogic getCapability(Level level, BlockPos blockPos, @Nullable Direction direction) {
        return GTCapabilityHelper.getRecipeLogic(level, blockPos, direction);
    }

    @Override
    protected void addProbeInfo(RecipeLogic recipeLogic, IProbeInfo iProbeInfo, Player player, BlockEntity blockEntity,
                                IProbeHitData iProbeHitData) {
        if (recipeLogic.isWorking()) {
            var recipe = recipeLogic.getLastRecipe();
            if (recipe != null) {
                int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
                int chanceTier = recipeTier + recipe.ocLevel;
                var function = recipe.getType().getChanceFunction();
                var itemContents = recipe.getOutputContents(ItemRecipeCapability.CAP);
                var fluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);

                List<ItemStack> itemOutputs = new ArrayList<>();
                for (var item : itemContents) {
                    var stacks = ItemRecipeCapability.CAP.of(item.content).getItems();
                    if (stacks.length == 0) continue;
                    if (stacks[0].isEmpty()) continue;
                    var stack = stacks[0].copy();

                    if (item.chance < item.maxChance) {
                        int count = stack.getCount();
                        double countD = (double) count * recipe.parallels *
                                function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                        count = countD < 1 ? 1 : (int) Math.round(countD);
                        stack.setCount(count);
                    }
                    itemOutputs.add(stack);
                }

                List<FluidStack> fluidOutputs = new ArrayList<>();
                for (var fluid : fluidContents) {
                    var stacks = FluidRecipeCapability.CAP.of(fluid.content).getStacks();
                    if (stacks.length == 0) continue;
                    if (stacks[0].isEmpty()) continue;
                    var stack = stacks[0].copy();

                    if (fluid.chance < fluid.maxChance) {
                        int amount = stack.getAmount();
                        double amountD = (double) amount * recipe.parallels *
                                function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                        amount = amountD < 1 ? 1 : (int) Math.round(amountD);
                        stack.setAmount(amount);
                    }
                    fluidOutputs.add(stack);
                }

                if (!itemOutputs.isEmpty() || !fluidOutputs.isEmpty()) {
                    IProbeInfo verticalPane = iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle().spacing(0));
                    verticalPane.text(
                            CompoundText.create().info(Component.translatable("gtceu.top.recipe_output").append(" ")));
                    addItemInfo(verticalPane, itemOutputs);
                    addFluidInfo(verticalPane, fluidOutputs);
                }
            }
        }
    }

    private void addItemInfo(IProbeInfo verticalPane, List<ItemStack> outputItems) {
        IProbeInfo horizontalPane;
        for (ItemStack itemOutput : outputItems) {
            if (itemOutput != null && !itemOutput.isEmpty()) {
                horizontalPane = verticalPane
                        .horizontal(verticalPane.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalPane.item(itemOutput, new ItemStyle().width(16).height(16)).text(" ").itemLabel(itemOutput);
            }
        }
    }

    private void addFluidInfo(IProbeInfo verticalPane, List<FluidStack> outputFluids) {
        IProbeInfo horizontalPane;
        for (FluidStack fluidOutput : outputFluids) {
            if (fluidOutput != null && !fluidOutput.isEmpty()) {
                horizontalPane = verticalPane
                        .horizontal(verticalPane.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalPane.element(new FluidStackElement(fluidOutput, new FluidStyle())).text(" ")
                        .text(fluidOutput.getDisplayName());
            }
        }
    }
}
