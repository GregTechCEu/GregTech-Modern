package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.blockentity.ICopyable;
import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitHolder;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.feature.IFrontFacingTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IInteractionTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRenderingTrait;
import com.gregtechceu.gtceu.api.misc.*;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.ManagedSyncBlockEntity;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.utils.DummyWorld;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.capabilities.Capabilities;
import com.mojang.datafixers.util.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MetaMachine extends ManagedSyncBlockEntity implements IGregtechBlockEntity, IToolGridHighlight,
                         IFancyTooltip, IPaintable, IMachineFeature, ICopyable {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    @Getter
    @SaveField
    @SyncToClient
    @Nullable
    private UUID ownerUUID;

    @Getter
    @SyncToClient
    @SaveField(nbtKey = "cover")
    protected final MachineCoverContainer coverContainer;

    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private int paintingColor = -1;

    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private MachineRenderState renderState;
    @Getter(value = AccessLevel.PROTECTED)
    private final long offset = GTValues.RNG.nextInt(20);

    @Getter
    protected final MachineTraitHolder traitHolder;

    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public MetaMachine(BlockEntityCreationInfo info) {
        super(info);
        this.renderState = getDefinition().defaultRenderState();
        this.coverContainer = new MachineCoverContainer(this);
        this.traitHolder = new MachineTraitHolder(this);
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
    }

    //////////////////////////////////////
    // ***** Machine Lifecycle ******//
    //////////////////////////////////////

    @Override
    public void load(CompoundTag tag) {
        TagCompatibilityFixer.fixMachineAutoOutputTag(tag);
        super.load(tag);
    }

    @MustBeInvokedByOverriders
    public void onLoad() {
        getAllTraits().forEach(MachineTrait::onMachineLoad);
        coverContainer.onLoad();

        // update the painted model property if the machine is painted
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_PAINTED) &&
                this.isPainted() != renderState.getValue(GTMachineModelProperties.IS_PAINTED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_PAINTED, this.isPainted()));
        }
    }

    @Override
    public final void setRemoved() {
        super.setRemoved();
        onUnload();
    }

    @MustBeInvokedByOverriders
    public void onUnload() {
        getAllTraits().forEach(MachineTrait::onMachineUnload);
        coverContainer.onUnload();
        for (TickableSubscription serverTick : serverTicks) {
            serverTick.unsubscribe();
        }
        serverTicks.clear();
    }

    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player instanceof ServerPlayer sPlayer) {
            ownerUUID = sPlayer.getUUID();
        }

        if (this instanceof IDropSaveMachine dropSaveMachine) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                dropSaveMachine.loadFromItem(tag);
            }
        }
    }

    public void onMachineDestroyed() {
        for (Direction direction : GTUtil.DIRECTIONS) {
            getCoverContainer().removeCover(direction, null);
        }
    }

    public void modifyDrops(List<ItemStack> drops) {}

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

        for (var iter = serverTicks.iterator(); iter.hasNext();) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (isRemoved()) break;
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
    }

    //////////////////////////////////////
    // ******* Machine Traits *******//
    //////////////////////////////////////

    public @UnmodifiableView List<MachineTrait> getAllTraits() {
        return traitHolder.getAllTraits();
    }

    public <T extends MachineTrait> T attachTrait(T trait) {
        return traitHolder.attachTrait(trait);
    }

    /**
     * Registers a trait with data to be saved or synced to the client.
     * Do not register a persistent trait and also store that trait as a syncable machine field, otherwise the trait
     * data will be duplicated. Use only one sync method.
     *
     * @param traitName Unique identifier for this trait.
     * @param trait     The trait to register
     */
    public void attachPersistentTrait(String traitName, MachineTrait trait) {
        traitHolder.attachTrait(trait);
        traitHolder.registerPersistentTrait(traitName, trait);
    }

    public @Nullable <T extends MachineTrait> T getPersistentTrait(String traitName) {
        return traitHolder.getPersistentTrait(traitName);
    }

    /**
     * Gets the first trait with the specified type.
     */
    public <T extends MachineTrait> @Nullable T getTrait(MachineTraitType<T> type) {
        return traitHolder.getTrait(type);
    }

    public <T extends MachineTrait> Optional<T> getTraitOptional(MachineTraitType<T> type) {
        return Optional.ofNullable(getTrait(type));
    }

    /**
     * Get all traits with the specified type.
     */
    public <T extends MachineTrait> @Unmodifiable List<T> getTraits(MachineTraitType<T> type) {
        return traitHolder.getTraits(type);
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    /**
     * Called when a player clicks this machine with a tool
     *
     * @return SUCCESS / CONSUME (will damage tool) / FAIL if something happened, so tools will get damaged and
     *         animations will be played
     */
    public final Pair<@Nullable GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        // the side hit from the machine grid
        var player = context.getPlayer();
        if (player == null) return Pair.of(null, InteractionResult.PASS);

        var toolType = context.getToolType();

        Pair<@Nullable GTToolType, InteractionResult> result = null;

        // Prioritize covers
        var cover = getCoverContainer().getCoverAtSide(context.getClickedFace());
        if (cover != null) {
            result = cover.onToolClick(context);
            if (result.getSecond() != InteractionResult.PASS) return result;

            if (toolType.contains(GTToolType.CROWBAR) && !isRemote()) {
                getCoverContainer().removeCover(context.getGridSide(), player);
                return Pair.of(GTToolType.CROWBAR, InteractionResult.SUCCESS);
            }
        }

        if (toolType.contains(GTToolType.SCREWDRIVER)) {
            result = Pair.of(GTToolType.SCREWDRIVER, onScrewdriverClick(context));
        } else if (toolType.contains(GTToolType.SOFT_MALLET)) {
            result = Pair.of(GTToolType.SOFT_MALLET, onSoftMalletClick(context));
        } else if (toolType.contains(GTToolType.WRENCH)) {
            result = Pair.of(GTToolType.WRENCH, onWrenchClick(context));
        } else if (toolType.contains(GTToolType.CROWBAR)) {
            result = Pair.of(GTToolType.CROWBAR, onCrowbarClick(context));
        } else if (toolType.contains(GTToolType.HARD_HAMMER)) {
            result = Pair.of(GTToolType.HARD_HAMMER, onHardHammerClick(context));
        }

        if (result != null && result.getSecond() != InteractionResult.PASS) return result;

        for (var trait : getAllTraits()) {
            if (trait instanceof IInteractionTrait interactionTrait) {
                var r = interactionTrait.onToolClick(context);
                if (r.getSecond() != InteractionResult.PASS) return r;
            }
        }

        return result != null ? result : Pair.of(null, InteractionResult.PASS);
    }

    protected InteractionResult onHardHammerClick(ExtendedUseOnContext context) {
        if (this instanceof IMufflableMachine mufflableMachine) {
            if (!isRemote()) {
                mufflableMachine.setMuffled(!mufflableMachine.isMuffled());
                context.getPlayer().sendSystemMessage(Component.translatable(mufflableMachine.isMuffled() ?
                        "gtceu.machine.muffle.on" : "gtceu.machine.muffle.off"));
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult onCrowbarClick(ExtendedUseOnContext context) {
        return InteractionResult.PASS;
    }

    protected InteractionResult onWrenchClick(ExtendedUseOnContext context) {
        var player = context.getPlayer();
        var gridSide = context.getGridSide();
        if (gridSide == getFrontFacing() && allowExtendedFacing()) {
            setUpwardsFacing(player.isShiftKeyDown() ? getUpwardsFacing().getCounterClockWise() :
                    getUpwardsFacing().getClockWise());
            return InteractionResult.sidedSuccess(isRemote());
        }
        if (player.isShiftKeyDown()) {
            if (gridSide == getFrontFacing() || !isFacingValid(gridSide)) {
                return InteractionResult.FAIL;
            }
            setFrontFacing(gridSide);
            return InteractionResult.sidedSuccess(isRemote());
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        var controllable = GTCapabilityHelper.getControllable(getLevel(), getBlockPos(), context.getGridSide());
        if (controllable == null) return InteractionResult.PASS;
        if (!isRemote()) {
            controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
            context.getPlayer().sendSystemMessage(Component.translatable(controllable.isWorkingEnabled() ?
                    "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled_cycle"));
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }

    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (isRemote()) return InteractionResult.SUCCESS;
        return InteractionResult.PASS;
    }

    /**
     * Called when a machine is right clicked with an item.
     */
    public InteractionResult onUseWithItem(ExtendedUseOnContext context) {
        var types = context.getToolType();
        var itemStack = context.getItemInHand();
        var player = context.getPlayer();
        if (!types.isEmpty() && ToolHelper.canUse(itemStack) || types.isEmpty() && player.isShiftKeyDown()) {
            var result = onToolClick(context);
            if (result.getSecond() == InteractionResult.CONSUME && player instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(result.getFirst(), serverPlayer);

                if (!serverPlayer.isCreative()) {
                    ToolHelper.damageItem(itemStack, serverPlayer, 1);
                }
            }
            if (result.getSecond() != InteractionResult.PASS) return result.getSecond();
        }
        return InteractionResult.PASS;
    }

    /**
     * Called when a machine is right clicked without an item.
     */
    public InteractionResult onUse(ExtendedUseOnContext context) {
        if (context.getPlayer().isShiftKeyDown()) {
            var cover = coverContainer.getCoverAtSide(context.getClickedFace());
            if (cover != null) cover.onScrewdriverClick(context);
        }

        for (var trait : getAllTraits()) {
            if (trait instanceof IInteractionTrait interactionTrait) {
                InteractionResult result = interactionTrait.onUse(context);
                if (result != InteractionResult.PASS) return result;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * Called when a machine is left clicked.
     * 
     * @return true to cancel the click event, false to continue processing
     */
    public boolean onLeftClick(Player player, InteractionHand hand, @Nullable Direction face) {
        return false;
    }

    //////////////////////////////////////
    // ********** MISC ***********//
    //////////////////////////////////////

    @Nullable
    public static MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MetaMachine m) {
            return m;
        }
        return null;
    }

    public void notifyBlockUpdate() {
        if (getLevel() != null) {
            getLevel().updateNeighborsAt(getBlockPos(), getLevel().getBlockState(getBlockPos()).getBlock());
        }
    }

    public @UnknownNullability Level getLevel() {
        return super.getLevel();
    }

    public void setOwnerUUID(UUID uuid) {
        ownerUUID = uuid;
        syncDataHolder.markClientSyncFieldDirty("ownerUUID");
    }

    @Override
    public boolean triggerEvent(int id, int para) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }

    public void setRenderState(MachineRenderState renderState) {
        this.renderState = renderState;
        if (level != null && !level.isClientSide) {
            syncDataHolder.markClientSyncFieldDirty("renderState");
        }
        scheduleRenderUpdate();
    }

    public void setPaintingColor(int color) {
        if (color == this.paintingColor) return;

        this.paintingColor = color;
        syncDataHolder.markClientSyncFieldDirty("paintingColor");
        this.onPaintingColorChanged(color);

        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_PAINTED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_PAINTED, this.isPainted()));
        }
    }

    public void onPaintingColorChanged(int color) {}

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        if (toolTypes.contains(GTToolType.WRENCH)) return true;
        for (CoverBehavior cover : coverContainer.getCovers()) {
            if (cover.shouldRenderGrid(player, pos, state, held, toolTypes)) return true;
        }

        for (var trait : getAllTraits()) {
            if (trait instanceof IRenderingTrait renderingTrait) {
                var result = renderingTrait.shouldRenderGridOverlay(player, pos, state, held, toolTypes);
                if (result) return true;
            }
        }

        return false;
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        var cover = coverContainer.getCoverAtSide(side);
        if (cover != null) {
            var tips = cover.sideTips(player, pos, state, toolTypes, side);
            if (tips != null) return tips;
        }

        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (player.isShiftKeyDown()) {
                if (isFacingValid(side) || (allowExtendedFacing() && hasFrontFacing() && side == getFrontFacing())) {
                    return GuiTextures.TOOL_FRONT_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (this instanceof IControllable controllable) {
                return controllable.isWorkingEnabled() ? GuiTextures.TOOL_START : GuiTextures.TOOL_PAUSE;
            }
        } else if (toolTypes.contains(GTToolType.HARD_HAMMER)) {
            if (this instanceof IMufflableMachine mufflableMachine) {
                return mufflableMachine.isMuffled() ? GuiTextures.TOOL_SOUND : GuiTextures.TOOL_MUTE;
            }
        }

        for (var trait : getAllTraits()) {
            if (trait instanceof IRenderingTrait renderingTrait) {
                var result = renderingTrait.getGridOverlayIcon(player, pos, state, toolTypes, side);
                if (result != null) return result;
            }
        }

        return null;
    }

    public void addDebugOverlayText(Consumer<String> lines) {
        lines.accept(ChatFormatting.UNDERLINE + "Targeted Machine: ");
        lines.accept(this.getDefinition().getId().toString());

        // add render state info
        MachineRenderState renderState = this.getRenderState();
        for (var property : renderState.getValues().entrySet()) {
            lines.accept(ModelUtils.getPropertyValueString(property));
        }
    }

    public MachineDefinition getDefinition() {
        if (getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getDefinition();
        } else {
            throw new IllegalStateException(
                    "MetaMachine created for an incompatible block: " + getBlockState().getBlock());
        }
    }

    public RotationState getRotationState() {
        return getDefinition().getRotationState();
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     */
    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(Shapes.block());
    }

    public static Direction getFrontFacing(@Nullable MetaMachine machine) {
        return machine == null ? Direction.NORTH : machine.getFrontFacing();
    }

    public Direction getFrontFacing() {
        return getRotationState() == RotationState.NONE ? Direction.NORTH :
                getBlockState().getValue(getRotationState().property);
    }

    public final boolean hasFrontFacing() {
        return getRotationState() != RotationState.NONE;
    }

    public boolean isFacingValid(Direction facing) {
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

        for (var trait : getAllTraits()) {
            if (trait instanceof IFrontFacingTrait modifyFacingTrait) {
                if (!modifyFacingTrait.isValidFrontFace(facing)) return false;
            }
        }

        return getRotationState().test(facing);
    }

    public void setFrontFacing(Direction facing) {
        var oldFacing = getFrontFacing();

        if (allowExtendedFacing()) {
            var newUpwardsFacing = RelativeDirection.simulateAxisRotation(facing, oldFacing, getUpwardsFacing());
            setUpwardsFacing(newUpwardsFacing);
        }

        var blockState = getBlockState();
        if (isFacingValid(facing)) {
            getLevel().setBlockAndUpdate(getBlockPos(), blockState.setValue(getRotationState().property, facing));
        }

        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
        }
    }

    @Override
    public ModelData getModelData() {
        ModelData.Builder data = super.getModelData().derive();
        updateModelData(data);
        return data.build();
    }

    public Direction getUpwardsFacing() {
        return this.allowExtendedFacing() ? this.getBlockState().getValue(GTBlockStateProperties.UPWARDS_FACING) :
                Direction.NORTH;
    }

    public void setUpwardsFacing(Direction upwardsFacing) {
        if (!getDefinition().isAllowExtendedFacing()) {
            return;
        }
        if (upwardsFacing.getAxis() == Direction.Axis.Y) {
            GTCEu.LOGGER.error("Tried to set upwards facing to invalid facing {}! Skipping", upwardsFacing);
            return;
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock &&
                blockState.getValue(GTBlockStateProperties.UPWARDS_FACING) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getBlockPos(),
                    blockState.setValue(GTBlockStateProperties.UPWARDS_FACING, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
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
        getAllTraits().forEach(t -> t.onMachineNeighborChanged(block, fromPos, isMoving));
    }

    public void animateTick(RandomSource random) {}

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

    @MustBeInvokedByOverriders
    public void updateModelData(ModelData.Builder builder) {
        for (MachineTrait trait : getAllTraits()) {
            if (trait instanceof IRenderingTrait renderingTrait) renderingTrait.updateModelData(builder);
        }
    }

    public final long getOffsetTimer() {
        if (getLevel() == null) return getOffset();
        else if (getLevel().isClientSide()) return GTValues.CLIENT_TIME + getOffset();

        var server = getLevel().getServer();
        if (server == null) return getOffset();
        return server.getTickCount() + getOffset();
    }

    @Override
    public boolean isRemote() {
        return IGregtechBlockEntity.super.isRemote();
    }

    ////////////////////////////////
    // ***** Redstone Signals ****//
    ////////////////////////////////

    public int getOutputSignal(@Nullable Direction side) {
        if (side == null) return 0;

        // For some reason, Minecraft requests the output signal from the opposite side...
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side.getOpposite());
        if (cover == null) return 0;

        return cover.getRedstoneSignalOutput();
    }

    public int getOutputDirectSignal(@Nullable Direction side) {
        // IDK what this does but MC wants it
        return 0;
    }

    public int getAnalogOutputSignal() {
        return 0;
    }

    public boolean canConnectRedstone(Direction side) {
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
        if (Language.getInstance().has(mainKey)) {
            tooltips.add(0, Component.translatable(mainKey));
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return getDefinition().getDefaultPaintingColor();
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getRenderBoundingBox() {
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = blockRenderDispatcher.getBlockModel(this.getBlockState());

        if (model instanceof IBlockEntityRendererBakedModel<?> modelWithBER) {
            if (modelWithBER.getBlockEntityType() == this.getType()) {
                return ((IBlockEntityRendererBakedModel<MetaMachine>) modelWithBER)
                        .getRenderBoundingBox(this);
            }
        }
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }

    //////////////////////////////////////
    // ******** Capabilities *********//
    //////////////////////////////////////

    public Predicate<ItemStack> getItemCapFilter(@Nullable Direction side, IO io) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof ItemFilterCover filterCover) {
                if (!filterCover.getFilterMode().filters(io)) {
                    if (filterCover.getAllowFlow() == ManualIOMode.DISABLED) {
                        return item -> false;
                    }
                    if (filterCover.getAllowFlow() == ManualIOMode.UNFILTERED) {
                        return item -> true;
                    }
                }
                return filterCover.getItemFilter();
            }
        }
        return item -> true;
    }

    public Predicate<FluidStack> getFluidCapFilter(@Nullable Direction side, IO io) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof FluidFilterCover filterCover) {
                if (!filterCover.getFilterMode().filters(io)) {
                    if (filterCover.getAllowFlow() == ManualIOMode.DISABLED) {
                        return fluid -> false;
                    }
                    if (filterCover.getAllowFlow() == ManualIOMode.UNFILTERED) {
                        return fluid -> true;
                    }
                }
                return filterCover.getFluidFilter();
            }
        }
        return fluid -> true;
    }

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        var list = getAllTraits().stream()
                .filter(IItemHandlerModifiable.class::isInstance)
                .filter(t -> t.hasCapability(side))
                .map(IItemHandlerModifiable.class::cast)
                .toList();

        if (list.isEmpty()) return null;

        var io = IO.BOTH;
        var autoOutputTrait = getTrait(AutoOutputTrait.TYPE);
        if (side != null && autoOutputTrait != null && autoOutputTrait.getItemOutputDirection() == side &&
                !autoOutputTrait.allowsItemInputFromOutputSide()) {
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
        var list = getAllTraits().stream()
                .filter(IFluidHandler.class::isInstance)
                .filter(t -> t.hasCapability(side))
                .map(IFluidHandler.class::cast)
                .toList();

        if (list.isEmpty()) return null;

        var io = IO.BOTH;
        var autoOutputTrait = getTrait(AutoOutputTrait.TYPE);
        if (side != null && autoOutputTrait != null && autoOutputTrait.getFluidOutputDirection() == side &&
                !autoOutputTrait.allowsFluidInputFromOutputSide()) {
            io = IO.OUT;
        }

        IOFluidHandlerList handlerList = new IOFluidHandlerList(list, io, getFluidCapFilter(side, IO.IN),
                getFluidCapFilter(side, IO.OUT));
        if (!useCoverCapability || side == null) return handlerList;

        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        return cover != null ? cover.getFluidHandlerCap(handlerList) : handlerList;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        var result = getCapability(this, cap, side);
        return result.isPresent() ? result : super.getCapability(cap, side);
    }

    private static <T> List<T> getCapabilitiesFromTraits(List<MachineTrait> traits, @Nullable Direction accessSide,
                                                         Class<T> capability) {
        if (traits.isEmpty()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (MachineTrait trait : traits) {
            if (trait.hasCapability(accessSide) && capability.isInstance(trait)) {
                list.add(capability.cast(trait));
            }
        }
        return list;
    }

    public static <T> LazyOptional<T> getCapability(MetaMachine machine, Capability<T> cap,
                                                    @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(cap, LazyOptional.of(machine::getCoverContainer));
        } else if (cap == GTCapability.CAPABILITY_WORKABLE) {
            if (machine instanceof IWorkable workable) {
                return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
            }
            for (MachineTrait trait : machine.getAllTraits()) {
                if (trait instanceof IWorkable workable) {
                    return GTCapability.CAPABILITY_WORKABLE.orEmpty(cap, LazyOptional.of(() -> workable));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_CONTROLLABLE) {
            if (machine instanceof IControllable controllable) {
                return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
            }
            for (MachineTrait trait : machine.getAllTraits()) {
                if (trait instanceof IControllable controllable) {
                    return GTCapability.CAPABILITY_CONTROLLABLE.orEmpty(cap, LazyOptional.of(() -> controllable));
                }
            }
        } else if (cap == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
            if (machine instanceof IEnergyContainer energyContainer) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side, IEnergyContainer.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_ENERGY_CONTAINER.orEmpty(cap,
                        LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyContainerList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER) {
            if (machine instanceof IEnergyInfoProvider energyInfoProvider) {
                return GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> energyInfoProvider));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side, IEnergyInfoProvider.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new EnergyInfoProviderList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_MAINTENANCE_MACHINE) {
            if (machine instanceof IMaintenanceMachine maintenanceMachine) {
                return GTCapability.CAPABILITY_MAINTENANCE_MACHINE.orEmpty(cap,
                        LazyOptional.of(() -> maintenanceMachine));
            }
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            var handler = machine.getItemHandlerCap(side, true);
            if (handler != null) {
                return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
            }
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            var handler = machine.getFluidHandlerCap(side, true);
            if (handler != null) {
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
            }
        } else if (cap == ForgeCapabilities.ENERGY) {
            if (machine instanceof IEnergyStorage energyStorage) {
                return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> energyStorage));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side, IEnergyStorage.class);
            if (!list.isEmpty()) {
                // TODO wrap list in the future
                return ForgeCapabilities.ENERGY.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_LASER) {
            if (machine instanceof ILaserContainer energyContainer) {
                return GTCapability.CAPABILITY_LASER.orEmpty(cap, LazyOptional.of(() -> energyContainer));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side, ILaserContainer.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_LASER.orEmpty(cap,
                        LazyOptional.of(() -> list.size() == 1 ? list.get(0) : new LaserContainerList(list)));
            }
        } else if (cap == GTCapability.CAPABILITY_COMPUTATION_PROVIDER) {
            if (machine instanceof IOpticalComputationProvider computationProvider) {
                return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(cap,
                        LazyOptional.of(() -> computationProvider));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side,
                    IOpticalComputationProvider.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_COMPUTATION_PROVIDER.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_DATA_ACCESS) {
            if (machine instanceof IDataAccessHatch computationProvider) {
                return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(cap, LazyOptional.of(() -> computationProvider));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side, IDataAccessHatch.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_DATA_ACCESS.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        } else if (cap == GTCapability.CAPABILITY_MONITOR_COMPONENT) {
            if (machine instanceof IMonitorComponent monitorComponent) {
                return GTCapability.CAPABILITY_MONITOR_COMPONENT.orEmpty(cap, LazyOptional.of(() -> monitorComponent));
            }
            var list = getCapabilitiesFromTraits(machine.getAllTraits(), side, IMonitorComponent.class);
            if (!list.isEmpty()) {
                return GTCapability.CAPABILITY_MONITOR_COMPONENT.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
            }
        }
        if (GTCEu.Mods.isAE2Loaded()) {
            LazyOptional<?> opt = AE2CallWrapper.getGridNodeHostCapability(cap, machine, side);
            if (opt.isPresent()) {
                // noinspection unchecked
                return (LazyOptional<T>) opt;
            }
        }
        return LazyOptional.empty();
    }

    public static class AE2CallWrapper {

        public static LazyOptional<?> getGridNodeHostCapability(Capability<?> cap, MetaMachine machine,
                                                                @Nullable Direction side) {
            if (cap == Capabilities.IN_WORLD_GRID_NODE_HOST) {
                if (machine instanceof IInWorldGridNodeHost nodeHost) {
                    return Capabilities.IN_WORLD_GRID_NODE_HOST.orEmpty(cap, LazyOptional.of(() -> nodeHost));
                }
                var list = getCapabilitiesFromTraits(machine.getAllTraits(), side,
                        IInWorldGridNodeHost.class);
                if (!list.isEmpty()) {
                    // TODO wrap list in the future (or not.)
                    return Capabilities.IN_WORLD_GRID_NODE_HOST.orEmpty(cap, LazyOptional.of(() -> list.get(0)));
                }
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        return ICopyable.super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        ICopyable.super.pasteConfig(player, tag);
    }

    @Override
    public List<ItemStack> getItemsRequiredToPaste() {
        return coverContainer.getItemsRequiredToPaste();
    }
}
