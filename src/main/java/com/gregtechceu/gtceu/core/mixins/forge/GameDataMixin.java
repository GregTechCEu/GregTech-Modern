package com.gregtechceu.gtceu.core.mixins.forge;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Comparator;

import static java.util.Arrays.compare;

@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    // Make GT register events fire first, even before minecraft registries.
    @ModifyArg(method = "postRegisterEvents",
               at = @At(value = "INVOKE",
                        target = "Ljava/util/stream/Stream;sorted(Ljava/util/Comparator;)Ljava/util/stream/Stream;",
                        ordinal = 0))
    private static Comparator<ResourceLocation> gtceu$forceGTRegistriesFirst(Comparator<ResourceLocation> original) {
        return ((Comparator<ResourceLocation>) (a, b) -> {
            boolean aIsGT = GTRegistries.getRegistryOrder().contains(a);
            boolean bIsGT = GTRegistries.getRegistryOrder().contains(b);
            if (aIsGT && !bIsGT) return -1;
            if (!aIsGT && bIsGT) return 1;
            return Integer.compare(GTRegistries.getRegistryOrder().indexOf(a),
                    GTRegistries.getRegistryOrder().indexOf(b));
        }).thenComparing(original);
    }
}
