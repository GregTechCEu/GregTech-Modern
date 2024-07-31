package com.gregtechceu.gtceu.data.tag;

import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.lens;
import static com.gregtechceu.gtceu.data.material.GTMaterials.*;

public class ItemTagLoader {

    public static void init(RegistrateTagsProvider<Item> provider) {
        create(provider, lens, Color.White, GTItems.MATERIAL_ITEMS.get(lens, Glass).getId(),
                GTItems.MATERIAL_ITEMS.get(lens, NetherStar).getId());
        create(provider, lens, Color.LightBlue, GTItems.MATERIAL_ITEMS.get(lens, Diamond).getId());
        create(provider, lens, Color.Red, GTItems.MATERIAL_ITEMS.get(lens, Ruby).getId());
        create(provider, lens, Color.Green, GTItems.MATERIAL_ITEMS.get(lens, Emerald).getId());
        create(provider, lens, Color.Blue, GTItems.MATERIAL_ITEMS.get(lens, Sapphire).getId());

        create(provider, CustomTags.TAG_PISTONS, Items.PISTON, Items.STICKY_PISTON);

        create(provider, dye, Color.Brown, GTItems.MATERIAL_ITEMS.get(dust, MetalMixture).getId());

        // add treated wood stick to vanilla sticks tag
        // noinspection DataFlowIssue ChemicalHelper#getTag can't return null with treated wood rod
        provider.addTag(Tags.Items.RODS_WOODEN)
                .add(TagEntry.element(GTItems.MATERIAL_ITEMS.get(TagPrefix.rod, TreatedWood).getId()));
        // TODO add to planks mc tag?
        // for (Material material : new Material[]{GTMaterials.Wood, GTMaterials.TreatedWood}) {
        // for (ItemLike woodPlateStack : ChemicalHelper.getItems(new UnificationEntry(TagPrefix.plate, material))) {
        // ChemicalHelper.registerUnificationEntry(woodPlateStack, TagPrefix.plank, material);
        // }
        // }

        // todo match ae2 certus quartz tag
        // OreDictionary.registerUnificationEntry("crystalCertusQuartz", ChemicalHelper.get(TagPrefix.gem,
        // GTMaterials.CertusQuartz));
    }

    private static void create(RegistrateTagsProvider<Item> provider, String tagName, ResourceLocation... rls) {
        create(provider, TagUtil.createItemTag(tagName), rls);
    }

    private static void create(RegistrateTagsProvider<Item> provider, TagPrefix prefix, Material material,
                               ResourceLocation... rls) {
        create(provider, ChemicalHelper.getTag(prefix, material), rls);
    }

    public static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, ResourceLocation... rls) {
        var builder = provider.addTag(tagKey);
        for (ResourceLocation rl : rls) {
            builder.addOptional(rl);
        }
    }

    public static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, Item... rls) {
        var builder = provider.addTag(tagKey);
        for (Item item : rls) {
            builder.add(BuiltInRegistries.ITEM.getResourceKey(item).get());
        }
    }

    private static ResourceLocation rl(String name) {
        return ResourceLocation.parse(name);
    }
}
