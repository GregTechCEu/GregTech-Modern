package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpMachine extends TieredEnergyMachine implements IAutoOutputFluid, IUIMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PumpMachine.class,
            TieredEnergyMachine.MANAGED_FIELD_HOLDER);
    public static final int BASE_PUMP_RADIUS = 16;
    public static final int EXTRA_PUMP_RADIUS = 4;
    public static final int PUMP_SPEED_BASE = 80;
    private final Set<BlockPos> forbiddenBlocks = new ObjectOpenHashSet<>();
    private PumpQueue pumpQueue = null;
    @Getter
    @Persisted
    private int pumpHeadY;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean autoOutputFluids;
    @Persisted
    @DropSaved
    protected final NotifiableFluidTank cache;

    public PumpMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.cache = createCacheFluidHandler(args);
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createCacheFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidType.BUCKET_VOLUME * Math.max(1, getTier()), IO.NONE, IO.OUT);
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    public void setAllowInputFromOutputSideFluids(boolean allow) {}

    @Override
    public Direction getOutputFacingFluids() {
        return getFrontFacing();
    }

    @Override
    public void setOutputFacingFluids(Direction outputFacing) {
        setFrontFacing(outputFacing);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::update);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    //////////////////////////////////////
    // ********* Logic **********//
    //////////////////////////////////////
    public static int getMaxPumpRadius(int tier) {
        return BASE_PUMP_RADIUS + EXTRA_PUMP_RADIUS * tier;
    }

    /**
     * Returns a list of directions, starting with Up and then horizontal directions with the directions most matching
     * the vector first.
     */
    private List<Direction> biasedInVecDirections(RandomSource randomSource, Vec3i vec, boolean goUp) {
        List<Direction> searchList = new ArrayList<>();
        if (goUp) {
            searchList.add(Direction.UP);
        }

        ObjectArrayList<Direction.Axis> axes = new ObjectArrayList<>();
        int zValue = Math.abs(vec.getZ());
        int xValue = Math.abs(vec.getX());
        if (zValue > xValue) {
            axes.add(Direction.Axis.Z);
            axes.add(Direction.Axis.X);
        } else if (zValue < xValue) {
            axes.add(Direction.Axis.X);
            axes.add(Direction.Axis.Z);
        } else {
            axes.add(Direction.Axis.Z);
            axes.add(Direction.Axis.X);
            Util.shuffle(axes, randomSource);
        }

        Direction lastDirection = null;
        for (int i = 0; i < 2; i++) {
            Direction.Axis axis = axes.get(i);
            int value;
            if (axis.equals(Direction.Axis.Z)) {
                value = vec.getZ();
            } else {
                value = vec.getX();
            }

            Direction direction;
            if (value < 0) {
                direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
            } else if (value > 0) {
                direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            } else {
                direction = Direction.fromAxisAndDirection(axis,
                        Util.getRandom(Direction.AxisDirection.values(), randomSource));
            }
            searchList.add(direction);
            if (i == 0) {
                lastDirection = direction.getOpposite();
            } else {
                searchList.add(direction.getOpposite());
            }

        }
        searchList.add(lastDirection);

        return searchList;
    }

    protected record PumpQueue(Queue<Deque<BlockPos>> queue, FluidType fluidType) {}

    protected record SearchResult(BlockPos pos, boolean isSource) {}

    /**
     * Returns the next block to search at.
     */
    @Nullable
    private SearchResult searchNext(Level level, BlockPos headPosBelow, BlockPos searchHead, FluidType fluidType,
                                    int maxPumpRange, boolean goUp, Set<BlockPos> checked) {
        // Vector from the pump head to the search head, so points in the direction away from the pump head
        Vec3i subVec = searchHead.subtract(headPosBelow);

        List<Direction> searchList = biasedInVecDirections(level.getRandom(), subVec, goUp);

        for (Direction direction : searchList) {
            BlockPos check = searchHead.relative(direction);
            // The pos at the same y-level as the spot to check, but the x and z of the pump
            // This is to compute the square distance only in the horizontal plane
            BlockPos pumpY = headPosBelow.atY(check.getY());

            // Skip if outside pump range or not loaded or already checked
            if (check.distSqr(pumpY) > maxPumpRange * maxPumpRange || checked.contains(check) ||
                    !level.isLoaded(check) || forbiddenBlocks.contains(check)) {
                continue;
            }

            // Make sure we don't look at it again
            checked.add(check);

            BlockState state = level.getBlockState(check);
            FluidState fluidState;

            // If it's not a fluid of the right type, we stop
            if ((fluidState = state.getFluidState()).getFluidType() == fluidType &&
                    state.getBlock() instanceof LiquidBlock liquidBlock) {
                // Remember all the sources we find
                boolean isSource = fluidState.isSource();
                if (isSource) {
                    var fluidHandler = new BucketPickupHandlerWrapper(liquidBlock, level, check);
                    FluidStack drainStack = fluidHandler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                    if (!drainStack.isEmpty()) {
                        return new SearchResult(check, true);
                    }
                }
                return new SearchResult(check, false);
            }
        }

        return null;
    }

    /**
     * Update the pump queue if it is empty.
     *
     * @param fluidType Use this if the pump queue must have the same fluid type because it was already decided in the
     *                  pump cycle.
     */
    private void updatePumpQueue(@Nullable FluidType fluidType) {
        if (getLevel() == null) return;

        if (pumpQueue != null && !pumpQueue.queue().isEmpty()) {
            return;
        }

        BlockPos headPos = getPos().below(pumpHeadY);

        BlockPos downPos = headPos.below(1);
        var downBlock = getLevel().getBlockState(downPos);

        if (!(downBlock.getBlock() instanceof LiquidBlock)) {
            pumpQueue = null;
            return;
        }

        if (fluidType != null && downBlock.getFluidState().getFluidType() != fluidType) {
            pumpQueue = null;
            return;
        }

        pumpQueue = buildPumpQueue(getLevel(), headPos, downBlock.getFluidState().getFluidType(), queueSize(), true);
    }

    /**
     * Does a "depth-first"-ish search to find a path to a source. It prioritizes going up and away from the pump head.
     * If the path it finds only contains sources at the level below the pump head, it will keep looking until it finds
     * one that has a source at a higher location. If it cannot find one, it will return the original path.
     */
    private PumpQueue buildPumpQueue(Level level, BlockPos headPos, FluidType fluidType, int queueSourceAmount,
                                     boolean upSources) {
        Set<BlockPos> checked = new ObjectOpenHashSet<>();

        BlockPos headPosBelow = headPos.below();

        checked.add(headPos);
        checked.add(headPosBelow);

        int maxPumpRange = getMaxPumpRadius(getTier());

        List<BlockPos> pathStack = new ArrayList<>();

        Deque<BlockPos> nonSources = new ArrayDeque<>();
        Deque<BlockPos> pathToLastSource = new ArrayDeque<>();
        Deque<BlockPos> sourceStack = new ArrayDeque<>();

        pathStack.add(headPosBelow);
        nonSources.add(headPosBelow);

        int iterations = 0;
        int previousSources = 0;
        Queue<Deque<BlockPos>> paths = new ArrayDeque<>();
        List<BlockPos> sources = new ArrayList<>();
        // We do at most 1000 iterations to try and find source blocks
        while (!pathStack.isEmpty() && iterations < 1000) {
            // Peeks at the tail
            BlockPos searchHead = pathStack.get(pathStack.size() - 1);

            SearchResult next = searchNext(level, headPosBelow, searchHead, fluidType, maxPumpRange, upSources,
                    checked);

            iterations++;

            if (next == null) {
                boolean continueSearch = sources.size() < queueSourceAmount;

                int addedSources = sources.size() - previousSources;
                previousSources = sources.size();
                if (addedSources > 0) {
                    var toAdd = new ArrayDeque<>(pathToLastSource);
                    // This is always the headPosBelow, which we do not want to include
                    toAdd.removeFirst();
                    paths.add(toAdd);
                }

                if (!continueSearch) {
                    return new PumpQueue(paths, fluidType);
                }

                // Now we need to rewind our stack
                BlockPos last = pathStack.remove(pathStack.size() - 1);
                BlockPos lastSource = sourceStack.peekLast();
                if (last.equals(lastSource)) {
                    BlockPos prevSource = sourceStack.removeLast();
                    // Rebuild nonSources until previous source
                    for (int i = pathStack.size() - 1; i >= 0; i--) {
                        BlockPos p = pathStack.get(i);
                        if (!p.equals(prevSource)) {
                            nonSources.addFirst(p);
                        } else {
                            break;
                        }
                    }
                    // If the last is a source, then nonSources will be empty regardless
                } else if (!nonSources.isEmpty()) {
                    nonSources.removeLast();
                }
            } else {
                // Add the next
                pathStack.add(next.pos());
                // If we are in search up mode, we only count it as a source if it's up
                if (next.isSource() && (!upSources || next.pos().getY() > headPosBelow.getY())) {
                    sources.add(next.pos());
                    // Found a source, so add all the non-source blocks we passed since the last one
                    pathToLastSource.addAll(nonSources);
                    // Also add the source itself
                    pathToLastSource.add(next.pos());
                    // Reset non-sources because we just added them and found a source
                    nonSources.clear();
                    sources.add(next.pos());
                } else {
                    // Not a source, but we want to track it
                    nonSources.add(next.pos());
                }

            }
        }
        if (upSources) {
            // If we found none, we try again without the restriction
            if (paths.isEmpty()) {
                return buildPumpQueue(level, headPos, fluidType, queueSourceAmount, false);
            }

            return new PumpQueue(paths, fluidType);
        }

        // Only after everything except the block directly below the pipe is pumped, do we want to pump it
        // Otherwise we might advance the pump head prematurely
        if (paths.isEmpty() && level.getBlockState(headPosBelow).getFluidState().isSource()) {
            return new PumpQueue(new ArrayDeque<>(List.of(new ArrayDeque<>(List.of(headPosBelow)))), fluidType);
        }

        return new PumpQueue(paths, fluidType);
    }

    /**
     * Advances the pump head if the block below is air and the pump queue is empty.
     */
    private boolean canAdvancePumpHead() {
        // position of the pump head, i.e. the position of the lowest mining pipe
        BlockPos headPos = getPos().below(pumpHeadY);

        if (pumpQueue == null || pumpQueue.queue.isEmpty()) {
            Level level;
            if ((level = getLevel()) != null) {
                BlockPos downPos = headPos.below(1);
                var downBlock = level.getBlockState(downPos);

                if (downBlock.isAir()) {
                    this.pumpHeadY++;

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.setBlockAndUpdate(downPos, GTBlocks.MINER_PIPE.getDefaultState());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onMachineRemoved() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            var pos = getPos().relative(Direction.DOWN);
            while (serverLevel.getBlockState(pos).is(GTBlocks.MINER_PIPE.get())) {
                serverLevel.removeBlock(pos, false);
                pos = pos.relative(Direction.DOWN);
            }
        }
    }

    protected record SourceState(BlockState state, BlockPos pos) {}

    /**
     * Does a full pump cycle, trying to do the required number of pumps. It will rebuild the queue if it becomes
     * empty without having fulfilled its required number of pumps. All paths computed in the queue are checked
     * if they are still valid and consist only of the right fluid.
     */
    private void pumpCycle() {
        Level level;
        if ((level = getLevel()) == null) {
            return;
        }
        // Will only update if the queue is empty
        updatePumpQueue(null);
        int pumps = pumpsPerCycle();

        // We try to pump `pumps` amount of source blocks, using multiple paths if necessary
        boolean pumped = false;
        int iterations = 0;
        // We keep looking at paths as long as we still have pumps to go
        // We put the iterations at max 10 just to be sure
        while (pumps > 0 && pumpQueue != null && !pumpQueue.queue().isEmpty() && iterations < 10) {
            iterations++;

            Deque<BlockPos> pumpPath = pumpQueue.queue().peek();
            Deque<SourceState> states = new ArrayDeque<>();

            // We iterate through the positions to check if it is still a valid path, saving the states
            for (BlockPos pos : pumpPath) {
                // Stop once an unloaded block is found
                if (!level.isLoaded(pos)) {
                    break;
                }
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof LiquidBlock liquidBlock &&
                        (liquidBlock.getFluidState(state)).getFluidType() == pumpQueue.fluidType()) {
                    states.add(new SourceState(state, pos));
                } else {
                    break;
                }
            }

            // We remove from the end until we find a matching state, everything after must be no longer valid
            while (pumps > 0 && !pumpPath.isEmpty()) {
                BlockPos pos = pumpPath.removeLast();
                SourceState sourceState = states.peekLast();
                if (sourceState != null && pos.equals(sourceState.pos())) {
                    states.removeLast();
                    FluidState fluidState = sourceState.state().getFluidState();
                    if (sourceState.state().getBlock() instanceof LiquidBlock liquidBlock && fluidState.isSource()) {
                        var fluidHandler = new BucketPickupHandlerWrapper(liquidBlock, getLevel(), pos);
                        FluidStack drainStack = fluidHandler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                        if (!drainStack.isEmpty() &&
                                cache.fillInternal(drainStack, FluidAction.SIMULATE) == drainStack.getAmount()) {
                            cache.fillInternal(drainStack, FluidAction.EXECUTE);
                            fluidHandler.drain(drainStack, FluidAction.EXECUTE);
                            getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            pumped = true;
                            pumps--;
                        } else if (!drainStack.isEmpty()) {
                            // In this case we just couldn't fill the internal tank, it's most likely full
                            // So we add back to the pump path and return
                            pumpPath.add(pos);
                            return;
                        } else {
                            // drain stack is empty even though it's a fluid source, probably something went wrong
                            // ignore block for a while
                            forbiddenBlocks.add(pos);
                            return;
                        }
                    }
                }
            }

            if (pumpPath.isEmpty()) {
                pumpQueue.queue().remove();
            }

            // If we have pumps left over and there is still more to be pumped at the current level
            // (But it wasn't in the queue because maybe it's the final source block below the pump head)
            // We still want to be able to pump
            if (pumps > 0 && pumpQueue.queue().isEmpty()) {
                updatePumpQueue(pumpQueue.fluidType());
            }
        }

        // Use energy if any pumps happened at all
        if (pumped) {
            energyContainer.changeEnergy(-GTValues.V[getTier()] * 2);
        }
    }

    public void update() {
        if (getOutputFacingFluids() != null) {
            cache.exportToNearby(getOutputFacingFluids());
        }

        // do not do anything without enough energy supplied
        if (energyContainer.getEnergyStored() < GTValues.V[getTier()] * 2) {
            return;
        }
        // Try to put 5 times as many in the queue as there are pumps in the cycle
        // In practice only EV tier has more than 1 pump per cycle
        // The queue can contain at most the y-levels at the pump head or just the y-level below, so for many oil veins
        // It will not be the ideal size
        boolean advanced = false;
        if (getOffsetTimer() % (getPumpingCycleLength() * 2L) == 0) {
            advanced = canAdvancePumpHead();
        }
        if (!advanced && getOffsetTimer() % getPumpingCycleLength() == 0) {
            pumpCycle();
        }
        if (getOffsetTimer() % (20 * 60) == 0) {
            forbiddenBlocks.clear();
        }
    }

    private int queueSize() {
        return 5 * pumpsPerCycle();
    }

    private float ticksPerPump() {
        // How many ticks pass per pump. This is the ideal amount and thus can be less than 1
        // For LV this is 80/1 = 80
        float tierMultiplier = (float) (1 << (getTier() - 1));
        return PUMP_SPEED_BASE / tierMultiplier;
    }

    private int pumpsPerCycle() {
        // The pumping cycle length can not be less than 20, so to ensure we still have the right amount of pumps
        // We need to compensate with pumps per cycle

        return (int) (getPumpingCycleLength() / ticksPerPump());
    }

    private int getPumpingCycleLength() {
        // For basic pumps this means once every 80 ticks
        // It never pumps more than once every 20 ticks, but pumps more per cycle to compensate
        return Math.max(20, (int) ticksPerPump());
    }

    //////////////////////////////////////
    // ********** Gui ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> cache.getFluidInTank(0).getAmount() + "").setTextColor(-1)
                        .setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(cache.getStorages()[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(new ToggleButtonWidget(7, 53, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (player.isShiftKeyDown()) {
                if (hasFrontFacing() && side != this.getFrontFacing() && isFacingValid(side)) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }
}
