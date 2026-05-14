package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
public class SteamTextureSet {

    private final UITexture main;
    private final @Nullable UITexture bronze;
    private final @Nullable UITexture steel;

    public SteamTextureSet(UITexture main, @Nullable UITexture bronze, @Nullable UITexture steel) {
        this.main = main;
        this.bronze = bronze;
        this.steel = steel;
    }

    public SteamTextureSet(UITexture main) {
        this.main = main;
        this.bronze = null;
        this.steel = null;
    }

    public UITexture get(@Nullable MetaMachine machine) {
        if (!(machine instanceof SteamMachine steamMachine)) return main;
        if (steamMachine.isHighPressure && steel != null) return steel;
        if (!steamMachine.isHighPressure && bronze != null) return bronze;
        return main;
    }
}
