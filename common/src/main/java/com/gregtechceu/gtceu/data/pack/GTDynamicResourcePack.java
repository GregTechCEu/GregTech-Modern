package com.gregtechceu.gtceu.data.pack;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.SharedConstants;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GTDynamicResourcePack implements PackResources {

    protected static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();
    protected static final Map<ResourceLocation, JsonObject> DATA = new HashMap<>();

    private final String name;

    static {
        CLIENT_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "forge", "c"));
    }

    public GTDynamicResourcePack(String name, Collection<String> domains) {
        this.name = name;
        CLIENT_DOMAINS.addAll(domains);
    }

    public static void clearClient() {
        DATA.clear();
    }

    public static void addModel(ResourceLocation loc, JsonObject obj) {
        ResourceLocation l = getBlockModelLocation(loc);
        DATA.put(l, obj);
    }

    public static void addBlockState(ResourceLocation loc, JsonObject stateJson) {
        ResourceLocation l = getBlockStateLocation(loc);
        DATA.put(l, stateJson);
    }

    public static void addBlockState(ResourceLocation loc, BlockStateGenerator generator) {
        addBlockState(loc, generator.get().getAsJsonObject());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.SERVER_DATA) {
            if (DATA.containsKey(location))
                return () -> new ByteArrayInputStream(DATA.get(location).toString().getBytes(StandardCharsets.UTF_8));
            else return null;
        } else {
            return null;
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA)
            DATA.keySet().stream().filter(Objects::nonNull).filter(loc -> loc.getPath().startsWith(path)).forEach((id) -> {
                IoSupplier<InputStream> resource = this.getResource(packType, id);
                if (resource != null) {
                    resourceOutput.accept(id, resource);
                }
            });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? CLIENT_DOMAINS : Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if(metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic assets"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        }
        return null;
    }

    @Override
    public String packId() {
        return this.name;
    }

    @Override
    public void close() {
        //NOOP
    }

    public static ResourceLocation getBlockStateLocation(ResourceLocation blockId) {
        return new ResourceLocation(blockId.getNamespace(), String.join("", "blockstates/", blockId.getPath(), ".json"));
    }

    public static ResourceLocation getBlockModelLocation(ResourceLocation blockId) {
        return new ResourceLocation(blockId.getNamespace(), String.join("", "models/block/", blockId.getPath(), ".json"));
    }

    public static ResourceLocation getItemModelLocation(ResourceLocation itemId) {
        return new ResourceLocation(itemId.getNamespace(), String.join("", "models/item/", itemId.getPath(), ".json"));
    }

    public static ResourceLocation getTagLocation(String ResourceLocation, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), String.join("", "tags/", ResourceLocation, "/", tagId.getPath(), ".json"));
    }
}
