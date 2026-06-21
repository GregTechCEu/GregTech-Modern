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
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
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

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
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
        super(info, isHighPressure, new SteamMinerLogic(fortune, speed, maximumRadius));

        this.inventorySize = 4;
        this.energyPerTick = energyPerTick;
        this.importItems = attachTrait(createImportItemHandler());
        this.exportItems = attachTrait(createExportItemHandler());
        this.exhaustVentTrait = attachTrait(new ExhaustVentMachineTrait());
        exhaustVentTrait.setVentingDirection(Direction.UP);
        exhaustVentTrait.setVentingDamageAmount(isHighPressure() ? 12F : 6F);
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
        mainWidget.width(200)
                .child(Flow.row()
                        .coverChildrenHeight()
                        .margin(5)
                        .childPadding(5)
                        .width(200)
                        .child(Flow.column()
                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                .padding(5)
                                .background(GTGuiTextures.DISPLAY)
                                .widthRel(.6f)
                                .coverChildrenHeight()
                                .child(new TextWidget<>(Text.dynamic(() -> {
                                    List<Component> text = new ArrayList<>();
                                    addDisplayText(text);
                                    return text.stream()
                                            .map(Component::copy)
                                            .reduce((a, b) -> a.append("\n").append(b))
                                            .orElse(Component.empty());
                                })).color(Color.WHITE.main)))
                        .child(GTMuiMachineUtil.createSquareSlotGroupFromInventory(exportItems, "export_inv",
                                syncManager).posRel(0.875f, 0.5f)));
    }

    private void addDisplayText(List<Component> textList) {
        int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
        textList.add(recipeLogic.getCustomProgressLine());
        textList.add(
                Component.translatable("gtceu.machine.miner.x", getRecipeLogic().getX(), getRecipeLogic().getMineX()));
        textList.add(
                Component.translatable("gtceu.machine.miner.y", getRecipeLogic().getY(), getRecipeLogic().getMineY()));
        textList.add(
                Component.translatable("gtceu.machine.miner.x", getRecipeLogic().getZ(), getRecipeLogic().getMineZ()));
        textList.add(Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        if (getRecipeLogic().isDone())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        else if (getRecipeLogic().isWorking())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        if (getRecipeLogic().isInventoryFull())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        if (exhaustVentTrait.isVentingBlocked())
            textList.add(Component.translatable("gtceu.multiblock.large_miner.vent")
                    .withStyle(ChatFormatting.RED));
        else if (!drainInput(true))
            textList.add(Component.translatable("gtceu.multiblock.large_miner.steam")
                    .withStyle(ChatFormatting.RED));
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
