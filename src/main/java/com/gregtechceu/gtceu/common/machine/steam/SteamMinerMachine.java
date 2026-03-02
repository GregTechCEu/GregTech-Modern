package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.api.machine.trait.ExhaustVentMachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Grid;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.trait.miner.SteamMinerLogic;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamMinerMachine extends SteamWorkableMachine implements IControllable,
                               IDataInfoProvider, IMiner, IMuiMachine {

    @SaveField
    public final NotifiableItemStackHandler importItems;
    @SaveField
    public final NotifiableItemStackHandler exportItems;
    private final int inventorySize;
    private final int energyPerTick;
    @Nullable
    protected TickableSubscription autoOutputSubs;
    @Nullable
    protected ISubscription exportItemSubs;

    @Getter
    private final ExhaustVentMachineTrait exhaustVentTrait;

    public SteamMinerMachine(BlockEntityCreationInfo info, boolean isHighPressure, int speed, int maximumRadius,
                             int fortune, int energyPerTick) {
        super(info, isHighPressure, (m) -> new SteamMinerLogic(m, fortune, speed, maximumRadius));

        this.inventorySize = 4;
        this.energyPerTick = energyPerTick;
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.exhaustVentTrait = new ExhaustVentMachineTrait(this);
        exhaustVentTrait.setVentingDirection(Direction.UP);
        exhaustVentTrait.setVentingDamageAmount(isHighPressure() ? 12F : 6F);
    }

    @Override
    public SteamMinerLogic getRecipeLogic() {
        return (SteamMinerLogic) super.getRecipeLogic();
    }

    protected NotifiableItemStackHandler createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 0, IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, IO.OUT);
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        getRecipeLogic().onRemove();
        exportItems.dropInventoryInWorld();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
        getRecipeLogic().updateTickSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
            }
            exportItemSubs = exportItems.addChangedListener(this::updateAutoOutputSubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
    }

    //////////////////////////////////////
    // ********** LOGIC **********//
    //////////////////////////////////////
    protected void updateAutoOutputSubscription() {
        var outputFacingItems = getFrontFacing();
        if (!exportItems.isEmpty() &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getBlockPos(), outputFacingItems)) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            exportItems.exportToNearby(getFrontFacing());
        }
        updateAutoOutputSubscription();
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = GTGuis.createPanel(this, 176, 166);

        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176));
        int rowSize = 2;

        SlotGroup group = new SlotGroup("item_inv", rowSize, 0, true);
        panel.child(new Grid()
                        .coverChildren()
                        .top(10)
                        .alignX(0.75f)
                        .mapTo(rowSize, rowSize * rowSize, index -> new ItemSlot()
                                .slot(SyncHandlers.itemSlot(exportItems, index)
                                        .slotGroup(group)
                                        .changeListener((newItem, amount, client, init) -> {
                                            if (amount) {
                                                exportItems.onContentsChanged();
                                            }
                                        })
                                        .accessibility(false, true))))
                .child(new ListWidget<>()
                        .top(10)
                        .alignX(0.25f)
                        .coverChildren()
                        .childSeparator(Icon.EMPTY_2PX)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .alignX(Alignment.CenterLeft).children(getDisplayTextWidgets()))
                .child(SlotGroupWidget.playerInventory(true)
                        .left(7)
                        .bottom(7));

        return panel;
    }

    List<IWidget> getDisplayTextWidgets() {
        List<IWidget> widgets = new ArrayList<>();
        int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
        widgets.add(IKey.lang("gtceu.machine.miner.x", getRecipeLogic().getX(), getRecipeLogic().getMineX()).asWidget());
        widgets.add(IKey.lang("gtceu.machine.miner.y", getRecipeLogic().getY(), getRecipeLogic().getMineY()).asWidget());
        widgets.add(IKey.lang("gtceu.machine.miner.z", getRecipeLogic().getZ(), getRecipeLogic().getMineZ()).asWidget());
        widgets.add(IKey.lang("gtceu.universal.tooltip.working_area", workingArea, workingArea).asWidget());
        if (this.getRecipeLogic().isDone())
            widgets.add(IKey.lang(Component.translatable("gtceu.multiblock.large_miner.done").withStyle(ChatFormatting.GREEN)).asWidget());
        else if (this.getRecipeLogic().isWorking())
            widgets.add(IKey.lang(Component.translatable("gtceu.multiblock.large_miner.working").withStyle(ChatFormatting.GOLD)).asWidget());
        else if (!this.isWorkingEnabled())
            widgets.add(IKey.lang("gtceu.multiblock.work_paused").asWidget());
        if (getRecipeLogic().isInventoryFull())
            widgets.add(IKey.lang(Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .withStyle(ChatFormatting.RED)).asWidget());
        if (exhaustVentTrait.isVentingBlocked())
            widgets.add(IKey.lang(Component.translatable("gtceu.multiblock.large_miner.vent")
                    .withStyle(ChatFormatting.RED)).asWidget());
        else if (!drainInput(true))
            widgets.add(IKey.lang(Component.translatable("gtceu.multiblock.large_miner.steam")
                    .withStyle(ChatFormatting.RED)).asWidget());
        return widgets;
    }

    @Override
    public boolean drainInput(boolean simulate) {
        long resultSteam = steamTank.getFluidInTank(0).getAmount() - energyPerTick;
        if (!exhaustVentTrait.isVentingBlocked() && resultSteam >= 0L && resultSteam <= steamTank.getTankCapacity(0)) {
            if (!simulate)
                steamTank.drainInternal(energyPerTick, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            return Collections.singletonList(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        }
        return new ArrayList<>();
    }
}
