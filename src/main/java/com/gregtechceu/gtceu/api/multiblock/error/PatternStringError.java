package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

public class PatternStringError extends PatternError {

    public static Codec<PatternStringError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.COMPONENT.fieldOf("component").forGetter(PatternStringError::getComponent))
            .apply(instance, PatternStringError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("pattern_string_error"), CODEC);

    @Getter
    public final Component component;

    public PatternStringError(Component component) {
        super(BlockPos.ZERO);
        this.component = component;
    }

    public static PatternStringError of(Component component) {
        return new PatternStringError(component);
    }

    public static PatternStringError literal(String s) {
        return new PatternStringError(Component.literal(s));
    }

    public static PatternStringError literal(String s, Object... args) {
        return new PatternStringError(Component.literal(String.format(s, args)));
    }

    public static PatternStringError translatable(String s) {
        return new PatternStringError(Component.translatable(s));
    }

    public static PatternStringError translatable(String s, Object... args) {
        return new PatternStringError(Component.translatable(s, args));
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> parent.child(Text.of(component).asWidget());
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
