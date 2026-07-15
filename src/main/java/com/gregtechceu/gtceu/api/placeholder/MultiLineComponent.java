package com.gregtechceu.gtceu.api.placeholder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Accessors(chain = true)
public class MultiLineComponent extends ArrayList<MutableComponent> {

    // spotless:off
    public static final Codec<MultiLineComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.listOf().fieldOf("text").forGetter(MultiLineComponent::toImmutable),
            GraphicsComponent.CODEC.listOf().orElse(Collections.emptyList()).fieldOf("graphics").forGetter(MultiLineComponent::getGraphics)
    ).apply(instance, MultiLineComponent::of));
    // spotless:on

    public MultiLineComponent() {}

    @Getter
    @Setter
    private boolean ignoreSpaces = false;

    @Getter
    private final List<GraphicsComponent> graphics = new ArrayList<>();

    public MultiLineComponent(List<MutableComponent> components) {
        super(components);
    }

    protected static MultiLineComponent of(List<Component> lines, List<GraphicsComponent> graphics) {
        MultiLineComponent component = lines.stream()
                .map(Component::copy)
                .collect(Collectors.toCollection(MultiLineComponent::new));
        component.addGraphics(graphics);
        return component;
    }

    public static MultiLineComponent of(List<Component> lines) {
        List<MutableComponent> mutableLines = lines.stream()
                .map(Component::copy)
                .toList();
        return new MultiLineComponent(mutableLines);
    }

    public static MultiLineComponent of(Component c) {
        return MultiLineComponent.of(c.copy());
    }

    public static MultiLineComponent of(MutableComponent c) {
        MultiLineComponent value = new MultiLineComponent();
        value.add(c);
        return value;
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
        String s = this.get(0).getString();
        if (s.startsWith("0x")) return Integer.parseInt(s.substring(2), 16);
        if (s.startsWith("0b")) return Integer.parseInt(s.substring(2), 2);
        return Integer.parseInt(s);
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

    public MultiLineComponent append(MultiLineComponent multiLineComponent) {
        if (multiLineComponent == null) return this;
        this.graphics.addAll(multiLineComponent.getGraphics());
        return this.append(multiLineComponent.toImmutable());
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
        if (!out.isEmpty()) out.removeLast();
        return out;
    }

    public MultiLineComponent withStyle(UnaryOperator<Style> style) {
        MultiLineComponent out = MultiLineComponent.empty();
        for (MutableComponent c : this) {
            out.append(MultiLineComponent.of(c.withStyle(style)));
            out.appendNewline();
        }
        if (!out.isEmpty()) out.removeLast();
        out.addGraphics(this.getGraphics());
        return out;
    }

    public MultiLineComponent withStyle(ChatFormatting... style) {
        MultiLineComponent out = MultiLineComponent.empty();
        for (MutableComponent c : this) {
            out.append(c.withStyle(style));
            out.appendNewline();
        }
        if (!out.isEmpty()) out.removeLast();
        out.addGraphics(this.getGraphics());
        return out;
    }

    public List<Component> toImmutable() {
        return new ArrayList<>(this);
    }

    public Tag toTag(HolderLookup.Provider registries) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag tag = new ListTag();
        for (MutableComponent component : this) {
            tag.add(StringTag.valueOf(Component.Serializer.toJson(component, registries)));
        }
        compoundTag.put("text", tag);
        ListTag graphicsTag = new ListTag();
        for (GraphicsComponent component : this.getGraphics()) {
            graphicsTag.add(GraphicsComponent.CODEC.encodeStart(NbtOps.INSTANCE, component).result().orElseThrow());
        }
        compoundTag.put("graphics", graphicsTag);
        return compoundTag;
    }

    public static MultiLineComponent fromTag(@Nullable Tag tag, HolderLookup.Provider registries) {
        MultiLineComponent out = MultiLineComponent.empty();
        out.clear();
        if (tag == null) return out;
        if (tag instanceof ListTag listTag) {
            for (Tag i : listTag) {
                out.add(Component.Serializer.fromJson(i.getAsString(), registries));
            }
        } else if (tag instanceof CompoundTag compoundTag) {
            ListTag textTag = compoundTag.getList("text", Tag.TAG_STRING);
            for (Tag i : textTag) out.add(Component.Serializer.fromJson(i.getAsString(), registries));
            ListTag graphicsTag = compoundTag.getList("graphics", Tag.TAG_COMPOUND);
            for (Tag i : graphicsTag) out.addGraphics(GraphicsComponent.CODEC.parse(NbtOps.INSTANCE, i).getOrThrow());
        }
        return out;
    }

    public long toLong() {
        if (this.isEmpty()) return 0;
        if (this.size() > 1) throw new NumberFormatException(this.toString());
        String s = this.get(0).getString();
        if (s.startsWith("0b")) return Long.parseLong(s.substring(2), 2);
        if (s.startsWith("0x")) return Long.parseLong(s.substring(2), 16);
        return Long.parseLong(s);
    }

    public MultiLineComponent addGraphics(GraphicsComponent... graphicsComponents) {
        return this.addGraphics(List.of(graphicsComponents));
    }

    public MultiLineComponent addGraphics(Collection<GraphicsComponent> graphicsComponents) {
        this.graphics.addAll(graphicsComponents);
        return this;
    }
}
