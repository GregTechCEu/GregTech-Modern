package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.pipenet.IPipeVariant;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import lombok.Getter;

import java.util.Locale;

public enum DuctPipeVariant implements IPipeVariant<DuctPipeProperties>, StringRepresentable {

    SMALL("small", 0.375f, 2f),
    NORMAL("normal", 0.5f, 4f),
    LARGE("large", 0.75f, 8f),
    HUGE("huge", 0.875f, 16f),
    ;

    public static final DuctPipeVariant[] VALUES = values();

    @Getter
    public final String name;
    @Getter
    private final float thickness;
    @Getter
    private final float rateMultiplier;

    DuctPipeVariant(String name, float thickness, float rateMultiplier) {
        this.name = name;
        this.thickness = thickness;
        this.rateMultiplier = rateMultiplier;
    }

    @Override
    public DuctPipeProperties createSegmentProperties(PipeBlock<DuctPipeProperties> block) {
        return new DuctPipeProperties(getRateMultiplier());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public PipeModel createPipeModel(PipeBlock<?> block, GTBlockstateProvider provider) {
        return new PipeModel(block, provider, getThickness(),
                GTCEu.id("block/pipe/pipe_duct_side"), GTCEu.id("block/pipe/pipe_duct_in"));
    }
}
