package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidHatchPartMachine extends TieredIOPartMachine implements IMuiMachine, IHasCircuitSlot, IPaintable {

    public static final int INITIAL_TANK_CAPACITY_1X = 8 * FluidType.BUCKET_VOLUME;
    public static final int INITIAL_TANK_CAPACITY_4X = 2 * FluidType.BUCKET_VOLUME;
    public static final int INITIAL_TANK_CAPACITY_9X = FluidType.BUCKET_VOLUME;

    @SaveField
    public final NotifiableFluidTank tank;
    private final int slots;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;
    @Getter
    @SaveField
    @SyncToClient
    protected boolean circuitSlotEnabled;
    @Getter
    @SaveField
    protected final NotifiableItemStackHandler circuitInventory;

    public FluidHatchPartMachine(BlockEntityCreationInfo info, int tier, IO io, int initialCapacity, int slots) {
        super(info, tier, io);
        this.slots = slots;
        this.tank = attachTrait(createTank(initialCapacity, slots));

        if (io == IO.IN) {
            this.circuitSlotEnabled = true;
            this.circuitInventory = attachTrait(new NotifiableItemStackHandler(1, IO.IN, IO.NONE))
                    .setFilter(IntCircuitBehaviour::isIntegratedCircuit).shouldSearchContent(false)
                    .shouldDropInventoryInWorld(!ConfigHolder.INSTANCE.machines.ghostCircuit);
        } else {
            this.circuitSlotEnabled = false;
            this.circuitInventory = attachTrait(new NotifiableItemStackHandler(0, IO.NONE)).shouldSearchContent(false);
        }
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableFluidTank createTank(int initialCapacity, int slots) {
        return new NotifiableFluidTank(slots, getTankCapacity(initialCapacity, getTier()), io);
    }

    public static int getTankCapacity(int initialCapacity, int tier) {
        return initialCapacity * (1 << Math.min(9, tier));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scheduleForNextServerTick(this::updateTankSubscription);
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
    public void addedToController(MultiblockControllerMachine controller) {
        if (!controller.allowCircuitSlots()) {
            if (!ConfigHolder.INSTANCE.machines.ghostCircuit) {
                circuitInventory.dropInventoryInWorld();
            } else {
                circuitInventory.setStackInSlot(0, ItemStack.EMPTY);
            }
            setCircuitSlotEnabled(false);
        }
        super.addedToController(controller);
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
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

    public void setCircuitSlotEnabled(boolean enabled) {
        circuitSlotEnabled = enabled;
        syncDataHolder.markClientSyncFieldDirty("circuitSlotEnabled");
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
                GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), newFacing)) {
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
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        InteractionResult superResult = super.onScrewdriverClick(context);
        if (superResult != InteractionResult.PASS) return superResult;
        if (io == IO.BOTH) return InteractionResult.PASS;
        if (context.getPlayer().isShiftKeyDown()) {
            if (swapIO()) {
                return InteractionResult.sidedSuccess(getLevel().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public boolean swapIO() {
        BlockPos blockPos = getBlockPos();
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

        if (getLevel().getBlockEntity(blockPos) instanceof FluidHatchPartMachine newMachine) {
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
            newMachine.setPaintingColor(this.getPaintingColor());
            for (int i = 0; i < this.tank.getTanks(); i++) {
                newMachine.tank.setFluidInTank(i, this.tank.getFluidInTank(i));
            }
        }
        return true;
    }

    private Component getFluidNameText() {
        return this.tank.getFluidInTank(0).isEmpty() ?
                Component.translatable("gtceu.fluid.empty") :
                this.tank.getFluidInTank(0).getDisplayName();
    }

    private Component getFluidAmountText() {
        return Component.literal(FormattingUtil.formatBuckets(this.tank.getFluidInTank(0).getAmount()));
    }

    private Component getFluidText() {
        return getFluidNameText().copy().append("\n").append(getFluidAmountText());
    }

    public String getFormattedFluidAmount(FluidStack fluidStack) {
        return String.format("%,d", fluidStack.isEmpty() ? 0 : fluidStack.getAmount());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        mainWidget.child(slots == 1 ? createSingleSlotUI(syncManager) : createMultiSlotUI(syncManager));
    }

    protected Flow createSingleSlotUI(PanelSyncManager syncManager) {
        BooleanSyncValue locked = new BooleanSyncValue(this.tank::isLocked, this.tank::setLocked);
        syncManager.syncValue("locked", locked);
        return Flow.col()
                .width(MachineUIPanel.DEFAULT_CONTENT_WIDTH)
                .height(60)
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .childPadding(4)
                .child(new TextWidget<>(Text.dynamic(this::getFluidNameText))
                        .horizontalCenter())
                .child(new TextWidget<>(Text.dynamic(this::getFluidAmountText))
                        .horizontalCenter())
                .child(Flow.row()
                        .childPadding(2)
                        .coverChildren()
                        .childIf(io.support(IO.OUT), () -> new FluidSlot()
                                .name("lockedFluid")
                                .syncHandler(new FluidSlotSyncHandler(tank.getLockedFluid()))
                                .alwaysShowFull(true)
                                .tooltip(t -> t.addLine("Locked Fluid")))
                        .childIf(io.support(IO.OUT), () -> new ToggleButton()
                                .syncHandler("locked")
                                .tooltip(t -> t.addLine("gtceu.gui.fluid_lock.tooltip"))
                                .overlay(false, GTGuiTextures.BUTTON_LOCK)
                                .overlay(true, GTGuiTextures.BUTTON_LOCK)
                                .background(GuiTextures.MC_BUTTON)
                                .selectedBackground(GuiTextures.MC_BUTTON_PRESSED)

                        )
                        .child(new FluidSlot()
                                .name("regularFluid")
                                .syncHandler(new FluidSlotSyncHandler(tank.getStorages()[0])
                                        .canFillSlot(io.support(IO.IN)))));
    }

    protected SlotGroupWidget createMultiSlotUI(PanelSyncManager syncManager) {
        return GTMuiMachineUtil.createSlotGroupFromInventory(
                syncManager,
                tank, "fluid_inv",
                slots, 'F', GTMuiMachineUtil.createSquareMatrix(slots, 'F'));
    }
}
