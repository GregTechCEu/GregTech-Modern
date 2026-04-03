package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamSolarBoiler extends SteamBoilerMachine {

    public SteamSolarBoiler(BlockEntityCreationInfo info, boolean isHighPressure) {
        super(info, isHighPressure);
    }

    @Override
    public Direction getFrontFacing() {
        return Direction.UP;
    }

    @Override
    protected long getBaseSteamOutput() {
        return isHighPressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput :
                ConfigHolder.INSTANCE.machines.smallBoilers.solarBoilerBaseOutput;
    }

    @Override
    protected void updateSteamSubscription() {
        if (temperatureSubs == null) {
            temperatureSubs = subscribeServerTick(null, this::updateCurrentTemperature);
        }
    }

    @Override
    protected void updateCurrentTemperature() {
        if (GTUtil.canSeeSunClearly(Objects.requireNonNull(getLevel()), getBlockPos())) {
            recipeLogic.setStatus(RecipeLogic.Status.WORKING);
        } else {
            recipeLogic.setStatus(RecipeLogic.Status.IDLE);
        }
        super.updateCurrentTemperature();
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 50 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 3;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        super.buildMainUI(mainWidget, guiData, syncManager, settings);

        UITexture progressTexture = isHighPressure() ? GTGuiTextures.PROGRESS_BAR_SOLAR_STEEL :
                GTGuiTextures.PROGRESS_BAR_SOLAR_BRONZE;

        DoubleSyncValue canSeeSun = syncManager.getOrCreateSyncHandler("canSeeSun", DoubleSyncValue.class,
                () -> new DoubleSyncValue(() -> {
                    if (GTUtil.canSeeSunClearly(getLevel(), getBlockPos())) return 1.0f;
                    return 0.0f;
                }));

        mainWidget.child(new ProgressWidget()
                .size(18)
                .texture(progressTexture, 20)
                .value(canSeeSun));
    }

    @Override
    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {}
}
