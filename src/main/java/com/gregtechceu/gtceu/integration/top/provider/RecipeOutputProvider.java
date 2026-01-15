package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
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
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import lombok.experimental.ExtensionMethod;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ExtensionMethod(SizedIngredientExtensions.class)
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

                List<SizedIngredient> itemOutputs = new ArrayList<>();
                for (var item : itemContents) {
                    ItemStack[] stacks;
                    SizedIngredient content = ItemRecipeCapability.CAP.of(item.content);
                    // if (content instanceof IntProviderIngredient provider) {
                    // stacks = provider.getInner().getItems();
                    // } else {
                    stacks = content.getItems();
                    // }
                    if (stacks.length == 0) continue;
                    if (stacks[0].isEmpty()) continue;
                    var stack = stacks[0].copy();

                    if (item.chance < item.maxChance) {
                        int count = stack.getCount();
                        double countD = (double) count * recipe.parallels *
                                function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                        count = Math.max(1, (int) Math.round(countD));
                        stack.setCount(count);
                    }
                    itemOutputs.add(RecipeHelper.makeSizedIngredient(stack));
                }

                List<SizedFluidIngredient> fluidOutputs = new ArrayList<>();
                for (var fluid : fluidContents) {
                    var stacks = FluidRecipeCapability.CAP.of(fluid.content).getFluids();
                    if (stacks.length == 0) continue;
                    if (stacks[0].isEmpty()) continue;
                    var stack = stacks[0].copy();

                    if (fluid.chance < fluid.maxChance) {
                        int amount = stack.getAmount();
                        double amountD = (double) amount * recipe.parallels *
                                function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                        amount = Math.max(1, (int) Math.round(amountD));
                        stack.setAmount(amount);
                    }
                    fluidOutputs.add(RecipeHelper.makeSizedFluidIngredient(stack));
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

    private void addItemInfo(IProbeInfo verticalPane, List<SizedIngredient> outputItems) {
        for (SizedIngredient itemOutput : outputItems) {
            if (itemOutput != null && !itemOutput.ingredient().hasNoItems()) {
                ItemStack stack = itemOutput.getItems()[0];

                IProbeInfo horizontalPane = verticalPane
                        .horizontal(verticalPane.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                String spacer = " ";

                if (itemOutput.getContainedCustom() instanceof IntProviderIngredient provider) {
                    spacer += (Component.translatable("gtceu.gui.content.range",
                            String.valueOf(provider.getCountProvider().getMinValue()),
                            String.valueOf(provider.getCountProvider().getMaxValue()))) + " ";
                    provider.setItemStacks(null); // no roll
                    provider.setSampledCount(1);
                }
                horizontalPane.item(stack,
                        new ItemStyle().width(16).height(16))
                        .text(spacer)
                        .itemLabel(stack);
            }
        }
    }

    private void addFluidInfo(IProbeInfo verticalPane, List<SizedFluidIngredient> outputFluids) {
        for (SizedFluidIngredient fluidOutput : outputFluids) {
            if (fluidOutput != null && !fluidOutput.ingredient().hasNoFluids()) {
                FluidStack stack = fluidOutput.getFluids()[0];

                IProbeInfo horizontalPane = verticalPane
                        .horizontal(verticalPane.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                String spacer = " ";
                if (fluidOutput.ingredient() instanceof IntProviderFluidIngredient provider) {
                    spacer += (Component.translatable("gtceu.gui.content.range",
                            String.valueOf(provider.getCountProvider().getMinValue()),
                            String.valueOf(provider.getCountProvider().getMaxValue()))) + " ";
                    provider.setFluidStacks(null); // no roll
                    provider.setSampledCount(1);
                    stack.setAmount(provider.getCountProvider().getMaxValue()); // no roll
                }
                horizontalPane.element(new FluidStackElement(stack, new FluidStyle()))
                        .text(spacer)
                        .text(stack.getHoverName());
            }
        }
    }
}
