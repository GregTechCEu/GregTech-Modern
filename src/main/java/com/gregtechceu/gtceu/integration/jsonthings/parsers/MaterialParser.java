package com.gregtechceu.gtceu.integration.jsonthings.parsers;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.integration.jsonthings.builders.MaterialBuilder;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.util.parse.JParse;
import dev.gigaherz.jsonthings.util.parse.value.IntValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Consumer;

public class MaterialParser extends ThingParser<MaterialBuilder> {
    public MaterialParser(IEventBus bus) {
        super(GSON, "material");
        bus.addListener(this::registerMaterials);
        bus.addListener(this::registerMaterials);
    }

    public void registerMaterialRegistries(MaterialRegistryEvent event) {
        LOGGER.info("Started registering material registries, errors about unexpected registry domains are harmless...");
        processAndConsumeErrors(this.getThingType(), this.getBuilders(), (thing) -> {
            String location = thing.getRegistryName().getNamespace();
            if (GTCEuAPI.materialManager.getRegistry(location) == GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID)) {
                GTCEuAPI.materialManager.createRegistry(location);
            }
        }, BaseBuilder::getRegistryName);
        LOGGER.info("Done processing thingpack Material Registries.");

    }

    public void registerMaterials(MaterialEvent event) {
        LOGGER.info("Started registering material things, errors about unexpected registry domains are harmless...");
        processAndConsumeErrors(this.getThingType(), this.getBuilders(), (thing) -> {
            ResourceLocation location = thing.getRegistryName();
            GTCEuAPI.materialManager.getRegistry(location.getNamespace()).register(location.getPath(), thing.get());
        }, BaseBuilder::getRegistryName);
        LOGGER.info("Done processing thingpack Materials.");
    }

    @Override
    protected MaterialBuilder processThing(ResourceLocation key, JsonObject data, Consumer<MaterialBuilder> builderModification) {
        MaterialBuilder builder = MaterialBuilder.begin(this, key);
        JParse.begin(data)
            .ifKey("material_info", (materialInfo) -> {
                materialInfo.obj().ifKey("colors", colors -> {
                    builder.getInternal().getMaterialInfo().setColors(colors.array().ints().flatMap(values -> values.map(IntValue::getAsInt).mapToInt(Integer::intValue).toArray()));
                }).ifKey("has_fluid_color", hasFluidColor -> {
                    builder.getInternal().getMaterialInfo().setHasFluidColor(hasFluidColor.bool().getAsBoolean());
                }).ifKey("icon_set", iconSet -> {
                    builder.getInternal().iconSet(MaterialIconSet.getByName(iconSet.string().getAsString()));
                }).ifKey("components", components -> {
                    builder.getInternal().kjs$components(components.array().map(MaterialStackParser::of).flatMap(values -> values.toArray(MaterialStackWrapper[]::new)));
                }).ifKey("element", element -> {
                    builder.getInternal().element(GTElements.get(element.string().getAsString()));
                });
            }).ifKey("properties", properties -> {
                properties.array().map(any -> {
                    var ref = new Object() {
                        IMaterialProperty<?> property = null;
                    };
                    any.ifObj(objValue -> {
                        objValue.ifKey("name", name -> {
                            ref.property = PropertyKey.getByName(name.string().getAsString()).;
                        });
                    }).ifString(stringValue -> ref.property = PropertyKey.getByName(stringValue.getAsString()));
                    return ref.property;
                });
            });
        return builder;
    }
}
