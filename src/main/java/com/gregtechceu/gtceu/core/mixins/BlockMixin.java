package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(Block.class)
public abstract class BlockMixin {

    /**
     * Because most mods override Block#getDrops instead of using LootItemFunction to save custom data,
     * we use Mixin instead of GlobalLootModifier for compatibility.
     */
    @ModifyReturnValue(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
                       at = @At(value = "RETURN"))
    private static List<ItemStack> gtceu$modifyDrops(List<ItemStack> original, BlockState state, ServerLevel level,
                                                     BlockPos pos, @Nullable BlockEntity blockEntity,
                                                     @Nullable Entity entity, ItemStack tool) {
        if (!tool.isEmpty() && entity instanceof Player player) {
            boolean isSilktouch = EnchantmentHelper.hasSilkTouch(tool);
            int fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            return ToolEventHandlers.onHarvestDrops(player, tool, level, pos, state, isSilktouch,
                    fortuneLevel,
                    original, 1);
        }
        return original;
    }
}
