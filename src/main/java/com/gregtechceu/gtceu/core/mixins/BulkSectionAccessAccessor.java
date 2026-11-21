package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BulkSectionAccess.class)
public interface BulkSectionAccessAccessor {

    @Accessor
    Long2ObjectMap<LevelChunkSection> getAcquiredSections();

    @Accessor
    @Nullable
    LevelChunkSection getLastSection();

    @Accessor
    void setLastSection(@Nullable LevelChunkSection section);

    @Accessor
    long getLastSectionKey();

    @Accessor
    void setLastSectionKey(long lastSectionKey);
}
