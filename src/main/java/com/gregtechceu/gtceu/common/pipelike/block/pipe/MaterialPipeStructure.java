package com.gregtechceu.gtceu.common.pipelike.block.pipe;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRedirector;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRegistry;

import org.jetbrains.annotations.NotNull;

public record MaterialPipeStructure(String name, int material, int channelCount, boolean restrictive, TagPrefix prefix,
                                    float renderThickness, PipeModelRedirector model)
        implements IPipeMaterialStructure, IPipeChanneledStructure {

    public static final MaterialPipeStructure TINY = new MaterialPipeStructure("tiny", 1, 1, false,
            TagPrefix.pipeTiny, 0.25f, PipeModelRegistry.getPipeModel(0));
    public static final MaterialPipeStructure SMALL = new MaterialPipeStructure("small", 2, 1, false,
            TagPrefix.pipeSmall, 0.375f, PipeModelRegistry.getPipeModel(1));
    public static final MaterialPipeStructure NORMAL = new MaterialPipeStructure("normal", 6, 1, false,
            TagPrefix.pipeNormal, 0.5f, PipeModelRegistry.getPipeModel(2));
    public static final MaterialPipeStructure LARGE = new MaterialPipeStructure("large", 12, 1, false,
            TagPrefix.pipeLarge, 0.75f, PipeModelRegistry.getPipeModel(3));
    public static final MaterialPipeStructure HUGE = new MaterialPipeStructure("huge", 24, 1, false,
            TagPrefix.pipeHuge, 0.875f, PipeModelRegistry.getPipeModel(4));

    public static final MaterialPipeStructure QUADRUPLE = new MaterialPipeStructure("quadruple", 8, 4, false,
            TagPrefix.pipeQuadruple, 0.95f, PipeModelRegistry.getPipeModel(5));
    public static final MaterialPipeStructure NONUPLE = new MaterialPipeStructure("nonuple", 18, 9, false,
            TagPrefix.pipeNonuple, 0.95f, PipeModelRegistry.getPipeModel(6));

    public static final MaterialPipeStructure TINY_RESTRICTIVE = new MaterialPipeStructure("tiny_restrictive", 1,
            1, true, TagPrefix.pipeTinyRestrictive, 0.25f, PipeModelRegistry.getPipeRestrictiveModel(0));
    public static final MaterialPipeStructure SMALL_RESTRICTIVE = new MaterialPipeStructure("small_restrictive", 2,
            1, true, TagPrefix.pipeSmallRestrictive, 0.375f, PipeModelRegistry.getPipeRestrictiveModel(1));
    public static final MaterialPipeStructure NORMAL_RESTRICTIVE = new MaterialPipeStructure("normal_restrictive",
            6, 1, true, TagPrefix.pipeNormalRestrictive, 0.5f, PipeModelRegistry.getPipeRestrictiveModel(2));
    public static final MaterialPipeStructure LARGE_RESTRICTIVE = new MaterialPipeStructure("large_restrictive",
            12, 1, true, TagPrefix.pipeLargeRestrictive, 0.75f, PipeModelRegistry.getPipeRestrictiveModel(3));
    public static final MaterialPipeStructure HUGE_RESTRICTIVE = new MaterialPipeStructure("huge_restrictive", 24,
            1, true, TagPrefix.pipeHugeRestrictive, 0.875f, PipeModelRegistry.getPipeRestrictiveModel(4));

    public static final MaterialPipeStructure QUADRUPLE_RESTRICTIVE = new MaterialPipeStructure(
            "quadruple_restrictive", 8, 4, true, TagPrefix.pipeQuadrupleRestrictive, 0.95f,
            PipeModelRegistry.getPipeRestrictiveModel(5));
    public static final MaterialPipeStructure NONUPLE_RESTRICTIVE = new MaterialPipeStructure(
            "nonuple_restrictive", 18, 9, true, TagPrefix.pipeNonupleRestrictive, 0.95f,
            PipeModelRegistry.getPipeRestrictiveModel(6));

    public MaterialPipeStructure {
        PipeStructureRegistry.register(this);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Override
    public TagPrefix getPrefix() {
        return prefix;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public int getChannelCount() {
        return channelCount;
    }

    @Override
    public PipeModelRedirector getModel() {
        return model;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
