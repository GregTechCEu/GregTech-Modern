package com.gregtechceu.gtceu.common.machines.steam;

import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machines.trait.RecipeLogic;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class SteamSolarBoiler extends SteamBoilerMachine {

    public SteamSolarBoiler(IMachineBlockEntity holder, boolean isHighPressure, Object... args) {
        super(holder, isHighPressure, args);
    }

    @Override
    public Direction getFrontFacing() {
        return Direction.UP;
    }

    @Override
    protected long getBaseSteamOutput() {
        return isHighPressure ? 360 : 120;
    }

    @Override
    protected void updateSteamSubscription() {
        if (temperatureSubs == null) {
            temperatureSubs = subscribeServerTick(temperatureSubs, this::updateCurrentTemperature);
        }
    }

    @Override
    protected void updateCurrentTemperature() {
        if (GTUtil.canSeeSunClearly(getLevel(), getPos())){
            recipeLogic.setStatus(RecipeLogic.Status.WORKING);
        }else {
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
    public ModularUI createUI(Player entityPlayer) {
        return super.createUI(entityPlayer)
                .widget(new ProgressWidget(() -> GTUtil.canSeeSunClearly(getLevel(), getPos()) ? 1.0 : 0.0, 114, 44, 20, 20)
                        .setProgressTexture(GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure).getSubTexture(0, 0, 1, 0.5),
                                GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure).getSubTexture(0, 0.5, 1, 0.5)));
    }

    @Override
    protected void randomDisplayTick(RandomSource random, float x, float y, float z) {
        
    }
}
