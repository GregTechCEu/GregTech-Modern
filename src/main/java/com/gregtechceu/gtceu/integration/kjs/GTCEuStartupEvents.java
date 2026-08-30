package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.kjs.events.*;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.rhino.type.TypeInfo;

public interface GTCEuStartupEvents {

    EventTargetType<ResourceKey<Registry<?>>> REGISTRY_ASSUME_GT = Cast.to(EventTargetType.create(ResourceKey.class)
            .transformer(GTCEuStartupEvents::toRegistryKeyAssumeGTNamespace)
            .identity()
            .describeType(TypeInfo.of(ResourceKey.class)
                    .withParams(TypeInfo.of(Registry.class))));

    EventGroup GROUP = EventGroup.of("GTCEuStartupEvents");

    EventHandler MATERIAL_ICON_INFO = GROUP.startup("materialIconInfo", () -> MaterialIconInfoEventJS.class);

    TargetedEventHandler<ResourceKey<Registry<?>>> REGISTRY = GROUP.startup("registry", () -> GTRegistryKubeEvent.class)
            .requiredTarget(REGISTRY_ASSUME_GT);
    EventHandler MATERIAL_MODIFICATION = GROUP.startup("materialModification", () -> MaterialModificationEventJS.class);
    EventHandler CRAFTING_COMPONENTS = GROUP.startup("craftingComponents", () -> CraftingComponentsEventJS.class);

    EventHandler REGISTER_WOODS = GROUP.startup("registerWoods", () -> RegisterWoodsEventJS.class);
    EventHandler MACHINE_MODIFICATION = GROUP.startup("machineModification", () -> ModifyMachineEventJS.class);
    EventHandler REGISTER_SPOILABLES = GROUP.startup("registerSpoilables", () -> RegisterSpoilablesEventJS.class);

    @SuppressWarnings("rawtypes")
    private static ResourceKey<? extends Registry<?>> toRegistryKeyAssumeGTNamespace(Object object) {
        return switch (object) {
            case null -> null;
            case ResourceKey rl -> rl;
            case ResourceLocation rl -> ResourceKey.createRegistryKey(rl);
            default -> {
                String s = object.toString();
                yield s.isBlank() ? null : ResourceKey.createRegistryKey(GTCEu.id(s));
            }
        };
    }
}
