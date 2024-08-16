package com.gregtechceu.gtceu.integration.jsonthings.parsers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.common.data.GTElements;
import com.gregtechceu.gtceu.integration.jsonthings.builders.MaterialBuilder;
import com.gregtechceu.gtceu.integration.kjs.helpers.MaterialStackWrapper;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.util.parse.JParse;
import dev.gigaherz.jsonthings.util.parse.value.IntValue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.function.Consumer;

public class MaterialParser extends ThingParser<MaterialBuilder> {

    public MaterialParser(IEventBus bus) {
        super(GSON, "material");
        bus.addListener(this::registerMaterialRegistries);
        bus.addListener(this::registerMaterials);
    }

    public void registerMaterialRegistries(MaterialRegistryEvent event) {
        LOGGER.info(
                "Started registering Material registries, errors about unexpected registry domains are harmless...");
        processAndConsumeErrors(this.getThingType(), this.getBuilders(), (thing) -> {
            String namespace = thing.getRegistryName().getNamespace();
            if (!namespace.equals(GTCEu.MOD_ID) && GTCEuAPI.materialManager.getRegistry(namespace) ==
                    GTCEuAPI.materialManager.getRegistry(GTCEu.MOD_ID)) {
                GTCEuAPI.materialManager.createRegistry(namespace);
            }
        }, BaseBuilder::getRegistryName);
        LOGGER.info("Done processing thingpack Material Registries.");
    }

    public void registerMaterials(MaterialEvent event) {
        LOGGER.info("Started registering Material things, errors about unexpected registry domains are harmless...");
        processAndConsumeErrors(this.getThingType(), this.getBuilders(), (thing) -> {
            ResourceLocation location = thing.getRegistryName();
            LOGGER.info("loading material {}", location);
            thing.build();
        }, BaseBuilder::getRegistryName);
        LOGGER.info("Done processing thingpack Materials.");
    }

    @Override
    protected MaterialBuilder processThing(ResourceLocation key, JsonObject data,
                                           Consumer<MaterialBuilder> builderModification) {
        MaterialBuilder builder = MaterialBuilder.begin(this, key);
        JParse.begin(data)
                .ifKey("material_info", (materialInfo) -> {
                    materialInfo.obj().ifKey("colors", colors -> {
                        builder.getInternal().getMaterialInfo()
                                .setColors(colors.array().ints()
                                        .flatMap(values -> values.map(IntValue::getAsInt).mapToInt(Integer::intValue)
                                                .collect(IntArrayList::new, IntList::add, IntList::addAll)));
                    }).ifKey("has_fluid_color", hasFluidColor -> {
                        builder.getInternal().getMaterialInfo().setHasFluidColor(hasFluidColor.bool().getAsBoolean());
                    }).ifKey("icon_set", iconSet -> {
                        builder.getInternal().iconSet(MaterialIconSet.getByName(iconSet.string().getAsString()));
                    }).ifKey("components", components -> {
                        builder.getInternal()
                                .kjs$components((MaterialStackWrapper[]) components.array().map(MaterialStackParser::of)
                                        .flatMap(values -> values.toArray(MaterialStackWrapper[]::new)));
                    }).ifKey("element", element -> {
                        builder.getInternal().element(GTElements.get(element.string().getAsString()));
                    });
                }).ifKey("properties", properties -> {
                    properties.array().forEach((index, any) -> {
                        var objValue = any.obj();
                        objValue.ifKey("type", type -> {
                            PropertyKey<?> propertyKey = PropertyKey.getByName(type.string().getAsString());
                            var property = propertyKey.getCodec().parse(JsonOps.INSTANCE, objValue.getAsJsonObject())
                                    .getOrThrow(false, GTCEu.LOGGER::error);
                            builder.getInternal().getProperties().setPropertyNoGeneric(propertyKey, property);
                        });
                    });
                }).ifKey("flags", flags -> {
                    flags.array().forEach((index, any) -> {
                        String name = any.string().getAsString();
                        builder.getInternal().flags(MaterialFlag.getByName(name));
                    });
                });
        builderModification.accept(builder);
        return builder;
    }
}
