package com.gregtechceu.gtceu.utils.memoization.function;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.function.BiFunction;

public interface MemoizedBiFunction<T, U, R> extends BiFunction<T, U, R> {

    Map<Pair<T, U>, R> getCache();
}
