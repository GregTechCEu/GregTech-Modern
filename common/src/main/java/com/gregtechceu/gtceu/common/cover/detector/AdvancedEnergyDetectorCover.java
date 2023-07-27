package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.utils.RedstoneUtil;
import com.gregtechceu.gtceu.utils.TextFormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedEnergyDetectorCover extends EnergyDetectorCover implements IUICover {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AdvancedEnergyDetectorCover.class, DetectorCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted @DescSynced @Getter @Setter
    public long minValue, maxValue;
    @Persisted @DescSynced @Getter @Setter
    private int outputAmount;
    @Persisted @DescSynced @Getter
    private boolean usePercent;

    private WidgetGroup widgetsToUpdate;
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

        IEnergyContainer energyContainer = getEnergyContainer();

        if (energyContainer != null) {
            if (usePercent) {
                if (energyContainer.getEnergyCapacity() > 0) {
                    float ratio = (float) energyContainer.getEnergyStored() / energyContainer.getEnergyCapacity();
                    this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(ratio * 100, this.maxValue, this.minValue, isInverted(), this.outputAmount);
                } else {
                    this.outputAmount = isInverted() ? 0 : 15;
                }
            } else {
                this.outputAmount = RedstoneUtil.computeLatchedRedstoneBetweenValues(energyContainer.getEnergyStored(),
                        this.maxValue, this.minValue, isInverted(), this.outputAmount);
            }
        }
        setRedstoneSignalOutput(outputAmount);
    }

    public void setUsePercent(boolean usePercent) {
        var wasPercent = this.usePercent;
        this.usePercent = usePercent;

        initializeMinMaxInputs(wasPercent);
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 105);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_energy_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.max"))));

        minValueInput = new LongInputWidget(40, 50, 176 - 40 - 10, 20, this.getMinValue(), this::setMinValue);
        maxValueInput = new LongInputWidget(40, 75, 176 - 40 - 10, 20, this.getMaxValue(), this::setMaxValue);
        initializeMinMaxInputs(usePercent);
        group.addWidget(minValueInput);
        group.addWidget(maxValueInput);


        // Invert Redstone Output Toggle:
        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted
        ) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(TextFormattingUtil.getTranslatableMultilineText(
                        "cover.advanced_energy_detector.invert." + (isPressed ? "enabled" : "disabled")
                ));
            }
        });

        // Mode (EU / Percent) Toggle:
        group.addWidget(new ToggleButtonWidget(
                176 - 29, 20, 20, 20,
                GuiTextures.ENERGY_DETECTOR_COVER_MODE_BUTTON, this::isUsePercent, this::setUsePercent
        ) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(TextFormattingUtil.getTranslatableMultilineText(
                        "cover.advanced_energy_detector.use_percent." + (isPressed ? "enabled" : "disabled")
                ));
            }
        });

        return group;
    }

    private void initializeMinMaxInputs(boolean wasPercent) {
        if (minValueInput == null || maxValueInput == null)
            return;

        long energyCapacity = getEnergyContainer().getEnergyCapacity();

        minValueInput.setMin(0);
        maxValueInput.setMin(0);

        if (usePercent) {
            // This needs to be before setting the maximum, because otherwise the value would be limited to 100 EU
            // before converting to percent.
            if (!wasPercent) {
                minValueInput.setValue(Mth.clamp((int) (((double) minValue / energyCapacity) * 100), 0, 100));
                maxValueInput.setValue(Mth.clamp((int) (((double) maxValue / energyCapacity) * 100), 0, 100));
            }

            minValueInput.setMax(100);
            maxValueInput.setMax(100);
        } else {
            minValueInput.setMax(energyCapacity);
            maxValueInput.setMax(energyCapacity);

            // This needs to be after setting the maximum, because otherwise the converted value would be
            // limited to 100.
            if (wasPercent) {
                minValueInput.setValue(Mth.clamp((int) ((minValue / 100.0) * energyCapacity), 0, energyCapacity));
                maxValueInput.setValue(Mth.clamp((int) ((maxValue / 100.0) * energyCapacity), 0, energyCapacity));
            }
        }
    }
}
