package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import net.minecraft.core.Direction;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

final class CoverBehaviourProviders {

    private CoverBehaviourProviders() {}

    static Supplier<CoverDefinition.CoverBehaviourProvider> of(String className) {
        return () -> reflect(className);
    }

    static Supplier<CoverDefinition.TieredCoverBehaviourProvider> tiered(String className) {
        return () -> (definition, coverable, side, tier) -> {
            try {
                var constructor = findConstructor(className, CoverDefinition.class, ICoverable.class,
                        Direction.class, int.class);
                return (CoverBehavior) constructor.newInstance(definition, coverable, side, tier);
            } catch (ReflectiveOperationException | LinkageError e) {
                GTCEu.LOGGER.error("Could not create tiered cover behavior {}", className, e);
                throw new IllegalStateException("Could not create tiered cover behavior " + className, e);
            }
        };
    }

    private static CoverDefinition.CoverBehaviourProvider reflect(String className) {
        return (definition, coverable, side) -> {
            try {
                var constructor = findConstructor(className, CoverDefinition.class, ICoverable.class, Direction.class);
                return (CoverBehavior) constructor.newInstance(definition, coverable, side);
            } catch (ReflectiveOperationException | LinkageError e) {
                GTCEu.LOGGER.error("Could not create cover behavior {}", className, e);
                throw new IllegalStateException("Could not create cover behavior " + className, e);
            }
        };
    }

    private static Constructor<?> findConstructor(String className, Class<?>... parameterTypes)
                                                                                                throws ReflectiveOperationException {
        var type = Class.forName(className, false, CoverBehaviourProviders.class.getClassLoader());
        var constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor;
    }
}
