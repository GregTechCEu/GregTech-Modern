package com.gregtechceu.gtceu.common.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import org.jetbrains.annotations.ApiStatus;

public class GTRegistration {

    /**
     * Addon devs: You must use your own registrate instance.
     */
    @ApiStatus.Internal
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(GTCEu.MOD_ID, false);

    static {
        GTRegistration.REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTRegistration() {/**/}
}
