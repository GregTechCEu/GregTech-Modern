package net.minecraft.resources;

import java.util.function.UnaryOperator;

/**
 * Compile-time shim for {@code net.minecraft.resources.ResourceLocation}, which was renamed to
 * {@link Identifier} in NeoForge 26.1.x. The shim is required because third-party mod APIs
 * (EMI 1.1.22, KubeJS, Jade) still expose this exact FQN in their public types — code compiled
 * against those APIs needs the symbol to resolve at this name.
 *
 * <p><b>Do not import this class in core gtceu code.</b> Use {@link Identifier} directly. Boundary
 * call sites that need to convert between the two should use {@link #unwrap()} /
 * {@link #fromIdentifier(Identifier)}.
 *
 * <p>The {@code minecraftCompat} source set holding this shim is on main's compile classpath only;
 * it is excluded from the published jar so it cannot conflict with vanilla classes at runtime.
 */
public final class ResourceLocation implements Comparable<ResourceLocation> {

    private final Identifier delegate;

    private ResourceLocation(Identifier delegate) {
        this.delegate = delegate;
    }

    public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(Identifier.fromNamespaceAndPath(namespace, path));
    }

    public static ResourceLocation fromIdentifier(Identifier id) {
        return new ResourceLocation(id);
    }

    public static ResourceLocation parse(String name) {
        return new ResourceLocation(Identifier.parse(name));
    }

    public static ResourceLocation withDefaultNamespace(String path) {
        return new ResourceLocation(Identifier.withDefaultNamespace(path));
    }

    public static ResourceLocation tryParse(String name) {
        Identifier id = Identifier.tryParse(name);
        return id == null ? null : new ResourceLocation(id);
    }

    public Identifier toIdentifier() {
        return delegate;
    }

    public Identifier unwrap() {
        return delegate;
    }

    public String getNamespace() {
        return delegate.getNamespace();
    }

    public String getPath() {
        return delegate.getPath();
    }

    public ResourceLocation withPath(String path) {
        return new ResourceLocation(delegate.withPath(path));
    }

    public ResourceLocation withPath(UnaryOperator<String> path) {
        return new ResourceLocation(delegate.withPath(path));
    }

    public ResourceLocation withPrefix(String prefix) {
        return new ResourceLocation(delegate.withPrefix(prefix));
    }

    public ResourceLocation withSuffix(String suffix) {
        return new ResourceLocation(delegate.withSuffix(suffix));
    }

    @Override
    public int compareTo(ResourceLocation other) {
        return delegate.compareTo(other.delegate);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResourceLocation other && delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
