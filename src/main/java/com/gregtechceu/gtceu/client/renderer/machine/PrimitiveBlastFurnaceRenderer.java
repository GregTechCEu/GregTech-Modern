package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;

public class PrimitiveBlastFurnaceRenderer extends WorkableCasingMachineRenderer {

    private final FluidBlockRenderer fluidBlockRenderer;

    public PrimitiveBlastFurnaceRenderer(ResourceLocation base, ResourceLocation overlay) {
        super(base, overlay);
        fluidBlockRenderer = FluidBlockRenderer.Builder.create()
                .setFaceOffset(-.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer();
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

        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;
        if (blockEntity instanceof MetaMachineBlockEntity mm) {
            if (mm.metaMachine instanceof PrimitiveBlastFurnaceMachine pbf && pbf.isFormed()) {
                Direction opposite = pbf.getFrontFacing().getOpposite();
                RenderType lavaRenderType = ItemBlockRenderTypes.getRenderLayer(Fluids.LAVA.defaultFluidState());

                stack.pushPose();
                var pose = stack.last().pose();
                pose.translate(opposite.getStepX(), opposite.getStepY(), opposite.getStepZ());

                var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(lavaRenderType, true));
                var up = RelativeDirection.UP.getRelativeFacing(pbf.getFrontFacing(), pbf.getUpwardsFacing(),
                        pbf.isFlipped());
                if (up != Direction.UP && up != Direction.DOWN) up = up.getOpposite();

                fluidBlockRenderer.drawFace(up, pose, consumer, Fluids.LAVA.getSource(),
                        RenderUtil.FluidTextureType.STILL, combinedOverlay, combinedLight);

                if (pbf.getOffsetTimer() % 20 == 0) {
                    var xPos = opposite.getStepX() * 0.76F + pbf.getPos().getX() + 0.5F;
                    var yPos = opposite.getStepY() * 0.76F + pbf.getPos().getY() + 0.25F;
                    var zPos = opposite.getStepZ() * 0.76F + pbf.getPos().getZ() + 0.5F;

                    var relUp = RelativeDirection.UP.getRelativeFacing(pbf.getFrontFacing(), pbf.getUpwardsFacing(),
                            pbf.isFlipped());
                    var sign = relUp == Direction.UP || relUp == Direction.EAST || relUp == Direction.SOUTH ? 1 : -1;
                    var shouldX = relUp == Direction.EAST || relUp == Direction.WEST;
                    var shouldY = relUp == Direction.UP || relUp == Direction.DOWN;
                    var shouldZ = relUp == Direction.NORTH || relUp == Direction.SOUTH;
                    var speed = ((shouldY ? opposite.getStepY() : shouldX ? opposite.getStepX() : opposite.getStepZ()) *
                            0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat()) * sign * 2;
                    pbf.getLevel().addParticle(ParticleTypes.LAVA, xPos, yPos, zPos,
                            shouldX ? speed : 0,
                            shouldY ? speed : 0,
                            shouldZ ? speed : 0);
                }

                stack.popPose();
            }
        }
    }
}
