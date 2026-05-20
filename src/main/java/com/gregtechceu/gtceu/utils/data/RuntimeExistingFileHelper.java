package com.gregtechceu.gtceu.utils.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Existing file helper that wraps the client/server resource manager instead of creating its own.<br>
 * Useful for using data generators outside datagen.
 * <p>
 * By default, this class assumes all resources exist and does not check any references' validity.
 * To enable actual checking, you may use a try-with-resources statement like this:
 * 
 * <pre>{@code
 * try (var helper = RuntimeExistingFileHelper.INSTANCE.activeHelper()) {
 *     // If you don't use a try-with-resources or try-finally block to
 *     // enable checking, calling `exists` will always return true.
 *     if (helper.exists(texture, GTBlockstateProvider.TEXTURE)) {
 *         // do stuff
 *     }
 * }
 * }</pre>
 *
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RuntimeExistingFileHelper extends ExistingFileHelper {

    public static final RuntimeExistingFileHelper INSTANCE = new RuntimeExistingFileHelper(HashMultimap.create());

    protected final Multimap<PackType, ResourceLocation> generated;
    protected @Nullable Active activeHelper;

    protected RuntimeExistingFileHelper(Multimap<PackType, ResourceLocation> generated) {
        super(Collections.emptySet(), Collections.emptySet(), false, null, null);
        this.generated = generated;
    }

    public static ResourceManager getManager(PackType packType) {
        if (packType == PackType.CLIENT_RESOURCES) {
            return Minecraft.getInstance().getResourceManager();
        } else if (packType == PackType.SERVER_DATA) {
            if (GTCEu.getMinecraftServer() == null) {
                throw new IllegalStateException("Cannot get server resources without a server / on a remote client.");
            }
            return GTCEu.getMinecraftServer().getResourceManager();
        } else {
            throw new IllegalStateException("Invalid pack type " + packType);
        }
    }

    protected ResourceLocation getLocation(ResourceLocation base, String prefix, String suffix) {
        return base.withPath(path -> prefix + "/" + path + suffix);
    }

    public RuntimeExistingFileHelper.Active activeHelper() {
        if (this.activeHelper == null) {
            // pass the same generated resources map into the subclass
            // so any resources added/checked by it are automatically updated here
            this.activeHelper = new Active(this.generated);
        }
        return this.activeHelper;
    }

    /**
     * Bypass the normal {@code exists} function so missing/invalid references etc. don't cause runtime errors.<br>
     * A toggle for enabling proper functionality is implemented in the form of {@link #activeHelper()}-
     */
    @Override
    public boolean exists(ResourceLocation loc, PackType packType) {
        return true;
    }

    /// Implement a copy of the normal {@code exists} function that we can use for checking

    @Override
    public void trackGenerated(ResourceLocation loc, IResourceType type) {
        trackGenerated(loc, type.getPackType(), type.getSuffix(), type.getPrefix());
    }

    @Override
    public void trackGenerated(ResourceLocation loc, PackType packType, String suffix, String prefix) {
        this.generated.put(packType, getLocation(loc, prefix, suffix));
    }

    @Override
    public Resource getResource(ResourceLocation loc, PackType packType,
                                String pathSuffix, String pathPrefix) throws FileNotFoundException {
        return getResource(getLocation(loc, pathPrefix, pathSuffix), packType);
    }

    @Override
    public Resource getResource(ResourceLocation loc, PackType packType) throws FileNotFoundException {
        return getManager(packType).getResourceOrThrow(loc);
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation loc, PackType packType) {
        return getManager(packType).getResourceStack(loc);
    }

    /**
     * This class implements {@link AutoCloseable} for ease of enabling actual checking when it is required.
     * <p>
     * Note that it's safe to ignore "unclosed AutoCloseable"/{@code resource} warnings on this class, as is done in
     * {@linkplain com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays#get WorkableOverlays#get}.
     * </p>
     */
    public static class Active extends RuntimeExistingFileHelper implements AutoCloseable {

        protected Active(Multimap<PackType, ResourceLocation> parentGenerated) {
            super(parentGenerated);
        }

        @Override
        public Active activeHelper() {
            return this;
        }

        @Override
        public boolean exists(ResourceLocation loc, PackType packType) {
            return this.generated.get(packType).contains(loc) || getManager(packType).getResource(loc).isPresent();
        }

        @Override
        public void close() {}
    }
}
