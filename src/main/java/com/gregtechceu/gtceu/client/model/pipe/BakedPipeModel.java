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
import net.minecraftforge.client.model.data.ModelData;

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

        if (level == null || pos == null || state == null) {
            connectionMask = ITEM_CONNECTIONS;
            blockedMask = Node.ALL_CLOSED;
        }
        if (connectionMask == null || connectionMask != Node.ALL_OPENED) {
            quads.addAll(parts.get(null).getQuads(state, side, rand, modelData, renderType));
            if (connectionMask == null) {
                // return unconnected base model if the model property isn't set
                return quads;
            }
        }
        for (Direction dir : GTUtil.DIRECTIONS) {
            if (PipeBlockEntity.isConnected(connectionMask, dir)) {
                quads.addAll(parts.get(dir).getQuads(state, side, rand, modelData, renderType));
                if (blockedMask != null && PipeBlockEntity.isFaceBlocked(blockedMask, dir)) {
                    quads.addAll(restrictors.get(dir).getQuads(state, side, rand, modelData, renderType));
                }
            }
        }
        if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipeNode)) {
            return quads;
        }
        ICoverableRenderer.super.renderCovers(quads, pipeNode.getCoverContainer(), pos, level, side, rand,
                modelData, renderType);

        if (pipeNode.getFrameMaterial().isNull() || (renderType != null && renderType != RenderType.translucent())) {
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
        ModelData.Builder builder = modelData.derive()
                .with(GTModelProperties.LEVEL, level)
                .with(GTModelProperties.POS, pos);

        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity<?, ?> blockEntity)) {
            return builder.build();
        }
        return builder.with(GTModelProperties.PIPE_CONNECTION_MASK, blockEntity.getVisualConnections())
                .with(GTModelProperties.PIPE_BLOCKED_MASK, blockEntity.getBlockedConnections())
                .build();
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
