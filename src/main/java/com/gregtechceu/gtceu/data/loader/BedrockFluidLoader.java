package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncBedrockOreVeins;
import com.gregtechceu.gtceu.data.worldgen.GTBedrockFluids;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTFluidVeinEventJS;

import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.network.PacketDistributor;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class BedrockFluidLoader extends SimpleJsonResourceReloadListener {

    public static final Gson GSON_INSTANCE = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().setLenient()
            .create();
    private static final String FOLDER = "gtceu/fluid_veins";
    protected static final Logger LOGGER = LogManager.getLogger();

    public BedrockFluidLoader() {
        super(GSON_INSTANCE, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        if (GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        }
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry().clear();

        GTBedrockFluids.init();
        ModLoader.postEvent(new GTCEuAPI.RegisterEvent(GTRegistries.BEDROCK_FLUID_DEFINITIONS));
        if (GTCEu.isKubeJSLoaded()) {
            KJSCallWrapper.fireKJSEvent();
        }
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                BedrockFluidDefinition fluid = fromJson(location,
                        GsonHelper.convertToJsonObject(entry.getValue(), "top element"), ops);
                if (fluid == null) {
                    LOGGER.info("Skipping loading fluid vein {} as it's serializer returned null", location);
                }
                GTRegistries.BEDROCK_FLUID_DEFINITIONS.registerOrOverride(location, fluid);
            } catch (IllegalArgumentException | JsonParseException jsonParseException) {
                LOGGER.error("Parsing error loading ore vein {}", location, jsonParseException);
            }
        }

        if (!GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.freeze();
        }
    }

    public static BedrockFluidDefinition fromJson(ResourceLocation id, JsonObject json, RegistryOps<JsonElement> ops) {
        return BedrockFluidDefinition.FULL_CODEC.parse(ops, json).getOrThrow();
    }

    public static final class KJSCallWrapper {

        public static void fireKJSEvent() {
            GTCEuServerEvents.FLUID_VEIN_MODIFICATION.post(ScriptType.SERVER, new GTFluidVeinEventJS());
        }
    }
}
