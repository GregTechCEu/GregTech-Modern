package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private ICoilType coilType = CoilBlock.CoilType.CUPRONICKEL;
    @SyncToClient
    @Getter
    private int coilTier = 1;

    public CoilWorkableElectricMultiblockMachine(BlockEntityCreationInfo info, RecipeLogic recipeLogic) {
        super(info, recipeLogic);
    }

    public CoilWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        var cache = patternStates.get(substructureName).getCache();
        ICoilType coilType = null;
        for (var entry : cache.long2ObjectEntrySet()) {
            var state = entry.getValue().getBlockState();
            if (state.getBlock() instanceof CoilBlock coil) {
                if (GTCEuAPI.HEATING_COILS.containsKey(coil.coilType)) {
                    if (coilType == null) coilType = coil.coilType;
                    else {
                        if (coilType != coil.coilType) {
                            patternStates.get(substructureName).setError(
                                    new CoilMatchingError(BlockPos.of(entry.getLongKey()), coilType, coil.coilType));
                            invalidateStructure(substructureName);
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

    // todo jurre custom coil error reporting

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var Texlocation = (CoilBlock.CoilType.values()[coilTier].getTexture());

        IDrawable leftTop = new UITexture.Builder()
                .location(Texlocation)
                .subAreaUV(0f, 0f, 7f / 16f, 4f / 16f)
                .colorType(ColorType.DEFAULT)
                .build();

        IDrawable leftMiddle = new UITexture.Builder()
                .location(Texlocation)
                .imageSize(16, 16)
                .subAreaUV(0f, 4f / 16f, 7f / 16f, 12f / 16f)
                .colorType(ColorType.DEFAULT)
                .tiled(7, 8)
                .build();

        IDrawable leftBottom = new UITexture.Builder()
                .location(Texlocation)
                .subAreaUV(0f, 12f / 16f, 7f / 16f, 16f / 16f)
                .colorType(ColorType.DEFAULT)
                .build();

        IDrawable rightBottom = new UITexture.Builder()
                .location(Texlocation)
                .subAreaUV(7f / 16f, 4f / 16f, 0f, 0f / 16f)
                .colorType(ColorType.DEFAULT)
                .build();

        IDrawable rightMiddle = new UITexture.Builder()
                .location(Texlocation)
                .imageSize(16, 16)
                .subAreaUV(7f / 16f, 12f / 16f, 0f, 4f / 16f)
                .colorType(ColorType.DEFAULT)
                .tiled(7, 8)
                .build();

        IDrawable rightTop = new UITexture.Builder()
                .location(Texlocation)
                .subAreaUV(7f / 16f, 16f / 16f, 0f, 12f / 16f)
                .colorType(ColorType.DEFAULT)
                .build();

        mainWidget
                .child(Flow.row().height(MULTI_UI_TEXT_PANEL_HEIGHT).coverChildrenWidth()
                        .child(Flow.col().coverChildrenWidth()
                                .child(new IDrawable.DrawableWidget(leftTop).size(7, 4))
                                .child(new IDrawable.DrawableWidget(leftMiddle).size(7, MULTI_UI_TEXT_PANEL_HEIGHT - 8))
                                .child(new IDrawable.DrawableWidget(leftBottom).size(7, 4)))
                        .child(getMainTextPanel(syncManager))
                        .child(Flow.col().coverChildrenWidth()
                                .child(new IDrawable.DrawableWidget(rightTop).size(7, 4))
                                .child(new IDrawable.DrawableWidget(rightMiddle).size(7,
                                        MULTI_UI_TEXT_PANEL_HEIGHT - 8))
                                .child(new IDrawable.DrawableWidget(rightBottom).size(7, 4))));
    }
}
