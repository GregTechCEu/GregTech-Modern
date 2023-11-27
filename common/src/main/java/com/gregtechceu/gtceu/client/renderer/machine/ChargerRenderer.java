package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
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

import java.util.*;
import java.util.function.Consumer;

/**
 * @author lucifer_ll
 * @date 2023/7/13
 * @implNote ChargerRenderer
 */
public class ChargerRenderer extends TieredHullMachineRenderer{
    public final static ResourceLocation CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public final static ResourceLocation CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public final static ResourceLocation CHARGER_RUNNING_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_running_emissive");
    public final static ResourceLocation CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public final static ResourceLocation CHARGER_FINISHED_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_finished_emissive");

    public ChargerRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var state = ChargerMachine.State.IDLE;
        if (machine instanceof ChargerMachine charger) {
            state = charger.getState();
        }

        if (side == frontFacing && modelFacing != null) {
            var bakedFaces = new ArrayList<BakedQuad>();
            switch (state) {
                case IDLE -> bakedFaces.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_IDLE), modelState, -1, 0, false, true));
                case RUNNING -> {
                    bakedFaces.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_RUNNING), modelState, -1, 0, true, true));
                    if (ConfigHolder.INSTANCE.client.machinesEmissiveTextures) {
                        bakedFaces.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_RUNNING_EMISSIVE), modelState, -101, 15, true, false));
                    }
                }
                case FINISHED -> {
                    bakedFaces.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_FINISHED), modelState, -1, 0, true, true));
                    if (ConfigHolder.INSTANCE.client.machinesEmissiveTextures) {
                        bakedFaces.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_FINISHED_EMISSIVE), modelState, -101, 15, true, false));
                    }
                }
            }
            quads.addAll(bakedFaces);

        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(CHARGER_IDLE);
            register.accept(CHARGER_RUNNING);
            register.accept(CHARGER_RUNNING_EMISSIVE);
            register.accept(CHARGER_FINISHED);
            register.accept(CHARGER_FINISHED_EMISSIVE);
        }
    }

}
