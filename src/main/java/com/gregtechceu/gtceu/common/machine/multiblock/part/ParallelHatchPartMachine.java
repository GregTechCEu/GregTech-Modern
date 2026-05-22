package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;

public class ParallelHatchPartMachine extends TieredPartMachine implements IMuiMachine {

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
        for (MultiblockControllerMachine controller : this.getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().markLastRecipeDirty();
            }
        }
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue parallels = new IntSyncValue(this::getCurrentParallel, this::setCurrentParallel);
        mainWidget.child(Flow.row()
                .size(180, 60)
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            MouseData mouseData = MouseData.create(-1);
                            if (mouseData.ctrl() && mouseData.shift()) {
                                return Text.str("/16");
                            } else if (mouseData.ctrl()) {
                                return Text.str("/8");
                            } else if (mouseData.shift()) {
                                return Text.str("/4");
                            } else {
                                return Text.str("/2");
                            }
                        }))
                        .width(32)
                        .height(16)
                        .onMousePressed((GuiContext context, int button) -> {
                            MouseData mouseData = MouseData.create(button);
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
                                return Text.str("x16");
                            } else if (mouseData.ctrl()) {
                                return Text.str("x8");
                            } else if (mouseData.shift()) {
                                return Text.str("x4");
                            } else {
                                return Text.str("x2");
                            }
                        }))
                        .width(32)
                        .height(16)
                        .onMousePressed((GuiContext context, int button) -> {
                            MouseData mouseData = MouseData.create(button);
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
                .child(Text.lang("gtceu.machine.parallel_hatch.parallel_ui")
                        .asWidget()
                        .marginLeft(4)
                        .marginRight(4)
                        .verticalCenter()));
    }
}
