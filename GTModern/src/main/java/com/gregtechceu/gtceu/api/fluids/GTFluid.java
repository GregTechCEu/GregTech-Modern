package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.IAttributedFluid;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class GTFluid extends FlowingFluid implements IAttributedFluid {

    @Getter
    private final Collection<FluidAttribute> attributes = new ObjectLinkedOpenHashSet<>();
    @Getter
    private final FluidState state;
    private final Supplier<? extends Item> bucketItem;
    private final Supplier<? extends Fluid> stillFluid;
    private final Supplier<? extends Fluid> flowingFluid;
    private final Supplier<? extends LiquidBlock> block;
    @Getter
    private final int burnTime;

    public GTFluid(@NotNull FluidState state, Supplier<? extends Fluid> stillFluid,
                   Supplier<? extends Fluid> flowingFluid, Supplier<? extends LiquidBlock> block,
                   Supplier<? extends Item> bucket, int burnTime) {
        super();
        this.state = state;
        this.stillFluid = stillFluid;
        this.flowingFluid = flowingFluid;
        this.block = block;
        this.bucketItem = bucket;
        this.burnTime = burnTime;
    }

    @Override
    public void addAttribute(@NotNull FluidAttribute attribute) {
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
    protected BlockState createLegacyBlock(net.minecraft.world.level.material.FluidState state) {
        if (block != null && block.get() != null)
            return block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public Fluid getFlowing() {
        return flowingFluid != null && flowingFluid.get() != null ? flowingFluid.get() : Fluids.EMPTY;
    }

    @Override
    public Fluid getSource() {
        return stillFluid != null && stillFluid.get() != null ? stillFluid.get() : Fluids.EMPTY;
    }

    @Override
    public Item getBucket() {
        return bucketItem != null && bucketItem.get() != null ? bucketItem.get() : Items.AIR;
    }

    @Override
    protected boolean canConvertToSource(Level world) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    public boolean isSame(Fluid fluid) {
        boolean still = this.getSource() == fluid;
        boolean flowing = this.getFlowing() == fluid;
        return still || flowing;
    }
}
