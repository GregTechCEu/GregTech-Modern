package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.data.loader.VirtualGtceuDataPack;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldLoader.PackConfig.class)
public class PackConfigMixin {

    @ModifyVariable(method = "createResourceManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V"), index = 2)
    private List<PackResources> injectGTCEuPacks(List<PackResources> resourcePacks) {
        ArrayList<PackResources> mutableList = new ArrayList<>(resourcePacks);
        VirtualGtceuDataPack pack = new VirtualGtceuDataPack();
        mutableList.add(pack);
        return mutableList;
    }

}
