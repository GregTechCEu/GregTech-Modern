package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.LargeStackSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator;
import com.gregtechceu.gtceu.api.machine.trait.CatalystFluidHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.GTValues.UV;
import static com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine.INITIAL_TANK_CAPACITY;
import static com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine.getShareTankSlots;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DualHatchPartMachine extends ItemBusPartMachine {

    @Persisted
    public final NotifiableFluidTank tank;

    @Nullable
    protected ISubscription tankSubs;

    @Getter
    @Persisted
    protected final NotifiableFluidTank shareTank;

    private boolean hasFluidHandler;
    private boolean hasItemHandler;

    public DualHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io, true);
        this.tank = createTank(INITIAL_TANK_CAPACITY, FluidHatchPartMachine.TANKS[tier]);
        this.shareTank = new CatalystFluidHandler(this,
                io == IO.IN ? getShareTankSlots(getTier()) : 0,
                INITIAL_TANK_CAPACITY, IO.IN, IO.NONE)
                .shouldSearchContent(false);
        shareTank.setCapabilityValidator(dir -> false);
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        return new NotifiableFluidTank(this, slots,
                FluidHatchPartMachine.getTankCapacity(initialCapacity, getTier()), io);
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
            this.hasItemHandler = GTTransferUtils.hasAdjacentItemHandler(level, getPos(), getFrontFacing());
            this.hasFluidHandler = GTTransferUtils.hasAdjacentFluidHandler(level, getPos(), getFrontFacing());
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
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;

        if (io == IO.IN) {
            newDefinition = GTMachines.DUAL_EXPORT_HATCH[this.getTier()];
        } else if (io == IO.OUT) {
            newDefinition = GTMachines.DUAL_IMPORT_HATCH[this.getTier()];
        }
        if (newDefinition == null) return false;

        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof IMachineBlockEntity newHolder) {
            if (newHolder.getMetaMachine() instanceof DualHatchPartMachine newMachine) {
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                for (int i = 0; i < this.tank.getTanks(); i++) {
                    newMachine.tank.setFluidInTank(i, this.tank.getFluidInTank(i));
                }
            }
        }
        return true;
    }

    ///////////////////////////////
    // ********** GUI ***********//
    ///////////////////////////////

    @Override
    public Widget createUIWidget() {
        int itemRows = LINE_NUM[getTier()];
        int itemColumns = (getInventorySize() + itemRows - 1) / itemRows;
        int tankRows = FluidHatchPartMachine.LINE_NUM[getTier()];
        int tankColumns = (tank.getTanks() + tankRows - 1) / tankRows;
        int columns = Math.max(itemColumns, tankColumns);
        int rows = itemRows + tankRows;
        if (getTier() == UV) {
            columns = 10;
            rows = 6;
        } else if (getTier() == UHV) {
            columns = 10;
            rows = 10;
        }
        var group = new WidgetGroup(0, 0, 18 * columns + 16, 18 * rows + 16);
        var container = new WidgetGroup(4, 4, 18 * columns + 8, 18 * rows + 8);

        if (getTier() == UHV) {
            int tankIndex = 0;
            for (int x = 0; x < 10; x++) {
                addTankWidget(container, tankIndex++, x, 0);
            }
            for (int y = 1; y < 9; y++) {
                addTankWidget(container, tankIndex++, 0, y);
                addTankWidget(container, tankIndex++, 9, y);
            }
            for (int x = 0; x < 10; x++) {
                addTankWidget(container, tankIndex++, x, 9);
            }
            addItemGrid(container, 1, 1, 8, 8);
        } else if (getTier() == UV) {
            int tankIndex = 0;
            for (int y = 0; y < 6; y++) {
                for (int x = 0; x < 2; x++) {
                    addTankWidget(container, tankIndex++, x, y);
                    addTankWidget(container, tankIndex++, x + 8, y);
                }
            }
            addItemGrid(container, 2, 0, 6, 6);
        } else {
            addItemGrid(container, 0, 0, itemColumns, itemRows);
            addTankGrid(container, 0, itemRows, tankColumns, tankRows);
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }

    private void addItemGrid(WidgetGroup container, int startX, int startY, int columns, int rows) {
        int index = 0;
        for (int y = 0; y < rows && index < getInventorySize(); y++) {
            for (int x = 0; x < columns && index < getInventorySize(); x++) {
                container.addWidget(new LargeStackSlotWidget(
                        getInventory().storage, index++, 4 + (startX + x) * 18, 4 + (startY + y) * 18, true,
                        io.support(IO.IN))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT));
            }
        }
    }

    private void addTankGrid(WidgetGroup container, int startX, int startY, int columns, int rows) {
        int index = 0;
        for (int y = 0; y < rows && index < tank.getTanks(); y++) {
            for (int x = 0; x < columns && index < tank.getTanks(); x++) {
                addTankWidget(container, index++, startX + x, startY + y);
            }
        }
    }

    private void addTankWidget(WidgetGroup container, int index, int x, int y) {
        container.addWidget(new TankWidget(
                tank.getStorages()[index], 4 + x * 18, 4 + y * 18, true, io.support(IO.IN))
                .setBackground(GuiTextures.FLUID_SLOT));
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel left, ConfiguratorPanel right) {
        super.attachConfigurators(left, right);
        if (this.io == IO.IN && shareTank.getTanks() != 0) {
            right.attachConfigurators(new FancyTankConfigurator(
                    shareTank.getStorages(), Component.translatable("gui.gtceu.share_tank.title"))
                    .setTooltips(List.of(
                            Component.translatable("gui.gtceu.share_inventory.desc.1"))));
        }
    }

    @Override
    protected void refundAll(ClickData clickData) {
        super.refundAll(clickData);
        if (!clickData.isRemote) {
            tank.exportToNearby(getFrontFacing());
        }
    }
}
