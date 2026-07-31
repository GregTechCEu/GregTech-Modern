package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.behavior.TurbineRotorBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class LargeSteamTurbineTest {

    @TestHolder()
    @GameTest(template = "lst_with_steam", setupTicks = 2, timeoutTicks = 20 + 40 + 1)
    public static void LstActivatesTest(GameTestHelper helper) {
        {
            final var block = (PipeBlockEntity) helper.getBlockEntity(new BlockPos(4, 3, 3));
            block.setConnection(Direction.SOUTH, true, false);
            final var tank = (CreativeTankMachine) helper.getBlockEntity(new BlockPos(4, 3, 4));
            tank.setWorkingEnabled(true);
        }
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        final var dynamo = (EnergyHatchPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 1));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(machine.getRecipeLogic().isWorking(), "Expected turbine to be running");
            rotorPart.setRotorSpeed(3000);
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(dynamo.energyContainer.getEnergyStored() > 0,
                        "Expected some energy to be in the dynamo hatch");
                helper.succeed();
            });
        });
    }

    @TestHolder()
    @GameTest(template = "lst_with_steam", setupTicks = 2, timeoutTicks = 20 + 40 + 1)
    public static void LstStopsOnBrokenRotorTest(GameTestHelper helper) {
        {
            final var block = (PipeBlockEntity) helper.getBlockEntity(new BlockPos(4, 3, 3));
            block.setConnection(Direction.SOUTH, true, false);
            final var tank = (CreativeTankMachine) helper.getBlockEntity(new BlockPos(4, 3, 4));
            tank.setWorkingEnabled(true);
        }
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        final var dynamo = (EnergyHatchPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 1));
        helper.runAfterDelay(20, () -> {
            {
                final var rotor = rotorPart.inventory.storage.getStackInSlot(0);
                final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
                behavior.setPartDamage(rotor, behavior.getPartMaxDurability(rotor) - 1);
            }
            helper.assertTrue(machine.getRecipeLogic().isWorking(), "Expected turbine to be running");
            rotorPart.setRotorSpeed(0);
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(!machine.getRecipeLogic().isWorking(),
                        "Expected turbine to be not running after the rotor broke");
                helper.succeed();
            });
        });
    }

    @TestHolder()
    @GameTest(template = "lst_infinite_steam", setupTicks = 2, timeoutTicks = 2)
    public static void LstGeneratesCorrectPowerTest(GameTestHelper helper) {
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        helper.runAfterDelay(1, () -> {
            rotorPart.setRotorSpeed(rotorPart.getMaxRotorHolderSpeed());
            final var recipe = (GTRecipe) GTCEu.getMinecraftServer().getRecipeManager()
                    .byKey(GTCEu.id("steam_turbine/steam"))
                    .get()
                    .value();
            final var mod = LargeTurbineMachine.recipeModifier(machine, recipe);
            final var res = mod.apply(recipe);
            final var totalEu = res.getOutputEUt().getTotalEU();
            final var expectedEu = (2 * GTValues.V[GTValues.HV]);
            helper.assertTrue(totalEu == expectedEu,
                    String.format("Expected turbine to get correct V value, got %d, wanted %d", totalEu, expectedEu));
            rotorPart.setRotorSpeed(0);
            helper.succeed();
        });
    }

    @TestHolder()
    @GameTest(template = "lst_infinite_steam", setupTicks = 2, timeoutTicks = 101)
    public static void LstRotorSpeedIncreasesAndDecreasesAsExpectedTest(GameTestHelper helper) {
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        machine.setWorkingEnabled(true);
        rotorPart.setRotorSpeed(0);
        machine.recipeLogic.findAndHandleRecipe();
        final AtomicInteger lastSpeed = new AtomicInteger(0);
        final AtomicBoolean wasWorking = new AtomicBoolean(false);
        final AtomicBoolean hasSample = new AtomicBoolean(false);
        final AtomicInteger increasedTicks = new AtomicInteger(0);
        final AtomicInteger decreasedTicks = new AtomicInteger(0);
        final int stopTick = 17 * 5 - 1;
        helper.onEachTick(() -> {
            if (helper.getTick() == stopTick) {
                machine.setWorkingEnabled(false);
            }

            final boolean working = machine.getRecipeLogic().isWorking();
            final int speed = rotorPart.getRotorSpeed();
            final int last = lastSpeed.getAndSet(speed);
            final boolean previouslyWorking = wasWorking.getAndSet(working);
            if (!hasSample.getAndSet(true)) return;
            if (working != previouslyWorking) return;
            if (!working && last <= 0) return;

            if (working) {
                helper.assertTrue(speed == last + RotorHolderPartMachine.SPEED_INCREMENT,
                        String.format("Expected speed to increase by %d, got %d",
                                RotorHolderPartMachine.SPEED_INCREMENT, speed - last));
                increasedTicks.incrementAndGet();
            } else {
                helper.assertTrue(speed == last - RotorHolderPartMachine.SPEED_DECREMENT,
                        String.format("Expected speed to decrease by %d, got %d",
                                RotorHolderPartMachine.SPEED_DECREMENT, last - speed));
                decreasedTicks.incrementAndGet();
            }
        });
        helper.runAtTickTime(100, () -> {
            helper.assertTrue(increasedTicks.get() > 0, "Expected the rotor to have spun up at all");
            helper.assertTrue(decreasedTicks.get() > 0, "Expected the rotor to have spun down at all");
            helper.succeed();
        });
    }

    @TestHolder()
    @GameTest(template = "lst_infinite_steam_with_output", setupTicks = 2, timeoutTicks = 101)
    public static void LstOutputsCorrectAmountOfDistilledWaterTest(GameTestHelper helper) {
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        machine.setWorkingEnabled(true);
        rotorPart.setRotorSpeed(0);
        machine.recipeLogic.findAndHandleRecipe();
        // It should've ran 5 times
        helper.runAtTickTime(17 * 5 - 1, () -> {
            machine.setWorkingEnabled(false);
        });
        helper.runAtTickTime(17 * 5, () -> {
            final var outputHatch = (FluidHatchPartMachine) helper.getBlockEntity(new BlockPos(2, 4, 2));
            helper.assertTrue(outputHatch.tank.getFluidInTank(0).getAmount() == 128 * 5,
                    "Should have exactly 128 * 5 distilled water");
            helper.succeed();
        });
    }

    @TestHolder()
    @GameTest(template = "lst_with_steam", setupTicks = 2, timeoutTicks = 20 + 40 + 1)
    public static void LstRunsWithFullDynamoTest(GameTestHelper helper) {
        {
            final var block = (PipeBlockEntity) helper.getBlockEntity(new BlockPos(4, 3, 3));
            block.setConnection(Direction.SOUTH, true, false);
            final var tank = (CreativeTankMachine) helper.getBlockEntity(new BlockPos(4, 3, 4));
            tank.setWorkingEnabled(true);
        }
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        final var dynamo = (EnergyHatchPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 1));
        dynamo.energyContainer.setEnergyStored(dynamo.energyContainer.getEnergyCapacity());
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(machine.getRecipeLogic().isWorking(), "Expected turbine to be running");
            rotorPart.setRotorSpeed(3000);
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(
                        dynamo.energyContainer.getEnergyStored() == dynamo.energyContainer.getEnergyCapacity(),
                        "Expected some energy to be in the dynamo hatch");
                helper.succeed();
            });
        });
    }

    @TestHolder()
    @GameTest(template = "lst_infinite_steam_nomaintenance", setupTicks = 2, timeoutTicks = 50 + 1)
    public static void LstRefusesToRunWithoutMaintenanceTest(GameTestHelper helper) {
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        final var dynamo = (EnergyHatchPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 1));
        helper.onEachTick(() -> {
            helper.assertTrue(!machine.getRecipeLogic().isWorking(), "Expected turbine to refuse to run");
            rotorPart.setRotorSpeed(3000);
        });
        helper.runAtTickTime(50, () -> {
            helper.assertTrue(dynamo.energyContainer.getEnergyStored() == 0,
                    "Expected no energy to have been produced");
            helper.succeed();
        });
    }

    @TestHolder()
    @GameTest(template = "lst_with_lava", setupTicks = 2, timeoutTicks = 50 + 1)
    public static void LstRefusesToRunOnWrongFluidTest(GameTestHelper helper) {
        final var rotorPart = (RotorHolderPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 4));
        {
            final var rotor = GTItems.TURBINE_ROTOR.asStack();
            final var behavior = TurbineRotorBehaviour.getBehaviour(rotor);
            behavior.setPartMaterial(rotor, GTMaterials.RoseGold);
            rotorPart.inventory.storage.setStackInSlot(0, rotor);
        }
        final var machine = (LargeTurbineMachine) helper.getBlockEntity(new BlockPos(1, 3, 2));
        final var dynamo = (EnergyHatchPartMachine) helper.getBlockEntity(new BlockPos(2, 3, 1));
        helper.onEachTick(() -> {
            helper.assertTrue(!machine.getRecipeLogic().isWorking(), "Expected turbine to refuse to run");
            rotorPart.setRotorSpeed(3000);
        });
        helper.runAtTickTime(50, () -> {
            helper.assertTrue(dynamo.energyContainer.getEnergyStored() == 0,
                    "Expected no energy to have been produced");
            helper.succeed();
        });
    }
}
