package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.pipenet.IPipeVariant;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum LaserPipeVariant implements IPipeVariant<LaserPipeSegmentProperties>, StringRepresentable {

    NORMAL;

    @Override
    public float getThickness() {
        return 0.375f;
    }

    @Override
    public LaserPipeSegmentProperties createSegmentProperties(PipeBlock<LaserPipeSegmentProperties> block) {
        return LaserPipeSegmentProperties.INSTANCE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public PipeModel createPipeModel(PipeBlock<?> block, GTBlockstateProvider provider) {
        ActivablePipeModel model = new ActivablePipeModel(block, getThickness(),
                GTCEu.id("block/pipe/pipe_laser_side"), GTCEu.id("block/pipe/pipe_laser_in"),
                provider);
        model.setSideOverlay(GTCEu.id("block/pipe/pipe_laser_side_overlay"));
        model.setSideOverlayActive(GTCEu.id("block/pipe/pipe_laser_side_overlay_emissive"));
        return model;
    }
}
