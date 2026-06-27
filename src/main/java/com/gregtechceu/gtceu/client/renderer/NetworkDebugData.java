package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.core.BlockPos;

import java.util.List;

public record NetworkDebugData(Type type, List<BlockPos> nodes, List<Edge> edges) {

    public record Edge(BlockPos first, BlockPos second) {
    }

    public enum Type {
        ENERGY(0xFF3A3A),
        FLUID(0x34B9FF),
        ITEM(0xFFB13B),
        OPTICAL(0xD95CFF),
        LASER(0xFF4FFF),
        DUCT(0x61D65F),
        COMPUTATION(0xFFE45C);

        public final int color;

        Type(int color) {
            this.color = color;
        }
    }
}
