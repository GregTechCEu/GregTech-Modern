package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.msic.FluidBlockTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author KilaBash
 * @date 2023/3/22
 * @implNote PumpMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpMachine extends TieredEnergyMachine implements IAutoOutputFluid, IUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PumpMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);
    public static final int BASE_PUMP_RANGE = 32;
    public static final int EXTRA_PUMP_RANGE = 8;
    public static final int PUMP_SPEED_BASE = 80;
    private final Deque<BlockPos> fluidSourceBlocks = new ArrayDeque<>();
    private final Deque<BlockPos> blocksToCheck = new ArrayDeque<>();
    private boolean initializedQueue = false;
    @DescSynced @Persisted @Getter
    private int pumpHeadY; //TODO RENDER PIPE IN THE FUTURE
    @Getter @Setter @Persisted @DescSynced
    protected boolean autoOutputFluids;
    @Persisted @DropSaved
    protected final NotifiableFluidTank cache;

    public PumpMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.cache = createCacheFluidHandler(args);
        if (isRemote()) {
            addSyncUpdateListener("autoOutputFluids", this::scheduleRender);
        }
    }

    //////////////////////////////////////
    //*****     Initialization     *****//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createCacheFluidHandler(Object... args) {
        return new NotifiableFluidTank(this, 1, 16 * FluidHelper.getBucket() * Math.max(1, getTier()), IO.NONE, IO.OUT);
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    public void setAllowInputFromOutputSideFluids(boolean allow) {
    }

    @Override
    public Direction getOutputFacingFluids() {
        return getFrontFacing();
    }

    @Override
    public void setOutputFacingFluids(Direction outputFacing) {
        setFrontFacing(outputFacing);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscribeServerTick(this::update);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    //////////////////////////////////////
    //*********     Logic     **********//
    //////////////////////////////////////
    private int getMaxPumpRange() {
        return BASE_PUMP_RANGE + EXTRA_PUMP_RANGE * getTier();
    }

    private boolean isStraightInPumpRange(BlockPos checkPos) {
        BlockPos pos = getPos();
        return checkPos.getX() == pos.getX() &&
                checkPos.getZ() == pos.getZ() &&
                pos.getY() < checkPos.getY() &&
                pos.getY() + pumpHeadY >= checkPos.getY();
    }

    private void updateQueueState(int blocksToCheckAmount) {
        BlockPos selfPos = getPos().below(pumpHeadY);

        for (int i = 0; i < blocksToCheckAmount; i++) {
            BlockPos checkPos = null;
            int amountIterated = 0;
            do {
                if (checkPos != null) {
                    blocksToCheck.push(checkPos);
                    amountIterated++;
                }
                checkPos = blocksToCheck.poll();

            } while (checkPos != null &&
                    !getLevel().isLoaded(checkPos) &&
                    amountIterated < blocksToCheck.size());
            if (checkPos != null) {
                checkFluidBlockAt(selfPos, checkPos);
            } else break;
        }

        if (fluidSourceBlocks.isEmpty()) {
            if (getOffsetTimer() % 20 == 0) {
                BlockPos downPos = selfPos.below(1);
                if (downPos.getY() >= getLevel().getMinBuildHeight()) {
                    var downBlock = getLevel().getBlockState(downPos);
                    if (downBlock.getBlock() instanceof LiquidBlock) {
                        this.pumpHeadY++;
                    }
                }

                //schedule queue rebuild because we changed our position and no fluid is available
                this.initializedQueue = false;
            }

            if (!initializedQueue || getOffsetTimer() % 6000 == 0) {
                this.initializedQueue = true;
                //just add ourselves to check list and see how this will go
                this.blocksToCheck.add(selfPos);
            }
        }
    }

    private void checkFluidBlockAt(BlockPos pumpHeadPos, BlockPos checkPos) {
        var blockHere = getLevel().getBlockState(checkPos);
        boolean shouldCheckNeighbours = isStraightInPumpRange(checkPos);

        if (blockHere.getBlock() instanceof LiquidBlock liquidBlock && liquidBlock.getFluidState(blockHere).isSource()) {
            var fluidHandler = new FluidBlockTransfer(liquidBlock, getLevel(), checkPos);
            FluidStack drainStack = fluidHandler.drain(Integer.MAX_VALUE, true);
            if (!drainStack.isEmpty()) {
                this.fluidSourceBlocks.add(checkPos);
            }
            shouldCheckNeighbours = true;
        }

        if (shouldCheckNeighbours) {
            int maxPumpRange = getMaxPumpRange();
            for (var facing : Direction.values()) {
                BlockPos offsetPos = checkPos.relative(facing);
                if (offsetPos.distSqr(pumpHeadPos) > maxPumpRange * maxPumpRange)
                    continue; //do not add blocks outside bounds
                if (!fluidSourceBlocks.contains(offsetPos) &&
                        !blocksToCheck.contains(offsetPos)) {
                    this.blocksToCheck.add(offsetPos);
                }
            }
        }
    }

    private void tryPumpFirstBlock() {
        BlockPos fluidBlockPos = fluidSourceBlocks.poll();
        if (fluidBlockPos == null) return;
        var blockHere = getLevel().getBlockState(fluidBlockPos);
        if (blockHere.getBlock() instanceof LiquidBlock liquidBlock && liquidBlock.getFluidState(blockHere).isSource()) {
            var fluidHandler = new FluidBlockTransfer(liquidBlock, getLevel(), fluidBlockPos);
            FluidStack drainStack = fluidHandler.drain(Integer.MAX_VALUE, true);
            if (!drainStack.isEmpty() && cache.fillInternal(drainStack, true) == drainStack.getAmount()) {
                cache.fillInternal(drainStack, false);
                fluidHandler.drain(drainStack, false);
                getLevel().setBlockAndUpdate(fluidBlockPos, Blocks.AIR.defaultBlockState());
                this.fluidSourceBlocks.remove(fluidBlockPos);
                energyContainer.changeEnergy(-GTValues.V[getTier()] * 2);
            }
        }
    }

    public void update() {
        cache.exportToNearby(getOutputFacingFluids());

        //do not do anything without enough energy supplied
        if (energyContainer.getEnergyStored() < GTValues.V[getTier()] * 2) {
            return;
        }
        updateQueueState(getTier());
        if (getOffsetTimer() % getPumpingCycleLength() == 0 && !fluidSourceBlocks.isEmpty()) {
            tryPumpFirstBlock();
        }
    }

    private int getPumpingCycleLength() {
        return PUMP_SPEED_BASE / (1 << (getTier() - 1));
    }

    //////////////////////////////////////
    //**********     Gui     ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new ImageWidget(7, 16, 81, 55, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gtceu.gui.fluid_amount"))
                .widget(new LabelWidget(11, 30, () -> cache.getFluidInTank(0).getAmount() + "").setTextColor(-1).setDropShadow(true))
                .widget(new LabelWidget(6, 6, getBlockState().getBlock().getDescriptionId()))
                .widget(new TankWidget(cache.storages[0], 90, 35, true, true)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .widget(new ToggleButtonWidget(7, 53, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
    //////////////////////////////////////
    @Override
    public ResourceTexture sideTips(Player player, GTToolType toolType, Direction side) {
        if (toolType == GTToolType.WRENCH) {
            if (player.isCrouching()) {
                if (hasFrontFacing() && side != this.getFrontFacing() && isFacingValid(side)) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        }
        return super.sideTips(player, toolType, side);
    }

}
