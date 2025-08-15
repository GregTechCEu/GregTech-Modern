package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidHatchPartMachine extends TieredIOPartMachine implements IMachineLife, IHasCircuitSlot, IPaintable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FluidHatchPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    public static final int INITIAL_TANK_CAPACITY_1X = 8 * FluidType.BUCKET_VOLUME;
    public static final int INITIAL_TANK_CAPACITY_4X = 2 * FluidType.BUCKET_VOLUME;
    public static final int INITIAL_TANK_CAPACITY_9X = FluidType.BUCKET_VOLUME;

    @Persisted
    public final NotifiableFluidTank tank;
    private final int slots;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    protected boolean circuitSlotEnabled;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;

    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public FluidHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int initialCapacity, int slots,
                                 Object... args) {
        super(holder, tier, io);
        this.slots = slots;
        this.tank = createTank(initialCapacity, slots, args);
        this.circuitSlotEnabled = true;
        this.circuitInventory = createCircuitItemHandler(io).shouldSearchContent(false);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        return new NotifiableFluidTank(this, slots, getTankCapacity(initialCapacity, getTier()), io);
    }

    public static int getTankCapacity(int initialCapacity, int tier) {
        return initialCapacity * (1 << Math.min(9, tier));
    }

    protected NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                    .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        } else {
            return new NotifiableItemStackHandler(this, 0, IO.NONE);
        }
    }

    @Override
    public void onMachineRemoved() {
        if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
            clearInventory(circuitInventory.storage);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTankSubscription));
        }
        getHandlerList().setColor(getPaintingColor());
        tankSubs = tank.addChangedListener(this::updateTankSubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tankSubs != null) {
            tankSubs.unsubscribe();
            tankSubs = null;
        }
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerList().setColor(color, true);
    }

    @Override
    public void addedToController(IMultiController controller) {
        if (!controller.allowCircuitSlots()) {
            if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
                clearInventory(circuitInventory.storage);
            } else {
                circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
            }
            setCircuitSlotEnabled(false);
        }
        super.addedToController(controller);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        for (var c : controllers) {
            if (!c.allowCircuitSlots()) {
                return;
            }
        }
        setCircuitSlotEnabled(true);
    }

    @Override
    public int tintColor(int index) {
        if (index == 9) return getRealColor();
        return -1;
    }

    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateTankSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateTankSubscription(newFacing);
    }

    protected void updateTankSubscription() {
        updateTankSubscription(getFrontFacing());
    }

    protected void updateTankSubscription(Direction newFacing) {
        if (isWorkingEnabled() && ((io.support(IO.OUT) && !tank.isEmpty()) || io.support(IO.IN)) &&
                GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getPos(), newFacing)) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    tank.exportToNearby(getFrontFacing());
                } else if (io == IO.IN) {
                    tank.importFromNearby(getFrontFacing());
                } else if (io == IO.BOTH) {
                    tank.importFromNearby(getFrontFacing());
                    tank.exportToNearby(getFrontFacing().getOpposite());
                }
            }
            updateTankSubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateTankSubscription();
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        InteractionResult superResult = super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
        if (superResult != InteractionResult.PASS) return superResult;
        if (io == IO.BOTH) return InteractionResult.PASS;
        if (playerIn.isShiftKeyDown()) {
            if (swapIO()) {
                return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public boolean swapIO() {
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;

        if (io == IO.IN) {
            if (this.slots == 1) newDefinition = GTMachines.FLUID_EXPORT_HATCH[this.getTier()];
            else if (this.slots == 4) newDefinition = GTMachines.FLUID_EXPORT_HATCH_4X[this.getTier()];
            else if (this.slots == 9) newDefinition = GTMachines.FLUID_EXPORT_HATCH_9X[this.getTier()];
        } else if (io == IO.OUT) {
            if (this.slots == 1) newDefinition = GTMachines.FLUID_IMPORT_HATCH[this.getTier()];
            else if (this.slots == 4) newDefinition = GTMachines.FLUID_IMPORT_HATCH_4X[this.getTier()];
            else if (this.slots == 9) newDefinition = GTMachines.FLUID_IMPORT_HATCH_9X[this.getTier()];
        }
        if (newDefinition == null) return false;

        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof IMachineBlockEntity newHolder) {
            if (newHolder.getMetaMachine() instanceof FluidHatchPartMachine newMachine) {
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                newMachine.setPaintingColor(this.getPaintingColor());
                for (int i = 0; i < this.tank.getTanks(); i++) {
                    newMachine.tank.setFluidInTank(i, this.tank.getFluidInTank(i));
                }
            }
        }
        return true;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        if (isCircuitSlotEnabled() && this.io == IO.IN) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    @Override
    public Widget createUIWidget() {
        if (slots == 1) {
            return createSingleSlotGUI();
        } else {
            return createMultiSlotGUI();
        }
    }

    protected Widget createSingleSlotGUI() {
        var group = new WidgetGroup(0, 0, 89, 63);
        group.addWidget(new ImageWidget(4, 4, 81, 55, GuiTextures.DISPLAY));
        TankWidget tankWidget;

        // Add input/output-specific widgets
        if (this.io == IO.OUT) {
            // if this is an output hatch, assign tankWidget to the phantom widget displaying the locked fluid...
            group.addWidget(tankWidget = new PhantomFluidWidget(this.tank.getLockedFluid(), 0, 67, 40, 18, 18,
                    () -> this.tank.getLockedFluid().getFluid(), f -> {
                        if (!this.tank.getFluidInTank(0).isEmpty()) {
                            return;
                        }
                        if (f == null || f.isEmpty()) {
                            this.tank.setLocked(false);
                        } else {
                            FluidStack newFluid = f.copy();
                            newFluid.setAmount(1);
                            this.tank.setLocked(true, newFluid);
                        }
                    }).setShowAmount(false).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT));

            group.addWidget(new ToggleButtonWidget(7, 40, 18, 18,
                    GuiTextures.BUTTON_LOCK, this.tank::isLocked, this.tank::setLocked)
                    .setTooltipText("gtceu.gui.fluid_lock.tooltip")
                    .setShouldUseBaseBackground())
                    // ...and add the actual tank widget separately.
                    .addWidget(new TankWidget(tank.getStorages()[0], 67, 22, 18, 18, true, io.support(IO.IN))
                            .setShowAmount(true).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT));
        } else {
            group.addWidget(tankWidget = new TankWidget(tank.getStorages()[0], 67, 22, 18, 18, true, io.support(IO.IN))
                    .setShowAmount(true).setDrawHoverTips(true).setBackground(GuiTextures.FLUID_SLOT));
        }

        group.addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () -> getFluidAmountText(tankWidget)))
                .addWidget(new LabelWidget(8, 28, () -> getFluidNameText(tankWidget).getString()));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private Component getFluidNameText(TankWidget tankWidget) {
        Component translation;
        if (!tank.getFluidInTank(tankWidget.getTank()).isEmpty()) {
            translation = tank.getFluidInTank(tankWidget.getTank()).getDisplayName();
        } else {
            translation = this.tank.getLockedFluid().getFluid().getDisplayName();
        }
        return translation;
    }

    private String getFluidAmountText(TankWidget tankWidget) {
        String fluidAmount = "";
        if (!tank.getFluidInTank(tankWidget.getTank()).isEmpty()) {
            fluidAmount = getFormattedFluidAmount(tank.getFluidInTank(tankWidget.getTank()));
        } else {
            // Display Zero to show information about the locked fluid
            if (!this.tank.getLockedFluid().getFluid().isEmpty()) {
                fluidAmount = "0";
            }
        }
        return fluidAmount;
    }

    public String getFormattedFluidAmount(FluidStack fluidStack) {
        return String.format("%,d", fluidStack.isEmpty() ? 0 : fluidStack.getAmount());
    }

    protected Widget createMultiSlotGUI() {
        int rowSize = (int) Math.sqrt(slots);
        int colSize = rowSize;
        if (slots == 8) {
            rowSize = 4;
            colSize = 2;
        }

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new TankWidget(tank.getStorages()[index++], 4 + x * 18, 4 + y * 18, true, io.support(IO.IN))
                                .setBackground(GuiTextures.FLUID_SLOT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}
