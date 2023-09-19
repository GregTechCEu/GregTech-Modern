package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(ServerPacksSource.class)
public class ServerPacksSourceMixin {
    @Unique
    private static final PackSource GTCEU$RUNTIME = PackSource.decorating("pack.source.runtime");

    @SuppressWarnings("resource")
    @Inject(method = "loadPacks", at = @At("HEAD"))
    public void register(Consumer<Pack> adder, Pack.PackConstructor infoFactory, CallbackInfo ci) {
        GTDynamicDataPack.clearServer();
        GTRecipes.recipeAddition(GTDynamicDataPack::addRecipe);
        GTDynamicDataPack dataPack = new GTDynamicDataPack("gtceu:dynamic_data", AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet()));
        adder.accept(Pack.create(
                dataPack.getName(),
                true,
                () -> dataPack,
                infoFactory,
                Pack.Position.TOP,
                GTCEU$RUNTIME
        ));
    }
}
