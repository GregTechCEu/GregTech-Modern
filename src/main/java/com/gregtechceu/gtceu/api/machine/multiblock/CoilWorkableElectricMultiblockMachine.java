package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.multiblock.error.CoilMatchingError;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;

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
    public void formStructure(String name) {
        super.formStructure(name);
        var cache = patternStates.get(name).getCache();
        ICoilType coilType = null;
        for (var entry : cache.long2ObjectEntrySet()) {
            var state = entry.getValue().getBlockState();
            if (state.getBlock() instanceof CoilBlock coil) {
                if (GTCEuAPI.HEATING_COILS.containsKey(coil.coilType)) {
                    if (coilType == null) coilType = coil.coilType;
                    else {
                        if (coilType != coil.coilType) {
                            patternStates.get(name).setError(
                                    new CoilMatchingError(BlockPos.of(entry.getLongKey()), coilType, coil.coilType));
                            invalidateStructure(name);
                            return;
                        }
                    }
                }
            }
        }
        if (coilType != null) {
            this.coilType = coilType;
            this.coilTier = coilType.getTier();
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
