package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.RotationState;
import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.block.IAppearance;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.misc.IOFluidTransferList;
import com.gregtechceu.gtceu.api.misc.IOItemTransferList;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsComponent;
import static com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior.ModeType.BOTH;
import static com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior.ModeType.ITEM;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MetaMachine, an abstract layer of gregtech machine.
 *           Because I have to implement BlockEntities for both fabric and forge platform.
 *           All fundamental features will be implemented here.
 *           To add additional features, you can see {@link IMachineFeature}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetaMachine implements IEnhancedManaged, IToolable, ITickSubscription, IAppearance, IToolGridHighLight,
                         IFancyTooltip, IPaintable, IRedstoneSignalMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MetaMachine.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Getter
    public final IMachineBlockEntity holder;
    @Getter
    @DescSynced
    @Persisted(key = "cover")
    protected final MachineCoverContainer coverContainer;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    @RequireRerender
    private int paintingColor = -1;
    @Getter
    protected final List<MachineTrait> traits;
    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public MetaMachine(IMachineBlockEntity holder) {
        this.holder = holder;
        this.coverContainer = new MachineCoverContainer(this);
        this.traits = new ArrayList<>();
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
        // bind sync storage
        if (holder.getRootStorage() != null) {
            this.holder.getRootStorage().attach(getSyncStorage());
        }
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(this::markDirty);
        }
    }

    public Level getLevel() {
        return holder.level();
    }

    public BlockPos getPos() {
        return holder.pos();
    }

    public BlockState getBlockState() {
        return holder.getSelf().getBlockState();
    }

    public boolean isRemote() {
        return getLevel() == null ? LDLib.isRemote() : getLevel().isClientSide;
    }

    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    public void scheduleRenderUpdate() {
        holder.scheduleRenderUpdate();
    }

    public void scheduleNeighborShapeUpdate() {
        Level level = getLevel();
        BlockPos pos = getPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    public long getOffsetTimer() {
        return holder.getOffsetTimer();
    }

    public void markDirty() {
        holder.getSelf().setChanged();
    }

    public boolean isInValid() {
        return holder.getSelf().isRemoved();
    }

    public void onUnload() {
        traits.forEach(MachineTrait::onMachineUnLoad);
        coverContainer.onUnload();
        for (TickableSubscription serverTick : serverTicks) {
            serverTick.unsubscribe();
        }
        serverTicks.clear();
    }

    public void onLoad() {
        traits.forEach(MachineTrait::onMachineLoad);
        coverContainer.onLoad();
    }

    /**
     * Use for data not able to be saved with the SyncData system, like optional mod compatiblity in internal machines.
     *
     * @param tag     the CompoundTag to load data from
     * @param forDrop if the save is done for dropping the machine as an item.
     */
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        for (MachineTrait trait : this.getTraits()) {
            trait.saveCustomPersistedData(tag, forDrop);
        }
    }

    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        for (MachineTrait trait : this.getTraits()) {
            trait.loadCustomPersistedData(tag);
        }
    }

    //////////////////////////////////////
    // ***** Tickable Manager ****//
    //////////////////////////////////////

    /**
     * For initialization. To get level and property fields after auto sync, you can subscribe it in {@link #onLoad()}
     * event.
     */
    @Nullable
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isRemote()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            var blockState = getBlockState();
            if (!blockState.getValue(BlockProperties.SERVER_TICK)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    blockState = blockState.setValue(BlockProperties.SERVER_TICK, true);
                    holder.getSelf().setBlockState(blockState);
                    serverLevel.getServer().tell(new TickTask(0, () -> {
                        if (!isInValid()) {
                            serverLevel.setBlockAndUpdate(getPos(),
                                    getBlockState().setValue(BlockProperties.SERVER_TICK, true));
                        }
                    }));
                }
            }
            return subscription;
        } else if (getLevel() instanceof DummyWorld) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            return subscription;
        }
        return null;
    }

    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    public final void serverTick() {
        executeTick();
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty() && !isInValid()) {
            getLevel().setBlockAndUpdate(getPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, false));
        }
    }

    public boolean isFirstDummyWorldTick = true;

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (getLevel() instanceof DummyWorld) {
            if (isFirstDummyWorldTick) {
                isFirstDummyWorldTick = false;
                onLoad();
            }
            executeTick();
        }
    }

    private void executeTick() {
        if (!waitingToAdd.isEmpty()) {
            serverTicks.addAll(waitingToAdd);
            waitingToAdd.clear();
        }
        var iter = serverTicks.iterator();
        while (iter.hasNext()) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (isInValid()) break;
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    /**
     * Called when a player clicks this meta tile entity with a tool
     *
     * @return SUCCESS / CONSUME (will damage tool) / FAIL if something happened, so tools will get damaged and
     *         animations will be played
     */
    @Override
    public final Pair<GTToolType, ItemInteractionResult> onToolClick(Set<@NotNull GTToolType> toolType,
                                                                     ItemStack itemStack,
                                                                     UseOnContext context) {
        // the side hit from the machine grid
        var playerIn = context.getPlayer();
        if (playerIn == null) return Pair.of(null, ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);

        var hand = context.getHand();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        CoverBehavior coverBehavior = gridSide == null ? null : coverContainer.getCoverAtSide(gridSide);
        if (gridSide == null) gridSide = hitResult.getDirection();

        // Prioritize covers where they apply (Screwdriver, Soft Mallet)
        if (toolType.contains(GTToolType.SCREWDRIVER)) {
            if (coverBehavior != null) {
                return Pair.of(GTToolType.SCREWDRIVER, coverBehavior.onScrewdriverClick(playerIn, hand, hitResult));
            } else return Pair.of(GTToolType.SCREWDRIVER, onScrewdriverClick(playerIn, hand, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.SOFT_MALLET)) {
            if (coverBehavior != null) {
                return Pair.of(GTToolType.SOFT_MALLET, coverBehavior.onSoftMalletClick(playerIn, hand, hitResult));
            } else return Pair.of(GTToolType.SOFT_MALLET, onSoftMalletClick(playerIn, hand, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.WRENCH)) {
            return Pair.of(GTToolType.WRENCH, onWrenchClick(playerIn, hand, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.CROWBAR)) {
            if (coverBehavior != null) {
                if (!isRemote()) {
                    getCoverContainer().removeCover(gridSide, playerIn);
                }
                return Pair.of(GTToolType.CROWBAR, ItemInteractionResult.CONSUME);
            }
            return Pair.of(GTToolType.CROWBAR, onCrowbarClick(playerIn, hand, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.HARD_HAMMER)) {
            return Pair.of(GTToolType.HARD_HAMMER, onHardHammerClick(playerIn, hand, gridSide, hitResult));
        }
        return Pair.of(null, ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    protected ItemInteractionResult onHardHammerClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                      BlockHitResult hitResult) {
        if (this instanceof IMufflableMachine mufflableMachine) {
            if (!isRemote()) {
                mufflableMachine.setMuffled(!mufflableMachine.isMuffled());
                playerIn.sendSystemMessage(Component.translatable(mufflableMachine.isMuffled() ?
                        "gtceu.machine.muffle.on" : "gtceu.machine.muffle.off"));
            }
            playerIn.swing(hand);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ItemInteractionResult onCrowbarClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ItemInteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                  BlockHitResult hitResult) {
        if (playerIn.isShiftKeyDown()) {
            if (gridSide == getFrontFacing() || !isFacingValid(gridSide)) {
                return ItemInteractionResult.FAIL;
            }
            if (!isRemote()) {
                setFrontFacing(gridSide);
            }
            playerIn.swing(hand);
            return ItemInteractionResult.CONSUME;
        } else {
            if (isRemote())
                return ItemInteractionResult.CONSUME;
            var itemStack = playerIn.getItemInHand(hand);
            var component = getBehaviorsComponent(itemStack);
            ToolModeSwitchBehavior.ModeType type = Optional
                    .ofNullable(component.getBehavior(GTToolBehaviors.MODE_SWITCH))
                    .map(ToolModeSwitchBehavior::getModeType).orElse(BOTH);

            if (type == ToolModeSwitchBehavior.ModeType.ITEM || type == ToolModeSwitchBehavior.ModeType.BOTH) {
                if (this instanceof IAutoOutputItem autoOutputItem &&
                        (!hasFrontFacing() || gridSide != getFrontFacing())) {
                    autoOutputItem.setOutputFacingItems(gridSide);
                }
            }
            if (type == ToolModeSwitchBehavior.ModeType.FLUID || type == ToolModeSwitchBehavior.ModeType.BOTH) {
                if (this instanceof IAutoOutputFluid autoOutputFluid &&
                        (!hasFrontFacing() || gridSide != getFrontFacing())) {
                    autoOutputFluid.setOutputFacingFluids(gridSide);
                }
            }
            playerIn.swing(hand);
            return ItemInteractionResult.CONSUME;
        }
    }

    protected ItemInteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                      BlockHitResult hitResult) {
        var controllable = GTCapabilityHelper.getControllable(getLevel(), getPos(), gridSide);
        if (controllable != null) {
            if (!isRemote()) {
                controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
                playerIn.sendSystemMessage(Component.translatable(controllable.isWorkingEnabled() ?
                        "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"));
            }
            playerIn.swing(hand);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ItemInteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                       BlockHitResult hitResult) {
        var itemStack = playerIn.getItemInHand(hand);
        if (isRemote())
            return ItemInteractionResult.CONSUME;
        if (playerIn.isShiftKeyDown()) {
            boolean flag = false;
            if (this instanceof IAutoOutputItem autoOutputItem) {
                if (autoOutputItem.getOutputFacingItems() == gridSide) {
                    autoOutputItem.setAllowInputFromOutputSideItems(!autoOutputItem.isAllowInputFromOutputSideItems());
                    playerIn.displayClientMessage(Component.translatable("gtceu.machine.basic.input_from_output_side." +
                            (autoOutputItem.isAllowInputFromOutputSideItems() ? "allow" : "disallow"))
                            .append(Component.translatable("gtceu.creative.chest.item")), true);
                    flag = true;
                }
            }
            if (this instanceof IAutoOutputFluid autoOutputFluid) {
                if (autoOutputFluid.getOutputFacingFluids() == gridSide) {
                    autoOutputFluid
                            .setAllowInputFromOutputSideFluids(!autoOutputFluid.isAllowInputFromOutputSideFluids());
                    playerIn.displayClientMessage(Component.translatable("gtceu.machine.basic.input_from_output_side." +
                            (autoOutputFluid.isAllowInputFromOutputSideFluids() ? "allow" : "disallow"))
                            .append(Component.translatable("gtceu.creative.tank.fluid")), true);
                    flag = true;
                }
            }
            if (flag) {
                playerIn.swing(hand);
                return ItemInteractionResult.SUCCESS;
            }
        } else {
            boolean flag = false;
            if (this instanceof IAutoOutputItem autoOutputItem) {
                if (autoOutputItem.getOutputFacingItems() == gridSide) {
                    autoOutputItem.setAutoOutputItems(!autoOutputItem.isAutoOutputItems());
                    flag = true;
                }
            }
            if (this instanceof IAutoOutputFluid autoOutputFluid) {
                if (autoOutputFluid.getOutputFacingFluids() == gridSide) {
                    autoOutputFluid.setAutoOutputFluids(!autoOutputFluid.isAutoOutputFluids());
                    flag = true;

                }
            }
            if (flag) {
                playerIn.swing(hand);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    @Nullable
    public static MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBlockEntity) {
            return machineBlockEntity.getMetaMachine();
        }
        return null;
    }

    /**
     * All traits should be initialized while MetaMachine is creating. you cannot add them on the fly.
     */
    public void attachTraits(MachineTrait trait) {
        traits.add(trait);
    }

    public static void clearInventory(List<ItemStack> itemBuffer, IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                itemBuffer.add(stackInSlot);
            }
        }
    }

    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, Set<GTToolType> toolTypes) {
        if (toolTypes.contains(GTToolType.WRENCH) || toolTypes.contains(GTToolType.SCREWDRIVER)) return true;
        if (toolTypes.contains(GTToolType.HARD_HAMMER) && this instanceof IMufflableMachine) return true;
        for (CoverBehavior cover : coverContainer.getCovers()) {
            if (cover.shouldRenderGrid(player, held, toolTypes)) return true;
        }
        return false;
    }

    @Override
    public ResourceTexture sideTips(Player player, Set<GTToolType> toolTypes, Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (player.isShiftKeyDown()) {
                if (isFacingValid(side)) {
                    return GuiTextures.TOOL_FRONT_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (this instanceof IControllable controllable) {
                return controllable.isWorkingEnabled() ? GuiTextures.TOOL_PAUSE : GuiTextures.TOOL_START;
            }
        } else if (toolTypes.contains(GTToolType.HARD_HAMMER)) {
            if (this instanceof IMufflableMachine mufflableMachine) {
                return mufflableMachine.isMuffled() ? GuiTextures.TOOL_SOUND : GuiTextures.TOOL_MUTE;
            }
        }
        var cover = coverContainer.getCoverAtSide(side);
        if (cover != null) {
            return cover.sideTips(player, toolTypes, side);
        }
        return null;
    }

    public MachineDefinition getDefinition() {
        return holder.getDefinition();
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     */
    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(Shapes.block());
    }

    public boolean canSetIoOnSide(@Nullable Direction direction) {
        return !hasFrontFacing() || getFrontFacing() != direction;
    }

    public Direction getFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getFrontFacing(blockState);
        }
        return Direction.NORTH;
    }

    public final boolean hasFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getRotationState() != RotationState.NONE;
        }
        return false;
    }

    public boolean isFacingValid(Direction facing) {
        if (hasFrontFacing() && facing == getFrontFacing()) return false;
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock) {
            return metaMachineBlock.rotationState.test(facing);
        }
        return false;
    }

    public void setFrontFacing(Direction facing) {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock && isFacingValid(facing)) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(metaMachineBlock.rotationState.property, facing));
        }
    }

    public void onRotated(Direction oldFacing, Direction newFacing) {}

    public int tintColor(int index) {
        // index < -100 => emission if shimmer is installed.
        if (index == 1 || index == -111) {
            return getRealColor();
        }
        return -1;
    }

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        coverContainer.onNeighborChanged(block, fromPos, isMoving);
    }

    public void animateTick(RandomSource random) {}

    @Override
    @NotNull
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         BlockState sourceState, BlockPos sourcePos) {
        var appearance = getCoverContainer().getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        if (appearance != null) return appearance;
        if (this instanceof IMultiPart part && part.isFormed()) {
            appearance = part.getFormedAppearance(sourceState, sourcePos, side);
            if (appearance != null) return appearance;
        }
        return getDefinition().getAppearance().get();
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (side == null) return 0;

        // For some reason, Minecraft requests the output signal from the opposite side...
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side.getOpposite());
        if (cover == null) return 0;

        return cover.getRedstoneSignalOutput();
    }

    @Override
    public boolean canConnectRedstone(@Nullable Direction side) {
        if (side == null) return false;

        // For some reason, Minecraft requests the output signal from the opposite side...
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        if (cover == null) return false;

        return cover.canConnectRedstone();
    }

    //////////////////////////////////////
    // ****** Capability ********//
    //////////////////////////////////////

    protected Predicate<ItemStack> getItemCapFilter(@Nullable Direction side) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof ItemFilterCover filterCover) {
                return filterCover.getItemFilter();
            }
        }
        return item -> true;
    }

    protected Predicate<FluidStack> getFluidCapFilter(@Nullable Direction side) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof FluidFilterCover filterCover) {
                return filterCover.getFluidFilter();
            }
        }
        return fluid -> true;
    }

    @Nullable
    public IItemHandlerModifiable getItemTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        var list = getTraits().stream()
                .filter(IItemHandlerModifiable.class::isInstance)
                .filter(t -> t.hasCapability(side))
                .map(IItemHandlerModifiable.class::cast)
                .toList();

        if (list.isEmpty()) return null;

        var io = IO.BOTH;
        if (side != null && this instanceof IAutoOutputItem autoOutput && autoOutput.getOutputFacingItems() == side &&
                !autoOutput.isAllowInputFromOutputSideItems()) {
            io = IO.OUT;
        }

        IOItemTransferList transferList = new IOItemTransferList(list, io, getItemCapFilter(side));
        if (!useCoverCapability || side == null) return transferList;

        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        return cover != null ? cover.getItemTransferCap(transferList) : transferList;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidTransferCap(@Nullable Direction side, boolean useCoverCapability) {
        var list = getTraits().stream()
                .filter(IFluidHandler.class::isInstance)
                .filter(t -> t.hasCapability(side))
                .map(IFluidHandler.class::cast)
                .toList();

        if (list.isEmpty()) return null;

        var io = IO.BOTH;
        if (side != null && this instanceof IAutoOutputFluid autoOutput && autoOutput.getOutputFacingFluids() == side &&
                !autoOutput.isAllowInputFromOutputSideFluids()) {
            io = IO.OUT;
        }

        IOFluidTransferList transferList = new IOFluidTransferList(list, io, getFluidCapFilter(side));
        if (!useCoverCapability || side == null) return transferList;

        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        return cover != null ? cover.getFluidTransferCap(transferList) : transferList;
    }

    //////////////////////////////////////
    // ******** GUI *********//
    //////////////////////////////////////
    @Override
    public IGuiTexture getFancyTooltipIcon() {
        return GuiTextures.INFO_ICON;
    }

    @Override
    public final List<Component> getFancyTooltip() {
        var tooltips = new ArrayList<Component>();
        onAddFancyInformationTooltip(tooltips);
        return tooltips;
    }

    @Override
    public boolean showFancyTooltip() {
        return !getFancyTooltip().isEmpty();
    }

    public void onAddFancyInformationTooltip(List<Component> tooltips) {
        getDefinition().getTooltipBuilder().accept(getDefinition().asStack(), tooltips);
        String mainKey = String.format("%s.machine.%s.tooltip", getDefinition().getId().getNamespace(),
                getDefinition().getId().getPath());
        if (LocalizationUtils.exist(mainKey)) {
            tooltips.add(0, Component.translatable(mainKey));
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return getDefinition().getDefaultPaintingColor();
    }
}
