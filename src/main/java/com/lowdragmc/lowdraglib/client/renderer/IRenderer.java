package com.lowdragmc.lowdraglib.client.renderer;

import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ModelResourceLocation;
import com.gregtechceu.gtceu.client.model.compat.TriState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface IRenderer {

    Set<IRenderer> EVENT_REGISTERS = new java.util.HashSet<>();
    IRenderer EMPTY = new IRenderer() {};

    default void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack,
                            MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {}

    default List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                                        RandomSource rand, ModelData data, RenderType renderType) {
        return List.of();
    }

    default void onPrepareTextureAtlas(Identifier atlas, Consumer<Identifier> register) {}

    default void onAdditionalModel(Consumer<ModelResourceLocation> registry) {}

    default void registerEvent() {
        EVENT_REGISTERS.add(this);
    }

    default boolean isRaw() {
        return false;
    }

    default boolean hasTESR(BlockEntity blockEntity) {
        return false;
    }

    default boolean isGlobalRenderer(BlockEntity blockEntity) {
        return false;
    }

    default int getViewDistance() {
        return 64;
    }

    default boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    default void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                        int packedLight, int packedOverlay) {}

    default TriState useAO() {
        return TriState.DEFAULT;
    }

    default TriState useAO(BlockState state, ModelData data, RenderType renderType) {
        return useAO();
    }

    default boolean useBlockLight(ItemStack stack) {
        return true;
    }

    default boolean reBakeCustomQuads() {
        return false;
    }

    default float reBakeCustomQuadsOffset() {
        return 0;
    }

    default boolean isGui3d() {
        return true;
    }
}
