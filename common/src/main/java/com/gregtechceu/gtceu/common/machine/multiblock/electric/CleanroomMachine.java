package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomLogic;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.*;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanroomMachine extends WorkableElectricMultiblockMachine implements ICleanroomProvider, IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CleanroomMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final int CLEAN_AMOUNT_THRESHOLD = 90;
    public static final int MIN_CLEAN_AMOUNT = 0;

    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 4;

    @Persisted
    private int lDist = 0, rDist = 0, bDist = 0, fDist = 0, hDist = 0;
    @Nullable
    private CleanroomType cleanroomType = null;
    @Persisted
    private int cleanAmount;
    //runtime
    @Getter @Nullable
    private EnergyContainerList inputEnergyContainers;
    @Getter @Nullable
    private Collection<ICleanroomReceiver> cleanroomReceivers;


    public CleanroomMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);
    }

    //////////////////////////////////////
    //******    Initialization    ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new CleanroomLogic(this);
    }

    @Override
    @Nonnull
    public CleanroomLogic getRecipeLogic() {
        return (CleanroomLogic) super.getRecipeLogic();
    }

    //////////////////////////////////////
    //***    Multiblock LifeCycle    ***//
    //////////////////////////////////////

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        initializeAbilities();
        IFilterType filterType = getMultiblockState().getMatchContext().get("FilterType");
        if (filterType != null) {
            this.cleanroomType = filterType.getCleanroomType();
        } else {
            this.cleanroomType = CleanroomType.CLEANROOM;
        }

        // bind cleanroom
        if (cleanroomReceivers != null) {
            this.cleanroomReceivers.forEach(receiver -> receiver.setCleanroom(null));
            this.cleanroomReceivers = null;
        }
        Set<ICleanroomReceiver> receivers = getMultiblockState().getMatchContext().getOrCreate("cleanroomReceiver", Sets::newHashSet);
        this.cleanroomReceivers = ImmutableSet.copyOf(receivers);
        this.cleanroomReceivers.forEach(receiver -> receiver.setCleanroom(this));

        // max progress is based on the dimensions of the structure: (x^3)-(x^2)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        this.getRecipeLogic().setDuration((Math.max(100, ((lDist + rDist + 1) * (bDist + fDist + 1) * hDist) - ((lDist + rDist + 1) * (bDist + fDist + 1)))));
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.inputEnergyContainers = null;
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        if (cleanroomReceivers != null) {
            this.cleanroomReceivers.forEach(receiver -> receiver.setCleanroom(null));
            this.cleanroomReceivers = null;
        }
    }

    @Override
    public boolean shouldAddPartToController(IMultiPart part) {
        var cache = getMultiblockState().getCache();
        for (Direction side : Direction.values()) {
            if (!cache.contains(part.self().getPos().relative(side))) {
                return true;
            }
        }
        return false;
    }

    protected void initializeAbilities() {
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE || io == IO.OUT) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }
            }
            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                getRecipeLogic().setMaintenanceMachine(maintenanceMachine);
            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
        getRecipeLogic().setEnergyContainer(this.inputEnergyContainers);
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public void updateStructureDimensions() {
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
            return;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.fDist = fDist;
        this.hDist = hDist;
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
        TraceabilityPredicate basePredicate = Predicates.autoAbilities(true, false, false)
                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3));

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
                .where('F', Predicates.cleanroomFilters())
                .where(' ', innerPredicate())
                .build();
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
            Set<ICleanroomReceiver> receivers = blockWorldState.getMatchContext().getOrCreate("cleanroomReceiver", Sets::newHashSet);
            // all non-GTMachines are allowed inside by default
            BlockEntity blockEntity = blockWorldState.getTileEntity();
            if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
                var machine = machineBlockEntity.getMetaMachine();
                if (isMachineBanned(machine)) {
                    return false;
                }
            }
            if (blockEntity != null) {
                var receiver = GTCapabilityHelper.getCleanroomReceiver(blockWorldState.getWorld(), blockWorldState.getPos(), null);
                if (receiver != null) {
                    receivers.add(receiver);
                }
            }
            return true;
        }, null) {
            @Override
            public boolean isAny() {
                return true;
            }

            @Override
            public boolean addCache() {
                return true;
            }
        };
    }

    protected boolean isMachineBanned(MetaMachine metaTileEntity) {
        // blacklisted machines: mufflers and all generators, miners/drills, primitives
        if (metaTileEntity instanceof ICleanroomProvider) return true;
        if (metaTileEntity instanceof IMufflerMachine) return true;
        if (metaTileEntity instanceof SimpleGeneratorMachine) return true;
        // todo: enable checks when these are added?
//        if (metaTileEntity instanceof FuelMultiblockController) return true;
//        if (metaTileEntity instanceof MetaTileEntityLargeMiner) return true;
//        if (metaTileEntity instanceof MetaTileEntityFluidDrill) return true;
//        if (metaTileEntity instanceof MetaTileEntityCentralMonitor) return true;
        if (metaTileEntity instanceof CokeOvenMachine) return true;
        if (metaTileEntity instanceof PrimitiveBlastFurnaceMachine) return true;
        return metaTileEntity instanceof PrimitivePumpMachine;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            var maxVoltage = getMaxVoltage();
            if (maxVoltage > 0) {
                String voltageName = GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (cleanroomType != null) {
                textList.add(Component.translatable(cleanroomType.getTranslationKey()));
            }

            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
                textList.add(Component.translatable("gtceu.multiblock.progress", currentProgress));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.waiting").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }

            if (isClean()) textList.add(Component.translatable("gtceu.multiblock.cleanroom.clean_state"));
            else textList.add(Component.translatable("gtceu.multiblock.cleanroom.dirty_state"));
            textList.add(Component.translatable("gtceu.multiblock.cleanroom.clean_amount", this.cleanAmount));
        } else {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip").withStyle(ChatFormatting.GRAY);
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        }
    }

    @Override
    public Set<CleanroomType> getTypes() {
        return this.cleanroomType == null ? Set.of() : Set.of(this.cleanroomType);
    }

    /**
     * Adjust the cleanroom's clean amount
     *
     * @param amount the amount of cleanliness to increase/decrease by
     */
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
    public long getMaxVoltage() {
        if (inputEnergyContainers == null) return GTValues.LV;
        return inputEnergyContainers.getInputVoltage();
    }

}
