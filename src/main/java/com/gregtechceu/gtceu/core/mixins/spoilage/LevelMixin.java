package com.gregtechceu.gtceu.core.mixins.spoilage;

import com.gregtechceu.gtceu.api.item.spoilage.BlockEntitySpoilTicker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.ticks.TickPriority;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Accessor
    abstract ArrayList<BlockEntity> getFreshBlockEntities();

    @Inject(method = "tickBlockEntities",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/ArrayList;forEach(Ljava/util/function/Consumer;)V",
                     shift = At.Shift.AFTER))
    private void gtceu$addBlockEntityToSpoilTickers(CallbackInfo ci) {
        if ((Object) this instanceof ServerLevel serverLevel) {
            for (var be : getFreshBlockEntities()) {
                BlockEntitySpoilTicker.get(serverLevel).scheduleTickIfValid(be, 0, TickPriority.EXTREMELY_HIGH);
            }
        }
    }
}
