package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.*;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.data.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private ICoilType coilType = CoilBlock.CoilType.CUPRONICKEL;
    @DescSynced
    private int coilTier = coilType.getTier();

    public CoilWorkableElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
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
            coilTier = coil.getTier();
        }
    }

    public int getCoilTier() {
        return coilType.getTier();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {

        var panel = GTGuis.createPanel(this, 176 + 32, 164 + 36);

        UITexture coilTexture = new UITexture.Builder().location(CoilBlock.CoilType.values()[coilTier].getTexture())
                .imageSize(16, 16).colorType(ColorType.DEFAULT).tiled().build();

        //var coilWidget = coilTexture.asWidget().size(4, 16).heightRel(1.0f);

        var widget1 = coilTexture.asWidget().size(4, 16).heightRel(1.0f);
        var widget2 = coilTexture.asWidget().size(4, 16).heightRel(1.0f);

        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176 + 36))
                .child(new ParentWidget<>()
                        .widthRel(0.95f)
                        .heightRel(.45f)
                        .margin(4, 0)
                        .left(3).top(3)
                    .child(new Row()
                        .child(widget1.size(16, 16))
                        .child(new IDrawable.DrawableWidget(GTGuiTextures.MUI_DISPLAY).widthRel(.95f).heightRel(1.0f))
                        .child(widget2)
                    )
                    .child(IKey.dynamic(() ->
                            GTMultiblockTextUtil.addProgressLine(isFormed, isActive(), getRecipeLogic().getProgress(), getRecipeLogic().getMaxProgress(), getRecipeLogic().getProgressPercent()))
                            .color(0xffffff)
                            .asWidget().left(7).top(3))
                )
                .child(new Column()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(0, 8, 4, 4)
                        .childPadding(2)
                        .background(GTGuiTextures.BACKGROUND.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(this, syncManager))
                        .child(GTMuiWidgets.createVoidingButton(this, syncManager)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));

        return panel;
    }
}
