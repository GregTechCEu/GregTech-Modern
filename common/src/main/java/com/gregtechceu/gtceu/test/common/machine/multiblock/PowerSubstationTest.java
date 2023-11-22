/*
package com.gregtechceu.gtceu.test.common.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PowerSubstationTest {

    @GameTest(template = "gtceu:pss")
    public void Test_1_Slot(GameTestHelper helper) {
        BlockEntity be = helper.getBlockEntity(new BlockPos(2, 2, 0));
        if (!(be instanceof IMachineBlockEntity mbe)) {
            helper.fail("wrong block at pos [2, 2, 0]! (not a machine block entity)");
            return;
        }
        if (!(mbe.getMetaMachine() instanceof PowerSubstationMachine pss)) {
            helper.fail("wrong machine at pos [2, 2, 0]! (not a Power Substation)");
            return;
        }

        PowerSubstationMachine.PowerStationEnergyBank storage = createStorage(pss, 100);
        helper.assertTrue(storage.getCapacity().equals(BigInteger.valueOf(100)), "Wrong max storage! was" + storage.getCapacity() + ", expected 100");

        // Random fill and drain tests
        long filled = storage.fill(50);
        helper.assertTrue(filled == 50, "Expected `fill` to return 50, was" + filled);
        helper.assertTrue(storage.getStored().equals(BigInteger.valueOf(50)), "Expected stored energy amount to be 50, was" + storage.getStored());
        filled = storage.fill(100);
        helper.assertTrue(filled == 50, "Expected `fill` to return 50, was" + filled);
        helper.assertTrue(storage.getStored().equals(BigInteger.valueOf(100)), "Expected stored energy amount to be 100, was" + storage.getStored());
        filled = storage.fill(100);
        helper.assertTrue(filled == 0, "Expected `fill` to return 0, was" + filled);
        helper.assertTrue(storage.getStored().equals(BigInteger.valueOf(100)), "Expected stored energy amount to be 100, was" + storage.getStored());
        MatcherAssert.assertThat(storage.drain(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(50));
        MatcherAssert.assertThat(storage.drain(100), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Fully fill and drain
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));

        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Try to overfill and overdrain
        MatcherAssert.assertThat(storage.fill(1000), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));

        MatcherAssert.assertThat(storage.drain(1000), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
    }

    @Test
    public void Test_4_Slot_Equal_Sizes() {
        PowerStationEnergyBank storage = createStorage(100, 100, 100, 100);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(400));

        // No overlap of slots
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Overlap slots
        MatcherAssert.assertThat(storage.fill(150), is(150L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(150));
        MatcherAssert.assertThat(storage.fill(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.fill(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(150), is(150L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(250));
        MatcherAssert.assertThat(storage.drain(50), is(50L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.drain(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Fully fill and drain
        MatcherAssert.assertThat(storage.fill(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));
        MatcherAssert.assertThat(storage.fill(400), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(400), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Try to overfill and overdrain
        MatcherAssert.assertThat(storage.fill(1000), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(400));

        MatcherAssert.assertThat(storage.drain(1000), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
    }

    @Test
    public void Test_4_Slot_Different_Sizes() {
        PowerStationEnergyBank storage = createStorage(100, 200, 300, 400);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(1000));

        // No overlap of slots
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.fill(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.fill(300), is(300L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(600));
        MatcherAssert.assertThat(storage.fill(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(400), is(400L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(600));
        MatcherAssert.assertThat(storage.drain(300), is(300L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.drain(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(100));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Overlap slots
        MatcherAssert.assertThat(storage.fill(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.fill(600), is(600L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(900));
        MatcherAssert.assertThat(storage.fill(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));
        MatcherAssert.assertThat(storage.fill(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(900));
        MatcherAssert.assertThat(storage.drain(600), is(600L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(300));
        MatcherAssert.assertThat(storage.drain(100), is(100L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(200));
        MatcherAssert.assertThat(storage.drain(200), is(200L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(100), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Fully fill and drain
        MatcherAssert.assertThat(storage.fill(1000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));
        MatcherAssert.assertThat(storage.fill(1000), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(1000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
        MatcherAssert.assertThat(storage.drain(1000), is(0L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Try to overfill and overdrain
        MatcherAssert.assertThat(storage.fill(10000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1000));

        MatcherAssert.assertThat(storage.drain(10000), is(1000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));
    }

    @SuppressWarnings("NumericOverflow")
    @Test
    public void Test_Over_Long() {
        PowerStationEnergyBank storage = createStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE));

        long halfLong = Long.MAX_VALUE / 2;

        MatcherAssert.assertThat(storage.fill(halfLong), is(halfLong));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(halfLong));
        MatcherAssert.assertThat(storage.fill(Long.MAX_VALUE), is(Long.MAX_VALUE));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(halfLong, Long.MAX_VALUE));

        MatcherAssert.assertThat(storage.drain(halfLong), is(halfLong));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(Long.MAX_VALUE));
        MatcherAssert.assertThat(storage.drain(Long.MAX_VALUE), is(Long.MAX_VALUE));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(0));

        // Test overflow
        Assertions.assertThrows(IllegalArgumentException.class, () -> storage.fill(Long.MAX_VALUE + 1000));
        Assertions.assertThrows(IllegalArgumentException.class, () -> storage.drain(Long.MAX_VALUE + 1000));
    }

    @Test
    public void Test_Rebuild_Storage() {
        PowerStationEnergyBank storage = createStorage(100, 500, 4000);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(4600));

        // Set up the storage with some amount of energy
        MatcherAssert.assertThat(storage.fill(3000), is(3000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(3000));

        // Rebuild with more storage than needed
        storage = rebuildStorage(storage, 1000, 4000, 4000);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(9000));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(3000));

        // Reset
        storage = createStorage(100, 500, 4000);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(4600));

        // Set up storage with energy again
        MatcherAssert.assertThat(storage.fill(3000), is(3000L));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(3000));

        // Rebuild with less storage than needed
        storage = rebuildStorage(storage, 100, 100, 400, 500);
        MatcherAssert.assertThat(storage.getCapacity(), isBigInt(1100));
        MatcherAssert.assertThat(storage.getStored(), isBigInt(1100));
    }

    @Test
    public void Test_Optimized_Big_Integer_Summarize() {
        Consumer<Random> testRunner = r -> {
            BigInteger summation = BigInteger.ZERO;
            long[] storageValues = new long[9 * MAX_BATTERY_LAYERS];
            for (int i = 0; i < storageValues.length; i++) {
                long randomLong = Math.abs(r.nextLong());
                storageValues[i] = randomLong;
                summation = summation.add(BigInteger.valueOf(randomLong));
            }

            PowerStationEnergyBank storage = createStorage(storageValues);
            MatcherAssert.assertThat(storage.getCapacity(), is(summation));
        };

        for (int i = 0; i < 100; i++) {
            testRunner.accept(new Random());
        }
    }

    @Test
    public void Test_Passive_Drain_Calculation() {
        // 100kEU/t per storage block "too large" (like max long)
        PowerStationEnergyBank storage = createStorage(Long.MAX_VALUE, Long.MAX_VALUE);
        MatcherAssert.assertThat(storage.getPassiveDrainPerTick(),
                is(2 * PASSIVE_DRAIN_MAX_PER_STORAGE));

        Consumer<Random> testRunner = r -> {
            int numTruncated = 0;
            BigInteger nonTruncated = BigInteger.ZERO;

            long[] storageValues = new long[9 * MAX_BATTERY_LAYERS];
            for (int i = 0; i < storageValues.length; i++) {
                long randomLong = Math.abs(r.nextLong());
                storageValues[i] = randomLong;
                if (randomLong / PASSIVE_DRAIN_DIVISOR >= PASSIVE_DRAIN_MAX_PER_STORAGE) {
                    numTruncated++;
                } else {
                    nonTruncated = nonTruncated.add(BigInteger.valueOf(randomLong));
                }
            }

            PowerStationEnergyBank testStorage = createStorage(storageValues);
            MatcherAssert.assertThat(testStorage.getPassiveDrainPerTick(),
                    is(nonTruncated.divide(BigInteger.valueOf(PASSIVE_DRAIN_DIVISOR))
                            .add(BigInteger.valueOf(numTruncated * PASSIVE_DRAIN_MAX_PER_STORAGE))
                            .longValue()));
        };

        for (int i = 0; i < 100; i++) {
            testRunner.accept(new Random());
        }
    }

    @Test
    public void Test_Fill_Drain_Randomized() {
        Consumer<Random> testRunner = r -> {
            BigInteger totalStorage = BigInteger.ZERO;
            long[] storageValues = new long[9 * MAX_BATTERY_LAYERS];
            for (int i = 0; i < storageValues.length; i++) {
                long randomLong = Math.abs(r.nextLong());
                storageValues[i] = randomLong;
                totalStorage = totalStorage.add(BigInteger.valueOf(randomLong));
            }

            PowerStationEnergyBank storage = createStorage(storageValues);

            // test capacity
            MatcherAssert.assertThat(storage.getCapacity(), is(totalStorage));

            // test fill
            BigInteger amountToFill = totalStorage;
            do {
                long randomLong = Math.abs(r.nextLong());
                BigInteger randomBigInt = BigInteger.valueOf(randomLong);

                if (amountToFill.compareTo(randomBigInt) <= 0) {
                    MatcherAssert.assertThat(storage.fill(randomLong), is(amountToFill.longValue()));
                    amountToFill = BigInteger.ZERO;
                } else {
                    MatcherAssert.assertThat(storage.fill(randomLong), is(randomLong));
                    amountToFill = amountToFill.subtract(randomBigInt);
                }
            } while (!amountToFill.equals(BigInteger.ZERO));

            // test drain
            BigInteger amountToDrain = totalStorage;
            do {
                long randomLong = Math.abs(r.nextLong());
                BigInteger randomBigInt = BigInteger.valueOf(randomLong);

                if (amountToDrain.compareTo(randomBigInt) <= 0) {
                    MatcherAssert.assertThat(storage.drain(randomLong), is(amountToDrain.longValue()));
                    amountToDrain = BigInteger.ZERO;
                } else {
                    MatcherAssert.assertThat(storage.drain(randomLong), is(randomLong));
                    amountToDrain = amountToDrain.subtract(randomBigInt);
                }
            } while (!amountToDrain.equals(BigInteger.ZERO));
        };

        for (int i = 0; i < 100; i++) {
            testRunner.accept(new Random());
        }
    }

    private static Matcher<BigInteger> isBigInt(long value, long... additional) {
        BigInteger retVal = BigInteger.valueOf(value);
        if (additional != null) {
            for (long l : additional) {
                retVal = retVal.add(BigInteger.valueOf(l));
            }
        }
        return is(retVal);
    }

    private static PowerSubstationMachine.PowerStationEnergyBank createStorage(MetaMachine machine, long... storageValues) {
        List<IBatteryData> batteries = new ArrayList<>();
        for (long value : storageValues) {
            batteries.add(new TestBattery(value));
        }
        return new PowerSubstationMachine.PowerStationEnergyBank(machine, batteries);
    }

    private static PowerSubstationMachine.PowerStationEnergyBank rebuildStorage(PowerSubstationMachine.PowerStationEnergyBank storage, long... storageValues) {
        List<IBatteryData> batteries = new ArrayList<>();
        for (long value : storageValues) {
            batteries.add(new TestBattery(value));
        }
        return storage.rebuild(batteries);
    }

    private static class TestBattery implements IBatteryData {

        private final long capacity;

        private TestBattery(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        // not used in this test
        @Override
        public int getTier() {
            return 0;
        }

        // not used in this test
        @NotNull
        @Override
        public String getBatteryName() {
            return "";
        }
    }
}
*/