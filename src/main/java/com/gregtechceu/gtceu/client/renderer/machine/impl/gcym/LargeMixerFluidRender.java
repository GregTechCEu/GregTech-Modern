package com.gregtechceu.gtceu.client.renderer.machine.impl.gcym;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym.LargeMixerMachine;

import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;

public class LargeMixerFluidRender extends DynamicRender<LargeMixerMachine, LargeMixerFluidRender> {

    // spotless:off
    public static final Codec<LargeMixerFluidRender> CODEC = Codec.unit(LargeMixerFluidRender::new);
    public static final DynamicRenderType<LargeMixerMachine, LargeMixerFluidRender> TYPE = new DynamicRenderType<>(LargeMixerFluidRender.CODEC);
    // spotless:on

    private final FluidBlockRenderer fluidBlockRenderer;
    private Fluid cachedFluid;
    private ResourceLocation cachedRecipe;

    public LargeMixerFluidRender() {
        fluidBlockRenderer = FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer();
    }

    @Override
    public DynamicRenderType<LargeMixerMachine, LargeMixerFluidRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public void render(LargeMixerMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {

        var lastRecipe = machine.recipeLogic.getLastRecipe();
        if (lastRecipe == null) {
            cachedRecipe = null;
            cachedFluid = null;
        } else if (machine.getOffsetTimer() % 20 == 0 || lastRecipe.id != cachedRecipe) {
            cachedRecipe = lastRecipe.id;
            if (machine.isActive()) {
                cachedFluid = RenderUtil.getRecipeFluidToRender(lastRecipe);
            } else {
                cachedFluid = null;
            }
        }

        if (cachedFluid == null) {
            return;
        }

        poseStack.pushPose();
        var pose = poseStack.last().pose();

        var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
        var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

        var up = RelativeDirection.UP.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        if (up != Direction.UP && up != Direction.DOWN) up = up.getOpposite();
        fluidBlockRenderer.drawPlane(up, machine.getFluidBlockOffsets(), pose, consumer, cachedFluid,
                RenderUtil.FluidTextureType.STILL, packedOverlay, machine.getPos());

        poseStack.popPose();
    }
}
