package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IItemUseFirst;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.minecraft.client.particle.AshParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
            method = {"useItemOn"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )},
            cancellable = true
    )
    public void port_lib$onItemFirstUse(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        Item held = itemStack.getItem();
        if (held instanceof IItemUseFirst first) {
            UseOnContext useoncontext = new UseOnContext(serverPlayer, interactionHand, blockHitResult);
            InteractionResult result = first.onItemUseFirst(itemStack, useoncontext);
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = {"destroyBlock"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"
            )}
    )
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandItem = player.getMainHandItem();

        if (GTToolType.MINING_HAMMER.is(mainHandItem) && mainHandItem.isCorrectToolForDrops(level.getBlockState(pos)) && !player.isCrouching()) {
            List<BlockPos> blockPosList = ToolHelper.getAOEPositions(player, player.getMainHandItem(), pos, 1);

            BlockState originBlock = level.getBlockState(pos);

            for (BlockPos blockPos : blockPosList) {
                if (!ToolHelper.aoeCanBreak(mainHandItem, level, pos, blockPos)) continue;
                level.destroyBlock(blockPos, true, player);
                if (mainHandItem.hurt(1, RandomSource.create(), player)) break;
            }
        }

    }
}
