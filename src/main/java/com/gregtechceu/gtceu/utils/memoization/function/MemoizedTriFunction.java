package com.gregtechceu.gtceu.utils.memoization.function;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;

public interface MemoizedTriFunction<T, U, V, R> extends TriFunction<T, U, V, R> {

    Map<Triple<T, U, V>, R> getCache();
}
