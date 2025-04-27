package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.SharedConstants;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GTDynamicDataPack implements PackResources {

    protected static final ObjectSet<String> SERVER_DOMAINS = new ObjectOpenHashSet<>();
    protected static final Node ROOT = new Node();

    protected static class Node {

        /**
         * Holds either a byte[] with the data for a given location, or a map of string -> Node.
         */
        Object contents = new Object2ObjectOpenHashMap<>();

        void collectResources(String namespace, String[] pathComponents, int curIndex,
                              PackResources.ResourceOutput output) {
            if (curIndex < pathComponents.length) {
                String component = pathComponents[curIndex];

                Node n = getChild(component);
                if (n != null) {
                    n.collectResources(namespace, pathComponents, curIndex + 1, output);
                }
            } else {
                // We reached the desired path. Collect all resources
                this.outputResources(namespace, String.join("/", pathComponents), output);
            }
        }

        private boolean isTerminalNode() {
            return contents instanceof byte[];
        }

        @SuppressWarnings("unchecked")
        private Map<String, Node> getChildren() {
            if (!(contents instanceof Map<?, ?>)) {
                throw new IllegalStateException("attempting to get children on a terminal node");
            }
            return (Map<String, Node>) contents;
        }

        void outputResources(String namespace, String path, PackResources.ResourceOutput output) {
            if (isTerminalNode()) {
                // This is a terminal node.
                ResourceLocation location = new ResourceLocation(namespace, path);
                output.accept(location, this.createIoSupplier());
            } else {
                for (var entry : getChildren().entrySet()) {
                    entry.getValue().outputResources(namespace, path + "/" + entry.getKey(), output);
                }
            }
        }

        IoSupplier<InputStream> createIoSupplier() {
            if (!isTerminalNode()) {
                throw new IllegalStateException("Node has no data");
            }
            // Capture the byte array here to avoid capturing the whole node in the lambda
            byte[] byteArray = (byte[]) contents;
            return () -> new ByteArrayInputStream(byteArray);
        }

        @Nullable
        Node getChild(String name) {
            if (isTerminalNode()) {
                return null;
            } else {
                return getChildren().get(name);
            }
        }
    }

    private final String name;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "forge", "c"));
    }

    public GTDynamicDataPack(String name) {
        this(name, AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet()));
    }

    public GTDynamicDataPack(String name, Collection<String> domains) {
        this.name = name;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void clearServer() {
        ROOT.getChildren().clear();
    }

    private static void addToData(ResourceLocation location, byte[] bytes) {
        String[] pathComponents = location.getPath().split("/");
        Node node = ROOT.getChildren().computeIfAbsent(location.getNamespace(), $ -> new Node());
        for (String component : pathComponents) {
            node = node.getChildren().computeIfAbsent(component, $ -> new Node());
        }
        node.contents = bytes;
    }

    public static void addRecipe(FinishedRecipe recipe) {
        JsonObject recipeJson = recipe.serializeRecipe();
        Path parent = GTCEu.getGameDir().resolve("gtceu/dumped/data");
        ResourceLocation recipeId = recipe.getId();
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(recipeId, "recipes", parent, recipeJson);
        }
        addToData(getRecipeLocation(recipeId), recipeJson.toString().getBytes(StandardCharsets.UTF_8));
        if (recipe.serializeAdvancement() != null) {
            JsonObject advancement = recipe.serializeAdvancement();
            if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
                writeJson(recipe.getAdvancementId(), "advancements", parent, advancement);
            }
            addToData(getAdvancementLocation(Objects.requireNonNull(recipe.getAdvancementId())),
                    advancement.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * if subdir is null, no file ending is appended.
     * 
     * @param id     the resource location of the file to be written.
     * @param subdir a nullable subdirectory for the data.
     * @param parent the parent folder where to write data to.
     * @param json   the json to write.
     */
    @ApiStatus.Internal
    public static void writeJson(ResourceLocation id, @Nullable String subdir, Path parent, JsonElement json) {
        try {
            Path file;
            if (subdir != null) {
                file = parent.resolve(id.getNamespace()).resolve(subdir).resolve(id.getPath() + ".json"); // assume JSON
            } else {
                file = parent.resolve(id.getNamespace()).resolve(id.getPath()); // assume the file type is also appended
                                                                                // if a full path is given.
            }
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                output.write(json.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addAdvancement(ResourceLocation loc, JsonObject obj) {
        ResourceLocation l = getAdvancementLocation(loc);
        synchronized (ROOT) {
            addToData(l, obj.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.SERVER_DATA) {
            Node node = ROOT.getChild(location.getNamespace());
            String[] pathComponents = location.getPath().split("/");
            for (String path : pathComponents) {
                if (node == null) {
                    return null;
                }
                node = node.getChild(path);
            }
            if (node == null) {
                return null;
            }
            return node.createIoSupplier();
        } else {
            return null;
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA) {
            Node base = ROOT.getChild(namespace);
            if (base == null) {
                return;
            }
            base.collectResources(namespace, path.split("/"), 0, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? SERVER_DOMAINS : Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if (metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic data"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        } else if (metaReader.getMetadataSectionName().equals("filter")) {
            JsonObject filter = new JsonObject();
            JsonArray block = new JsonArray();
            GTRecipes.RECIPE_FILTERS.forEach((id) -> { // Collect removed recipes in here, in the pack filter section.
                JsonObject entry = new JsonObject();
                entry.addProperty("namespace", "^" + id.getNamespace().replaceAll("[\\W]", "\\\\$0") + "$");
                entry.addProperty("path", "^recipes/" + id.getPath().replaceAll("[\\W]", "\\\\$0") + "\\.json" + "$");
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
        // NOOP
    }

    public static ResourceLocation getRecipeLocation(ResourceLocation recipeId) {
        return new ResourceLocation(recipeId.getNamespace(), String.join("", "recipes/", recipeId.getPath(), ".json"));
    }

    public static ResourceLocation getAdvancementLocation(ResourceLocation advancementId) {
        return new ResourceLocation(advancementId.getNamespace(),
                String.join("", "advancements/", advancementId.getPath(), ".json"));
    }

    public static ResourceLocation getTagLocation(String identifier, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(),
                String.join("", "tags/", identifier, "/", tagId.getPath(), ".json"));
    }
}
