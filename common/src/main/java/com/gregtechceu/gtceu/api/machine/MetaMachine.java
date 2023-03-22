package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighLight;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.misc.IOFluidTransferList;
import com.gregtechceu.gtceu.api.misc.IOItemTransferList;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.msic.FluidTransferList;
import com.lowdragmc.lowdraglib.msic.ItemTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MetaMachine, an abstract layer of gregtech machine.
 * Because I have to implement BlockEntities for both fabric and forge platform.
 * All fundamental features will be implemented here.
 * To add additional features, you can see {@link IMachineFeature}
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetaMachine implements IManaged, IToolable, ITickSubscription, IToolGridHighLight {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MetaMachine.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Getter
    public final IMetaMachineBlockEntity holder;
    @Getter
    @DescSynced
    @Persisted(key = "cover")
    protected final MachineCoverContainer coverContainer;
    @Getter
    protected final List<MachineTrait> traits;
    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public MetaMachine(IMetaMachineBlockEntity holder) {
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
    //*****     Initialization    ******//
    //////////////////////////////////////
    protected void scheduleRender(String fieldName, Object oldName, Object newName) {
        scheduleRenderUpdate();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        markDirty();
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

    //////////////////////////////////////
    //*****     Tickable Manager    ****//
    //////////////////////////////////////

    /**
     * For initialization. To get level and property fields after auto sync, you can subscribe it in {@link #onLoad()} event.
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
                            serverLevel.setBlockAndUpdate(getPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, true));
                        }
                    }));
                }
            }
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
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty() && !isInValid()) {
            getLevel().setBlockAndUpdate(getPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, false));
        }
    }

    @Environment(EnvType.CLIENT)
    public void clientTick() {

    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////
    /**
     * Called when a player clicks this meta tile entity with a tool
     *
     * @return SUCCESS / CONSUME (will damage tool) / FAIL if something happened, so tools will get damaged and animations will be played
     */
    @Override
    public final InteractionResult onToolClick(@NotNull GTToolType toolType, ItemStack itemStack, UseOnContext context) {
        // the side hit from the machine grid
        var playerIn = context.getPlayer();
        if (playerIn == null) return InteractionResult.PASS;

        var hand = context.getHand();
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), false);
        Direction gridSide = ICoverable.determineGridSideHit(hitResult);
        CoverBehavior coverBehavior = gridSide == null ? null : coverContainer.getCoverAtSide(gridSide);
        if (gridSide == null) gridSide = hitResult.getDirection();

        // Prioritize covers where they apply (Screwdriver, Soft Mallet)
        if (toolType == GTToolType.SCREWDRIVER) {
            if (coverBehavior != null) {
                return coverBehavior.onScrewdriverClick(playerIn, hand, hitResult);
            } else return onScrewdriverClick(playerIn, hand, gridSide, hitResult);
        } else if (toolType == GTToolType.SOFT_MALLET) {
            if (coverBehavior != null) {
                return coverBehavior.onSoftMalletClick(playerIn, hand, hitResult);
            } else return onSoftMalletClick(playerIn, hand, gridSide, hitResult);
        } else
        if (toolType == GTToolType.WRENCH) {
            return onWrenchClick(playerIn, hand, gridSide, hitResult);
        } else if (toolType == GTToolType.CROWBAR) {
            if (coverBehavior != null) {
                if (!isRemote()) {
                    getCoverContainer().removeCover(gridSide);
                }
                return InteractionResult.CONSUME;
            }
            return onCrowbarClick(playerIn, hand, gridSide, hitResult);
        } else if (toolType == GTToolType.HARD_HAMMER) {
            return onHardHammerClick(playerIn, hand, gridSide, hitResult);
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult onHardHammerClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (this instanceof IMufflableMachine mufflableMachine) {
            if (!isRemote()) {
                mufflableMachine.setMuffled(mufflableMachine.isMuffled());
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult onCrowbarClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (playerIn.isCrouching()) {
            if (gridSide == getFrontFacing() || !isFacingValid(gridSide) || !hasFrontFacing()) {
                return InteractionResult.FAIL;
            }
            if (!isRemote()) {
                setFrontFacing(gridSide);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        var controllable = GTCapabilityHelper.getControllable(getLevel(), getPos(), gridSide);
        if (controllable != null) {
            if (!isRemote()) {
                controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
                playerIn.sendSystemMessage(Component.translatable(controllable.isWorkingEnabled() ?
                        "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"));
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }


    //////////////////////////////////////
    //**********     MISC    ***********//
    //////////////////////////////////////

    @Nullable
    public static MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMetaMachineBlockEntity machineBlockEntity) {
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

    public static void clearInventory(List<ItemStack> itemBuffer, IItemTransfer inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                itemBuffer.add(stackInSlot);
            }
        }
    }

    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, GTToolType toolType) {
        if (toolType == GTToolType.WRENCH || toolType == GTToolType.SCREWDRIVER) return true;
        if (toolType == GTToolType.HARD_HAMMER && this instanceof IMufflableMachine) return true;
        for (CoverBehavior cover : coverContainer.getCovers()) {
            if (cover.shouldRenderGrid(player, held, toolType)) return true;
        }
        return false;
    }

    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH) {
            if (player.isCrouching()) {
                if (hasFrontFacing() && side != this.getFrontFacing() && isFacingValid(side)) {
                    return GuiTextures.TOOL_FRONT_FACING_ROTATION;
                }
            }
        } else if (toolType == GTToolType.SOFT_MALLET) {
            if (this instanceof IControllable controllable) {
                return controllable.isWorkingEnabled() ? GuiTextures.TOOL_PAUSE : GuiTextures.TOOL_START;
            }
        } else if (toolType == GTToolType.HARD_HAMMER) {
            if (this instanceof IMufflableMachine mufflableMachine) {
                return mufflableMachine.isMuffled() ? GuiTextures.TOOL_SOUND : GuiTextures.TOOL_MUTE;
            }
        }
        var cover = coverContainer.getCoverAtSide(side);
        if (cover != null) {
            return cover.sideTips(player, toolType, side);
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
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock) {
            return metaMachineBlock.rotationState.test(facing);
        }
        return false;
    }

    public void setFrontFacing(Direction facing) {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock && isFacingValid(facing)) {
            getLevel().setBlockAndUpdate(getPos(), blockState.setValue(metaMachineBlock.rotationState.property, facing));
        }
    }

    public void onRotated(Direction oldFacing, Direction newFacing) {

    }

    public int getPaintingColor() {
        return getDefinition().getDefaultPaintingColor();
    }

    public int tintColor(int index) {
        if (index == 1) {
            return getPaintingColor();
        }
        return -1;
    }

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        coverContainer.onNeighborChanged(block, fromPos, isMoving);
    }

    public void animateTick(RandomSource random) {
    }

    //////////////////////////////////////
    //******     Capability     ********//
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
    public ItemTransferList getItemTransferCap(@Nullable Direction side) {
        var list = getTraits().stream().filter(IItemTransfer.class::isInstance).filter(t -> t.hasCapability(side)).map(IItemTransfer.class::cast).toList();
        if (!list.isEmpty()) {
            var io = IO.BOTH;
            if (side != null && this instanceof IAutoOutputItem autoOutput && autoOutput.getOutputFacingItems() == side && !autoOutput.isAllowInputFromOutputSideItems()) {
                io = IO.OUT;
            }
            return new IOItemTransferList(list, io, getItemCapFilter(side));
        }
        return null;
    }

    @Nullable
    public FluidTransferList getFluidTransferCap(@Nullable Direction side) {
        var list = getTraits().stream().filter(IFluidTransfer.class::isInstance).filter(t -> t.hasCapability(side)).map(IFluidTransfer.class::cast).toList();
        if (!list.isEmpty()) {
            var io = IO.BOTH;
            if (side != null && this instanceof IAutoOutputFluid autoOutput && autoOutput.getOutputFacingFluids() == side && !autoOutput.isAllowInputFromOutputSideFluids()) {
                io = IO.OUT;
            }
            return new IOFluidTransferList(list, io, getFluidCapFilter(side));
        }
        return null;
    }

}
