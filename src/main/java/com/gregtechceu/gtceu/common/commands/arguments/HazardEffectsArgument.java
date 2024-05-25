package com.gregtechceu.gtceu.common.commands.arguments;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HazardEffectsArgument implements ArgumentType<List<HazardProperty.HazardEffect>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("[{}]", "gtceu:steel");

    @Override
    public List<HazardProperty.HazardEffect> parse(StringReader reader) throws CommandSyntaxException {
        Tag tag = new TagParser(reader).readValue();
        if (!(tag instanceof ListTag listTag)) {
            throw TagParser.ERROR_INVALID_ARRAY.create(tag);
        }
        List<HazardProperty.HazardEffect> effects = new ArrayList<>();
        for (Tag inner : listTag) {
            if (!(inner instanceof CompoundTag compoundTag)) {
                throw TagParser.ERROR_EXPECTED_VALUE.create();
            }
            var effect = HazardProperty.HazardEffect.deserializeNBT(compoundTag);
            effects.add(effect);
        }
        return effects;
    }

    @SuppressWarnings("unchecked")
    public static <S> List<HazardProperty.HazardEffect> getEffects(CommandContext<S> context, String name) {
        return context.getArgument(name, List.class);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
