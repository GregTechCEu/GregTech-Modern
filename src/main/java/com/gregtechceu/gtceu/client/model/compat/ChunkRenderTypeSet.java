package com.gregtechceu.gtceu.client.model.compat;

import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ChunkRenderTypeSet {

    private static final ChunkRenderTypeSet ALL = new ChunkRenderTypeSet(null);

    private final Set<RenderType> renderTypes;

    private ChunkRenderTypeSet(Set<RenderType> renderTypes) {
        this.renderTypes = renderTypes;
    }

    public static ChunkRenderTypeSet all() {
        return ALL;
    }

    public static ChunkRenderTypeSet of(RenderType renderType) {
        return new ChunkRenderTypeSet(new LinkedHashSet<>(Set.of(renderType)));
    }

    public static ChunkRenderTypeSet union(Collection<ChunkRenderTypeSet> sets) {
        if (sets.isEmpty() || sets.stream().anyMatch(ChunkRenderTypeSet::isAll)) {
            return all();
        }
        Set<RenderType> merged = new LinkedHashSet<>();
        for (ChunkRenderTypeSet set : sets) {
            merged.addAll(set.renderTypes);
        }
        return new ChunkRenderTypeSet(merged);
    }

    public boolean contains(RenderType renderType) {
        return isAll() || renderTypes.contains(renderType);
    }

    private boolean isAll() {
        return renderTypes == null;
    }
}
