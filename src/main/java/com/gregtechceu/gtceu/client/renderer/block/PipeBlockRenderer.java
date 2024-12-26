package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.Minecraft;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public boolean useAO() {
        return true;
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
            return pipeModel.bakeQuads(side, PipeModel.ITEM_CONNECTIONS, 0);
        } else if (level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipeNode) {
            var quads = new LinkedList<>(
                    pipeModel.bakeQuads(side, pipeNode.getVisualConnections(), pipeNode.getBlockedConnections()));
            var modelState = ModelFactory.getRotation(pipeNode.getCoverContainer().getFrontFacing());
            var modelFacing = side == null ? null :
                    ModelFactory.modelFacing(side, pipeNode.getCoverContainer().getFrontFacing());
            ICoverableRenderer.super.renderCovers(quads, side, rand, pipeNode.getCoverContainer(), modelFacing, pos,
                    level, modelState);
            if (pipeNode.getFrameMaterial() != null) {
                ResourceLocation rl = MaterialIconType.frameGt
                        .getBlockTexturePath(pipeNode.getFrameMaterial().getMaterialIconSet(), true);
                BlockState blockState = GTMaterialBlocks.MATERIAL_BLOCKS
                        .get(TagPrefix.frameGt, pipeNode.getFrameMaterial())
                        .getDefaultState();
                var frameModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
                for (Direction face : Direction.values()) {
                    if ((pipeNode.getConnections() & 1 << (12 + face.get3DDataValue())) == 0) {
                        var frameTintedFaces = frameModel.getQuads(state, face, rand)
                                .stream()
                                .map(quad -> new BakedQuad(quad.getVertices(),
                                        quad.getTintIndex() + (quad.isTinted() ? 3 : 0),
                                        quad.getDirection(),
                                        quad.getSprite(),
                                        quad.isShade(),
                                        quad.hasAmbientOcclusion()))
                                .toList();
                        quads.addAll(frameTintedFaces);
                    }
                }
            }
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
