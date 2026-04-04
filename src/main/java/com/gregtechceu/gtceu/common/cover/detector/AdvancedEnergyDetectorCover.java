package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;

import java.math.BigInteger;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.utils.RedstoneUtil.computeLatchedRedstoneBetweenValues;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedEnergyDetectorCover extends EnergyDetectorCover implements IMuiCover {

    private static final int DEFAULT_MIN_PERCENT = 33;
    private static final int DEFAULT_MAX_PERCENT = 66;

    @SaveField
    @Getter
    public long minValue, maxValue;

    @SaveField
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
        this.minValue = GTMath.clamp(value, 0, maxValue - 1);
        if (this.minValue < 0) this.minValue = 0;
    }

    public void setMaxValue(long value) {
        if (usePercent) maxValue = GTMath.clamp(value, 0, 100);
        else maxValue = GTMath.clamp(value, 0, getEnergyCapacity());
        setMinValue(this.getMinValue());
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
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        syncManager.syncValue("usePercent", new BooleanSyncValue(this::isUsePercent, this::setUsePercent));
        var minValueSync = new LongSyncValue(this::getMinValue, this::setMinValue);
        var maxValueSync = new LongSyncValue(this::getMaxValue, this::setMaxValue);

        syncManager.syncValue("minValue", minValueSync);
        syncManager.syncValue("maxValue", maxValueSync);

        column.child(coverUIRow().child(IKey.lang("cover.advanced_energy_detector.min").asWidget().width(20))
                .child(GTMuiWidgets.createLongInputWithButtons(minValueSync, () -> 0, this::getMaxValue).width(142)))
                .child(coverUIRow().child(IKey.lang("cover.advanced_energy_detector.max").asWidget().width(20))
                        .child(GTMuiWidgets.createLongInputWithButtons(maxValueSync, () -> 0,
                                () -> usePercent ? 100 : getEnergyCapacity()).width(142)))
                .child(coverUIRow()
                        .child(new ToggleButton().value(new BooleanSyncValue(this::isInverted, this::setInverted))
                                .overlay(false, GTGuiTextures.OVERLAY_REDSTONE_OFF)
                                .overlay(true, GTGuiTextures.OVERLAY_REDSTONE_ON)
                                .tooltip(false, t -> t.add("cover.advanced_energy_detector.invert.disabled"))
                                .tooltip(true, t -> t.add("cover.advanced_energy_detector.invert.enabled")))
                        .child(new ToggleButton().value(new BooleanSyncValue(this::isUsePercent, this::setUsePercent))
                                .selectedBackground(ThemeAPI.INSTANCE.getTheme(settings.getTheme())
                                        .getToggleButtonTheme().theme().getBackground())
                                .overlay(false, GTGuiTextures.BUTTON_EU)
                                .overlay(true, GTGuiTextures.BUTTON_PERCENT)
                                .tooltip(false,
                                        t -> t.add("cover.advanced_energy_detector.use_percent.disabled"))
                                .tooltip(true,
                                        t -> t.add("cover.advanced_energy_detector.use_percent.enabled"))));
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

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putLong("min", minValue);
        tag.putLong("max", maxValue);
        tag.putBoolean("percent", usePercent);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setMinValue(tag.getLong("min"));
        setMaxValue(tag.getLong("max"));
        setUsePercent(tag.getBoolean("percent"));
        super.pasteConfig(player, tag);
    }
}
