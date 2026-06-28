package com.gregtechceu.gtceu.core.mixins.kjs;

import com.gregtechceu.gtceu.GTCEu;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * This mixin defaults string resource locations for GT registries to use the gtceu namespace instead of the kjs namespace.
 */
@Mixin(RegistryEventJS.class)
public class RegistryEventJSMixin {

    @Final
    @Shadow
    private RegistryInfo<?> registry;

    @ModifyArg(
            method = "create*",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/latvian/mods/kubejs/KubeJS;appendModId(Ljava/lang/String;)Ljava/lang/String;"
            ),
            index = 0
    )
    private String modifyId(String id) {
        if (registry.key.location().getNamespace().equals("gtceu")) {
            return GTCEu.appendIdString(id);
        }
        return id;
    }

    @ModifyArg(
            method = "createCustom*",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/latvian/mods/kubejs/KubeJS;appendModId(Ljava/lang/String;)Ljava/lang/String;"
            ),
            index = 0
    )
    private String modifyCustomId(String id) {
        if (registry.key.location().getNamespace().equals("gtceu")) {
            return GTCEu.appendIdString(id);
        }
        return id;
    }

}
