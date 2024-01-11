package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DummyCleanroom implements ICleanroomProvider {

    private final boolean allowsAllTypes;
    private final Collection<CleanroomType> allowedTypes;

    /**
     * Create a Dummy Cleanroom that provides specific types
     * @param types the types to provide
     */
    @Nonnull
    public static DummyCleanroom createForTypes(@Nonnull Collection<CleanroomType> types) {
        return new DummyCleanroom(types, false);
    }

    /**
     * Create a Dummy Cleanroom that provides all types
     */
    @Nonnull
    public static DummyCleanroom createForAllTypes() {
        return new DummyCleanroom(Collections.emptyList(), true);
    }

    private DummyCleanroom(@Nonnull Collection<CleanroomType> allowedTypes, boolean allowsAllTypes) {
        this.allowedTypes = allowedTypes;
        this.allowsAllTypes = allowsAllTypes;
    }

    @Override
    public boolean isClean() {
        return true;
    }


    @Override
    public Set<CleanroomType> getTypes() {
        return allowsAllTypes ? CleanroomType.getAllTypes() : new HashSet<>(allowedTypes);
    }

}
