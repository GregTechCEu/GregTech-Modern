package com.gregtechceu.gtceu.integration.kjs.builders.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.recipeviewer.CategoryIcon;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
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

    @Override
    public RegistryInfo<GTRecipeCategory> getRegistryType() {
        return GTRegistryInfo.RECIPE_CATEGORY;
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
        if (langValue != null) lang.add(object.getLanguageKey(), langValue);
        else lang.add(GTCEu.MOD_ID, object.getLanguageKey(), FormattingUtil.toEnglishName(object.name));
    }

    @Override
    public GTRecipeCategory createObject() {
        return new GTRecipeCategory(name, recipeType)
                .setIcon(icon)
                .setXEIVisible(isXEIVisible);
    }
}
