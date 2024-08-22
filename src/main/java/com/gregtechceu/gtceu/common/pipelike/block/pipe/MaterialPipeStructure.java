package com.gregtechceu.gtceu.common.pipelike.block.pipe;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeChanneledStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record MaterialPipeStructure(String name, int material, int channelCount, boolean restrictive, TagPrefix prefix,
                                    float renderThickness, PipeModel model)
        implements IPipeMaterialStructure, IPipeChanneledStructure {

    public static final MaterialPipeStructure TINY = new MaterialPipeStructure("pipe_tiny", 1, 1, false,
            TagPrefix.pipeTiny, 0.25f,
            PipeModel.INSTANCES[0]);
    public static final MaterialPipeStructure SMALL = new MaterialPipeStructure("pipe_small", 2, 1, false,
            TagPrefix.pipeSmall, 0.375f,
            PipeModel.INSTANCES[1]);
    public static final MaterialPipeStructure NORMAL = new MaterialPipeStructure("pipe_normal", 6, 1, false,
            TagPrefix.pipeNormal, 0.5f,
            PipeModel.INSTANCES[2]);
    public static final MaterialPipeStructure LARGE = new MaterialPipeStructure("pipe_large", 12, 1, false,
            TagPrefix.pipeLarge, 0.75f,
            PipeModel.INSTANCES[3]);
    public static final MaterialPipeStructure HUGE = new MaterialPipeStructure("pipe_huge", 24, 1, false,
            TagPrefix.pipeHuge, 0.875f,
            PipeModel.INSTANCES[4]);

    public static final MaterialPipeStructure QUADRUPLE = new MaterialPipeStructure("pipe_quadruple", 8, 4, false,
            TagPrefix.pipeQuadruple,
            0.95f, PipeModel.INSTANCES[5]);
    public static final MaterialPipeStructure NONUPLE = new MaterialPipeStructure("pipe_nonuple", 18, 9, false,
            TagPrefix.pipeNonuple, 0.95f,
            PipeModel.INSTANCES[6]);

    public static final MaterialPipeStructure TINY_RESTRICTIVE = new MaterialPipeStructure("pipe_tiny_restrictive", 1,
            1, true,
            TagPrefix.pipeTinyRestrictive, 0.25f, PipeModel.RESTRICTIVE_INSTANCES[0]);
    public static final MaterialPipeStructure SMALL_RESTRICTIVE = new MaterialPipeStructure("pipe_small_restrictive", 2,
            1, true,
            TagPrefix.pipeSmallRestrictive, 0.375f, PipeModel.RESTRICTIVE_INSTANCES[1]);
    public static final MaterialPipeStructure NORMAL_RESTRICTIVE = new MaterialPipeStructure("pipe_normal_restrictive",
            6, 1, true,
            TagPrefix.pipeNormalRestrictive, 0.5f, PipeModel.RESTRICTIVE_INSTANCES[2]);
    public static final MaterialPipeStructure LARGE_RESTRICTIVE = new MaterialPipeStructure("pipe_large_restrictive",
            12, 1, true,
            TagPrefix.pipeLargeRestrictive, 0.75f, PipeModel.RESTRICTIVE_INSTANCES[3]);
    public static final MaterialPipeStructure HUGE_RESTRICTIVE = new MaterialPipeStructure("pipe_huge_restrictive", 24,
            1, true,
            TagPrefix.pipeHugeRestrictive, 0.875f, PipeModel.RESTRICTIVE_INSTANCES[4]);

    public static final MaterialPipeStructure QUADRUPLE_RESTRICTIVE = new MaterialPipeStructure(
            "pipe_quadruple_restrictive", 8, 4, true,
            TagPrefix.pipeQuadrupleRestrictive, 0.95f, PipeModel.RESTRICTIVE_INSTANCES[5]);
    public static final MaterialPipeStructure NONUPLE_RESTRICTIVE = new MaterialPipeStructure(
            "pipe_nonuple_restrictive", 18, 9, true,
            TagPrefix.pipeNonupleRestrictive, 0.95f, PipeModel.RESTRICTIVE_INSTANCES[6]);

    public MaterialPipeStructure(String name, int material, int channelCount, boolean restrictive, TagPrefix prefix,
                                 float renderThickness, PipeModel model) {
        this.name = name;
        this.material = material;
        this.channelCount = channelCount;
        this.restrictive = restrictive;
        this.prefix = prefix;
        this.renderThickness = renderThickness;
        this.model = model;
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
    public AbstractPipeModel<?> getModel() {
        return model;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    public static void registerDefaultStructures(Consumer<MaterialPipeStructure> register) {
        register.accept(TINY);
        register.accept(SMALL);
        register.accept(NORMAL);
        register.accept(LARGE);
        register.accept(HUGE);
        register.accept(QUADRUPLE);
        register.accept(NONUPLE);
        register.accept(TINY_RESTRICTIVE);
        register.accept(SMALL_RESTRICTIVE);
        register.accept(NORMAL_RESTRICTIVE);
        register.accept(LARGE_RESTRICTIVE);
        register.accept(HUGE_RESTRICTIVE);
        register.accept(QUADRUPLE_RESTRICTIVE);
        register.accept(NONUPLE_RESTRICTIVE);
    }
}
