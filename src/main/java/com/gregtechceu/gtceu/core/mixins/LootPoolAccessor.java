package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootPool.class)
public interface LootPoolAccessor {

    @Accessor
    LootPoolEntryContainer[] getEntries();

    @Accessor
    @Mutable
    void setEntries(LootPoolEntryContainer[] entries);
}
