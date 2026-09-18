package brachy.modularui.integration.emi;

import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidTagList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemTagList;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.utils.math.MathUtils;

import dev.emi.emi.api.neoforge.NeoForgeEmiStack;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom {@link EmiStack} <-> vanilla/neoforge/mod stack converters
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
            ItemStack itemStack = new ItemStack(key, MathUtils.saturatedCast(stack.getAmount()));
            itemStack.applyComponents(stack.getComponentChanges());
            return itemStack;
        }

        private static EmiIngredient toEMIIngredient(Stream<ItemStack> stream) {
            return EmiIngredient.of(stream.map(EmiStack::of).toList());
        }

        @Override
        public EmiIngredient convertTo(EntryList<ItemStack> stack, float chance) {
            if (stack.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (stack instanceof ItemStackList stackList) {
                return toEMIIngredient(stackList.stream()).setChance(chance);
            } else if (stack instanceof ItemTagList tagList) {
                return EmiIngredient.of(tagList.getEntries().stream()
                        .map(ItemTagList.ItemTagEntry::stacks)
                        .map(stream -> toEMIIngredient(stream))
                        .collect(Collectors.toList()), tagList.getEntries().getFirst().amount()).setChance(chance);
            }
            return EmiStack.EMPTY;
        }
    });
    public static final Converter<FluidStack> FLUID = register(FluidStack.class, new Converter<>() {

        @Override
        public @Nullable FluidStack convertFrom(EmiStack stack) {
            Fluid key = stack.getKeyOfType(Fluid.class);
            if (key != null && key != Fluids.EMPTY) {
                return new FluidStack(key.builtInRegistryHolder(), MathUtils.saturatedCast(stack.getAmount()), stack.getComponentChanges());
            }
            var item = ITEM.convertFrom(stack);
            if (item != null) {
                IFluidHandlerItem fluidHandler = item.getCapability(Capabilities.FluidHandler.ITEM);
                if(fluidHandler != null && fluidHandler.getTanks() == 1) {
                    return fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                }
            }
            return null;
        }

        private static EmiIngredient toEMIIngredient(Stream<FluidStack> stream) {
            return EmiIngredient.of(stream.map(NeoForgeEmiStack::of).toList());
        }

        @Override
        public EmiIngredient convertTo(EntryList<FluidStack> stack, float chance) {
            if (stack.isEmpty()) {
                return EmiStack.EMPTY;
            }
            if (stack instanceof FluidStackList stackList) {
                return toEMIIngredient(stackList.stream()).setChance(chance);
            } else if (stack instanceof FluidTagList tagList) {
                return EmiIngredient.of(tagList.getEntries().stream()
                        .map(FluidTagList.FluidTagEntry::stacks)
                        .map(stream -> toEMIIngredient(stream))
                        .collect(Collectors.toList()), tagList.getEntries().getFirst().amount()).setChance(chance);
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

    public static <T> Optional<Converter<T>> getFor(Class<T> clazz) {
        return Optional.ofNullable(getForNullable(clazz));
    }

    public interface Converter<T> {

        @Nullable
        T convertFrom(EmiStack stack);

        EmiIngredient convertTo(EntryList<T> stack, float chance);

        default EmiIngredient convertTo(IngredientProvider<T> slot) {
            return this.convertTo(slot.getIngredients(), slot.chance());
        }
    }
}
