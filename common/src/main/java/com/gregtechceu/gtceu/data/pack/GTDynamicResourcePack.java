package com.gregtechceu.gtceu.data.pack;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class GTDynamicResourcePack implements PackResources {

    protected static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();
    protected static final ConcurrentMap<ResourceLocation, IoSupplier<InputStream>> DATA = new ConcurrentHashMap<>();

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

    public static void addBlockModel(ResourceLocation loc, JsonElement obj) {
        ResourceLocation l = getModelLocation(loc);
        DATA.put(l, () -> new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static void addBlockModel(ResourceLocation loc, Supplier<JsonElement> obj) {
        addBlockModel(loc, obj.get());
    }

    public static void addItemModel(ResourceLocation loc, JsonElement obj) {
        ResourceLocation l = getItemModelLocation(loc);
        DATA.put(l, () -> new ByteArrayInputStream(obj.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static void addItemModel(ResourceLocation loc, Supplier<JsonElement> obj) {
        addItemModel(loc, obj.get());
    }

    public static void addBlockState(ResourceLocation loc, JsonElement stateJson) {
        ResourceLocation l = getBlockStateLocation(loc);
        DATA.put(l, () -> new ByteArrayInputStream(stateJson.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static void addBlockState(ResourceLocation loc, Supplier<JsonElement> generator) {
        addBlockState(loc, generator.get());
    }

    public static void addBlockTexture(ResourceLocation loc, byte[] data) {
        ResourceLocation l = getTextureLocation("block", loc);
        DATA.put(l, () -> new ByteArrayInputStream(data));
    }

    public static void addBlockTexture(ResourceLocation loc, IoSupplier<InputStream> data) {
        ResourceLocation l = getTextureLocation("block", loc);
        DATA.put(l, data);
    }

    public static void addItemTexture(ResourceLocation loc, byte[] data) {
        ResourceLocation l = getTextureLocation("item", loc);
        DATA.put(l, () -> new ByteArrayInputStream(data));
    }

    public static void addItemTexture(ResourceLocation loc, IoSupplier<InputStream> data) {
        ResourceLocation l = getTextureLocation("item", loc);
        DATA.put(l, data);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.CLIENT_RESOURCES) {
            return DATA.getOrDefault(location, null);
        } else {
            return null;
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES) {
            if (!path.endsWith("/")) path += "/";
            final String finalPath = path;
            DATA.keySet().stream().filter(Objects::nonNull).filter(loc -> loc.getPath().startsWith(finalPath)).forEach((id) -> {
                IoSupplier<InputStream> resource = this.getResource(packType, id);
                if (resource != null) {
                    resourceOutput.accept(id, resource);
                }
            });
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? CLIENT_DOMAINS : Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if(metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic assets"), SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
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

    public static ResourceLocation getModelLocation(ResourceLocation blockId) {
        return new ResourceLocation(blockId.getNamespace(), String.join("", "models/", blockId.getPath(), ".json"));
    }

    public static ResourceLocation getItemModelLocation(ResourceLocation itemId) {
        return new ResourceLocation(itemId.getNamespace(), String.join("", "models/item/", itemId.getPath(), ".json"));
    }

    public static ResourceLocation getTextureLocation(String path, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), String.join("", "textures/", path, path == null || path.isBlank() ? "" : "/", tagId.getPath(), ".png"));
    }
}
