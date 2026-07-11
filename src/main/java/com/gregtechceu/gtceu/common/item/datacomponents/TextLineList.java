package com.gregtechceu.gtceu.common.item.datacomponents;

import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.With;

import java.util.Collections;
import java.util.List;

public record TextLineList(@With List<Component> lines, @With double scale, @With boolean paused) {

    // spotless:off
    public static final Codec<TextLineList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.listOf().fieldOf("lines").forGetter(TextLineList::lines),
            Codec.DOUBLE.fieldOf("scale").forGetter(TextLineList::scale),
            Codec.BOOL.fieldOf("paused").forGetter(TextLineList::paused)
    ).apply(instance, TextLineList::new));
    // spotless:on

    public static final TextLineList EMPTY = new TextLineList(Collections.emptyList(), 1.0f, false);

    public TextLineList {
        lines = Collections.unmodifiableList(lines);
        scale = Mth.clamp(scale, 0.0001f, 1000f);
    }

    public MultiLineComponent toMultiLineComponent() {
        MultiLineComponent multiLine = new MultiLineComponent();
        for (Component c : this.lines) {
            multiLine.add(c.copy());
        }
        return multiLine;
    }
}
