package com.gregtechceu.gtceu.client.renderer.machine.gcym;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym.LargeChemicalBathMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;

public class LargeChemicalBathRenderer extends WorkableCasingMachineRenderer {

    private final FluidBlockRenderer fluidBlockRenderer;
    private Fluid cachedFluid;
    private ResourceLocation cachedRecipe;

    public LargeChemicalBathRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        super(baseCasing, workableModel);

        fluidBlockRenderer = FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer();
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        super.render(blockEntity, partialTicks, stack, buffer, combinedLight, combinedOverlay);

        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;
        if (blockEntity instanceof MetaMachineBlockEntity mm) {
            if (mm.metaMachine instanceof LargeChemicalBathMachine lcb) {
                var lastRecipe = lcb.recipeLogic.getLastRecipe();
                if (lastRecipe == null) {
                    cachedRecipe = null;
                    cachedFluid = null;
                } else if (lcb.getOffsetTimer() % 20 == 0 || lastRecipe.id != cachedRecipe) {
                    cachedRecipe = lastRecipe.id;
                    if (lcb.isActive()) {
                        cachedFluid = RenderUtil.getRecipeFluidToRender(lastRecipe);
                    } else {
                        cachedFluid = null;
                    }
                }

                if (cachedFluid == null) {
                    return;
                }

                stack.pushPose();
                var pose = stack.last().pose();

                var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
                var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

                var up = RelativeDirection.UP.getRelativeFacing(lcb.getFrontFacing(), lcb.getUpwardsFacing(),
                        lcb.isFlipped());
                if (up != Direction.UP && up != Direction.DOWN) up = up.getOpposite();
                fluidBlockRenderer.drawPlane(up, lcb.getFluidBlockOffsets(), pose, consumer, cachedFluid,
                        RenderUtil.FluidTextureType.STILL, combinedOverlay, lcb.getPos());

                stack.popPose();
            }
        }
    }
}
