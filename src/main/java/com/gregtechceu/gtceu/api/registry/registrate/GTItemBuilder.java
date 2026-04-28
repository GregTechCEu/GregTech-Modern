package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.client.color.GTItemColors;
import com.gregtechceu.gtceu.client.color.ItemColor;

import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.GeneratorType;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateRecipeProvider;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.nullness.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GTItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {

    private static final int MAX_LEGACY_TINT_LAYERS = 16;

    public static <T extends Item, P> GTItemBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
                                                                 BuilderCallback callback,
                                                                 NonNullFunction<Item.Properties, T> factory) {
        return new GTItemBuilder<>(owner, parent, name, callback, factory)
                .defaultModel().defaultLang();
    }

    protected GTItemBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                            NonNullFunction<Item.Properties, T> factory) {
        super(owner, parent, name, callback, factory);
    }

    public GTItemBuilder<T, P> color(NonNullSupplier<? extends ItemColor> colorHandler) {
        onRegister(item -> GTItemColors.register(item, colorHandler));
        return this;
    }

    @Override
    public GTItemBuilder<T, P> properties(NonNullUnaryOperator<Item.Properties> func) {
        return (GTItemBuilder<T, P>) super.properties(func);
    }

    @Override
    public GTItemBuilder<T, P> initialProperties(NonNullSupplier<Item.Properties> properties) {
        return (GTItemBuilder<T, P>) super.initialProperties(properties);
    }

    @Override
    public GTItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab, Consumer<CreativeModeTabModifier> modifier) {
        return (GTItemBuilder<T, P>) super.tab(tab, modifier);
    }

    @Override
    public GTItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab,
                                   NonNullBiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier> modifier) {
        return (GTItemBuilder<T, P>) super.tab(tab, modifier);
    }

    @Override
    public GTItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab) {
        return (GTItemBuilder<T, P>) super.tab(tab);
    }

    @Override
    public GTItemBuilder<T, P> removeTab(ResourceKey<CreativeModeTab> tab) {
        return (GTItemBuilder<T, P>) super.removeTab(tab);
    }

    @Override
    public GTItemBuilder<T, P> defaultModel() {
        return (GTItemBuilder<T, P>) super.defaultModel();
    }

    @Override
    public GTItemBuilder<T, P> model(NonNullSupplier<NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator>> cons) {
        return (GTItemBuilder<T, P>) super.model(cons);
    }

    public GTItemBuilder<T, P> model(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> cons) {
        return model(() -> cons);
    }

    public GTItemBuilder<T, P> defaultTintedModel() {
        return model(() -> (ctx, prov) -> prov.itemModelOutput.accept(ctx.get(), ItemModelUtils.tintedModel(
                ModelLocationUtils.getModelLocation(ctx.get()),
                createLegacyTintSources())));
    }

    @Override
    public GTItemBuilder<T, P> defaultLang() {
        return (GTItemBuilder<T, P>) super.defaultLang();
    }

    @Override
    public GTItemBuilder<T, P> lang(String name) {
        return (GTItemBuilder<T, P>) super.lang(name);
    }

    @Override
    public GTItemBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons) {
        return (GTItemBuilder<T, P>) super.recipe(cons);
    }

    @Override
    public GTItemBuilder<T, P> burnTime(int tick) {
        return (GTItemBuilder<T, P>) super.burnTime(tick);
    }

    @Override
    public GTItemBuilder<T, P> compostable(float chance) {
        return (GTItemBuilder<T, P>) super.compostable(chance);
    }

    @Override
    public GTItemBuilder<T, P> clientExtension(NonNullSupplier<Supplier<IClientItemExtensions>> clientExtension) {
        return (GTItemBuilder<T, P>) super.clientExtension(clientExtension);
    }

    @Override
    @Deprecated(forRemoval = true)
    public GTItemBuilder<T, P> clientExtension(Function<T, NonNullSupplier<Supplier<IClientItemExtensions>>> clientExtension) {
        return (GTItemBuilder<T, P>) super.clientExtension(clientExtension);
    }

    @Override
    public <D> GTItemBuilder<T, P> setData(GeneratorType<? extends D> type,
                                           NonNullBiConsumer<DataGenContext<Item, T>, D> cons) {
        return (GTItemBuilder<T, P>) super.setData(type, cons);
    }

    @Override
    public <D> GTItemBuilder<T, P> addMiscData(GeneratorType<? extends D> type, NonNullConsumer<? extends D> cons) {
        return (GTItemBuilder<T, P>) super.addMiscData(type, cons);
    }

    @Override
    public <D> GTItemBuilder<T, P> dataMap(DataMapType<Item, D> type, D val) {
        return (GTItemBuilder<T, P>) super.dataMap(type, val);
    }

    @Override
    public <D> GTItemBuilder<T, P> dataMap(DataMapType<Item, D> type,
                                           NonNullFunction<DataGenContext<Item, T>, D> factory) {
        return (GTItemBuilder<T, P>) super.dataMap(type, factory);
    }

    @Override
    public GTItemBuilder<T, P> onRegister(NonNullConsumer<? super T> callback) {
        return (GTItemBuilder<T, P>) super.onRegister(callback);
    }

    @SafeVarargs
    public final GTItemBuilder<T, P> gtTag(TagKey<Item>... tags) {
        tag(tags);
        return this;
    }

    private static net.minecraft.client.color.item.ItemTintSource[] createLegacyTintSources() {
        net.minecraft.client.color.item.ItemTintSource[] sources = new net.minecraft.client.color.item.ItemTintSource[MAX_LEGACY_TINT_LAYERS];
        for (int i = 0; i < MAX_LEGACY_TINT_LAYERS; i++) {
            sources[i] = new com.gregtechceu.gtceu.client.color.GTItemTintSource(i);
        }
        return sources;
    }
}
