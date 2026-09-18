package brachy.modularui.utils.memoization;

import com.google.common.base.Suppliers;

import java.time.Duration;
import java.util.function.Supplier;

public final class Memoizer {

    /**
     * This method doesn't return a thread-safe memoization of the delegate.
     * <p>
     * If you need thread safety, you should use
     * {@link Suppliers#memoize(com.google.common.base.Supplier) Suppliers#memoize} instead.
     * </p>
     *
     * @param delegate the supplier to memoize
     * @return memoized supplier for the delegate
     * @see #memoize(Supplier, long)
     * @see Suppliers#memoize(com.google.common.base.Supplier)
     */
    public static <T> MemoizedSupplier<T> memoize(Supplier<T> delegate) {
        return memoize(delegate, -1);
    }

    /**
     * Store a supplier in a delegate function to be computed once, and only again after time to live has expired.
     * <p>
     * If you need thread safety, you should use
     * {@link Suppliers#memoize(com.google.common.base.Supplier) Suppliers#memoize} instead.
     * </p>
     *
     * @param <T>        The type of object supplied
     * @param delegate   The supplier to memoize
     * @param timeToLive Time to retain calculation. If negative, retain indefinitely.
     * @see #memoize(Supplier)
     * @see Suppliers#memoize(com.google.common.base.Supplier)
     */
    public static <T> MemoizedSupplier<T> memoize(Supplier<T> delegate, Duration timeToLive) {
        return memoize(delegate, timeToLive.toNanos());
    }

    /**
     * Store a supplier in a delegate function to be computed once, and only again after time to live has expired.
     * <p>
     * If you need thread safety, you should use
     * {@link Suppliers#memoize(com.google.common.base.Supplier) Suppliers#memoize} instead.
     * </p>
     *
     * @param <T>        The type of object supplied
     * @param delegate   The supplier to memoize
     * @param timeToLive Time in nanoseconds to retain calculation. If negative, retain indefinitely.
     * @see #memoize(Supplier)
     * @see Suppliers#memoize(com.google.common.base.Supplier)
     */
    public static <T> MemoizedSupplier<T> memoize(Supplier<T> delegate, long timeToLive) {
        return new MemoizedSupplier<>(delegate, timeToLive);
    }

    private Memoizer() {
        throw new AssertionError();
    }
}
