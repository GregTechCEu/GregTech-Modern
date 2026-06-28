package com.gregtechceu.gtceu.integration.kjs.helpers;

import com.gregtechceu.gtceu.core.mixins.kjs.RegistryInfoMixin;

/**
 * A KJS builder that wraps an object whose registry is handled by GT.
 * The standard KJS dummy builder does not work for this because its registration function is never called,
 * while GT still needs these builders to be registered at the correct time, but without kjs registering the object.
 * This logic is handled by {@link RegistryInfoMixin}
 */
public interface IGTDummyBuilder<T> {

    T createObject();
}
