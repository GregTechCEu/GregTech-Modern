package com.gregtechceu.gtceu.client.renderer;

import net.minecraft.client.renderer.RenderType;

import java.util.function.BooleanSupplier;

public record CustomChunkRenderPass(RenderType renderType, AlphaCutoff alphaCutoff, boolean mipped,
                                    DrawStage drawStage, TerrainPhase terrainPhase, BooleanSupplier loadCondition) {

    public enum AlphaCutoff {
        // These names intentionally match Sodium and Embeddium's AlphaCutoffParameter values.
        ZERO,
        ONE_TENTH,
        HALF,
        ONE
    }

    public enum DrawStage {
        AFTER_CUTOUT,
        MANUAL
    }

    public enum TerrainPhase {
        SOLID,
        CUTOUT_MIPPED,
        CUTOUT,
        TRANSLUCENT,
        TRIPWIRE,
        CUSTOM
    }
}
