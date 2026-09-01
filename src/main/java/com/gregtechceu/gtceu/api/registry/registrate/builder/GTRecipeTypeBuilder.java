package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class GTRecipeTypeBuilder extends AbstractBuilder<RecipeType<?>, GTRecipeType, GTRegistrate, GTRecipeTypeBuilder> {

    @Getter
    private final GTRecipeType.Properties properties;

    public GTRecipeTypeBuilder(GTRegistrate owner, String name, BuilderCallback callback, String group, RecipeType<?>... proxyRecipes) {
        super(owner, owner, name, callback, Registries.RECIPE_TYPE);
        this.properties = new GTRecipeType.Properties(group, proxyRecipes);
    }

    public GTRecipeTypeBuilder setMaxIOSize(int maxItemInputs, int maxItemOutputs, int maxFluidInputs, int maxFluidOutputs) {
        return setMaxSize(IO.IN, ItemRecipeCapability.CAP, maxItemInputs)
                .setMaxSize(IO.IN, FluidRecipeCapability.CAP, maxFluidInputs)
                .setMaxSize(IO.OUT, ItemRecipeCapability.CAP, maxItemOutputs)
                .setMaxSize(IO.OUT, FluidRecipeCapability.CAP, maxFluidOutputs);
    }

    public GTRecipeTypeBuilder setEUIO(IO io) {
        if (io.support(IO.IN)) {
            setMaxSize(IO.IN, EURecipeCapability.CAP, 1);
        }
        if (io.support(IO.OUT)) {
            setMaxSize(IO.OUT, EURecipeCapability.CAP, 1);
        }
        return this;
    }

    public GTRecipeTypeBuilder setMaxSize(IO io, RecipeCapability<?> cap, int max) {
        if (io == IO.IN || io == IO.BOTH) {
            properties.maxInputs().put(cap, max);
        }
        if (io == IO.OUT || io == IO.BOTH) {
            properties.maxOutputs().put(cap, max);
        }
        return this;
    }

    public GTRecipeTypeBuilder setIconSupplier(Supplier<ItemStack> iconSupplier) {
        properties.iconSupplier(iconSupplier);
        return this;
    }

    public GTRecipeTypeBuilder setSound(Holder<SoundEntry> sound) {
        properties.sound(sound);
        return this;
    }

    public GTRecipeTypeBuilder setSound(SoundEntry sound) {
        return setSound(Holder.direct(sound));
    }

    public GTRecipeTypeBuilder UI(UnaryOperator<GTRecipeTypeUILayout.Builder> builder) {
        properties.uiLayout(builder);
        return this;
    }

    public GTRecipeTypeBuilder setXEIVisible(boolean XEIVisible) {
        properties.recipeViewerCategoryVisible(XEIVisible);
        return this;
    }

    public GTRecipeTypeBuilder setMinRecipeConditions(int n) {
        properties.minRecipeConditions(Math.max(properties.minRecipeConditions(), n));
        return this;
    }

    public GTRecipeTypeBuilder setHasResearchSlot(boolean hasSlot) {
        properties.hasResearchSlot(hasSlot);
        return this;
    }

    public GTRecipeTypeBuilder setScanner(boolean scanner) {
        properties.isScanner(scanner);
        return this;
    }

    /**
     *
     * @param recipeLogic A function which is passed the normal findRecipe() result. Returns null if no valid recipe for
     *                    the custom logic is found.
     */
    public GTRecipeTypeBuilder addCustomRecipeLogic(GTRecipeType.ICustomRecipeLogic recipeLogic) {
        properties.customRecipeLogicRunners().add(recipeLogic);
        return this;
    }

    public GTRecipeTypeBuilder prepareBuilder(Consumer<GTRecipeBuilder> onPrepare) {
        properties.builderPreparer(onPrepare);
        return this;
    }

    public GTRecipeTypeBuilder onRecipeBuild(BiConsumer<GTRecipeBuilder, RecipeOutput> onBuild) {
        properties.onRecipeBuild(onBuild);
        return this;
    }

    @Override
    protected GTRecipeTypeEntry createEntryWrapper(DeferredHolder<RecipeType<?>, GTRecipeType> delegate) {
        return new GTRecipeTypeEntry(getOwner(), delegate);
    }

    @Override
    protected GTRecipeType createEntry() {
        return null;
    }

    @Override
    public GTRecipeTypeEntry register() {
        return (GTRecipeTypeEntry)super.register();
    }
}
