package com.gregtechceu.gtceu.api.mui.base.value.sync;

import com.gregtechceu.gtceu.api.mui.base.value.IValue;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A helper interface for syncing an object value.
 *
 * @param <T> object value type
 */
public interface IValueSyncHandler<T> extends IValue<T> {

    /**
     * Updates the current value and the source and syncs it to client/server.
     *
     * @param value new value
     */
    @Override
    default void setValue(T value) {
        setValue(value, true, true);
    }

    /**
     * Updates the current value and syncs it to client/server.
     *
     * @param value     new value
     * @param setSource whether the source should be updated with the new value
     */
    default void setValue(T value, boolean setSource) {
        setValue(value, setSource, true);
    }

    /**
     * Updates the current value.
     *
     * @param value     new value
     * @param setSource whether the source should be updated with the new value
     * @param sync      whether the new value should be synced to client/server
     */
    void setValue(T value, boolean setSource, boolean sync);

    /**
     * Determines if the current value is different from source and updates the current value if it is.
     *
     * @param isFirstSync true if it's the first tick in the ui
     * @return true if the current value was different from source
     */
    boolean updateCacheFromSource(boolean isFirstSync);

    /**
     * Updates the cache from source and syncs it to the other sides.
     * <p>
     * Usually this is
     * <code>
     * setValue(getter.get(), false, true);
     * </code>
     * </p>
     * Where {@code getter} is the source.
     */
    void notifyUpdate();

    /**
     * Writes the current value to the buffer
     *
     * @param buffer buffer to write to
     */
    void write(FriendlyByteBuf buffer);

    /**
     * Reads a value from the buffer and sets the current value
     *
     * @param buffer buffer to read from
     */
    void read(FriendlyByteBuf buffer);
}
