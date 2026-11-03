package com.gregtechceu.gtceu.integration.kjs.builders.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class GTRecipeCategoryBuilderJS extends BuilderBase<GTRecipeCategory> {

    private transient Supplier<GTRecipeType> recipeType;
    @Setter
    private transient CategoryIcon icon;
    @Setter
    private transient boolean isXEIVisible;
    @Setter
    @Nullable
    private transient String langValue;

    public GTRecipeCategoryBuilderJS(ResourceLocation id) {
        super(id);
        recipeType = GTRecipeTypes.DUMMY_RECIPES;
        icon = null;
        isXEIVisible = true;
        langValue = null;
    }

    public GTRecipeCategoryBuilderJS setRecipeType(Supplier<GTRecipeType> recipeType) {
        this.recipeType = recipeType;
        return this;
    }

    public GTRecipeCategoryBuilderJS setRecipeType(GTRecipeType recipeType) {
        return setRecipeType(() -> recipeType);
    }

    public GTRecipeCategoryBuilderJS setCustomIcon(ResourceLocation location) {
        this.icon = new CategoryIcon(location);
        return this;
    }

    public GTRecipeCategoryBuilderJS setItemIcon(ItemStack stack) {
        this.icon = new CategoryIcon(stack);
        return this;
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        if (langValue != null) lang.add(get().id.toLanguageKey(), langValue);
        else lang.add(id.getNamespace(), get().getLanguageKey(), FormattingUtil.toEnglishName(get().id.getPath()));
    }

    @Override
    public GTRecipeCategory createObject() {
        return new GTRecipeCategory(id, recipeType)
                .setIcon(icon)
                .setXEIVisible(isXEIVisible);
    }
}
