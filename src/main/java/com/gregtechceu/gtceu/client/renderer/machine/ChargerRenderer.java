package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

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

import java.util.*;
import java.util.function.Consumer;

public class ChargerRenderer extends TieredHullMachineRenderer {

    public final static ResourceLocation CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public final static ResourceLocation CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public final static ResourceLocation CHARGER_RUNNING_EMISSIVE = GTCEu
            .id("block/machines/charger/overlay_charger_running_emissive");
    public final static ResourceLocation CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public final static ResourceLocation CHARGER_FINISHED_EMISSIVE = GTCEu
            .id("block/machines/charger/overlay_charger_finished_emissive");

    public ChargerRenderer(int tier) {
        super(tier, GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var state = ChargerMachine.State.IDLE;
        if (machine instanceof ChargerMachine charger) {
            state = charger.getState();
        }

        if (side != frontFacing || modelFacing == null) {
            return;
        }
        switch (state) {
            case IDLE -> quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_IDLE),
                    modelState, -1, 0, false, true));
            case RUNNING -> {
                quads.add(StaticFaceBakery.bakeFace(modelFacing,
                        ModelFactory.getBlockSprite(CHARGER_RUNNING),
                        modelState, -1, 0, true, true));
                if (ConfigHolder.INSTANCE.client.machinesEmissiveTextures) {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(CHARGER_RUNNING_EMISSIVE),
                            modelState, -101, 15, true, false));
                }
            }
            case FINISHED -> {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(CHARGER_FINISHED),
                        modelState, -1, 0, true, true));
                if (ConfigHolder.INSTANCE.client.machinesEmissiveTextures) {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(CHARGER_FINISHED_EMISSIVE),
                            modelState, -101, 15, true, false));
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
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
