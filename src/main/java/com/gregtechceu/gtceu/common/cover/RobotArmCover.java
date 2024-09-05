package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.gregtechceu.gtceu.common.pipelike.net.item.IItemTransferController;
import com.gregtechceu.gtceu.common.pipelike.net.item.ItemEQTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.item.ItemRRTraverseData;
import com.gregtechceu.gtceu.common.pipelike.net.item.ItemTraverseData;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RobotArmCover extends ConveyorCover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RobotArmCover.class,
            ConveyorCover.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    @Getter
    protected TransferMode transferMode;
    protected boolean noTransferDueToMinimum = false;

    @Persisted
    @Getter
    protected int globalTransferLimit;
    protected int itemsTransferBuffered;

    private IntInputWidget stackSizeInput;

    public RobotArmCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);

        setTransferMode(TransferMode.TRANSFER_ANY);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_arm"),
                GTCEu.id("block/cover/overlay_arm_emissive")).build();
    }

    @Override
    protected CoverRenderer buildRendererInverted() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_arm"),
                GTCEu.id("block/cover/overlay_arm_inverted_emissive")).build();
    }

    @Override
    protected void refreshBuffer(int transferRate) {
        if (this.transferMode == TransferMode.TRANSFER_EXACT && noTransferDueToMinimum) {
            if (getFilterHandler().isFilterPresent()) {
                this.noTransferDueToMinimum = false;
                this.itemsLeftToTransferLastSecond += transferRate;
                int max = getFilterHandler().getFilter().getMaxTransferSize();
                if (this.itemsLeftToTransferLastSecond > max) {
                    this.itemsLeftToTransferLastSecond = max;
                }
                return;
            }
        }
        super.refreshBuffer(transferRate);
    }

    @Override
    protected void performTransferOnUpdate(@NotNull IItemTransfer sourceHandler, @NotNull IItemTransfer destHandler) {
        if (transferMode == TransferMode.TRANSFER_ANY) {
            super.performTransferOnUpdate(sourceHandler, destHandler);
            return;
        }
        if (!getFilterHandler().isFilterPresent()) return;
        ItemFilter filter = getFilterHandler().getFilter();
        if (transferMode == TransferMode.KEEP_EXACT) {
            IntUnaryOperator maxflow = s -> Math.min(filter.getTransferLimit(s), getItemsLeftToTransfer());
            reportItemsTransfer(performTransfer(sourceHandler, destHandler, true, s -> 0, maxflow, null));
        } else if (transferMode == TransferMode.TRANSFER_EXACT) {
            IntUnaryOperator maxflow = s -> {
                int limit = filter.getTransferLimit(s);
                if (getItemsLeftToTransfer() < limit) {
                    noTransferDueToMinimum = true;
                    return 0;
                } else return limit;
            };
            performTransfer(sourceHandler, destHandler, true, maxflow, maxflow, (a, b) -> reportItemsTransfer(b));
        }
    }

    @Override
    protected @NotNull ItemTraverseData getTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                              long queryTick, BlockPos sourcePos, Direction inputFacing) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepItemTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }
        return super.getTD(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Override
    protected @NotNull ItemEQTraverseData getEQTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                                  long queryTick, BlockPos sourcePos, Direction inputFacing) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepItemEQTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }
        return super.getEQTD(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    @Override
    protected @NotNull ItemRRTraverseData getRRTD(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator,
                                                  long queryTick, BlockPos sourcePos, Direction inputFacing,
                                                  boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            return new KeepItemRRTraverseData(net, testObject, simulator, queryTick, sourcePos, inputFacing,
                    getRoundRobinCache(simulate));
        }
        return super.getRRTD(net, testObject, simulator, queryTick, sourcePos, inputFacing, simulate);
    }

    @Override
    protected int simpleInsert(@NotNull IItemTransfer destHandler, ItemTestObject testObject, int count,
                               boolean simulate) {
        if (transferMode == TransferMode.KEEP_EXACT) {
            assert getFilterHandler().isFilterPresent();
            int kept = getFilterHandler().getFilter().getTransferLimit(testObject.recombine());
            count = Math.min(count, kept - computeContained(destHandler, testObject));
        }
        return super.simpleInsert(destHandler, testObject, count, simulate);
    }

    public void setTransferMode(TransferMode transferMode) {
        if (this.transferMode != transferMode) {
            this.transferMode = transferMode;
            this.coverHolder.markDirty();
            configureStackSizeInput();
            this.getFilterHandler().getFilter().setMaxTransferSize(transferMode.maxStackSize);
        }
    }

    @Override
    public int insertToHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemTransfer destHandler,
                               boolean simulate) {
        if (io == IO.OUT) {
            if (transferMode == TransferMode.KEEP_EXACT) {
                int contained = computeContained(destHandler, testObject);
                assert getFilterHandler().isFilterPresent();
                int keep = getFilterHandler().getFilter().getTransferLimit(testObject.recombine());
                if (contained >= keep) return amount;
                int allowed = Math.min(keep - contained, amount);
                return (amount - allowed) + super.insertToHandler(testObject, allowed, destHandler, simulate);
            } else if (transferMode == TransferMode.TRANSFER_EXACT) {
                assert getFilterHandler().isFilterPresent();
                int required = getFilterHandler().getFilter().getTransferLimit(testObject.recombine());
                if (amount < required) return amount;
                return (amount - required) + super.insertToHandler(testObject, required, destHandler, simulate);
            }
        }
        return super.insertToHandler(testObject, amount, destHandler, simulate);
    }

    @Override
    public int extractFromHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemTransfer sourceHandler,
                                  boolean simulate) {
        if (io == IO.IN) {
            // TODO should extraction instead be ignored for transfer exact?
            if (transferMode == TransferMode.TRANSFER_EXACT) {
                assert getFilterHandler().isFilterPresent();
                int required = getFilterHandler().getFilter().getTransferLimit(testObject.recombine());
                if (amount < required) return 0;
                else amount = required;
            }
        }
        return super.extractFromHandler(testObject, amount, sourceHandler, simulate);
    }

    private int getFilteredItemAmount(ItemStack itemStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferLimit;

        ItemFilter filter = filterHandler.getFilter();
        return filter.supportsAmounts() ? filter.testItemCount(itemStack) : globalTransferLimit;
    }

    public int getBuffer() {
        return itemsTransferBuffered;
    }

    public void buffer(int amount) {
        itemsTransferBuffered += amount;
    }

    public void clearBuffer() {
        itemsTransferBuffered = 0;
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    @NotNull
    protected String getUITitle() {
        return "cover.robotic_arm.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), transferMode, this::setTransferMode));

        this.stackSizeInput = new IntInputWidget(64, 45, 80, 20,
                () -> globalTransferLimit, val -> globalTransferLimit = val);
        configureStackSizeInput();

        group.addWidget(this.stackSizeInput);
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleItemFilter filter) {
            filter.setMaxStackSize(filter.isBlackList() ? 1 : transferMode.maxStackSize);
        }

        configureStackSizeInput();
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null)
            return;

        this.stackSizeInput.setVisible(shouldShowStackSize());
        this.stackSizeInput.setMin(1);
        this.stackSizeInput.setMax(this.transferMode.maxStackSize);
    }

    private boolean shouldShowStackSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return !this.filterHandler.getFilter().supportsAmounts();
    }

    protected int computeContained(@NotNull IItemTransfer handler, @NotNull ItemTestObject testObject) {
        int found = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack contained = handler.getStackInSlot(i);
            if (testObject.test(contained)) {
                found += contained.getCount();
            }
        }
        return found;
    }

    protected class KeepItemTraverseData extends ItemTraverseData {

        public KeepItemTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                                    BlockPos sourcePos, Direction inputFacing) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                if (destination.getEquivalencyData().equals(sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IItemHandler containerCap = capability.getValue()
                        .getCapability(ForgeCapabilities.ITEM_HANDLER,
                                capability.getKey().getOpposite())
                        .resolve().orElse(null);
                if (containerCap != null) {
                    IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(containerCap);
                    int contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow = IItemTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, availableFlow), container,
                                    getSimulatorKey() != null);
                }
            }
            return flowReachingDestination - availableFlow;
        }
    }

    protected class KeepItemEQTraverseData extends ItemEQTraverseData {

        public KeepItemEQTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                                      BlockPos sourcePos, Direction inputFacing) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode node, long flowReachingNode,
                                          int expectedDestinations) {
            long availableFlow = flowReachingNode;
            long flowPerDestination = flowReachingNode / expectedDestinations;
            if (flowPerDestination == 0) return 0;
            for (var capability : node.getBlockEntity().getTargetsWithCapabilities(node).entrySet()) {
                if (node.getEquivalencyData().equals(sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IItemHandler containerCap = capability.getValue()
                        .getCapability(ForgeCapabilities.ITEM_HANDLER,
                                capability.getKey().getOpposite())
                        .resolve().orElse(null);
                if (containerCap != null) {
                    IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(containerCap);
                    int contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow = IItemTransferController.CONTROL.get(node.getBlockEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, flowPerDestination), container, simulating());
                }
            }
            return flowReachingNode - availableFlow;
        }

        @Override
        public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
                if (destination.getEquivalencyData().equals(sourcePos) &&
                        capability.getKey() == inputFacing)
                    continue; // anti insert-to-our-source logic

                IItemHandler containerCap = capability.getValue()
                        .getCapability(ForgeCapabilities.ITEM_HANDLER,
                                capability.getKey().getOpposite())
                        .resolve().orElse(null);
                if (containerCap != null) {
                    IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(containerCap);
                    int contained = computeContained(container, getTestObject());
                    assert getFilterHandler().isFilterPresent();
                    int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                    if (contained >= kept) continue;
                    availableFlow = IItemTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                            .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                    (int) Math.min(kept - contained, availableFlow), container,
                                    getSimulatorKey() != null);
                }
            }
            return flowReachingDestination - availableFlow;
        }
    }

    protected class KeepItemRRTraverseData extends ItemRRTraverseData {

        public KeepItemRRTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                                      BlockPos sourcePos, Direction inputFacing,
                                      @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> cache) {
            super(net, testObject, simulator, queryTick, sourcePos, inputFacing, cache);
        }

        @Override
        public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IItemHandler> data,
                                          @NotNull WorldPipeNetNode destination,
                                          long flowReachingDestination) {
            long availableFlow = flowReachingDestination;
            Direction pointerFacing = data.getPointerFacing(getSimulatorKey());
            if (destination.getEquivalencyData().equals(sourcePos) && pointerFacing == inputFacing)
                return 0; // anti insert-to-our-source logic

            IItemHandler containerCap = data.getAtPointer(destination, getSimulatorKey());
            if (containerCap != null) {
                IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(containerCap);
                int contained = computeContained(container, getTestObject());
                assert getFilterHandler().isFilterPresent();
                int kept = getFilterHandler().getFilter().getTransferLimit(getTestObject().recombine());
                if (contained >= kept) return 0;
                availableFlow = IItemTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                        .getCoverAtSide(pointerFacing)).insertToHandler(getTestObject(),
                                (int) Math.min(kept - contained, availableFlow), container,
                                getSimulatorKey() != null);
            }
            return flowReachingDestination - availableFlow;
        }
    }
}
