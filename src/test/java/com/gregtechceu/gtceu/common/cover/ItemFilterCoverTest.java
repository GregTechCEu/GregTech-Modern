package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.BufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ItemFilterCoverTest {

    private static final ItemFilter DIAMOND_FILTER = new ItemFilter() {

        @Override
        public int testItemCount(ItemStack itemStack) {
            return 1;
        }

        @Override
        public WidgetGroup openConfigurator(int x, int y) {
            return new WidgetGroup();
        }

        @Override
        public CompoundTag saveFilter() {
            return new CompoundTag();
        }

        @Override
        public void setOnUpdated(Consumer<ItemFilter> onUpdated) {}

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack.is(Items.DIAMOND);
        }
    };

    public static void setupCrates(GameTestHelper helper) {
        helper.setBlock(new BlockPos(0, 1, 0), GTMachines.BUFFER[GTValues.LV].getBlock());
        helper.setBlock(new BlockPos(0, 2, 0), GTMachines.BUFFER[GTValues.LV].getBlock());
    }

    // Test for seeing if conveyors pass items correctly
    @GameTest(template = "empty_5x5", batch = "coverTests")
    public static void conveyorTransferFilteredItemsTest(GameTestHelper helper) {
        setupCrates(helper);
        BufferMachine crate1 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 1, 0)))
                .getMetaMachine();
        BufferMachine crate2 = (BufferMachine) ((MetaMachineBlockEntity) helper.getBlockEntity(new BlockPos(0, 2, 0)))
                .getMetaMachine();
        crate1.getInventory().setStackInSlot(0, new ItemStack(Items.FLINT, 16));
        crate1.getInventory().setStackInSlot(1, new ItemStack(Items.DIAMOND, 16));
        // LV Cover
        ConveyorCover cover = (ConveyorCover) TestUtils.placeCover(helper, crate2, GTItems.CONVEYOR_MODULE_LV.asStack(),
                Direction.DOWN);
        // Set the cover to import from crate1 to crate2
        cover.setIo(IO.IN);
        // Filter to whitelist diamonds
        ItemFilterCover filterCover = (ItemFilterCover) TestUtils.placeCover(helper, crate1,
                GTItems.ITEM_FILTER.asStack(), Direction.UP);
        filterCover.setFilterMode(FilterMode.FILTER_EXTRACT);
        filterCover.itemFilter = DIAMOND_FILTER;

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.getInventory().getStackInSlot(0),
                            new ItemStack(Items.DIAMOND, 16)),
                    "Filter cover didn't function correctly");
            helper.assertTrue(
                    TestUtils.isItemStackEqual(crate2.getInventory().getStackInSlot(1), ItemStack.EMPTY),
                    "Filter cover didn't function correctly");
        });
    }
}
