package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.data.pack.GTDynamicDataPack.writeJson;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTDynamicResourcePack implements PackResources {

    protected static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();
    protected static final GTDynamicPackContents CONTENTS = new GTDynamicPackContents();

    private static final FileToIdConverter ATLAS_ID_CONVERTER = FileToIdConverter.json("atlases");
    public static final FileToIdConverter TEXTURE_ID_CONVERTER = SpriteSource.TEXTURE_ID_CONVERTER;
    public static final FileToIdConverter BLOCKSTATE_ID_CONVERTER = FileToIdConverter.json("blockstates");
    public static final FileToIdConverter MODEL_ID_CONVERTER = FileToIdConverter.json("models");

    private final String name;

    static {
        CLIENT_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "forge", "c"));
    }

    public GTDynamicResourcePack(String name) {
        this(name, AddonFinder.getAddons().stream().map(IGTAddon::addonModId).collect(Collectors.toSet()));
    }

    public GTDynamicResourcePack(String name, Collection<String> domains) {
        this.name = name;
        CLIENT_DOMAINS.addAll(domains);
    }

    public static void clearClient() {
        CONTENTS.clearData();
    }

    public static void addResource(ResourceLocation location, JsonElement obj) {
        addResource(location, obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void addResource(ResourceLocation location, byte[] data) {
        if (ConfigHolder.INSTANCE.dev.dumpAssets) {
            Path parent = GTCEu.GTCEU_FOLDER.resolve("dumped/assets");
            writeJson(location, null, parent, data);
        }
        CONTENTS.addToData(location, data);
    }

    public static void addBlockModel(ResourceLocation loc, JsonElement obj) {
        if (!loc.getPath().startsWith("block/")) {
            loc = loc.withPrefix("block/");
        }
        addModel(loc, obj);
    }

    public static void addBlockModel(ResourceLocation loc, Supplier<JsonElement> obj) {
        addBlockModel(loc, obj.get());
    }

    public static void addBlockModel(BlockModelBuilder builder) {
        addBlockModel(builder.getLocation(), builder.toJson());
    }

    public static void addItemModel(ResourceLocation loc, JsonElement obj) {
        if (!loc.getPath().startsWith("item/")) {
            loc = loc.withPrefix("item/");
        }
        addModel(loc, obj);
    }

    public static void addItemModel(ItemModelBuilder builder) {
        addItemModel(builder.getLocation(), builder.toJson());
    }

    public static void addItemModel(ResourceLocation loc, Supplier<JsonElement> obj) {
        addItemModel(loc, obj.get());
    }

    public static void addModel(ResourceLocation loc, JsonElement obj) {
        loc = MODEL_ID_CONVERTER.idToFile(loc);
        addResource(loc, obj);
    }

    public static void addModel(ResourceLocation loc, Supplier<JsonElement> obj) {
        addModel(loc, obj.get());
    }

    public static <T extends ModelBuilder<T>> void addModel(T builder) {
        addModel(builder.getLocation(), builder.toJson());
    }

    public static void addBlockState(ResourceLocation loc, JsonElement stateJson) {
        loc = BLOCKSTATE_ID_CONVERTER.idToFile(loc);
        addResource(loc, stateJson);
    }

    public static void addBlockState(ResourceLocation loc, Supplier<JsonElement> generator) {
        addBlockState(loc, generator.get());
    }

    public static void addBlockState(BlockStateGenerator generator) {
        addBlockState(BuiltInRegistries.BLOCK.getKey(generator.getBlock()), generator.get());
    }

    public static void addAtlasSpriteSource(ResourceLocation atlasLoc, SpriteSource source) {
        addAtlasSpriteSourceList(atlasLoc, Collections.singletonList(source));
    }

    public static void addAtlasSpriteSourceList(ResourceLocation loc, List<SpriteSource> sources) {
        loc = ATLAS_ID_CONVERTER.idToFile(loc);
        JsonElement sourceJson = SpriteSources.FILE_CODEC.encodeStart(JsonOps.INSTANCE, sources)
                .getOrThrow(false, error -> GTCEu.LOGGER.error("Failed to encode atlas sprite source. {}", error));
        addResource(loc, sourceJson);
    }

    public static void addBlockTexture(ResourceLocation loc, byte[] data) {
        ResourceLocation l = getTextureLocation("block", loc);
        if (ConfigHolder.INSTANCE.dev.dumpAssets) {
            Path parent = GTCEu.GTCEU_FOLDER.resolve("dumped/assets");
            writeByteArray(l, null, parent, data);
        }
        CONTENTS.addToData(l, data);
    }

    public static void addItemTexture(ResourceLocation loc, byte[] data) {
        ResourceLocation l = getTextureLocation("item", loc);
        if (ConfigHolder.INSTANCE.dev.dumpAssets) {
            Path parent = GTCEu.GTCEU_FOLDER.resolve("dumped/assets");
            writeByteArray(l, null, parent, data);
        }
        CONTENTS.addToData(l, data);
    }

    @ApiStatus.Internal
    public static void writeByteArray(ResourceLocation id, @Nullable String subdir, Path parent, byte[] data) {
        try {
            Path file;
            if (subdir != null) {
                // assume PNG
                file = parent.resolve(id.getNamespace()).resolve(subdir).resolve(id.getPath() + ".png");
            } else {
                // assume the file type is also appended if a full path is given.
                file = parent.resolve(id.getNamespace()).resolve(id.getPath());
            }
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                output.write(data);
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
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if (metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic assets"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        }
        return null;
    }

    @Override
    public String packId() {
        return this.name;
    }

    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void close() {
        // NOOP
    }

    public static ResourceLocation getTextureLocation(@Nullable String path, ResourceLocation textureId) {
        if (path == null) {
            return TEXTURE_ID_CONVERTER.idToFile(textureId);
        }
        return TEXTURE_ID_CONVERTER.idToFile(textureId.withPrefix(path + "/"));
    }
}
