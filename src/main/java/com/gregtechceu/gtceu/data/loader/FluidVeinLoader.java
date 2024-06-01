package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBedrockFluids;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncBedrockOreVeins;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTFluidVeinEventJS;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.fml.ModLoader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class FluidVeinLoader extends SimpleJsonResourceReloadListener {

    public static FluidVeinLoader INSTANCE;
    public static final Gson GSON_INSTANCE = Deserializers.createFunctionSerializer().create();
    private static final String FOLDER = "gtceu/fluid_veins";
    protected static final Logger LOGGER = LogManager.getLogger();

    public FluidVeinLoader() {
        super(GSON_INSTANCE, FOLDER);
        INSTANCE = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        if (GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        }
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry().clear();

        GTBedrockFluids.init();
        AddonFinder.getAddons().forEach(IGTAddon::registerFluidVeins);
        ModLoader.get().postEvent(
                new GTCEuAPI.RegisterEvent<>(GTRegistries.BEDROCK_FLUID_DEFINITIONS, BedrockFluidDefinition.class));
        if (GTCEu.isKubeJSLoaded()) {
            RunKJSEventInSeparateClassBecauseForgeIsDumb.fireKJSEvent();
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

        if (Platform.getMinecraftServer() != null) {
            GTNetwork.NETWORK
                    .sendToAll(new SPacketSyncBedrockOreVeins(GTRegistries.BEDROCK_ORE_DEFINITIONS.registry()));
        }
    }

    public static BedrockFluidDefinition fromJson(ResourceLocation id, JsonObject json, RegistryOps<JsonElement> ops) {
        return BedrockFluidDefinition.FULL_CODEC.parse(ops, json).getOrThrow(false, LOGGER::error);
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
