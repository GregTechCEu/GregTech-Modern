package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class RotorHolderMachineRenderer extends TieredHullMachineRenderer {

    public static final AABB ROTOR_TEXTURE_SHAPE = new AABB(-1, -1, -0.01, 2, 2, 1.01);
    public static final ResourceLocation ROTOR_HOLDER_OVERLAY = GTCEu.id("block/overlay/machine/overlay_rotor_holder");
    public static final ResourceLocation BASE_RING = GTCEu.id("block/multiblock/large_turbine/base_ring");
    public static final ResourceLocation BASE_BG = GTCEu.id("block/multiblock/large_turbine/base_bg");
    public static final ResourceLocation IDLE = GTCEu.id("block/multiblock/large_turbine/rotor_idle");
    public static final ResourceLocation SPINNING = GTCEu.id("block/multiblock/large_turbine/rotor_spinning");

    public RotorHolderMachineRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side != frontFacing || modelFacing == null) {
            return;
        }
        quads.add(
                StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(ROTOR_HOLDER_OVERLAY), modelState));
        if (machine instanceof IRotorHolderMachine rotorHolderMachine && rotorHolderMachine.isFormed()) {
            quads.add(StaticFaceBakery.bakeFace(ROTOR_TEXTURE_SHAPE, modelFacing,
                    ModelFactory.getBlockSprite(BASE_RING), modelState, -101, 0, true, false));
            quads.add(StaticFaceBakery.bakeFace(ROTOR_TEXTURE_SHAPE, modelFacing, ModelFactory.getBlockSprite(BASE_BG),
                    modelState, -101, 0, true, false));
            var material = rotorHolderMachine.getRotorMaterial();
            if (material.isNull()) {
                return;
            }
            quads.add(StaticFaceBakery.bakeFace(ROTOR_TEXTURE_SHAPE, modelFacing,
                    ModelFactory.getBlockSprite(rotorHolderMachine.isRotorSpinning() ? SPINNING : IDLE),
                    modelState, 2,
                    material.hasProperty(PropertyKey.ORE) && material.getProperty(PropertyKey.ORE).isEmissive() ? 12 :
                            5,
                    true, true));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(ROTOR_HOLDER_OVERLAY);
            register.accept(BASE_RING);
            register.accept(BASE_BG);
            register.accept(IDLE);
            register.accept(SPINNING);
        }
    }
}
