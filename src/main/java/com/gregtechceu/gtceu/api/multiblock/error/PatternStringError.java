package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

public class PatternStringError extends PatternError {

    // spotless:off
    public static final MapCodec<PatternStringError> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("component").forGetter(PatternStringError::getComponent),
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::pos)
    ).apply(instance, PatternStringError::new));
    //spotless:on

    @Getter
    public final Component component;

    public PatternStringError(Component component, BlockPos pos) {
        super(pos);
        this.component = component;
    }

    public PatternStringError(Component component) {
        super();
        this.component = component;
    }

    public static PatternStringError of(Component component) {
        return new PatternStringError(component);
    }

    public static PatternStringError of(Component component, BlockPos pos) {
        return new PatternStringError(component, pos);
    }

    public static PatternStringError literal(String s) {
        return new PatternStringError(Component.literal(s));
    }

    public static PatternStringError literal(String s, Object... args) {
        return new PatternStringError(Component.literal(String.format(s, args)));
    }

    public static PatternStringError translatable(String langKey) {
        return new PatternStringError(Component.translatable(langKey));
    }

    public static PatternStringError translatable(String langKey, Object... args) {
        return new PatternStringError(Component.translatable(langKey, args));
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> parent.child(Text.of(component).asWidget());
    }

    @Override
    public PatternErrorType type() {
        return GTPatternErrors.PATTERN_STRING_ERROR.value();
    }
}
