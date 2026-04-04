package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockTankMachine extends MultiblockControllerMachine implements IMuiMachine {

    @SaveField
    @Getter
    @NotNull
    private final NotifiableFluidTank tank;

    public MultiblockTankMachine(BlockEntityCreationInfo info, int capacity, @Nullable PropertyFluidFilter filter) {
        super(info);

        this.tank = new NotifiableFluidTank(this, 1, capacity, IO.BOTH);
        if (filter != null) tank.setFilter(filter);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        var superResult = super.onUse(state, world, pos, player, hand, hit);

        if (superResult != InteractionResult.PASS) return superResult;
        if (!isFormed()) return InteractionResult.FAIL;

        return InteractionResult.PASS; // Otherwise let MetaMachineBlock.use() open the UI
    }

    @Override
    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (isFormed) {
            return super.getFluidHandlerCap(side, useCoverCapability);
        }
        return null;
    }

    /////////////////////////////////////
    // *********** GUI ***********//
    /////////////////////////////////////

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IntSyncValue bucketSyncer = new IntSyncValue(() -> tank.getFluidInTank(0).getAmount(), (ignored) -> {});
        syncManager.syncValue("bucket_amount", bucketSyncer);

        return new ModularPanel<>(this.getDefinition().getName())
                .child(
                        // Top half of the screen
                        new ParentWidget<>()
                                .widthRel(1)
                                .height(20 + 60)
                                .child(new ParentWidget<>()
                                        .background(GTGuiTextures.DISPLAY)
                                        .size(90, 63)
                                        .center()
                                        .child(IKey.lang("gtceu.gui.fluid_amount").asWidget()
                                                .color(0xffffff)
                                                .margin(8, 0, 8, 0))
                                        .child(IKey.dynamic(
                                                () -> Component.literal(
                                                        FormattingUtil.formatBuckets(bucketSyncer.getIntValue())))
                                                .asWidget()
                                                .color(0xffffff)
                                                .margin(8, 0, 20, 0))
                                        .child(Flow.column()
                                                .margin(68, 0, 23, 0)
                                                .coverChildren()
                                                .child(createFluidSlot(syncManager)))))
                .child(GTMuiWidgets.createTitleBar(getDefinition(), 176, GTGuiTextures.BACKGROUND))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    private IWidget createFluidSlot(PanelSyncManager syncManager) {
        syncManager.syncValue("fluid_slot",
                SyncHandlers.fluidSlot(tank.getStorages()[0]).controlsAmount(false));
        return new FluidSlot().syncHandler("fluid_slot", 0).background(GTGuiTextures.FLUID_SLOT);
    }
}
