package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.TriState;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote PipeBlockRenderer
 */
public class PipeBlockRenderer implements IRenderer, ICoverableRenderer {

    @Getter
    PipeModel pipeModel;

    public PipeBlockRenderer(PipeModel pipeModel) {
        this.pipeModel = pipeModel;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack,
                           ItemDisplayContext transformType,
                           boolean leftHand, PoseStack matrixStack,
                           MultiBufferSource buffer, int combinedLight,
                           int combinedOverlay, BakedModel model) {
        pipeModel.renderItem(stack, transformType, leftHand, matrixStack, buffer, combinedLight, combinedOverlay,
                model);
    }

    @Override
    public TriState useAO() {
        return TriState.TRUE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                                       RandomSource rand) {
        if (level == null) {
            return pipeModel.bakeQuads(side, PipeModel.ITEM_CONNECTIONS);
        } else if (level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipeNode) {
            var quads = new LinkedList<>(pipeModel.bakeQuads(side, pipeNode.getVisualConnections()));
            var modelState = ModelFactory.getRotation(pipeNode.getCoverContainer().getFrontFacing());
            var modelFacing = side == null ? null :
                    ModelFactory.modelFacing(side, pipeNode.getCoverContainer().getFrontFacing());
            ICoverableRenderer.super.renderCovers(quads, side, rand, pipeNode.getCoverContainer(), modelFacing,
                    modelState);
            return quads;
        }
        return Collections.emptyList();
    }

    @NotNull
    @Override
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return pipeModel.getParticleTexture();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            pipeModel.registerTextureAtlas(register);
        }
    }
}
