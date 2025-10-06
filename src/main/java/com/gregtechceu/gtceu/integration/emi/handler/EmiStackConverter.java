package com.gregtechceu.gtceu.integration.emi.handler;

import com.gregtechceu.gtceu.integration.xei.entry.EntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidTagList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemStackList;
import com.gregtechceu.gtceu.integration.xei.entry.item.ItemTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom EmiStack -> vanilla/forge/mod stack converters
 */
public class EmiStackConverter {

    public static final Map<Class<?>, Converter<?>> CONVERTERS = new Reference2ReferenceOpenHashMap<>();

    public static final Converter<ItemStack> ITEM = register(ItemStack.class, new Converter<>() {

        @Override
        public @Nullable ItemStack convertFrom(EmiStack stack) {
            Item key = stack.getKeyOfType(Item.class);
            if (key == null || key == Items.AIR) {
                return null;
            }
            ItemStack itemStack = new ItemStack(key, GTMath.saturatedCast(stack.getAmount()));
            itemStack.setTag(stack.getNbt());
            return itemStack;
        }

        private static EmiIngredient toEMIIngredient(Stream<ItemStack> stream) {
            return EmiIngredient.of(stream.map(EmiStack::of).toList());
        }

        @Override
        public @NotNull EmiIngredient convertTo(EntryList<ItemStack> stack, float chance,
                                                UnaryOperator<ItemStack> mapper) {
            if (stack == null || stack.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (stack instanceof ItemStackList stackList) {
                return toEMIIngredient(stackList.stream()).setChance(chance);
            } else if (stack instanceof ItemTagList tagList) {
                return EmiIngredient.of(tagList.getEntries().stream()
                        .map(ItemTagList.ItemTagEntry::stacks)
                        .map(stream -> toEMIIngredient(stream))
                        .collect(Collectors.toList()), tagList.getEntries().get(0).amount()).setChance(chance);
            }
            return EmiStack.EMPTY;
        }
    });
    public static final Converter<FluidStack> FLUID = register(FluidStack.class, new Converter<>() {

        @Override
        public @Nullable FluidStack convertFrom(EmiStack stack) {
            Fluid key = stack.getKeyOfType(Fluid.class);
            if (key == null || key == Fluids.EMPTY) {
                return null;
            }
            return new FluidStack(key, GTMath.saturatedCast(stack.getAmount()), stack.getNbt());
        }

        private static EmiIngredient toEMIIngredient(Stream<FluidStack> stream) {
            return EmiIngredient.of(stream.map(ForgeEmiStack::of).toList());
        }

        @Override
        public @NotNull EmiIngredient convertTo(EntryList<FluidStack> stack, float chance,
                                                UnaryOperator<FluidStack> mapper) {
            if (stack == null || stack.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (stack instanceof FluidStackList stackList) {
                return toEMIIngredient(stackList.stream().map(mapper)).setChance(chance);
            } else if (stack instanceof FluidTagList tagList) {
                return EmiIngredient.of(tagList.getEntries().stream()
                        .map(FluidTagList.FluidTagEntry::stacks)
                        .map(stream -> toEMIIngredient(stream))
                        .collect(Collectors.toList()), tagList.getEntries().get(0).amount()).setChance(chance);
            }
            return EmiStack.EMPTY;
        }
    });

    public static <T> Converter<T> register(Class<T> clazz, Converter<T> converter) {
        CONVERTERS.put(clazz, converter);
        return converter;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> Converter<T> getForNullable(Class<T> clazz) {
        return (Converter<T>) CONVERTERS.get(clazz);
    }

    @NotNull
    public static <T> Optional<Converter<T>> getFor(Class<T> clazz) {
        return Optional.ofNullable(getForNullable(clazz));
    }

    public interface Converter<T> {

        @Nullable
        T convertFrom(EmiStack stack);

        @NotNull
        EmiIngredient convertTo(EntryList<T> stack, float chance, UnaryOperator<T> mapper);

        @NotNull
        default EmiIngredient convertTo(IngredientProvider<T> slot) {
            return this.convertTo(slot.getIngredients(), slot.chance(), slot.renderMappingFunction());
        }
    }
}
