package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
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
                IProbeInfo verticalPane = iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle().spacing(0));
                verticalPane.text(
                        CompoundText.create().info(Component.translatable("gtceu.top.recipe_output").append(" ")));
                List<ItemStack> outputItems = RecipeHelper.getOutputItems(recipe);
                if (!outputItems.isEmpty()) {
                    addItemInfo(verticalPane, outputItems);
                }

                List<FluidStack> outputFluids = RecipeHelper.getOutputFluids(recipe);
                if (!outputFluids.isEmpty()) {
                    addFluidInfo(verticalPane, outputFluids);
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
