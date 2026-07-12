package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ItemBusPartMachine extends TieredIOPartMachine
                                implements IDistinctPart, IMuiMachine, IPaintable {

    @Getter
    @SaveField
    private final NotifiableItemStackHandler inventory;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isDistinct = false;
    @SaveField
    @SyncToClient
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;

    @Getter
    @SaveField
    protected final ProgrammableCircuitSlotTrait circuitSlot;

    /**
     * Creates an item bus with the default number of slots
     *
     * @param info {@link BlockEntityCreationInfo}
     * @param tier Machine tier.
     * @param io   IO mode of this item bus.
     */
    public ItemBusPartMachine(BlockEntityCreationInfo info, int tier, IO io) {
        this(info, tier, io,
                new NotifiableItemStackHandler(getInventorySize(tier), io, io.support(IO.IN) ? IO.BOTH : io));
    }

    /**
     * Creates an item bus with a custom {@link NotifiableItemStackHandler}.
     *
     * @param info      {@link BlockEntityCreationInfo}
     * @param tier      Machine tier.
     * @param io        IO mode of this item bus.
     * @param inventory The {@link NotifiableItemStackHandler} to attach
     */
    public ItemBusPartMachine(BlockEntityCreationInfo info, int tier, IO io, NotifiableItemStackHandler inventory) {
        super(info, tier, io);
        this.inventory = attachTrait(inventory);
        this.circuitSlot = attachTrait(new ProgrammableCircuitSlotTrait());
        circuitSlot.setEnabled(io == IO.IN);
        filterHandler = FilterHandlers.item(this);

        inventory.setFilter(this::matchesFilter);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected static int getInventorySize(int tier) {
        int sizeRoot = 1 + Math.min(9, tier);
        return sizeRoot * sizeRoot;
    }

    protected boolean matchesFilter(ItemStack stack) {
        if (filterHandler.isFilterPresent())
            return filterHandler.getFilter().test(stack);
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scheduleForNextServerTick(this::updateInventorySubscription);
        getHandlerList().setDistinct(isDistinct);
        getHandlerList().setColor(getPaintingColor());
        inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerList().setColor(color, true);
    }

    @Override
    public void setDistinct(boolean distinct) {
        isDistinct = (io != IO.OUT && distinct);
        syncDataHolder.markClientSyncFieldDirty("isDistinct");
        getHandlerList().setDistinctAndNotify(isDistinct);
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
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateInventorySubscription(newFacing);
    }

    protected void updateInventorySubscription() {
        updateInventorySubscription(getFrontFacing());
    }

    protected void updateInventorySubscription(Direction newFacing) {
        if (isWorkingEnabled() && ((io.support(IO.OUT) && !getInventory().isEmpty()) || io.support(IO.IN)) &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getBlockPos(), newFacing)) {
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
                    getInventory().exportToNearby(getFrontFacing());
                } else if (io == IO.IN) {
                    getInventory().importFromNearby(getFrontFacing());
                } else if (io == IO.BOTH) {
                    getInventory().importFromNearby(getFrontFacing());
                    getInventory().exportToNearby(getFrontFacing().getOpposite());
                }
            }
            updateInventorySubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        InteractionResult superResult = super.onScrewdriverClick(context);
        if (superResult != InteractionResult.PASS) return superResult;
        if (io == IO.BOTH) return InteractionResult.PASS;
        if (context.getPlayer().isShiftKeyDown()) {
            if (swapIO()) {
                return InteractionResult.sidedSuccess(isRemote());
            }
        }
        return InteractionResult.PASS;
    }

    public boolean swapIO() {
        BlockPos blockPos = getBlockPos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = GTMachines.ITEM_EXPORT_BUS[this.getTier()];
        } else if (io == IO.OUT) {
            newDefinition = GTMachines.ITEM_IMPORT_BUS[this.getTier()];
        }

        if (newDefinition == null) return false;
        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof ItemBusPartMachine newMachine) {
            // We don't set the circuit or distinct busses, since
            // that doesn't make sense on an output bus.
            // Furthermore, existing inventory items
            // and conveyors will drop to the floor on block override.
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
            newMachine.setPaintingColor(this.getPaintingColor());
        }
        return true;
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        int rowSize = (int) Math.sqrt(getInventorySize(tier));

        SlotGroup group = new SlotGroup("item_inv", rowSize, 0, true);
        mainWidget.child(new Grid()
                .coverChildren()
                .center()
                .margin(7, 5)
                .gridOfSizeHeight(rowSize * rowSize, rowSize, (x, y, index) -> new ItemSlot()
                        .slot(SyncHandlers.itemSlot(inventory, index)
                                .slotGroup(group)
                                .changeListener((oldStack, newStack, client, init) -> {
                                    // TODO uncomment once mui updated
                                    /*
                                     * if (ItemStack.isSameItem(oldStack, newStack)) {
                                     * inventory.onContentsChanged();
                                     * }
                                     */
                                })
                                .accessibility(inventory.handlerIO.support(IO.IN), true))));
    }
}
