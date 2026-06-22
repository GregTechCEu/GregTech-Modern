package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;

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
import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinerMachine extends WorkableTieredMachine
                          implements IControllable, IMuiMachine, IDataInfoProvider, IMiner {

    @Getter
    @SaveField
    protected final CustomItemStackHandler chargerInventory;
    private final long energyPerTick;
    @Nullable
    protected TickableSubscription batterySubs;
    @Nullable
    protected ISubscription energySubs;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public MinerMachine(BlockEntityCreationInfo info, int tier, int speed, int maximumRadius, int fortune) {
        super(info, tier,
                new MinerLogic(fortune, speed, maximumRadius),
                0, (tier + 1) * (tier + 1), 0, 0, ($) -> 0);
        this.energyPerTick = GTValues.V[tier - 1];
        this.chargerInventory = createChargerItemHandler();
        this.autoOutput = attachTrait(AutoOutputTrait.ofItems(exportItems));
        autoOutput.setItemOutputDirectionValidator(d -> d != Direction.DOWN);
        getRecipeLogic().resetRecipeLogic();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected CustomItemStackHandler createChargerItemHandler() {
        var handler = new CustomItemStackHandler();
        handler.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
        return handler;
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        // Remove the miner pipes below this miner
        chargerInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    @Override
    public MinerLogic getRecipeLogic() {
        return (MinerLogic) super.getRecipeLogic();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            updateBatterySubscription();
            energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);
            chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
    }

    //////////////////////////////////////
    // ********** LOGIC **********//
    //////////////////////////////////////
    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true)) {
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        } else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false)) {
            updateBatterySubscription();
        }
    }

    @Override
    public boolean drainInput(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        if (isRemote()) return InteractionResult.SUCCESS;

        if (!this.isActive()) {
            int currentRadius = getRecipeLogic().getCurrentRadius();
            if (currentRadius == 1)
                getRecipeLogic().setCurrentRadius(getRecipeLogic().getMaximumRadius());
            else if (context.getPlayer().isShiftKeyDown())
                getRecipeLogic().setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                getRecipeLogic().setCurrentRadius(Math.max(1, currentRadius - 1));

            getRecipeLogic().resetArea(true);

            int workingArea = IMiner.getWorkingArea(getRecipeLogic().getCurrentRadius());
            context.getPlayer().sendSystemMessage(
                    Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea));
        } else {
            context.getPlayer().sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"));
        }
        return InteractionResult.SUCCESS;
    }

    @NotNull
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
}
