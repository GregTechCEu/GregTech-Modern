package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

@Mixin(BlockBehaviour.Properties.class)
public interface BlockPropertiesAccessor {
    @Accessor
    float getDestroyTime();
    @Accessor
    float getExplosionResistance();
    @Accessor
    boolean isHasCollision();
    @Accessor
    boolean isIsRandomlyTicking();
    @Accessor
    ToIntFunction<BlockState> getLightEmission();
    @Accessor
    Function<BlockState, MaterialColor> getMaterialColor();
    @Accessor
    void setMaterialColor(Function<BlockState, MaterialColor> function);
    @Accessor
    SoundType getSoundType();
    @Accessor
    float getFriction();
    @Accessor
    float getSpeedFactor();
    @Accessor
    boolean isDynamicShape();
    @Accessor
    boolean isCanOcclude();
    @Accessor
    boolean isIsAir();
    @Accessor
    boolean isRequiresCorrectToolForDrops();
    @Accessor
    Function<BlockState, BlockBehaviour.OffsetType> getOffsetType();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Accessor
    void setOffsetType(Function<BlockState, BlockBehaviour.OffsetType> function);
    @Accessor
    BlockBehaviour.StatePredicate getEmissiveRendering();
}
