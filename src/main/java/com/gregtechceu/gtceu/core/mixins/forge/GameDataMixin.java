package com.gregtechceu.gtceu.core.mixins.forge;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    // Make GT register events fire first, even before minecraft registries.
    @ModifyVariable(
                    method = "postRegisterEvents",
                    at = @At(
                             value = "INVOKE",
                             target = "Ljava/util/Set;addAll(Ljava/util/Collection;)Z",
                             ordinal = 1,
                             shift = At.Shift.AFTER),
                    name = "ordered")
    private static Set<ResourceLocation> gtceuFirst(Set<ResourceLocation> ordered) {
        return ordered.stream()
                .sorted((a, b) -> {
                    boolean aGt = a.getNamespace().equals(GTCEu.MOD_ID);
                    boolean bGt = b.getNamespace().equals(GTCEu.MOD_ID);
                    if (aGt && !bGt) return -1;
                    if (!aGt && bGt) return 1;
                    return 0;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
