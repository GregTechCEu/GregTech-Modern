package brachy.modularui.utils;

import org.jspecify.annotations.NullMarked;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSources;
import net.neoforged.neoforge.common.conditions.ICondition;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@NullMarked
@Accessors(fluent = true)
public final class RegistryAccessContainer implements RegistryAccess.Frozen {

    public static final RegistryAccessContainer BUILTIN = new RegistryAccessContainer(
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

    @Getter
    private static RegistryAccessContainer current = BUILTIN;
    private static ICondition.IContext tagContext = ICondition.IContext.TAGS_INVALID;

    @Getter
    private final RegistryAccess access;
    private DamageSources damageSources;

    public RegistryAccessContainer(RegistryAccess access) {
        this.access = access;
        this.damageSources = null;
    }

    @ApiStatus.Internal
    public static void update(RegistryAccess registries, @Nullable ICondition.IContext tagContext) {
        RegistryAccessContainer.current = new RegistryAccessContainer(registries);
        if (tagContext != null) {
            RegistryAccessContainer.tagContext = tagContext;
        }
    }

    public DamageSources damageSources() {
        if (damageSources == null) {
            damageSources = new DamageSources(access);
        }

        return damageSources;
    }

    // region tag context methods

    public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
        return tagContext.getTag(key);
    }

    public ICondition.IContext conditionContext() {
        return tagContext;
    }

    // endregion

    // region registry access methods

    public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return access.lookup(registryKey);
    }

    @Override
    public <T> Optional<Registry<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return access.lookup(registryKey);
    }

    public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return access.lookupOrThrow(registryKey);
    }

    @Override
    public Stream<RegistryEntry<?>> registries() {
        return access.registries();
    }

    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return access.listRegistryKeys();
    }

    // endregion
}
