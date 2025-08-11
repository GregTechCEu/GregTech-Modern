package com.gregtechceu.gtceu.integration.ae2.machine;


import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.me.ManagedGridNode;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class PatternBufferTest {

    private static GTRecipeType LCRrecipeType;

    @BeforeBatch(batch = "PatternBuffer")
    public static void prepare(ServerLevel level) {
        LCRrecipeType = TestUtils.createRecipeTypeAndInsertRecipe("PatternBufferTests");
    }

    private static MetaMachine getMetaMachine(BlockEntity entity) {
        return ((MetaMachineBlockEntity) entity).getMetaMachine();
    }

    private record BusHolder(ItemBusPartMachine inputBus1, ItemBusPartMachine inputBus2, ItemBusPartMachine outputBus1,
                             FluidHatchPartMachine outputHatch1, MEPatternBufferPartMachine patternBuffer, WorkableMultiblockMachine controller) {}

    /**
     * Retrieves the busses for this specific template and force a multiblock structure check
     * 
     * @param helper the GameTestHelper
     * @return the busses, in the BusHolder record.
     */
    private static BusHolder getBussesAndForm(GameTestHelper helper) {
        WorkableMultiblockMachine controller = (WorkableMultiblockMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(1, 2, 0)));
        TestUtils.formMultiblock(controller);
        controller.setRecipeType(LCRrecipeType);
        ItemBusPartMachine inputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 1, 0)));
        ItemBusPartMachine inputBus2 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 0)));
        ItemBusPartMachine outputBus1 = (ItemBusPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 1, 0)));
        FluidHatchPartMachine outputHatch1 = (FluidHatchPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(0, 2, 0)));
        MEPatternBufferPartMachine patternBuffer = (MEPatternBufferPartMachine) getMetaMachine(
                helper.getBlockEntity(new BlockPos(2, 2, 1)));
        return new BusHolder(inputBus1, inputBus2, outputBus1, outputHatch1, patternBuffer, controller);
    }

    // Test for putting ingredient on the normal input bus when the pattern buffer exists on machine
    @GameTest(template = "patternbuffertest", batch = "PatternBuffer", setupTicks = 40, timeoutTicks = 200)
    public static void patternBufferNormalInputBusTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);
        busHolder.inputBus1.getInventory().setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE));
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Crafting items in same bus failed, expected STONE but was " +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
    }

    @GameTest(template = "patternbuffertest", batch = "PatternBuffer", setupTicks = 40, timeoutTicks = 200)
    public static void patternBufferPatternBufferTest(GameTestHelper helper) {
        BusHolder busHolder = getBussesAndForm(helper);

        IGrid grid = busHolder.patternBuffer.getGrid();
        ManagedGridNode node = (ManagedGridNode) busHolder.patternBuffer.getMainNode();
        ICraftingProvider provider = node.getNode().getService(ICraftingProvider.class);

        // busHolder.patternBuffer.requestItem ????? do some request logic here
        /*
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    TestUtils.isItemStackEqual(busHolder.outputBus1.getInventory().getStackInSlot(0),
                            new ItemStack(Blocks.STONE)),
                    "Crafting items in same bus failed, expected STONE but was " +
                            busHolder.outputBus1.getInventory().getStackInSlot(0).getDisplayName());
        });
         */
        helper.succeed();
    }
}
