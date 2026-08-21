package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.AssemblyLineMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;

public class AssemblyLineRender extends DynamicRender<AssemblyLineMachine, AssemblyLineRender> {

    public static final MapCodec<AssemblyLineRender> CODEC = MapCodec.unit(AssemblyLineRender::new);
    public static final DynamicRenderType<AssemblyLineMachine, AssemblyLineRender> TYPE = new DynamicRenderType<>(
            AssemblyLineRender.CODEC);

    public AssemblyLineRender() {}

    @Override
    public DynamicRenderType<AssemblyLineMachine, AssemblyLineRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(AssemblyLineMachine machine, Vec3 cameraPos) {
        return (machine.recipeLogic.isWorking()) && super.shouldRender(machine, cameraPos);
    }

    @Override
    public void render(AssemblyLineMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        renderLines(machine, partialTick, poseStack, buffer.getBuffer(GTRenderTypes.assemblyLine()));
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLines(AssemblyLineMachine machine, float partialTick, PoseStack stack, VertexConsumer buffer) {
        if (machine.getRecipeLogic().getLastRecipe() == null) return;
        int asslineColor = Long.decode(ConfigHolder.INSTANCE.client.renderer.assemblyLineLaser).intValue();
        float progress = machine.getProgress() / (float) machine.getMaxProgress();
        int recipeInputs = Math.max(
                machine.getRecipeLogic().getLastRecipe().getInputContents(ItemRecipeCapability.CAP).size(),
                machine.getRecipeLogic().getLastRecipe().getInputContents(FluidRecipeCapability.CAP).size());
        progress *= recipeInputs;
        Direction down = RelativeDirection.DOWN.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        Direction back = RelativeDirection.BACK.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        Direction right = RelativeDirection.RIGHT.getRelativeFacing(machine.getFrontFacing(),
                machine.getUpwardsFacing(), machine.isFlipped());

        BlockPos.MutableBlockPos pos = BlockPos.ZERO.offset(down.getNormal()).mutable();
        for (int i = 0; i < (int) progress; i++) {
            renderLineInternal(buffer, stack, pos, down, asslineColor | 0xff000000);
            pos.move(back.getNormal().multiply(2));

            renderLineInternal(buffer, stack, pos, down, asslineColor | 0xff000000);
            pos.move(back.getOpposite().getNormal().multiply(2)).move(right.getNormal());
        }
        renderLineInternal(buffer, stack, pos, down,
                (asslineColor | (int) ((progress - (int) (progress)) * 255.f) << 24));

        pos.move(back.getNormal().multiply(2));
        renderLineInternal(buffer, stack, pos, down,
                (asslineColor | (int) ((progress - (int) (progress)) * 255.f) << 24));
    }

    public void renderLineInternal(VertexConsumer buffer, PoseStack stack, BlockPos pos, Direction down, int color) {
        var top = Vec3.atBottomCenterOf(pos.offset(down.getOpposite().getNormal()));
        var bottom = Vec3.atBottomCenterOf(pos);
        RenderBufferHelper.renderLine(buffer, stack, bottom, top, 0.03, color);
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyLineMachine machine) {
        return false;
    }

    @Override
    public AABB getRenderBoundingBox(AssemblyLineMachine machine) {
        Direction down = RelativeDirection.DOWN.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        Direction back = RelativeDirection.BACK.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        Direction right = RelativeDirection.RIGHT.getRelativeFacing(machine.getFrontFacing(),
                machine.getUpwardsFacing(), machine.isFlipped());
        AABB aabb = new AABB(machine.getBlockPos()).expandTowards(Vec3.atLowerCornerOf(down.getNormal().multiply(2)))
                .expandTowards(Vec3.atLowerCornerOf(down.getOpposite().getNormal()))
                .expandTowards(Vec3.atLowerCornerOf(back.getNormal().multiply(2)))
                .expandTowards(Vec3.atLowerCornerOf(right.getNormal().multiply(17)));
        return aabb;
    }
}
