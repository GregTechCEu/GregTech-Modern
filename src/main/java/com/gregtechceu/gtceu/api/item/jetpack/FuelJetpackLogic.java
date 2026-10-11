package com.gregtechceu.gtceu.api.item.jetpack;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.core.IGTRecipeManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

public class FuelJetpackLogic extends AbstractJetpackLogic {

    private static final String BURN_TIME_TAG = "burnTimer";

    public FuelJetpackLogic(JetpackStats stats, @Nullable JetpackStats hoverStats) {
        super(stats, hoverStats);
    }

    /** @return map of fuel ingredient to burn time in ticks */
    public static Object2IntMap<FluidIngredient> getFuels(RecipeManager recipeManager) {
        return ((IGTRecipeManager) recipeManager).gtceu$getJetpackFuels();
    }

    public static boolean isFuel(RecipeManager recipeManager, FluidStack fluid) {
        for (FluidIngredient fuel : getFuels(recipeManager).keySet()) {
            if (fuel.test(fluid)) {
                return true;
            }
        }
        return false;
    }

    private static int getBurnTimer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(BURN_TIME_TAG) : 0;
    }

    private static FluidIngredient findFuel(JetpackUseContext context) {
        FluidStack fluid = FluidUtil.getFluidContained(context.stack()).orElse(FluidStack.EMPTY);
        if (!fluid.isEmpty()) {
            for (FluidIngredient fuel : getFuels(context.player().level().getRecipeManager()).keySet()) {
                if (fuel.test(fluid) && fluid.getAmount() >= fuel.getAmount()) {
                    return fuel;
                }
            }
        }
        return FluidIngredient.EMPTY;
    }

    @Override
    public boolean canUseEnergy(JetpackUseContext context, int amount) {
        return getBurnTimer(context.stack()) > 0 || !findFuel(context).isEmpty();
    }

    @Override
    public void drainEnergy(JetpackUseContext context, int amount) {
        ItemStack stack = context.stack();
        int burnTimer = getBurnTimer(stack);
        if (burnTimer <= 0) {
            FluidIngredient fuel = findFuel(context);
            if (!fuel.isEmpty()) {
                FluidUtil.getFluidHandler(stack).ifPresent(h -> h.drain(fuel.getAmount(), FluidAction.EXECUTE));
                burnTimer += getFuels(context.player().level().getRecipeManager()).getInt(fuel);
            }
        }
        stack.getOrCreateTag().putInt(BURN_TIME_TAG, burnTimer - amount);
    }
}
