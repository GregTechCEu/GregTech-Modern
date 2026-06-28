package com.gregtechceu.gtceu.core.mixins.forge;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    // Make GT register events fire first, even before minecraft registries.
    @Definition(id = "sorted", method = "Ljava/util/stream/Stream;sorted")
    @Expression("?.sorted(@(?))")
    @ModifyExpressionValue(method = "postRegisterEvents", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static Comparator<ResourceLocation> gtceu$forceGTRegistriesFirst(Comparator<ResourceLocation> original) {
        return ((Comparator<ResourceLocation>) (a, b) -> {
            boolean aIsGT = GTRegistries.Keys.all().contains(a);
            boolean bIsGT = GTRegistries.Keys.all().contains(b);
            if (aIsGT && !bIsGT) return -1;
            if (!aIsGT && bIsGT) return 1;
            return 0;
        }).thenComparing(original);
    }
}
