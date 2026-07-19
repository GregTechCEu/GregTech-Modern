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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gregtechceu.gtceu.api.GTValues.HV;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class LargeSteamTurbine {

    @GameTest(template = "lst_with_steam", timeoutTicks = 20 + 40 + 1)
    public static void LstActivates(GameTestHelper helper) {
        {
            final var block = (PipeBlockEntity) helper.getBlockEntity(new BlockPos(4, 3, 3));
            block.setConnection(Direction.SOUTH, true, false);
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

    @GameTest(template = "lst_with_steam", timeoutTicks = 20 + 40 + 1)
    public static void LstStopsOnBrokenRotor(GameTestHelper helper) {
        final var block = (PipeBlockEntity) helper.getBlockEntity(new BlockPos(4, 3, 3));
        block.setConnection(Direction.SOUTH, true, false);
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
                behavior.setPartDamage(rotor, behavior.getMaxDurability(rotor) - 1);
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

    @GameTest(template = "lst_infinite_steam", timeoutTicks = 2)
    public static void LstGeneratesCorrectPower(GameTestHelper helper) {
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
                    .get();
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

    @GameTest(template = "lst_infinite_steam", timeoutTicks = 101)
    public static void LstRotorSpeedIncreasesAndDecreasesAsExpected(GameTestHelper helper) {
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
        final AtomicBoolean isIncreasing = new AtomicBoolean(true);
        final AtomicInteger skip = new AtomicInteger(1);
        helper.onEachTick(() -> {
            if (skip.getAndUpdate(value -> Math.max(0, value - 1)) != 0) {
                lastSpeed.set(rotorPart.getRotorSpeed());
                return;
            }
            var last = lastSpeed.get();
            var now = rotorPart.getRotorSpeed();
            if (isIncreasing.get())
                helper.assertTrue(now == last + 1,
                        String.format("Expected speed to increase by 1, got %d", now - last));
            else
                helper.assertTrue(now == last - 3,
                        String.format("Expected speed to decrease by 3, got %d", last - now));
            last = rotorPart.getRotorSpeed();
            lastSpeed.set(now);
        });
        helper.runAtTickTime(17 * 5 - 1, () -> {
            isIncreasing.set(false);
            machine.setWorkingEnabled(false);
            // REASON: I don't care to know why, I don't want to know why, and this fixes it.
            // Both the increment and decrement occur at once; so we need this to make the test pass '_'
            lastSpeed.addAndGet(1);
        });
        helper.runAtTickTime(100, () -> helper.succeed());
    }

    @GameTest(template = "lst_infinite_steam_with_output", timeoutTicks = 101)
    public static void LstOutputsCorrectAmountOfDistilledWater(GameTestHelper helper) {
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
            helper.assertTrue(outputHatch.tank.getFluidInTank(0).getAmount() == 128 * 5, "Should have exactly 128 * 5 distilled water");
            helper.succeed();
        });
    }

    @GameTest(template = "lst_with_steam", timeoutTicks = 20 + 40 + 1)
    public static void LstRunsWithFullDynamo(GameTestHelper helper) {
        {
            final var block = (PipeBlockEntity) helper.getBlockEntity(new BlockPos(4, 3, 3));
            block.setConnection(Direction.SOUTH, true, false);
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

    @GameTest(template = "lst_infinite_steam_nomaintenance", timeoutTicks = 50 + 1)
    public static void LstRefusesToRunWithoutMaintenance(GameTestHelper helper) {
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


    @GameTest(template = "lst_with_lava", timeoutTicks = 50 + 1)
    public static void LstRefusesToRunOnWrongFluid(GameTestHelper helper) {
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
