package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// TODO figure out better solution??? forge doesn't want to remap BEs for w/e reason
@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    @ModifyArg(method = "loadStatic",
               at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/resources/ResourceLocation;tryParse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static String gtceu$remapBlockEntityLoadBecauseDataFixDoesntWantToWork(String s) {
        switch (s) {
            case "gtceu:cable", "gtceu:fluid_pipe", "gtceu:item_pipe" ->
                    s = GTBlockEntities.MATERIAL_PIPE.getId().toString();
            case "gtceu:laser_pipe", "gtceu:optical_pipe" ->
                    s = GTBlockEntities.ACTIVABLE_PIPE.getId().toString();
            case "gtceu:duct_pipe" ->
                    s = GTBlockEntities.PIPE.getId().toString();
        }
        return s;
    }
}
