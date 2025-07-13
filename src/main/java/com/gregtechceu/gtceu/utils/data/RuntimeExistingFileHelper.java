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
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Existing file helper that wraps the client/server resource manager instead of creating its own.<br>
 * Useful for using data generators outside datagen.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RuntimeExistingFileHelper extends ExistingFileHelper {

    public static final RuntimeExistingFileHelper INSTANCE = new RuntimeExistingFileHelper();

    protected final Multimap<PackType, ResourceLocation> generated = HashMultimap.create();

    protected RuntimeExistingFileHelper() {
        super(Collections.emptySet(), Collections.emptySet(), false, null, null);
    }

    public static @NotNull ResourceManager getManager(PackType packType) {
        if (packType == PackType.CLIENT_RESOURCES) {
            return Minecraft.getInstance().getResourceManager();
        } else if (packType == PackType.SERVER_DATA) {
            if (GTCEu.getMinecraftServer() == null) {
                throw new IllegalStateException("Cannot get server resources without a server or on a remote client.");
            }
            return GTCEu.getMinecraftServer().getResourceManager();
        } else {
            throw new IllegalStateException("Invalid pack type " + packType);
        }
    }

    protected ResourceLocation getLocation(ResourceLocation base, String prefix, String suffix) {
        return base.withPath(path -> prefix + "/" + path + suffix);
    }

    @Override
    public boolean exists(ResourceLocation loc, PackType packType) {
        return generated.get(packType).contains(loc) || getManager(packType).getResource(loc).isPresent();
    }

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
}
