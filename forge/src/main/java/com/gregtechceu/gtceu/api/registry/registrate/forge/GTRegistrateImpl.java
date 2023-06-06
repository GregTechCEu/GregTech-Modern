package com.gregtechceu.gtceu.api.registry.registrate.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.IGTFluidBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTRegistrateImpl
 */
public class GTRegistrateImpl {
    public static IGTFluidBuilder fluid(GTRegistrate parent, Material material, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return parent.entry(name, callback -> new GTFluidBuilder<>(parent, parent, material, name, callback, stillTexture, flowingTexture, GTFluidBuilder::defaultFluidType, ForgeFlowingFluid.Flowing::new).defaultLang().defaultSource().setData(ProviderType.LANG, NonNullBiConsumer.noop()));
    }

    @Nonnull
    public static GTRegistrate create(String modId) {
        return new GTRegistrate(modId) {
            @Override
            public void registerRegistrate() {
                registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
            }
        };
    }

}
