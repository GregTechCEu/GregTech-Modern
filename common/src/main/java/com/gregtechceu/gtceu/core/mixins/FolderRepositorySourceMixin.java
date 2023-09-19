package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mixin(FolderRepositorySource.class)
public class FolderRepositorySourceMixin {
    @Shadow @Final private PackType packType;
    @Unique
    private static final PackSource GTCEU$RUNTIME = PackSource.create(component -> Component.translatable("pack.source.runtime", component), true);

    @SuppressWarnings("resource")
    @Inject(method = "loadPacks", at = @At("HEAD"))
    public void register(Consumer<Pack> adder, CallbackInfo ci) {
        if (packType == PackType.SERVER_DATA) {
            GTDynamicDataPack.clearServer();
            GTRecipes.recipeAddition(GTDynamicDataPack::addRecipe);
            GTDynamicDataPack dataPack = new GTDynamicDataPack("gtceu:dynamic_data", AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet()));
            Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
                @Override
                public PackResources open(String string) {
                    return dataPack;
                }
            };
            adder.accept(Pack.create(
                    dataPack.packId(),
                    Component.literal(dataPack.packId()),
                    true,
                    supplier,
                    Pack.readPackInfo(dataPack.packId(), supplier),
                    PackType.SERVER_DATA,
                    Pack.Position.TOP,
                    false,
                    GTCEU$RUNTIME
            ));
        }

    }
}
