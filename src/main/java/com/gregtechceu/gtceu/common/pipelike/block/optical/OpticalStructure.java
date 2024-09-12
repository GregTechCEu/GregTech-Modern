package com.gregtechceu.gtceu.common.pipelike.block.optical;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistrationEvent;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRedirector;
import com.gregtechceu.gtceu.client.renderer.pipe.PipeModelRegistry;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

public record OpticalStructure(String name, float renderThickness, PipeModelRedirector model)
        implements IPipeStructure {

    public static final OpticalStructure INSTANCE = new OpticalStructure("optical_pipe_normal", 0.375f,
            PipeModelRegistry.getOpticalModel());

    @Override
    public ResourceTexture getPipeTexture(boolean isBlock) {
        return isBlock ? GuiTextures.TOOL_WIRE_CONNECT : GuiTextures.TOOL_WIRE_BLOCK;
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
    public PipeModelRedirector getModel() {
        return model;
    }

    public static void register(@NotNull PipeStructureRegistrationEvent event) {
        event.register(INSTANCE);
    }
}
