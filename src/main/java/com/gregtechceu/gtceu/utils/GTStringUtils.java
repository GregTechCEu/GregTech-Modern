package com.gregtechceu.gtceu.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GTStringUtils {

    /**
     * Better implementation of {@link ItemStack#toString()} which respects the stack-aware
     * {@link net.minecraft.world.item.Item#getDescriptionId(ItemStack)} method.
     *
     * @param stack the stack to convert
     * @return the string form of the stack
     */
    @NotNull
    public static String itemStackToString(@NotNull ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return stack.getCount() + "x_" + itemId.getNamespace() + "_" + itemId.getPath();
    }

    @NotNull
    public static String fluidStackToString(@NotNull FluidStack stack) {
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return stack.getAmount() + "x_" + fluidId.getNamespace() + "_" + fluidId.getPath();
    }

    /**
     * This function does this:
     * <ul>
     * <li>{@code 1} -> {@code "1st"}</li>
     * <li>{@code 2} -> {@code "2nd"}</li>
     * <li>{@code 3} -> {@code "3rd"}</li>
     * <li>{@code 4} -> {@code "4th"}</li>
     * <li>...</li>
     * </ul>
     */
    @NotNull
    public static String getIntOrderingSuffix(int x) {
        if ((x % 100) / 10 == 1) return x + "th";
        if (x % 10 == 1) return x + "st";
        if (x % 10 == 2) return x + "nd";
        if (x % 10 == 3) return x + "rd";
        return x + "th";
    }

    public static List<MutableComponent> literalLine(String s) {
        return new ArrayList<>(List.of(Component.literal(s)));
    }

    public static List<MutableComponent> literalLine(long n) {
        return literalLine(String.valueOf(n));
    }

    public static boolean equals(List<? extends Component> components, String s) {
        return Objects.equals(componentsToString(components), s);
    }

    public static double toDouble(List<? extends Component> components) throws NumberFormatException {
        if (components.isEmpty()) return 0;
        if (components.size() > 1) throw new NumberFormatException(componentsToString(components));
        return Double.parseDouble(components.get(0).getString());
    }

    public static int toInt(List<? extends Component> components) throws NumberFormatException {
        if (components.isEmpty()) return 0;
        if (components.size() > 1) throw new NumberFormatException(componentsToString(components));
        return Integer.parseInt(components.get(0).getString());
    }

    public static String componentsToString(List<? extends Component> components) {
        StringBuilder out = new StringBuilder();
        if (components.isEmpty()) return out.toString();
        for (Component component : components) {
            out.append(component.getString());
            out.append('\n');
        }
        return out.substring(0, out.length() - 1);
    }

    public static void append(List<MutableComponent> components, @Nullable String s) {
        if (s != null)
            GTUtil.getLast(components).append(s);
    }

    public static void append(List<MutableComponent> components, char c) {
        append(components, String.valueOf(c));
    }

    public static void append(List<MutableComponent> components, @Nullable List<? extends Component> lines) {
        if (lines == null) return;
        if (lines.isEmpty()) return;
        for (Component line : lines) {
            GTUtil.getLast(components).append(line);
            components.add(MutableComponent.create(ComponentContents.EMPTY));
        }
        components.remove(components.size() - 1);
    }

    public static List<Component> toImmutable(List<MutableComponent> singleOrMultiLang) {
        return singleOrMultiLang.stream().map((c) -> (Component) c).toList();
    }

    public static List<MutableComponent> literalLine(double d) {
        return literalLine(String.valueOf(d));
    }

    public static String replace(String s, String regex, List<String> replacements) {
        List<String> out = new ArrayList<>();
        out.add(s);
        replacements.forEach(replacement -> out.set(0, out.get(0).replaceFirst(regex, replacement)));
        return out.get(0);
    }

    public static Component toComponent(ListTag arr) {
        MutableComponent component = Component.literal("[");
        if (arr.size() <= 5) {
            for (int i = 0; i < arr.size(); i++) {
                component.append(Component.literal('"' + arr.getString(i) + '"').withStyle(ChatFormatting.DARK_AQUA));
                if (i != arr.size() - 1) component.append(", ");
            }
        } else {
            for (int i = 0; i < 2; i++) {
                component.append(Component.literal('"' + arr.getString(i) + '"').withStyle(ChatFormatting.DARK_AQUA));
                component.append(", ");
            }
            component.append("..., ");
            for (int i = arr.size() - 2; i < arr.size(); i++) {
                component.append(Component.literal('"' + arr.getString(i) + '"').withStyle(ChatFormatting.DARK_AQUA));
                if (i != arr.size() - 1) component.append(", ");
            }
        }
        component.append("]");
        return component;
    }

    public static Component toComponent(List<Component> components) {
        MutableComponent component = Component.empty();
        for (Component comp : components) {
            component.append(comp).append("\n");
        }
        return component;
    }

    public static String formatInt(long n) {
        Map<Long, String> suffixes = Map.of(
                1L, "",
                1000L, "K",
                1000000L, "M",
                1000000000L, "B",
                1000000000000L, "T");
        long max = 1;
        for (Long i : suffixes.keySet()) {
            if (n >= i && max < i) max = i;
        }
        return "%.2f%s".formatted(((double) n) / max, suffixes.get(max));
    }
}
