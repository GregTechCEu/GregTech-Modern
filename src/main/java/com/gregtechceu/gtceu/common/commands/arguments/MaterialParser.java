package com.gregtechceu.gtceu.common.commands.arguments;

import com.gregtechceu.gtceu.api.data.chemical.material.IMaterialRegistryManager;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MaterialParser {

    private static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
            Component.translatable("argument.item.tag.disallowed"));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
            id -> Component.translatable("argument.item.id.invalid", id));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
            tag -> Component.translatable("arguments.item.tag.unknown", tag));
    private static final char SYNTAX_START_NBT = '{';
    private static final char SYNTAX_TAG = '#';
    private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    private final IMaterialRegistryManager materials;
    private final StringReader reader;
    private Material result;
    /**
     * Builder to be used when creating a list of suggestions
     */
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    private MaterialParser(IMaterialRegistryManager materials, StringReader reader) {
        this.materials = materials;
        this.reader = reader;
    }

    public static Material parseForMaterial(IMaterialRegistryManager registry,
                                            StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        try {
            MaterialParser materialParser = new MaterialParser(registry, reader);
            materialParser.parse();
            return materialParser.result;
        } catch (CommandSyntaxException var5) {
            reader.setCursor(i);
            throw var5;
        }
    }

    public static CompletableFuture<Suggestions> fillSuggestions(IMaterialRegistryManager lookup,
                                                                 SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        MaterialParser materialParser = new MaterialParser(lookup, stringReader);

        try {
            materialParser.parse();
        } catch (CommandSyntaxException ignored) {}

        return materialParser.suggestions.apply(builder.createOffset(stringReader.getCursor()));
    }

    private void readMaterial() throws CommandSyntaxException {
        int i = this.reader.getCursor();
        ResourceLocation resourceLocation = ResourceLocation.read(this.reader);
        Material material = this.materials.getRegistry(resourceLocation.getNamespace()).get(resourceLocation.getPath());
        this.result = Optional.ofNullable(material).orElseThrow(() -> {
            this.reader.setCursor(i);
            return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, resourceLocation);
        });
    }

    private void parse() throws CommandSyntaxException {
        this.suggestions = this::suggestMaterial;
        this.readMaterial();
    }

    private CompletableFuture<Suggestions> suggestMaterial(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(
                this.materials.getRegisteredMaterials().stream().map(Material::getResourceLocation), builder);
    }
}
