package com.gregtechceu.gtceu.core.mixins.dev.test;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.gametest.framework.GameTestRegistry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameTestRegistry.class)
public class GameTestRegistryMixin {

    @ModifyExpressionValue(method = "turnMethodIntoTestFunction",
                           at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Method;getName()Ljava/lang/String;"))
    private static String gtceu$makeTestMethodNameSnakeCase(String original) {
        return FormattingUtil.toLowerCaseUnderscore(original);
    }
}
