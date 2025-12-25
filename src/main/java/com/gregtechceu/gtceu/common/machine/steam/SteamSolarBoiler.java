package com.gregtechceu.gtceu.common.machine.steam;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.ProgressWidget;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.NotNull;

public class SteamSolarBoiler extends SteamBoilerMachine {

    public SteamSolarBoiler(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder, isHighPressure, args);
    }

    @Override
    public @NotNull Direction getFrontFacing() {
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
            temperatureSubs = subscribeServerTick(temperatureSubs, this::updateCurrentTemperature);
        }
    }

    @Override
    protected void updateCurrentTemperature() {
        if (GTUtil.canSeeSunClearly(getLevel(), getPos())) {
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
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        UITexture progressTexture = isHighPressure() ? GTGuiTextures.PROGRESS_BAR_SOLAR_STEEL :
                GTGuiTextures.PROGRESS_BAR_SOLAR_BRONZE;

        return super.buildUI(data, syncManager, settings)
                .child(new ProgressWidget()
                        .top(30).right(18)
                        .size(18)
                        .texture(progressTexture, 20)
                        .progress(() -> GTUtil.canSeeSunClearly(getLevel(), getPos()) ? 1.0 : 0.0));
    }

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * return super.createUI(entityPlayer)
     * .widget(new ProgressWidget(() -> GTUtil.canSeeSunClearly(getLevel(), getPos()) ? 1.0 : 0.0, 114, 44, 20,
     * 20)
     * .setProgressTexture(
     * GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
     * GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure).getSubTexture(0, 0.5, 1,
     * 0.5)));
     * }
     */

    @Override
    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {}
}
