package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;
import com.gregtechceu.gtceu.syncsystem.data_transformers.collections.*;

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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ValueTransformers {

    private static final Map<Class<?>, IValueTransformer<?>> REGISTERED = new ConcurrentHashMap<>();
    private static final Map<Class<?>, IValueTransformer<?>> REGISTERED_INTERFACES = new ConcurrentHashMap<>();

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

    // Logic for determining which IValueTransformer should be used to serialise a value
    private static final ClassValue<IValueTransformer<?>> TRANSFORMERS = new ClassValue<>() {

        @Override
        protected IValueTransformer<?> computeValue(@NotNull Class<?> type) {
            type = boxIfPrimitive(type);
            IValueTransformer<?> tx = REGISTERED.get(type);
            if (tx != null) return tx;
            IValueTransformer<?> ifaceTx = REGISTERED_INTERFACES.get(type);
            if (ifaceTx != null) return ifaceTx;

            if (type.isEnum()) {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) type;
                return new EnumTransformer<>(enumClass);
            }

            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                IValueTransformer<?> componentTx = get(componentType);
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

    public static IValueTransformer<?> getCollectionTransformer(Field type) {
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

    @SuppressWarnings("unchecked")
    public static <T> IValueTransformer<T> get(Class<T> type) {
        return (IValueTransformer<T>) TRANSFORMERS.get(boxIfPrimitive(type));
    }

    public static void registerClassTransformer(Class<?> type, IValueTransformer<?> transformer) {
        REGISTERED.putIfAbsent(type, transformer);
    }

    public static void registerInterfaceTransformer(Class<?> type, IValueTransformer<?> transformer) {
        REGISTERED_INTERFACES.put(type, transformer);
    }

    public static <T> IValueTransformer<T> simpleNBT(Function<T, Tag> write, Function<Tag, T> read) {
        return new IValueTransformer<>() {

            @Override
            public Tag serializeNBT(T value, ISyncManaged holder) {
                return write.apply(value);
            }

            @Override
            public T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
                return read.apply(tag);
            }
        };
    }

    static {

        registerClassTransformer(Integer.class,
                simpleNBT(IntTag::valueOf, t -> (t instanceof IntTag intTag) ? intTag.getAsInt() : 0));
        registerClassTransformer(Long.class,
                simpleNBT(LongTag::valueOf, t -> (t instanceof LongTag longTag) ? longTag.getAsLong() : 0L));
        registerClassTransformer(Float.class,
                simpleNBT(FloatTag::valueOf, t -> (t instanceof FloatTag floatTag) ? floatTag.getAsFloat() : 0f));
        registerClassTransformer(Double.class,
                simpleNBT(DoubleTag::valueOf, t -> (t instanceof DoubleTag doubleTag) ? doubleTag.getAsDouble() : 0.0));
        registerClassTransformer(Short.class, simpleNBT(ShortTag::valueOf,
                t -> (t instanceof ShortTag shortTag) ? shortTag.getAsShort() : (short) 0));
        registerClassTransformer(Byte.class,
                simpleNBT(ByteTag::valueOf, t -> (t instanceof ByteTag byteTag) ? byteTag.getAsByte() : (byte) 0));
        registerClassTransformer(Character.class, simpleNBT((v) -> IntTag.valueOf((int) v),
                t -> (t instanceof IntTag intTag) ? intTag.getAsByte() : 0x0));
        registerClassTransformer(Boolean.class,
                simpleNBT(ByteTag::valueOf, (t) -> t instanceof ByteTag byteTag && byteTag.getAsByte() != 0));

        // Primtive arrays
        registerClassTransformer(int[].class, new PrimitiveArrayTransformers.IntArrayTransformer());
        registerClassTransformer(long[].class, new PrimitiveArrayTransformers.LongArrayTransformer());
        registerClassTransformer(byte[].class, new PrimitiveArrayTransformers.ByteArrayTransformer());

        // Classes

        registerClassTransformer(String.class, simpleNBT(StringTag::valueOf,
                (t) -> (t instanceof StringTag stringTag) ? stringTag.getAsString() : ""));
        registerClassTransformer(ItemStack.class, simpleNBT(IForgeItemStack::serializeNBT,
                (t) -> (t instanceof CompoundTag compoundTag) ? ItemStack.of(compoundTag) : ItemStack.EMPTY));
        registerClassTransformer(FluidStack.class, simpleNBT((FluidStack v) -> v.writeToNBT(new CompoundTag()),
                (t) -> (t instanceof CompoundTag compoundTag) ? FluidStack.loadFluidStackFromNBT(compoundTag) :
                        FluidStack.EMPTY));
        registerClassTransformer(UUID.class, simpleNBT(NbtUtils::createUUID, NbtUtils::loadUUID));
        registerClassTransformer(BlockPos.class, simpleNBT(NbtUtils::writeBlockPos,
                (t) -> (t instanceof CompoundTag compoundTag) ? NbtUtils.readBlockPos(compoundTag) : BlockPos.ZERO));
        registerClassTransformer(CompoundTag.class, simpleNBT((CompoundTag v) -> v,
                (t) -> (t instanceof CompoundTag compoundTag) ? compoundTag : new CompoundTag()));

        registerClassTransformer(GTRecipe.class, new GTRecipeTransformer());
        registerClassTransformer(GTRecipeType.class, new GTRecipeTypeTransformer());
        registerClassTransformer(MachineRenderState.class, new MachineRenderStateTransformer());
        registerClassTransformer(Material.class, new MaterialTransformer());
        registerClassTransformer(MonitorGroup.class, new MonitorGroupTransformer());
        registerClassTransformer(CustomFluidTank.class, new CustomFluidTankTransformer());

        // Interfaces & abstract classes

        registerInterfaceTransformer(INBTSerializable.class, new NBTSerialisableTransformer());
        registerInterfaceTransformer(Component.class,
                simpleNBT((Component c) -> StringTag.valueOf(Component.Serializer.toJson(c)),
                        t -> (t instanceof StringTag stringTag) ?
                                Component.Serializer.fromJson(stringTag.getAsString()) : Component.empty()));
        registerInterfaceTransformer(CoverBehavior.class, new CoverBehaviorTransformer());

    }
}
