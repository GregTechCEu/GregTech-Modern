package com.gregtechceu.gtceu.client.renderer.pipe.cover;

import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class CoverRendererPackage {

    public static final ModelProperty<CoverRendererPackage> PROPERTY = new ModelProperty<>();

    public static final CoverRendererPackage EMPTY = new CoverRendererPackage(false);

    private final EnumMap<Direction, CoverRenderer> renderers = new EnumMap<>(Direction.class);
    private final EnumSet<Direction> plates = EnumSet.allOf(Direction.class);

    private final boolean renderBackside;

    public CoverRendererPackage(boolean renderBackside) {
        this.renderBackside = renderBackside;
    }

    public void addRenderer(CoverRenderer renderer, @NotNull Direction facing) {
        renderers.put(facing, renderer);
        plates.remove(facing);
    }

    public void addQuads(List<BakedQuad> quads, RandomSource rand, ModelData modelData, ColorData data, RenderType renderType) {
        for (var renderer : renderers.entrySet()) {
            EnumSet<Direction> plates = EnumSet.copyOf(this.plates);
            // force front and back plates to render
            plates.add(renderer.getKey());
            plates.add(renderer.getKey().getOpposite());
            renderer.getValue().addQuads(quads, renderer.getKey(), rand, plates, renderBackside, modelData, data, renderType);
        }
    }

    public byte getMask() {
        byte mask = 0;
        for (Direction facing : renderers.keySet()) {
            mask |= 1 << facing.ordinal();
        }
        return mask;
    }
}
