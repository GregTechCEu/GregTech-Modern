package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import java.util.stream.Collectors;

public class MaterialLang {

    public static void generateMaterialLang(GTLangProvider provider, String namespace) {
        for (Material material : GTRegistries.MATERIALS.values().stream().filter(v -> v.getModid().equals(namespace))
                .collect(Collectors.toSet())) {
            provider.add(material.getUnlocalizedName(), material.getDefaultTranslation());

            for (var entry : material.getLangOverrides().entrySet()) {
                var key = String.format("item.%s.%s", material.getResourceLocation().getNamespace(),
                        entry.getKey().idPattern().formatted(material.getResourceLocation().getPath()));
                provider.add(key, entry.getValue());
            }
        }
    }

    public static void init(GTLangProvider provider) {
        generateCustomMaterialNames(provider);
        generateFluidKeys(provider);

        // Cauldron Washing
        provider.add("tagprefix.gtceu.impure_dust.purify_tooltip", "Right click a Cauldron to get clean Dust");
        provider.add("tagprefix.gtceu.crushed_ore.purify_tooltip", "Right click a Cauldron to get Purified Ore");
    }

    private static void generateCustomMaterialNames(GTLangProvider provider) {
        provider.add("item.gtceu.nether_quartz_netherrack", "Nether Quartz Ore");
    }

    private static void generateFluidKeys(GTLangProvider provider) {
        // Fluid Types
        provider.add("material.gtceu.fluid_type.liquid_generic", "Liquid %s");

        provider.add("material.gtceu.fluid_type.generic", "%s");

        provider.add("material.gtceu.fluid_type.gas_generic", "%s Gas");
        provider.add("material.gtceu.fluid_type.gas_vapor", "%s Vapor");
        provider.add("material.gtceu.fluid_type.plasma", "%s Plasma");
        provider.add("material.gtceu.fluid_type.molten", "Molten %s");

        // Fluid Tooltips
        provider.add("material.gtceu.fluid_state.gas", "State: Gaseous");
        provider.add("material.gtceu.fluid_state.liquid", "State: Liquid");
        provider.add("material.gtceu.fluid_state.plasma", "State: Plasma");
        provider.add("material.gtceu.fluid_property.temperature", "Temperature: %s");
        provider.add("material.gtceu.fluid_property.cryogenic", "Cryogenic! Handle with care!");
        provider.add("material.gtceu.fluid_property.acid", "Acidic! Handle with care!");
    }
}
