package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {

    @Mutable
    @Accessor("drops")
    void setDrops(Optional<ResourceKey<LootTable>> location);
}
