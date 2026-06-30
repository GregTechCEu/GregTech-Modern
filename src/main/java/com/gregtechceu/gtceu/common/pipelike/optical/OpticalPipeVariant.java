package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.pipenet.IPipeVariant;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;

import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum OpticalPipeVariant implements IPipeVariant<OpticalPipeSegmentProperties>, StringRepresentable {

    NORMAL;

    public static final ResourceLocation TYPE = GTCEu.id("optical");

    @Override
    public float getThickness() {
        return 0.375F;
    }

    @Override
    public OpticalPipeSegmentProperties createSegmentProperties(PipeBlock block) {
        return new OpticalPipeSegmentProperties();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public PipeModel createPipeModel(PipeBlock block, GTBlockstateProvider provider) {
        ActivablePipeModel pipeModel = new ActivablePipeModel(block, getThickness(),
                GTCEu.id("block/pipe/pipe_optical_side"), GTCEu.id("block/pipe/pipe_optical_in"),
                provider);
        pipeModel.setSideOverlay(GTCEu.id("block/pipe/pipe_optical_side_overlay"));
        pipeModel.setSideOverlayActive(GTCEu.id("block/pipe/pipe_optical_side_overlay_active"));
        return pipeModel;
    }

    @Override
    public PipeNetworkType getNetworkType() {
        return GTPipeNetworks.OPTICAL;
    }
}
