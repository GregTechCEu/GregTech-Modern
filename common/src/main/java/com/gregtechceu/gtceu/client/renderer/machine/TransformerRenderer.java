package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/10
 * @implNote TransformerRenderer
 */
public class TransformerRenderer extends TieredHullMachineRenderer{
    public final static ResourceLocation ENERGY_IN = GTCEu.id("block/overlay/machine/overlay_energy_in");
    public final static ResourceLocation ENERGY_OUT = GTCEu.id("block/overlay/machine/overlay_energy_out");
    public final static ResourceLocation ENERGY_IN_HI = GTCEu.id("block/overlay/machine/overlay_energy_in_hi");
    public final static ResourceLocation ENERGY_OUT_HI = GTCEu.id("block/overlay/machine/overlay_energy_out_hi");
    public final static ResourceLocation ENERGY_IN_MULTI = GTCEu.id("block/overlay/machine/overlay_energy_in_multi");
    public final static ResourceLocation ENERGY_OUT_MULTI = GTCEu.id("block/overlay/machine/overlay_energy_out_multi");
    public final static ResourceLocation ENERGY_IN_ULTRA = GTCEu.id("block/overlay/machine/overlay_energy_in_ultra");
    public final static ResourceLocation ENERGY_OUT_ULTRA = GTCEu.id("block/overlay/machine/overlay_energy_out_ultra");

    private final int baseAmp;

    public TransformerRenderer(int tier, int baseAmp) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
        this.baseAmp = baseAmp;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var otherFaceTexture = ENERGY_OUT;
        var frontFaceTexture = ENERGY_IN_MULTI;
        var isTransformUp = false;
        if (machine instanceof TransformerMachine transformer) {
            isTransformUp = transformer.isTransformUp();
        }

        switch(baseAmp) {
            case 1 -> { // 1A <-> 4A
                otherFaceTexture = isTransformUp ? ENERGY_IN : otherFaceTexture;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_MULTI : frontFaceTexture;
            }
            case 2 -> { // 2A <-> 8A
                otherFaceTexture = isTransformUp ? ENERGY_IN_MULTI : ENERGY_OUT_MULTI;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_HI : ENERGY_IN_HI;
            }
            // 4A <-> 16A
            default -> { // 16A <-> 64A or more
                otherFaceTexture = isTransformUp ? ENERGY_IN_HI : ENERGY_OUT_HI;
                frontFaceTexture = isTransformUp ? ENERGY_OUT_ULTRA : ENERGY_IN_ULTRA;

            }
        }

        if (side == frontFacing && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(frontFaceTexture), modelState, 2));
        } else if (side != null && modelFacing != null) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(otherFaceTexture), modelState, 3));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(ENERGY_IN);
            register.accept(ENERGY_OUT);
            register.accept(ENERGY_IN_MULTI);
            register.accept(ENERGY_OUT_MULTI);
            register.accept(ENERGY_IN_HI);
            register.accept(ENERGY_OUT_HI);
            register.accept(ENERGY_IN_ULTRA);
            register.accept(ENERGY_OUT_ULTRA);
        }
    }

}
