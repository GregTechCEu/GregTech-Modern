package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {

    @Accessor("properties")
    BlockBehaviour.Properties getBlockProperties();

    @Accessor
    void setDrops(Identifier location);
}
