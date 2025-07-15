package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface IMachineRendererModel<T extends IMachineFeature> {

    MachineDefinition getDefinition();

    @OnlyIn(Dist.CLIENT)
    default @NotNull List<BakedQuad> getRenderQuads(@Nullable T machine, @Nullable BlockAndTintGetter level,
                                                    @Nullable BlockPos pos, @Nullable BlockState blockState,
                                                    @Nullable Direction side, RandomSource rand,
                                                    @NotNull ModelData modelData, @Nullable RenderType renderType) {
        return Collections.emptyList();
    }

    @OnlyIn(Dist.CLIENT)
    void render(T machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay);

    @OnlyIn(Dist.CLIENT)
    default void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                              PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    default boolean shouldRenderOffScreen(T machine) {
        return false;
    }

    default boolean shouldRender(T machine, Vec3 cameraPos) {
        return Vec3.atCenterOf(machine.self().getPos()).closerThan(cameraPos, this.getViewDistance());
    }

    default int getViewDistance() {
        return 64;
    }

    default AABB getRenderBoundingBox(T machine) {
        BlockPos pos = machine.self().getPos();
        return new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
    }

    default boolean isBlockEntityRenderer() {
        return false;
    }
}
