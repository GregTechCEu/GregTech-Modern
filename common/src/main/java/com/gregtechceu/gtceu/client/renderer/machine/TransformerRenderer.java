package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtlib.client.bakedpipeline.FaceQuad;
import com.gregtechceu.gtlib.client.model.ModelFactory;
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
    public final static ResourceLocation ENERGY_IN_MULTI = GTCEu.id("block/overlay/machine/overlay_energy_in_multi");
    public final static ResourceLocation ENERGY_OUT_MULTI = GTCEu.id("block/overlay/machine/overlay_energy_out_multi");
    public final static ResourceLocation ENERGY_IN_ULTRA = GTCEu.id("block/overlay/machine/overlay_energy_in_ultra");
    public final static ResourceLocation ENERGY_OUT_ULTRA = GTCEu.id("block/overlay/machine/overlay_energy_out_ultra");

    public TransformerRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var otherFaceTexture = ENERGY_OUT;
        var frontFaceTexture = ENERGY_IN_MULTI;
        if (machine instanceof TransformerMachine transformer) {
            otherFaceTexture = transformer.isTransformUp() ? ENERGY_IN : otherFaceTexture;
            frontFaceTexture = transformer.isTransformUp() ? ENERGY_OUT_MULTI : frontFaceTexture;
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
            register.accept(ENERGY_IN_ULTRA);
            register.accept(ENERGY_OUT_ULTRA);
        }
    }

}
