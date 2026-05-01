package com.gregtechceu.gtceu.gametest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gametest.IGTGameTestBootstrap;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogicTest;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializerTest;
import com.gregtechceu.gtceu.api.recipe.InputSeparationTest;
import com.gregtechceu.gtceu.api.recipe.MultipleEnergyHatchTest;
import com.gregtechceu.gtceu.api.recipe.OverclockLogicTest;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredientTest;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredientTest;
import com.gregtechceu.gtceu.api.recipe.lookup.GTRecipeLookupTest;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item.NBTItemStackMapIngredientLookupTest;
import com.gregtechceu.gtceu.common.cover.AdvancedDetectorCoverTest;
import com.gregtechceu.gtceu.common.cover.ConveyorCoverTest;
import com.gregtechceu.gtceu.common.cover.DetectorCoverTest;
import com.gregtechceu.gtceu.common.cover.EnderCoversTest;
import com.gregtechceu.gtceu.common.cover.ItemFilterCoverTest;
import com.gregtechceu.gtceu.common.cover.MonitorCoverTest;
import com.gregtechceu.gtceu.common.cover.PumpCoverTest;
import com.gregtechceu.gtceu.common.cover.RobotArmTest;
import com.gregtechceu.gtceu.common.cover.ShutterCoverTest;
import com.gregtechceu.gtceu.common.cover.WirelessTransmitterCoverTest;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.AssemblyLineTests;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachineTest;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidConditionTest;
import com.gregtechceu.gtceu.gametest.annotation.AfterBatch;
import com.gregtechceu.gtceu.gametest.annotation.BeforeBatch;
import com.gregtechceu.gtceu.gametest.annotation.GameTest;
import com.gregtechceu.gtceu.gametest.annotation.GameTestHolder;
import com.gregtechceu.gtceu.gametest.annotation.PrefixGameTestTemplate;
import com.gregtechceu.gtceu.gametest.stresstest.RecipeIteratorStressTest;
import com.gregtechceu.gtceu.gametest.world.RealWorldItemUsage;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import com.mojang.serialization.MapCodec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GTGameTestBootstrap implements IGTGameTestBootstrap {

    private static final List<Class<?>> TEST_CLASSES = List.of(
            RealWorldItemUsage.class,
            RecipeIteratorStressTest.class,
            AdjacentFluidConditionTest.class,
            OverclockLogicTest.class,
            MultipleEnergyHatchTest.class,
            ConveyorCoverTest.class,
            AdvancedDetectorCoverTest.class,
            EnderCoversTest.class,
            ItemBusPartMachineTest.class,
            DetectorCoverTest.class,
            MonitorCoverTest.class,
            WirelessTransmitterCoverTest.class,
            ShutterCoverTest.class,
            PumpCoverTest.class,
            RobotArmTest.class,
            ItemFilterCoverTest.class,
            InputSeparationTest.class,
            GTRecipeLookupTest.class,
            GTRecipeSerializerTest.class,
            AssemblyLineTests.class,
            RecipeLogicTest.class,
            NBTItemStackMapIngredientLookupTest.class,
            IntProviderIngredientTest.class,
            IntProviderFluidIngredientTest.class);

    private static boolean initialized;

    /** Public no-arg constructor for {@link java.util.ServiceLoader}. */
    public GTGameTestBootstrap() {}

    @Override
    public void init(IEventBus modBus) {
        if (initialized) {
            return;
        }
        initialized = true;
        modBus.addListener(GTGameTestBootstrap::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        List<TestMethod> tests = new ArrayList<>();
        Map<String, List<Method>> beforeBatch = new HashMap<>();
        Map<String, List<Method>> afterBatch = new HashMap<>();

        for (Class<?> testClass : testClasses()) {
            GameTestHolder holder = testClass.getAnnotation(GameTestHolder.class);
            if (holder == null) {
                continue;
            }
            for (Method method : testClass.getDeclaredMethods()) {
                BeforeBatch before = method.getAnnotation(BeforeBatch.class);
                if (before != null) {
                    beforeBatch.computeIfAbsent(before.batch(), ignored -> new ArrayList<>()).add(method);
                }
                AfterBatch after = method.getAnnotation(AfterBatch.class);
                if (after != null) {
                    afterBatch.computeIfAbsent(after.batch(), ignored -> new ArrayList<>()).add(method);
                }
                GameTest test = method.getAnnotation(GameTest.class);
                if (test != null) {
                    tests.add(new TestMethod(testClass, holder, method, test));
                }
            }
        }

        beforeBatch.values().forEach(GTGameTestBootstrap::sortMethods);
        afterBatch.values().forEach(GTGameTestBootstrap::sortMethods);
        tests.sort(Comparator.comparing(test -> test.testClass().getName() + "#" + test.method().getName()));

        Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new HashMap<>();
        for (TestMethod test : tests) {
            Holder<TestEnvironmentDefinition<?>> environment = environments.computeIfAbsent(test.test().batch(),
                    batch -> event.registerEnvironment(environmentId(batch),
                            new BatchEnvironment(beforeBatch.getOrDefault(batch, List.of()),
                                    afterBatch.getOrDefault(batch, List.of()))));
            event.registerTest(testId(test), new ReflectiveGameTestInstance(test, testData(environment, test)));
        }

        GTCEu.LOGGER.info("Registered {} GTCEu GameTests for NeoForge 26.1.2", tests.size());
    }

    private static List<Class<?>> testClasses() {
        List<Class<?>> testClasses = new ArrayList<>(TEST_CLASSES);
        addOptionalTest(testClasses, GTCEu.Mods.isAE2Loaded(),
                "com.gregtechceu.gtceu.integration.ae2.machine.PatternBufferTest");
        return testClasses;
    }

    private static void addOptionalTest(List<Class<?>> testClasses, boolean enabled, String className) {
        if (!enabled) {
            return;
        }
        try {
            testClasses.add(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Optional GameTest class is missing: " + className, e);
        }
    }

    private static void sortMethods(List<Method> methods) {
        methods.sort(Comparator.comparing(method -> method.getDeclaringClass().getName() + "#" + method.getName()));
    }

    private static Identifier testId(TestMethod test) {
        return GTCEu.id("gametest/" + sanitize(test.testClass().getSimpleName()) + "/" +
                sanitize(test.method().getName()));
    }

    private static Identifier environmentId(String batch) {
        return GTCEu.id("gametest_environment/" + sanitize(batch));
    }

    private static TestData<Holder<TestEnvironmentDefinition<?>>> testData(
                                                                           Holder<TestEnvironmentDefinition<?>> environment,
                                                                           TestMethod test) {
        GameTest annotation = test.test();
        return new TestData<>(
                environment,
                structureId(test),
                toTicks(annotation.timeoutTicks(), "timeoutTicks", test.method()),
                toTicks(annotation.setupTicks(), "setupTicks", test.method()),
                annotation.required(),
                rotation(annotation.rotationSteps()),
                annotation.manualOnly(),
                Math.max(1, annotation.attempts()),
                Math.max(1, annotation.requiredSuccesses()),
                annotation.skyAccess(),
                0);
    }

    private static Identifier structureId(TestMethod test) {
        String namespace = test.test().templateNamespace().isBlank() ?
                test.holder().value() : test.test().templateNamespace();
        String template = test.test().template().isBlank() ?
                test.method().getName().toLowerCase(Locale.ROOT) : test.test().template();
        if (prefixTemplate(test)) {
            template = test.testClass().getSimpleName().toLowerCase(Locale.ROOT) + "." + template;
        }
        return Identifier.fromNamespaceAndPath(namespace, template);
    }

    private static boolean prefixTemplate(TestMethod test) {
        PrefixGameTestTemplate methodPrefix = test.method().getAnnotation(PrefixGameTestTemplate.class);
        if (methodPrefix != null) {
            return methodPrefix.value();
        }
        PrefixGameTestTemplate classPrefix = test.testClass().getAnnotation(PrefixGameTestTemplate.class);
        return classPrefix == null || classPrefix.value();
    }

    private static int toTicks(long ticks, String name, Method method) {
        if (ticks > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(name + " is too large on " + method);
        }
        return (int) ticks;
    }

    private static Rotation rotation(int steps) {
        return switch (Math.floorMod(steps, 4)) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static String sanitize(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9/._-]", "_");
    }

    private static void invoke(Method method, Object... args) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("GameTest method must be static: " + method);
        }
        try {
            method.setAccessible(true);
            method.invoke(null, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access GameTest method " + method, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(cause);
        }
    }

    private record TestMethod(Class<?> testClass, GameTestHolder holder, Method method, GameTest test) {}

    private record BatchEnvironment(List<Method> beforeBatch, List<Method> afterBatch)
            implements TestEnvironmentDefinition<Void> {

        @Override
        public Void setup(ServerLevel level) {
            beforeBatch.forEach(method -> invokeBatchMethod(method, level));
            return null;
        }

        @Override
        public void teardown(ServerLevel level, Void savedData) {
            afterBatch.forEach(method -> invokeBatchMethod(method, level));
        }

        @Override
        public MapCodec<? extends TestEnvironmentDefinition<Void>> codec() {
            return MapCodec.unit(this);
        }

        private static void invokeBatchMethod(Method method, ServerLevel level) {
            if (method.getParameterCount() == 0) {
                invoke(method);
                return;
            }
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(ServerLevel.class)) {
                invoke(method, level);
                return;
            }
            throw new IllegalArgumentException("Unsupported batch method signature: " + method);
        }
    }

    private static final class ReflectiveGameTestInstance extends GameTestInstance {

        private final TestMethod test;

        private ReflectiveGameTestInstance(TestMethod test, TestData<Holder<TestEnvironmentDefinition<?>>> data) {
            super(data);
            this.test = test;
        }

        @Override
        public void run(GameTestHelper helper) {
            Method method = test.method();
            if (method.getParameterCount() == 0) {
                invoke(method);
                return;
            }
            if (method.getParameterCount() == 1) {
                Class<?> helperType = method.getParameterTypes()[0];
                if (helperType.isAssignableFrom(GameTestHelper.class)) {
                    invoke(method, new LegacyGameTestHelper(helper.testInfo));
                    return;
                }
                if (helperType.isAssignableFrom(ExtendedGameTestHelper.class)) {
                    invoke(method, new LegacyExtendedGameTestHelper(helper.testInfo));
                    return;
                }
            }
            throw helper.assertionException("Unsupported GameTest method signature: %s", method);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return MapCodec.unit(this);
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal(test.testClass().getSimpleName() + "#" + test.method().getName());
        }
    }
}
