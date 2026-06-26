package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.blockentity.ICopyable;
import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitHolder;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.feature.IFrontFacingTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IInteractionTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRedstoneSignalTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRenderingTrait;
import com.gregtechceu.gtceu.api.misc.*;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.sync_system.managed.ManagedSyncBlockEntity;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.cover.ItemFilterCover;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.item.behavior.MachineConfigCopyBehaviour;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.data.TagCompatibilityFixer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import brachy.modularui.drawable.UITexture;
import com.mojang.datafixers.util.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The base BlockEntity for all GT machines.
 */
public class MetaMachine extends ManagedSyncBlockEntity implements IGregtechBlockEntity, IToolGridHighlight,
                         IPaintable, IMachineFeature, ICopyable {

    private static final int MIN_OFFSET_BOUND = 20;

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
    @Setter(value = AccessLevel.PROTECTED)
    private long offset = GTValues.RNG.nextInt(MIN_OFFSET_BOUND);

    @Getter
    @SaveField
    @SyncToClient
    protected final MachineTraitHolder traitHolder;

    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    // If this machine data needs to be migrated from 7.x to 8.x
    private boolean isOldMachineData = false;

    public MetaMachine(BlockEntityCreationInfo info) {
        super(info);
        this.renderState = getDefinition().defaultRenderState();
        this.traitHolder = new MachineTraitHolder(this);
        this.coverContainer = attachTrait(new MachineCoverContainer(this));
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
    }

    //////////////////////////////////////
    // ***** Machine Lifecycle ******//
    //////////////////////////////////////

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        TagCompatibilityFixer.fixMachineAutoOutputTag(tag);
        super.loadAdditional(tag, registries);
    }

    /**
     * Called when this machine is loaded.<br>
     * On the server side, the entire world may not be loaded when this method is called.<br>
     * On the client side, this method is called before this machine's data has been received.<br>
     * To schedule code to run on the first full world tick, see {@link #scheduleForNextServerTick(Runnable)}
     */
    @MustBeInvokedByOverriders
    public void onLoad() {
        getAllTraits().forEach(MachineTrait::onMachineLoad);

        if (isOldMachineData) {
            Direction upwardsGlobal = TagCompatibilityFixer.fixUpwardsFacing(this.getFrontFacing(),
                    this.getUpwardsFacing());
            if (upwardsGlobal != null && getBlockState().hasProperty(GTBlockStateProperties.UPWARDS_FACING)) {
                // force the global upwards direction
                var blockState = getBlockState();
                boolean changeGlobal = blockState.getValue(GTBlockStateProperties.UPWARDS_FACING) != upwardsGlobal;
                if (blockState.getBlock() instanceof MetaMachineBlock && changeGlobal) {
                    getLevel().setBlock(getBlockPos(),
                            blockState.setValue(GTBlockStateProperties.UPWARDS_FACING, upwardsGlobal),
                            Block.UPDATE_IMMEDIATE);
                }
            }
        }

        // update the painted model property if the machine is painted
        MachineRenderState renderState = getRenderState();
        if (renderState.hasProperty(GTMachineModelProperties.IS_PAINTED) &&
                this.isPainted() != renderState.getValue(GTMachineModelProperties.IS_PAINTED)) {
            setRenderState(renderState.setValue(GTMachineModelProperties.IS_PAINTED, this.isPainted()));
        }

        // Force model data refresh on client when BlockEntity finishes loading,
        // in case the chunk was rendered before this BlockEntity was available
        if (isRemote()) {
            scheduleRenderUpdate();
        }
    }

    /**
     * Schedules a callback to be executed on the next server tick. Only works on the server-side. <br>
     * Should be called from methods such as {@link #onLoad()}, when the world may not be fully loaded.
     *
     * @param runnable The callback to execute
     */
    public final void scheduleForNextServerTick(Runnable runnable) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, runnable));
        }
    }

    @Override
    public final void setRemoved() {
        super.setRemoved();
        onUnload();
    }

    /**
     * Called when this machine is about to be unloaded.
     */
    @MustBeInvokedByOverriders
    public void onUnload() {
        getAllTraits().forEach(MachineTrait::onMachineUnload);

        for (TickableSubscription serverTick : serverTicks) {
            serverTick.unsubscribe();
        }
        serverTicks.clear();
    }

    /**
     * Called when this machine is destroyed.
     */
    public void onMachineDestroyed() {
        getAllTraits().forEach(MachineTrait::onMachineDestroyed);
    }

    /**
     * Called to modify the drops returned when this block is destroyed
     *
     * @param drops A modifiable list of drops.
     */
    public void modifyDrops(List<ItemStack> drops) {}

    /**
     * Applies item stack component data when this machine is placed.
     *
     * @param componentInput Component Input
     */
    protected void applyImplicitComponents(DataComponentInput componentInput) {}

    /**
     * Saves this machine's data to item stack components.
     *
     * @param components Component Builder
     */
    public void collectImplicitComponents(DataComponentMap.Builder components) {}

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
        }
        return null;
    }

    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    @ApiStatus.Internal
    public final void serverTick() {
        super.serverTick();
        executeTick();
    }

    /**
     * Called every tick on the client side.
     */
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {}

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

    /**
     * @return An unmodifiable list of all traits attached to this machine.
     */
    public @Unmodifiable List<MachineTrait> getAllTraits() {
        return traitHolder.getAllTraits();
    }

    /**
     * Attaches a trait to this machine, with the default trait callback priority of 1.
     *
     * @param trait The trait to attach
     * @return The attached trait
     */
    public <T extends MachineTrait> T attachTrait(T trait) {
        return traitHolder.attachTrait(trait);
    }

    /**
     * Attaches a trait to this machine.
     *
     * @param trait            The trait to attach
     * @param callbackPriority The trait's callback priority. Traits with a higher priority will have their events fired
     *                         first, which may prevent traits with a lower priority from handling some events.
     * @return The attached trait
     */
    public <T extends MachineTrait> T attachTrait(T trait, int callbackPriority) {
        return traitHolder.attachTrait(trait, callbackPriority);
    }

    /**
     * Registers a trait with data to be saved or synced to the client.
     * Do not register a persistent trait and also store that trait as a syncable machine field, otherwise the trait
     * data will be duplicated. Use only one sync method.
     *
     * @param traitName Unique identifier for this trait.
     * @param trait     The trait to register
     */
    public <T extends MachineTrait> T attachPersistentTrait(String traitName, T trait) {
        traitHolder.attachTrait(trait);
        traitHolder.registerPersistentTrait(traitName, trait);
        return trait;
    }

    /**
     * Registers a trait with data to be saved or synced to the client.
     * Do not register a persistent trait and also store that trait as a syncable machine field, otherwise the trait
     * data will be duplicated. Use only one sync method.
     *
     * @param traitName        Unique identifier for this trait.
     * @param callbackPriority The trait's callback priority. Traits with a higher priority will have their events fired
     *                         first, which may prevent traits with a lower priority from handling some events.
     * @param trait            The trait to register
     */
    public <T extends MachineTrait> T attachPersistentTrait(String traitName, T trait, int callbackPriority) {
        traitHolder.attachTrait(trait, callbackPriority);
        traitHolder.registerPersistentTrait(traitName, trait);
        return trait;
    }

    /**
     * Gets a trait registered by {@code registerPersistentTrait}
     *
     * @param traitName the unique identifier for the trait
     * @return the trait, or null if not present
     */
    public @Nullable <T extends MachineTrait> T getPersistentTrait(String traitName) {
        return traitHolder.getPersistentTrait(traitName);
    }

    /**
     * Gets the first trait (trait with highest priority) of a specified type
     *
     * @param type The trait type to get
     * @return The trait, or null if no traits of the given type are present.
     */
    public <T extends MachineTrait> @Nullable T getTrait(MachineTraitType<T> type) {
        return traitHolder.getTrait(type);
    }

    /**
     * Gets the first trait (trait with highest priority) of a specified type
     *
     * @param type The trait type to get
     * @return An optional result containing the trait if present.
     */
    public <T extends MachineTrait> Optional<T> getTraitOptional(MachineTraitType<T> type) {
        return Optional.ofNullable(getTrait(type));
    }

    /**
     * Get all traits with the specified type.
     *
     * @return An unmodifiable list containing all traits of the specified type.
     */
    public <T extends MachineTrait> @Unmodifiable List<T> getTraits(MachineTraitType<T> type) {
        return traitHolder.getTraits(type);
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    /**
     * Called when a player clicks this machine with a GT tool
     *
     * @param context The context of this interaction.
     * @return A pair containing the type of the tool (if the interaction was successful), and the result of the
     *         interaction.
     *         {@link InteractionResult#sidedSuccess(boolean)} will play the tool sound (based on the first element of
     *         the pair) and consume
     *         durability.
     */
    public final Pair<@Nullable GTToolType, InteractionResult> onToolClick(ExtendedUseOnContext context) {
        // the side hit from the machine grid
        var player = context.getPlayer();
        if (player == null) return Pair.of(null, InteractionResult.PASS);

        var toolType = context.getToolType();

        Pair<@Nullable GTToolType, InteractionResult> result = null;

        // Prioritize covers
        CoverBehavior cover = getCoverContainer().getCoverAtSide(context.getGridSide());
        if (cover != null) {
            result = cover.onToolClick(context);
            if (result.getSecond() != InteractionResult.PASS) return result;

            if (toolType.contains(GTToolType.CROWBAR)) {
                getCoverContainer().removeCover(context.getGridSide(), player);
                return Pair.of(GTToolType.CROWBAR, InteractionResult.sidedSuccess(isRemote()));
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
        if (!context.getItemInHand().canPerformAction(GTItemAbilities.HAMMER_MUTE)) {
            return InteractionResult.PASS;
        }
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
            Direction newUpwards = GTUtil.cross(getFrontFacing(), getUpwardsFacing());
            setUpwardsFacing(player.isShiftKeyDown() ? newUpwards : newUpwards.getOpposite());
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
        if (!context.getItemInHand().canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            return InteractionResult.PASS;
        }
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
     *
     * @param context The context which this interaction is being performed from.
     * @return The result of this interaction callback.
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
     * Called when a machine is right clicked without an item, or if this machine was clicked with an item but no
     * item-specific interaction was performed.
     *
     * @param context The context which this interaction is being performed from.
     * @return The result of this interaction callback.
     */
    public InteractionResult onUse(ExtendedUseOnContext context) {
        if (context.getPlayer().isShiftKeyDown()) {
            var cover = coverContainer.getCoverAtSide(context.getGridSide());
            if (cover != null) {
                var result = cover.onScrewdriverClick(context);
                if (result != InteractionResult.PASS) return result;
            }
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
     * @param player Player that clicked
     * @param hand   Player hand
     * @param face   Clicked face
     * @return true to cancel the click event, false to continue processing
     */
    public boolean onLeftClick(Player player, InteractionHand hand, @Nullable Direction face) {
        for (var trait : getAllTraits()) {
            if (trait instanceof IInteractionTrait interactionTrait) {
                if (interactionTrait.onLeftClick(player, hand, face)) return true;
            }
        }
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

    public @UnknownNullability Level getLevel() {
        return super.getLevel();
    }

    @ApiStatus.Internal
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

    public void setOffsetBound(int offsetBound) {
        var bound = Math.max(offsetBound, MIN_OFFSET_BOUND);
        offset = GTValues.RNG.nextInt(bound);
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        if (toolTypes.contains(GTToolType.WRENCH) || held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
            return true;
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
    public @Nullable UITexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                        ItemStack held, Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH) || held.canPerformAction(GTItemAbilities.WRENCH_ROTATE)) {
            if (player.isShiftKeyDown()) {
                if (isFacingValid(side) || (allowExtendedFacing() && hasFrontFacing() && side == getFrontFacing())) {
                    return GTGuiTextures.TOOL_FRONT_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET) || held.canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            if (this instanceof IControllable controllable) {
                return controllable.isWorkingEnabled() ? GTGuiTextures.TOOL_START : GTGuiTextures.TOOL_PAUSE;
            }
        } else if (toolTypes.contains(GTToolType.HARD_HAMMER) || held.canPerformAction(GTItemAbilities.HAMMER_MUTE)) {
            if (this instanceof IMufflableMachine mufflableMachine) {
                return mufflableMachine.isMuffled() ? GTGuiTextures.TOOL_SOUND : GTGuiTextures.TOOL_MUTE;
            }
        }

        for (var trait : getAllTraits()) {
            if (trait instanceof IRenderingTrait renderingTrait) {
                var result = renderingTrait.getGridOverlayIcon(player, pos, state, toolTypes, held, side);
                if (result != null) return result;
            }
        }

        return null;
    }

    /**
     * Adds extra information to the F3 debug overlay when looking at this machine.
     *
     * @param lines A string consumer which lines are added to.
     */
    public void addDebugOverlayText(Consumer<String> lines) {
        lines.accept(ChatFormatting.UNDERLINE + "Targeted Machine: ");
        lines.accept(this.getDefinition().getId().toString());

        // add render state info
        MachineRenderState renderState = this.getRenderState();
        for (var property : renderState.getValues().entrySet()) {
            lines.accept(GTStringUtils.getPropertyValueString(property));
        }
    }

    /**
     * The {@link MachineDefinition} of this machine.
     *
     * @return The {@link MachineDefinition}
     */
    public MachineDefinition getDefinition() {
        if (getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getDefinition();
        } else {
            throw new IllegalStateException(
                    "MetaMachine created for an incompatible block: " + getBlockState().getBlock());
        }
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this machine's block in world
     */
    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(Shapes.block());
    }

    /**
     * Gets the direction which this machine is facing.
     *
     * @return The direction the machine is facing, or north if this machine does not have a front face.
     */
    public Direction getFrontFacing() {
        return getRotationState() == RotationState.NONE ? Direction.NORTH :
                getBlockState().getValue(getRotationState().property);
    }

    /**
     * Returns whether this machine has a front face.
     *
     * @return If this machine has a front face.
     */
    public final boolean hasFrontFacing() {
        return getRotationState() != RotationState.NONE;
    }

    /**
     * Returns whether this machine can be rotated to face a specific direction
     *
     * @param facing The direction to test
     * @return If it is possible to rotate this machine to face the given direction.
     */
    public boolean isFacingValid(Direction facing) {
        if (hasFrontFacing() && facing == getFrontFacing()) return false;

        for (var trait : getAllTraits()) {
            if (trait instanceof IFrontFacingTrait modifyFacingTrait) {
                if (!modifyFacingTrait.isValidFrontFace(facing)) return false;
            }
        }

        return getRotationState().test(facing);
    }

    /**
     * Returns the {@link RotationState} properties which this machine type supports.
     *
     * @return The {@link RotationState}
     */
    public RotationState getRotationState() {
        return getDefinition().getRotationState();
    }

    /**
     * Rotates this machine to face a specific direction, if that direction is a valid facing direction.
     *
     * @param facing The new facing direction.
     */
    public void setFrontFacing(Direction facing) {
        var oldFacing = getFrontFacing();
        if (oldFacing == facing) return;

        if (getUpwardsFacing().getAxis() == facing.getAxis()) {
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

    /**
     * Gets the direction which is this machine's upwards face.
     *
     * @return The upwards facing direction, or north if this machine does not allow extended facing.
     */
    public Direction getUpwardsFacing() {
        return this.allowExtendedFacing() ? this.getBlockState().getValue(GTBlockStateProperties.UPWARDS_FACING) :
                Direction.UP;
    }

    /**
     * Changes this machine's upwards facing direction, if this machine supports extended facing directions.
     *
     * @param upwardsFacing The new upwards facing direction.
     */
    public void setUpwardsFacing(Direction upwardsFacing) {
        if (!getDefinition().isAllowExtendedFacing()) {
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

    /**
     * Returns whether this machine supports extended facing directions.
     *
     * @return If extended facing directions are supported.
     */
    public boolean allowExtendedFacing() {
        return getDefinition().isAllowExtendedFacing();
    }

    /**
     * Called when this machine is rotated
     *
     * @param oldFacing The previous facing direction
     * @param newFacing The new facing direction
     */
    public void onRotated(Direction oldFacing, Direction newFacing) {}

    /**
     * Called by the block colour handler to get tint colour for a specific layer index
     *
     * @param index colour layer index
     * @return Integer colour, or -1 to not apply a colour tint.
     */
    public int tintColor(int index) {
        // index < -100 => emission if shimmer is installed.
        if (index == 1 || index == -111) {
            return getRealColor();
        }
        return -1;
    }

    /**
     * @see ModelData
     * @return ModelData to be passed to the {@link BakedModel}
     */
    @Override
    public ModelData getModelData() {
        return super.getModelData().derive().build();
    }

    /**
     * Called when a neighboring block is updated.
     *
     * @param neighborBlock The neighbor block type.
     * @param neighborPos   The neighbor position.
     * @param isMoving      If the neighbor block is moving (e.g. moved by a piston)
     */
    public void onNeighborChanged(Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        getAllTraits().forEach(t -> t.onMachineNeighborChanged(neighborBlock, neighborPos, isMoving));
    }

    public void animateTick(RandomSource random) {}

    /**
     * Returns the {@link BlockState} that this block reports at a given side.
     *
     * @param level       The level this block is in
     * @param pos         The block's position in the level
     * @param side        The side of the block that is being queried
     * @param sourceState The state of the block that is querying the appearance, or {@code null} if not applicable
     * @param sourcePos   The position of the block that is querying the appearance, or {@code null} if not applicable
     * @return The appearance of this block from the given side
     * @see IBlockExtension#getAppearance(BlockState, BlockAndTintGetter, BlockPos, Direction, BlockState, BlockPos)
     */
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        var appearance = getCoverContainer().getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        if (appearance != null) return appearance;
        return getDefinition().getAppearance().get();
    }

    /**
     * Gets the current tick offset, which can be used to run code after a certain number of ticks.
     * For example, {@code getOffsetTimer() % 20 == 0} will be true every 20 ticks (1 second)
     *
     * @return The current tick offset.
     */
    public final long getOffsetTimer() {
        if (getLevel() == null) return getOffset();
        else if (getLevel().isClientSide()) return GTValues.CLIENT_TIME + getOffset();

        var server = getLevel().getServer();
        if (server == null) return getOffset();
        return server.getTickCount() + getOffset();
    }

    ////////////////////////////////
    // ***** Redstone Signals ****//
    ////////////////////////////////

    /**
     * Gets the redstone output signal at a specific side
     *
     * @param side Side
     * @return Output signal
     */
    public int getOutputSignal(@Nullable Direction side) {
        if (side == null) return 0;

        // For some reason, Minecraft requests the output signal from the opposite side...
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side.getOpposite());

        if (cover != null) return cover.getRedstoneSignalOutput();

        var signal = 0;
        for (var trait : getAllTraits()) {
            if (trait instanceof IRedstoneSignalTrait redstoneSignalTrait) {
                signal = Math.max(signal, redstoneSignalTrait.getOutputSignal(side));
            }
        }

        return signal;
    }

    /**
     * Gets the direct output signal at a specific side
     *
     * @param side Side
     * @return Direct output signal
     */
    public int getOutputDirectSignal(@Nullable Direction side) {
        var signal = 0;
        for (var trait : getAllTraits()) {
            if (trait instanceof IRedstoneSignalTrait redstoneSignalTrait) {
                signal = Math.max(signal, redstoneSignalTrait.getOutputDirectSignal(side));
            }
        }

        return signal;
    }

    /**
     * Gets the analog (comparator) output signal
     *
     * @return Analog output signal.
     */
    public int getAnalogOutputSignal() {
        var signal = 0;
        for (var trait : getAllTraits()) {
            if (trait instanceof IRedstoneSignalTrait redstoneSignalTrait) {
                signal = Math.max(signal, redstoneSignalTrait.getAnalogOutputSignal());
            }
        }

        return signal;
    }

    /**
     * Returns if redstone can be connected to a specific side of this machine
     *
     * @param side The side to check
     * @return If redstone can be connected
     */
    public boolean canConnectRedstone(Direction side) {
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        if (cover != null) return cover.canConnectRedstone();

        for (var trait : getAllTraits()) {
            if (trait instanceof IRedstoneSignalTrait redstoneSignalTrait) {
                if (redstoneSignalTrait.canConnectRedstone(side)) return true;
            }
        }
        return false;
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

    @Override
    public int getDefaultPaintingColor() {
        return getDefinition().getDefaultPaintingColor();
    }

    //////////////////////////////////////
    // ******** Capabilities *********//
    //////////////////////////////////////

    /**
     * Gets the item filter for a specific side of this machine.
     *
     * @param side Side
     * @param io   The IO mode this filter should be applicable to.
     * @return A {@code Predicate<ItemStack>} representing this filter
     */
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

    /**
     * Gets the fluid filter for a specific side of this machine.
     *
     * @param side Side
     * @param io   The IO mode this filter should be applicable to.
     * @return A {@code Predicate<FluidStack>} representing this filter
     */
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

    /**
     * Gets the item handler capability for a specific side of this machine
     *
     * @param side               The side
     * @param useCoverCapability Whether to return an item handler provided by an attached cover, if present.
     * @return The {@link IItemHandlerModifiable} capability, or null.
     */
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

    /**
     * Gets the fluid handler capability for a specific side of this machine
     *
     * @param side               The side
     * @param useCoverCapability Whether to return a fluid handler provided by an attached cover, if present.
     * @return The {@link IFluidHandlerModifiable} capability, or null.
     */
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

    // NBT keys for machine config values
    private static final String COVER = "cover";
    private static final String FACING_DIR = "front_facing";

    private static final String ITEM_OUTPUT_SIDE = "output_direction_item";
    private static final String ITEM_AUTO_OUTPUT = "item_auto_output";
    private static final String ALLOW_ITEM_IN_FROM_OUT = "allow_input_from_output_item";

    private static final String FLUID_OUTPUT_SIDE = "output_direction_fluid";
    private static final String FLUID_AUTO_OUTPUT = "fluid_auto_output";
    private static final String ALLOW_FLUID_IN_FROM_OUT = "allow_input_from_output_fluid";

    private static final String MUFFLED = "muffled";
    private static final String CIRCUIT = "circuit_config";

    @Override
    public void copyConfig(CompoundTag tag) {
        tag.putString(FACING_DIR, MachineConfigCopyBehaviour.directionToString(getFrontFacing()));

        var outputTrait = getTrait(AutoOutputTrait.TYPE);
        if (outputTrait != null && outputTrait.supportsAutoOutputItems() &&
                outputTrait.getItemOutputDirection() != null) {
            tag.putString(ITEM_OUTPUT_SIDE,
                    MachineConfigCopyBehaviour.directionToString(outputTrait.getItemOutputDirection()));
            tag.putBoolean(ITEM_AUTO_OUTPUT, outputTrait.isAutoOutputItems());
            tag.putBoolean(ALLOW_ITEM_IN_FROM_OUT, outputTrait.allowsItemInputFromOutputSide());
        }

        if (outputTrait != null && outputTrait.supportsAutoOutputFluids() &&
                outputTrait.getFluidOutputDirection() != null) {
            tag.putString(FLUID_OUTPUT_SIDE,
                    MachineConfigCopyBehaviour.directionToString(outputTrait.getFluidOutputDirection()));
            tag.putBoolean(FLUID_AUTO_OUTPUT, outputTrait.isAutoOutputFluids());
            tag.putBoolean(ALLOW_FLUID_IN_FROM_OUT, outputTrait.allowsFluidInputFromOutputSide());
        }

        if (this instanceof IMufflableMachine mufflableMachine) {
            tag.putBoolean(MUFFLED, mufflableMachine.isMuffled());
        }

        if (this instanceof IHasCircuitSlot circuitMachine) {
            var circuit = IntCircuitBehaviour
                    .getCircuitConfiguration(circuitMachine.getCircuitInventory().getStackInSlot(0));
            if (circuitMachine.isCircuitSlotEnabled() && circuit != 0) {
                tag.putInt(CIRCUIT, circuit);
            }
        }

        var coverTag = new CompoundTag();
        getCoverContainer().copyConfig(coverTag);
        tag.put(COVER, coverTag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        var outputTrait = getTrait(AutoOutputTrait.TYPE);
        if (outputTrait != null) {
            if (tag.contains(ITEM_OUTPUT_SIDE))
                outputTrait.setItemOutputDirection(
                        MachineConfigCopyBehaviour.stringToDirection(tag.getString(ITEM_OUTPUT_SIDE)));
            if (tag.contains(ITEM_AUTO_OUTPUT)) outputTrait.setAllowAutoOutputItems(tag.getBoolean(ITEM_AUTO_OUTPUT));
            if (tag.contains(ALLOW_ITEM_IN_FROM_OUT))
                outputTrait.setAllowItemInputFromOutputSide(tag.getBoolean(ALLOW_ITEM_IN_FROM_OUT));
            if (tag.contains(FLUID_OUTPUT_SIDE))
                outputTrait.setFluidOutputDirection(
                        MachineConfigCopyBehaviour.stringToDirection(tag.getString(FLUID_OUTPUT_SIDE)));
            if (tag.contains(FLUID_AUTO_OUTPUT))
                outputTrait.setAllowAutoOutputFluids(tag.getBoolean(FLUID_AUTO_OUTPUT));
            if (tag.contains(ALLOW_FLUID_IN_FROM_OUT))
                outputTrait.setAllowFluidInputFromOutputSide(tag.getBoolean(ALLOW_FLUID_IN_FROM_OUT));
        }

        Direction facingDir = Direction.byName(tag.getString(FACING_DIR));
        if (facingDir != null) setFrontFacing(facingDir);

        if (this instanceof IMufflableMachine mufflableMachine) {
            if (tag.contains(MUFFLED)) mufflableMachine.setMuffled(tag.getBoolean(MUFFLED));
        }

        if (this instanceof IHasCircuitSlot circuitMachine) {
            if (tag.contains(CIRCUIT))
                circuitMachine.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(tag.getInt(CIRCUIT)));
        }

        getCoverContainer().pasteConfig(player, tag.getCompound(COVER));
    }

    @Override
    public List<ItemStack> getItemsRequiredToPaste() {
        return coverContainer.getItemsRequiredToPaste();
    }
}
