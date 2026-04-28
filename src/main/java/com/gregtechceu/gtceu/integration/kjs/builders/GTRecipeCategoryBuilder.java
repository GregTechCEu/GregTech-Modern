package com.gregtechceu.gtceu.integration.kjs.builders;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeCategoryIcons;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true, fluent = true)
public class GTRecipeCategoryBuilder extends BuilderBase<GTRecipeCategory> {

    @Setter
    private transient GTRecipeType recipeType;
    @Setter
    @Nullable
    private transient Object icon;
    @Setter
    private transient boolean isXEIVisible;
    @Setter
    @Nullable
    private transient String langValue;

    public GTRecipeCategoryBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        recipeType = GTRecipeTypes.DUMMY_RECIPES;
        icon = null;
        isXEIVisible = true;
        langValue = null;
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        if (langValue != null) lang.add(get().getLanguageKey(), langValue);
        else lang.add(id.getNamespace(), get().getLanguageKey(), FormattingUtil.toEnglishName(get().name));
    }

    @Override
    public GTRecipeCategory createObject() {
        return GTRecipeCategories.register(id.toIdentifier(), recipeType)
                .setIcon(icon)
                .setXEIVisible(isXEIVisible);
    }

    public GTRecipeCategoryBuilder setCustomIcon(Identifier location) {
        this.icon = GTRecipeCategoryIcons.resource(location);
        return this;
    }

    public GTRecipeCategoryBuilder setItemIcon(ItemStack... stacks) {
        this.icon = GTRecipeCategoryIcons.itemStack(stacks);
        return this;
    }
}
