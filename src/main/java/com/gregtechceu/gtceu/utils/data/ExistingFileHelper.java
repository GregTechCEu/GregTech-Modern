package com.gregtechceu.gtceu.utils.data;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Compatibility shim for the 1.21-era datagen helpers removed from NeoForge's
 * public API. GTCEu still uses the old model generator surface internally while
 * it is migrated to the 26.1 vanilla model provider API.
 */
@Deprecated(forRemoval = true)
public class ExistingFileHelper {

    public interface IResourceType {

        PackType getPackType();

        String getSuffix();

        String getPrefix();
    }

    public static class ResourceType implements IResourceType {

        private final PackType packType;
        private final String suffix;
        private final String prefix;

        public ResourceType(PackType packType, String suffix, String prefix) {
            this.packType = packType;
            this.suffix = suffix;
            this.prefix = prefix;
        }

        @Override
        public PackType getPackType() {
            return packType;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }
    }

    protected final Multimap<PackType, Identifier> generated;

    public ExistingFileHelper() {
        this(HashMultimap.create());
    }

    public ExistingFileHelper(Multimap<PackType, Identifier> generated) {
        this.generated = generated;
    }

    protected Identifier getLocation(Identifier base, String suffix, String prefix) {
        return base.withPath(path -> prefix + "/" + path + suffix);
    }

    public boolean exists(Identifier loc, PackType packType) {
        if (generated.get(packType).contains(loc)) return true;
        // Datagen has no ResourceManager, so check the classpath. The build pulls
        // src/main/resources onto the runtime classpath under assets/<ns>/... and
        // data/<ns>/..., so a getResource() against the context loader resolves
        // to the source assets.
        String root = packType == PackType.CLIENT_RESOURCES ? "assets" : "data";
        String fullPath = root + "/" + loc.getNamespace() + "/" + loc.getPath();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = ExistingFileHelper.class.getClassLoader();
        return loader.getResource(fullPath) != null;
    }

    public boolean exists(Identifier loc, IResourceType type) {
        return exists(getLocation(loc, type.getSuffix(), type.getPrefix()), type.getPackType());
    }

    public boolean exists(Identifier loc, PackType packType, String pathSuffix, String pathPrefix) {
        return exists(getLocation(loc, pathSuffix, pathPrefix), packType);
    }

    public void trackGenerated(Identifier loc, IResourceType type) {
        trackGenerated(loc, type.getPackType(), type.getSuffix(), type.getPrefix());
    }

    public void trackGenerated(Identifier loc, PackType packType, String suffix, String prefix) {
        generated.put(packType, getLocation(loc, suffix, prefix));
    }
}
