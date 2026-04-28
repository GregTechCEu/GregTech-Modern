package com.gregtechceu.gtceu.gametest.world;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTestPlayer;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class RealWorldItemUsage {

    public static void lookAndBreak(GameTestPlayer player, ExtendedGameTestHelper helper, BlockPos pos) {
        player.lookAt(Anchor.EYES, helper.absoluteVec(pos.getCenter()));
        helper.runAfterDelay(1, () -> {
            helper.breakBlock(pos, player.getMainHandItem(), player);
        });
    }

    @TestHolder()
    // TODO this should use an actual structure instead of building it here
    @EmptyTemplate("5")
    // TODO disabled until we implement the NeoForge test framework
    // @GameTest(template = "empty_5x5")
    public static void testPickaxeInstantPickup(ExtendedGameTestHelper helper) {
        var player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        player.setItemSlot(EquipmentSlot.MAINHAND,
                new ItemStack(helper.getLevel().registryAccess().registry(Registries.ITEM).orElseThrow()
                        .getOrThrow(ResourceKey.create(Registries.ITEM, GTCEu.id("neutronium_pickaxe")))));
        // Allow player to stand
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.BEDROCK);
        // Blocks to break
        BlockPos[] positions = {
                new BlockPos(1, 1, 1),
                new BlockPos(1, 1, 2),
                new BlockPos(1, 1, 3),
                new BlockPos(2, 1, 1),
                new BlockPos(2, 1, 3),
                new BlockPos(3, 1, 1),
                new BlockPos(3, 1, 2),
                new BlockPos(3, 1, 3), };
        for (var pos : positions) {
            helper.setBlock(pos, Blocks.STONE);
        }
        long i = 0;
        for (var pos : positions) {
            helper.runAtTickTime(i * 2, () -> {
                lookAndBreak(player, helper, pos);
            });
            i += 1;
        }
        helper.runAtTickTime(i * 2 + 8, () -> {
            helper.assertPlayerHasItem(player, Items.COBBLESTONE);
            helper.assertBlockState(new BlockPos(1, 1, 1), BlockBehaviour.BlockStateBase::isAir, () -> "Expected air!");
            var slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.COBBLESTONE));
            helper.assertTrue(player.getSlot(slot).get().getCount() == positions.length,
                    "Player should have picked up four cobblestone");
            helper.succeed();
        });
    }
}
