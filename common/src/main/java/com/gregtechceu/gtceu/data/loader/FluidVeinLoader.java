package com.gregtechceu.gtceu.data.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTFluidVeinEventJS;
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

public class FluidVeinLoader extends SimpleJsonResourceReloadListener {
    public static FluidVeinLoader INSTANCE;
    public static final Gson GSON_INSTANCE = Deserializers.createFunctionSerializer().create();
    private static final String FOLDER = "gtceu/fluid_veins";
    protected static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private final RegistryAccess registryAccess;

    public FluidVeinLoader(@Nullable RegistryAccess registryAccess) {
        super(GSON_INSTANCE, FOLDER);
        this.registryAccess = registryAccess;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registryAccess == null ? Platform.getMinecraftServer().registryAccess() : this.registryAccess);
        for(Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                BedrockFluidDefinition fluid = fromJson(location, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), ops);
                if (fluid == null) {
                    LOGGER.info("Skipping loading fluid vein {} as it's serializer returned null", location);
                } /*else if(fluid.getVeinGenerator() instanceof NoopVeinGenerator) {
                    LOGGER.info("Removing fluid vein {} as it's generator was marked as no-operation", location);
                    GTRegistries.BEDROCK_FLUID_DEFINITIONS.remove(location);
                }*/else if (GTRegistries.BEDROCK_FLUID_DEFINITIONS.containKey(location)) {
                    GTRegistries.BEDROCK_FLUID_DEFINITIONS.replace(location, fluid);
                }  else {
                    GTRegistries.BEDROCK_FLUID_DEFINITIONS.register(location, fluid);
                }
            } catch (IllegalArgumentException | JsonParseException jsonParseException) {
                LOGGER.error("Parsing error loading ore vein {}", location, jsonParseException);
            }
        }
        if (GTCEu.isKubeJSLoaded()) {
            RunKJSEventInSeparateClassBecauseForgeIsDumb.fireKJSEvent();
        }
    }

    public static BedrockFluidDefinition fromJson(ResourceLocation id, JsonObject json, RegistryOps<JsonElement> ops) {
        return BedrockFluidDefinition.FULL_CODEC.decode(ops, json).map(Pair::getFirst).getOrThrow(false, LOGGER::error);
    }

    /**
     * Holy shit this is dumb, thanks forge for trying to classload things that are never called!
     */
    public static final class RunKJSEventInSeparateClassBecauseForgeIsDumb {
        public static void fireKJSEvent() {
            GTCEuServerEvents.FLUID_VEIN_MODIFICATION.post(ScriptType.SERVER, new GTFluidVeinEventJS());
        }
    }
}

