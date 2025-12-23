package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputBoth;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BufferMachine extends TieredMachine implements IMachineLife, IAutoOutputBoth, IMuiMachine {

    public static final int TANK_SIZE = 64000;

    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected Direction outputFacingItems;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected Direction outputFacingFluids;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputItems;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputFluids;
    @Getter
    @Setter
    @SaveField
    protected boolean allowInputFromOutputSideItems;
    @Getter
    @Setter
    @SaveField
    protected boolean allowInputFromOutputSideFluids;

    @SaveField
    @Getter
    protected final NotifiableItemStackHandler inventory;

    @SaveField
    @Getter
    protected final NotifiableFluidTank tank;

    @Nullable
    protected TickableSubscription autoOutputSubs;

    @Nullable
    protected ISubscription invSubs, tankSubs;

    public BufferMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier);
        this.inventory = createInventory(args);
        this.tank = createTank(args);
    }

    ////////////////////////////////
    // ***** Initialization ******//
    ////////////////////////////////

    public static int getInventorySize(int tier) {
        return (int) Math.pow(tier + 2, 2);
    }

    public static int getTankSize(int tier) {
        return tier + 2;
    }

    protected NotifiableItemStackHandler createInventory(Object... args) {
        return new NotifiableItemStackHandler(this, getInventorySize(tier), IO.BOTH);
    }

    protected NotifiableFluidTank createTank(Object... args) {
        return new NotifiableFluidTank(this, getTankSize(tier), TANK_SIZE, IO.BOTH);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
        this.invSubs = inventory.addChangedListener(this::updateAutoOutputSubscription);
        this.tankSubs = tank.addChangedListener(this::updateAutoOutputSubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (invSubs != null) {
            invSubs.unsubscribe();
            this.invSubs = null;
        }

        if (tankSubs != null) {
            tankSubs.unsubscribe();
            this.tankSubs = null;
        }
    }

    ////////////////////////////////
    // ******* Auto Output *******//
    ////////////////////////////////

    @Override
    public void setAutoOutputFluids(boolean allow) {
        this.autoOutputFluids = allow;
        syncDataHolder.markClientSyncFieldDirty("autoOutputFluids");
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction outputFacing) {
        this.outputFacingFluids = outputFacing;
        syncDataHolder.markClientSyncFieldDirty("outputFacingFluids");
        updateAutoOutputSubscription();
    }

    @Override
    public void setAutoOutputItems(boolean allow) {
        this.autoOutputItems = allow;
        syncDataHolder.markClientSyncFieldDirty("autoOutputItems");
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        syncDataHolder.markClientSyncFieldDirty("outputFacingItems");
        updateAutoOutputSubscription();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        var outputFacingItems = getOutputFacingItems();
        var outputFacingFluids = getOutputFacingFluids();
        if ((isAutoOutputItems() && !inventory.isEmpty() && outputFacingItems != null &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), outputFacingItems)) ||
                (isAutoOutputFluids() && !tank.isEmpty() && outputFacingFluids != null &&
                        GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getPos(), outputFacingFluids))) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputFluids() && getOutputFacingFluids() != null) {
                tank.exportToNearby(getOutputFacingFluids());
            }
            if (isAutoOutputItems() && getOutputFacingItems() != null) {
                inventory.exportToNearby(getOutputFacingItems());
            }
        }
        updateAutoOutputSubscription();
    }

    ////////////////////////////////
    // ********** GUI *********** //
    ////////////////////////////////

    // TODO MUI: Needs EIO widget
    @Override
    public @NotNull ModularPanel buildUI(@NotNull PosGuiData data, @NotNull PanelSyncManager syncManager,
                                         @NotNull UISettings settings) {
        for (int i = 0; i < tank.getTanks(); i++) {
            syncManager.syncValue("fluids", i, SyncHandlers.fluidSlot(tank.getStorages()[i]));
        }

        SlotGroup slotGroup = new SlotGroup("inventory", inventory.getSlots());

        int size = tank.getTanks();
        String[] matrix = new String[size];
        for (int i = 0; i < size; i++) {
            var row = new StringBuilder(size + 1);
            for (int j = 0; j < size; j++) {
                row.append("I");
            }
            row.append("F");
            matrix[i] = row.toString();
        }

        SlotGroupWidget slotWidget = SlotGroupWidget.builder()
                .matrix(matrix)
                .key('I', i -> new ItemSlot()
                        .slot(new ModularSlot(inventory, i)
                                .slotGroup(slotGroup)))
                .key('F', i -> new FluidSlot()
                        .syncHandler("fluids", i))
                .build();

        return new ModularPanel(this.getDefinition().getName())
                .size(176, 100 + (18 * size))
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(1)
                        .height(20 + 18 * size)
                        .child(Flow.row()
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .align(Alignment.CENTER)
                                .coverChildren()
                                .child(slotWidget
                                        .marginLeft(30)
                                        .marginRight(30)
                                        .verticalCenter())))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(createAutoOutputItemButton(syncManager))
                        .child(createAutoOutputFluidButton(syncManager))
                        .child(createInputFromOutputItem(syncManager))
                        .child(createInputFromOutputFluid(syncManager))
                        .excludeAreaInXei());
    }

    private ToggleButton createAutoOutputItemButton(PanelSyncManager syncManager) {
        BooleanSyncValue itemOutputs = new BooleanSyncValue(this::isAutoOutputItems,
                this::setAutoOutputItems);
        syncManager.syncValue("auto_output_items", itemOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(itemOutputs::getBoolValue, itemOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_auto_output",
                        Component.translatable(itemOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    private ToggleButton createAutoOutputFluidButton(PanelSyncManager syncManager) {
        BooleanSyncValue fluidOutputs = new BooleanSyncValue(this::isAutoOutputFluids,
                this::setAutoOutputFluids);
        syncManager.syncValue("auto_output_fluids", fluidOutputs);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(fluidOutputs::getBoolValue, fluidOutputs::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.fluid_auto_output",
                        Component.translatable(fluidOutputs.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    private ToggleButton createInputFromOutputItem(PanelSyncManager syncManager) {
        BooleanSyncValue inputFromOutputItem = new BooleanSyncValue(this::isAllowInputFromOutputSideItems,
                this::setAllowInputFromOutputSideItems);
        syncManager.syncValue("input_from_output_item", inputFromOutputItem);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(inputFromOutputItem::getBoolValue, inputFromOutputItem::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                .tooltipAutoUpdate(true)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.item_input_from_output",
                        Component.translatable(inputFromOutputItem.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }

    private ToggleButton createInputFromOutputFluid(PanelSyncManager syncManager) {
        BooleanSyncValue inputFromOutputFluid = new BooleanSyncValue(this::isAllowInputFromOutputSideFluids,
                this::setAllowInputFromOutputSideFluids);
        syncManager.syncValue("input_from_output_fluid", inputFromOutputFluid);
        return new ToggleButton()
                .value(new BoolValue.Dynamic(inputFromOutputFluid::getBoolValue, inputFromOutputFluid::setBoolValue))
                .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                .tooltipBuilder((r) -> r.addLine(IKey.lang(Component.translatable("gtceu.gui.fluid_input_from_output",
                        Component.translatable(inputFromOutputFluid.getBoolValue() ? "cover.voiding.label.enabled" :
                                "cover.voiding.label.disabled")))));
    }
    /*
     * @Override
     * public Widget createUIWidget() {
     * int invTier = getTankSize(tier);
     * var group = new WidgetGroup(0, 0, 18 * (invTier + 1) + 16, 18 * invTier + 16);
     * var container = new WidgetGroup(4, 4, 18 * (invTier + 1) + 8, 18 * invTier + 8);
     *
     * int index = 0;
     * for (int y = 0; y < invTier; y++) {
     * for (int x = 0; x < invTier; x++) {
     * container.addWidget(new SlotWidget(
     * getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, true)
     * .setBackgroundTexture(GuiTextures.SLOT));
     * }
     * }
     *
     * index = 0;
     * for (int y = 0; y < invTier; y++) {
     * container.addWidget(new TankWidget(
     * tank.getStorages()[index++], 4 + invTier * 18, 4 + y * 18, true, true)
     * .setBackground(GuiTextures.FLUID_SLOT));
     * }
     *
     * container.setBackground(GuiTextures.BACKGROUND_INVERSE);
     * group.addWidget(container);
     * return group;
     * }
     */

    ///////////////////////////////
    // ******* Rendering ********//
    ///////////////////////////////
    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getOutputFacingItems() || side == getOutputFacingFluids()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }

    ////////////////////////////////
    // ********** Misc ***********//
    ////////////////////////////////

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }
}
