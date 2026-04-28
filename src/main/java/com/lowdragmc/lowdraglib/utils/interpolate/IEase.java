package com.lowdragmc.lowdraglib.utils.interpolate;

@FunctionalInterface
public interface IEase {

    float apply(float progress);
}
