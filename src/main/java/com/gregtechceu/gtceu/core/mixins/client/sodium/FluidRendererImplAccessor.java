package com.gregtechceu.gtceu.core.mixins.client.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.neoforge.render.FluidRendererImpl;
import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FluidRendererImpl.class, remap = false)
public interface FluidRendererImplAccessor {

    // coerced to the accessor because the class is private
    @Accessor("CURRENT_DEFAULT_CONTEXT")
    ThreadLocal<DefaultRenderContextAccessor> getCurrentDefaultContext();

    @Mixin(targets = "net.caffeinemc.mods.sodium.neoforge.render.FluidRendererImpl$DefaultRenderContext", remap = false)
    interface DefaultRenderContextAccessor {

        @Accessor("collector")
        TranslucentGeometryCollector gtceu$getCollector();
    }
}
