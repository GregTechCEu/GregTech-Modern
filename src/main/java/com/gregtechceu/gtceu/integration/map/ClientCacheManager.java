package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.cache.client.IClientCache;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ClientCacheManager {

    public static final File clientCacheDir = new File(Minecraft.getInstance().gameDirectory,
            GTCEu.MOD_ID + File.separator + "prospection_cache");
    private static final char resourceLocationSeparator = '=';
    private static final String filePrefix = "DIM";
    private static final String fileEnding = ".nbt";
    @Getter
    private static File worldFolder;
    private static final Reference2ObjectMap<IClientCache, ClientCacheInfo> caches = new Reference2ObjectArrayMap<>();
    private static boolean shouldInit = true;

    private static String sanitizeFilename(String input) {
        return input.replaceAll("(?U)[^\\w-]+", "_").trim();
    }

    public static void init(String worldId) {
        if (shouldInit) {
            final Player player = Minecraft.getInstance().player;
            worldFolder = new File(clientCacheDir,
                    sanitizeFilename(player.getName().getString()) + "_" + player.getUUID() +
                            File.separator + sanitizeFilename(worldId));
            worldFolder.mkdirs();
            // to ensure any cache data that might somehow be lying around gets dealt with
            clearCaches();
            loadCaches();
            shouldInit = false;
        }
    }

    private static void loadCaches() {
        for (IClientCache cache : caches.keySet()) {
            cache.setupCacheFiles();
            ClientCacheInfo cacheInfo = caches.get(cache);
            cacheInfo.cacheFolder = new File(worldFolder, cacheInfo.key);
            cacheInfo.cacheFolder.mkdirs();
            for (String dimFilePrefix : cacheInfo.dimFilePrefixes) {
                for (File dimFile : getDimFiles(cacheInfo.cacheFolder, dimFilePrefix)) {
                    ResourceKey<Level> dimId = ResourceKey.create(Registries.DIMENSION,
                            ResourceLocation.of(
                                    dimFile.getName().substring(dimFilePrefix.length() + filePrefix.length(),
                                            dimFile.getName().length() - fileEnding.length()),
                                    resourceLocationSeparator));
                    try {
                        cache.readDimFile(dimFilePrefix, dimId, NbtIo.readCompressed(new FileInputStream(dimFile)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (String singleFileName : cacheInfo.singleFiles) {
                File singleFile = new File(cacheInfo.cacheFolder, singleFileName + fileEnding);
                if (!singleFile.exists()) continue;
                try {
                    cache.readSingleFile(singleFileName, NbtIo.readCompressed(new FileInputStream(singleFile)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void clearCaches() {
        for (IClientCache cache : caches.keySet()) {
            cache.clear();
        }
        GroupingMapRenderer.getInstance().clear();
    }

    public static void saveCaches() {
        for (IClientCache cache : caches.keySet()) {
            ClientCacheInfo cacheInfo = caches.get(cache);
            for (String dimFilePrefix : cacheInfo.dimFilePrefixes) {
                for (ResourceKey<Level> dim : cache.getExistingDimensions(dimFilePrefix)) {
                    CompoundTag data = cache.saveDimFile(dimFilePrefix, dim);
                    if (data == null) continue;
                    File dimFile = new File(cacheInfo.cacheFolder,
                            dimFilePrefix + filePrefix + dim.location().getNamespace() + "=" +
                                    dim.location().getPath() + fileEnding);
                    try {
                        NbtIo.writeCompressed(data, new FileOutputStream(dimFile));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (String singleFileName : cacheInfo.singleFiles) {
                CompoundTag data = cache.saveSingleFile(singleFileName);
                if (data == null) continue;
                File singleFile = new File(cacheInfo.cacheFolder, singleFileName + fileEnding);
                try {
                    NbtIo.writeCompressed(data, new FileOutputStream(singleFile));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void resetCaches() {
        clearCaches();
        for (ClientCacheInfo cacheInfo : caches.values()) {
            FileUtils.deleteQuietly(cacheInfo.cacheFolder);
            cacheInfo.cacheFolder.mkdirs();
        }
    }

    public static void registerClientCache(IClientCache cache, String key) {
        caches.put(cache, new ClientCacheInfo(key));
    }

    public static void addDimFiles(IClientCache cache, String prefix) {
        caches.get(cache).dimFilePrefixes.add(prefix);
    }

    public static void addSingleFile(IClientCache cache, String prefix) {
        caches.get(cache).singleFiles.add(prefix);
    }

    public static List<ProspectionInfo> getProspectionShareData() {
        List<ProspectionInfo> result = new ArrayList<>();
        for (IClientCache cache : caches.keySet()) {
            ClientCacheInfo cacheInfo = caches.get(cache);
            for (String dimPrefix : cacheInfo.dimFilePrefixes) {
                for (ResourceKey<Level> dim : cache.getExistingDimensions(dimPrefix)) {
                    CompoundTag data = cache.saveDimFile(dimPrefix, dim);
                    if (data == null) continue;
                    result.add(new ProspectionInfo(cacheInfo.key, dimPrefix, true, dim, data));
                }
            }
            for (String singleFileName : cacheInfo.singleFiles) {
                CompoundTag data = cache.saveSingleFile(singleFileName);
                if (data == null) continue;
                result.add(new ProspectionInfo(cacheInfo.key, singleFileName, false, Level.OVERWORLD, data));
            }
        }
        return result;
    }

    public static void processProspectionShare(String cacheName, String key, boolean isDimCache, ResourceKey<Level> dim,
                                               CompoundTag data) {
        for (IClientCache cache : caches.keySet()) {
            ClientCacheInfo cacheInfo = caches.get(cache);
            if (cacheInfo.key.equals(cacheName)) {
                if (isDimCache) {
                    cache.readDimFile(key, dim, data);
                } else {
                    cache.readSingleFile(key, data);
                }
                break;
            }
        }
    }

    public static void allowReinit() {
        shouldInit = true;
    }

    private static List<File> getDimFiles(File parent, String prefix) {
        try (var stream = Files.walk(parent.toPath(), 1)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(prefix + filePrefix))
                    .map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ClientCacheInfo {

        public String key;
        public File cacheFolder;
        public Set<String> dimFilePrefixes;
        public Set<String> singleFiles;

        public ClientCacheInfo(String key) {
            this.key = key;
            dimFilePrefixes = new HashSet<>();
            singleFiles = new HashSet<>();
        }
    }

    public static class ProspectionInfo {

        public String cacheName;
        public String key;
        public boolean isDimCache;
        public ResourceKey<Level> dim;
        public CompoundTag data;

        public ProspectionInfo(String cacheName, String key, boolean isDimCache, ResourceKey<Level> dim,
                               CompoundTag data) {
            this.cacheName = cacheName;
            this.key = key;
            this.isDimCache = isDimCache;
            this.dim = dim;
            this.data = data;
        }
    }
}
