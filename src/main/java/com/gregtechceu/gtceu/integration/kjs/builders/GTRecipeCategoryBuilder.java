package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
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
    @Nullable
    private transient String langValue;

    public GTRecipeCategoryBuilder(ResourceLocation id) {
        super(id);
        name = id.getPath();
        recipeType = GTRecipeTypes.DUMMY_RECIPES;
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
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        if (langValue != null) lang.add(get().getLanguageKey(), langValue);
        else lang.add(id.getNamespace(), get().getLanguageKey(), FormattingUtil.toEnglishName(get().name));
    }

    @Override
    public GTRecipeCategory createObject() {
        return GTRecipeCategories.register(id, recipeType)
                .setIcon(icon)
                .setXEIVisible(isXEIVisible);
    }
}
