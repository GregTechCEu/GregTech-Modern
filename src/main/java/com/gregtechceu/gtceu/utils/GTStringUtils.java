package com.gregtechceu.gtceu.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * Returns a string with the result of the provided expression.
     * This function is intended for use with user input.
     * For example:
     * <ul>
     *     <li>{@code {"12", "+", "34"}} -> {@code "46"}</li>
     *     <li>{@code {"sqrt", "16"}} -> {@code "4"}</li>
     *     <li>{@code {"round", "4.5"}} -> {@code "5"}</li>
     *     <li>{@code {"literally any string"}} -> {@code "literally any string"}</li>
     *     <li>{@code {"~", "0"}} -> {@code "-1"} // signed bitwise inversion</li>
     * </ul>
     * Currently the operations are: {@code {"+", "-", "*", "/", "//", "%", ">>", "<<", "~", "round", "ceil", "floor", "sqrt"}}
     * @param args the arguments, including operands and operation to calculate
     * @return the result of the calculation, {@code "Invalid number!"} or {@code "Invalid expression!"}
     */
    @NotNull
    public static String calc(@NotNull List<String> args) {
        // yes I know this is terrible code, but I want to be able to do math in placeholders
        // not going to do anything crazy like including lua or python here
        if (args.size() == 3) {
            try {
                long a = Long.parseLong(args.get(0));
                long b = Long.parseLong(args.get(2));
                switch (args.get(1)) {
                    case "+":
                        return String.valueOf(a + b);
                    case "-":
                        return String.valueOf(a - b);
                    case "*":
                        return String.valueOf(a * b);
                    case "/":
                        return String.valueOf(((double) a) / b);
                    case "//":
                        return String.valueOf(a / b);
                    case "%":
                        return String.valueOf(a % b);
                    case "<<":
                        return String.valueOf(a << b);
                    case ">>":
                        return String.valueOf(a >> b);
                }
            } catch (NumberFormatException e) {
                try {
                    double a = Double.parseDouble(args.get(0));
                    double b = Double.parseDouble(args.get(2));
                    switch (args.get(1)) {
                        case "/":
                            return String.valueOf(a / b);
                        case "//":
                            return String.valueOf(((long) a) / b);
                        case "+":
                            return String.valueOf(a + b);
                        case "-":
                            return String.valueOf(a - b);
                        case "*":
                            return String.valueOf(a * b);
                    }
                } catch (NumberFormatException ex) {
                    return "Invalid number!";
                }
            }
        } else if (args.size() == 2) {
            try {
                long a = Long.parseLong(args.get(1));
                switch (args.get(0)) {
                    case "~":
                        return String.valueOf(~a);
                    case "sqrt":
                        return String.valueOf(Math.sqrt(a));
                }
            } catch (NumberFormatException e) {
                try {
                    double a = Double.parseDouble(args.get(1));
                    switch (args.get(0)) {
                        case "round":
                            return String.valueOf(Math.round(a));
                        case "ceil":
                            return String.valueOf(Math.ceil(a));
                        case "floor":
                            return String.valueOf(Math.floor(a));
                        case "sqrt":
                            return String.valueOf(Math.sqrt(a));
                    }
                } catch (NumberFormatException e2) {
                    return "Invalid number!";
                }
            }
        } else if (args.size() == 1) return args.get(0);
        return "Invalid expression!";
    }
}
