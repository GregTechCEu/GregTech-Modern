package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ConverterRenderer extends TieredHullMachineRenderer {
    private static final ResourceLocation ENERGY_IN = GTCEu.id("block/overlay/machine/overlay_energy_in"),
            ENERGY_OUT = GTCEu.id("block/overlay/machine/overlay_energy_out"),
            CONVERTER_NATIVE_IN = GTCEu.id("block/overlay/converter/converter_native_in"),
            CONVERTER_NATIVE_OUT = GTCEu.id("block/overlay/converter/converter_native_out");

    public ConverterRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var otherFaceTexture = ENERGY_IN;
        var frontFaceTexture = CONVERTER_NATIVE_OUT;
        if (machine instanceof ConverterMachine converter) {
            otherFaceTexture = converter.isFeToEu() ? CONVERTER_NATIVE_IN : otherFaceTexture;
            frontFaceTexture = converter.isFeToEu() ? ENERGY_OUT : frontFaceTexture;
        }
        if (side == frontFacing && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(frontFaceTexture), modelState));
        } else if (side != null && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(otherFaceTexture), modelState));
        }
    }

    @Override
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(CONVERTER_NATIVE_IN);
            register.accept(CONVERTER_NATIVE_OUT);
        }
    }
}
