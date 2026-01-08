package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
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
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.util.Mth;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ParallelHatchPartMachine extends TieredPartMachine implements IMuiMachine, IParallelHatch {

    private static final int MIN_PARALLEL = 1;

    private final int maxParallel;

    @SaveField
    @Getter
    private int currentParallel = 1;

    public ParallelHatchPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
        this.maxParallel = (int) Math.pow(4, tier - GTValues.EV);
        this.currentParallel = maxParallel;
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
    public boolean canShared() {
        return false;
    }

    @Override
    public @NotNull ModularPanel buildUI(@NotNull PosGuiData data, @NotNull PanelSyncManager syncManager,
                                         @NotNull UISettings settings) {
        IntSyncValue parallels = new IntSyncValue(this::getCurrentParallel, this::setCurrentParallel);
        ModularPanel panel = new ModularPanel(this.getDefinition().getName());
        panel
                .size(180, 60)
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 240))
                .child(createParallelRow(parallels));
        return panel;
    }

    private Flow createParallelRow(IntSyncValue parallels) {
        return Flow.row()
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .align(Alignment.CENTER)
                .coverChildren()
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            MouseData mouseData = MouseData.create(-1);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                return IKey.str("/16");
                            } else if (mouseData.ctrl()) {
                                return IKey.str("/8");
                            } else if (mouseData.shift()) {
                                return IKey.str("/4");
                            } else {
                                return IKey.str("/2");
                            }
                        }))
                        .width(32)
                        .height(16)
                        .onMousePressed((a, b, c) -> {
                            MouseData mouseData = MouseData.create(c);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() / 16, 1, this.maxParallel));
                            } else if (mouseData.ctrl()) {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() / 8, 1, this.maxParallel));
                            } else if (mouseData.shift()) {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() / 4, 1, this.maxParallel));
                            } else {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() / 2, 1, this.maxParallel));
                            }
                            return true;
                        })
                        .marginLeft(4)
                        .verticalCenter())
                .child(
                        new TextFieldWidget()
                                .width(40)
                                .setTextAlignment(Alignment.CENTER)
                                .setNumbers(1, this.maxParallel)
                                .value(parallels)
                                .setDefaultNumber(1)
                                .marginLeft(4)
                                .verticalCenter())
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            MouseData mouseData = MouseData.create(-1);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                return IKey.str("x16");
                            } else if (mouseData.ctrl()) {
                                return IKey.str("x8");
                            } else if (mouseData.shift()) {
                                return IKey.str("x4");
                            } else {
                                return IKey.str("x2");
                            }
                        }))
                        .width(32)
                        .height(16)
                        .onMousePressed((a, b, c) -> {
                            MouseData mouseData = MouseData.create(c);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() * 16, 1, this.maxParallel));
                            } else if (mouseData.ctrl()) {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() * 8, 1, this.maxParallel));
                            } else if (mouseData.shift()) {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() * 4, 1, this.maxParallel));
                            } else {
                                parallels.setValue((int) GTMath.clamp(parallels.getValue() * 2, 1, this.maxParallel));
                            }
                            return true;
                        })
                        .marginLeft(4)
                        .verticalCenter())
                .child(IKey.lang("gtceu.machine.parallel_hatch.parallel_ui")
                        .asWidget()
                        .marginLeft(4)
                        .marginRight(4)
                        .verticalCenter());
    }
}
