package com.gregtechceu.gtceu.api.registry.registrate.holder;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.datafixers.util.Either;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HolderRegistryEntry<T> extends RegistryEntry<T> implements Holder<T> {

    @ApiStatus.Internal
    @Getter
    private final AbstractRegistrate<?> owner;
    @ApiStatus.Internal
    @Getter
    private final RegistryObject<T> delegate;

    public HolderRegistryEntry(AbstractRegistrate<?> owner, RegistryObject<T> delegate) {
        super(owner, delegate);
        this.owner = owner;
        this.delegate = delegate;
    }

    /**
     * Gets the object stored by this DeferredHolder, if this holder {@linkplain #isBound() is bound}.
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    @Override
    public T value() {
        return this.get();
    }

    /**
     * Add a callback to be invoked when this entry is registered. Can be called multiple times to add multiple callbacks.
     *
     * @param callback the callback to invoke
     * @return this
     */
    public HolderRegistryEntry<T> onRegister(NonNullConsumer<? super T> callback) {
        getOwner().addRegisterCallback(getId().getPath(), ResourceKey.createRegistryKey(getKey().registry()), callback);
        return this;
    }

    /**
     * {@return an optional containing the target object, if {@link #isBound() bound}; otherwise {@linkplain Optional#empty() an empty optional}}
     */
    public Optional<T> asOptional() {
        return isBound() ? Optional.of(get()) : Optional.empty();
    }

    /**
     * {@return the registry that this DeferredHolder is pointing at, or {@code null} if it doesn't exist}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected Registry<T> getRegistry() {
        return (Registry<T>) BuiltInRegistries.REGISTRY.get(this.getKey().registry());
    }

    /**
     * Binds this DeferredHolder to the underlying registry and target object.
     *
     * <p>Has no effect if already bound.
     *
     * @param throwOnMissingRegistry If true, an exception will be thrown if the registry is absent.
     * @throws IllegalStateException If throwOnMissingRegistry is true and the backing registry is unavailable.
     */
    protected final void bind(boolean throwOnMissingRegistry) {
        if (this.getHolder().isPresent()) return;

        Registry<T> registry = getRegistry();
        if (registry != null) {
            updateReference(registry);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.getKey().registry());
        }
    }

    // safe, reference holders should always have a key
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        return obj instanceof Holder<?> h && h.kind() == Kind.REFERENCE && h.unwrapKey().get() == this.getKey();
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "HolderRegistryEntry{%s}", this.getKey());
    }

    /**
     * {@return true if the underlying object is available}
     *
     * <p>
     * If {@code true}, the underlying object was added to the registry, and {@link #value()} or {@link #get()} can be
     * called.
     */
    @Override
    public boolean isBound() {
        bind(false);
        return this.getHolder().isPresent() && this.getHolder().get().isBound();
    }

    /**
     * {@return true if the passed ResourceLocation is the same as the ID of the target object}
     */
    @Override
    public boolean is(ResourceLocation id) {
        return id.equals(this.getKey().location());
    }

    /**
     * {@return true if the passed ResourceKey is the same as this holder's resource key}
     */
    @Override
    public boolean is(ResourceKey<T> key) {
        return key == this.getKey();
    }

    /**
     * Evaluates the passed predicate against this holder's resource key.
     *
     * @return {@code true} if the filter matches {@linkplain #getKey() this DH's resource key}
     */
    @Override
    public boolean is(Predicate<ResourceKey<T>> filter) {
        return filter.test(this.getKey());
    }

    /**
     * {@return true if this holder is a member of the passed tag}
     */
    @Override
    public boolean is(TagKey<T> tag) {
        bind(false);
        return this.getHolder().isPresent() && this.getHolder().get().is(tag);
    }

    /**
     * {@return all tags present on the underlying object}
     *
     * <p>If the underlying object is not {@linkplain #isBound() bound} yet, and empty stream is returned.
     */
    @Override
    public Stream<TagKey<T>> tags() {
        bind(false);
        return this.getHolder().isPresent() ? this.getHolder().get().tags() : Stream.empty();
    }

    /**
     * Returns an {@link Either#left()} containing {@linkplain #getKey() the resource key of this holder}.
     *
     * @apiNote This method is implemented for {@link Holder} compatibility, but {@link #getKey()} should be preferred.
     */
    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        // Holder.Reference always returns the key, do the same here.
        return Either.left(this.getKey());
    }

    /**
     * Returns the resource key of this holder.
     *
     * @return a present optional containing {@linkplain #getKey() the resource key of this holder}
     * @apiNote This method is implemented for {@link Holder} compatibility, but {@link #getKey()} should be preferred.
     */
    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.getKey());
    }

    @Override
    public Holder.Kind kind() {
        return Holder.Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        bind(false);
        return this.getHolder().isPresent() && this.getHolder().get().canSerializeIn(owner);
    }

    @SuppressWarnings("unchecked")
    public @Nullable Holder<T> getRegistryHolder() {
        try {
            return (Holder<T>) _getHolder.invokeExact(this.delegate);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle _updateReference_VanillaRegistry;
    private static final MethodHandle _getHolder;

    static {
        MethodHandle ret;
        try {
            ret = MethodHandles.lookup().unreflect(ObfuscationReflectionHelper.findMethod(RegistryObject.class, "updateReference", Registry.class));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        _updateReference_VanillaRegistry = ret;

        try {
            ret = MethodHandles.lookup().unreflectGetter(ObfuscationReflectionHelper.findField(RegistryObject.class, "holder"));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        _getHolder = ret;
    }

    /**
     * Update the underlying entry manually from the given registry.
     *
     * @param registry The registry to pull the entry from
     */
    @ApiStatus.Internal
    protected void updateReference(Registry<? super T> registry) {
        RegistryObject<T> delegate = this.delegate;
        try {
            _updateReference_VanillaRegistry.invoke(Objects.requireNonNull(delegate, "Registry entry is empty"), registry);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @ApiStatus.Internal
    @Override
    public void updateReference(RegisterEvent event) {
        super.updateReference(event);
    }
}
