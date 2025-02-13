package com.gregtechceu.gtceu.test;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.test.api.machine.trait.RecipeLogicTest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;

import com.mojang.datafixers.util.Pair;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

public class GTGameTests {

    private static final Class<?>[] testHolders = {
            RecipeLogicTest.class,
    };

    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        return getTestsFrom(testHolders);
    }

    public static Collection<TestFunction> getTestsFrom(Class<?>... classes) {
        return Stream.of(classes)
                .map(Class::getDeclaredMethods)
                .flatMap(Stream::of)
                .filter(method -> !method.isSynthetic() && method.getAnnotation(GameTest.class) != null)
                .map(method -> Pair.of(method, method.getAnnotation(GameTest.class)))
                .map(method -> new TestFunction(
                        "gtceu",
                        GTCEu.MOD_ID + "." + method.getFirst().getDeclaringClass().getSimpleName() + "." +
                                method.getFirst().getName(),
                        method.getSecond().template(),
                        Rotation.NONE,
                        method.getSecond().timeoutTicks(),
                        method.getSecond().setupTicks(),
                        method.getSecond().required(),
                        method.getSecond().requiredSuccesses(),
                        method.getSecond().attempts(),
                        gameTestHelper -> {
                            try {
                                Object object = null;
                                if (!Modifier.isStatic(method.getFirst().getModifiers())) {
                                    object = method.getFirst().getDeclaringClass().getConstructor().newInstance();
                                }
                                method.getFirst().invoke(object, gameTestHelper);
                            } catch (ReflectiveOperationException e) {
                                throw new RuntimeException(e);
                            }
                        }))
                .sorted(Comparator.comparing(TestFunction::getTestName))
                .toList();
    }
}
