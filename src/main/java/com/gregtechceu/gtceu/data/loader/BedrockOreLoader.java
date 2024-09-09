package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncFluidVeins;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinEventJS;

import com.lowdragmc.lowdraglib.Platform;

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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BedrockOreLoader extends SimpleJsonResourceReloadListener {

    public static final Gson GSON_INSTANCE = new GsonBuilder().disableHtmlEscaping().setLenient().create();
    public static final String FOLDER = "gtceu/bedrock_ore_veins";
    protected static final Logger LOGGER = LogManager.getLogger();

    public BedrockOreLoader() {
        super(GSON_INSTANCE, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        if (GTRegistries.BEDROCK_ORE_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.unfreeze();
        }
        GTRegistries.BEDROCK_ORE_DEFINITIONS.registry().clear();

        ModLoader.postEvent(new GTCEuAPI.RegisterEvent(GTRegistries.BEDROCK_ORE_DEFINITIONS));
        if (GTCEu.isKubeJSLoaded()) {
            KJSCallWrapper.fireKJSEvent();
        }

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation location = entry.getKey();

            try {
                BedrockOreDefinition bedrockOre = fromJson(location,
                        GsonHelper.convertToJsonObject(entry.getValue(), "top element"), ops);
                if (bedrockOre == null) {
                    LOGGER.info("Skipping loading bedrock ore vein {} as it's serializer returned null", location);
                }
                GTRegistries.BEDROCK_ORE_DEFINITIONS.registerOrOverride(location, bedrockOre);
            } catch (IllegalArgumentException | JsonParseException jsonParseException) {
                LOGGER.error("Parsing error loading bedrock ore vein {}", location, jsonParseException);
            }
        }

        if (!GTRegistries.BEDROCK_ORE_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.freeze();
        }
    }

    public static BedrockOreDefinition fromJson(ResourceLocation id, JsonObject json, RegistryOps<JsonElement> ops) {
        return BedrockOreDefinition.FULL_CODEC.parse(ops, json).getOrThrow();
    }

    public static final class KJSCallWrapper {

        public static void fireKJSEvent() {
            GTCEuServerEvents.BEDROCK_ORE_VEIN_MODIFICATION.post(ScriptType.SERVER, new GTBedrockOreVeinEventJS());
        }
    }
}
