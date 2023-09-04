package com.gregtechceu.gtceu.data.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.NoopVeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTFeatures;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinEventJS;
import com.lowdragmc.lowdraglib.Platform;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

public class OreDataLoader extends SimpleJsonResourceReloadListener {
    public static OreDataLoader INSTANCE;
    public static final Gson GSON_INSTANCE = Deserializers.createFunctionSerializer().create();
    private static final String FOLDER = "gtceu/ore_veins";
    protected static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private final RegistryAccess registryAccess;

    public OreDataLoader(@Nullable RegistryAccess registryAccess) {
        super(GSON_INSTANCE, FOLDER);
        this.registryAccess = registryAccess;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        GTFeatures.register();
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registryAccess == null ? Platform.getMinecraftServer().registryAccess() : this.registryAccess);
        for(Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                GTOreDefinition ore = fromJson(location, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), ops);
                if (ore == null) {
                    LOGGER.info("Skipping loading ore vein {} as it's serializer returned null", location);
                } else if (ore.getVeinGenerator() instanceof NoopVeinGenerator) {
                    LOGGER.info("Removing ore vein {} as it's generator was marked as no-operation", location);
                    GTRegistries.ORE_VEINS.remove(location);
                } else if (GTRegistries.ORE_VEINS.containKey(location)) {
                    GTRegistries.ORE_VEINS.replace(location, ore);
                } else {
                    GTRegistries.ORE_VEINS.register(location, ore);
                }
            } catch (IllegalArgumentException | JsonParseException jsonParseException) {
                LOGGER.error("Parsing error loading ore vein {}", location, jsonParseException);
            }
        }
        if (GTCEu.isKubeJSLoaded()) {
            RunKJSEventInSeparateClassBecauseForgeIsDumb.fireKJSEvent();
        }
        for (GTOreDefinition entry : GTRegistries.ORE_VEINS) {
            if (entry.getVeinGenerator() != null) {
                entry.getVeinGenerator().build();
            } else {
                GTRegistries.ORE_VEINS.remove(GTRegistries.ORE_VEINS.getKey(entry));
            }
        }
    }

    public static GTOreDefinition fromJson(ResourceLocation id, JsonObject json, RegistryOps<JsonElement> ops) {
        return GTOreDefinition.FULL_CODEC.decode(ops, json).map(Pair::getFirst).getOrThrow(false, LOGGER::error);
    }

    /**
     * Holy shit this is dumb, thanks forge for trying to classload things that are never called!
     */
    public static final class RunKJSEventInSeparateClassBecauseForgeIsDumb {
        public static void fireKJSEvent() {
            GTCEuServerEvents.ORE_VEIN_MODIFICATION.post(ScriptType.SERVER, new GTOreVeinEventJS());
        }
    }
}
