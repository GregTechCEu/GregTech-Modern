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
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.data_transformers.collections.*;
import com.gregtechceu.gtceu.syncsystem.data_transformers.gtceu.*;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ValueTransformers {

    private static final Map<Class<?>, ValueTransformer<?>> REGISTERED = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ValueTransformer<?>> REGISTERED_INTERFACES = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class);

    public static Class<?> boxIfPrimitive(Class<?> cls) {
        return cls.isPrimitive() ? PRIMITIVE_TO_BOXED.get(cls) : cls;
    }

    // Logic for determining which ValueTransformer should be used to serialise a value
    private static final ClassValue<ValueTransformer<?>> TRANSFORMERS = new ClassValue<>() {

        @Override
        protected ValueTransformer<?> computeValue(@NotNull Class<?> type) {
            type = boxIfPrimitive(type);
            ValueTransformer<?> tx = REGISTERED.get(type);
            if (tx != null) return tx;
            ValueTransformer<?> ifaceTx = REGISTERED_INTERFACES.get(type);
            if (ifaceTx != null) return ifaceTx;

            if (type.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) type;
                return new EnumTransformer<>(enumClass);
            }

            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                ValueTransformer<?> componentTx = get(componentType);
                if (componentTx != null) return new ObjectArrayTransformer<>(componentTx);
            }

            for (var ifaceEntry : REGISTERED_INTERFACES.entrySet()) {
                if (ifaceEntry.getKey().isAssignableFrom(type)) return ifaceEntry.getValue();
            }

            if (!ISyncManaged.class.isAssignableFrom(type)) throw new IllegalStateException(
                    "No value transformer for sync object type: %s".formatted(type.getCanonicalName()));
            else return null;
        }
    };

    public static ValueTransformer<?> getCollectionTransformer(Field type) {
        Class<?> collectionType = type.getType();
        if (!Collection.class.isAssignableFrom(collectionType)) return null;
        if (type.getGenericType() instanceof ParameterizedType ptype) {
            Type[] actualTypes = ptype.getActualTypeArguments();
            Type keyType = actualTypes[0];
            Type valueType = actualTypes.length > 1 ? actualTypes[1] : null;
            if (List.class.isAssignableFrom(collectionType)) {
                if (keyType instanceof Class<?> keyClass) {
                    if (ISyncManaged.class.isAssignableFrom(keyClass))
                        throw new IllegalArgumentException("Cannot sync collection of ISyncManaged objects");
                    return new ListTransformer<>(ValueTransformers.get(keyClass));
                }
            } else if (Set.class.isAssignableFrom(collectionType)) {
                if (keyType instanceof Class<?> keyClass) {
                    if (ISyncManaged.class.isAssignableFrom(keyClass))
                        throw new IllegalArgumentException("Cannot sync collection of ISyncManaged objects");
                    return new SetTransformer<>(ValueTransformers.get(keyClass));
                }
            } else if (Map.class.isAssignableFrom(collectionType)) {
                if (keyType instanceof Class<?> keyClass && valueType instanceof Class<?> valueClass) {
                    if (ISyncManaged.class.isAssignableFrom(keyClass) ||
                            ISyncManaged.class.isAssignableFrom(valueClass))
                        throw new IllegalArgumentException("Cannot sync collection of ISyncManaged objects");

                    return new MapTransformer<>(ValueTransformers.get(keyClass), ValueTransformers.get(valueClass));
                }
            }
        }
        return null;
    }

    public static ValueTransformer<?> getForField(Field field) {
        ValueTransformer<?> collectionTransformer = getCollectionTransformer(field);
        if (collectionTransformer == null) {
            return TRANSFORMERS.get(boxIfPrimitive(field.getType()));
        } else {
            return collectionTransformer;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ValueTransformer<T> get(Class<T> type) {
        return (ValueTransformer<T>) TRANSFORMERS.get(boxIfPrimitive(type));
    }

    public static <T> void registerClassTransformer(Class<T> type, ValueTransformer<T> transformer) {
        REGISTERED.putIfAbsent(type, transformer);
    }

    public static void registerInterfaceTransformer(Class<?> type, ValueTransformer<?> transformer) {
        REGISTERED_INTERFACES.put(type, transformer);
    }

    public static <T,
            TagType extends Tag> void registerSimpleClassTransformer(Class<T> type, Function<T, TagType> write,
                                                                     Function<TagType, T> read, Class<TagType> tagClass,
                                                                     Supplier<T> defaultSupplier) {
        REGISTERED.putIfAbsent(type, simpleNBT(write, read, tagClass, defaultSupplier));
    }

    public static <T, TagType extends Tag> void registerSimpleInterfaceTransformer(Class<T> type,
                                                                                   Function<T, TagType> write,
                                                                                   Function<TagType, T> read,
                                                                                   Class<TagType> tagClass,
                                                                                   Supplier<T> defaultSupplier) {
        REGISTERED_INTERFACES.putIfAbsent(type, simpleNBT(write, read, tagClass, defaultSupplier));
    }

    private static <T,
            TagType extends Tag> ValueTransformer<T> simpleNBT(Function<T, TagType> write, Function<TagType, T> read,
                                                               Class<TagType> tagClass, Supplier<T> defaultSupplier) {
        return new ValueTransformer<>() {

            @Override
            public Tag serializeNBT(T value, ISyncManaged holder) {
                return write.apply(value);
            }

            @Override
            public T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
                if (tagClass.isAssignableFrom(tag.getClass())) {
                    return read.apply(tagClass.cast(tag));
                }
                return defaultSupplier.get();
            }
        };
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

        registerInterfaceTransformer(INBTSerializable.class, new NBTSerialisableTransformer());
        registerSimpleInterfaceTransformer(Component.class, (c) -> StringTag.valueOf(Component.Serializer.toJson(c)),
                t -> Component.Serializer.fromJson(t.getAsString()), StringTag.class, Component::empty);

        //// GT specific classes

        registerClassTransformer(GTRecipe.class, new GTRecipeTransformer());
        registerClassTransformer(MachineRenderState.class, new CodecTransformer<>(MachineRenderState.CODEC));
        registerClassTransformer(GTRecipeType.class, new ResourceLocationReferenceTransformer<>(
                GTRecipeType::getRegistryName, GTRegistries.RECIPE_TYPES::get));
        registerClassTransformer(Material.class, new ResourceLocationReferenceTransformer<>(
                Material::getResourceLocation, GTCEuAPI.materialManager::getMaterial));
        registerClassTransformer(MonitorGroup.class, new MonitorGroupTransformer());
        registerClassTransformer(CustomFluidTank.class, new CustomFluidTankTransformer());

        registerInterfaceTransformer(CoverBehavior.class, new CoverBehaviorTransformer());
    }
}
