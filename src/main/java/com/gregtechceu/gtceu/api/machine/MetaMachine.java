package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.block.IAppearance;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
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
import com.gregtechceu.gtceu.api.misc.IOFilteredInvWrapper;
import com.gregtechceu.gtceu.api.misc.IOFluidHandlerList;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.data.item.GTItemAbilities;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
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

import java.util.*;
import java.util.function.Predicate;

public class MetaMachine implements IEnhancedManaged, IToolable, ITickSubscription, IAppearance, IToolGridHighLight,
                         IFancyTooltip, IPaintable, IRedstoneSignalMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MetaMachine.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Setter
    @Getter
    @Persisted
    @DescSynced
    @Nullable
    private UUID ownerUUID;
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
        this.holder.getRootStorage().attach(getSyncStorage());
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

    public @Nullable Level getLevel() {
        return holder.level();
    }

    public BlockPos getPos() {
        return holder.pos();
    }

    public BlockState getBlockState() {
        return holder.getSelf().getBlockState();
    }

    public boolean isRemote() {
        return getLevel() == null ? GTCEu.isClientThread() : getLevel().isClientSide;
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

    public void applyImplicitComponents(MetaMachineBlockEntity.ExDataComponentInput componentInput) {}

    public void collectImplicitComponents(DataComponentMap.Builder components) {}

    public void removeItemComponentsFromTag(CompoundTag tag) {}

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
                                                                     ItemStack itemStack, UseOnContext context) {
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
        if (toolType.isEmpty() && playerIn.isShiftKeyDown()) {
            if (coverBehavior != null) {
                return Pair.of(null, coverBehavior.onScrewdriverClick(playerIn, hand, itemStack, hitResult));
            }
        }
        if (toolType.contains(GTToolType.SCREWDRIVER) &&
                itemStack.canPerformAction(GTItemAbilities.SCREWDRIVER_CONFIGURE)) {
            if (coverBehavior != null) {
                return Pair.of(GTToolType.SCREWDRIVER,
                        coverBehavior.onScrewdriverClick(playerIn, hand, itemStack, hitResult));
            } else return Pair.of(GTToolType.SCREWDRIVER,
                    onScrewdriverClick(playerIn, hand, itemStack, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.SOFT_MALLET)) {
            if (coverBehavior != null) {
                return Pair.of(GTToolType.SOFT_MALLET,
                        coverBehavior.onSoftMalletClick(playerIn, hand, itemStack, hitResult));
            } else return Pair.of(GTToolType.SOFT_MALLET,
                    onSoftMalletClick(playerIn, hand, itemStack, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.WRENCH)) {
            return Pair.of(GTToolType.WRENCH, onWrenchClick(playerIn, hand, itemStack, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.CROWBAR)) {
            if (coverBehavior != null && itemStack.canPerformAction(GTItemAbilities.CROWBAR_REMOVE_COVER)) {
                if (!isRemote()) {
                    getCoverContainer().removeCover(gridSide, playerIn);
                }
                return Pair.of(GTToolType.CROWBAR, ItemInteractionResult.CONSUME);
            }
            return Pair.of(GTToolType.CROWBAR, onCrowbarClick(playerIn, hand, itemStack, gridSide, hitResult));
        } else if (toolType.contains(GTToolType.HARD_HAMMER)) {
            return Pair.of(GTToolType.HARD_HAMMER, onHardHammerClick(playerIn, hand, itemStack, gridSide, hitResult));
        }
        return Pair.of(null, ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    protected ItemInteractionResult onHardHammerClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                      Direction gridSide, BlockHitResult hitResult) {
        if (!held.canPerformAction(GTItemAbilities.HAMMER_MUTE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
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

    protected ItemInteractionResult onCrowbarClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                   Direction gridSide, BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ItemInteractionResult onWrenchClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                  Direction gridSide, BlockHitResult hitResult) {
        boolean isShiftDown = playerIn.isShiftKeyDown();
        if (held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
            if (gridSide == getFrontFacing() && allowExtendedFacing()) {
                setUpwardsFacing(isShiftDown ? getUpwardsFacing().getCounterClockWise() :
                        getUpwardsFacing().getClockWise());
                playerIn.swing(hand);
                return ItemInteractionResult.CONSUME;
            }
            if (isShiftDown) {
                if (gridSide == getFrontFacing() || !isFacingValid(gridSide)) {
                    return ItemInteractionResult.FAIL;
                }
                if (!isRemote()) {
                    setFrontFacing(gridSide);
                }
                playerIn.swing(hand);
                return ItemInteractionResult.CONSUME;
            }
        }
        if (!isShiftDown && held.canPerformAction(GTItemAbilities.WRENCH_CONFIGURE)) {
            if (isRemote()) return ItemInteractionResult.CONSUME;

            boolean canConfigAll = held.canPerformAction(GTItemAbilities.WRENCH_CONFIGURE_ALL);

            if (canConfigAll || held.canPerformAction(GTItemAbilities.WRENCH_CONFIGURE_ITEMS)) {
                if (this instanceof IAutoOutputItem autoOutputItem &&
                        (!hasFrontFacing() || gridSide != getFrontFacing())) {
                    autoOutputItem.setOutputFacingItems(gridSide);
                }
            }
            if (canConfigAll || held.canPerformAction(GTItemAbilities.WRENCH_CONFIGURE_FLUIDS)) {
                if (this instanceof IAutoOutputFluid autoOutputFluid &&
                        (!hasFrontFacing() || gridSide != getFrontFacing())) {
                    autoOutputFluid.setOutputFacingFluids(gridSide);
                }
            }
            playerIn.swing(hand);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ItemInteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                      Direction gridSide, BlockHitResult hitResult) {
        if (!held.canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        var controllable = GTCapabilityHelper.getControllable(getLevel(), getPos(), gridSide);
        if (controllable != null) {
            if (!isRemote()) {
                if (!playerIn.isShiftKeyDown() || !controllable.isWorkingEnabled()) {
                    controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
                    playerIn.sendSystemMessage(Component.translatable(controllable.isWorkingEnabled() ?
                            "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"));
                } else {
                    controllable.setSuspendAfterFinish(true);
                    playerIn.sendSystemMessage(Component.translatable("behaviour.soft_hammer.idle_after_cycle"));
                }
            }
            playerIn.swing(hand);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected ItemInteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, ItemStack held,
                                                       Direction gridSide, BlockHitResult hitResult) {
        if (isRemote())
            return ItemInteractionResult.CONSUME;
        if (playerIn.isShiftKeyDown()) {
            boolean flag = false;
            if (this instanceof IAutoOutputItem autoOutputItem) {
                if (autoOutputItem.getOutputFacingItems() == gridSide) {
                    autoOutputItem.setAllowInputFromOutputSideItems(!autoOutputItem.isAllowInputFromOutputSideItems());
                    playerIn.displayClientMessage(Component
                            .translatable("gtceu.machine.basic.input_from_output_side." +
                                    (autoOutputItem.isAllowInputFromOutputSideItems() ? "allow" : "disallow"))
                            .append(Component.translatable("gtceu.creative.chest.item")), true);
                    flag = true;
                }
            }
            if (this instanceof IAutoOutputFluid autoOutputFluid) {
                if (autoOutputFluid.getOutputFacingFluids() == gridSide) {
                    autoOutputFluid
                            .setAllowInputFromOutputSideFluids(!autoOutputFluid.isAllowInputFromOutputSideFluids());
                    playerIn.displayClientMessage(Component
                            .translatable("gtceu.machine.basic.input_from_output_side." +
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

    public void clearInventory(IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                Block.popResource(getLevel(), getPos(), stackInSlot);
            }
        }
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        if (toolTypes.contains(GTToolType.WRENCH) || held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
            return true;
        }
        if ((toolTypes.contains(GTToolType.SCREWDRIVER) ||
                held.canPerformAction(GTItemAbilities.SCREWDRIVER_CONFIGURE)) &&
                (this instanceof IAutoOutputItem || this instanceof IAutoOutputFluid))
            return true;
        for (CoverBehavior cover : coverContainer.getCovers()) {
            if (cover.shouldRenderGrid(player, pos, state, held, toolTypes)) return true;
        }
        return false;
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state,
                                              Set<GTToolType> toolTypes, ItemStack held, Direction side) {
        var cover = coverContainer.getCoverAtSide(side);
        if (cover != null) {
            var tips = cover.sideTips(player, pos, state, toolTypes, held, side);
            if (tips != null) return tips;
        }

        if (toolTypes.contains(GTToolType.WRENCH) || held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
            if (player.isShiftKeyDown()) {
                if (isFacingValid(side)) {
                    return GuiTextures.TOOL_FRONT_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET) || held.canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            if (this instanceof IControllable controllable) {
                return controllable.isWorkingEnabled() ? GuiTextures.TOOL_START : GuiTextures.TOOL_PAUSE;
            }
        } else if (toolTypes.contains(GTToolType.HARD_HAMMER) || held.canPerformAction(GTItemAbilities.HAMMER_MUTE)) {
            if (this instanceof IMufflableMachine mufflableMachine) {
                return mufflableMachine.isMuffled() ? GuiTextures.TOOL_SOUND : GuiTextures.TOOL_MUTE;
            }
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

    public static @NotNull Direction getFrontFacing(@Nullable MetaMachine machine) {
        return machine == null ? Direction.NORTH : machine.getFrontFacing();
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
        if (allowExtendedFacing()) {
            return true;
        }
        if (hasFrontFacing() && facing == getFrontFacing()) return false;
        var coverContainer = getCoverContainer();
        if (coverContainer.hasCover(facing)) {
            // noinspection DataFlowIssue
            var coverDefinition = coverContainer.getCoverAtSide(facing).coverDefinition;
            var behaviour = coverDefinition.createCoverBehavior(coverContainer, getFrontFacing());
            if (!behaviour.canAttach()) {
                return false;
            }
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock) {
            return metaMachineBlock.rotationState.test(facing);
        }
        return false;
    }

    public void setFrontFacing(Direction facing) {
        var oldFacing = getFrontFacing();

        if (allowExtendedFacing()) {
            var newUpwardsFacing = RelativeDirection.simulateAxisRotation(facing, oldFacing, getUpwardsFacing());
            setUpwardsFacing(newUpwardsFacing);
        }

        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock && isFacingValid(facing)) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(metaMachineBlock.rotationState.property, facing));
        }

        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
        }
    }

    public static @NotNull Direction getUpwardFacing(@Nullable MetaMachine machine) {
        return machine == null || !machine.allowExtendedFacing() ? Direction.NORTH :
                machine.getBlockState().getValue(IMachineBlock.UPWARDS_FACING_PROPERTY);
    }

    public Direction getUpwardsFacing() {
        return this.allowExtendedFacing() ? this.getBlockState().getValue(IMachineBlock.UPWARDS_FACING_PROPERTY) :
                Direction.NORTH;
    }

    public void setUpwardsFacing(@NotNull Direction upwardsFacing) {
        if (!getDefinition().isAllowExtendedFacing()) {
            return;
        }
        if (upwardsFacing.getAxis() == Direction.Axis.Y) {
            GTCEu.LOGGER.error("Tried to set upwards facing to invalid facing {}! Skipping", upwardsFacing);
            return;
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock &&
                blockState.getValue(IMachineBlock.UPWARDS_FACING_PROPERTY) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                markDirty();
            }
        }
    }

    public void onRotated(Direction oldFacing, Direction newFacing) {}

    public boolean allowExtendedFacing() {
        return getDefinition().isAllowExtendedFacing();
    }

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
                                         @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
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
    // ****** Ownership ********//
    //////////////////////////////////////

    public @Nullable MachineOwner getOwner() {
        return MachineOwner.getOwner(ownerUUID);
    }

    public @Nullable PlayerOwner getPlayerOwner() {
        return MachineOwner.getPlayerOwner(ownerUUID);
    }

    //////////////////////////////////////
    // ****** Capability ********//
    //////////////////////////////////////

    public Predicate<ItemStack> getItemCapFilter(@Nullable Direction side, IO io) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof ItemFilterCover filterCover && filterCover.getFilterMode().filters(io)) {
                return filterCover.getItemFilter();
            }
        }
        return item -> true;
    }

    public Predicate<FluidStack> getFluidCapFilter(@Nullable Direction side, IO io) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof FluidFilterCover filterCover && filterCover.getFilterMode().filters(io)) {
                return filterCover.getFluidFilter();
            }
        }
        return fluid -> true;
    }

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
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

        IOFilteredInvWrapper handlerList = new IOFilteredInvWrapper(list, io,
                getItemCapFilter(side, IO.IN), getItemCapFilter(side, IO.OUT));
        if (!useCoverCapability || side == null) return handlerList;

        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        return cover != null ? cover.getItemHandlerCap(handlerList) : handlerList;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
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

        IOFluidHandlerList handlerList = new IOFluidHandlerList(list, io, getFluidCapFilter(side, IO.IN),
                getFluidCapFilter(side, IO.OUT));
        if (!useCoverCapability || side == null) return handlerList;

        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        return cover != null ? cover.getFluidHandlerCap(handlerList) : handlerList;
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
