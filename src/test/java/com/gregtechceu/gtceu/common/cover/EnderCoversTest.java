package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.cover.ender.EnderFluidLinkCover;
import com.gregtechceu.gtceu.common.cover.ender.EnderItemLinkCover;
import com.gregtechceu.gtceu.common.cover.ender.EnderRedstoneLinkCover;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class EnderCoversTest {

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void fluidLinkCoverTest(GameTestHelper helper) {
        QuantumTankMachine tank1 = (QuantumTankMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1),
                GTMachines.SUPER_TANK[1]);
        QuantumTankMachine tank2 = (QuantumTankMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 3),
                GTMachines.SUPER_TANK[1]);
        EnderFluidLinkCover cover1 = (EnderFluidLinkCover) TestUtils.placeCover(helper, tank1,
                GTItems.COVER_ENDER_FLUID_LINK.asStack(), Direction.UP);
        EnderFluidLinkCover cover2 = (EnderFluidLinkCover) TestUtils.placeCover(helper, tank2,
                GTItems.COVER_ENDER_FLUID_LINK.asStack(), Direction.UP);
        cover1.setIo(IO.IN);
        cover2.setIo(IO.OUT);
        tank1.getFluidHandlerCap(Direction.UP, false).fill(new FluidStack(Fluids.WATER, 1000),
                IFluidHandler.FluidAction.EXECUTE);
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(TestUtils.isFluidStackEqual(tank2.getStored(), new FluidStack(Fluids.WATER, 1000)),
                    "ender fluid link cover didn't transfer fluid");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5", batch = "coverTests", required = false)
    public static void only_works_in_game_itemLinkCoverTest(GameTestHelper helper) {
        QuantumChestMachine chest1 = (QuantumChestMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1),
                GTMachines.SUPER_CHEST[1]);
        QuantumChestMachine chest2 = (QuantumChestMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 3),
                GTMachines.SUPER_CHEST[1]);
        EnderItemLinkCover cover1 = (EnderItemLinkCover) TestUtils.placeCover(helper, chest1,
                GTItems.COVER_ENDER_ITEM_LINK.asStack(), Direction.UP);
        EnderItemLinkCover cover2 = (EnderItemLinkCover) TestUtils.placeCover(helper, chest2,
                GTItems.COVER_ENDER_ITEM_LINK.asStack(), Direction.UP);
        cover1.setIo(IO.IN);
        cover2.setIo(IO.OUT);
        chest1.getItemHandlerCap(Direction.UP, false).insertItem(0, new ItemStack(Items.DIAMOND, 64), false);
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(TestUtils.isItemStackEqual(chest2.getStored(), new ItemStack(Items.DIAMOND, 64)),
                    "ender item link cover didn't transfer items");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void redstoneLinkCoverTest(GameTestHelper helper) {
        QuantumTankMachine tank1 = (QuantumTankMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 1),
                GTMachines.SUPER_TANK[1]);
        QuantumTankMachine tank2 = (QuantumTankMachine) TestUtils.setMachine(helper, new BlockPos(1, 1, 3),
                GTMachines.SUPER_TANK[1]);
        EnderRedstoneLinkCover cover1 = (EnderRedstoneLinkCover) TestUtils.placeCover(helper, tank1,
                GTItems.COVER_ENDER_REDSTONE_LINK.asStack(), Direction.UP);
        EnderRedstoneLinkCover cover2 = (EnderRedstoneLinkCover) TestUtils.placeCover(helper, tank2,
                GTItems.COVER_ENDER_REDSTONE_LINK.asStack(), Direction.UP);
        cover1.setIo(IO.IN);
        cover2.setIo(IO.OUT);
        helper.setBlock(new BlockPos(1, 2, 1),
                Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR));
        helper.setBlock(new BlockPos(1, 2, 3), Blocks.REDSTONE_LAMP);
        helper.pullLever(new BlockPos(1, 2, 1));

        helper.onEachTick(() -> {
            if (helper.getTick() < 10) return;
            TestUtils.assertLampOn(helper, new BlockPos(1, 2, 3));
        });
        helper.runAtTickTime(20, helper::succeed);
    }
}
