package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.util.GTQuadTransformers;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TextureOverrideModel<T extends BakedModel> extends BakedModelWrapper<T> {

    private static final IQuadTransformer OVERLAY_OFFSET = GTQuadTransformers.offset(0.001f);

    @NotNull
    @Getter
    protected final Map<TextureAtlasSprite, TextureAtlasSprite> textureOverrides;

    public TextureOverrideModel(T child, Map<TextureAtlasSprite, TextureAtlasSprite> textureOverrides) {
        super(child);
        this.textureOverrides = textureOverrides;
    }

    public BakedModel getChild() {
        return this.originalModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData extraData,
                                             @Nullable RenderType renderType) {
        return retextureQuads(super.getQuads(state, side, rand, extraData, renderType), textureOverrides);
    }

    public static Map<TextureAtlasSprite, TextureAtlasSprite> resolveOverrides(Map<String, TextureAtlasSprite> overrides,
                                                                               Map<String, TextureAtlasSprite> toOverride) {
        Map<TextureAtlasSprite, TextureAtlasSprite> textures = new HashMap<>();
        for (var entry : overrides.entrySet()) {
            textures.put(toOverride.get(entry.getKey()), entry.getValue());
        }
        return textures;
    }

    public static List<BakedQuad> retextureQuads(List<BakedQuad> quads,
                                                 Map<TextureAtlasSprite, TextureAtlasSprite> overrides) {
        List<BakedQuad> newQuads = new LinkedList<>();
        for (BakedQuad quad : quads) {
            TextureAtlasSprite original = quad.getSprite();
            TextureAtlasSprite replacement = overrides.get(original);
            if (replacement != null) {
                newQuads.add(GTQuadTransformers.setSprite(quad, replacement));
            } else {
                newQuads.add(OVERLAY_OFFSET.process(quad));
            }
        }
        return newQuads;
    }
}
