package com.gregtechceu.gtceu.api.fluids;

import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.IAttributedFluid;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

// TODO implement burning/acid/etc. damage when standing in a fluid block
public abstract class GTFluid extends BaseFlowingFluid implements IAttributedFluid {

    private static final double MAX_FLUID_HEIGHT = 8.0D / 9.0D;

    @Getter
    private final Collection<FluidAttribute> attributes = new ObjectLinkedOpenHashSet<>();
    @Getter
    private final com.gregtechceu.gtceu.api.fluids.FluidState state;
    @Getter
    private final int burnTime;

    public GTFluid(com.gregtechceu.gtceu.api.fluids.FluidState state, int burnTime,
                   BaseFlowingFluid.Properties properties) {
        super(properties);
        this.state = state;
        this.burnTime = burnTime;
    }

    @Override
    public void addAttribute(FluidAttribute attribute) {
        attributes.add(attribute);
    }

    // region up-flowing gas handling
    // all of these should have early return conditions that invoke the superclass's method if the config is disabled or
    // the fluid is "heavier than air".

    /**
     * {@return {@code true} if this fluid should flow up instead of down}
     */
    protected boolean shouldFlowUpward() {
        if (!ConfigHolder.INSTANCE.gameplay.lowDensityFluidsFlowUp) {
            return false;
        }
        return this.getFluidType().isLighterThanAir();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level,
                                        BlockPos pos, Fluid fluid, Direction direction) {
        if (!shouldFlowUpward()) {
            return super.canBeReplacedWith(state, level, pos, fluid, direction);
        }

        // same implementation was WaterFluid. LavaFluid has a different one, but we don't need to change this rn.
        // super method uses Direction.DOWN here
        return direction == Direction.UP && !isSame(fluid);
    }

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState myState) {
        if (!shouldFlowUpward()) {
            return super.getFlow(level, pos, myState);
        }

        double xMotion = 0.0D;
        double zMotion = 0.0D;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            cursor.setWithOffset(pos, dir);
            FluidState otherState = level.getFluidState(cursor);
            if (!this.affectsFlow(otherState)) {
                continue;
            }
            float otherFluidHeight = otherState.getOwnHeight();
            double speedMultiplier = 0.0D;

            if (otherFluidHeight == 0.0f) {
                // source block

                // noinspection deprecation
                if (!level.getBlockState(cursor).blocksMotion()) {
                    FluidState aboveState = level.getFluidState(cursor.above());
                    if (this.affectsFlow(aboveState)) {
                        otherFluidHeight = aboveState.getOwnHeight();
                        if (otherFluidHeight > 0.0f) {
                            speedMultiplier = myState.getOwnHeight() - (otherFluidHeight - MAX_FLUID_HEIGHT);
                        }
                    }
                }
            } else if (otherFluidHeight > 0.0f) {
                speedMultiplier = myState.getOwnHeight() - otherFluidHeight;
            }

            if (speedMultiplier != 0.0D) {
                xMotion += dir.getStepX() * speedMultiplier;
                zMotion += dir.getStepZ() * speedMultiplier;
            }
        }

        Vec3 flowVector = new Vec3(xMotion, 0.0D, zMotion);
        if (myState.getValue(FALLING)) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                cursor.setWithOffset(pos, dir);
                if (this.isSolidFace(level, cursor, dir) || this.isSolidFace(level, cursor.below(), dir)) {
                    // make it flow UP instead of DOWN :tr:
                    flowVector = flowVector.normalize().add(0.0D, 6.0D, 0.0D);
                    break;
                }
            }
        }

        return flowVector.normalize();
    }

    @Override
    protected boolean isSolidFace(BlockGetter level, BlockPos neighborPos, Direction side) {
        if (!shouldFlowUpward()) {
            return super.isSolidFace(level, neighborPos, side);
        }

        BlockState neighborBlock = level.getBlockState(neighborPos);
        if (neighborBlock.getFluidState().getType().isSame(this)) {
            return false;
        } else if (side == Direction.DOWN) {
            return true;
        } else if (neighborBlock.getBlock() instanceof IceBlock) {
            return false;
        } else {
            return neighborBlock.isFaceSturdy(level, neighborPos, side);
        }
    }

    @Override
    protected void spread(Level level, BlockPos pos, FluidState state) {
        // we can safely do this here because the parent method does the same
        if (state.isEmpty()) {
            return;
        }
        if (!shouldFlowUpward()) {
            super.spread(level, pos, state);
            return;
        }

        BlockState blockState = level.getBlockState(pos);
        BlockPos spreadPos = pos.above();
        BlockState spreadState = level.getBlockState(spreadPos);
        FluidState spreadFluid = this.getNewLiquid(level, spreadPos, spreadState);
        if (this.canSpreadTo(level, pos, blockState, Direction.UP, spreadPos, spreadState,
                level.getFluidState(spreadPos), spreadFluid.getType())) {
            this.spreadTo(level, spreadPos, spreadState, Direction.UP, spreadFluid);
            if (this.sourceNeighborCount(level, pos) >= 3) {
                this.spreadToSides(level, pos, state, blockState);
            }
        } else if (state.isSource() ||
                !this.isWaterHole(level, spreadFluid.getType(), pos, blockState, spreadPos, spreadState)) {
                    this.spreadToSides(level, pos, state, blockState);
                }
    }

    @Override
    protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState blockState) {
        if (!shouldFlowUpward()) {
            return super.getNewLiquid(level, pos, blockState);
        }

        int highestAdjacentLevel = 0;
        int adjacentSourceCount = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos spreadPos = pos.relative(direction);
            BlockState spreadState = level.getBlockState(spreadPos);
            FluidState spreadStateFluid = spreadState.getFluidState();
            if (spreadStateFluid.getType().isSame(this) &&
                    this.canPassThroughWall(direction, level, pos, blockState, spreadPos, spreadState)) {
                if (spreadStateFluid.isSource() && EventHooks.canCreateFluidSource(level, spreadPos, spreadState)) {
                    adjacentSourceCount++;
                }

                highestAdjacentLevel = Math.max(highestAdjacentLevel, spreadStateFluid.getAmount());
            }
        }

        if (adjacentSourceCount >= 2) {
            // >=2 adjacent source blocks can create a new source block if canConvertToSource is true for them
            BlockState aboveBlock = level.getBlockState(pos.above());
            FluidState aboveFluid = aboveBlock.getFluidState();
            // noinspection deprecation
            if (aboveBlock.isSolid() || this.isSourceBlockOfThisType(aboveFluid)) {
                return this.getSource(false);
            }
        }

        BlockPos spreadPos = pos.below();
        BlockState spreadState = level.getBlockState(spreadPos);
        FluidState spreadStateFluid = spreadState.getFluidState();
        if (!spreadStateFluid.isEmpty() && spreadStateFluid.getType().isSame(this) &&
                this.canPassThroughWall(Direction.DOWN, level, pos, blockState, spreadPos, spreadState)) {
            return this.getFlowing(8, true);
        } else {
            int actualLevel = highestAdjacentLevel - this.getDropOff(level);
            return actualLevel <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(actualLevel, false);
        }
    }

    @Override
    protected int getSlopeDistance(LevelReader level, BlockPos spreadPos, int distance, Direction currentDirection,
                                   BlockState currentSpreadState, BlockPos sourcePos,
                                   Short2ObjectMap<Pair<BlockState, FluidState>> stateCache,
                                   Short2BooleanMap waterHoleCache) {
        if (!shouldFlowUpward()) {
            return super.getSlopeDistance(level, spreadPos, distance, currentDirection, currentSpreadState,
                    sourcePos, stateCache, waterHoleCache);
        }

        int lowestDistance = 1000;

        for (Direction spreadDirection : Direction.Plane.HORIZONTAL) {
            if (spreadDirection == currentDirection) {
                continue;
            }
            BlockPos nextSpreadPos = spreadPos.relative(spreadDirection);
            short cacheKey = getCacheKey(sourcePos, nextSpreadPos);
            Pair<BlockState, FluidState> pair = stateCache.computeIfAbsent(cacheKey, $ -> {
                BlockState blockstate1 = level.getBlockState(nextSpreadPos);
                return Pair.of(blockstate1, blockstate1.getFluidState());
            });
            BlockState spreadBlock = pair.getFirst();
            FluidState spreadFluid = pair.getSecond();

            if (!this.canPassThrough(level, this.getFlowing(), spreadPos, currentSpreadState, spreadDirection,
                    nextSpreadPos, spreadBlock, spreadFluid)) {
                continue;
            }

            boolean hasWaterHole = waterHoleCache.computeIfAbsent(cacheKey, $ -> {
                BlockPos abovePos = nextSpreadPos.above();
                BlockState aboveState = level.getBlockState(abovePos);
                return this.isWaterHole(level, this.getFlowing(), nextSpreadPos, spreadBlock, abovePos, aboveState);
            });
            if (hasWaterHole) {
                return distance;
            }
            if (distance >= this.getSlopeFindDistance(level)) {
                continue;
            }

            int newDistance = this.getSlopeDistance(level, nextSpreadPos, distance + 1, spreadDirection.getOpposite(),
                    spreadBlock, sourcePos, stateCache, waterHoleCache);
            if (newDistance < lowestDistance) {
                lowestDistance = newDistance;
            }
        }

        return lowestDistance;
    }

    @Override
    public boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos pos, BlockState state, BlockPos spreadPos,
                               BlockState spreadState) {
        if (!shouldFlowUpward()) {
            return super.isWaterHole(level, fluid, pos, state, spreadPos, spreadState);
        }

        // inverted direction
        if (!this.canPassThroughWall(Direction.UP, level, pos, state, spreadPos, spreadState)) {
            return false;
        } else if (spreadState.getFluidState().getType().isSame(this)) {
            return true;
        } else {
            return this.canHoldFluid(level, spreadPos, spreadState, fluid);
        }
    }

    @Override
    protected Map<Direction, FluidState> getSpread(Level level, BlockPos pos, BlockState state) {
        int lowestDistance = 1000;
        Map<Direction, FluidState> result = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> stateCache = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap waterHoleCache = new Short2BooleanOpenHashMap();

        for (Direction spreadDirection : Direction.Plane.HORIZONTAL) {
            BlockPos spreadPos = pos.relative(spreadDirection);
            short cacheKey = getCacheKey(pos, spreadPos);
            Pair<BlockState, FluidState> pair = stateCache.computeIfAbsent(cacheKey, $ -> {
                BlockState spreadState = level.getBlockState(spreadPos);
                return Pair.of(spreadState, spreadState.getFluidState());
            });
            BlockState spreadBlock = pair.getFirst();
            FluidState spreadFluid = pair.getSecond();
            FluidState newFluid = this.getNewLiquid(level, spreadPos, spreadBlock);

            if (!this.canPassThrough(level, newFluid.getType(), pos, state, spreadDirection, spreadPos, spreadBlock,
                    spreadFluid)) {
                continue;
            }

            boolean hasWaterHole = waterHoleCache.computeIfAbsent(cacheKey, $ -> {
                BlockPos abovePos = spreadPos.above();
                BlockState aboveState = level.getBlockState(abovePos);
                return this.isWaterHole(level, this.getFlowing(), spreadPos, spreadBlock, abovePos, aboveState);
            });
            int slopeDistance = 0;
            if (!hasWaterHole) {
                slopeDistance = this.getSlopeDistance(level, spreadPos, 1, spreadDirection.getOpposite(), spreadBlock,
                        pos,
                        stateCache, waterHoleCache);
            }

            if (slopeDistance <= lowestDistance) {
                if (slopeDistance < lowestDistance) {
                    result.clear();
                }

                result.put(spreadDirection, newFluid);
                lowestDistance = slopeDistance;
            }
        }

        return result;
    }

    protected static boolean hasSameBelow(FluidState fluidState, BlockGetter level, BlockPos pos) {
        return fluidState.getType().isSame(level.getFluidState(pos.below()).getType());
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        if (!shouldFlowUpward()) {
            return super.getHeight(state, level, pos);
        }
        return hasSameBelow(state, level, pos) ? 1.0f : state.getOwnHeight();
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        if (!shouldFlowUpward()) {
            return super.getShape(state, level, pos);
        }
        if (state.getAmount() == FluidState.AMOUNT_MAX && hasSameBelow(state, level, pos)) {
            return Shapes.block();
        }
        // we know the fluid should flow up and that it doesn't have the same block below it, so we can safely skip
        // calling FluidState#getHeight here and shortcut to getOwnHeight
        return this.shapes.computeIfAbsent(state,
                fluidState -> Shapes.box(0.0D, 1.0D - fluidState.getOwnHeight(), 0.0D, 1.0D, 1.0D, 1.0D));
    }

    // endregion upside down gas handling

    public static class Source extends GTFluid {

        public Source(com.gregtechceu.gtceu.api.fluids.FluidState state, int burnTime,
                      BaseFlowingFluid.Properties properties) {
            super(state, burnTime, properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends GTFluid {

        public Flowing(com.gregtechceu.gtceu.api.fluids.FluidState state, int burnTime,
                       BaseFlowingFluid.Properties properties) {
            super(state, burnTime, properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
