package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public record CapabilityMapComponent() implements RecipeComponent<CapabilityMap> {

    // spotless:off
    public static final Codec<CapabilityMap> CODEC = RecipeCapability.CODEC
            .xmap(CapabilityMap::new, Function.identity());
    public static final CapabilityMapComponent INSTANCE = new CapabilityMapComponent();
    public static final RecipeComponentType<CapabilityMap> CAPABILITY_MAP = RecipeComponentType.unit(ResourceLocation.parse("capability_map"), INSTANCE);
    // spotless:on

    @Override
    public Codec<CapabilityMap> codec() {
        return CODEC;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(CapabilityMap.class)
                .or(TypeInfo.RAW_MAP.withParams(
                        GTRecipeComponents.RECIPE_CAPABILITY.typeInfo(),
                        TypeInfo.RAW_LIST.withParams(TypeInfo.of(Content.class))));
    }

    @Override
    public String toString() {
        return "capability_map";
    }

    @Override
    public CapabilityMap replace(RecipeScriptContext cx, CapabilityMap original,
                                 ReplacementMatchInfo match, Object with) {
        AtomicBoolean changed = new AtomicBoolean(false);
        original.forEach((key, values) -> {
            var content = GTRecipeComponents.VALID_CAPS.get(key);
            for (int i = 0; i < values.size(); ++i) {
                Content value = values.get(i);
                Content result = content.replace(cx, value, match, with);
                if (!result.equals(value)) {
                    changed.set(true);
                    values.set(i, result);
                }
            }
        });
        return changed.get() ? new CapabilityMap(original) : original;
    }

    public @Override RecipeComponentType<CapabilityMap> type() {
        return CAPABILITY_MAP;
    }
}
