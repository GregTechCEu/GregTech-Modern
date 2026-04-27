package com.gregtechceu.gtceu.core.util;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true)
public class CapturedQuadData implements AutoCloseable {

    @Getter
    private @Nullable RenderType renderType;
    @Getter
    private BlockPos pos;

    public CapturedQuadData with(RenderType renderType, BlockPos pos) {
        this.renderType = renderType;
        this.pos = pos;
        return this;
    }

    @Override
    public void close() {
        this.renderType = null;
        this.pos = null;
    }
}
