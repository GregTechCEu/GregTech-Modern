package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.client.model.PipeModel;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public enum FluidPipeType implements IMaterialPipeType<FluidPipeData> {

    TINY("tiny", 0.25f, 1, pipeTinyFluid),
    SMALL("small", 0.375f, 2, pipeSmallFluid),
    NORMAL("normal", 0.5f, 6, pipeNormalFluid),
    LARGE("large", 0.75f, 12, pipeLargeFluid),
    HUGE("huge", 0.875f, 24, pipeHugeFluid),
    QUADRUPLE("quadruple", 0.95f, 2, pipeQuadrupleFluid, 4),
    NONUPLE("nonuple", 0.95f, 2, pipeNonupleFluid, 9);

    public static final ResourceLocation TYPE_ID = GTCEu.id("fluid");

    public final String name;
    public final float thickness;
    public final int capacityMultiplier;
    @Getter
    public final TagPrefix tagPrefix;
    public final int channels;

    FluidPipeType(String name, float thickness, int capacityMultiplier, TagPrefix TagPrefix) {
        this(name, thickness, capacityMultiplier, TagPrefix, 1);
    }

    FluidPipeType(String name, float thickness, int capacityMultiplier, TagPrefix TagPrefix, int channels) {
        this.name = name;
        this.thickness = thickness;
        this.capacityMultiplier = capacityMultiplier;
        this.tagPrefix = TagPrefix;
        this.channels = channels;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public FluidPipeData modifyProperties(FluidPipeData fluidPipeData) {
        var baseProperties = fluidPipeData.properties;
        return new FluidPipeData(new FluidPipeProperties(
                baseProperties.getMaxFluidTemperature(),
                baseProperties.getThroughput() * capacityMultiplier,
                baseProperties.isGasProof(),
                baseProperties.isAcidProof(),
                baseProperties.isCryoProof(),
                baseProperties.isPlasmaProof(),
                channels
        ), fluidPipeData.connections);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    public PipeModel createPipeModel(Material material) {
        if (material.hasProperty(PropertyKey.WOOD)) {
            return new PipeModel(thickness, () -> GTCEu.id("block/pipe/pipe_side_wood"), () -> GTCEu.id("block/pipe/pipe_%s_in_wood".formatted(name)), null, null);
        }
        return new PipeModel(thickness, () -> GTCEu.id("block/pipe/pipe_side"), () -> GTCEu.id("block/pipe/pipe_%s_in".formatted(name)),
                null, null/*() -> GTCEu.id("block/pipe/pipe_side_secondary"), () -> GTCEu.id("block/pipe/pipe_%s_in_secondary".formatted(name)) TODO enable once the textures are added*/);
    }
}
