package com.gregtechceu.gtceu.common.pipelike.longdistance;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.client.model.PipeModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * This class defines a long distance pipe type. This class MUST be a singleton class!
 */
public enum LDPipeType implements IPipeType<LDPipeData> {

    FLUID("ldfluid"),
    ITEM("lditem");
    public static final ResourceLocation TYPE_ID = GTCEu.id("longdistance");

    public final String name;

    LDPipeType(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public float getThickness() {
        // use the thickest possible
        return 0.95f;
    }

    @Override
    public LDPipeData modifyProperties(LDPipeData baseProperties) {
        // There is no properties to modify
        return baseProperties;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public ResourceLocation type() {
        return TYPE_ID;
    }

    public PipeModel createPipeModel() {
        // TODO: rename location of resources
        return new PipeModel(this.getThickness(), () -> GTCEu.id("block/pipe/pipe_side"), () -> GTCEu.id("block/pipe/pipe_%s_in".formatted(name)));
    }
}
