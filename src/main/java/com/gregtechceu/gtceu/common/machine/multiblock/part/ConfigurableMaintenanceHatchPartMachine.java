package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ConfigurableMaintenanceHatchPartMachine extends MaintenanceHatchPartMachine {

    // As integers, for use in the UI handler
    private static final int MAX_DURATION_MULTIPLIER = 110;
    private static final int MIN_DURATION_MULTIPLIER = 90;

    /**
     * Duration modifier for recipe.
     * It's configurable in the Configurable Maintenance Part.
     */
    @Getter
    @SaveField
    private float durationMultiplier = 1f;

    public ConfigurableMaintenanceHatchPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier);
    }

    public ConfigurableMaintenanceHatchPartMachine(BlockEntityCreationInfo info) {
        this(info, GTValues.HV);
    }

    public void setDurationMultiplier(float durationMultiplier) {
        this.durationMultiplier = durationMultiplier;
        syncDataHolder.markClientSyncFieldDirty("durationMultiplier");
    }

    /**
     * Higher {@link #getDurationMultiplier()} refers to a lower time multiplier.
     * The lower time multiplier means more likely causing problems.
     */
    public float getTimeMultiplier() {
        if (!isFormed()) return 1f;
        var controller = getControllers().getFirst();
        float result;
        if (durationMultiplier < 1.0 ^ controller.getDefinition().isGenerator())
            result = -20 * durationMultiplier + 21;
        else
            result = -8 * durationMultiplier + 9;
        return BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     *
     * @param duration in ticks to add to the counter of active time
     */
    @Override
    public void calculateMaintenance(int duration) {
        if (!ConfigHolder.INSTANCE.machines.enableMaintenance || isFullAuto()) {
            return;
        }

        setTimeActive(getTimeActive() + duration);
        float rate = ConfigHolder.INSTANCE.machines.maintenanceCheckRate / getTimeMultiplier();
        if (getTimeActive() >= rate) {
            setTimeActive(0);
            if (GTValues.RNG.nextInt(6000) == 0) {
                causeRandomMaintenanceProblems();
                setTaped(false);
            }
        }
    }

    @Override
    public @Nullable GTRecipe modifyRecipe(GTRecipe recipe) {
        recipe = super.modifyRecipe(recipe);
        if (recipe == null) return null;

        var durationMultiplier = getDurationMultiplier();
        if (durationMultiplier != 1) {
            recipe = recipe.copy();
            recipe.duration = Math.max(1, Math.round(recipe.duration * durationMultiplier));
        }
        return recipe;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var maintenanceProblems = new ByteSyncValue(this::getMaintenanceProblems);
        syncManager.syncValue("maintenanceProblems", maintenanceProblems);

        InteractionSyncHandler buttonSyncHandler = new InteractionSyncHandler();
        buttonSyncHandler.setOnMousePressed((button) -> {
            fixMaintenanceProblems(guiData.getPlayer());
        });

        IntSyncValue durationSyncValue = new IntSyncValue((() -> (int) (getDurationMultiplier() * 100)),
                (duration) -> setDurationMultiplier(duration / 100f)).allowC2S();
        syncManager.syncValue("durationMultiplier", durationSyncValue);
        FloatSyncValue timeMultiplierSyncValue = new FloatSyncValue(this::getTimeMultiplier);
        syncManager.syncValue("timeMultiplier", timeMultiplierSyncValue);

        RichTooltip speedTooltip = new RichTooltip().autoUpdate(true).tooltipBuilder(builder -> {
            builder.addLine(
                    Text.dynamic(() -> durationSyncValue.getIntValue() == 100 ?
                            Component.translatable(
                                    "gtceu.maintenance.configurable_duration.unchanged_description") :
                            Component.translatable(
                                    "gtceu.maintenance.configurable_duration.changed_description",
                                    FormattingUtil.formatNumbers(durationSyncValue.getIntValue() / 100f))));
        });
        RichTooltip frequencyTooltip = new RichTooltip().autoUpdate(true).tooltipBuilder(builder -> {
            builder.addLine(
                    Text.dynamic(() -> durationSyncValue.getIntValue() == 100 ?
                            Component.translatable(
                                    "gtceu.maintenance.configurable_time.unchanged_description") :
                            Component.translatable(
                                    "gtceu.maintenance.configurable_time.changed_description",
                                    FormattingUtil.formatNumbers(timeMultiplierSyncValue.getFloatValue()))));
        });

        mainWidget.child(Flow.column()
                .size(MachineUIPanel.DEFAULT_CONTENT_WIDTH, 103)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .child(Flow.column()
                        .coverChildren()
                        .padding(5)
                        .paddingLeft(0)
                        .marginLeft(5)
                        .paddingBottom(1)
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(5)
                                .leftRel(0)
                                .child(new TextWidget<>(
                                        Text.lang("gtceu.maintenance.configurable_duration.modify")))
                                .tooltip(speedTooltip)
                                .child(
                                        GTMuiWidgets
                                                .createDecimalInputWithButtons(durationSyncValue,
                                                        () -> MIN_DURATION_MULTIPLIER,
                                                        () -> MAX_DURATION_MULTIPLIER, 1, 2)))

                        .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(
                                "gtceu.maintenance.configurable_frequency",
                                timeMultiplierSyncValue.getValue())))
                                .leftRel(0)
                                .paddingTop(5))
                        .tooltip(frequencyTooltip))
                .child(manualMaintenancePanel(buttonSyncHandler))
                .child(new DynamicWidget<>().syncHandler(maintenanceProblemsDisplay(maintenanceProblems))));
    }
}
