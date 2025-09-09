package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiLineComponent extends ArrayList<MutableComponent> {

    @Getter
    private boolean ignoreSpaces = false;

    public MultiLineComponent(List<MutableComponent> components) {
        super();
        this.addAll(components);
    }

    public static MultiLineComponent of(Component c) {
        return new MultiLineComponent(List.of(c.copy()));
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
        if (s != null)
            GTUtil.getLast(this).append(s);
    }

    public void append(char c) {
        append(String.valueOf(c));
    }

    public MultiLineComponent append(@Nullable List<? extends Component> lines) {
        if (lines == null) return this;
        if (lines.isEmpty()) return this;
        for (Component line : lines) {
            GTUtil.getLast(this).append(line);
            this.add(MutableComponent.create(ComponentContents.EMPTY));
        }
        this.remove(this.size() - 1);
        return this;
    }

    public void appendNewline() {
        this.add(MutableComponent.create(ComponentContents.EMPTY));
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
            out.append(MultiLineComponent.of(c.withStyle(style)));
            out.appendNewline();
        }
        if (!out.isEmpty()) out.remove(out.size() - 1);
        return out;
    }

    public List<Component> toImmutable() {
        return new ArrayList<>(this);
    }

    public Tag toTag() {
        ListTag tag = new ListTag();
        for (MutableComponent component : this) {
            tag.add(StringTag.valueOf(Component.Serializer.toJson(component)));
        }
        return tag;
    }

    public static MultiLineComponent fromTag(ListTag tag) {
        MultiLineComponent out = MultiLineComponent.empty();
        out.clear();
        for (Tag i : tag) {
            out.add(Component.Serializer.fromJson(i.getAsString()));
        }
        return out;
    }

    public long toLong() {
        if (this.isEmpty()) return 0;
        if (this.size() > 1) throw new NumberFormatException(this.toString());
        return Long.parseLong(this.get(0).getString());
    }

    public MultiLineComponent setIgnoreSpaces(boolean ignoreSpaces) {
        this.ignoreSpaces = ignoreSpaces;
        return this;
    }
}
