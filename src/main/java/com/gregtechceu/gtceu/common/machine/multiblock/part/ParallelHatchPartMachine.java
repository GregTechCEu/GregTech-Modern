package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.DynamicDrawable;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.util.Mth;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ParallelHatchPartMachine extends TieredPartMachine implements IMuiMachine, IParallelHatch {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ParallelHatchPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    private static final int MIN_PARALLEL = 1;

    private final int maxParallel;

    @Persisted
    @Getter
    private int currentParallel = 1;

    public ParallelHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        this.maxParallel = (int) Math.pow(4, tier - GTValues.EV);
    }

    public void setCurrentParallel(int parallelAmount) {
        this.currentParallel = Mth.clamp(parallelAmount, MIN_PARALLEL, this.maxParallel);
        for (IMultiController controller : this.getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().markLastRecipeDirty();
            }
        }
    }

    /*
     * @Override
     * public Widget createUIWidget() {
     * WidgetGroup parallelAmountGroup = new WidgetGroup(0, 0, 100, 20);
     * parallelAmountGroup.addWidget(new IntInputWidget(this::getCurrentParallel, this::setCurrentParallel)
     * .setMin(MIN_PARALLEL)
     * .setMax(maxParallel));
     * 
     * return parallelAmountGroup;
     * }
     */

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    // TODO: This is called by .UI, will want this to implement IMuiMachine later
    // @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IntSyncValue parallels = new IntSyncValue(this::getCurrentParallel, this::setCurrentParallel);
        ModularPanel panel = new ModularPanel(this.getDefinition().getName());
        panel
                .size(180, 60)
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 240))
                .child(createParallelRow(parallels));
        return panel;
    }

    static Flow createParallelRow(IntSyncValue parallels) {
        return Flow.row()
                .align(Alignment.CENTER)
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            MouseData mouseData = MouseData.create(-1);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                return IKey.str("1/16x");
                            } else if (mouseData.ctrl()) {
                                return IKey.str("1/8x");
                            } else if (mouseData.shift()) {
                                return IKey.str("1/4x");
                            } else {
                                return IKey.str("1/2x");
                            }
                        }))
                        .width(32)
                        .height(16)
                        .onMousePressed((a, b, c) -> {
                            MouseData mouseData = MouseData.create(c);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                parallels.setValue(parallels.getValue() / 16);
                            } else if (mouseData.ctrl()) {
                                parallels.setValue(parallels.getValue() / 8);
                            } else if (mouseData.shift()) {
                                parallels.setValue(parallels.getValue() / 4);
                            } else {
                                parallels.setValue(parallels.getValue() / 2);
                            }
                            return true;
                        })
                        .marginLeft(4)
                        .verticalCenter())
                .child(
                        new TextFieldWidget()
                                .width(40)
                                .setTextAlignment(Alignment.CENTER)
                                .setNumbers(1, Integer.MAX_VALUE)
                                .value(parallels)
                                .setDefaultNumber(1)
                                .marginLeft(4)
                                .verticalCenter())
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            MouseData mouseData = MouseData.create(-1);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                return IKey.str("16x");
                            } else if (mouseData.ctrl()) {
                                return IKey.str("8x");
                            } else if (mouseData.shift()) {
                                return IKey.str("4x");
                            } else {
                                return IKey.str("2x");
                            }
                        }))
                        .width(32)
                        .height(16)
                        .onMousePressed((a, b, c) -> {
                            MouseData mouseData = MouseData.create(c);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                parallels.setValue(parallels.getValue() * 16);
                            } else if (mouseData.ctrl()) {
                                parallels.setValue(parallels.getValue() * 8);
                            } else if (mouseData.shift()) {
                                parallels.setValue(parallels.getValue() * 4);
                            } else {
                                parallels.setValue(parallels.getValue() * 2);
                            }
                            return true;
                        })
                        .marginLeft(4)
                        .verticalCenter())
                .child(IKey.str("Parallels")
                        .asWidget()
                        .marginLeft(4)
                        .verticalCenter());
    }
}
