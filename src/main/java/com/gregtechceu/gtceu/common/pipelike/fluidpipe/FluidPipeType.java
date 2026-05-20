package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.pipenet.IMaterialPipeType;
import com.gregtechceu.gtceu.api.pipenet.PipeSegmentPropertyHolder;
import com.gregtechceu.gtceu.api.pipenet.property.BoolSegmentProperty;
import com.gregtechceu.gtceu.api.pipenet.property.IntSegmentProperty;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.pipelike.SegmentPropertyTypes;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public enum FluidPipeType implements IMaterialPipeType {

    TINY("tiny", 0.25f, 1, pipeTinyFluid),
    SMALL("small", 0.375f, 2, pipeSmallFluid),
    NORMAL("normal", 0.5f, 6, pipeNormalFluid),
    LARGE("large", 0.625f, 12, pipeLargeFluid),
    HUGE("huge", 0.75f, 24, pipeHugeFluid),
    QUADRUPLE("quadruple", 0.875f, 2, pipeQuadrupleFluid, 4),
    NONUPLE("nonuple", 0.875f, 2, pipeNonupleFluid, 9);

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
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    public PipeModel createPipeModel(PipeBlock<?> block, Material material, GTBlockstateProvider provider) {
        String side = "block/pipe/pipe%s_side";
        String end = "block/pipe/pipe_%s_in".formatted(name);
        if (material.hasProperty(PropertyKey.WOOD)) {
            side += "_wood";
            end += "_wood";
        }
        if (channels == 9) {
            side = side.formatted("_non");
        } else if (channels == 4) {
            side = side.formatted("_quad");
        } else {
            side = side.formatted("");
        }
        return new PipeModel(block, provider, thickness, GTCEu.id(side), GTCEu.id(end));
    }

    @Override
    public PipeSegmentPropertyHolder buildSegmentProperties(@Nullable Material material) {
        if (material == null || material.getProperty(PropertyKey.FLUID_PIPE) == null)
            throw new IllegalArgumentException(
                    "Attempted to build fluid pipe properties for null material or material without PropertyKey.FLUID_PIPE");
        var segmentProperties = new PipeSegmentPropertyHolder();
        var materialProperties = material.getProperty(PropertyKey.FLUID_PIPE);

        segmentProperties
                .setProperty(SegmentPropertyTypes.FLUID_THROUGHPUT,
                        new IntSegmentProperty(materialProperties.getThroughput() * capacityMultiplier))
                .setProperty(SegmentPropertyTypes.MAX_TEMPERATURE,
                        new IntSegmentProperty(materialProperties.getMaxFluidTemperature()))
                .setProperty(SegmentPropertyTypes.CHANNELS, new IntSegmentProperty(channels))
                .setProperty(SegmentPropertyTypes.GAS_PROOF, new BoolSegmentProperty(materialProperties.isGasProof()))
                .setProperty(SegmentPropertyTypes.ACID_PROOF, new BoolSegmentProperty(materialProperties.isAcidProof()))
                .setProperty(SegmentPropertyTypes.CRYO_PROOF, new BoolSegmentProperty(materialProperties.isCryoProof()))
                .setProperty(SegmentPropertyTypes.PLASMA_PROOF,
                        new BoolSegmentProperty(materialProperties.isPlasmaProof()));

        return segmentProperties;
    }
}
