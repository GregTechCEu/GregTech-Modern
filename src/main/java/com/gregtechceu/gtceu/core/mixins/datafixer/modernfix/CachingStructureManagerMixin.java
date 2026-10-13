package com.gregtechceu.gtceu.core.mixins.datafixer.modernfix;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.DataFixHelper;
import com.gregtechceu.gtceu.common.data.datafixer.GTDataFixers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import org.embeddedt.modernfix.structure.CachingStructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(value = CachingStructureManager.class, remap = false)
public class CachingStructureManagerMixin {

    @Definition(id = "currentDataVersion", local = @Local(type = int.class, name = "currentDataVersion"))
    @Expression("currentDataVersion < ?")
    @ModifyExpressionValue(method = "readStructureTag", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean gtceu$alsoRequireGTDataVersionToBeUpToDate1(boolean original,
                                                                       @Local CompoundTag currentTag) {
        if (GTDataFixers.getDataFixer() == null) return original;
        return original || DataFixHelper.getGTDataVersion(currentTag) < GTCEu.GT_DATA_VERSION;
    }

    @Definition(id = "cachedUpgraded", local = @Local(type = CompoundTag.class, name = "cachedUpgraded"))
    @Definition(id = "getInt", method = "Lnet/minecraft/nbt/CompoundTag;getInt(Ljava/lang/String;)I", remap = true)
    @Expression("cachedUpgraded.getInt('DataVersion') == ?")
    @ModifyExpressionValue(method = "readStructureTag", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean gtceu$alsoRequireGTDataVersionToBeUpToDate2(boolean original,
                                                                       @Local(name = "cachedUpgraded") CompoundTag cachedUpgraded) {
        if (GTDataFixers.getDataFixer() == null) return original;
        return original && cachedUpgraded.getInt(DataFixHelper.GT_DATA_VERSION_TAG) == GTCEu.GT_DATA_VERSION;
    }

    @Inject(method = "readStructureTag",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
                     remap = true,
                     ordinal = 1))
    private static void gtceu$addGTDataVersion(ResourceLocation location, DataFixer datafixer, InputStream stream,
                                               CallbackInfoReturnable<CompoundTag> cir,
                                               @Local(name = "currentTag") CompoundTag currentTag) {
        DataFixHelper.addGTDataVersion(currentTag);
    }
}
