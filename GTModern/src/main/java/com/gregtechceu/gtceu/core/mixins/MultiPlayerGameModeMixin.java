package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote MultiPlayerGameModeMixin
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void gtceu$destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        if (minecraft.player == null ||
                minecraft.level == null ||
                !ToolHelper.hasBehaviorsTag(mainHandItem) ||
                ToolHelper.getAoEDefinition(mainHandItem) == AoESymmetrical.none() ||
                minecraft.player.isShiftKeyDown() ||
                !mainHandItem.isCorrectToolForDrops(minecraft.level.getBlockState(pos)))
            return;

        cir.setReturnValue(false);
        Level level = minecraft.level;

        if (level == null) return;
        BlockState state = level.getBlockState(pos);

        state.getBlock().destroy(level, pos, state);
    }
}
