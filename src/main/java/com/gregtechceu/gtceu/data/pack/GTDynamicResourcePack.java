package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;

import net.minecraft.SharedConstants;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.data.pack.GTDynamicDataPack.writeJson;

public class GTDynamicResourcePack implements PackResources {

    protected static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();
    protected static final GTDynamicPackContents CONTENTS = new GTDynamicPackContents();

    private static final FileToIdConverter ATLAS_ID_CONVERTER = FileToIdConverter.json("atlases");
    public static final FileToIdConverter TEXTURE_ID_CONVERTER = SpriteSource.TEXTURE_ID_CONVERTER;
    public static final FileToIdConverter BLOCKSTATE_ID_CONVERTER = FileToIdConverter.json("blockstates");
    public static final FileToIdConverter MODEL_ID_CONVERTER = FileToIdConverter.json("models");
    public static final FileToIdConverter ITEM_DEFINITION_ID_CONVERTER = FileToIdConverter.json("items");

    private final PackLocationInfo info;

    static {
        CLIENT_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "neoforge", "c", "kubejs"));
    }

    public GTDynamicResourcePack(PackLocationInfo info) {
        this(info, AddonFinder.getAddons().keySet());
    }

    public GTDynamicResourcePack(PackLocationInfo info, Collection<String> domains) {
        this.info = info;
        CLIENT_DOMAINS.addAll(domains);

        if (GTCEu.Mods.isKubeJSLoaded()) {
            GregTechKubeJSPlugin.generateMachineBlockModels();
        }
    }

    public static void addNamespace(String namespace) {
        CLIENT_DOMAINS.add(namespace);
    }

    public static void clearClient() {
        CONTENTS.clearData();
    }

    public static void addResource(Identifier location, JsonElement obj) {
        addResource(location, obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void addResource(Identifier location, byte[] data) {
        if (ConfigHolder.INSTANCE.dev.dumpAssets) {
            Path parent = GTCEu.GTCEU_FOLDER.resolve("dumped/assets");
            writeJson(location, parent, data);
        }
        CONTENTS.addToData(location, data);
    }

    public static void addBlockModel(Identifier loc, JsonElement obj) {
        if (!loc.getPath().startsWith("block/")) {
            loc = loc.withPrefix("block/");
        }
        addModel(loc, obj);
    }

    public static void addBlockModel(Identifier loc, Supplier<JsonElement> obj) {
        addBlockModel(loc, obj.get());
    }

    public static void addBlockModel(BlockModelBuilder builder) {
        addBlockModel(builder.getLocation(), builder.toJson());
    }

    public static void addItemModel(Identifier loc, JsonElement obj) {
        Identifier itemId = loc.getPath().startsWith("item/") ?
                loc.withPath(path -> path.substring("item/".length())) :
                loc;
        if (!loc.getPath().startsWith("item/")) {
            loc = loc.withPrefix("item/");
        }
        addModel(loc, obj);
        addItemDefinition(itemId, loc);
    }

    public static void addItemModel(ItemModelBuilder builder) {
        addItemModel(builder.getLocation(), builder.toJson());
    }

    public static void addItemModel(Identifier loc, Supplier<JsonElement> obj) {
        addItemModel(loc, obj.get());
    }

    public static void addModel(Identifier loc, JsonElement obj) {
        loc = MODEL_ID_CONVERTER.idToFile(loc);
        addResource(loc, obj);
    }

    public static void addModel(Identifier loc, Supplier<JsonElement> obj) {
        addModel(loc, obj.get());
    }

    public static <T extends ModelBuilder<T>> void addModel(T builder) {
        addModel(builder.getLocation(), builder.toJson());
    }

    public static void addItemDefinition(Identifier itemId, Identifier modelId) {
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", modelId.toString());

        JsonObject definition = new JsonObject();
        definition.add("model", model);

        addResource(ITEM_DEFINITION_ID_CONVERTER.idToFile(itemId), definition);
    }

    public static void addItemDefinition(Identifier itemId, JsonElement definition) {
        addResource(ITEM_DEFINITION_ID_CONVERTER.idToFile(itemId), definition);
    }

    public static void addBlockState(Identifier loc, JsonElement stateJson) {
        loc = BLOCKSTATE_ID_CONVERTER.idToFile(loc);
        addResource(loc, stateJson);
    }

    public static void addBlockState(Identifier loc, Supplier<JsonElement> generator) {
        addBlockState(loc, generator.get());
    }

    public static void addBlockState(BlockModelDefinitionGenerator generator) {
        JsonElement stateJson = BlockStateModelDispatcher.CODEC.encodeStart(JsonOps.INSTANCE, generator.create())
                .getOrThrow();
        addBlockState(BuiltInRegistries.BLOCK.getKey(generator.block()), stateJson);
    }

    public static void addAtlasSpriteSource(Identifier atlasLoc, SpriteSource source) {
        addAtlasSpriteSourceList(atlasLoc, Collections.singletonList(source));
    }

    public static void addAtlasSpriteSourceList(Identifier loc, List<SpriteSource> sources) {
        loc = ATLAS_ID_CONVERTER.idToFile(loc);
        JsonElement sourceJson = SpriteSources.FILE_CODEC.encodeStart(JsonOps.INSTANCE, sources)
                .getOrThrow();
        addResource(loc, sourceJson);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length > 0 && elements[0].equals("pack.png")) {
            return () -> GTCEu.class.getResourceAsStream("/icon.png");
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type == PackType.CLIENT_RESOURCES) {
            return CONTENTS.getResource(location);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES) {
            CONTENTS.listResources(namespace, path, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? CLIENT_DOMAINS : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionType<T> metaReader) throws IOException {
        if (metaReader == PackMetadataSection.forPackType(PackType.CLIENT_RESOURCES) ||
                metaReader == PackMetadataSection.FALLBACK_TYPE) {
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic assets"),
                    SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).minorRange());
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
