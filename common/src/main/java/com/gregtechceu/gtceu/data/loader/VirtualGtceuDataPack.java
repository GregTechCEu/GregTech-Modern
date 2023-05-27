package com.gregtechceu.gtceu.data.loader;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VirtualGtceuDataPack extends AbstractPackResources {
    private final Map<ResourceLocation, JsonObject> locationToData;
    private final Map<String, JsonObject> pathToData;
    private final Set<String> namespaces;

    public VirtualGtceuDataPack() {
        super(new File("dummy"));
        locationToData = new HashMap<>();
        pathToData = new HashMap<>();
        namespaces = new HashSet<>();
    }

    public void addData(ResourceLocation id, JsonObject data) {
        locationToData.put(id, data);
        pathToData.put("data/" + id.getNamespace() + "/" + id.getPath(), data);
        namespaces.add(id.getNamespace());
    }

    public void addManyData(Map<ResourceLocation, JsonObject> locationToData) {
        this.locationToData.putAll(locationToData);
        pathToData.putAll(locationToData.entrySet().stream().map(entry -> Pair.of("data/" + entry.getKey().getNamespace() + "/" + entry.getKey().getPath(), entry.getValue())).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        namespaces.addAll(locationToData.keySet().stream().map(ResourceLocation::getNamespace).toList());
    }

    @Override
    protected InputStream getResource(String path) throws IOException {
        JsonObject s = pathToData.get(path);
        if (s != null) {
            return new ByteArrayInputStream(s.toString().getBytes(StandardCharsets.UTF_8));
        }
        throw new FileNotFoundException(path);
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation location) throws IOException {
        JsonObject s = locationToData.get(location);
        if (s != null) {
            return new ByteArrayInputStream(s.toString().getBytes(StandardCharsets.UTF_8));
        }
        throw new FileNotFoundException(location.toString());
    }

    @Override
    protected boolean hasResource(String path) {
        return pathToData.containsKey(path);
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation location) {
        return type == PackType.SERVER_DATA && locationToData.containsKey(location);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, Predicate<ResourceLocation> filter) {
        return locationToData.keySet()
                .stream()
                .filter(r -> !r.getPath().endsWith(".mcmeta"))
                .filter(r -> r.getNamespace().equals(namespace) && r.getPath().startsWith(path))
                .filter(filter)
                .toList();
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return new HashSet<>(namespaces);
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        return null;
    }

    @Override
    public String toString() {
        return "GTCEu Virtual Data Pack";
    }

    @Override
    public String getName() {
        return this.toString();
    }

    @Override
    public void close() {

    }
}
