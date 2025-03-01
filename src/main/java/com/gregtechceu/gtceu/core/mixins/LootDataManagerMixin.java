package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LootDataManager.class)
public abstract class LootDataManagerMixin {

    @Inject(method = "apply",
            at = @At(value = "HEAD"))
    public void gtceu$injectLootTables(Map<LootDataType<?>, Map<ResourceLocation, ?>> allElements, CallbackInfo ci) {
        if (GTCEu.isDataGen()) return;

        Map<ResourceLocation, LootTable> lootTables = (Map<ResourceLocation, LootTable>) allElements
                .get(LootDataType.TABLE);
        MixinHelpers.generateGTDynamicLoot(lootTables);
    }
}
