package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.model.GTModelProperties;
import com.gregtechceu.gtceu.client.model.IBlockEntityRendererBakedModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BakedPipeModel extends BaseBakedModel implements ICoverableRenderer,
                            IBlockEntityRendererBakedModel<PipeBlockEntity<?, ?>> {

    public final static int ITEM_CONNECTIONS = 0b001100;

    private final Map<Direction, BakedModel> parts;
    private final Map<Direction, BakedModel> restrictors;

    public BakedPipeModel(Map<Direction, BakedModel> parts, Map<Direction, BakedModel> restrictors) {
        this.parts = parts;
        this.restrictors = restrictors;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                    RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();

        BlockAndTintGetter level = modelData.get(GTModelProperties.LEVEL);
        BlockPos pos = modelData.get(GTModelProperties.POS);
        Integer connectionMask = modelData.get(GTModelProperties.PIPE_CONNECTION_MASK);
        Integer blockedMask = modelData.get(GTModelProperties.PIPE_BLOCKED_MASK);

        if (state == null) {
            connectionMask = ITEM_CONNECTIONS;
            blockedMask = Node.ALL_CLOSED;
        }
        if (connectionMask == null || connectionMask != Node.ALL_OPENED) {
            BakedModel centerModel = parts.get(null);
            if (renderType == null ||
                    state != null && centerModel.getRenderTypes(state, rand, modelData).contains(renderType)) {
                quads.addAll(centerModel.getQuads(state, side, rand, modelData, renderType));
            }
            if (connectionMask == null) {
                // return unconnected base model if the model property isn't set
                return quads;
            }
        }
        for (Direction dir : GTUtil.DIRECTIONS) {
            if (PipeBlockEntity.isConnected(connectionMask, dir)) {
                BakedModel model = parts.get(dir);
                if (renderType == null ||
                        (state != null && model.getRenderTypes(state, rand, modelData).contains(renderType))) {
                    quads.addAll(model.getQuads(state, side, rand, modelData, renderType));
                }
                if (blockedMask != null && PipeBlockEntity.isFaceBlocked(blockedMask, dir)) {
                    model = restrictors.get(dir);
                    if (renderType == null ||
                            (state != null && model.getRenderTypes(state, rand, modelData).contains(renderType))) {
                        quads.addAll(model.getQuads(state, side, rand, modelData, renderType));
                    }
                }
            }
        }
        if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipeNode)) {
            return quads;
        }
        ICoverableRenderer.super.renderCovers(quads, pipeNode.getCoverContainer(), pos, level, side, rand,
                modelData, renderType);

        if (pipeNode.getFrameMaterial().isNull()) {
            return quads;
        }
        var frameBlockEntry = GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeNode.getFrameMaterial());
        if (frameBlockEntry == null) {
            return quads;
        }
        BlockState frameState = frameBlockEntry.getDefaultState();
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

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        for (BakedModel part : this.parts.values()) {
            modelData = part.getModelData(level, pos, state, modelData);
        }
        for (BakedModel restrictor : this.restrictors.values()) {
            modelData = restrictor.getModelData(level, pos, state, modelData);
        }
        return modelData;
    }

    @SuppressWarnings("deprecation")
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return parts.get(null).getParticleIcon();
    }

    @Override
    public @Nullable BlockEntityType<? extends PipeBlockEntity<?, ?>> getBlockEntityType() {
        return null;
    }

    @Override
    public void render(PipeBlockEntity<?, ?> blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}
}
