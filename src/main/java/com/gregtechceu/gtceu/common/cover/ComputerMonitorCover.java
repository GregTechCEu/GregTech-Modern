package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import com.gregtechceu.gtceu.client.renderer.cover.ComputerMonitorCoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class ComputerMonitorCover extends CoverBehavior {

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canPipePassThrough() {
        return false;
    }

    @Override
    public @Nullable IDynamicCoverRenderer getDynamicRenderer() {
        return new ComputerMonitorCoverRenderer();
    }

    // No implementation here, this cover is just for decorative purposes
}
