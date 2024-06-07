package com.gregtechceu.gtceu.common.commands.arguments;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MedicalConditionParser {

    private static final SimpleCommandExceptionType ERROR_NO_TAGS_ALLOWED = new SimpleCommandExceptionType(
            Component.translatable("argument.item.tag.disallowed"));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
            id -> Component.translatable("argument.item.id.invalid", id));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
            tag -> Component.translatable("arguments.item.tag.unknown", tag));
    private static final char SYNTAX_START_NBT = '{';
    private static final char SYNTAX_TAG = '#';
    private static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    private final StringReader reader;
    private MedicalCondition result;
    /**
     * Builder to be used when creating a list of suggestions
     */
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

    private MedicalConditionParser(StringReader reader) {
        this.reader = reader;
    }

    public static MedicalCondition parseForMedicalCondition(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        try {
            MedicalConditionParser materialParser = new MedicalConditionParser(reader);
            materialParser.parse();
            return materialParser.result;
        } catch (CommandSyntaxException var5) {
            reader.setCursor(i);
            throw var5;
        }
    }

    public static CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        MedicalConditionParser materialParser = new MedicalConditionParser(stringReader);

        try {
            materialParser.parse();
        } catch (CommandSyntaxException ignored) {}

        return materialParser.suggestions.apply(builder.createOffset(stringReader.getCursor()));
    }

    private void readMedicalCondition() throws CommandSyntaxException {
        int i = this.reader.getCursor();
        String name = reader.getString();
        MedicalCondition material = MedicalCondition.CONDITIONS.get(name);
        this.result = Optional.ofNullable(material).orElseThrow(() -> {
            this.reader.setCursor(i);
            return ERROR_UNKNOWN_ITEM.createWithContext(this.reader, name);
        });
    }

    private void parse() throws CommandSyntaxException {
        this.suggestions = this::suggestMedicalCondition;
        this.readMedicalCondition();
    }

    private CompletableFuture<Suggestions> suggestMedicalCondition(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(MedicalCondition.CONDITIONS.keySet(), builder);
    }
}
