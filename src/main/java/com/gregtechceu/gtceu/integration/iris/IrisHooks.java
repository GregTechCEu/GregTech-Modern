package com.gregtechceu.gtceu.integration.iris;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@UtilityClass
public class IrisHooks {

    /// Do not access directly, use {@link #getBloomRenderingPhase()}
    @ApiStatus.Internal
    public static @Nullable WorldRenderingPhase BLOOM_RENDERING_PHASE;

    public static WorldRenderingPhase getBloomRenderingPhase() {
        return Objects.requireNonNull(BLOOM_RENDERING_PHASE, "BLOOM_PHASE == null");
    }
}
