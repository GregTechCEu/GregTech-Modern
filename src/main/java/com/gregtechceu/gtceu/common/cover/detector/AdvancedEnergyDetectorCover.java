package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.DynamicDrawable;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.LongSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;

import java.math.BigInteger;

import javax.annotation.ParametersAreNonnullByDefault;

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
    public long minValue, maxValue;

    @Persisted
    @Getter
    private boolean usePercent;

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

    public long getEnergyCapacity() {
        try {
            return getEnergyInfoProvider().getEnergyInfo().capacity().longValueExact();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    public void setMinValue(long value) {
        minValue = GTMath.clamp(value, 0, getEnergyCapacity());
    }

    public void setMaxValue(long value) {
        maxValue = GTMath.clamp(value, 0, getEnergyCapacity());
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
                                .childPadding(2)
                                .expanded()
                                .coverChildrenHeight())
                        .widthRel(1F)
                        .childPadding(3)
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
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            int value = getIncrementValue();
                            return IKey.str("-" + value).scale(1f - (String.valueOf(value).length() * 0.1f));
                        }))
                        .onMousePressed((x, y, button) -> {
                            voltageSyncer.setLongValue(voltageSyncer.getValue() - getIncrementValue());
                            return true;
                        })
                        .width(24))
                .child(new TextFieldWidget().value(voltageSyncer)
                        .tooltip(t -> t.add(Component.translatable("gtceu.creative.energy.voltage")))
                        .setNumbersLong(num -> {
                            if (usePercent) {
                                return GTMath.clamp(num, 0, 100);
                            } else return GTMath.clamp(num, 0, getEnergyCapacity());
                        })
                        .widthRelOffset(1f, -52)
                        .height(16))
                .child(new ButtonWidget<>()
                        .overlay(new DynamicDrawable(() -> {
                            int value = getIncrementValue();
                            return IKey.str("+" + value).scale(1f - (String.valueOf(value).length() * 0.1f));
                        }))
                        .onMousePressed((x, y, button) -> {
                            voltageSyncer.setLongValue(voltageSyncer.getValue() + getIncrementValue());
                            return true;
                        })
                        .width(24))
                .onUpdateListener(flow -> flow.scheduleResize())
                .widthRel(1F)
                .coverChildrenHeight();
    }

    private void updateEUValues(boolean wasPercent) {
        if (GTCEu.isClientThread()) return;

        long energyCapacity = getEnergyCapacity();

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
