package com.gregtechceu.gtceu.api.gametest;

import net.neoforged.bus.api.IEventBus;

import org.jetbrains.annotations.ApiStatus;

/**
 * Service-loadable bridge that lets {@code src/main} initialise GameTest
 * registration that lives in {@code src/test} without holding a compile-time
 * reference to the test source set. The {@code gameTestServer} /
 * {@code gameTestClient} run configs put the test source set on the launch
 * classpath; production runs do not, so {@link java.util.ServiceLoader}
 * naturally finds zero implementations off-test and skips the bootstrap.
 */
@ApiStatus.Internal
public interface IGTGameTestBootstrap {

    void init(IEventBus modBus);
}
