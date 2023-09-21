package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Mixin(FolderRepositorySource.class)
public class FolderRepositorySourceMixin {
    @Shadow @Final private PackType packType;
    @Unique
    private static final PackSource GTCEU$RUNTIME = PackSource.create(getSourceTextSupplier(), true);

    @Unique
    private static UnaryOperator<Component> getSourceTextSupplier() {
        Component text = Component.translatable("pack.source.runtime");
        return name -> Component.translatable("pack.nameAndSource", name, text).withStyle(ChatFormatting.GRAY);
    }

    @SuppressWarnings("resource")
    @Inject(method = "loadPacks", at = @At("HEAD"))
    public void register(Consumer<Pack> adder, CallbackInfo ci) {
        if (packType == PackType.SERVER_DATA) {
            // Clear old data
            GTDynamicDataPack.clearServer();

            // Register recipes & unification data again
            long startTime = System.currentTimeMillis();
            ChemicalHelper.reinitializeUnification();
            GTRecipes.recipeAddition(GTDynamicDataPack::addRecipe);
            GTCEu.LOGGER.info("GregTech Recipe loading took {}ms", System.currentTimeMillis() - startTime);

            // Load the data
            GTDynamicDataPack dataPack = new GTDynamicDataPack("gtceu:dynamic_data", AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet()));
            adder.accept(Pack.readMetaAndCreate(
                    dataPack.packId(),
                    Component.literal(dataPack.packId()),
                    true,
                    string -> dataPack,
                    PackType.SERVER_DATA,
                    Pack.Position.TOP,
                    GTCEU$RUNTIME
            ));
        }

    }
}
