package com.gregtechceu.gtceu.common.pipelike.block.laser;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeStructure;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.PipeStructureRegistry;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.client.renderer.pipe.AbstractPipeModel;
import com.gregtechceu.gtceu.client.renderer.pipe.ActivablePipeModel;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record LaserStructure(String name, float renderThickness, boolean mirror, ActivablePipeModel model)
        implements IPipeStructure {

    public static final LaserStructure NORMAL = new LaserStructure("laser_pipe_normal", 0.375f,
            false, ActivablePipeModel.LASER);
    public static final LaserStructure MIRROR = new LaserStructure("laser_pipe_mirror", 0.5f,
            true, ActivablePipeModel.LASER);

    public LaserStructure(String name, float renderThickness, boolean mirror, ActivablePipeModel model) {
        this.name = name;
        this.renderThickness = renderThickness;
        this.mirror = mirror;
        this.model = model;
        PipeStructureRegistry.register(this);
    }

    @Override
    public ResourceTexture getPipeTexture(boolean isBlock) {
        return isBlock ? GuiTextures.TOOL_WIRE_CONNECT : GuiTextures.TOOL_WIRE_BLOCK;
    }

    @Override
    public boolean canConnectTo(Direction side, byte connectionMask) {
        if (mirror) {
            byte connectionCount = 0;
            for (Direction facing : GTUtil.DIRECTIONS) {
                if (facing == side) continue;
                if (GTUtil.evalMask(facing, connectionMask)) {
                    if (facing.getOpposite() == side) return false; // must be a bent connection
                    connectionCount++;
                }
                if (connectionCount > 1) return false;
            }
        } else {
            for (Direction facing : GTUtil.DIRECTIONS) {
                if (facing == side) continue;
                if (GTUtil.evalMask(facing, connectionMask)) {
                    return facing.getOpposite() == side;
                }
            }
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

    public static void registerDefaultStructures(Consumer<LaserStructure> register) {
        register.accept(NORMAL);
    }
}
