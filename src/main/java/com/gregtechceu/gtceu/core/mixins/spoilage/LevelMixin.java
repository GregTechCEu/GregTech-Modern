package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.BlockEntitySpoilTicker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.ticks.TickPriority;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Definition(id = "freshBlockEntities",
                field = "Lnet/minecraft/world/level/Level;freshBlockEntities:Ljava/util/ArrayList;",
                remap = false)
    @Definition(id = "forEach", method = "Ljava/util/ArrayList;forEach(Ljava/util/function/Consumer;)V", remap = false)
    @Expression("this.freshBlockEntities.forEach(@(?))")
    @ModifyExpressionValue(method = "tickBlockEntities", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private Consumer<BlockEntity> gtceu$addBlockEntityToSpoilTickers(Consumer<BlockEntity> original) {
        if ((Object) this instanceof ServerLevel serverLevel) {
            BlockEntitySpoilTicker ticker = BlockEntitySpoilTicker.get(serverLevel);
            return original.andThen(be -> ticker.scheduleTickIfValid(be, BlockEntitySpoilTicker.INITIAL_TICK_DELAY,
                    TickPriority.EXTREMELY_HIGH));
        } else {
            return original;
        }
    }
}
