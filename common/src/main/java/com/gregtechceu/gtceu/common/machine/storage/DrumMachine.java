package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
public class DrumMachine extends MetaMachine implements IAutoOutputFluid, IDropSaveMachine, IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DrumMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Getter @Persisted @DescSynced @RequireRerender
    protected boolean autoOutputFluids;
    @Getter
    private final int maxStoredFluids;
    @Persisted @DropSaved
    protected final NotifiableFluidTank cache;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportFluidSubs;
    @Persisted(key = "Fluid") @DescSynced @Getter @DropSaved // rename "Fluid" for Item capability
    protected FluidStack stored = FluidStack.empty();
    @Getter
    protected final Material material;

    public DrumMachine(IMachineBlockEntity holder, Material material, int maxStoredFluids, Object... args) {
        super(holder);
        this.material = material;
        this.maxStoredFluids = maxStoredFluids;
        this.cache = createCacheFluidHandler(args);
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createCacheFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, 1, maxStoredFluids, IO.BOTH)
                .setFilter(material.getProperty(PropertyKey.FLUID_PIPE));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.stored = cache.getFluidInTank(0);
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        this.exportFluidSubs = cache.addChangedListener(this::onFluidChanged);
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

    //////////////////////////////////////
    //******     Fluid Logic     *******//
    //////////////////////////////////////

    @Override
    public void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        if (!tag.contains("Fluid")) {
            stored = FluidStack.empty();
        }
        // "stored" may not be same as cache (due to item's fluid cap). we should update it.
        cache.storages[0].setFluid(stored.copy());
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public void setAutoOutputFluids(boolean allow) {
        this.autoOutputFluids = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    // always is facing down, and can never accept fluids from output side
    @Override public void setAllowInputFromOutputSideFluids(boolean allow) {}
    @Override public void setOutputFacingFluids(@Nullable Direction outputFacing) {
        updateAutoOutputSubscription();
    }

    @Override
    public @Nullable Direction getOutputFacingFluids() {
        return Direction.DOWN;
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

    @SuppressWarnings("resource")
    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var currentStack = player.getMainHandItem();
        if (!currentStack.isEmpty()) {
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
        return world.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!isRemote()) {
            if (!playerIn.isCrouching()) {
                setAutoOutputFluids(!isAutoOutputFluids());
                playerIn.sendSystemMessage(Component.translatable("gtceu.machine.drum." + (autoOutputFluids ? "enable" : "disable") + "_output"));
                return InteractionResult.SUCCESS;
            }
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.SCREWDRIVER) {
            if (side == getOutputFacingFluids()) {
                return isAutoOutputFluids() ? GuiTextures.TOOL_DISABLE_AUTO_OUTPUT : GuiTextures.TOOL_AUTO_OUTPUT;
            }
        }
        return super.sideTips(player, toolType, side);
    }
}
