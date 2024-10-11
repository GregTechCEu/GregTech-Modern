package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrimitiveBlastFurnaceRenderer extends MachineRenderer {
    public PrimitiveBlastFurnaceRenderer(ResourceLocation modelLocation) {
        super(modelLocation);
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);

        if(machine instanceof PrimitiveBlastFurnaceMachine pbf) {
            if(pbf.isActive()) {
                var lavaModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.LAVA.defaultBlockState());

                quads.addAll(lavaModel.getQuads(Blocks.LAVA.defaultBlockState(), null, rand, ModelData.EMPTY, null));
            }
        }
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        super.render(blockEntity, partialTicks, stack, buffer, combinedLight, combinedOverlay);

        if(blockEntity instanceof MetaMachineBlockEntity mm) {
            if(mm.metaMachine instanceof PrimitiveBlastFurnaceMachine pbf) {
                if (pbf.isActive()) {
                    var lavaModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.LAVA.defaultBlockState());

                    quads.addAll(lavaModel.getQuads(Blocks.LAVA.defaultBlockState(), null, rand, ModelData.EMPTY, null));
                }
            }
        }
    }
}
