package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote ServerPlayerGameModeMixin
 */
@Mixin({ServerPlayerGameMode.class})
public class ServerPlayerGameModeMixin {

    @Final
    @Shadow protected ServerPlayer player;

    @Shadow protected ServerLevel level;

    @Inject(
            method = {"destroyBlock"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"
            )}
    )
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandItem = player.getMainHandItem();

        if (ToolHelper.getAoEDefinition(mainHandItem) != AoESymmetrical.none() && mainHandItem.isCorrectToolForDrops(level.getBlockState(pos)) && !player.isCrouching()) {
            Set<BlockPos> breakablePositions = ToolHelper.getHarvestableBlocks(mainHandItem, ToolHelper.getAoEDefinition(mainHandItem), level, player, player.pick(4.5F, 1, false)); // TODO in 1.21 change hitDistance to entity reach attrib value
            for (BlockPos blockPos : breakablePositions) {
                //if (!ToolHelper.aoeCanBreak(mainHandItem, level, pos, blockPos)) continue;
                level.destroyBlock(blockPos, true, player);
                if (mainHandItem.hurt(1, RandomSource.create(), player)) break;
            }
        }

    }
}
