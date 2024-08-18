package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DualHatchPartMachine extends ItemBusPartMachine {

    public static final int INITIAL_TANK_CAPACITY = 16 * FluidHelper.getBucket();
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(DualHatchPartMachine.class,
            ItemBusPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableFluidTank tank;

    @Nullable
    protected ISubscription tankSubs;

    private boolean hasFluidTransfer;
    private boolean hasItemTransfer;

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
            this.hasItemTransfer = ItemTransferHelper.getItemTransfer(
                    level, getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null;
            this.hasFluidTransfer = FluidTransferHelper.getFluidTransfer(
                    level, getPos().relative(getFrontFacing()), getFrontFacing().getOpposite()) != null;
        } else {
            this.hasItemTransfer = false;
            this.hasFluidTransfer = false;
        }

        if (isWorkingEnabled() && (canOutput || io == IO.IN) && (hasItemTransfer || hasFluidTransfer)) {
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
                    if (hasItemTransfer) {
                        getInventory().exportToNearby(getFrontFacing());
                    }
                    if (hasFluidTransfer) {
                        tank.exportToNearby(getFrontFacing());
                    }
                } else if (io == IO.IN) {
                    if (hasItemTransfer) {
                        getInventory().importFromNearby(getFrontFacing());
                    }
                    if (hasFluidTransfer) {
                        tank.importFromNearby(getFrontFacing());
                    }
                }
            }
            updateInventorySubscription();
        }
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
    public boolean isDistinct() {
        return super.isDistinct() && tank.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        super.setDistinct(isDistinct);
        tank.setDistinct(isDistinct);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
