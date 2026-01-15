package com.gregtechceu.gtceu.api.placeholder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import com.mojang.serialization.Codec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MultiLineComponent extends ArrayList<MutableComponent> {

    public static final Codec<MultiLineComponent> CODEC = ComponentSerialization.CODEC.listOf()
            .xmap(list -> {
                if (((List<? extends Component>) list) instanceof MultiLineComponent multiLine) {
                    return multiLine;
                }
                MultiLineComponent multiLine = new MultiLineComponent();
                for (Component c : list) {
                    multiLine.add(c.copy());
                }
                return multiLine;
            }, MultiLineComponent::toImmutable);

    public MultiLineComponent() {}

    @Getter
    private boolean ignoreSpaces = false;

    public MultiLineComponent(List<MutableComponent> components) {
        super(components);
    }

    public static MultiLineComponent of(Component c) {
        return new MultiLineComponent(List.of(c.copy()));
    }

    public static MultiLineComponent literal(char c) {
        return MultiLineComponent.of(Component.literal(String.valueOf(c)));
    }

    public static MultiLineComponent literal(String s) {
        return MultiLineComponent.of(Component.literal(s));
    }

    public static MultiLineComponent literal(long n) {
        return MultiLineComponent.literal(String.valueOf(n));
    }

    public static MultiLineComponent literal(double n) {
        return MultiLineComponent.literal(String.valueOf(n));
    }

    public static MultiLineComponent empty() {
        return MultiLineComponent.of(CommonComponents.EMPTY);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MultiLineComponent)
            return Objects.equals(this.toString(), o.toString());
        return false;
    }

    public boolean equalsString(String s) {
        return Objects.equals(this.toString(), s);
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        if (this.isEmpty()) return out.toString();
        for (Component component : this) {
            out.append(component.getString());
            out.append('\n');
        }
        return out.substring(0, out.length() - 1);
    }

    public double toDouble() {
        if (this.isEmpty()) return 0;
        if (this.size() > 1) throw new NumberFormatException(this.toString());
        return Double.parseDouble(this.get(0).getString());
    }

    public int toInt() {
        if (this.isEmpty()) return 0;
        if (this.size() > 1) throw new NumberFormatException(this.toString());
        return Integer.parseInt(this.get(0).getString());
    }

    public void append(@Nullable String s) {
        if (s != null) {
            this.getLast().append(s);
        }
    }

    public void append(char c) {
        append(String.valueOf(c));
    }

    public MultiLineComponent append(@Nullable List<? extends Component> lines) {
        if (lines == null) return this;
        if (lines.isEmpty()) return this;
        for (Component line : lines) {
            this.getLast().append(line);
            this.add(Component.empty());
        }
        this.removeLast();
        return this;
    }

    public MultiLineComponent append(@NotNull Component line) {
        this.getLast().append(line);
        return this;
    }

    public void appendNewline() {
        this.add(Component.empty());
    }

    public MultiLineComponent withStyle(Style style) {
        MultiLineComponent out = MultiLineComponent.empty();
        for (MutableComponent c : this) {
            out.append(MultiLineComponent.of(c.withStyle(style)));
            out.appendNewline();
        }
        if (!out.isEmpty()) out.remove(out.size() - 1);
        return out;
    }

    public MultiLineComponent withStyle(ChatFormatting... style) {
        MultiLineComponent out = MultiLineComponent.empty();
        for (MutableComponent c : this) {
            out.append(c.withStyle(style));
            out.appendNewline();
        }
        if (!out.isEmpty()) out.remove(out.size() - 1);
        return out;
    }

    public @UnmodifiableView List<Component> toImmutable() {
        return Collections.unmodifiableList(this);
    }

    public MultiLineComponent setIgnoreSpaces(boolean ignoreSpaces) {
        this.ignoreSpaces = ignoreSpaces;
        return this;
    }
}
