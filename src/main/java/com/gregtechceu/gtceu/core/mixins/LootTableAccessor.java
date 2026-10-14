package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTable.class)
public interface LootTableAccessor {

    @Accessor()
    @Nullable
    ResourceLocation getRandomSequence();
}
