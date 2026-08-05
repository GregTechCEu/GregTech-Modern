package com.gregtechceu.gtceu.core.mixins.forge;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.compare;

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
    private static Set<ResourceLocation> gtceu$forceGTRegistriesFirst(Set<ResourceLocation> ordered) {
        LinkedHashSet<ResourceLocation> newSet = new LinkedHashSet<>(GTRegistries.getRegistryOrder());
        newSet.addAll(ordered);
        return newSet;
    }
}
