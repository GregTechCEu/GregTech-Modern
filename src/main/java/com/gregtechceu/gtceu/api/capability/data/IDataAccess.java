package com.gregtechceu.gtceu.api.capability.data;

import com.gregtechceu.gtceu.api.capability.data.query.DataAccessFormat;
import com.gregtechceu.gtceu.api.capability.data.query.DataQueryObject;

import org.jetbrains.annotations.NotNull;

public interface IDataAccess {

    /**
     * Queries this {@link IDataAccess} with the specified query.
     * 
     * @param queryObject the object representing the query. Can be cached in a weak set created by
     *                    {@link com.gregtechceu.gtceu.utils.GTUtil#createWeakHashSet()} in order to prevent endless
     *                    recursion.
     * @return if the query has been cancelled
     */
    boolean accessData(@NotNull DataQueryObject queryObject);

    /**
     * @return the {@link DataAccessFormat} this {@link IDataAccess} uses.
     */
    @NotNull
    DataAccessFormat getFormat();

    /**
     * @param queryObject a query object
     * @return whether this {@link IDataAccess} supports the query object.
     */
    default boolean supportsQuery(@NotNull DataQueryObject queryObject) {
        return getFormat().supportsFormat(queryObject.getFormat());
    }

    /**
     * Provides standardized logic for querying a collection of {@link IDataAccess}es.
     * 
     * @param accesses the {@link IDataAccess}es to query.
     * @param query    the object representing the query.
     * @return if the query has been cancelled
     */
    static boolean accessData(@NotNull Iterable<? extends IDataAccess> accesses,
                              @NotNull DataQueryObject query) {
        boolean walk = false;
        boolean cancelled = false;
        for (IDataAccess access : accesses) {
            query.setShouldTriggerWalker(false);
            cancelled = access.accessData(query);
            if (!walk) walk = query.shouldTriggerWalker();
            if (cancelled) break;
        }
        query.setShouldTriggerWalker(walk);
        return cancelled;
    }
}
