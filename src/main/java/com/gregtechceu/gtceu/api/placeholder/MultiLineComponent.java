package com.gregtechceu.gtceu.api.placeholder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

@Accessors(chain = true)
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
    @Setter
    private boolean ignoreSpaces = false;

    @Getter
    private final List<GraphicsComponent> graphics = new ArrayList<>();

    public MultiLineComponent(List<MutableComponent> components) {
        super(components);
    }

    public static MultiLineComponent of(List<Component> lines) {
        List<MutableComponent> mutableLines = lines.stream()
                .map(Component::copy)
                .toList();
        return new MultiLineComponent(mutableLines);
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

    public Tag toTag(HolderLookup.Provider registries) {
        CompoundTag compoundTag = new CompoundTag();
        ListTag tag = new ListTag();
        for (MutableComponent component : this) {
            tag.add(StringTag.valueOf(Component.Serializer.toJson(component, registries)));
        }
        compoundTag.put("text", tag);
        ListTag graphicsTag = new ListTag();
        for (GraphicsComponent component : this.getGraphics()) {
            graphicsTag.add(component.toTag());
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
            for (Tag i : graphicsTag) out.addGraphics(GraphicsComponent.fromTag(i));
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
