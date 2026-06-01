package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.Text;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collections;

public class PatternStringError extends PatternError {

    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.flatXmap(
            json -> {
                try {
                    return DataResult.success(Component.Serializer.fromJson(json));
                } catch (JsonParseException e) {
                    return DataResult.error(() -> "Failed to parse component: " + e.getMessage());
                }
            },
            component -> DataResult.success(Component.Serializer.toJson(component)));

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }

    public static Codec<PatternStringError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COMPONENT_CODEC.fieldOf("component").forGetter(PatternStringError::getComponent))
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
}
