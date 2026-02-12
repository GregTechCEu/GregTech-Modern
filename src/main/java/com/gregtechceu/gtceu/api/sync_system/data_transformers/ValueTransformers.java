package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sync_system.TypeDeclaration;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.collections.ListTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.collections.MapTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.collections.ObjectArrayTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.collections.SetTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu.CoverBehaviorTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu.GTRecipeTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu.MonitorGroupTransformer;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ValueTransformers {

    private static final Map<Class<?>, ValueTransformer<?>> REGISTERED = new Reference2ReferenceOpenHashMap<>();
    private static final Map<Class<?>, Supplier<ValueTransformer<?>>> REGISTERED_SUPPLIERS = new Reference2ReferenceOpenHashMap<>();

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

    private static final Map<Type, ValueTransformer<?>> TYPE_CACHE = new Reference2ReferenceOpenHashMap<>();

    /**
     * Gets the {@link ValueTransformer} associated with a specific type.
     */
    public static @Nullable ValueTransformer<?> get(Type type) {
        if (type instanceof Class<?> cls) type = cls.isPrimitive() ? PRIMITIVE_TO_BOXED.get(cls) : cls;
        return TYPE_CACHE.computeIfAbsent(type, ValueTransformers::generateOrGetTransformer);
    }

    private static @Nullable ValueTransformer<?> generateOrGetTransformer(Type type) {
        TypeDeclaration declaration = new TypeDeclaration(type);
        Class<?> clazz = declaration.getClassValue();

        if (clazz != null && REGISTERED.containsKey(clazz)) return REGISTERED.get(clazz);

        if (clazz == null || clazz.isArray()) {
            ValueTransformer<?> componentTx = get(declaration.getArrayComponentType().getRawType());
            if (componentTx != null) return new ObjectArrayTransformer<>(componentTx);
            return null;
        }

        if (clazz.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
            return new EnumTransformer<>(enumClass);
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
        if (REGISTERED.containsKey(type))
            throw new IllegalArgumentException("Attempted to register transformer for %s twice".formatted(type));
        REGISTERED.put(type, transformer);
    }

    /**
     * Creates and registers a {@link ValueTransformer} for the given class using predefined NBT parsing functions.
     *
     * @param type     The class to register this {@link ValueTransformer} for
     * @param write    A function that writes the value into a specific tag type
     * @param read     A function that reads the value from a specific tag type
     * @param tagClass The tag type the value is serialized into
     */
    public static <T,
            TagType extends Tag> void registerSimpleClassTransformer(Class<T> type, Function<T, TagType> write,
                                                                     Function<TagType, T> read,
                                                                     Class<TagType> tagClass) {
        if (REGISTERED.containsKey(type))
            throw new IllegalArgumentException("Attempted to register transformer for %s twice".formatted(type));
        ValueTransformer<T> transformer = new SimpleClassTransformer<>(write, read, tagClass);
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
        if (REGISTERED_SUPPLIERS.containsKey(type))
            throw new IllegalArgumentException("Attempted to register transformer for %s twice".formatted(type));
        REGISTERED_SUPPLIERS.put(type, func);
    }

    static {

        //// Primitives

        registerSimpleClassTransformer(Integer.class, IntTag::valueOf, IntTag::getAsInt, IntTag.class);
        registerSimpleClassTransformer(Long.class, LongTag::valueOf, LongTag::getAsLong, LongTag.class);
        registerSimpleClassTransformer(Float.class, FloatTag::valueOf, FloatTag::getAsFloat, FloatTag.class);
        registerSimpleClassTransformer(Double.class, DoubleTag::valueOf, DoubleTag::getAsDouble, DoubleTag.class);
        registerSimpleClassTransformer(Short.class, ShortTag::valueOf, ShortTag::getAsShort, ShortTag.class);
        registerSimpleClassTransformer(Byte.class, ByteTag::valueOf, ByteTag::getAsByte, ByteTag.class);
        registerSimpleClassTransformer(Character.class, (b) -> IntTag.valueOf(b), (t) -> (char) t.getAsInt(),
                IntTag.class);
        registerSimpleClassTransformer(Boolean.class, ByteTag::valueOf, (b) -> b.getAsByte() != 0, ByteTag.class);

        // Primtive arrays
        registerSimpleClassTransformer(int[].class, IntArrayTag::new, IntArrayTag::getAsIntArray, IntArrayTag.class);
        registerSimpleClassTransformer(long[].class, LongArrayTag::new, LongArrayTag::getAsLongArray,
                LongArrayTag.class);
        registerSimpleClassTransformer(byte[].class, ByteArrayTag::new, ByteArrayTag::getAsByteArray,
                ByteArrayTag.class);

        //// Java classes and standard minecraft/forge classes

        registerSimpleClassTransformer(String.class, StringTag::valueOf, StringTag::getAsString, StringTag.class);
        registerSimpleClassTransformer(ItemStack.class, IForgeItemStack::serializeNBT, ItemStack::of,
                CompoundTag.class);
        registerSimpleClassTransformer(FluidStack.class, (v) -> v.writeToNBT(new CompoundTag()),
                FluidStack::loadFluidStackFromNBT, CompoundTag.class);

        // The default value supplier will never be called as NbtUtils::loadUUID will throw if the UUID is invalid.
        registerSimpleClassTransformer(UUID.class, NbtUtils::createUUID, NbtUtils::loadUUID, IntArrayTag.class);

        registerSimpleClassTransformer(BlockPos.class, NbtUtils::writeBlockPos, NbtUtils::readBlockPos,
                CompoundTag.class);
        registerSimpleClassTransformer(CompoundTag.class, (v) -> v, (v) -> v, CompoundTag.class);

        registerSimpleClassTransformer(Component.class, (c) -> StringTag.valueOf(Component.Serializer.toJson(c)),
                t -> {
                    var comp = Component.Serializer.fromJson(t.getAsString());
                    return comp == null ? Component.empty() : comp;
                }, StringTag.class);

        registerTransformer(INBTSerializable.class, new NBTSerializableTransformer());

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

        registerTransformer(CoverBehavior.class, new CoverBehaviorTransformer());
    }
}
