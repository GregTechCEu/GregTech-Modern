package com.gregtechceu.gtceu.common.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
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
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.DataMapLoader;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GTDynamicDataPack implements PackResources {

    protected static final ObjectSet<String> SERVER_DOMAINS = new ObjectOpenHashSet<>();
    protected static final GTDynamicPackContents CONTENTS = new GTDynamicPackContents();

    private final PackLocationInfo info;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "neoforge", "c"));
    }

    public GTDynamicDataPack(PackLocationInfo info) {
        this(info, AddonFinder.getAddons().keySet());
    }

    public GTDynamicDataPack(PackLocationInfo info, Collection<String> domains) {
        this.info = info;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void clearServer() {
        CONTENTS.clearData();
    }

    private static void addToData(ResourceLocation location, byte[] bytes) {
        CONTENTS.addToData(location, bytes);
    }

    public static void addRecipe(ResourceLocation recipeId, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                                 HolderLookup.Provider provider) {
        JsonElement recipeJson = Recipe.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), recipe)
                .getOrThrow();
        byte[] recipeBytes = recipeJson.toString().getBytes(StandardCharsets.UTF_8);
        Path parent = GTCEu.getGameDir().resolve("gtceu/dumped/data");
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(recipeId, "recipe", parent, recipeBytes);
        }
        addToData(getRecipeLocation(recipeId), recipeBytes);
        if (advancement != null) {
            JsonElement advancementJson = Advancement.CODEC
                    .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), advancement.value())
                    .getOrThrow();
            byte[] advancementBytes = advancementJson.toString().getBytes(StandardCharsets.UTF_8);
            if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
                writeJson(advancement.id(), "advancement", parent, advancementBytes);
            }
            addToData(getAdvancementLocation(advancement.id()), advancementBytes);
        }
    }

    public static void addLootTable(ResourceLocation lootTableId, LootTable table, HolderLookup.Provider provider) {
        JsonElement lootTableJson = LootTable.DIRECT_CODEC
                .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), table).getOrThrow();
        byte[] lootTableBytes = lootTableJson.toString().getBytes(StandardCharsets.UTF_8);
        Path parent = Platform.getGamePath().resolve("gtceu/dumped/data");
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(lootTableId, "loot_table", parent, lootTableBytes);
        }
        if (CONTENTS.getResource(lootTableId) != null) {
            GTCEu.LOGGER.error("duplicate loot table: {}", lootTableId);
        }
        addToData(getLootTableLocation(lootTableId), lootTableBytes);
    }

    public static <T, R> void addDataMap(DataMapType<R, T> type, DataMapProvider.Builder<T, R> builder,
                                         HolderLookup.Provider provider) {
        ResourceLocation dataMapId = type.id()
                .withPrefix(DataMapLoader.getFolderLocation(type.registryKey().location()) + "/");
        JsonElement dataMapJson = DataMapFile.codec(type.registryKey(), type)
                .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), builder.build().carrier())
                .getOrThrow();
        byte[] dataMapBytes = dataMapJson.toString().getBytes(StandardCharsets.UTF_8);
        Path parent = Platform.getGamePath().resolve("gtceu/dumped/data");
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            writeJson(dataMapId, null, parent, dataMapBytes);
        }
        addToData(dataMapId, dataMapBytes);
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
                // assume JSON
                file = parent.resolve(id.getNamespace()).resolve(subdir).resolve(id.getPath() + ".json");
            } else {
                // assume the file type is also appended if a full path is given.
                file = parent.resolve(id.getNamespace()).resolve(id.getPath());
            }
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                output.write(json);
            }
        } catch (IOException e) {
            GTCEu.LOGGER.error("Failed to write JSON export for file {}", id, e);
        }
    }

    public static void addAdvancement(ResourceLocation loc, JsonObject obj) {
        ResourceLocation l = getAdvancementLocation(loc);
        addToData(l, obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length > 0 && elements[0].equals("pack.png")) {
            return () -> GTCEu.class.getResourceAsStream("/icon.png");
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.SERVER_DATA) {
            return CONTENTS.getResource(location);
        } else {
            return null;
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA) {
            CONTENTS.listResources(namespace, path, resourceOutput);
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
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {
        // NOOP
    }

    public static ResourceLocation getRecipeLocation(ResourceLocation recipeId) {
        return recipeId.withPath(path -> "recipe/" + path + ".json");
    }

    public static ResourceLocation getLootTableLocation(ResourceLocation lootTableId) {
        return lootTableId.withPath(path -> "loot_table/" + path + ".json");
    }

    public static ResourceLocation getAdvancementLocation(ResourceLocation advancementId) {
        return advancementId.withPath(path -> "advancement/" + path + ".json");
    }

    public static ResourceLocation getTagLocation(String identifier, ResourceLocation tagId) {
        return tagId.withPath(path -> "tags/" + identifier + "/" + path + ".json");
    }
}
