package com.gregtechceu.gtceu.data.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OreDataLoader extends SimpleJsonResourceReloadListener {
    public static OreDataLoader INSTANCE;
    public static final Gson GSON_INSTANCE = Deserializers.createFunctionSerializer().create();
    private static final String FOLDER = "ore_veins";
    protected static final Logger LOGGER = LogManager.getLogger();

    public OreDataLoader() {
        super(GSON_INSTANCE, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        for(Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                GTOreFeatureEntry ore = fromJson(location, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                if (ore == null) {
                    LOGGER.info("Skipping loading ore {} as it's serializer returned null", location);
                    continue;
                }
                GTOreFeatureEntry.ALL.put(location, ore);
            } catch (IllegalArgumentException | JsonParseException jsonParseException) {
                LOGGER.error("Parsing error loading recipe {}", location, jsonParseException);
            }
        }
        for (GTOreFeatureEntry entry : GTOreFeatureEntry.ALL.values()) {
            entry.datagenExt().build();
        }
    }

    public static GTOreFeatureEntry fromJson(ResourceLocation id, JsonObject json) {
        if (!json.has("id")) json.addProperty("id", id.toString());
        return GTOreFeatureEntry.DIRECT_CODEC.decode(JsonOps.INSTANCE, json).map(Pair::getFirst).getOrThrow(false, LOGGER::error);
    }
}
