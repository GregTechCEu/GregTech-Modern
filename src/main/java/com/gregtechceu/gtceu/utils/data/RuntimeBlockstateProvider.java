package com.gregtechceu.gtceu.utils.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.google.gson.JsonElement;
import com.tterrag.registrate.AbstractRegistrate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    protected final BiConsumer<ResourceLocation, JsonElement> consumer;

    // replace models() and blockModels() with versions that don't use ExistingModelFile
    protected final BlockModelProvider blockModels;
    protected final ItemModelProvider itemModels;

    public RuntimeBlockstateProvider(AbstractRegistrate<?> parent, PackOutput packOutput,
                                     BiConsumer<ResourceLocation, JsonElement> consumer) {
        super(parent, packOutput, RuntimeExistingFileHelper.INSTANCE);
        this.consumer = consumer;
        this.blockModels = new RuntimeBlockModelProvider(packOutput, parent.getModid());
        this.itemModels = new RuntimeItemModelProvider(packOutput, parent.getModid());
    }

    @Override
    public BlockModelProvider models() {
        return this.blockModels;
    }

    @Override
    public ItemModelProvider itemModels() {
        return this.itemModels;
    }

    @Override
    protected void registerStatesAndModels() {}

    @SuppressWarnings("deprecation")
    public void run() {
        processModelProvider(models());
        processModelProvider(itemModels());

        for (Map.Entry<Block, IGeneratedBlockState> entry : registeredBlocks.entrySet()) {
            ResourceLocation loc = GTDynamicResourcePack.BLOCKSTATE_ID_CONVERTER
                    .idToFile(BuiltInRegistries.BLOCK.getKey(entry.getKey()));
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

    protected static ResourceLocation extendWithFolder(ResourceLocation rl, String folder) {
        if (rl.getPath().contains("/")) {
            return rl;
        }
        return rl.withPrefix(folder + "/");
    }

    protected static class RuntimeBlockModelProvider extends BlockModelProvider {

        public RuntimeBlockModelProvider(PackOutput output, String modid) {
            super(output, modid, RuntimeExistingFileHelper.INSTANCE);
        }

        @Override
        public ModelFile.ExistingModelFile getExistingFile(ResourceLocation path) {
            return new FakeExistingModelFile(extendWithFolder(path, this.folder), this.existingFileHelper);
        }

        @Override
        public @NotNull CompletableFuture<?> run(CachedOutput cache) {
            return CompletableFuture.allOf();
        }

        @Override
        protected void registerModels() {}
    }


    protected static class RuntimeItemModelProvider extends ItemModelProvider {

        public RuntimeItemModelProvider(PackOutput output, String modid) {
            super(output, modid, RuntimeExistingFileHelper.INSTANCE);
        }

        @Override
        public ModelFile.ExistingModelFile getExistingFile(ResourceLocation path) {
            return new FakeExistingModelFile(extendWithFolder(path, this.folder), this.existingFileHelper);
        }

        @Override
        public @NotNull CompletableFuture<?> run(CachedOutput cache) {
            return CompletableFuture.allOf();
        }

        @Override
        protected void registerModels() {}
    }

    protected static class FakeExistingModelFile extends ModelFile.ExistingModelFile {

        public FakeExistingModelFile(ResourceLocation location, ExistingFileHelper existingHelper) {
            super(location, existingHelper);
        }

        // Bypass file existence checks so referencing an invalid model in KubeJS doesn't make the game crash
        @Override
        protected boolean exists() {
            return true;
        }
    }
}
