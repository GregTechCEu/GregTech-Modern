package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
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
    Function<BlockState, MapColor> getMapColor();

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
    boolean isIgnitedByLava();

    @Accessor
    boolean isLiquid();

    @Accessor
    boolean isForceSolidOff();

    @Accessor
    boolean isForceSolidOn();

    @Accessor
    PushReaction getPushReaction();

    @Accessor
    boolean isRequiresCorrectToolForDrops();

    @Accessor
    Optional<BlockBehaviour.OffsetFunction> getOffsetFunction();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Accessor
    void setOffsetFunction(Optional<BlockBehaviour.OffsetFunction> function);

    @Accessor
    boolean isSpawnParticlesOnBreak();

    @Accessor
    FeatureFlagSet getRequiredFeatures();

    @Accessor
    void setRequiredFeatures(FeatureFlagSet set);

    @Accessor
    BlockBehaviour.StatePredicate getEmissiveRendering();

    @Accessor
    NoteBlockInstrument getInstrument();

    @Accessor
    boolean isReplaceable();
}
