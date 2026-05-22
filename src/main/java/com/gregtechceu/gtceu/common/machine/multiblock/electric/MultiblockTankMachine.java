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
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockTankMachine extends MultiblockControllerMachine implements IMuiMachine {

    @SaveField
    @Getter
    private final NotifiableFluidTank tank;

    public MultiblockTankMachine(BlockEntityCreationInfo info, int capacity, @Nullable PropertyFluidFilter filter) {
        super(info);

        this.tank = attachTrait(new NotifiableFluidTank(1, capacity, IO.BOTH));
        if (filter != null) tank.setFilter(filter);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        var superResult = super.onUse(context);

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
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue bucketSyncer = new IntSyncValue(() -> tank.getFluidInTank(0).getAmount(), (ignored) -> {});
        syncManager.syncValue("bucket_amount", bucketSyncer);

        mainWidget
                .background(GTGuiTextures.DISPLAY)
                .child(Text.lang("gtceu.gui.fluid_amount").asWidget()
                        .color(0xffffff)
                        .margin(8, 0, 8, 0))
                .child(Text.dynamic(
                        () -> Component.literal(
                                FormattingUtil.formatBuckets(bucketSyncer.getIntValue()))
                                .withStyle(ChatFormatting.WHITE))
                        .asWidget()
                        .margin(8, 0, 20, 0))
                .child(Flow.column()
                        .margin(68, 0, 23, 0)
                        .coverChildren()
                        .child(createFluidSlot(syncManager)));
    }

    private IWidget createFluidSlot(PanelSyncManager syncManager) {
        syncManager.syncValue("fluid_slot",
                SyncHandlers.fluidSlot(tank.getStorages()[0]).controlsAmount(false));
        return new FluidSlot().syncHandler("fluid_slot", 0).background(GTGuiTextures.FLUID_SLOT);
    }
}
