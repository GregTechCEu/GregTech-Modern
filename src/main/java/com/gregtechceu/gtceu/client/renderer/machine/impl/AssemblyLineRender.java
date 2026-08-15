package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.AssemblyLineMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import org.joml.Vector3f;

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
    public void render(AssemblyLineMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        GTRecipe recipe = machine.getRecipeLogic().getLastRecipe();
        if (recipe == null) return;

        VertexConsumer buffer = bufferSource.getBuffer(GTRenderTypes.assemblyLine());

        int color = ConfigHolder.INSTANCE.client.renderer.getAssemblyLineLaserColor();
        float progress = machine.getProgress() / (float) machine.getMaxProgress();
        int largestInputAmount = 0;
        for (RecipeCapability<?> cap : GTRegistries.RECIPE_CAPABILITIES) {
            largestInputAmount = Math.max(largestInputAmount, recipe.getInputContents(cap).size());
        }
        int lightAmount = (int) (progress * largestInputAmount);

        Direction down = RelativeDirection.DOWN
                .getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());
        Direction back = RelativeDirection.BACK
                .getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());
        Direction right = RelativeDirection.RIGHT
                .getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos().move(down);
        for (int i = 0; i < lightAmount; i++) {
            renderLine(buffer, poseStack, pos, down, color);
            pos.move(back, 2);

            renderLine(buffer, poseStack, pos, down, color);
            pos.move(back.getOpposite(), 2).move(right.getNormal());
        }

        color = FastColor.ARGB32.color(FastColor.as8BitChannel(progress), color);
        renderLine(buffer, poseStack, pos, down, color);

        pos.move(back, 2);
        renderLine(buffer, poseStack, pos, down, color);
    }

    private void renderLine(VertexConsumer buffer, PoseStack stack, BlockPos pos, Direction down, int color) {
        Vector3f top = Vec3.atBottomCenterOf(pos.relative(down.getOpposite())).toVector3f();
        Vector3f bottom = Vec3.atBottomCenterOf(pos).toVector3f();
        RenderBufferHelper.renderLine(buffer, stack, bottom, top, 0.03f, color);
    }

    @Override
    public boolean shouldRenderOffScreen(AssemblyLineMachine machine) {
        return false;
    }

    @Override
    public AABB getRenderBoundingBox(AssemblyLineMachine machine) {
        Direction down = RelativeDirection.DOWN
                .getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());
        Direction back = RelativeDirection.BACK
                .getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());
        Direction right = RelativeDirection.RIGHT
                .getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());

        return new AABB(machine.getBlockPos())
                .expandTowards(Vec3.atLowerCornerOf(down.getNormal().multiply(2)))
                .expandTowards(Vec3.atLowerCornerOf(down.getOpposite().getNormal()))
                .expandTowards(Vec3.atLowerCornerOf(back.getNormal().multiply(2)))
                .expandTowards(Vec3.atLowerCornerOf(right.getNormal().multiply(17)));
    }
}
