package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    public static ResourceLocation ID = GTCEu.id("pattern_string_error");

    @Getter
    public final Component component;

    public PatternStringError(Component component) {
        super(null, Collections.emptyList());
        this.component = component;
    }

    @Override
    public PatternErrorUI applyErrorInformation() {
        return (parent) -> {
            parent.child(Text.of(component).asWidget());
        };
    }

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }
}
