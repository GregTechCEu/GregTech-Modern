package com.gregtechceu.gtceu.common.pipelike.block.duct;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.client.renderer.pipe.DuctPipeModel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record DuctStructure(String name, float renderThickness, float rateMultiplier) implements IPipeStructure {

    public static final DuctStructure SMALL = new DuctStructure("small", 0.375f, 2f);
    public static final DuctStructure NORMAL = new DuctStructure("normal", 0.5f, 4f);
    public static final DuctStructure LARGE = new DuctStructure("large", 0.75f, 8f);
    public static final DuctStructure HUGE = new DuctStructure("huge", 0.75f, 16f);

    public DuctStructure {
        PipeStructureRegistry.register(this);
    }

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
    public AbstractPipeModel<?> getModel() {
        return DuctPipeModel.INSTANCE;
    }

    public static void registerDefaultStructures(Consumer<DuctStructure> register) {
        register.accept(SMALL);
        register.accept(NORMAL);
        register.accept(LARGE);
        register.accept(HUGE);
    }
}
