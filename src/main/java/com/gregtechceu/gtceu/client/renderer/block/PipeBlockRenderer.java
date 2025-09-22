package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
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
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel.*;

public class PipeBlockRenderer implements IRenderer, ICoverableRenderer {

    @Getter
    PipeModel pipeModel;

    public PipeBlockRenderer(PipeModel pipeModel) {
        this.pipeModel = pipeModel;
        if (GTCEu.isClientSide()) {
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
        }
        if (!(level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipeNode)) {
            return pipeModel.bakeQuads(side, 0, 0);
        }
        RenderType renderType = CURRENT_RENDER_TYPE.get();
        ModelData modelData = CURRENT_MODEL_DATA.get().get(MODEL_DATA);
        if (modelData == null) modelData = ModelData.EMPTY;

        List<BakedQuad> quads = new LinkedList<>();

        if (renderType == null || renderType == RenderType.cutoutMipped()) {
            quads.addAll(pipeModel.bakeQuads(side, pipeNode.getVisualConnections(), pipeNode.getBlockedConnections()));
        }
        ICoverableRenderer.super.renderCovers(quads, pipeNode.getCoverContainer(), pos, level, side, rand,
                modelData, renderType);

        if (pipeNode.getFrameMaterial().isNull() || (renderType != null && renderType != RenderType.translucent())) {
            return quads;
        }

        BlockState frameState = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeNode.getFrameMaterial())
                .getDefaultState();
        BakedModel frameModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(frameState);

        modelData = frameModel.getModelData(level, pos, frameState, modelData);

        List<BakedQuad> frameQuads = new LinkedList<>();
        if (side == null || pipeNode.getCoverContainer().getCoverAtSide(side) == null) {
            frameQuads.addAll(frameModel.getQuads(state, side, rand, modelData, renderType));
        }
        if (side == null) {
            for (Direction face : GTUtil.DIRECTIONS) {
                if (pipeNode.getCoverContainer().getCoverAtSide(face) != null) {
                    continue;
                }
                frameQuads.addAll(frameModel.getQuads(state, face, rand, modelData, renderType));
            }
        }

        // bake all the quads' tint colors into the vertices
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        for (BakedQuad frameQuad : frameQuads) {
            if (frameQuad.isTinted()) {
                int color = blockColors.getColor(frameState, level, pos, frameQuad.getTintIndex());
                frameQuad = GTQuadTransformers.setColor(frameQuad, color, true);
            }
            quads.add(frameQuad);
        }
        return quads;
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
