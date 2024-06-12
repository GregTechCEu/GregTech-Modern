package com.gregtechceu.gtceu.common.commands.arguments;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class MedicalConditionArgument implements ArgumentType<MedicalCondition> {

    private static final Collection<String> EXAMPLES = Arrays.asList("chemical_burns", "carcinogen", "asbestosis");

    public MedicalConditionArgument() {}

    public static MedicalConditionArgument medicalCondition() {
        return new MedicalConditionArgument();
    }

    @Override
    public MedicalCondition parse(StringReader reader) throws CommandSyntaxException {
        return MedicalConditionParser.parseForMedicalCondition(reader);
    }

    public static <S> MedicalCondition getCondition(CommandContext<S> context, String name) {
        return context.getArgument(name, MedicalCondition.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return MedicalConditionParser.fillSuggestions(builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
