package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class PrimitiveBlastFurnaceRenderer extends WorkableCasingMachineRenderer {

    public PrimitiveBlastFurnaceRenderer(ResourceLocation base, ResourceLocation overlay) {
        super(base, overlay);
    }

    @Override
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        super.render(blockEntity, partialTicks, stack, buffer, combinedLight, combinedOverlay);

        if (blockEntity instanceof MetaMachineBlockEntity mm) {
            if (mm.metaMachine instanceof PrimitiveBlastFurnaceMachine pbf) {
                if (pbf.isActive()) {
                    Direction opposite = pbf.getFrontFacing().getOpposite();
                    RenderType lavaRenderType = ItemBlockRenderTypes.getRenderLayer(Fluids.LAVA.defaultFluidState());
                    combinedLight = LightTexture.FULL_BRIGHT;

                    stack.pushPose();
                    Matrix4f pose = stack.last().pose();

                    pose.translate(opposite.getStepX(), opposite.getStepY(), opposite.getStepZ());

                    VertexConsumer vertexConsumer = buffer
                            .getBuffer(RenderTypeHelper.getEntityRenderType(lavaRenderType, true));

                    RenderUtil.renderFluidBlockFace(pose, vertexConsumer, Fluids.LAVA,
                            RenderUtil.FluidTextureType.STILL, Direction.UP, combinedOverlay, combinedLight);

                    stack.popPose();
                }
            }
        }
    }
}
