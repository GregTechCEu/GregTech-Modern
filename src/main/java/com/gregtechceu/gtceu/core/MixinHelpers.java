package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockFluidVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinEventJS;

import net.minecraft.core.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class MixinHelpers {

    public static void postKJSVeinEvents(RegistryAccess.Frozen registries) {
        if (!GTCEu.Mods.isKubeJSLoaded()) {
            return;
        }
        KJSCallWrapper.updateRegistryAccessContainer(registries);

        KJSCallWrapper.postEventWithRegistry(KJSCallWrapper::postOreVeinEvent,
                registries.registryOrThrow(GTRegistries.Keys.ORE_VEIN));

        KJSCallWrapper.postEventWithRegistry(KJSCallWrapper::postBedrockFluidEvent,
                registries.registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID));

        KJSCallWrapper.postEventWithRegistry(KJSCallWrapper::postBedrockOreEvent,
                registries.registryOrThrow(GTRegistries.Keys.BEDROCK_ORE));
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
        if (extensions instanceof GTClientFluidTypeExtensions gtExtensions && value.getBuilder() != null) {
            value.getBuilder().determineTextures(material, value.getKey());

            gtExtensions.setFlowingTexture(value.getBuilder().flowing());
            gtExtensions.setStillTexture(value.getBuilder().still());
        }
    }

    private static final class KJSCallWrapper {

        private static <T> void postEventWithRegistry(Consumer<WritableRegistry<T>> eventProvider,
                                                      Registry<T> registry) {
            if (registry instanceof MappedRegistry<T> writable) {
                // unfreeze the registry, register to it, refreeze it.
                writable.unfreeze();
                eventProvider.accept(writable);
                writable.freeze();
            }
        }

        private static void postOreVeinEvent(WritableRegistry<GTOreDefinition> registry) {
            GTCEuServerEvents.ORE_VEIN_MODIFICATION.post(new GTOreVeinEventJS(registry));
        }

        private static void postBedrockFluidEvent(WritableRegistry<BedrockFluidDefinition> registry) {
            GTCEuServerEvents.FLUID_VEIN_MODIFICATION.post(new GTBedrockFluidVeinEventJS(registry));
        }

        private static void postBedrockOreEvent(WritableRegistry<BedrockOreDefinition> registry) {
            GTCEuServerEvents.BEDROCK_ORE_VEIN_MODIFICATION.post(new GTBedrockOreVeinEventJS(registry));
        }

        private static void updateRegistryAccessContainer(RegistryAccess.Frozen registriesWithEverything) {
            if (RegistryAccessContainer.current.access().registries().count() <
                    registriesWithEverything.registries().count()) {
                RegistryAccessContainer.current = new RegistryAccessContainer(registriesWithEverything);
            }
        }
    }
}
