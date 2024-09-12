package com.gregtechceu.gtceu.common.pipelike.block.duct;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistrationEvent;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRedirector;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRegistry;

import org.jetbrains.annotations.NotNull;

public record DuctStructure(String name, float renderThickness, float rateMultiplier) implements IPipeStructure {

    public static final DuctStructure SMALL = new DuctStructure("small", 0.375f, 2f);
    public static final DuctStructure NORMAL = new DuctStructure("normal", 0.5f, 4f);
    public static final DuctStructure LARGE = new DuctStructure("large", 0.75f, 8f);
    public static final DuctStructure HUGE = new DuctStructure("huge", 0.75f, 16f);

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public PipeModelRedirector getModel() {
        return PipeModelRegistry.getDuctModel();
    }

    public static void register(@NotNull PipeStructureRegistrationEvent event) {
        event.register(SMALL);
        event.register(NORMAL);
        event.register(LARGE);
        event.register(HUGE);
    }
}
