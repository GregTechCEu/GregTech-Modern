package com.gregtechceu.gtceu.utils.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.*;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.tterrag.registrate.AbstractRegistrate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RuntimeBlockstateProvider extends GTBlockstateProvider {

    // Fake a data provider for the GT model builders so we don't need to handle this ourselves in any way :3
    public static final RuntimeBlockstateProvider INSTANCE = new RuntimeBlockstateProvider(
            GTRegistration.REGISTRATE, new PackOutput(GTCEu.GTCEU_FOLDER),
            (loc, json) -> {
                if (!loc.getPath().endsWith(".json")) {
                    loc = loc.withSuffix(".json");
                }
                GTDynamicResourcePack.addResource(loc, json);
            });

    protected final BiConsumer<Identifier, JsonElement> consumer;
    private final List<BlockModelDefinitionGenerator> registeredBlockStates;

    public RuntimeBlockstateProvider(AbstractRegistrate<?> parent, PackOutput packOutput,
                                     BiConsumer<Identifier, JsonElement> consumer) {
        this(parent, packOutput, consumer, new ArrayList<>());
    }

    private RuntimeBlockstateProvider(AbstractRegistrate<?> parent, PackOutput packOutput,
                                      BiConsumer<Identifier, JsonElement> consumer,
                                      List<BlockModelDefinitionGenerator> registeredBlockStates) {
        super(parent, packOutput, registeredBlockStates::add, new ItemModelOutput() {

            @Override
            public void accept(net.minecraft.world.item.Item item,
                               net.minecraft.client.renderer.item.ItemModel.Unbaked model,
                               net.minecraft.client.renderer.item.ClientItem.Properties properties) {}

            @Override
            public void copy(net.minecraft.world.item.Item source, net.minecraft.world.item.Item target) {}
        }, (loc, model) -> consumer.accept(loc.withPrefix("models/"), model.get()),
                RuntimeExistingFileHelper.INSTANCE);
        this.consumer = consumer;
        this.registeredBlockStates = registeredBlockStates;
    }

    public void run() {
        processModelProvider(models());
        processModelProvider(itemModels());

        for (BlockModelDefinitionGenerator generator : registeredBlockStates) {
            Identifier loc = GTDynamicResourcePack.BLOCKSTATE_ID_CONVERTER
                    .idToFile(BuiltInRegistries.BLOCK.getKey(generator.block()));
            JsonElement stateJson = BlockStateModelDispatcher.CODEC.encodeStart(JsonOps.INSTANCE, generator.create())
                    .getOrThrow();
            this.consumer.accept(loc, stateJson);
        }
        // only clear the data *after* saving so we can keep track of it during the KJS event
        models().generatedModels.clear();
        itemModels().generatedModels.clear();
        registeredBlockStates.clear();
    }

    public <T extends ModelBuilder<T>> void processModelProvider(ModelProvider<T> provider) {
        for (T model : provider.generatedModels.values()) {
            Identifier loc = model.getLocation().withPrefix("models/");
            this.consumer.accept(loc, model.toJson());
        }
    }
}
