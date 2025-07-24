package com.gregtechceu.gtceu.utils.data;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.*;

import com.google.gson.JsonElement;
import com.tterrag.registrate.AbstractRegistrate;

import java.util.Map;
import java.util.function.BiConsumer;

public class RuntimeBlockStateProvider extends GTBlockstateProvider {

    protected final BiConsumer<ResourceLocation, JsonElement> consumer;

    public RuntimeBlockStateProvider(AbstractRegistrate<?> parent, PackOutput packOutput,
                                     BiConsumer<ResourceLocation, JsonElement> consumer) {
        super(parent, packOutput, RuntimeExistingFileHelper.INSTANCE);
        this.consumer = consumer;
    }

    @Override
    protected void registerStatesAndModels() {}

    @SuppressWarnings("deprecation")
    public void run() {
        processModelProvider(models());
        processModelProvider(itemModels());

        for (Map.Entry<Block, IGeneratedBlockState> entry : registeredBlocks.entrySet()) {
            ResourceLocation loc = BuiltInRegistries.BLOCK.getKey(entry.getKey()).withPrefix("blockstates/");
            this.consumer.accept(loc, entry.getValue().toJson());
        }
        // only clear the data *after* saving so we can keep track of it during the KJS event
        models().generatedModels.clear();
        itemModels().generatedModels.clear();
        registeredBlocks.clear();
    }

    public <T extends ModelBuilder<T>> void processModelProvider(ModelProvider<T> provider) {
        for (T model : provider.generatedModels.values()) {
            ResourceLocation loc = model.getLocation().withPrefix("models/");
            this.consumer.accept(loc, model.toJson());
        }
    }
}
