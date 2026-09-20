package brachy.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;

/** Sprite selection now belongs to baked fluid models, not client fluid-type extensions. */
public enum FluidTextureType {
    STILL, FLOWING, OVERLAY;

    public TextureAtlasSprite map(FluidStack stack) {
        return map(stack.getFluid().defaultFluidState());
    }

    public TextureAtlasSprite map(FluidState state) {
        var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        var material = switch (this) {
            case STILL -> model.stillMaterial();
            case FLOWING -> model.flowingMaterial();
            case OVERLAY -> model.overlayMaterial() == null ? model.stillMaterial() : model.overlayMaterial();
        };
        return material.sprite();
    }
}
