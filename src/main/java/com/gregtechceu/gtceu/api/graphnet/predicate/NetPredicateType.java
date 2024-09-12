package com.gregtechceu.gtceu.api.graphnet.predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class NetPredicateType<T extends EdgePredicate<T, ?>> implements StringRepresentable {

    private final String name;
    private final @NotNull Supplier<@NotNull T> supplier;
    private final @NotNull T defaultable;

    public NetPredicateType(@NotNull ResourceLocation name, @NotNull Supplier<@NotNull T> supplier,
                            @NotNull T defaultable) {
        this.name = name.toString();
        this.supplier = supplier;
        this.defaultable = defaultable;
    }

    public NetPredicateType(@NotNull String namespace, @NotNull String name, @NotNull Supplier<@NotNull T> supplier,
                            @NotNull T defaultable) {
        this.name = namespace + ":" + name;
        this.supplier = supplier;
        this.defaultable = defaultable;
    }

    @SuppressWarnings("unchecked")
    public T cast(EdgePredicate<?, ?> predicate) {
        return (T) predicate;
    }

    public final @NotNull T getNew() {
        return supplier.get();
    }

    public final @NotNull T getDefault() {
        return defaultable;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
