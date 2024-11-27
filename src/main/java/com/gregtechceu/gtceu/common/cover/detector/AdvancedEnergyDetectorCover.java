package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedEnergyDetectorCover extends EnergyDetectorCover implements IUICover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedEnergyDetectorCover.class, DetectorCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    @Getter
    @Setter
    public long minValue, maxValue;
    @Persisted
    @Getter
    @Setter
    private int outputAmount;
    @Persisted
    @Getter
    private boolean usePercent;

    private LongInputWidget minValueInput;
    private LongInputWidget maxValueInput;

    public AdvancedEnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        final int DEFAULT_MIN_PERCENT = 33, DEFAULT_MAX_PERCENT = 66;

        this.minValue = DEFAULT_MIN_PERCENT;
        this.maxValue = DEFAULT_MAX_PERCENT;
        this.usePercent = true;
    }

    @Override
    protected void update() {
        if (coverHolder.getOffsetTimer() % 20 != 0) return;

        IEnergyInfoProvider energyInfoProvider = getEnergyInfoProvider();

        if (energyInfoProvider == null) {
            setRedstoneSignalOutput(outputAmount);
            return;
        }

        // TODO properly support values > MAX_LONG
        IEnergyInfoProvider.EnergyInfo energyInfo = energyInfoProvider.getEnergyInfo();
        long capacity = energyInfo.capacity().longValue();
        long stored = energyInfo.stored().longValue();

        if (usePercent) {
            if (capacity > 0) {
                float ratio = (float) stored / capacity;
                this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(ratio * 100, this.maxValue,
                        this.minValue, isInverted(), this.outputAmount);
            } else {
                this.outputAmount = isInverted() ? 0 : 15;
            }
        } else {
            this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(stored, this.maxValue, this.minValue,
                    isInverted(), this.outputAmount);
        }
        setRedstoneSignalOutput(outputAmount);
    }

    public void setUsePercent(boolean usePercent) {
        var wasPercent = this.usePercent;
        this.usePercent = usePercent;

        initializeMinMaxInputs(wasPercent);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 105);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_energy_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.max"))));

        minValueInput = new LongInputWidget(40, 50, 176 - 40 - 10, 20, this::getMinValue, this::setMinValue);
        maxValueInput = new LongInputWidget(40, 75, 176 - 40 - 10, 20, this::getMaxValue, this::setMaxValue);
        initializeMinMaxInputs(usePercent);
        group.addWidget(minValueInput);
        group.addWidget(maxValueInput);

        // Invert Redstone Output Toggle:
        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted) {

            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(List.copyOf(LangHandler.getMultiLang(
                        "cover.advanced_energy_detector.invert." + (isPressed ? "enabled" : "disabled"))));
            }
        });

        // Mode (EU / Percent) Toggle:
        group.addWidget(new ToggleButtonWidget(
                176 - 29, 20, 20, 20,
                GuiTextures.ENERGY_DETECTOR_COVER_MODE_BUTTON, this::isUsePercent, this::setUsePercent) {

            @Override
            public void updateScreen() {
                super.updateScreen();

                setHoverTooltips(List.copyOf(LangHandler.getMultiLang(
                        "cover.advanced_energy_detector.use_percent." + (isPressed ? "enabled" : "disabled"))));
            }
        });

        return group;
    }

    private void initializeMinMaxInputs(boolean wasPercent) {
        if (LDLib.isRemote() || minValueInput == null || maxValueInput == null)
            return;

        long energyCapacity = getEnergyInfoProvider().getEnergyInfo().capacity().longValue();

        minValueInput.setMin(0L);
        maxValueInput.setMin(0L);

        if (usePercent) {
            // This needs to be before setting the maximum, because otherwise the value would be limited to 100 EU
            // before converting to percent.
            if (!wasPercent) {
                minValueInput.setValue(Math.max((long) (((double) minValue / energyCapacity) * 100), 100));

                minValueInput.setValue(GTMath.clamp((long) (((double) minValue / energyCapacity) * 100), 0, 100));
                maxValueInput.setValue(GTMath.clamp((long) (((double) maxValue / energyCapacity) * 100), 0, 100));
            }

            minValueInput.setMax(100L);
            maxValueInput.setMax(100L);
        } else {
            minValueInput.setMax(energyCapacity);
            maxValueInput.setMax(energyCapacity);

            // This needs to be after setting the maximum, because otherwise the converted value would be
            // limited to 100.
            if (wasPercent) {
                minValueInput.setValue(GTMath.clamp((long) ((minValue / 100.0) * energyCapacity), 0, energyCapacity));
                maxValueInput.setValue(GTMath.clamp((long) ((maxValue / 100.0) * energyCapacity), 0, energyCapacity));
            }
        }
    }
}
