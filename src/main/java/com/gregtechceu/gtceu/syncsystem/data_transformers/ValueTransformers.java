package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.syncsystem.data_transformers.collections.*;
import com.gregtechceu.gtceu.syncsystem.data_transformers.gtceu.*;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ValueTransformers {

    private static final Map<Class<?>, ValueTransformer<?>> REGISTERED = new Object2ObjectOpenHashMap<>();
    private static final Map<Class<?>, Supplier<ValueTransformer<?>>> REGISTERED_SUPPLIERS = new Object2ObjectOpenHashMap<>();
    private static final Map<Type, Type> PRIMITIVE_TO_BOXED = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class);

    private static final Map<Type, ValueTransformer<?>> TYPE_CACHE = new Object2ObjectOpenHashMap<>();

    /**
     * Gets the {@link ValueTransformer} associated with a specific type.
     */
    public static ValueTransformer<?> get(Type type) {
        if (type instanceof Class<?> cls) type = cls.isPrimitive() ? PRIMITIVE_TO_BOXED.get(cls) : cls;
        TYPE_CACHE.computeIfAbsent(type, ValueTransformers::generateOrGetTransformer);
        throw new IllegalStateException(
                "Failed to find value transformer for sync object with type: %s".formatted(type));
    }

    private static ValueTransformer<?> generateOrGetTransformer(Type type) {
        Class<?> clazz;
        if (type instanceof ParameterizedType pType) {
            clazz = (Class<?>) pType.getRawType();
        } else {
            clazz = (Class<?>) type;
        }

        if (REGISTERED.containsKey(clazz)) return REGISTERED.get(clazz);

        if (clazz.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
            return new EnumTransformer<>(enumClass);
        }

        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            ValueTransformer<?> componentTx = get(componentType);
            if (componentTx != null) return new ObjectArrayTransformer<>(componentTx);
        }

        for (var entry : REGISTERED_SUPPLIERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) return entry.getValue().get();
        }

        for (var entry : REGISTERED.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) return entry.getValue();
        }

        return null;
    }

    /**
     * Registers a {@link ValueTransformer} for the given class or interface.
     * If registering a type with generic arguments, instead use {@code registerTransformerSupplier} to create a new
     * transformer instance for each set of generic type arguments.
     * 
     * @param type        The class to register this {@link ValueTransformer} for
     * @param transformer The transformer being registered
     */
    public static void registerTransformer(Class<?> type, ValueTransformer<?> transformer) {
        REGISTERED.putIfAbsent(type, transformer);
    }

    /**
     * Creates and registers a {@link ValueTransformer} for the given class using predefined NBT parsing functions.
     * 
     * @param type            The class to register this {@link ValueTransformer} for
     * @param write           A function that writes the value into a specific tag type
     * @param read            A function that reads the value from a specific tag type
     * @param tagClass        The tag type the value is serialized into
     * @param defaultSupplier A supplier that returns the default value if the tag is of an unexpected type.
     */
    public static <T,
            TagType extends Tag> void registerSimpleClassTransformer(Class<T> type, Function<T, TagType> write,
                                                                     Function<TagType, T> read, Class<TagType> tagClass,
                                                                     Supplier<T> defaultSupplier) {
        ValueTransformer<T> transformer = new ValueTransformer<>() {

            @Override
            public @NotNull Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
                return write.apply(value);
            }

            @Override
            public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
                if (tagClass.isAssignableFrom(tag.getClass())) {
                    return read.apply(tagClass.cast(tag));
                }
                return defaultSupplier.get();
            }
        };
        REGISTERED.putIfAbsent(type, transformer);
    }

    /**
     * Registers a supplier that supplies instances of a specific transformer type.
     * The supplier will be called to create new instances of the transformer for each unique set of generic type
     * arguments passed to the given class.
     *
     * @param type The class to register this {@link ValueTransformer} supplier for
     * @param func Supplier function
     */
    public static <T> void registerTransformerSupplier(Class<T> type, Supplier<ValueTransformer<?>> func) {
        REGISTERED_SUPPLIERS.put(type, func);
    }

    static {

        //// Primitives

        registerSimpleClassTransformer(Integer.class, IntTag::valueOf, IntTag::getAsInt, IntTag.class, () -> 0);
        registerSimpleClassTransformer(Long.class, LongTag::valueOf, LongTag::getAsLong, LongTag.class, () -> 0L);
        registerSimpleClassTransformer(Float.class, FloatTag::valueOf, FloatTag::getAsFloat, FloatTag.class, () -> 0f);
        registerSimpleClassTransformer(Double.class, DoubleTag::valueOf, DoubleTag::getAsDouble, DoubleTag.class,
                () -> 0.0);
        registerSimpleClassTransformer(Short.class, ShortTag::valueOf, ShortTag::getAsShort, ShortTag.class,
                () -> (short) 0);
        registerSimpleClassTransformer(Byte.class, ByteTag::valueOf, ByteTag::getAsByte, ByteTag.class, () -> (byte) 0);
        registerSimpleClassTransformer(Character.class, (b) -> IntTag.valueOf(b), (t) -> (char) t.getAsInt(),
                IntTag.class, () -> (char) 0);
        registerSimpleClassTransformer(Boolean.class, ByteTag::valueOf, (b) -> b.getAsByte() != 0, ByteTag.class,
                () -> false);

        // Primtive arrays
        registerSimpleClassTransformer(int[].class, IntArrayTag::new, IntArrayTag::getAsIntArray, IntArrayTag.class,
                () -> new int[0]);
        registerSimpleClassTransformer(long[].class, LongArrayTag::new, LongArrayTag::getAsLongArray,
                LongArrayTag.class, () -> new long[0]);
        registerSimpleClassTransformer(byte[].class, ByteArrayTag::new, ByteArrayTag::getAsByteArray,
                ByteArrayTag.class, () -> new byte[0]);

        //// Java classes and standard minecraft/forge classes

        registerSimpleClassTransformer(String.class, StringTag::valueOf, StringTag::getAsString, StringTag.class,
                () -> "");
        registerSimpleClassTransformer(ItemStack.class, IForgeItemStack::serializeNBT, ItemStack::of, CompoundTag.class,
                () -> ItemStack.EMPTY);
        registerSimpleClassTransformer(FluidStack.class, (v) -> v.writeToNBT(new CompoundTag()),
                FluidStack::loadFluidStackFromNBT, CompoundTag.class, () -> FluidStack.EMPTY);

        // The default value supplier will never be called as NbtUtils::loadUUID will throw if the UUID is invalid.
        registerSimpleClassTransformer(UUID.class, NbtUtils::createUUID, NbtUtils::loadUUID, IntArrayTag.class,
                UUID::randomUUID);

        registerSimpleClassTransformer(BlockPos.class, NbtUtils::writeBlockPos, NbtUtils::readBlockPos,
                CompoundTag.class, () -> BlockPos.ZERO);
        registerSimpleClassTransformer(CompoundTag.class, (v) -> v, (v) -> v, CompoundTag.class, CompoundTag::new);

        registerSimpleClassTransformer(Component.class, (c) -> StringTag.valueOf(Component.Serializer.toJson(c)),
                t -> Component.Serializer.fromJson(t.getAsString()), StringTag.class, Component::empty);

        registerTransformer(INBTSerializable.class, new NBTSerialisableTransformer());

        registerTransformerSupplier(List.class, ListTransformer::new);
        registerTransformerSupplier(Map.class, MapTransformer::new);
        registerTransformerSupplier(Set.class, SetTransformer::new);

        //// GT specific classes

        registerTransformer(GTRecipe.class, new GTRecipeTransformer());
        registerTransformer(MachineRenderState.class, new CodecTransformer<>(MachineRenderState.CODEC));
        registerTransformer(GTRecipeType.class, new ResourceLocationReferenceTransformer<>(
                GTRecipeType::getRegistryName, GTRegistries.RECIPE_TYPES::get));
        registerTransformer(Material.class, new ResourceLocationReferenceTransformer<>(
                Material::getResourceLocation, GTCEuAPI.materialManager::getMaterial));
        registerTransformer(MonitorGroup.class, new MonitorGroupTransformer());
        registerTransformer(CustomFluidTank.class, new CustomFluidTankTransformer());

        registerTransformer(CoverBehavior.class, new CoverBehaviorTransformer());
    }
}
