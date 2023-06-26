package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IMaintenanceHatch;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
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

public class MaintenanceHatchPartRenderer extends TieredHullMachineRenderer {
    private static final ResourceLocation MAINTENANCE_OVERLAY_TAPED = GTCEu.id("block/overlay/machine/overlay_maintenance_taped");

    private final ResourceLocation overlayTexture;

    public MaintenanceHatchPartRenderer(int tier, ResourceLocation overlayTexture) {
        super(tier, GTCEu.id("block/tinted_cube_all"));
        this.overlayTexture = overlayTexture;
    }

    @Override
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side == frontFacing && machine instanceof IMaintenanceHatch maintenanceHatch) {
            quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(maintenanceHatch.isTaped() ? MAINTENANCE_OVERLAY_TAPED : overlayTexture), modelState));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(overlayTexture);
        }
    }
}
