package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IFluidRenderMulti;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidAreaRender extends DynamicRender<IFluidRenderMulti, FluidAreaRender> {

    // spotless:off
    @SuppressWarnings("deprecation")
    public static final Codec<FluidAreaRender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidBlockRenderer.CODEC.forGetter(FluidAreaRender::getFluidBlockRenderer),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fixed_fluid").forGetter(FluidAreaRender::getFixedFluid)
    ).apply(instance, FluidAreaRender::new));
    public static final DynamicRenderType<IFluidRenderMulti, FluidAreaRender> TYPE = new DynamicRenderType<>(FluidAreaRender.CODEC);
    // spotless:on

    @Getter
    private final FluidBlockRenderer fluidBlockRenderer;
    private final boolean fixedFluid;
    private @Nullable Fluid cachedFluid;
    private @Nullable ResourceLocation cachedRecipe;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public FluidAreaRender(FluidBlockRenderer fluidBlockRenderer, Optional<Fluid> fixedFluid) {
        this.fluidBlockRenderer = fluidBlockRenderer;
        if (fixedFluid.isPresent()) {
            this.fixedFluid = true;
            this.cachedFluid = fixedFluid.get();
        } else {
            this.fixedFluid = false;
        }
    }

    public static FluidAreaRender createLargeMachineRender() {
        return new FluidAreaRender(FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer(), Optional.empty());
    }

    public static FluidAreaRender createPBFLavaRender() {
        return new FluidAreaRender(FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer(), Optional.of(Fluids.LAVA.getSource()));
    }

    @Override
    public DynamicRenderType<IFluidRenderMulti, FluidAreaRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public void render(IFluidRenderMulti machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;
        if (!fixedFluid) {
            var lastRecipe = machine.getRecipeLogic().getLastRecipe();
            if (lastRecipe == null) {
                cachedRecipe = null;
                cachedFluid = null;
            } else if (machine.self().getOffsetTimer() % 20 == 0 || lastRecipe.id != cachedRecipe) {
                cachedRecipe = lastRecipe.id;
                if (machine.isActive()) {
                    cachedFluid = RenderUtil.getRecipeFluidToRender(lastRecipe);
                } else {
                    cachedFluid = null;
                }
            }
        }
        if (cachedFluid == null) {
            return;
        }

        poseStack.pushPose();
        var pose = poseStack.last().pose();

        var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
        var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

        var up = RelativeDirection.UP.getRelativeFacing(machine.self().getFrontFacing(),
                machine.self().getUpwardsFacing(), machine.self().isFlipped());
        if (up.getAxis() != Direction.Axis.Y) up = up.getOpposite();

        fluidBlockRenderer.drawPlane(up, machine.getFluidBlockOffsets(), pose, consumer, cachedFluid,
                RenderUtil.FluidTextureType.STILL, packedOverlay, machine.self().getPos());

        poseStack.popPose();
    }

    private Optional<Fluid> getFixedFluid() {
        if (fixedFluid) return Optional.ofNullable(cachedFluid);
        else return Optional.empty();
    }
}
