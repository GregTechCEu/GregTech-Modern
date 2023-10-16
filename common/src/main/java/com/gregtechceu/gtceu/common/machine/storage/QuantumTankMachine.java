package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidActionResult;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuantumTankMachine extends TieredMachine implements IAutoOutputFluid, IInteractedMachine, IControllable, IDropSaveMachine, IFancyUIMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuantumTankMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Getter @Persisted @DescSynced @RequireRerender
    protected Direction outputFacingFluids;
    @Getter @Persisted @DescSynced @RequireRerender
    protected boolean autoOutputFluids;
    @Getter @Setter @Persisted
    protected boolean allowInputFromOutputSideFluids;
    @Getter
    private final long maxStoredFluids;
    @Persisted @DropSaved
    protected final NotifiableFluidTank cache;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportFluidSubs;
    @Persisted @DescSynced @Getter @DropSaved
    protected FluidStack stored = FluidStack.empty();
    @Persisted @Getter @Setter
    private boolean isVoiding;
    @Persisted @Getter
    private final FluidStorage lockedFluid;

    public QuantumTankMachine(IMachineBlockEntity holder, int tier, long maxStoredFluids, Object... args) {
        super(holder, tier);
        this.outputFacingFluids = getFrontFacing().getOpposite();
        this.maxStoredFluids = maxStoredFluids;
        this.cache = createCacheFluidHandler(args);
        this.lockedFluid = new FluidStorage(FluidHelper.getBucket());
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createCacheFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, 1, maxStoredFluids, IO.BOTH) {
            @Override
            public long fill(FluidStack resource, boolean simulate) {
                var filled = super.fill(resource, simulate);
                if (filled < resource.getAmount() && isVoiding && isFluidValid(0, resource)) {
                    filled = resource.getAmount();
                }
                return filled;
            }
        }.setFilter(fluidStack -> !isLocked() || lockedFluid.getFluid().isFluidEqual(fluidStack));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.stored = cache.getFluidInTank(0);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        exportFluidSubs = cache.addChangedListener(this::onFluidChanged);
    }

    private void onFluidChanged() {
        if (!isRemote()) {
            this.stored = cache.getFluidInTank(0);
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportFluidSubs != null) {
            exportFluidSubs.unsubscribe();
            exportFluidSubs = null;
        }
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    //////////////////////////////////////
    //*******     Auto Output    *******//
    //////////////////////////////////////

    @Override
    public void setAutoOutputFluids(boolean allow) {
        this.autoOutputFluids = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction outputFacing) {
        this.outputFacingFluids = outputFacing;
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isAutoOutputFluids();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setAutoOutputFluids(isWorkingAllowed);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingFluids();
        if ((isAutoOutputFluids() && !cache.isEmpty()) && outputFacing != null
                && FluidTransferHelper.getFluidTransfer(getLevel(), getPos().relative(outputFacing), outputFacing.getOpposite()) != null) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::checkAutoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputFluids() && getOutputFacingFluids() != null) {
                cache.exportToNearby(getOutputFacingFluids());
            }
            updateAutoOutputSubscription();
        }
    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == outputFacingFluids) return false;
        return super.isFacingValid(facing);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var currentStack = player.getMainHandItem();
        if (hit.getDirection() == getFrontFacing() && !currentStack.isEmpty()) {
            var handler = FluidTransferHelper.getFluidTransfer(player, InteractionHand.MAIN_HAND);
            var fluidTank = cache.storages[0];
            if (handler != null && !isRemote()) {
                if (cache.storages[0].getFluidAmount() > 0) {
                    FluidStack initialFluid = fluidTank.getFluid();
                    FluidActionResult result = FluidTransferHelper.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, false);
                    if (result.isSuccess()) {
                        ItemStack remainingStack = FluidTransferHelper.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
                        currentStack.shrink(1);
                        SoundEvent soundevent = FluidHelper.getFillSound(initialFluid);
                        if (soundevent != null) {
                            player.level().playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        if (!remainingStack.isEmpty() && !player.addItem(remainingStack)) {
                            Block.popResource(player.level(), player.getOnPos(), remainingStack);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }

                FluidActionResult result = FluidTransferHelper.tryEmptyContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, false);
                if (result.isSuccess()) {
                    ItemStack remainingStack = FluidTransferHelper.tryEmptyContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
                    currentStack.shrink(1);
                    SoundEvent soundevent = FluidHelper.getEmptySound(fluidTank.getFluid());
                    if (soundevent != null) {
                        player.level().playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    if (!remainingStack.isEmpty() && !player.getInventory().add(remainingStack)) {
                        Block.popResource(player.level(), player.getOnPos(), remainingStack);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!playerIn.isCrouching() && !isRemote()) {
            var tool = playerIn.getItemInHand(hand);
            if (tool.getDamageValue() >= tool.getMaxDamage()) return InteractionResult.PASS;
            if (hasFrontFacing() && gridSide == getFrontFacing()) return InteractionResult.PASS;
            if (gridSide != getOutputFacingFluids()) {
                setOutputFacingFluids(gridSide);
            } else {
                setOutputFacingFluids(null);
            }
            return InteractionResult.CONSUME;
        }

        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (gridSide == getOutputFacingFluids()) {
                if (isAllowInputFromOutputSideFluids()) {
                    setAllowInputFromOutputSideFluids(false);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.disallow").append(Component.translatable("gtceu.creative.chest.fluid")));
                } else {
                    setAllowInputFromOutputSideFluids(true);
                    playerIn.sendSystemMessage(Component.translatable("gtceu.machine.basic.input_from_output_side.allow").append(Component.translatable("gtceu.creative.chest.fluid")));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    public boolean isLocked() {
        return !lockedFluid.getFluid().isEmpty();
    }

    protected void setLocked(boolean locked) {
        if (!stored.isEmpty() && locked) {
            var copied = stored.copy();
            copied.setAmount(lockedFluid.getCapacity());
            lockedFluid.setFluid(copied);
        } else if (!locked) {
            lockedFluid.setFluid(FluidStack.empty());
        }
    }

    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 90, 63);
        group.addWidget(new ImageWidget(4, 4, 82, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () ->
                        String.valueOf(cache.getFluidInTank(0).getAmount() / (FluidHelper.getBucket() / 1000))
                ).setTextColor(-1).setDropShadow(true))
                .addWidget(new TankWidget(cache.storages[0], 68, 23, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .addWidget(new PhantomFluidWidget(lockedFluid, 68, 41, 18, 18)
                        .setShowAmount(false)
                        .setBackground(ColorPattern.T_GRAY.rectTexture()))
                .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_lock.tooltip"))
                .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
                        GuiTextures.BUTTON_VOID, this::isVoiding, this::setVoiding)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_voiding_partial.tooltip"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH) {
            if (!player.isCrouching()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        } else if (toolType == GTToolType.SCREWDRIVER) {
            if (side == getOutputFacingFluids()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        }
        return super.sideTips(player, toolType, side);
    }
}
