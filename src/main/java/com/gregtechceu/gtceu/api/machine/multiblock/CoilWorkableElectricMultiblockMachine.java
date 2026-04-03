package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.*;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private ICoilType coilType = CoilBlock.CoilType.CUPRONICKEL;
    @SyncToClient
    @Getter
    private int coilTier = 1;

    public CoilWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    //////////////////////////////////////
    // *** Multiblock LifeCycle ***//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var type = getMultiblockState().getMatchContext().get("CoilType");
        if (type instanceof ICoilType coil) {
            this.coilType = coil;
            this.coilTier = coil.getTier();
            getSyncDataHolder().markClientSyncFieldDirty("coilTier");
        }
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IDrawable coilTexture = new UITexture.Builder()
                .location(CoilBlock.CoilType.values()[coilTier].getTexture())
                .imageSize(16, 16)
                .colorType(ColorType.DEFAULT)
                .tiled().build();

        mainWidget
                .child(Flow.row().height(MULTI_UI_TEXT_PANEL_HEIGHT).coverChildrenWidth()
                        .child(new IDrawable.DrawableWidget(coilTexture).size(4, MULTI_UI_TEXT_PANEL_HEIGHT))
                        .child(getMainTextPanel(syncManager))
                        .child(new IDrawable.DrawableWidget(coilTexture).size(4, MULTI_UI_TEXT_PANEL_HEIGHT)));
    }
}
