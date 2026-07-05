package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PipeSegment<P extends PipeSegmentProperties> {

    @Getter
    private P properties;

    @Getter
    private int segmentLength = 1;

    private @Nullable PipeSegment<P>[] adjacentSegments;
    private List<PipeBlockEntity<P>> entities = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public PipeSegment(P segmentProperties) {
        adjacentSegments = (PipeSegment<P>[])new PipeSegment[6];
    }

}
