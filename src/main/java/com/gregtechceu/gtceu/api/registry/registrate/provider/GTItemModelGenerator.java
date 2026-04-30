package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.gregtechceu.gtceu.utils.data.ExistingFileHelper;

import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public final class GTItemModelGenerator {

    private static final ExistingFileHelper EXISTING_FILE_HELPER = new ExistingFileHelper();

    private GTItemModelGenerator() {}

    public static String name(DataGenContext<Item, ? extends Item> context) {
        return name(context::getEntry);
    }

    public static String name(NonNullSupplier<? extends ItemLike> item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }

    public static Identifier modLoc(RegistrateItemModelGenerator provider, String path) {
        return provider.modLoc(path);
    }

    public static ItemModelBuilder getBuilder(RegistrateItemModelGenerator provider, String path) {
        Identifier id = path.contains(":") ? Identifier.parse(path) :
                provider.modLoc(path.contains("/") ? path : "item/" + path);
        return new ItemModelBuilder(id, EXISTING_FILE_HELPER);
    }

    public static ItemModelBuilder withExistingParent(RegistrateItemModelGenerator provider, String name,
                                                      Identifier parent) {
        return emit(provider, getBuilder(provider, name).parent(new ModelFile.UncheckedModelFile(parent)));
    }

    /**
     * Like {@link #withExistingParent(RegistrateItemModelGenerator, String, Identifier)} but also
     * emits the new-format item definition file (assets/{@code ns}/items/{@code name}.json) pointing
     * at the legacy item model. Without this, the auto-generated item definition would reference
     * {@code <ns>:block/<name>} (a path that does not exist for blocks whose models live elsewhere
     * — e.g. {@code <ns>:block/variant/<name>}).
     */
    public static ItemModelBuilder withExistingParent(RegistrateItemModelGenerator provider,
                                                      DataGenContext<Item, ? extends Item> context,
                                                      Identifier parent) {
        ItemModelBuilder builder = withExistingParent(provider, name(context), parent);
        provider.itemModelOutput.accept(context.getEntry(), ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public static ItemModelBuilder singleTexture(RegistrateItemModelGenerator provider, String name, Identifier parent,
                                                 String textureKey, Identifier texture) {
        return withExistingParent(provider, name, parent).texture(textureKey, texture);
    }

    public static ItemModelBuilder generated(RegistrateItemModelGenerator provider,
                                             DataGenContext<Item, ? extends Item> context) {
        return generated(provider, context, provider.modLoc("item/" + name(context)));
    }

    public static ItemModelBuilder generated(RegistrateItemModelGenerator provider,
                                             DataGenContext<Item, ? extends Item> context, Identifier layer0) {
        ItemModelBuilder builder = generated(provider, context::getEntry, layer0);
        provider.itemModelOutput.accept(context.getEntry(), ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public static ItemModelBuilder generated(RegistrateItemModelGenerator provider,
                                             NonNullSupplier<? extends Item> item, Identifier layer0) {
        return emit(provider, getBuilder(provider, name(item))
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", layer0));
    }

    public static ItemModelBuilder buttonInventory(RegistrateItemModelGenerator provider, String name,
                                                   Identifier texture) {
        return singleTexture(provider, name, provider.mcLoc("block/button_inventory"), "texture", texture);
    }

    public static ItemModelBuilder buttonInventory(RegistrateItemModelGenerator provider,
                                                   DataGenContext<Item, ? extends Item> context,
                                                   Identifier texture) {
        ItemModelBuilder builder = buttonInventory(provider, name(context), texture);
        provider.itemModelOutput.accept(context.getEntry(), ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public static ItemModelBuilder fenceInventory(RegistrateItemModelGenerator provider, String name,
                                                  Identifier texture) {
        return singleTexture(provider, name, provider.mcLoc("block/fence_inventory"), "texture", texture);
    }

    public static ItemModelBuilder fenceInventory(RegistrateItemModelGenerator provider,
                                                  DataGenContext<Item, ? extends Item> context,
                                                  Identifier texture) {
        ItemModelBuilder builder = fenceInventory(provider, name(context), texture);
        provider.itemModelOutput.accept(context.getEntry(), ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public static ItemModelBuilder trapdoorOrientableBottom(RegistrateItemModelGenerator provider, String name,
                                                            Identifier texture) {
        return singleTexture(provider, name, provider.mcLoc("block/template_orientable_trapdoor_bottom"), "texture",
                texture);
    }

    public static ItemModelBuilder trapdoorOrientableBottom(RegistrateItemModelGenerator provider,
                                                            DataGenContext<Item, ? extends Item> context,
                                                            Identifier texture) {
        ItemModelBuilder builder = trapdoorOrientableBottom(provider, name(context), texture);
        provider.itemModelOutput.accept(context.getEntry(), ItemModelUtils.plainModel(builder.getLocation()));
        return builder;
    }

    public static ItemModelBuilder emit(RegistrateItemModelGenerator provider, ItemModelBuilder builder) {
        provider.modelOutput.accept(builder.getLocation(), builder::toJson);
        return builder;
    }
}
