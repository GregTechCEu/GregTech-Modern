package com.gregtechceu.gtceu.core;

import net.minecraft.server.level.WorldGenRegion;

import javax.annotation.Nullable;

public interface IGTBlender {

    @Nullable
    WorldGenRegion getRegion();

    void setRegion(@Nullable WorldGenRegion region);
}
