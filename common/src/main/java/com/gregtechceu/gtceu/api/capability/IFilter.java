package com.gregtechceu.gtceu.api.capability;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Base type for generic filters. In addition to the predicate method, this interface provides priority primarily used
 * in insertion logic.
 * <p>
 * Although this class can be used as lambda interface, it is strongly encouraged to define priority according to
 * criteria specified in javadocs on {@link #getPriority()}.
 *
 * @param <T> type of the instance for filtering
 */
@FunctionalInterface
public interface IFilter<T> extends Predicate<T> {

    /**
     * Compare logic for filter instances.
     */
    Comparator<IFilter<?>> FILTER_COMPARATOR = Comparator
            .<IFilter<?>>comparingInt(IFilter::getPriority)
            .reversed();

    /**
     * Test if this filter accepts the instance. If the parameter is {@code null}, then the behavior of this method is
     * undefined.
     *
     * @param t instance to be tested
     * @return if this filter accepts the instance
     */
    @Override
    boolean test(@Nonnull T t);

    /**
     * Return insertion priority for this filter. The priority is applied on some insertion logics, to prioritize
     * certain filters from others. The priority system uses <i>reverse ordering</i>; higher priority values have
     * precedence over lower ones.
     * <ul>
     *     <li>Whitelist filters have {@code Integer.MAX_VALUE - whitelistSize} as their default priority. The highest
     *     possible number for whitelist priority is {@code Integer.MAX_VALUE - 1}, where only one entry is
     *     whitelisted. The priority can be computed using {@link #whitelistPriority(int)}.</li>
     *     <li>Blacklist filters have {@code Integer.MIN_VALUE + 1 + blacklistSize} as their default priority. The
     *     lowest possible number for blacklist priority is {@code Integer.MIN_VALUE + 2}, where only one entry is
     *     blacklisted. The priority can be computed using {@link #blacklistPriority(int)}.</li>
     *     <li>Filters with unspecified priority have {@code 0} as their priority.</li>
     *     <li>Two values, {@link #firstPriority()}, and {@link #lastPriority()}, can be used to create filter
     *     with highest/lowest possible priority respectively.</li>
     *     <li>For custom filter implementations, it is expected to have at least positive priority for whitelist-like
     *     filters, and negative priority for blacklist-like filters. Methods {@link #whitelistLikePriority()} and
     *     {@link #blacklistLikePriority()} are available as standard priority.</li>
     *     <li>{@link #noPriority()} is reserved for 'no-priority' filters; it's applicable to no-op filters and
     *     its reverse (everything filter).</li>
     * </ul>
     * Although the priority is not a strict requirement, it is strongly encouraged to specify priority according to
     * these criteria.
     *
     * @return insertion priority
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Return the reverse of this filter. The resulting filter returns the opposite of what the original filter would,
     * has priority of inverse of the original.
     *
     * @return reverse of this filter
     */
    @Override
    @Nonnull
    default IFilter<T> negate() {
        return new IFilter<>() {
            @Override
            public boolean test(@Nonnull T t) {
                return !IFilter.this.test(t);
            }

            @Override
            public int getPriority() {
                return -IFilter.this.getPriority();
            }

            @Override
            @Nonnull
            public IFilter<T> negate() {
                return IFilter.this;
            }
        };
    }

    /**
     * Default priority logic for all whitelist filters.
     * <p>
     * Whitelist filters have {@code Integer.MAX_VALUE - whitelistSize} as their default priority. The highest possible
     * number for whitelist priority is {@code Integer.MAX_VALUE - 1}, where only one entry is whitelisted.
     *
     * @param whitelistSize the size of whitelist entries
     * @return default priority logic for all whitelist filters
     */
    static int whitelistPriority(int whitelistSize) {
        return Integer.MAX_VALUE - whitelistSize;
    }

    /**
     * Default priority logic for all blacklist filters.
     * <p>
     * Blacklist filters have {@code Integer.MIN_VALUE + 1 + blacklistSize} as their default priority. The lowest
     * possible number for blacklist priority is {@code Integer.MIN_VALUE + 2}, where only one entry is blacklisted.
     *
     * @param blacklistSize the size of whitelist entries
     * @return default priority logic for all blacklist filters
     */
    static int blacklistPriority(int blacklistSize) {
        return Integer.MIN_VALUE + 1 + blacklistSize;
    }

    /**
     * Recommended priority for 'whitelist-like' filters; can be adjusted.
     *
     * @return recommended priority for 'whitelist-like' filters
     */
    static int whitelistLikePriority() {
        return 1000;
    }

    /**
     * Recommended priority for 'blacklist-like' filters; can be adjusted.
     *
     * @return recommended priority for 'blacklist-like' filters
     */
    static int blacklistLikePriority() {
        return -1000;
    }

    /**
     * Highest possible priority for filters.
     *
     * @return highest possible priority
     */
    static int firstPriority() {
        return Integer.MAX_VALUE;
    }

    /**
     * Lowest possible priority for filters.
     *
     * @return lowest possible priority
     */
    static int lastPriority() {
        return Integer.MIN_VALUE + 1;
    }

    /**
     * Special priority for 'no-priority' filters; applicable to no-op filters and its reverse (everything filter).
     *
     * @return special priority for 'no-priority' filters
     */
    static int noPriority() {
        return Integer.MIN_VALUE;
    }
}
