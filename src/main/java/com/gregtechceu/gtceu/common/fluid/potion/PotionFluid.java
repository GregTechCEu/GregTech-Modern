package com.gregtechceu.gtceu.common.fluid.potion;

import com.gregtechceu.gtceu.common.data.GTFluids;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import org.jetbrains.annotations.NotNull;

public class PotionFluid extends BaseFlowingFluid {

    public PotionFluid(Properties properties) {
        super(properties
                .bucket(() -> Items.AIR)
                .block(() -> (LiquidBlock) Blocks.WATER));
        registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    public static FluidStack of(int amount, @NotNull Holder<Potion> potion) {
        return of(amount, new PotionContents(potion));
    }

    public static FluidStack of(int amount, @NotNull PotionContents potion) {
        FluidStack fluidStack = new FluidStack(GTFluids.POTION.get().getSource(), amount);
        fluidStack.set(DataComponents.POTION_CONTENTS, potion);
        return fluidStack;
    }

    @Override
    public boolean isSource(@NotNull FluidState state) {
        return this == GTFluids.POTION.get().getSource();
    }

    @Override
    public int getAmount(@NotNull FluidState state) {
        return state.getValue(LEVEL);
    }

    public static class PotionFluidType extends FluidType {

        /**
         * Default constructor.
         *
         * @param properties the general properties of the fluid type
         */
        public PotionFluidType(Properties properties, ResourceLocation still, ResourceLocation flow) {
            super(properties);
        }

        @Override
        public @NotNull String getDescriptionId(FluidStack stack) {
            return Potion.getName(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(),
                    this.getDescriptionId() + ".effect.");
        }
    }
}
