package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    /**
     * Because most mods do not use LootItemFunction to save custom data,
     * we use Mixin instead of GlobalLootModifier for compatibility.
     */
    @ModifyExpressionValue(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static List<ItemStack> gtceu$onDropResources(List<ItemStack> original, BlockState state, Level level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool) {
        if (entity instanceof Player player) {
            boolean isSilktouch = EnchantmentHelper.hasSilkTouch(tool);
            int fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            ToolEventHandlers.onHarvestDrops(player, tool, (ServerLevel) level, pos, state, isSilktouch, fortuneLevel, original, 1);
        }
        return original;
    }
}
