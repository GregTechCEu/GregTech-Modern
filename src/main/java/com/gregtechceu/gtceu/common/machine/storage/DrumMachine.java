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
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DrumMachine extends MetaMachine implements IAutoOutputFluid, IDropSaveMachine, IInteractedMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DrumMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean autoOutputFluids;
    @Persisted
    protected boolean allowInputFromOutputSideFluids;
    @Getter
    private final int maxStoredFluids;
    @Persisted
    protected final NotifiableFluidTank cache;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportFluidSubs;
    @Persisted(key = "Fluid")
    @DescSynced
    @Getter
    @DropSaved // rename "Fluid" for Item capability
    protected FluidStack stored = FluidStack.EMPTY;
    @Getter
    protected final Material material;

    public DrumMachine(IMachineBlockEntity holder, Material material, int maxStoredFluids, Object... args) {
        super(holder);
        this.material = material;
        this.maxStoredFluids = maxStoredFluids;
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
        return new NotifiableFluidTank(this, 1, maxStoredFluids, IO.BOTH)
                .setFilter(material.getProperty(PropertyKey.FLUID_PIPE));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateStoredFluidFromCache();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        this.exportFluidSubs = cache.addChangedListener(this::onFluidChanged);
    }

    private void onFluidChanged() {
        if (!isRemote()) {
            updateStoredFluidFromCache();
            updateAutoOutputSubscription();
        }
    }

    private void updateStoredFluidFromCache() {
        FluidStack cachedFluid = cache.getFluidInTank(0);
        this.stored = cachedFluid.isEmpty() ? FluidStack.EMPTY : cachedFluid;
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
    // ****** Fluid Logic *******//
    //////////////////////////////////////

    @Override
    public void loadFromItem(CompoundTag tag) {
        IDropSaveMachine.super.loadFromItem(tag);
        if (!tag.contains("Fluid")) {
            stored = FluidStack.EMPTY;
        }
        // "stored" may not be same as cache (due to item's fluid cap). we should update it.
        cache.getStorages()[0].setFluid(stored.copy());
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

    private static boolean canInputFluidsFromOutputSide() {
        return ConfigHolder.INSTANCE.machines.allowDrumsInputFluidsFromOutputSide;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return canInputFluidsFromOutputSide() && this.allowInputFromOutputSideFluids;
    }

    // always is facing down, and can never accept fluids from output side by default
    @Override
    public void setAllowInputFromOutputSideFluids(boolean allow) {
        this.allowInputFromOutputSideFluids = allow;
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction outputFacing) {
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
        if ((isAutoOutputFluids() && !cache.isEmpty()) && outputFacing != null &&
                GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getPos(), outputFacing)) {
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

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isRemote()) {
            if (FluidUtil.interactWithFluidHandler(player, hand, cache)) {
                return InteractionResult.SUCCESS;
            }
        }
        return world.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean saveBreak() {
        return !stored.isEmpty();
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            if (canInputFluidsFromOutputSide()) {
                setAllowInputFromOutputSideFluids(!isAllowInputFromOutputSideFluids());
                playerIn.sendSystemMessage(
                        Component
                                .translatable("gtceu.machine.basic.input_from_output_side." +
                                        (isAllowInputFromOutputSideFluids() ? "allow" : "disallow"))
                                .append(Component.translatable("gtceu.creative.tank.fluid")));
            } else if (!playerIn.isShiftKeyDown()) {
                setAutoOutputFluids(!isAutoOutputFluids());
                playerIn.sendSystemMessage(Component
                        .translatable("gtceu.machine.drum." + (autoOutputFluids ? "enable" : "disable") + "_output"));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    protected InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                  BlockHitResult hitResult) {
        if (!isRemote()) {
            if (!playerIn.isShiftKeyDown()) {
                setAutoOutputFluids(!isAutoOutputFluids());
                playerIn.sendSystemMessage(
                        Component.translatable(
                                "gtceu.machine.drum." + (autoOutputFluids ? "enable" : "disable") + "_output"));
                return InteractionResult.SUCCESS;
            }
        }
        return super.onSoftMalletClick(playerIn, hand, gridSide, hitResult);
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        return super.shouldRenderGrid(player, pos, state, held, toolTypes) ||
                toolTypes.contains(GTToolType.SOFT_MALLET) || toolTypes.contains(GTToolType.SCREWDRIVER);
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.SOFT_MALLET) ||
                (!canInputFluidsFromOutputSide() && toolTypes.contains(GTToolType.SCREWDRIVER))) {
            if (side == getOutputFacingFluids()) {
                return isAutoOutputFluids() ? GuiTextures.TOOL_DISABLE_AUTO_OUTPUT : GuiTextures.TOOL_AUTO_OUTPUT;
            }
        }
        if (canInputFluidsFromOutputSide() && toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getOutputFacingFluids()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        }

        return super.sideTips(player, pos, state, toolTypes, side);
    }
}
