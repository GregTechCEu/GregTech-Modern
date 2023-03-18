package com.lowdragmc.gtceu.api.registry.registrate.fabric;

import com.lowdragmc.gtceu.api.registry.registrate.GTRegistrate;
import com.lowdragmc.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTRegistrateImpl
 */
public class GTRegistrateImpl {
    public static IGTFluidBuilder fluid(GTRegistrate parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return parent.entry(name, callback -> new GTFluidBuilder<>(parent, parent, name, callback, stillTexture, flowingTexture, SimpleFlowableFluid.Flowing::new).defaultLang().defaultSource());
    }

    @Nonnull
    public static GTRegistrate create(String modId) {
        return new GTRegistrate(modId) {
            @Override
            public void registerRegistrate() {
                register();
            }
        };
    }

}
