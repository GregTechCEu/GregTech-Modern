package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidType;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoolantInletPartMachine extends MultiblockPartMachine {

    public static final int TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;

    @Getter
    @SaveField
    private final NotifiableFluidTank tank;
    @Nullable
    private TickableSubscription autoIOSubs;
    @Nullable
    private ISubscription tankSubs;

    public CoolantInletPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.tank = new NotifiableFluidTank(this, 1, TANK_CAPACITY, IO.IN)
                .setFilter(fs -> CoolantRegistry.isCoolant(fs.getFluid()));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateTankSubscription));
        }
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

    private void updateTankSubscription() {
        if (GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), getFrontFacing())) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    private void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            tank.importFromNearby(getFrontFacing());
            updateTankSubscription();
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        int panelWidth = 176;
        int panelHeight = 80;

        var panel = GTGuis.createPanel(this, panelWidth, panelHeight);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), panelWidth));
        panel.child(Flow.column()
                .coverChildren().padding(8).top(4).alignX(0.5f).childPadding(4)
                .child(new FluidSlot().syncHandler(SyncHandlers.fluidSlot(tank.getStorages()[0]))));
        return panel;
    }
}
