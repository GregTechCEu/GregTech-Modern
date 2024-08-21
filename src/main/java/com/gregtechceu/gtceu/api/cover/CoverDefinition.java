package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

public final class CoverDefinition {

    public interface CoverBehaviourProvider {

        CoverBehavior create(CoverDefinition definition, ICoverable coverable, Direction side);
    }

    public interface TieredCoverBehaviourProvider {

        CoverBehavior create(CoverDefinition definition, ICoverable coverable, Direction side, int tier);
    }

    @Getter
    private final ResourceLocation id;
    private final CoverBehaviourProvider behaviorCreator;
    @Getter
    private final CoverRenderer coverRenderer;

    public CoverDefinition(ResourceLocation id, CoverBehaviourProvider behaviorCreator, CoverRenderer coverRenderer) {
        this.behaviorCreator = behaviorCreator;
        this.id = id;
        this.coverRenderer = coverRenderer;
    }

    public CoverBehavior createCoverBehavior(ICoverable metaBlockEntity, Direction side) {
        return behaviorCreator.create(this, metaBlockEntity, side);
    }
}
