package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedFluidDetectorCover;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedItemDetectorCover;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.gametest.util.TestUtils;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * The "electrolyzer" template contains a creative tank with water,
 * that is set to auto-output into an electrolyzer when supplied with a redstone signal
 * The redstone lamp is connected to the covers that are placed in the tests in this class.
 * The creative tank's rate of output is equal to the electrolyzer's rate of processing
 */
@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class AdvancedDetectorCoverTest {

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedActivityDetectorCoverWithActivity(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((MetaMachine) helper.getBlockEntity(new BlockPos(1, 2, 1)));
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        MutableInt expected = new MutableInt();
        helper.runAtTickTime(40 - machine.getOffsetTimer() % 20, () -> {
            IWorkable workable = (IWorkable) machine;
            expected.setValue(Math.round(15f * workable.getProgress() / workable.getMaxProgress()));
        });
        helper.runAtTickTime(41 - machine.getOffsetTimer() % 20, () -> {
            // due to this cover updating only once every 20 ticks, we need to check multiple values
            TestUtils.assertRedstoneEither(helper, new BlockPos(0, 2, 1),
                    (expected.intValue() + 13) % 15,
                    (expected.intValue() + 14) % 15,
                    expected.intValue());
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedActivityDetectorCoverWithoutActivity(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        SimpleTieredMachine machine = ((SimpleTieredMachine) helper.getBlockEntity(new BlockPos(1, 2, 1)));
        TestUtils.placeCover(helper, machine, GTItems.COVER_ACTIVITY_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        int offset = (int) (machine.getOffsetTimer() % 20L);
        helper.runAtTickTime(20 - offset, () -> {
            // Stop the fluid input
            helper.pullLever(2, 2, 2);
            // Also clear out the tank
            NotifiableFluidTank tank = (NotifiableFluidTank) machine
                    .getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP).get(0);
            tank.setFluidInTank(0, FluidStack.EMPTY);
        });
        // 20 ticks for the cover to update, 11 ticks for the recipe to finish, 1 tick for the cover to update
        helper.runAtTickTime(52 - offset,
                () -> helper.succeedWhen(() -> TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1))));
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedFluidDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((MetaMachine) helper.getBlockEntity(new BlockPos(1, 2, 1)));
        AdvancedFluidDetectorCover cover = (AdvancedFluidDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_FLUID_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMaxValue(100000);
        cover.setMinValue(1);
        cover.setLatched(false);
        MutableInt expected = new MutableInt();
        int offset = (int) (machine.getOffsetTimer() % 20L);
        helper.runAtTickTime(80 - offset, () -> {
            // Actually pull in the value at the time, since offset might change the amount
            var handler = machine.getFluidHandlerCap(null, false);
            long storedFluid = 0;
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                storedFluid += handler.getFluidInTank(tank).getAmount();
            }
            expected.setValue(RedstoneUtil.computeRedstoneBetweenValues(storedFluid,
                    cover.getMaxValue(), cover.getMinValue(), cover.isInverted()));
        });

        helper.runAtTickTime(81 - offset, () -> {
            int value = expected.intValue();
            TestUtils.assertRedstoneEither(helper, new BlockPos(0, 2, 1),
                    value,
                    Math.max(0, value - 1),
                    Math.min(15, value + 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCover(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((MetaMachine) helper.getBlockEntity(new BlockPos(1, 2, 1)));
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setLatched(true);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOn(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCoverBelowThreshold(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((MetaMachine) helper.getBlockEntity(new BlockPos(1, 2, 1)));
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMinValue(1);
        cover.setMaxValue(4);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }

    @GameTest(template = "electrolyzer", batch = "coverTests")
    public static void testAdvancedItemDetectorCoverAboveThreshold(GameTestHelper helper) {
        helper.pullLever(new BlockPos(2, 2, 2));
        MetaMachine machine = ((MetaMachine) helper.getBlockEntity(new BlockPos(1, 2, 1)));
        machine.getItemHandlerCap(null, false).setStackInSlot(0, new ItemStack(Items.DIRT, 5));
        AdvancedItemDetectorCover cover = (AdvancedItemDetectorCover) TestUtils.placeCover(helper, machine,
                GTItems.COVER_ITEM_DETECTOR_ADVANCED.asStack(), Direction.WEST);
        cover.setMinValue(1);
        cover.setMaxValue(4);
        cover.setLatched(true);
        helper.runAtTickTime(40, () -> {
            TestUtils.assertLampOff(helper, new BlockPos(0, 2, 1));
            helper.succeed();
        });
    }
}
