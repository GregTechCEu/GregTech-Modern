package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.GTRecipes;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootTable;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
    protected static final Map<ResourceLocation, byte[]> DATA = new HashMap<>();

    private final PackLocationInfo info;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "neoforge", "c"));
    }

    public GTDynamicDataPack(PackLocationInfo info) {
        this(info, AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet()));
    }

    public GTDynamicDataPack(PackLocationInfo info, Collection<String> domains) {
        this.info = info;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void clearServer() {
        DATA.clear();
    }

    public static void addRecipe(ResourceLocation recipeId, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                                 HolderLookup.Provider provider) {
        JsonElement recipeJson = Recipe.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), recipe)
                .getOrThrow();
        byte[] recipeBytes = recipeJson.toString().getBytes(StandardCharsets.UTF_8);
        Path parent = Platform.getGamePath().resolve("gtceu/dumped/data");
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(recipeId, "recipe", parent, recipeBytes);
        }
        if (DATA.containsKey(recipeId)) {
            GTCEu.LOGGER.error("duplicated recipe: {}", recipeId);
        }
        DATA.put(getRecipeLocation(recipeId), recipeBytes);
        if (advancement != null) {
            JsonElement advancementJson = Advancement.CODEC
                    .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), advancement.value())
                    .getOrThrow();
            byte[] advancementBytes = advancementJson.toString().getBytes(StandardCharsets.UTF_8);
            if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
                writeJson(advancement.id(), "advancement", parent, advancementBytes);
            }
            DATA.put(getAdvancementLocation(Objects.requireNonNull(advancement.id())), advancementBytes);
        }
    }

    public static void addLootTable(ResourceLocation lootTableId, LootTable table, HolderLookup.Provider provider) {
        JsonElement lootTableJson = LootTable.CODEC
                .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), Holder.direct(table)).getOrThrow();
        byte[] lootTableBytes = lootTableJson.toString().getBytes(StandardCharsets.UTF_8);
        Path parent = Platform.getGamePath().resolve("gtceu/dumped/data");
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(lootTableId, "loot_table", parent, lootTableBytes);
        }
        if (DATA.containsKey(lootTableId)) {
            GTCEu.LOGGER.error("duplicated loot table: {}", lootTableId);
        }
        DATA.put(getLootTableLocation(lootTableId), lootTableBytes);
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
    public static void writeJson(ResourceLocation id, @Nullable String subdir, Path parent, byte[] json) {
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
                output.write(json);
            }
        } catch (IOException e) {
            GTCEu.LOGGER.error("Failed to save data JSON to disk.", e);
        }
    }

    public static void addAdvancement(ResourceLocation loc, JsonObject obj) {
        ResourceLocation l = getAdvancementLocation(loc);
        synchronized (DATA) {
            DATA.put(l, obj.toString().getBytes(StandardCharsets.UTF_8));
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
            var byteArray = DATA.get(location);
            if (byteArray != null)
                return () -> new ByteArrayInputStream(byteArray);
            else return null;
        } else {
            return null;
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA) {
            if (!path.endsWith("/")) path += "/";
            final String finalPath = path;
            DATA.keySet().stream().filter(Objects::nonNull).filter(loc -> loc.getPath().startsWith(finalPath))
                    .forEach((id) -> {
                        IoSupplier<InputStream> resource = this.getResource(packType, id);
                        if (resource != null) {
                            resourceOutput.accept(id, resource);
                        }
                    });
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
            GTRecipes.recipeRemoval((id) -> { // Collect removed recipes in here, in the pack filter section.
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
    public PackLocationInfo location() {
        return this.info;
    }

    @Override
    public void close() {
        // NOOP
    }

    public static ResourceLocation getRecipeLocation(ResourceLocation recipeId) {
        return ResourceLocation.fromNamespaceAndPath(recipeId.getNamespace(),
                String.join("", "recipe/", recipeId.getPath(), ".json"));
    }

    public static ResourceLocation getLootTableLocation(ResourceLocation lootTableId) {
        return ResourceLocation.fromNamespaceAndPath(lootTableId.getNamespace(),
                String.join("", "loot_table/", lootTableId.getPath(), ".json"));
    }

    public static ResourceLocation getAdvancementLocation(ResourceLocation advancementId) {
        return ResourceLocation.fromNamespaceAndPath(advancementId.getNamespace(),
                String.join("", "advancement/", advancementId.getPath(), ".json"));
    }

    public static ResourceLocation getTagLocation(String identifier, ResourceLocation tagId) {
        return ResourceLocation.fromNamespaceAndPath(tagId.getNamespace(),
                String.join("", "tags/", identifier, "/", tagId.getPath(), ".json"));
    }
}
