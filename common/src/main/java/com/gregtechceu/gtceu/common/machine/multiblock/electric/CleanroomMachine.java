package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.impl.CleanroomLogic;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.*;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.common.block.CleanroomCasingType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class CleanroomMachine extends WorkableMultiblockMachine implements ICleanroomProvider, IDisplayUIMachine {

    public static final int CLEAN_AMOUNT_THRESHOLD = 90;
    public static final int MIN_CLEAN_AMOUNT = 0;

    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 4;

    @Persisted @DescSynced
    private int lDist = 0;
    @Persisted @DescSynced
    private int rDist = 0;
    @Persisted @DescSynced
    private int bDist = 0;
    @Persisted @DescSynced
    private int fDist = 0;
    @Persisted @DescSynced
    private int hDist = 0;

    private CleanroomType cleanroomType = null;
    @Persisted @DescSynced
    private int cleanAmount;

    @Persisted @DescSynced
    private IEnergyContainer energyContainer;

    @Nullable
    protected TickableSubscription serverTickSubs;

    @Persisted @DescSynced
    private final CleanroomLogic cleanroomLogic;
    private final Collection<ICleanroomReceiver> cleanroomReceivers = new HashSet<>();

    public CleanroomMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);
        this.cleanroomLogic = new CleanroomLogic(this, GTValues.LV);
        this.energyContainer = new EnergyContainerList(List.of());
    }

    protected void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : parts) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE || io == IO.OUT) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
    }

    private void resetTileAbilities() {
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return false;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        initializeAbilities();
        CleanroomCasingType casingType = getMultiblockState().getMatchContext().get("FilterType");
        if (casingType.equals(CleanroomCasingType.FILTER_CASING)) {
            this.cleanroomType = CleanroomType.CLEANROOM;
        } else if (casingType.equals(CleanroomCasingType.FILTER_CASING_STERILE)) {
            this.cleanroomType = CleanroomType.STERILE_CLEANROOM;
        }

        // max progress is based on the dimensions of the structure: (x^3)-(x^2)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        this.cleanroomLogic.setMaxProgress(Math.max(100, ((lDist + rDist + 1) * (bDist + fDist + 1) * hDist) - ((lDist + rDist + 1) * (bDist + fDist + 1))));
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        resetTileAbilities();
        this.cleanroomLogic.invalidate();
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        cleanroomReceivers.forEach(receiver -> receiver.setCleanroom(null));
        cleanroomReceivers.clear();
    }

    protected void tickServer() {
        if (!getLevel().isClientSide && this.isFormed() && this.cleanroomLogic.getMaxProgress() > 0) {
            this.cleanroomLogic.updateLogic();
            if (this.cleanroomLogic.wasActiveAndNeedsUpdate()) {
                this.cleanroomLogic.setWasActiveAndNeedsUpdate(false);
                this.cleanroomLogic.setActive(false);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(serverTickSubs, this::tickServer);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (serverTickSubs != null) {
            serverTickSubs.unsubscribe();
            serverTickSubs = null;
        }
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public boolean updateStructureDimensions() {
        Level world = getLevel();
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = getPos().mutable();
        BlockPos.MutableBlockPos rPos = getPos().mutable();
        BlockPos.MutableBlockPos fPos = getPos().mutable();
        BlockPos.MutableBlockPos bPos = getPos().mutable();
        BlockPos.MutableBlockPos hPos = getPos().mutable();

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < 8; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (fDist == 0 && isBlockEdge(world, fPos, front)) fDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0) break;
        }

        // height is diameter instead of radius, so it needs to be done separately
        for (int i = 1; i < 15; i++) {
            if (isBlockFloor(world, hPos, Direction.DOWN)) hDist = i;
            if (hDist != 0) break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || fDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            this.isFormed = false;
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.fDist = fDist;
        this.hDist = hDist;
        return true;
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@Nonnull Level world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull Direction direction) {
        return world.getBlockState(pos.move(direction)) == GTBlocks.PLASTCRETE.getDefaultState();
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid floor block at pos moved in direction
     */
    public boolean isBlockFloor(@Nonnull Level world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull Direction direction) {
        return isBlockEdge(world, pos, direction) || world.getBlockState(pos) == GTBlocks.CLEANROOM_GLASS.getDefaultState();
    }

    @Nonnull
    @Override
    public BlockPattern getPattern() {
        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getLevel() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI
        if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
        if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
        if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
        if (fDist < MIN_RADIUS) fDist = MIN_RADIUS;
        if (hDist < MIN_DEPTH) hDist = MIN_DEPTH;

        if (this.getFrontFacing() == Direction.EAST || this.getFrontFacing() == Direction.WEST) {
            int tmp = lDist;
            lDist = rDist;
            rDist = tmp;
        }

        // build each row of the structure
        StringBuilder borderBuilder = new StringBuilder();     // BBBBB
        StringBuilder wallBuilder = new StringBuilder();       // BXXXB
        StringBuilder insideBuilder = new StringBuilder();     // X   X
        StringBuilder roofBuilder = new StringBuilder();       // BFFFB
        StringBuilder controllerBuilder = new StringBuilder(); // BFSFB
        StringBuilder centerBuilder = new StringBuilder();     // BXKXB

        // everything to the left of the controller
        for (int i = 0; i < lDist; i++) {
            borderBuilder.append("B");
            if (i == 0) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        // everything in-line with the controller
        borderBuilder.append("B");
        wallBuilder.append("X");
        insideBuilder.append(" ");
        roofBuilder.append("F");
        controllerBuilder.append("S");
        centerBuilder.append("K");

        // everything to the right of the controller
        for (int i = 0; i < rDist; i++) {
            borderBuilder.append("B");
            if (i == rDist - 1) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        // build each slice of the structure
        String[] wall = new String[hDist + 1]; // "BBBBB", "BXXXB", "BXXXB", "BXXXB", "BBBBB"
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = borderBuilder.toString();
        wall[wall.length - 1] = borderBuilder.toString();

        String[] slice = new String[hDist + 1]; // "BXXXB", "X   X", "X   X", "X   X", "BFFFB"
        Arrays.fill(slice, insideBuilder.toString());
        slice[0] = wallBuilder.toString();
        slice[slice.length - 1] = roofBuilder.toString();

        String[] center = Arrays.copyOf(slice, slice.length); // "BXKXB", "X   X", "X   X", "X   X", "BFSFB"
        if (this.getFrontFacing() == Direction.NORTH || this.getFrontFacing() == Direction.SOUTH) {
            center[0] = centerBuilder.reverse().toString();
            center[center.length - 1] = controllerBuilder.reverse().toString();
        } else {
            center[0] = centerBuilder.toString();
            center[center.length - 1] = controllerBuilder.toString();
        }

        TraceabilityPredicate wallPredicate = states(getCasingState(), getGlassState());
        TraceabilityPredicate basePredicate = Predicates.autoAbilities(this).or(abilities(PartAbility.INPUT_ENERGY)
                .setMinGlobalLimited(1).setMaxGlobalLimited(3));

        // layer the slices one behind the next
        return FactoryBlockPattern.start()
                .aisle(wall)
                .aisle(slice).setRepeatable(bDist - 1)
                .aisle(center)
                .aisle(slice).setRepeatable(fDist - 1)
                .aisle(wall)
                .where('S', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .where('B', states(getCasingState()).or(basePredicate))
                .where('X', wallPredicate.or(basePredicate)
                        .or(doorPredicate().setMaxGlobalLimited(8))
                        .or(abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30)))
                .where('K', wallPredicate) // the block beneath the controller must only be a casing for structure dimension checks
                .where('F', filterPredicate())
                .where(' ', innerPredicate())
                .build();
    }

    @Nonnull
    protected TraceabilityPredicate filterPredicate() {
        return new TraceabilityPredicate(blockLevelState -> {
            BlockState blockState = blockLevelState.getBlockState();
            Block block = blockState.getBlock();
            CleanroomCasingType casingType = CleanroomCasingType.getFromBlock(block);
            if (casingType == null || casingType.equals(CleanroomCasingType.PLASCRETE)) return false;

            Object currentFilter = blockLevelState.getMatchContext().getOrPut("FilterType", casingType);
            if (!currentFilter.toString().equals(casingType.getSerializedName())) {
                blockLevelState.setError(new PatternStringError("gtceu.multiblock.pattern.error.filters"));
                return false;
            }
            blockLevelState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockLevelState.getPos());
            return true;
        }, () -> Arrays.stream(CleanroomCasingType.values())
                .filter(type -> !type.equals(CleanroomCasingType.PLASCRETE))
                .map(type -> new BlockInfo(CleanroomCasingType.getFromType(type)))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.filters"));
    }

    // protected to allow easy addition of addon "cleanrooms"
    @Nonnull
    protected BlockState getCasingState() {
        return GTBlocks.PLASTCRETE.getDefaultState();
    }

    @Nonnull
    protected BlockState getGlassState() {
        return GTBlocks.CLEANROOM_GLASS.getDefaultState();
    }

    @Nonnull
    protected static TraceabilityPredicate doorPredicate() {
        return Predicates.custom(blockWorldState -> blockWorldState.getBlockState().getBlock() instanceof DoorBlock,
                () -> new BlockInfo[] {new BlockInfo(Blocks.IRON_DOOR.defaultBlockState()), new BlockInfo(Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER))});
    }

    @Nonnull
    protected TraceabilityPredicate innerPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            // all non-GTMachines are allowed inside by default
            BlockEntity tileEntity = blockWorldState.getTileEntity();
            if (!(tileEntity instanceof IMachineBlockEntity machineBlockEntity)) return true;

            MetaMachine metaTileEntity = machineBlockEntity.getMetaMachine();

            // always ban other cleanrooms, can cause problems otherwise
            if (metaTileEntity instanceof ICleanroomProvider)
                return false;

            if (isMachineBanned(metaTileEntity))
                return false;

            // the machine does not need a cleanroom, so do nothing more
            if (!(metaTileEntity instanceof ICleanroomReceiver cleanroomReceiver)) return true;

            // give the machine this cleanroom if it doesn't have this one
            if (cleanroomReceiver.getCleanroom() != this) {
                cleanroomReceiver.setCleanroom(this);
                cleanroomReceivers.add(cleanroomReceiver);
            }
            return true;
        }, null);
    }

    protected boolean isMachineBanned(MetaMachine metaTileEntity) {
        // blacklisted machines: mufflers and all generators, miners/drills, primitives
        if (metaTileEntity instanceof IMufflerMachine) return true;
        if (metaTileEntity instanceof SimpleGeneratorMachine) return true;
        // todo: enable checks when these are added?
//        if (metaTileEntity instanceof FuelMultiblockController) return true;
//        if (metaTileEntity instanceof MetaTileEntityLargeMiner) return true;
//        if (metaTileEntity instanceof MetaTileEntityFluidDrill) return true;
//        if (metaTileEntity instanceof MetaTileEntityCentralMonitor) return true;
        if (metaTileEntity instanceof CleanroomMachine) return true;
        if (metaTileEntity instanceof CokeOvenMachine) return true;
        if (metaTileEntity instanceof PrimitiveBlastFurnaceMachine) return true;
        return metaTileEntity instanceof PrimitivePumpMachine;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VNF[GTUtil.getTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (!cleanroomLogic.isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            } else if (cleanroomLogic.isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                textList.add(Component.translatable("gtceu.multiblock.progress", getProgressPercent()));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (!drainEnergy(true)) {
                textList.add(Component.translatable("gtceu.multiblock.not_enough_energy").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }

            if (isClean()) textList.add(Component.translatable("gtceu.multiblock.cleanroom.clean_state"));
            else textList.add(Component.translatable("gtceu.multiblock.cleanroom.dirty_state"));
            textList.add(Component.translatable("gtceu.multiblock.cleanroom.clean_amount", this.cleanAmount));
        }
    }

    @Override
    public Set<CleanroomType> getTypes() {
        return Set.of(this.cleanroomType);
    }

    @Override
    public void setCleanAmount(int amount) {
        this.cleanAmount = amount;
    }

    @Override
    public void adjustCleanAmount(int amount) {
        // do not allow negative cleanliness nor cleanliness above 100
        this.cleanAmount = Mth.clamp(this.cleanAmount + amount, 0, 100);
    }

    @Override
    public boolean isClean() {
        return this.cleanAmount >= CLEAN_AMOUNT_THRESHOLD;
    }

    @Nonnull
    public List<Component> getDataInfo() {
        return Collections.singletonList(Component.translatable(isClean() ? "gtceu.multiblock.cleanroom.clean_state" : "gtceu.multiblock.cleanroom.dirty_state"));
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.cleanroomLogic.isActive();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.cleanroomLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        if (!isActivationAllowed) // pausing sets not clean
            setCleanAmount(MIN_CLEAN_AMOUNT);
        this.cleanroomLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public int getProgress() {
        return cleanroomLogic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return cleanroomLogic.getMaxProgress();
    }

    public int getProgressPercent() {
        return cleanroomLogic.getProgressPercent();
    }

    @Override
    public int getEnergyTier() {
        if (energyContainer == null) return GTValues.LV;
        return Math.max(GTValues.LV, GTUtil.getFloorTierByVoltage(energyContainer.getInputVoltage()));
    }

    @Override
    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = isClean() ? (long) Math.min(4, Math.pow(4, getEnergyTier())) : GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }
}
