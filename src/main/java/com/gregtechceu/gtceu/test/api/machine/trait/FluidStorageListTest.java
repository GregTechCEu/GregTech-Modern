package com.gregtechceu.gtceu.test.api.machine.trait;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.OverlayedFluidHandler;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.minecraft.world.level.material.Fluids.*;

public class FluidStorageListTest {

    @GameTest(template = "gtceu:fluid")
    public void testSimpleFills(GameTestHelper helper) {
        new FluidHandlerTester(false,
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 1000)
                        .expectContents(FluidStack.create(WATER, 1000));

        new FluidHandlerTester(false,
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 333))
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 666))
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 999))
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 1000));

        new FluidHandlerTester(false,
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 333), FluidStack.empty())
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 666), FluidStack.empty())
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 999), FluidStack.empty())
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 1000), FluidStack.empty());

        new FluidHandlerTester(true,
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 333), FluidStack.empty())
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 666), FluidStack.empty())
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 999), FluidStack.empty())
                        .fill(WATER, 333)
                        .expectContents(FluidStack.create(WATER, 1000), FluidStack.create(WATER, 332));

        new FluidHandlerTester(false,
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 1500)
                        .expectContents(FluidStack.create(WATER, 1000), FluidStack.empty());

        new FluidHandlerTester(true,
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 1500)
                        .expectContents(FluidStack.create(WATER, 1000), FluidStack.create(WATER, 500));

        helper.succeed();
    }

    @GameTest(template = "gtceu:fluid")
    public void testMultipleFluidFills(GameTestHelper helper) {
        new FluidHandlerTester(false,
                new FluidStorage(1000),
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(LAVA, 800),
                                FluidStack.empty())
                        .drain(WATER, 1000)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.create(LAVA, 800),
                                FluidStack.empty())
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.create(LAVA, 1000),
                                FluidStack.empty())
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.create(LAVA, 1000),
                                FluidStack.empty());

        new FluidHandlerTester(true,
                new FluidStorage(1000),
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 600),
                                FluidStack.create(LAVA, 800))
                        .drain(WATER, 1600)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.empty(),
                                FluidStack.create(LAVA, 800))
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.create(LAVA, 600),
                                FluidStack.empty(),
                                FluidStack.create(LAVA, 1000))
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.create(LAVA, 1000),
                                FluidStack.create(LAVA, 400),
                                FluidStack.create(LAVA, 1000))
                        .fill(WATER, 69420)
                        .expectContents(
                                FluidStack.create(LAVA, 1000),
                                FluidStack.create(LAVA, 400),
                                FluidStack.create(LAVA, 1000));

        helper.succeed();
    }

    @GameTest(template = "gtceu:fluid")
    public void testMixedSameFluidFill(GameTestHelper helper) {
        new FluidHandlerTester(new FluidTransferList(
                new FluidTransferList(
                        new FluidStorage(1000),
                        new FluidStorage(1000)),
                new FluidStorage(1000),
                new FluidStorage(1000))) // distinct slots first
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.empty(),
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 400));

        new FluidHandlerTester(new FluidTransferList(
                new FluidTransferList(
                        new FluidStorage(1000),
                        new FluidStorage(1000)),
                new FluidStorage(1000),
                new FluidStorage(1000))) // non-distinct slots first
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 400),
                                FluidStack.empty());

        helper.succeed();
    }

    @GameTest(template = "gtceu:fluid")
    public void testDrain(GameTestHelper helper) {
        new FluidHandlerTester(true,
                new FluidStorage(1000),
                new FluidStorage(1000),
                new FluidStorage(1000))
                        .fill(WATER, 1500)
                        .fill(LAVA, 500)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 500),
                                FluidStack.create(LAVA, 500))
                        .drain(1000)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.create(WATER, 500),
                                FluidStack.create(LAVA, 500))
                        .drain(1000)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.empty(),
                                FluidStack.create(LAVA, 500))
                        .drain(1000)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.empty(),
                                FluidStack.empty())
                        .fill(LAVA, 500)
                        .fill(WATER, 1500)
                        .expectContents(
                                FluidStack.create(LAVA, 500),
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 500))
                        .drain(1000)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(WATER, 500))
                        .drain(500)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.create(WATER, 500),
                                FluidStack.create(WATER, 500))
                        .drain(1000)
                        .expectContents(
                                FluidStack.empty(),
                                FluidStack.empty(),
                                FluidStack.empty());

        helper.succeed();
    }

    @GameTest(template = "gtceu:fluid")
    public void testFilterOrdering(GameTestHelper helper) {
        Predicate<FluidStack> waterFilter = fluid -> fluid.getFluid() == WATER;
        Predicate<FluidStack> lavaFilter = fluid -> fluid.getFluid() == Fluids.LAVA;
        Predicate<FluidStack> creosoteFilter = fluid -> fluid.getFluid() == GTMaterials.Creosote.getFluid();

        new FluidHandlerTester(false,
                new FluidStorage(1000, waterFilter),
                new FluidStorage(1000, lavaFilter),
                new FluidStorage(1000, creosoteFilter))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(LAVA, 800),
                                FluidStack.empty());

        new FluidHandlerTester(true,
                new FluidStorage(1000, waterFilter),
                new FluidStorage(1000, lavaFilter),
                new FluidStorage(1000, creosoteFilter))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                FluidStack.create(WATER, 1000),
                                FluidStack.create(LAVA, 800),
                                FluidStack.empty());

        new FluidHandlerTester(true,
                new FluidStorage(1000, waterFilter),
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.create(WATER, 800),
                                FluidStack.create(LAVA, 800));

        new FluidHandlerTester(true,
                new FluidStorage(1000, waterFilter),
                new FluidStorage(1000))
                        .beginSimulation()
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                FluidStack.create(WATER, 800),
                                FluidStack.create(LAVA, 800));

        new FluidHandlerTester(true,
                new FluidStorage(1000),
                new FluidStorage(1000, waterFilter))
                        .beginSimulation()
                        .fill(WATER, 800)
                        .fill(LAVA, 800)
                        .expectContents(
                                FluidStack.create(LAVA, 800),
                                FluidStack.create(WATER, 800));

        new FluidHandlerTester(true,
                new FluidStorage(1000),
                new FluidStorage(1000, waterFilter))
                        .beginSimulation()
                        .fill(LAVA, 800)
                        .fill(WATER, 800)
                        .expectContents(
                                FluidStack.create(LAVA, 800),
                                FluidStack.create(WATER, 800));

        helper.succeed();
    }

    private static final class FluidHandlerTester {

        private final FluidTransferList tank;

        @Nullable
        private OverlayedFluidHandler overlayedFluidHandler;

        FluidHandlerTester(FluidTransferList tank) {
            this.tank = tank;
        }

        FluidHandlerTester(boolean allowSameFluidFill, IFluidTransfer... tanks) {
            this(new FluidTransferList(tanks));
        }

        FluidHandlerTester fill(Fluid fluid, int amount) {
            return fill(FluidStack.create(fluid, amount));
        }

        FluidHandlerTester fill(FluidStack fluidStack) {
            // make string representation before modifying the state, to produce better error message
            String tankString = this.tank.toString();

            long tankFillSim = this.tank.fill(fluidStack, true);

            if (this.overlayedFluidHandler != null) {
                String overlayString = this.overlayedFluidHandler.toString(true);
                long ofhSim = this.overlayedFluidHandler.insertFluid(fluidStack, fluidStack.getAmount());

                if (tankFillSim != ofhSim) {
                    throw new GameTestAssertException("Result of simulation fill from tank and OFH differ.\n" +
                            "Tank Simulation: " + tankFillSim + ", OFH simulation: " + ofhSim + "\n" +
                            "Tank: " + tankString + "\n" +
                            "OFH: " + overlayString);
                }
            }
            long actualFill = this.tank.fill(fluidStack, false);
            if (tankFillSim != actualFill) {
                throw new GameTestAssertException("Simulation fill to tank and actual fill differ.\n" +
                        "Simulated Fill: " + tankFillSim + ", Actual Fill: " + actualFill + "\n" +
                        "Tank: " + tankString);
            }
            return this;
        }

        FluidHandlerTester drain(Fluid fluid, int amount) {
            return drain(FluidStack.create(fluid, amount));
        }

        FluidHandlerTester drain(FluidStack fluidStack) {
            if (this.overlayedFluidHandler != null) {
                throw new GameTestAssertException("Cannot drain stuff in simulation");
            }
            // make string representation before modifying the state, to produce better error message
            String tankString = this.tank.toString();

            FluidStack drainSim = this.tank.drain(fluidStack, true);
            FluidStack actualDrain = this.tank.drain(fluidStack, false);

            if (!eq(drainSim, actualDrain)) {
                throw new GameTestAssertException("Simulation drain from tank and actual drain differ.\n" +
                        "Simulated Drain: " + ftos(drainSim) + ", Actual Drain: " + ftos(actualDrain) + "\n" +
                        "Tank: " + tankString);
            }
            return this;
        }

        FluidHandlerTester drain(int amount) {
            if (this.overlayedFluidHandler != null) {
                throw new GameTestAssertException("Cannot drain stuff in simulation");
            }
            // make string representation before modifying the state, to produce better error message
            String tankString = this.tank.toString();

            FluidStack drainSim = this.tank.drain(amount, true);
            FluidStack actualDrain = this.tank.drain(amount, false);

            if (!eq(drainSim, actualDrain)) {
                throw new GameTestAssertException("Simulation drain from tank and actual drain differ.\n" +
                        "Simulated Drain: " + ftos(drainSim) + ", Actual Drain: " + ftos(actualDrain) + "\n" +
                        "Tank: " + tankString);
            }
            return this;
        }

        FluidHandlerTester beginSimulation() {
            if (this.overlayedFluidHandler != null) {
                throw new IllegalStateException("Simulation already begun");
            }
            this.overlayedFluidHandler = new OverlayedFluidHandler(this.tank);
            return this;
        }

        FluidHandlerTester expectContents(@NotNull FluidStack... optionalFluidStacks) {
            if (optionalFluidStacks.length != this.tank.getTanks()) {
                throw new GameTestAssertException("Wrong number of fluids to compare; " +
                        "expected: " + this.tank.getTanks() + ", provided: " + optionalFluidStacks.length);
            }
            for (int i = 0; i < optionalFluidStacks.length; i++) {
                IFluidTransfer tank = this.tank.transfers[i];
                if (!eq(tank.getFluidInTank(0), optionalFluidStacks[i])) {
                    throw new GameTestAssertException("Contents of the tank don't match expected state.\n" +
                            "Expected: [\n  " + Arrays.stream(optionalFluidStacks)
                                    .map(FluidHandlerTester::ftos)
                                    .collect(Collectors.joining(",\n  ")) +
                            "\n]\n" +
                            "Tank: " + this.tank);
                }
            }
            return this;
        }

        static boolean eq(@Nullable FluidStack fluid1, @Nullable FluidStack fluid2) {
            if (fluid1.isEmpty() || fluid1.getAmount() <= 0) {
                return fluid2.isEmpty() || fluid2.getAmount() <= 0;
            } else {
                return fluid1.isFluidEqual(fluid2);
            }
        }

        static String ftos(@Nullable FluidStack fluid) {
            return fluid.isEmpty() ? "Empty" : BuiltInRegistries.FLUID.getKey(fluid.getFluid()) + " / " + fluid.getAmount();
        }
    }
}