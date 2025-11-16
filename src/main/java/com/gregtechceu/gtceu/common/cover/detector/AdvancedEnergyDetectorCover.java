package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.LongSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.mui.GTGuis.defaultPopupPanel;
import static com.gregtechceu.gtceu.utils.RedstoneUtil.computeLatchedRedstoneBetweenValues;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedEnergyDetectorCover extends EnergyDetectorCover implements IMuiCover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedEnergyDetectorCover.class, DetectorCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static final int DEFAULT_MIN_PERCENT = 33;
    private static final int DEFAULT_MAX_PERCENT = 66;

    @Persisted
    @Getter
    @Setter
    public long minValue, maxValue;

    @Persisted
    @Getter
    private boolean usePercent;

    private LongInputWidget minValueInput;
    private LongInputWidget maxValueInput;

    public AdvancedEnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.minValue = DEFAULT_MIN_PERCENT;
        this.maxValue = DEFAULT_MAX_PERCENT;
        this.usePercent = true;
    }

    @Override
    protected void update() {
        if (coverHolder.getOffsetTimer() % 20 != 0) return;

        IEnergyInfoProvider energyInfoProvider = getEnergyInfoProvider();
        if (energyInfoProvider == null) return;

        IEnergyInfoProvider.EnergyInfo energyInfo = energyInfoProvider.getEnergyInfo();
        boolean isBigInt = energyInfoProvider.supportsBigIntEnergyValues();

        if (isBigInt) {
            if (usePercent) {
                if (energyInfo.capacity().compareTo(BigInteger.ZERO) > 0) {
                    float ratio = GTMath.ratio(energyInfo.stored(), energyInfo.capacity());
                    setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(ratio * 100, maxValue,
                            minValue, isInverted(), redstoneSignalOutput));
                } else {
                    setRedstoneSignalOutput(isInverted() ? 15 : 0);
                }
            } else {
                setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(energyInfo.stored(),
                        BigInteger.valueOf(this.maxValue), BigInteger.valueOf(this.minValue),
                        isInverted(), redstoneSignalOutput));
            }
        } else {
            if (usePercent) {
                if (energyInfo.capacity().longValue() > 0) {
                    float ratio = energyInfo.stored().floatValue() / energyInfo.capacity().floatValue();
                    setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(ratio * 100, maxValue,
                            minValue, isInverted(), redstoneSignalOutput));
                } else {
                    setRedstoneSignalOutput(isInverted() ? 15 : 0);
                }
            } else {
                setRedstoneSignalOutput(computeLatchedRedstoneBetweenValues(energyInfo.stored().longValue(),
                        this.maxValue, this.minValue,
                        isInverted(), redstoneSignalOutput));
            }
        }
    }

    public void setUsePercent(boolean usePercent) {
        var wasPercent = this.usePercent;
        this.usePercent = usePercent;

        updateEUValues(wasPercent);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public ParentWidget<?> createCoverUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.syncValue("usePercent", new BooleanSyncValue(this::isUsePercent, this::setUsePercent));
        return new Column()
                .child(IMuiCover.createTitleRow(this.getAttachItem()))
                .child(new Row()
                        .child(new Column()
                                .child(IKey.lang("cover.advanced_energy_detector.min").asWidget().alignY(0.15F))
                                .child(IKey.lang("cover.advanced_energy_detector.max").asWidget().alignY(0.85F))
                                .heightRel(1F)
                                .coverChildrenWidth())
                        .child(new Column()
                                .child(createFieldRow(new LongSyncValue(this::getMinValue, this::setMinValue)))
                                .child(createFieldRow(new LongSyncValue(this::getMaxValue, this::setMaxValue)))
                                .childPadding(6)
                                .expanded()
                                .coverChildrenHeight())
                        .widthRel(1F)
                        .coverChildrenHeight())
                .child(new Row()
                        .child(new ToggleButton().value(new BooleanSyncValue(this::isInverted, this::setInverted))
                                .overlay(false, GTGuiTextures.OVERLAY_REDSTONE_OFF)
                                .overlay(true, GTGuiTextures.OVERLAY_REDSTONE_ON)
                                .tooltip(false, t -> {
                                    for (MutableComponent text : LangHandler
                                            .getMultiLang("cover.advanced_energy_detector.invert.disabled")) {
                                        t.addLine(text);
                                    }
                                })
                                .tooltip(true, t -> {
                                    for (MutableComponent text : LangHandler
                                            .getMultiLang("cover.advanced_energy_detector.invert.enabled")) {
                                        t.addLine(text);
                                    }
                                }))
                        .child(new ToggleButton().value(new BooleanSyncValue(this::isUsePercent, this::setUsePercent))
                                .selectedBackground(ThemeAPI.INSTANCE.getTheme(settings.getTheme())
                                        .getToggleButtonTheme().getTheme().getBackground())
                                .overlay(false, GTGuiTextures.BUTTON_EU)
                                .overlay(true, GTGuiTextures.BUTTON_PERCENT)
                                .tooltip(false, t -> {
                                    for (MutableComponent text : LangHandler
                                            .getMultiLang("cover.advanced_energy_detector.use_percent.disabled")) {
                                        t.addLine(text);
                                    }
                                })
                                .tooltip(true, t -> {
                                    for (MutableComponent text : LangHandler
                                            .getMultiLang("cover.advanced_energy_detector.use_percent.enabled")) {
                                        t.addLine(text);
                                    }
                                }))
                        .childPadding(5)
                        .coverChildren())
                .rightRel(0.5F)
                .margin(3)
                .childPadding(3)
                .coverChildren();
    }

    private Flow createFieldRow(LongSyncValue voltageSyncer) {
        return new Row()
                .child(new TextFieldWidget().value(voltageSyncer)
                        .tooltip(t -> t.add(Component.translatable("gtceu.creative.energy.voltage")))
                        .setNumbersLong(num -> {
                            if (usePercent) {
                                return GTMath.clamp(num, 0, 100);
                            } else return GTMath.clamp(num, 0, Long.MAX_VALUE);
                        })
                        .size(123, 16)
                        .margin(2, 0))
                .child(IKey.dynamic(() -> Component.literal(isUsePercent() ? "%" : "EU")).asWidget())
                .widthRel(1F)
                .coverChildrenHeight();
    }

    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 105);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_energy_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.max"))));

        minValueInput = new LongInputWidget(40, 50, 176 - 40 - 10, 20, this::getMinValue, this::setMinValue);
        maxValueInput = new LongInputWidget(40, 75, 176 - 40 - 10, 20, this::getMaxValue, this::setMaxValue);
        updateEUValues(usePercent);
        group.addWidget(minValueInput);
        group.addWidget(maxValueInput);

        // Invert Redstone Output Toggle:
        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.invert"));

        // Mode (EU / Percent) Toggle:
        group.addWidget(new ToggleButtonWidget(
                176 - 29, 20, 20, 20,
                GuiTextures.ENERGY_DETECTOR_COVER_MODE_BUTTON, this::isUsePercent, this::setUsePercent)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.use_percent"));

        return group;
    }

    private void updateEUValues(boolean wasPercent) {
        if (GTCEu.isClientThread()) return;

        long energyCapacity;
        try {
            energyCapacity = getEnergyInfoProvider().getEnergyInfo().capacity().longValueExact();
        } catch (ArithmeticException e) {
            energyCapacity = Long.MAX_VALUE;
        }

        if (usePercent && !wasPercent) {
            minValue = GTMath.clamp((long) (((double) minValue / energyCapacity) * 100), 0, 100);
            maxValue = GTMath.clamp((long) (((double) maxValue / energyCapacity) * 100), 0, 100);
        } else {
            if (wasPercent) {
                minValue = GTMath.clamp((long) Math.ceil((minValue / 100.0) * energyCapacity), 0, energyCapacity);
                maxValue = GTMath.clamp((long) Math.ceil((maxValue / 100.0) * energyCapacity), 0, energyCapacity);
            }
        }
    }
}
