package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

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

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote TransformerRenderer
 */
public class TransformerRenderer extends TieredHullMachineRenderer {

    public final static ResourceLocation ENERGY_IN_1A = GTCEu.id("block/overlay/machine/overlay_energy_in_1a");
    public final static ResourceLocation ENERGY_IN_2A = GTCEu.id("block/overlay/machine/overlay_energy_in_2a");
    public final static ResourceLocation ENERGY_IN_4A = GTCEu.id("block/overlay/machine/overlay_energy_in_4a");
    public final static ResourceLocation ENERGY_IN_8A = GTCEu.id("block/overlay/machine/overlay_energy_in_8a");
    public final static ResourceLocation ENERGY_IN_16A = GTCEu.id("block/overlay/machine/overlay_energy_in_16a");
    public final static ResourceLocation ENERGY_IN_64A = GTCEu.id("block/overlay/machine/overlay_energy_in_64a");

    public final static ResourceLocation ENERGY_OUT_1A = GTCEu.id("block/overlay/machine/overlay_energy_out_1a");
    public final static ResourceLocation ENERGY_OUT_2A = GTCEu.id("block/overlay/machine/overlay_energy_out_2a");
    public final static ResourceLocation ENERGY_OUT_4A = GTCEu.id("block/overlay/machine/overlay_energy_out_4a");
    public final static ResourceLocation ENERGY_OUT_8A = GTCEu.id("block/overlay/machine/overlay_energy_out_8a");
    public final static ResourceLocation ENERGY_OUT_16A = GTCEu.id("block/overlay/machine/overlay_energy_out_16a");
    public final static ResourceLocation ENERGY_OUT_64A = GTCEu.id("block/overlay/machine/overlay_energy_out_64a");
    private final int baseAmp;

    public TransformerRenderer(int tier, int baseAmp) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.baseAmp = baseAmp;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var otherFaceTexture = ENERGY_OUT_4A;
        var frontFaceTexture = ENERGY_IN_1A;
        var isTransformUp = false;
        if (machine instanceof TransformerMachine transformer) {
            isTransformUp = transformer.isTransformUp();
        }

        switch (baseAmp) {
            case 1 -> { // 1A <-> 4A
                otherFaceTexture = isTransformUp ? ENERGY_IN_4A : otherFaceTexture;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_1A : frontFaceTexture;
            }
            case 2 -> { // 2A <-> 8A
                otherFaceTexture = isTransformUp ? ENERGY_IN_8A : ENERGY_OUT_8A;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_2A : ENERGY_IN_2A;
            }
            case 4 -> { // 4A <-> 16A
                otherFaceTexture = isTransformUp ? ENERGY_IN_16A : ENERGY_OUT_16A;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_4A : ENERGY_IN_4A;
            }
            default -> { // 16A <-> 64A or more
                otherFaceTexture = isTransformUp ? ENERGY_IN_64A : ENERGY_OUT_64A;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_16A : ENERGY_IN_16A;

            }
        }

        if (side == frontFacing && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(frontFaceTexture), modelState,
                    2));
        } else if (side != null && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(otherFaceTexture), modelState,
                    3));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(ENERGY_IN_1A);
            register.accept(ENERGY_IN_2A);
            register.accept(ENERGY_IN_4A);
            register.accept(ENERGY_IN_8A);
            register.accept(ENERGY_IN_16A);
            register.accept(ENERGY_IN_64A);

            register.accept(ENERGY_OUT_1A);
            register.accept(ENERGY_OUT_2A);
            register.accept(ENERGY_OUT_4A);
            register.accept(ENERGY_OUT_8A);
            register.accept(ENERGY_OUT_16A);
            register.accept(ENERGY_OUT_64A);
        }
    }
}
