package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.lowdragmc.lowdraglib.client.scene.WorldSceneRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        super.render(blockEntity, partialTicks, stack, buffer, combinedLight, combinedOverlay);

        if(blockEntity instanceof MetaMachineBlockEntity mm) {
            if(mm.metaMachine instanceof PrimitiveBlastFurnaceMachine pbf) {
                if (pbf.isActive()) {
                    BlockPos pos = pbf.getPos().offset(pbf.getFrontFacing().getOpposite().getNormal());
                    WorldSceneRenderer.VertexConsumerWrapper vertexConsumer = new WorldSceneRenderer.VertexConsumerWrapper(buffer.getBuffer(RenderType.solid()));
                    vertexConsumer.addOffset((pos.getX() - (pos.getX() & 15)), (pos.getY() - (pos.getY() & 15)),
                            (pos.getZ() - (pos.getZ() & 15)));
                    Minecraft.getInstance().getBlockRenderer().renderLiquid(
                            pos,
                            pbf.getLevel(), vertexConsumer, Blocks.LAVA.defaultBlockState(),
                            Fluids.LAVA.defaultFluidState());
                }
            }
        }
    }
}
