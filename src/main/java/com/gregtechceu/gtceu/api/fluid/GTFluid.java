package com.gregtechceu.gtceu.api.fluid;

import com.gregtechceu.gtceu.api.fluid.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluid.attribute.IAttributedFluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class GTFluid extends BaseFlowingFluid implements IAttributedFluid {

    @Getter
    private final Collection<FluidAttribute> attributes = new ObjectLinkedOpenHashSet<>();
    @Getter
    private final FluidState state;
    @Getter
    private final int burnTime;

    public GTFluid(FluidState state, int burnTime, BaseFlowingFluid.Properties properties) {
        super(properties);
        this.state = state;
        this.burnTime = burnTime;
    }

    @Override
    public void addAttribute(FluidAttribute attribute) {
        attributes.add(attribute);
    }

    @Override
    protected boolean canBeReplacedWith(net.minecraft.world.level.material.FluidState state, BlockGetter level,
                                        BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 10;
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        boolean still = this.getSource() == fluid;
        boolean flowing = this.getFlowing() == fluid;
        return still || flowing;
    }

    public static class Source extends GTFluid {

        public Source(FluidState state, int burnTime, BaseFlowingFluid.Properties properties) {
            super(state, burnTime, properties);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return true;
        }
    }

    public static class Flowing extends GTFluid {

        public Flowing(FluidState state, int burnTime, BaseFlowingFluid.Properties properties) {
            super(state, burnTime, properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, net.minecraft.world.level.material.FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(net.minecraft.world.level.material.FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(net.minecraft.world.level.material.FluidState state) {
            return false;
        }
    }
}
