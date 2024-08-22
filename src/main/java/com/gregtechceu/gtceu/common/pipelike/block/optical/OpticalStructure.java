package com.gregtechceu.gtceu.common.pipelike.block.optical;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.client.renderer.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record OpticalStructure(String name, float renderThickness, ActivablePipeModel model) implements IPipeStructure {

    public static final OpticalStructure INSTANCE = new OpticalStructure("optical_pipe_normal", 0.375f,
            ActivablePipeModel.OPTICAL);

    public OpticalStructure(String name, float renderThickness, ActivablePipeModel model) {
        this.name = name;
        this.renderThickness = renderThickness;
        this.model = model;
        PipeStructureRegistry.register(this);
    }

    @Override
    public boolean canConnectTo(Direction side, byte connectionMask) {
        byte connectionCount = 0;
        for (Direction facing : GTUtil.DIRECTIONS) {
            if (facing == side) continue;
            if (GTUtil.evalMask(facing, connectionMask)) {
                connectionCount++;
            }
            if (connectionCount > 1) return false;
        }
        return true;
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
        return model;
    }

    public static void registerDefaultStructures(Consumer<OpticalStructure> register) {
        register.accept(INSTANCE);
    }
}
