package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidType;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DualHatchPartMachine extends ItemBusPartMachine {

    public static final int INITIAL_TANK_CAPACITY = 16 * FluidType.BUCKET_VOLUME;
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DualHatchPartMachine.class,
            ItemBusPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableFluidTank tank;

    @Nullable
    protected ISubscription tankSubs;

    private boolean hasFluidHandler;
    private boolean hasItemHandler;

    public DualHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io);
        this.tank = createTank(INITIAL_TANK_CAPACITY, (int) Math.sqrt(getInventorySize()), args);
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

    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        return new NotifiableFluidTank(this, slots, getTankCapacity(initialCapacity, getTier()), io);
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
        int slots = getInventorySize();
        int tanks = (int) Math.sqrt(slots);
        var group = new WidgetGroup(0, 0, 18 * (tanks + 1) + 16, 18 * tanks + 16);
        var container = new WidgetGroup(4, 4, 18 * (tanks + 1) + 8, 18 * tanks + 8);

        int index = 0;
        for (int y = 0; y < tanks; y++) {
            for (int x = 0; x < tanks; x++) {
                container.addWidget(new SlotWidget(
                        getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, io.support(IO.IN))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT));
            }
        }

        index = 0;
        for (int y = 0; y < tanks; y++) {
            container.addWidget(new TankWidget(
                    tank.getStorages()[index++], 4 + tanks * 18, 4 + y * 18, true, io.support(IO.IN))
                    .setBackground(GuiTextures.FLUID_SLOT));
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
