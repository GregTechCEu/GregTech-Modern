package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidType;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.ItemSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DualHatchPartMachine extends ItemBusPartMachine {

    public static final int INITIAL_TANK_CAPACITY = 16 * FluidType.BUCKET_VOLUME;

    @SaveField
    public final NotifiableFluidTank tank;

    @Nullable
    protected ISubscription tankSubs;

    private boolean hasFluidHandler;
    private boolean hasItemHandler;

    public DualHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
        this.tank = attachTrait(new NotifiableFluidTank((int) Math.sqrt(getInventorySize()),
                getTankCapacity(INITIAL_TANK_CAPACITY, getTier()), io));
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    public static int getTankCapacity(int initialCapacity, int tier) {
        return initialCapacity * (1 << (tier - 6));
    }

    @Override
    public int getInventorySize() {
        return (int) Math.pow((getTier() - 4), 2);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tankSubs = tank.addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
    }

    ///////////////////////////////
    // ******** Auto IO *********//
    ///////////////////////////////

    @Override
    protected void updateInventorySubscription() {
        boolean canOutput = io == IO.OUT && (!tank.isEmpty() || !getInventory().isEmpty());
        var level = getLevel();
        if (level != null) {
            this.hasItemHandler = GTTransferUtils.hasAdjacentItemHandler(level, getBlockPos(), getFrontFacing());
            this.hasFluidHandler = GTTransferUtils.hasAdjacentFluidHandler(level, getBlockPos(), getFrontFacing());
        } else {
            this.hasItemHandler = false;
            this.hasFluidHandler = false;
        }

        if (isWorkingEnabled() && (canOutput || io == IO.IN) && (hasItemHandler || hasFluidHandler)) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    if (hasItemHandler) {
                        getInventory().exportToNearby(getFrontFacing());
                    }
                    if (hasFluidHandler) {
                        tank.exportToNearby(getFrontFacing());
                    }
                } else if (io == IO.IN) {
                    if (hasItemHandler) {
                        getInventory().importFromNearby(getFrontFacing());
                    }
                    if (hasFluidHandler) {
                        tank.importFromNearby(getFrontFacing());
                    }
                }
            }
            updateInventorySubscription();
        }
    }

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getBlockPos();
        MachineDefinition newDefinition = null;

        if (io == IO.IN) {
            newDefinition = GTMachines.DUAL_EXPORT_HATCH[this.getTier()];
        } else if (io == IO.OUT) {
            newDefinition = GTMachines.DUAL_IMPORT_HATCH[this.getTier()];
        }
        if (newDefinition == null) return false;

        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof DualHatchPartMachine newMachine) {
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
            for (int i = 0; i < this.tank.getTanks(); i++) {
                newMachine.tank.setFluidInTank(i, this.tank.getFluidInTank(i));
            }
        }
        return true;
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        String slotGroupName = "inv_slot_group";
        SlotGroup slotGroup = new SlotGroup(slotGroupName, getInventorySize());
        mainWidget.child(SlotGroupWidget.builder()
                .matrix(Arrays.stream(GTMuiMachineUtil.createSquareMatrix(getInventorySize(), 'I'))
                        .map(s -> s + 'F')
                        .toArray(String[]::new))
                .key('I', i -> {
                    ModularSlot slot = new ModularSlot(getInventory(), i)
                            .accessibility(io.support(IO.IN), true);
                    ItemSlotSyncHandler syncHandler = new ItemSlotSyncHandler(slot.slotGroup(slotGroup));
                    syncManager.syncValue(slotGroupName, i, syncHandler);
                    return new ItemSlot().syncHandler(slotGroupName, i);
                })
                .key('F', i -> {
                    FluidSlotSyncHandler syncHandler = new FluidSlotSyncHandler(tank.getStorages()[i])
                            .canFillSlot(io.support(IO.IN))
                            .canDrainSlot(true);
                    syncManager.syncValue(slotGroupName + "_fluid", i, syncHandler);
                    return new FluidSlot().syncHandler(slotGroupName + "_fluid", i);
                }).build().margin(7, 10).center());
    }
}
