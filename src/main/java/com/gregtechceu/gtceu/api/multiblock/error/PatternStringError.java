package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collections;

public class PatternStringError extends PatternError {

    public static Codec<PatternStringError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.COMPONENT.fieldOf("component").forGetter(PatternStringError::getComponent))
            .apply(instance, PatternStringError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("pattern_string_error"), CODEC);

    @Getter
    public final Component component;

    public PatternStringError(Component component) {
        super(null, Collections.emptyList());
        this.component = component;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            parent.child(Text.of(component).asWidget());
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
