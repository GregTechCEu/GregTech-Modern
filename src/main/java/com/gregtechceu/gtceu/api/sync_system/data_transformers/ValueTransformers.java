package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.ConsumedInputsData;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.TypeDeclaration;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.collections.*;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu.*;
import com.gregtechceu.gtceu.api.sync_system.managed.ISyncManaged;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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
     * If registering a type with generic arguments, instead use {@code registerGenericTransformerSupplier} to create a
     * new
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

    public static <T> void registerCodecTransformer(Class<T> type, Codec<T> codec,
                                                    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        registerTransformer(type, new CodecTransformer<>(codec, streamCodec));
    }

    /**
     * Registers a supplier that supplies instances of a specific transformer type.
     * The supplier will be called to create new instances of the transformer for each unique set of generic type
     * arguments passed to the given class.
     *
     * @param type The class to register this {@link ValueTransformer} supplier for
     * @param func Supplier function
     */
    public static <T> void registerGenericTransformerSupplier(Class<T> type, Supplier<ValueTransformer<?>> func) {
        if (REGISTERED_SUPPLIERS.containsKey(type))
            throw new IllegalArgumentException("Attempted to register transformer for %s twice".formatted(type));
        REGISTERED_SUPPLIERS.put(type, func);
    }

    static {

        //// Primitives

        // spotless:off

        registerCodecTransformer(Integer.class, Codec.INT, ByteBufCodecs.VAR_INT);
        registerCodecTransformer(Long.class, Codec.LONG, ByteBufCodecs.VAR_LONG);
        registerCodecTransformer(Float.class, Codec.FLOAT, ByteBufCodecs.FLOAT);
        registerCodecTransformer(Double.class, Codec.DOUBLE, ByteBufCodecs.DOUBLE);
        registerCodecTransformer(Short.class, Codec.SHORT, ByteBufCodecs.SHORT);
        registerCodecTransformer(Byte.class, Codec.BYTE, ByteBufCodecs.BYTE);
        registerCodecTransformer(Character.class, SyncSystemCodecs.CHAR, SyncSystemCodecs.CHAR_STREAM);
        registerCodecTransformer(Boolean.class, Codec.BOOL, ByteBufCodecs.BOOL);

        // Primtive arrays
        registerCodecTransformer(int[].class, Codec.INT_STREAM.xmap(IntStream::toArray, IntStream::of), SyncSystemCodecs.INT_ARRAY_STREAM);
        registerCodecTransformer(long[].class, Codec.LONG_STREAM.xmap(LongStream::toArray, LongStream::of), SyncSystemCodecs.LONG_ARRAY_STREAM);
        registerCodecTransformer(byte[].class, Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap), ByteBufCodecs.BYTE_ARRAY);

        //// Java classes and standard minecraft/forge classes

        registerCodecTransformer(String.class, Codec.STRING, ByteBufCodecs.STRING_UTF8);
        registerCodecTransformer(UUID.class, UUIDUtil.CODEC, UUIDUtil.STREAM_CODEC);
        registerCodecTransformer(CompoundTag.class, CompoundTag.CODEC, ByteBufCodecs.COMPOUND_TAG);

        registerCodecTransformer(ItemStack.class, ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC);
        registerCodecTransformer(FluidStack.class, FluidStack.OPTIONAL_CODEC, FluidStack.OPTIONAL_STREAM_CODEC);
        registerCodecTransformer(Component.class, ComponentSerialization.CODEC, ComponentSerialization.STREAM_CODEC);

        registerCodecTransformer(BlockPos.class, BlockPos.CODEC, BlockPos.STREAM_CODEC);
        registerCodecTransformer(BlockState.class, BlockState.CODEC, ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY));

        registerTransformer(INBTSerializable.class, new NBTSerializableTransformer());
        registerTransformer(ISyncManaged.class, new SyncDataHolder.SyncManagedTransformer());

        registerGenericTransformerSupplier(List.class, ListTransformer::new);
        registerGenericTransformerSupplier(Map.class, MapTransformer::new);
        registerGenericTransformerSupplier(Set.class, SetTransformer::new);

        //// GT specific classes

        registerTransformer(CoverBehavior.class, new CoverBehaviorTransformer());
        registerTransformer(GTRecipe.class, new GTRecipeTransformer());

        registerCodecTransformer(MachineRenderState.class, MachineRenderState.CODEC, ByteBufCodecs.idMapper(MachineDefinition.RENDER_STATE_REGISTRY));
        registerCodecTransformer(MonitorGroup.class, MonitorGroup.CODEC, MonitorGroup.STREAM_CODEC);
        registerCodecTransformer(ConsumedInputsData.class, ConsumedInputsData.CODEC, ByteBufCodecs.fromCodecWithRegistries(ConsumedInputsData.CODEC));

        registerTransformer(GTRecipeType.class, new RegistryReferenceTransformer<>(GTRegistries.Keys.RECIPE_TYPE, GTRecipeType::getRegistryName));
        registerTransformer(Material.class, new RegistryReferenceTransformer<>(GTRegistries.Keys.MATERIAL, Material::getResourceLocation));

        // spotless:on
    }
}
