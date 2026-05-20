package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.trait.multiblock.MultiblockFluidRendererTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FluidAreaRender extends DynamicRender<WorkableMultiblockMachine, FluidAreaRender> {

    public static final List<RelativeDirection> DEFAULT_FACES = Collections.singletonList(RelativeDirection.UP);

    // spotless:off
    @SuppressWarnings("deprecation")
    public static final Codec<FluidAreaRender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidBlockRenderer.CODEC.forGetter(FluidAreaRender::getFluidBlockRenderer),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fixed_fluid").forGetter(FluidAreaRender::getFixedFluid),
            RelativeDirection.CODEC.listOf().optionalFieldOf("drawn_faces", DEFAULT_FACES).forGetter(FluidAreaRender::getDrawFaces)
    ).apply(instance, FluidAreaRender::new));
    public static final DynamicRenderType<WorkableMultiblockMachine, FluidAreaRender> TYPE = new DynamicRenderType<>(FluidAreaRender.CODEC);
    // spotless:on

    @Getter
    private final FluidBlockRenderer fluidBlockRenderer;
    private final boolean fixedFluid;
    @Getter
    private final List<RelativeDirection> drawFaces;

    private @Nullable Fluid cachedFluid;
    private @Nullable ResourceLocation cachedRecipe;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public FluidAreaRender(FluidBlockRenderer fluidBlockRenderer,
                           Optional<Fluid> fixedFluid, List<RelativeDirection> drawFaces) {
        this.fluidBlockRenderer = fluidBlockRenderer;
        if (fixedFluid.isPresent()) {
            this.fixedFluid = true;
            this.cachedFluid = fixedFluid.get();
        } else {
            this.fixedFluid = false;
        }
        this.drawFaces = drawFaces.isEmpty() ? DEFAULT_FACES : drawFaces;
    }

    @Override
    public DynamicRenderType<WorkableMultiblockMachine, FluidAreaRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public boolean shouldRender(WorkableMultiblockMachine machine, Vec3 cameraPos) {
        return machine.getTrait(MultiblockFluidRendererTrait.TYPE) != null;
    }

    @Override
    public void render(WorkableMultiblockMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;

        var trait = machine.getTrait(MultiblockFluidRendererTrait.TYPE);
        if (trait == null || !machine.isFormed() || trait.getFluidOffsets().isEmpty()) return;

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

        var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
        var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

        for (RelativeDirection face : this.drawFaces) {
            poseStack.pushPose();
            var pose = poseStack.last().pose();

            var dir = face.getRelative(machine.self().getFrontFacing(), machine.self().getUpwardsFacing(),
                    machine.self().isFlipped());
            if (dir.getAxis() != Direction.Axis.Y) dir = dir.getOpposite();

            fluidBlockRenderer.drawPlane(dir, trait.getFluidOffsets(), pose, consumer, cachedFluid,
                    RenderUtil.FluidTextureType.STILL, packedOverlay, machine.self().getBlockPos());
            poseStack.popPose();
        }
    }

    private Optional<Fluid> getFixedFluid() {
        if (fixedFluid) return Optional.ofNullable(cachedFluid);
        else return Optional.empty();
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableMultiblockMachine machine) {
        AABB box = super.getRenderBoundingBox(machine);
        var trait = machine.getTrait(MultiblockFluidRendererTrait.TYPE);
        if (trait == null) return box;
        var offsets = trait.getFluidOffsets();
        for (var offset : offsets) {
            box = box.minmax(new AABB(offset));
        }
        return box.inflate(getViewDistance());
    }
}
