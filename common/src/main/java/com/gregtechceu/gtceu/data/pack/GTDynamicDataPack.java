package com.gregtechceu.gtceu.data.pack;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.Platform;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GTDynamicDataPack implements PackResources {

    protected static final ObjectSet<String> SERVER_DOMAINS = new ObjectOpenHashSet<>();
    protected static final Map<ResourceLocation, JsonObject> DATA = new HashMap<>();

    private final String name;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "forge", "c"));
    }

    public GTDynamicDataPack(String name, Collection<String> domains) {
        this.name = name;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void clearServer() {
        DATA.clear();
    }

    public static void addRecipe(FinishedRecipe recipe) {
        JsonObject recipeJson = recipe.serializeRecipe();
        Path parent = Platform.getGamePath().resolve("gtceu/dumped/data");
        ResourceLocation recipeId = recipe.getId();
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(recipeId, "recipes", parent, recipeJson);
        }
        if (DATA.containsKey(recipeId)) {
            GTCEu.LOGGER.error("duplicated recipe: {}", recipeId);
        }
        DATA.put(getRecipeLocation(recipeId), recipeJson);
        if (recipe.serializeAdvancement() != null) {
            JsonObject advancement = recipe.serializeAdvancement();
            if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
                writeJson(recipe.getAdvancementId(), "advancements", parent, advancement);
            }
            DATA.put(getAdvancementLocation(Objects.requireNonNull(recipe.getAdvancementId())), advancement);
        }
    }

    private static void writeJson(ResourceLocation id, String subdir, Path parent, JsonObject json){
        try {
            Path file = parent.resolve(id.getNamespace()).resolve(subdir).resolve(id.getPath() + ".json");
            Files.createDirectories(file.getParent());
            try(OutputStream output = Files.newOutputStream(file)) {
                output.write(json.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addAdvancement(ResourceLocation loc, JsonObject obj) {
        ResourceLocation l = getAdvancementLocation(loc);
        synchronized (DATA) {
            DATA.put(l, obj);
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        throw new UnsupportedOperationException("Dynamic Resource Pack cannot have root resources");
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.SERVER_DATA) {
            if (DATA.containsKey(location))
                return () -> new ByteArrayInputStream(DATA.get(location).toString().getBytes(StandardCharsets.UTF_8));
            else return null;
        } else {
            return () -> new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA)
            DATA.keySet().stream().filter(Objects::nonNull).filter(loc -> loc.getPath().startsWith(path)).forEach((id) -> {
                if (this.getResource(packType, new ResourceLocation(namespace, path)) != null) {
                    resourceOutput.accept(id, this.getResource(packType, new ResourceLocation(namespace, path)));
                }
            });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? SERVER_DOMAINS : Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if(metaReader.getMetadataSectionName().equals("pack")) {
            JsonObject object = new JsonObject();
            object.addProperty("pack_format", 9);
            object.addProperty("description", "runtime data pack");
            return metaReader.fromJson(object);
        } else if (metaReader.getMetadataSectionName().equals("filter")) {
            JsonObject filter = new JsonObject();
            JsonArray block = new JsonArray();
            GTRecipes.recipeRemoval((id) -> { // Collect removed recipes in here, in the pack filter section.
                JsonObject entry = new JsonObject();
                entry.addProperty("namespace", id.getNamespace().replaceAll("[\\W]", "\\\\$0"));
                entry.addProperty("path", "recipes/" + id.getPath().replaceAll("[\\W]", "\\\\$0"));
                block.add(entry);
            });
            filter.add("block", block);
            return metaReader.fromJson(filter);
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

    public static ResourceLocation getRecipeLocation(ResourceLocation recipeId) {
        return new ResourceLocation(recipeId.getNamespace(), String.join("", "recipes/", recipeId.getPath(), ".json"));
    }

    public static ResourceLocation getAdvancementLocation(ResourceLocation advancementId) {
        return new ResourceLocation(advancementId.getNamespace(), String.join("", "advancements/", advancementId.getPath(), ".json"));
    }

    public static ResourceLocation getTagLocation(String identifier, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), String.join("", "tags/", identifier, "/", tagId.getPath(), ".json"));
    }
}
