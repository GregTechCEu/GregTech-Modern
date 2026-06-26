package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.steam.SteamWorkableMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.trait.ExhaustVentMachineTrait;
import com.gregtechceu.gtceu.common.machine.trait.miner.SteamMinerLogic;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.capability.IFluidHandler;

import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        super(info, isHighPressure, new SteamMinerLogic(fortune, speed, maximumRadius));

        this.inventorySize = 4;
        this.energyPerTick = energyPerTick;
        this.importItems = attachTrait(createImportItemHandler());
        this.exportItems = attachTrait(createExportItemHandler());
        this.exhaustVentTrait = attachTrait(new ExhaustVentMachineTrait());
        exhaustVentTrait.setVentingDirection(Direction.UP);
        exhaustVentTrait.setVentingDamageAmount(isHighPressure() ? 12F : 6F);
        getRecipeLogic().resetRecipeLogic();
    }

    @Override
    public SteamMinerLogic getRecipeLogic() {
        return (SteamMinerLogic) super.getRecipeLogic();
    }

    protected NotifiableItemStackHandler createImportItemHandler() {
        return new NotifiableItemStackHandler(0, IO.IN);
    }

    protected NotifiableItemStackHandler createExportItemHandler() {
        return new NotifiableItemStackHandler(inventorySize, IO.OUT);
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
        scheduleForNextServerTick(this::updateAutoOutputSubscription);
        if (!isRemote()) {
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
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.defaultSteamMachinePanelBuilder(this);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue startX = new IntSyncValue(() -> getRecipeLogic().getStartX()).allowC2S();
        IntSyncValue startY = new IntSyncValue(() -> getRecipeLogic().getStartY()).allowC2S();
        IntSyncValue startZ = new IntSyncValue(() -> getRecipeLogic().getStartZ()).allowC2S();
        IntSyncValue mineX = new IntSyncValue(() -> getRecipeLogic().getMineX()).allowC2S();
        IntSyncValue mineY = new IntSyncValue(() -> getRecipeLogic().getMineY()).allowC2S();
        IntSyncValue mineZ = new IntSyncValue(() -> getRecipeLogic().getMineZ()).allowC2S();
        IntSyncValue workingArea = new IntSyncValue(() -> IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius()))
                .allowC2S();

        ListWidget<?, ?> textList = new ListWidget<>()
                .heightRel(1.0f)
                .collapseDisabledChildren()
                .coverChildrenWidth()
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .childSeparator(IIcon.EMPTY_2PX)
                .child(Text
                        .dynamic(() -> Objects.requireNonNull(
                                getRecipeLogic().getCustomProgressLine().copy().withStyle(ChatFormatting.WHITE)))
                        .asWidget())
                .child(Text.dynamic(
                        () -> Component.translatable("gtceu.machine.miner.x", startX.getIntValue(), mineX.getIntValue())
                                .withStyle(ChatFormatting.WHITE))
                        .asWidget())
                .child(Text.dynamic(
                        () -> Component.translatable("gtceu.machine.miner.y", startY.getIntValue(), mineY.getIntValue())
                                .withStyle(ChatFormatting.WHITE))
                        .asWidget())
                .child(Text.dynamic(
                        () -> Component.translatable("gtceu.machine.miner.z", startZ.getIntValue(), mineZ.getIntValue())
                                .withStyle(ChatFormatting.WHITE))
                        .asWidget())
                .child(Text
                        .dynamic(() -> Component.translatable("gtceu.universal.tooltip.working_area",
                                workingArea.getIntValue(), workingArea.getIntValue()).withStyle(ChatFormatting.WHITE))
                        .asWidget())
                .child(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_miner.done")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))).asWidget()
                        .setEnabledIf(w -> getRecipeLogic().isDone()))
                .child(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_miner.working")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))).asWidget()
                        .setEnabledIf(w -> getRecipeLogic().isWorking()))
                .child(Text.dynamic(() -> Component.translatable("gtceu.multiblock.work_paused")).asWidget()
                        .setEnabledIf(w -> !isWorkingEnabled()))
                .child(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_miner.invfull")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))).asWidget()
                        .setEnabledIf(w -> getRecipeLogic().isInventoryFull()))
                .child(Text.dynamic(() -> Component.translatable("gtceu.multiblock.large_miner.needspower")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))).asWidget()
                        .setEnabledIf(w -> !drainInput(true)));

        mainWidget
                .name("content")
                .coverChildrenWidth(170)
                .child(Flow.row()
                        .name("mainRow")
                        .coverChildrenWidth(170)
                        .childPadding(4)
                        .child(new ParentWidget<>()
                                .name("displayScreen")
                                .heightRel(1.0f)
                                .coverChildrenWidth()
                                .background(GuiTextures.DISPLAY)
                                .child(textList.heightRel(1.0f).padding(3)))
                        .child(GTMuiMachineUtil
                                .createSquareSlotGroupFromInventory(exportItems, "export_inv", syncManager)
                                .verticalCenter())
                        .padding(4, 0));
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
