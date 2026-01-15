package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;

import java.util.List;

public class StringGridComponent extends SimpleRecipeComponent<List<String>> {

    private static final RecipeComponent<List<String>> PARENT = StringComponent.STRING.instance().asList();
    public static final RecipeComponentType<List<String>> STRING_GRID = RecipeComponentType
            .unit(KubeJS.id("string_grid"), StringGridComponent::new);

    private StringGridComponent(RecipeComponentType<?> type) {
        super(type, PARENT.codec(), PARENT.typeInfo());
    }

    @Override
    public boolean isEmpty(List<String> value) {
        return value.isEmpty();
    }

    @Override
    public List<String> wrap(RecipeScriptContext cx, Object from) {
        return PARENT.wrap(cx, from);
    }
}
