package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ConverterRenderer extends TieredHullMachineRenderer {
    private final ResourceLocation ENERGY_IN;
    private final ResourceLocation ENERGY_OUT;
    private static final ResourceLocation CONVERTER_FE_IN = GTCEu.id("block/overlay/converter/converter_native_in");
    private static final ResourceLocation CONVERTER_FE_OUT = GTCEu.id("block/overlay/converter/converter_native_out");

    public ConverterRenderer(int tier, int baseAmp) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        switch (baseAmp){
            case 4:
                ENERGY_IN = TransformerRenderer.ENERGY_IN_4A;
                ENERGY_OUT = TransformerRenderer.ENERGY_OUT_4A;
                break;
            case 8:
                ENERGY_IN = TransformerRenderer.ENERGY_IN_8A;
                ENERGY_OUT = TransformerRenderer.ENERGY_OUT_8A;
                break;
            case 16:
                ENERGY_IN = TransformerRenderer.ENERGY_IN_16A;
                ENERGY_OUT = TransformerRenderer.ENERGY_OUT_16A;
                break;
            default:
                ENERGY_IN = TransformerRenderer.ENERGY_IN_1A;
                ENERGY_OUT = TransformerRenderer.ENERGY_OUT_1A;
                break;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var otherFaceTexture = ENERGY_IN;
        var frontFaceTexture = CONVERTER_FE_OUT;
        var isFeToEu = false;
        if (machine instanceof ConverterMachine converter) {
            otherFaceTexture = converter.isFeToEu() ? CONVERTER_FE_IN : otherFaceTexture;
            frontFaceTexture = converter.isFeToEu() ? ENERGY_OUT : frontFaceTexture;
            isFeToEu = converter.isFeToEu();
        }
        if (side == frontFacing && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(frontFaceTexture), modelState, (isFeToEu?2:-1)));
        } else if (side != null && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(otherFaceTexture), modelState, (isFeToEu?-1:2)));
        }
    }

    @Override
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(CONVERTER_FE_IN);
            register.accept(CONVERTER_FE_OUT);
        }
    }
}
