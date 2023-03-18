package com.gregtechceu.gtceu.api.cover;


import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

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
    private final ICoverRenderer coverRenderer;

    public CoverDefinition(ResourceLocation id, CoverBehaviourProvider behaviorCreator, ICoverRenderer coverRenderer) {
        this.behaviorCreator = behaviorCreator;
        this.id = id;
        this.coverRenderer = coverRenderer;
    }

    public CoverBehavior createCoverBehavior(ICoverable metaTileEntity, Direction side) {
        return behaviorCreator.create(this, metaTileEntity, side);
    }

}
