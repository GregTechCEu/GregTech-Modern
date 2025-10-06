package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ItemBusPartMachineTest {

    @BeforeBatch(batch = "ItemBusPartMachine")
    public static void prepare(ServerLevel level) {}

    // Test for input busses auto importing
    @GameTest(template = "empty_5x5", batch = "ItemBusPartMachine")
    public static void ItemBusPartMachineAutoImportTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.ITEM_IMPORT_BUS[1].getBlock());
        CrateMachine crate = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        ItemBusPartMachine itemBus = (ItemBusPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        itemBus.setFrontFacing(Direction.DOWN);
        crate.inventory.setStackInSlot(0, new ItemStack(Blocks.STONE, 16));
        helper.succeedWhen(() -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    itemBus.getInventory().getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 16)), "Input bus didn't automatically import");
        });
    }

    // Test for input busses not auto importing when off
    @GameTest(template = "empty_5x5", batch = "ItemBusPartMachine")
    public static void ItemBusPartMachineAutoImportFalseWhenOffTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.ITEM_IMPORT_BUS[1].getBlock());
        CrateMachine crate = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        ItemBusPartMachine itemBus = (ItemBusPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        itemBus.setFrontFacing(Direction.DOWN);
        itemBus.setWorkingEnabled(false);
        crate.inventory.setStackInSlot(0, new ItemStack(Blocks.STONE, 16));
        helper.onEachTick(() -> {
            helper.assertFalse(TestUtils.isItemStackEqual(
                    itemBus.getInventory().getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 16)), "Input bus automatically imported when off");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for output busses auto exporting
    @GameTest(template = "empty_5x5", batch = "ItemBusPartMachine")
    public static void ItemBusPartMachineAutoExportTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.ITEM_EXPORT_BUS[1].getBlock());
        CrateMachine crate = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        ItemBusPartMachine itemBus = (ItemBusPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        itemBus.setFrontFacing(Direction.DOWN);
        itemBus.getInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 16));
        helper.succeedWhen(() -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    crate.inventory.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 16)), "Input bus didn't automatically export");
        });
    }

    // Test for export busses not auto export when off
    @GameTest(template = "empty_5x5", batch = "ItemBusPartMachine")
    public static void ItemBusPartMachineAutoExportFalseWhenOffTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.ITEM_EXPORT_BUS[1].getBlock());
        CrateMachine crate = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        ItemBusPartMachine itemBus = (ItemBusPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        itemBus.setFrontFacing(Direction.DOWN);
        itemBus.setWorkingEnabled(false);
        itemBus.getInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 16));
        helper.onEachTick(() -> {
            helper.assertFalse(TestUtils.isItemStackEqual(
                    crate.inventory.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 16)), "Export bus automatically exported when off");
        });
        TestUtils.succeedAfterTest(helper);
    }

    // Test for passthrough busses auto passthrough'ing
    @GameTest(template = "empty_5x5", batch = "ItemBusPartMachine")
    public static void ItemBusPartMachineAutoPassthroughTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.ITEM_PASSTHROUGH_HATCH[1].getBlock());
        helper.setBlock(new BlockPos(0, 3, 0), GTMachines.BRONZE_CRATE.getBlock());
        CrateMachine crate = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        ItemBusPartMachine itemBus = (ItemBusPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 3, 0)))
                .getMetaMachine();
        itemBus.setFrontFacing(Direction.DOWN);
        crate.inventory.setStackInSlot(0, new ItemStack(Blocks.STONE, 16));
        helper.succeedWhen(() -> {
            helper.assertTrue(TestUtils.isItemStackEqual(
                    crate2.inventory.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 16)), "Passthrough bus didn't automatically export");
        });
    }

    // Test for passthrough busses not auto passthrough when off
    @GameTest(template = "empty_5x5", batch = "ItemBusPartMachine")
    public static void ItemBusPartMachineAutoPassthroughFalseWhenOffTest(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BRONZE_CRATE.getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.ITEM_PASSTHROUGH_HATCH[1].getBlock());
        helper.setBlock(new BlockPos(0, 3, 0), GTMachines.BRONZE_CRATE.getBlock());
        CrateMachine crate = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        ItemBusPartMachine itemBus = (ItemBusPartMachine) ((MetaMachineBlockEntity) helper
                .getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        CrateMachine crate2 = (CrateMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 3, 0)))
                .getMetaMachine();
        itemBus.setFrontFacing(Direction.DOWN);
        itemBus.setWorkingEnabled(false);
        crate.inventory.setStackInSlot(0, new ItemStack(Blocks.STONE, 16));
        helper.onEachTick(() -> {
            helper.assertFalse(TestUtils.isItemStackEqual(
                    crate2.inventory.getStackInSlot(0),
                    new ItemStack(Blocks.STONE, 16)),
                    "Passthrough bus automatically exported when they shouldn't have");
        });
        TestUtils.succeedAfterTest(helper);
    }
}
