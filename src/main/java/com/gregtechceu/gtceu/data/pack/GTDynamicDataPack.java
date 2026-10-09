package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
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
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
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

    public static final FileToIdConverter RECIPE_ID_CONVERTER = FileToIdConverter.json("recipe");
    private static final FileToIdConverter LOOT_TABLE_ID_CONVERTER = FileToIdConverter.json("loot_table");
    private static final FileToIdConverter ADVANCEMENT_ID_CONVERTER = FileToIdConverter.json("advancement");

    private final PackLocationInfo info;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "neoforge", "c", "kubejs"));
    }

    public GTDynamicDataPack(PackLocationInfo info) {
        this(info, AddonFinder.getAddons().keySet());
    }

    public GTDynamicDataPack(PackLocationInfo info, Collection<String> domains) {
        this.info = info;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void addNamespace(String namespace) {
        SERVER_DOMAINS.add(namespace);
    }

    public static void clearServer() {
        CONTENTS.clearData();
    }

    public static void addResource(ResourceLocation location, JsonElement obj) {
        addResource(location, obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void addResource(ResourceLocation location, byte[] data) {
        if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
            Path parent = GTCEu.GTCEU_FOLDER.resolve("dumped/data");
            writeJson(location, parent, data);
        }
        CONTENTS.addToData(location, data);
    }

    /**
     * @deprecated API consumers shouldn't use this method. Instead, they should implement their recipe additions in
     *             {@link IGTAddon#addRecipes(RecipeOutput) IGTAddon.addRecipes}.
     * @see IGTAddon#addRecipes(RecipeOutput)
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public static void addRecipe(ResourceLocation recipeId, Recipe<?> recipe, @Nullable AdvancementHolder advancement,
                                 HolderLookup.Provider registries) {
        DynamicOps<JsonElement> serializationContext = registries.createSerializationContext(JsonOps.INSTANCE);
        JsonElement recipeJson = Recipe.CODEC
                .encodeStart(serializationContext, recipe)
                .getOrThrow();
        addResource(RECIPE_ID_CONVERTER.idToFile(recipeId), recipeJson);

        if (advancement != null) {
            addAdvancement(advancement, serializationContext);
        }
    }

    public static void addAdvancement(AdvancementHolder advancement, DynamicOps<JsonElement> serializationContext) {
        addAdvancement(advancement.id(), advancement.value(), serializationContext);
    }

    public static void addAdvancement(ResourceLocation loc, Advancement advancement,
                                      DynamicOps<JsonElement> serializationContext) {
        JsonElement advancementJson = Advancement.CODEC
                .encodeStart(serializationContext, advancement)
                .getOrThrow();
        addResource(ADVANCEMENT_ID_CONVERTER.idToFile(loc), advancementJson);
    }

    public static void addLootTable(ResourceLocation lootTableId, LootTable table,
                                    HolderLookup.Provider registries) {
        addLootTable(lootTableId, table, registries.createSerializationContext(JsonOps.INSTANCE));
    }

    public static void addLootTable(ResourceLocation lootTableId, LootTable table,
                                    DynamicOps<JsonElement> serializationContext) {
        JsonElement lootTableJson = LootTable.DIRECT_CODEC.encodeStart(serializationContext, table).getOrThrow();

        ResourceLocation fileName = LOOT_TABLE_ID_CONVERTER.idToFile(lootTableId);
        if (CONTENTS.getResource(fileName) != null) {
            GTCEu.LOGGER.error("duplicate loot table: {}", lootTableId);
        }
        addResource(fileName, lootTableJson);
    }

    /**
     * if subdir is null, no file ending is appended.
     *
     * @param id     the resource location of the file to be written.
     * @param parent the parent folder where to write data to.
     * @param json   the json to write.
     */
    @ApiStatus.Internal
    public static void writeJson(ResourceLocation id, Path parent, byte[] json) {
        try {
            Path file = parent.resolve(id.getNamespace()).resolve(id.getPath());

            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                output.write(json);
            }
        } catch (IOException e) {
            GTCEu.LOGGER.error("Failed to write JSON export for file {}", id, e);
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
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
}
