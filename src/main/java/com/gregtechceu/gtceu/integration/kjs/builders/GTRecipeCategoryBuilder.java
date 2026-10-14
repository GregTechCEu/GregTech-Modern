package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.client.LangEventJS;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public class GTRecipeCategoryBuilder extends BuilderBase<GTRecipeCategory> {

    private final transient String name;
    @Setter
    private transient GTRecipeType recipeType;
    @Setter
    private transient CategoryIcon icon;
    @Setter
    private transient boolean isXEIVisible;
    @Setter
    private transient String langValue;

    public GTRecipeCategoryBuilder(ResourceLocation id) {
        super(id);
        name = id.getPath();
        recipeType = null;
        icon = null;
        isXEIVisible = true;
        langValue = null;
    }

    public GTRecipeCategoryBuilder setCustomIcon(ResourceLocation location) {
        this.icon = new CategoryIcon(location);
        return this;
    }

    public GTRecipeCategoryBuilder setItemIcon(ItemStack stack) {
        this.icon = new CategoryIcon(stack);
        return this;
    }

    @Override
    public void generateLang(LangEventJS lang) {
        super.generateLang(lang);
        if (langValue != null) lang.add(value.getLanguageKey(), langValue);
        else lang.add(GTCEu.MOD_ID, value.getLanguageKey(), FormattingUtil.toEnglishName(value.name));
    }

    @Override
    public GTRecipeCategory register() {
        var category = GTRecipeCategories.register(name, recipeType)
                .setIcon(icon)
                .setXEIVisible(isXEIVisible);
        return value = category;
    }
}
