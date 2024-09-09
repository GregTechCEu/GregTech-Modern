package com.gregtechceu.gtceu.api.capability.data.query;

import com.gregtechceu.gtceu.api.capability.data.IDataAccess;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DataQueryObject {

    private static int ID = 0;

    private final int id;

    @Getter
    @Setter
    private boolean shouldTriggerWalker = false;

    private final ReferenceOpenHashSet<IDataAccess> visited = new ReferenceOpenHashSet<>();

    public DataQueryObject() {
        this.id = ID++;
    }

    /**
     * Used to tell this query when it passes through a location during traversal,
     * know if the location supports this query,
     * and know if this query has already visited the location.
     *
     * @param location the location next on traversal
     * @return whether the location is not null, supports this query, and has not yet been visited.
     */
    @Contract("null -> false")
    public final boolean traverseTo(@Nullable IDataAccess location) {
        return location != null && location.supportsQuery(this) && this.visited.add(location);
    }

    @NotNull
    public abstract DataQueryFormat getFormat();

    @Override
    public int hashCode() {
        return id;
    }
}
