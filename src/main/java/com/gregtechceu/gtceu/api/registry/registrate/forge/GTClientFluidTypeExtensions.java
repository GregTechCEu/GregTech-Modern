package com.gregtechceu.gtceu.api.registry.registrate.forge;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;

import com.tterrag.registrate.AbstractRegistrate;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;

public class GTClientFluidTypeExtensions implements IClientFluidTypeExtensions {

    public static final Identifier FLUID_SCREEN_OVERLAY = GTCEu.id("textures/misc/fluid_screen_overlay.png");

    public GTClientFluidTypeExtensions(Identifier stillTexture, Identifier flowingTexture, int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    @Getter
    @Setter
    private Identifier flowingTexture, stillTexture;
    @Getter
    @Setter
    private int tintColor;

    public static void register(AbstractRegistrate<?> owner, Supplier<FluidType> type,
                                Supplier<? extends Fluid> source, Supplier<? extends Fluid> flowing,
                                Identifier stillTexture, Identifier flowingTexture, int color,
                                boolean forceTranslucent) {
        var extensions = new GTClientFluidTypeExtensions(stillTexture, flowingTexture, color);
        owner.getModEventBus().addListener(RegisterClientExtensionsEvent.class,
                event -> event.registerFluidType(extensions, type.get()));
        // Model loading runs again on resource reload; this listener must remain registered.
        owner.getModEventBus().addListener(RegisterFluidModelsEvent.class,
                event -> event.register(extensions.createModel(forceTranslucent), source, flowing));
    }

    public FluidModel.Unbaked createModel(boolean forceTranslucent) {
        boolean translucent = forceTranslucent || (tintColor >>> 24) != 255;
        return new FluidModel.Unbaked(new Material(stillTexture, translucent),
                new Material(flowingTexture, translucent), null, BlockTintSources.constant(tintColor));
    }

    @Override
    public Identifier getRenderOverlayTexture(Minecraft mc) {
        return FLUID_SCREEN_OVERLAY;
    }
}
