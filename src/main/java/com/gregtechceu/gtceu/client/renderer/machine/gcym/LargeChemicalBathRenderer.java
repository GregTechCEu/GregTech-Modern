package com.gregtechceu.gtceu.client.renderer.machine.gcym;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
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
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LargeChemicalBathRenderer extends WorkableCasingMachineRenderer {

    private final FluidBlockRenderer fluidBlockRenderer;

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
            if (mm.metaMachine instanceof LargeChemicalBathMachine lcb && lcb.isActive()) {
                GTRecipe last = lcb.recipeLogic.getLastRecipe();
                if (last == null) return;

                List<Content> contents = last.inputs.get(FluidRecipeCapability.CAP);
                if (contents == null || contents.isEmpty()) return;

                Optional<Content> fluidContent = contents.stream().filter(
                        content -> content.content instanceof FluidIngredient ingredient && !ingredient.isEmpty())
                        .findAny();
                if (fluidContent.isEmpty()) return;
                FluidIngredient ingredient = (FluidIngredient) fluidContent.get().content;

                FluidStack[] stacks = ingredient.getStacks();
                if (stacks.length == 0) return;
                Optional<FluidStack> first = Arrays.stream(stacks).filter(s -> !s.isEmpty()).findFirst();
                if (first.isEmpty()) return;

                stack.pushPose();
                var pose = stack.last().pose();

                var fluid = first.get().getFluid();
                var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(fluid.defaultFluidState());
                var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

                var up = RelativeDirection.UP.getRelativeFacing(lcb.getFrontFacing(), lcb.getUpwardsFacing(),
                        lcb.isFlipped());
                if (up != Direction.UP && up != Direction.DOWN) up = up.getOpposite();
                fluidBlockRenderer.drawPlane(up, lcb.getFluidBlockOffsets(), pose, consumer, fluid,
                        RenderUtil.FluidTextureType.STILL, combinedOverlay, lcb.getPos());

                stack.popPose();
            }
        }
    }
}
